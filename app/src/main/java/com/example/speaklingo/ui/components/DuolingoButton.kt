package com.example.speaklingo.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuolingoGreen
import com.example.ui.theme.DuolingoGreenDark

@Composable
fun DuolingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonColor: Color = DuolingoGreen,
    shadowColor: Color = DuolingoGreenDark,
    textColor: Color = Color.White,
    height: Dp = 52.dp,
    testTag: String = "duolingo_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius = 16.dp
    val shadowOffset = if (isPressed) 1.dp else 4.dp
    val animatedShadowOffset by animateDpAsState(targetValue = shadowOffset, label = "button_depth")

    val actualBg = if (enabled) buttonColor else Color(0xFFE5E5E5)
    val actualShadow = if (enabled) shadowColor else Color(0xFFCCCCCC)
    val actualTextColor = if (enabled) textColor else Color(0xFFAAAAAA)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 4.dp)
            .testTag(testTag)
    ) {
        // Shadow base layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualShadow)
        )

        // Top button layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = if (enabled) animatedShadowOffset - 4.dp else 0.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(actualBg)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = actualTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
