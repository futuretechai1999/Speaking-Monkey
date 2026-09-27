package com.example.speaklingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueLight

@Composable
fun AudioPlayerButton(
    onPlayNormal: () -> Unit,
    onPlaySlow: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String = "audio_play_button"
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Normal Speed
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SpeakBlue)
                .clickable(onClick = onPlayNormal)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Listen normal speed",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Slow Speed (Turtle)
        if (onPlaySlow != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SpeakBlueLight)
                    .clickable(onClick = onPlaySlow)
                    .testTag("audio_slow_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐢",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
