package io.github.degipe.youtubewhitelist.ui.screen.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PinDots(
    pinLength: Int,
    maxLength: Int = 6,
    modifier: Modifier = Modifier,
    filledColor: Color = MaterialTheme.colorScheme.primary,
    emptyColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .then(
                        if (index < pinLength) {
                            Modifier.background(filledColor)
                        } else {
                            Modifier.border(2.dp, emptyColor, CircleShape)
                        }
                    )
            )
        }
    }
}
