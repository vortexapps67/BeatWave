/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beatwave.music.BuildConfig
import com.beatwave.music.R
import com.beatwave.music.constants.LastSeenChangelogVersionKey
import com.beatwave.music.ui.theme.AppleTokens
import com.beatwave.music.utils.rememberPreference

/**
 * Automatically prompts the user with a "What's New" changelog summary
 * when they install BeatWave or update to a new version.
 */
@Composable
fun WhatsNewPromptHost(
    navController: NavController? = null
) {
    var lastSeenVersion by rememberPreference(LastSeenChangelogVersionKey, "")
    val currentVersion = BuildConfig.VERSION_NAME

    var visible by remember(lastSeenVersion, currentVersion) {
        mutableStateOf(lastSeenVersion != currentVersion)
    }

    if (!visible) return

    fun dismiss() {
        visible = false
        lastSeenVersion = currentVersion
    }

    AlertDialog(
        onDismissRequest = ::dismiss,
        shape = RoundedCornerShape(AppleTokens.CardCornerLarge),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.update),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to BeatWave",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "What's New in Version $currentVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ChangelogHighlightItem(
                    emoji = "⚡",
                    title = "Instant Playback & Fast Lyrics",
                    description = "0ms song switching with background stream prefetching and ultra-fast parallel lyrics fetching."
                )

                ChangelogHighlightItem(
                    emoji = "👥",
                    title = "BeatWave Sync (Listen Together)",
                    description = "Real-time synchronized playback with friends, instant room share links, QR codes, and live in-room chat."
                )

                ChangelogHighlightItem(
                    emoji = "🎨",
                    title = "Revamped Liquid Glass Home",
                    description = "Dynamic time-of-day greeting, frosted glass cards, fluid animations, and high-density recommendation shelves."
                )

                ChangelogHighlightItem(
                    emoji = "🎵",
                    title = "Spotify Recommendation Engine",
                    description = "Smart algorithmic discovery feeds delivering real-time trending tracks, curated mixes, and verified regional playlists."
                )

                ChangelogHighlightItem(
                    emoji = "🎛️",
                    title = "Audio Presets & Custom Themes",
                    description = "Tailored equalizer presets (SoundCloud style, Bass Boost) and OLED / Liquid Glass theme color presets."
                )

                ChangelogHighlightItem(
                    emoji = "📡",
                    title = "Google Cast & Library Covers",
                    description = "One-tap Chromecast audio streaming directly from the player and dynamic cover art across library playlists."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = ::dismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Let's Explore!")
            }
        },
        dismissButton = {
            if (navController != null) {
                TextButton(
                    onClick = {
                        dismiss()
                        navController.navigate("settings/changelog")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Full Changelog")
                }
            }
        }
    )
}

@Composable
private fun ChangelogHighlightItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
