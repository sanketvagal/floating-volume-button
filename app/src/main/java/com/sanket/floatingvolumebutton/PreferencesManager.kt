package com.sanket.floatingvolumebutton

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            KEY_IS_ENABLED -> _isEnabled.value = sharedPreferences.getBoolean(KEY_IS_ENABLED, false)
            KEY_SIZE -> _size.value = sharedPreferences.getInt(KEY_SIZE, DEFAULT_SIZE)
            KEY_OPACITY -> _opacity.value = sharedPreferences.getFloat(KEY_OPACITY, DEFAULT_OPACITY)
            KEY_COLOR_OUTSIDE -> _colorOutside.value = sharedPreferences.getInt(KEY_COLOR_OUTSIDE, DEFAULT_COLOR_OUTSIDE)
            KEY_COLOR_INSIDE -> _colorInside.value = sharedPreferences.getInt(KEY_COLOR_INSIDE, DEFAULT_COLOR_INSIDE)
            KEY_STICK_TO_EDGES -> _stickToEdges.value = sharedPreferences.getBoolean(KEY_STICK_TO_EDGES, DEFAULT_STICK_TO_EDGES)
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    companion object {
        const val KEY_IS_ENABLED = "is_enabled"
        const val KEY_SIZE = "button_size"
        const val KEY_OPACITY = "button_opacity"
        const val KEY_COLOR_OUTSIDE = "color_outside"
        const val KEY_COLOR_INSIDE = "color_inside"
        const val KEY_STICK_TO_EDGES = "stick_to_edges"

        const val DEFAULT_SIZE = 60
        const val DEFAULT_OPACITY = 0.7f
        val DEFAULT_COLOR_OUTSIDE = Color.Blue.toArgb()
        val DEFAULT_COLOR_INSIDE = Color.White.toArgb()
        const val DEFAULT_STICK_TO_EDGES = false
    }

    private val _isEnabled = MutableStateFlow(sharedPreferences.getBoolean(KEY_IS_ENABLED, false))
    val isEnabled: StateFlow<Boolean> = _isEnabled

    private val _size = MutableStateFlow(sharedPreferences.getInt(KEY_SIZE, DEFAULT_SIZE))
    val size: StateFlow<Int> = _size

    private val _opacity = MutableStateFlow(sharedPreferences.getFloat(KEY_OPACITY, DEFAULT_OPACITY))
    val opacity: StateFlow<Float> = _opacity

    private val _colorOutside = MutableStateFlow(sharedPreferences.getInt(KEY_COLOR_OUTSIDE, DEFAULT_COLOR_OUTSIDE))
    val colorOutside: StateFlow<Int> = _colorOutside

    private val _colorInside = MutableStateFlow(sharedPreferences.getInt(KEY_COLOR_INSIDE, DEFAULT_COLOR_INSIDE))
    val colorInside: StateFlow<Int> = _colorInside

    private val _stickToEdges = MutableStateFlow(sharedPreferences.getBoolean(KEY_STICK_TO_EDGES, DEFAULT_STICK_TO_EDGES))
    val stickToEdges: StateFlow<Boolean> = _stickToEdges

    fun setEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_ENABLED, enabled).apply()
        _isEnabled.value = enabled
    }

    fun getSize(): Int = size.value

    fun setSize(size: Int) {
        sharedPreferences.edit().putInt(KEY_SIZE, size).apply()
        _size.value = size
    }

    fun getOpacity(): Float = opacity.value

    fun setOpacity(opacity: Float) {
        sharedPreferences.edit().putFloat(KEY_OPACITY, opacity).apply()
        _opacity.value = opacity
    }

    fun getColorOutside(): Int = colorOutside.value

    fun setColorOutside(color: Int) {
        sharedPreferences.edit().putInt(KEY_COLOR_OUTSIDE, color).apply()
        _colorOutside.value = color
    }

    fun getColorInside(): Int = colorInside.value

    fun setColorInside(color: Int) {
        sharedPreferences.edit().putInt(KEY_COLOR_INSIDE, color).apply()
        _colorInside.value = color
    }

    fun setStickToEdges(stick: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_STICK_TO_EDGES, stick).apply()
        _stickToEdges.value = stick
    }
}
