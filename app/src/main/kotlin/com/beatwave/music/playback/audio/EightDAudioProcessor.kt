/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * "8D audio": the track orbits the listener's head instead of sitting between
 * the speakers. Stereo 16-bit PCM only; any other format passes through.
 *
 * Two cues are combined, because the popular effect needs both to read as
 * position rather than as a volume wobble:
 *
 *  - **Equal-power panning.** Gains follow cos/sin of the pan angle rather than
 *    a linear ramp, so total power is constant as the image sweeps. A linear
 *    pan audibly dips through the centre, which is what makes naive autopan
 *    sound like the volume is pumping.
 *  - **Interaural time difference.** The far ear is delayed by up to
 *    [MAX_ITD_US] microseconds. This is the dominant cue the auditory system
 *    uses to place a source below ~1.5kHz; without it the sound stays "in the
 *    head" and only seems louder on one side. It is why this sounds like
 *    rotation on headphones and a plain autopan does not.
 *
 * Headphones only, really — on speakers the two ears hear both channels and the
 * ITD cue collapses.
 *
 * The processor stays active whenever the format is usable and passes audio
 * through untouched while disabled, so toggling it never reconfigures the audio
 * sink mid-track.
 */
@UnstableApi
class EightDAudioProcessor : AudioProcessor {

    /** Master switch. Off = bit-exact passthrough. */
    @Volatile
    var enabled: Boolean = false

    /** Full orbits per second. 0.125 ≈ one lap every 8 seconds. */
    @Volatile
    var rotationHz: Float = 0.125f

    /** How far toward each ear the image travels, 0..1. */
    @Volatile
    var depth: Float = 1f

    private var sampleRate = 0
    private var channelCount = 0

    /** LFO phase in radians, advanced per frame and wrapped to keep precision. */
    private var phase = 0.0

    /**
     * Depth actually applied, chased toward [depth] per frame. Jumping straight
     * to a new value on toggle steps the gains and the delay read position at
     * once, which clicks; this ramps over [RAMP_MS].
     */
    private var appliedDepth = 0f

    /** Per-channel ring buffer holding the last [maxItdSamples] + 1 samples. */
    private var itdBuffer: ShortArray = ShortArray(0)
    private var itdCapacity = 0
    private var itdWriteIndex = 0
    private var maxItdSamples = 0

    private var outputBuffer: ByteBuffer = EMPTY_BUFFER
    private var inputEnded = false

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        val usable = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT &&
            inputAudioFormat.channelCount == CHANNELS_STEREO &&
            inputAudioFormat.sampleRate > 0

        if (!usable) {
            sampleRate = 0
            channelCount = 0
            itdBuffer = ShortArray(0)
            return inputAudioFormat
        }

        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        maxItdSamples = ((sampleRate * MAX_ITD_US) / 1_000_000.0).roundToInt().coerceAtLeast(1)
        itdCapacity = maxItdSamples + 1
        itdBuffer = ShortArray(itdCapacity * channelCount)
        itdWriteIndex = 0
        phase = 0.0
        appliedDepth = 0f
        return inputAudioFormat
    }

    override fun isActive(): Boolean = sampleRate != 0 && channelCount == CHANNELS_STEREO

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!inputBuffer.hasRemaining()) {
            outputBuffer = EMPTY_BUFFER
            return
        }
        inputBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val out = replaceOutputBuffer(inputBuffer.remaining())
        out.order(ByteOrder.LITTLE_ENDIAN)

        val targetDepth = if (enabled) depth.coerceIn(0f, 1f) else 0f

        // Fully off and already ramped out: hand the bytes straight back.
        if (targetDepth == 0f && appliedDepth == 0f || itdBuffer.isEmpty()) {
            out.put(inputBuffer)
            out.flip()
            return
        }

        val frameCount = inputBuffer.remaining() / BYTES_PER_SAMPLE / channelCount
        val basePosition = inputBuffer.position()
        val phaseStep = 2.0 * PI * rotationHz / sampleRate
        val rampStep = 1f / (sampleRate * RAMP_MS / 1000f).coerceAtLeast(1f)

        repeat(frameCount) { frame ->
            appliedDepth = when {
                appliedDepth < targetDepth -> (appliedDepth + rampStep).coerceAtMost(targetDepth)
                appliedDepth > targetDepth -> (appliedDepth - rampStep).coerceAtLeast(targetDepth)
                else -> appliedDepth
            }

            val left = inputBuffer.getShort(basePosition + (frame * channelCount) * BYTES_PER_SAMPLE)
            val right = inputBuffer.getShort(basePosition + (frame * channelCount + 1) * BYTES_PER_SAMPLE)

            itdBuffer[itdWriteIndex * channelCount] = left
            itdBuffer[itdWriteIndex * channelCount + 1] = right

            // -1 fully left, +1 fully right.
            val pan = (sin(phase) * appliedDepth).toFloat().coerceIn(-1f, 1f)

            // Whichever ear the image is turned away from hears it later.
            val farEarDelay = (abs(pan) * maxItdSamples).roundToInt()
            val leftDelay = if (pan > 0f) farEarDelay else 0
            val rightDelay = if (pan < 0f) farEarDelay else 0

            val delayedLeft = itdBuffer[readIndex(leftDelay) * channelCount]
            val delayedRight = itdBuffer[readIndex(rightDelay) * channelCount + 1]

            // Equal power: angle sweeps 0..π/2, so cos²+sin² keeps total power flat.
            val angle = (pan + 1f) * (PI / 4.0)
            val gainLeft = cos(angle).toFloat()
            val gainRight = sin(angle).toFloat()

            out.putShort(scale(delayedLeft, gainLeft))
            out.putShort(scale(delayedRight, gainRight))

            itdWriteIndex = (itdWriteIndex + 1) % itdCapacity
            phase += phaseStep
            if (phase >= TWO_PI) phase -= TWO_PI
        }

        // Mark the input as consumed. The reads above are absolute
        // (getShort(index)), which does not move the position, and
        // AudioProcessingPipeline decides whether a processor accepted the data
        // by checking hasRemaining() afterwards. Without this the same chunk is
        // handed back on every pass and the last few milliseconds repeat
        // forever — audible as a continuous buzz rather than the track.
        inputBuffer.position(inputBuffer.limit())

        out.flip()
    }

    private fun readIndex(delaySamples: Int): Int =
        (itdWriteIndex - delaySamples + itdCapacity) % itdCapacity

    private fun scale(sample: Short, gain: Float): Short =
        (sample * gain).roundToInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            .toShort()

    override fun queueEndOfStream() {
        inputEnded = true
    }

    override fun getOutput(): ByteBuffer {
        val output = outputBuffer
        outputBuffer = EMPTY_BUFFER
        return output
    }

    override fun isEnded(): Boolean = inputEnded && outputBuffer === EMPTY_BUFFER

    @Deprecated("Deprecated in AudioProcessor")
    override fun flush() {
        outputBuffer = EMPTY_BUFFER
        inputEnded = false
        itdBuffer.fill(0)
        itdWriteIndex = 0
        phase = 0.0
        // Deliberately NOT resetting `enabled`: flush runs on every seek and
        // track change, and the user's choice has to survive both.
        appliedDepth = 0f
    }

    @Deprecated("Deprecated in AudioProcessor")
    override fun reset() {
        flush()
        sampleRate = 0
        channelCount = 0
        itdBuffer = ShortArray(0)
        itdCapacity = 0
        maxItdSamples = 0
    }

    private fun replaceOutputBuffer(size: Int): ByteBuffer {
        if (outputBuffer.capacity() < size) {
            outputBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        } else {
            outputBuffer.clear()
        }
        return outputBuffer
    }

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
        private const val BYTES_PER_SAMPLE = 2
        private const val CHANNELS_STEREO = 2
        private const val TWO_PI = 2.0 * PI

        /**
         * Widest interaural delay a human head produces, ear to ear (~23cm at
         * 343m/s). Going beyond it does not widen the image, it just smears
         * transients and starts to sound like a short echo.
         */
        private const val MAX_ITD_US = 660.0

        /** Gain/delay ramp time when the effect is toggled, in milliseconds. */
        private const val RAMP_MS = 120f

        /** Slowest and fastest orbit offered in the UI. */
        const val MIN_ROTATION_HZ = 0.05f
        const val MAX_ROTATION_HZ = 0.40f

        /**
         * Default swing width. Not 1.0: at full depth the far channel's gain
         * reaches zero, so the track drops out of one ear entirely, which is
         * more dramatic than musical. This keeps a little signal in both ears
         * through the whole orbit.
         */
        const val DEFAULT_DEPTH = 0.85f
    }
}
