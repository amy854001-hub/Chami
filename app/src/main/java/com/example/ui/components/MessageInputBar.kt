package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelTier
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiPurple

@Composable
fun MessageInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isGenerating: Boolean,
    selectedTier: ModelTier,
    isImageModeActive: Boolean,
    onToggleImageMode: () -> Unit,
    selectedAspectRatio: String,
    onSelectAspectRatio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Image generation control bar (active for Flash 3.7.0 or when toggled)
        AnimatedVisibility(visible = isImageModeActive) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = selectedTier.color.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, selectedTier.color.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = selectedTier.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Flash 3.7.0 Visual Mode",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = selectedTier.color
                        )
                    }

                    // Aspect ratio chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1:1", "16:9", "9:16").forEach { ratio ->
                            val isSelected = selectedAspectRatio == ratio
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) selectedTier.color else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isSelected) selectedTier.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectAspectRatio(ratio) }
                                    .testTag("aspect_ratio_chip_${ratio.replace(":", "_")}")
                            ) {
                                Text(
                                    text = ratio,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main pill input container
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                1.dp,
                if (isImageModeActive) selectedTier.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image Generation Button (prominently available for Flash 3.7.0)
                IconButton(
                    onClick = onToggleImageMode,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("image_generation_button")
                ) {
                    val iconTint = if (isImageModeActive) {
                        selectedTier.color
                    } else if (selectedTier == ModelTier.FLASH_3_7) {
                        selectedTier.color
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }

                    Box(
                        modifier = if (isImageModeActive) {
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(selectedTier.color.copy(alpha = 0.2f))
                        } else Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Image Generation",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Input text field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    placeholder = {
                        Text(
                            text = if (isImageModeActive) {
                                "Describe the image to generate..."
                            } else {
                                "Ask Charmi (${selectedTier.displayName})..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    maxLines = 4,
                    singleLine = false
                )

                // Clear input button
                if (inputText.isNotBlank()) {
                    IconButton(
                        onClick = { onInputTextChange("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Send Button
                val canSend = inputText.isNotBlank() && !isGenerating
                IconButton(
                    onClick = onSendMessage,
                    enabled = canSend,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("send_message_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = selectedTier.color
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) {
                                        Brush.linearGradient(
                                            listOf(selectedTier.color, GeminiPurple)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
