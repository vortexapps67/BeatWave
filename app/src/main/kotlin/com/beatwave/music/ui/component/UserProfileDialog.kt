/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.beatwave.music.R
import java.io.File

@Composable
fun UserProfileEditDialog(
    currentNickname: String,
    currentProfilePicUri: String,
    onSave: (nickname: String, profilePicUri: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tempNickname by rememberSaveable { mutableStateOf(currentNickname) }
    var tempPicUri by rememberSaveable { mutableStateOf(currentProfilePicUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { pickedUri ->
        pickedUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val destFile = File(context.filesDir, "user_profile_avatar.jpg")
                    destFile.outputStream().use { output -> input.copyTo(output) }
                    tempPicUri = destFile.absolutePath
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DefaultDialog(
        onDismiss = onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    onSave(tempNickname.trim(), tempPicUri)
                    onDismiss()
                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Edit Profile & Nickname",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E26))
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        photoPickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (tempPicUri.isNotEmpty() && File(tempPicUri).exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(tempPicUri))
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else if (tempNickname.isNotBlank()) {
                    Text(
                        text = tempNickname.trim().take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.person),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = { photoPickerLauncher.launch("image/*") }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.image),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change Photo", fontSize = 13.sp)
                }

                if (tempPicUri.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            tempPicUri = ""
                            runCatching { File(context.filesDir, "user_profile_avatar.jpg").delete() }
                        }
                    ) {
                        Text("Remove", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = tempNickname,
                onValueChange = { tempNickname = it },
                label = { Text("Your Nickname") },
                placeholder = { Text("e.g. Aarav, BeatMaster") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
