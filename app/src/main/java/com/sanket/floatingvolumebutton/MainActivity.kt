package com.sanket.floatingvolumebutton

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.sanket.floatingvolumebutton.ui.theme.FloatingVolumeButtonTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        enableEdgeToEdge()
        setContent {
            FloatingVolumeButtonTheme {
                val isEnabled by preferencesManager.isEnabled.collectAsState()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        isEnabled = isEnabled,
                        onToggleService = { enabled ->
                            if (enabled) {
                                checkAndStartService()
                            } else {
                                stopFloatingService()
                            }
                        },
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }

    private fun checkAndStartService() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        preferencesManager.setEnabled(true)
        val intent = Intent(this, FloatingVolumeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFloatingService() {
        preferencesManager.setEnabled(false)
        stopService(Intent(this, FloatingVolumeService::class.java))
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onToggleService: (Boolean) -> Unit,
    preferencesManager: PreferencesManager
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Floating Volume Button",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Enable Service", style = MaterialTheme.typography.titleLarge)
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleService
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Enable stick to edges
        val stickToEdges by preferencesManager.stickToEdges.collectAsState()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Stick to Edges",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEnabled) Color.Unspecified else Color.Gray
            )
            Switch(
                checked = stickToEdges,
                onCheckedChange = { preferencesManager.setStickToEdges(it) },
                enabled = isEnabled
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Size Setting
        val currentSize by preferencesManager.size.collectAsState()
        var size by remember(currentSize) { mutableFloatStateOf(currentSize.toFloat()) }
        Text(
            text = "Button Size: ${size.toInt()} dp",
            color = if (isEnabled) Color.Unspecified else Color.Gray
        )
        Slider(
            value = size,
            onValueChange = {
                size = it
                preferencesManager.setSize(it.toInt())
            },
            valueRange = 40f..120f,
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opacity Setting
        val currentOpacity by preferencesManager.opacity.collectAsState()
        var opacity by remember(currentOpacity) { mutableFloatStateOf(currentOpacity) }
        Text(
            text = "Opacity: ${(opacity * 100).toInt()}%",
            color = if (isEnabled) Color.Unspecified else Color.Gray
        )
        Slider(
            value = opacity,
            onValueChange = {
                opacity = it
                preferencesManager.setOpacity(it)
            },
            valueRange = 0.1f..1.0f,
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Outside Color
        val outsideColor by preferencesManager.colorOutside.collectAsState()
        Text(
            text = "Outside Color",
            color = if (isEnabled) Color.Unspecified else Color.Gray
        )
        ColorPickerRow(
            selectedColor = Color(outsideColor),
            onColorSelected = {
                preferencesManager.setColorOutside(it.toArgb())
            },
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Inside Color
        val insideColor by preferencesManager.colorInside.collectAsState()
        Text(
            text = "Speaker Icon Color",
            color = if (isEnabled) Color.Unspecified else Color.Gray
        )
        ColorPickerRow(
            selectedColor = Color(insideColor),
            onColorSelected = {
                preferencesManager.setColorInside(it.toArgb())
            },
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Note: Requires 'Display over other apps' permission.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ColorPickerRow(selectedColor: Color, onColorSelected: (Color) -> Unit, enabled: Boolean = true) {
    val colors = listOf(
        Color.Blue, Color.Red, Color.Green, Color.Yellow,
        Color.Black, Color.White, Color.Gray, Color.Cyan, Color.Magenta
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = if (enabled) 1f else 0.5f), CircleShape)
                    .clickable(enabled = enabled) { onColorSelected(color) }
                    .padding(4.dp)
            ) {
                if (color == selectedColor) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent, CircleShape)
                            .padding(4.dp)
                    ) {
                        // indicator for selected
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (color == Color.White) Color.Black else Color.White,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
