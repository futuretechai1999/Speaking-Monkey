package com.example.speaklingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HeartRed
import com.example.ui.theme.HeartRedDark
import com.example.ui.theme.HeartRedLight
import com.example.ui.theme.SpeakBlue
import com.example.ui.theme.SpeakBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRefillDialog(
    currentHearts: Int,
    onRefillWithPractice: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .testTag("hearts_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(HeartRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "❤️", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$currentHearts / 5 Hearts",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = HeartRed
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hearts allow you to make mistakes during lessons without stopping! Practice speaking or listening to restore your full energy.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                DuolingoButton(
                    text = "Refill Hearts (Full 5 ❤️)",
                    onClick = {
                        onRefillWithPractice()
                        onDismiss()
                    },
                    buttonColor = HeartRed,
                    shadowColor = HeartRedDark,
                    testTag = "refill_hearts_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                DuolingoButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    buttonColor = Color.LightGray,
                    shadowColor = Color.Gray,
                    textColor = Color.DarkGray,
                    testTag = "cancel_hearts_button"
                )
            }
        }
    }
}
