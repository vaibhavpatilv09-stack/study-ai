package com.example.ui.screens.focus

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AmbientSound
import com.example.ui.viewmodel.FocusTimerState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FocusModeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val isDark = uiState.focusThemeIsDark
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val minutes = uiState.focusSecondsRemaining / 60
    val seconds = uiState.focusSecondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val totalSeconds = uiState.focusDurationMinutes * 60
    val progress = if (totalSeconds > 0) (totalSeconds - uiState.focusSecondsRemaining).toFloat() / totalSeconds.toFloat() else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Exit button & Theme toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleFocusMode(false) },
                    modifier = Modifier.testTag("exit_focus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Focus Mode",
                        tint = textColor
                    )
                }

                Text(
                    text = "DEEP FOCUS IMMERSION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = subTextColor
                )

                IconButton(
                    onClick = { viewModel.toggleFocusTheme() },
                    modifier = Modifier.testTag("toggle_focus_theme_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = textColor
                    )
                }
            }

            // Middle: Animated Circular Timer Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color.White)
                        .border(
                            2.dp,
                            if (isDark) IndigoPrimary.copy(alpha = 0.3f) else IndigoLight.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(240.dp),
                        strokeWidth = 8.dp,
                        color = CyanSecondary,
                        trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = when (uiState.focusTimerState) {
                                FocusTimerState.RUNNING -> "ACTIVE FOCUS SESSION"
                                FocusTimerState.PAUSED -> "SESSION PAUSED"
                                FocusTimerState.COMPLETED -> "SESSION COMPLETED"
                                FocusTimerState.IDLE -> "READY TO FOCUS"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (uiState.focusTimerState == FocusTimerState.RUNNING) CyanSecondary else subTextColor
                        )
                    }
                }

                // Ambient Soundscape Selector
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AmbientSound.entries.forEach { sound ->
                            val isSelected = uiState.selectedAmbientSound == sound
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.selectAmbientSound(sound) },
                                color = if (isSelected) IndigoPrimary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else subTextColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = sound.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else textColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Duration Selector (15 / 25 / 45 / 60)
                if (uiState.focusTimerState == FocusTimerState.IDLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf(15, 25, 45, 60).forEach { mins ->
                            val isSelected = uiState.focusDurationMinutes == mins
                            OutlinedButton(
                                onClick = { viewModel.setFocusDuration(mins) },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) IndigoPrimary else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else textColor
                                )
                            ) {
                                Text(text = "${mins}m")
                            }
                        }
                    }
                }
            }

            // Bottom Controls: Start / Pause / Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.focusTimerState == FocusTimerState.RUNNING) {
                    Button(
                        onClick = { viewModel.pauseFocusTimer() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("pause_timer_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Pause Session", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.startFocusTimer() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("start_timer_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (uiState.focusTimerState == FocusTimerState.PAUSED) "Resume Focus" else "Start Focus Work", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalIconButton(
                    onClick = { viewModel.resetFocusTimer() },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("reset_timer_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Timer")
                }
            }
        }
    }
}
