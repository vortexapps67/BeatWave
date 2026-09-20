/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.screens

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.beatwave.music.LocalPlayerConnection
import com.beatwave.music.R
import com.beatwave.music.constants.AodDimLevelKey
import com.beatwave.music.constants.AodPixelShiftEnabledKey
import com.beatwave.music.extensions.togglePlayPause
import com.beatwave.music.models.MediaMetadata
import com.beatwave.music.ui.component.AmbientEdgeGlow
import com.beatwave.music.utils.makeTimeString
import com.beatwave.music.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * AOD Mode (Always-On Display & Standby Clock)
 *
 * Provides a pure black, power-efficient AMOLED standby player featuring:
 * - Dynamic digital clock, date, and live battery status
 * - Glowing breathing album art with halo
 * - Anti-burn-in periodic pixel shifting (every 60s)
 * - Intuitive gestures: tap to wake controls, double-tap to play/pause, swipe to skip tracks
 */
@Composable
fun AodScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()

    val pixelShiftEnabled by rememberPreference(AodPixelShiftEnabledKey, true)
    val (dimLevel, onDimLevelChange) = rememberPreference(AodDimLevelKey, 0.75f)

    // Current time and date
    var currentTimeText by remember { mutableStateOf("") }
    var currentDateText by remember { mutableStateOf("") }
    var batteryPercentage by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    // Pixel shift offsets (AMOLED anti burn-in)
    var shiftX by remember { mutableFloatStateOf(0f) }
    var shiftY by remember { mutableFloatStateOf(0f) }

    // Overlay controls visibility timer (auto-hide after 5 seconds)
    var showControls by remember { mutableStateOf(false) }

    // Fullscreen and Wake Lock
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Battery Broadcast Receiver
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent != null) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryPercentage = ((level / scale.toFloat()) * 100).toInt()
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val registered = context.registerReceiver(receiver, filter)
        if (registered != null) {
            val level = registered.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = registered.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                batteryPercentage = ((level / scale.toFloat()) * 100).toInt()
            }
            val status = registered.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Clock & Pixel Shift Periodic Update
    LaunchedEffect(pixelShiftEnabled) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM")
        var shiftStep = 0

        while (isActive) {
            val nowTime = LocalTime.now()
            val nowDate = LocalDate.now()
            currentTimeText = nowTime.format(timeFormatter)
            currentDateText = nowDate.format(dateFormatter)

            if (pixelShiftEnabled) {
                // Circular drift every step
                shiftStep = (shiftStep + 1) % 8
                val angle = shiftStep * (Math.PI / 4.0)
                shiftX = (kotlin.math.cos(angle) * 14f).toFloat()
                shiftY = (kotlin.math.sin(angle) * 14f).toFloat()
            } else {
                shiftX = 0f
                shiftY = 0f
            }

            delay(1000L)
        }
    }

    // Controls Auto-Hide timer
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000L)
            showControls = false
        }
    }

    BackHandler {
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AodArtHalo")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    },
                    onDoubleTap = {
                        playerConnection.player.togglePlayPause()
                    }
                )
            }
            .pointerInput(Unit) {
                var totalDragX = 0f
                detectDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX < -120f) {
                            playerConnection.player.seekToNextMediaItem()
                        } else if (totalDragX > 120f) {
                            playerConnection.player.seekToPreviousMediaItem()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                    }
                )
            }
    ) {
        // Content container with Pixel Shift offset
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(shiftX.roundToInt(), shiftY.roundToInt()) }
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Digital Clock, Date & Battery
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currentTimeText,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White.copy(alpha = (dimLevel * 0.9f).coerceIn(0.2f, 1f)),
                    letterSpacing = 2.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentDateText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = (dimLevel * 0.6f).coerceIn(0.15f, 0.8f))
                    )

                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isCharging) R.drawable.bolt else R.drawable.battery_full
                            ),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = (dimLevel * 0.6f).coerceIn(0.15f, 0.8f)),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$batteryPercentage%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = (dimLevel * 0.6f).coerceIn(0.15f, 0.8f))
                        )
                    }
                }
            }

            // Center Section: Glowing Artwork & Song Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Album Art with Ambient Breathing Halo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .drawBehind {
                            if (isPlaying) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = haloAlpha * 0.25f),
                                            Color.Transparent
                                        ),
                                        radius = size.width * 0.75f
                                    ),
                                    radius = size.width * 0.75f
                                )
                            }
                        }
                ) {
                    if (mediaMetadata?.thumbnailUrl != null) {
                        AsyncImage(
                            model = mediaMetadata?.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF181818)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.music_note),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Track Title & Artist with Marquee
                Text(
                    text = mediaMetadata?.title ?: "No track playing",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = (dimLevel * 0.95f).coerceIn(0.3f, 1f)),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .basicMarquee()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mediaMetadata?.artists?.joinToString { it.name } ?: "BeatWave",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = (dimLevel * 0.6f).coerceIn(0.2f, 0.75f)),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .basicMarquee()
                )
            }

            // Bottom Section: Minimal playback controls & exit button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    IconButton(
                        onClick = { playerConnection.player.seekToPreviousMediaItem() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_previous),
                            contentDescription = "Previous",
                            tint = Color.White.copy(alpha = (dimLevel * 0.75f).coerceIn(0.25f, 0.9f)),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { playerConnection.player.togglePlayPause() }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isPlaying) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = "Play/Pause",
                            tint = Color.White.copy(alpha = (dimLevel * 0.95f).coerceIn(0.4f, 1f)),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { playerConnection.player.seekToNextMediaItem() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = "Next",
                            tint = Color.White.copy(alpha = (dimLevel * 0.75f).coerceIn(0.25f, 0.9f)),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Text(
                    text = "Double tap to Play/Pause • Swipe to Skip • Tap for options",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Top Overlay Bar (shown on tap)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = "Exit AOD",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.brightness_medium),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${(dimLevel * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
