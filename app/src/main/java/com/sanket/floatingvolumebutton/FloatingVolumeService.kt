package com.sanket.floatingvolumebutton

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.sanket.floatingvolumebutton.ui.FloatingButton

class FloatingVolumeService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var composeView: ComposeView
    private lateinit var audioManager: AudioManager
    private lateinit var preferencesManager: PreferencesManager
    private var isAtEdge = true

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = _viewModelStore

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        preferencesManager = PreferencesManager(this)

        createNotificationChannel()
        
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

        setupFloatingButton()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_STICKY
    }

    private fun setupFloatingButton() {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingVolumeService)
            setViewTreeViewModelStoreOwner(this@FloatingVolumeService)
            setViewTreeSavedStateRegistryOwner(this@FloatingVolumeService)
            setContent {
                val size by preferencesManager.size.collectAsState()
                val opacity by preferencesManager.opacity.collectAsState()
                val outsideColor by preferencesManager.colorOutside.collectAsState()
                val insideColor by preferencesManager.colorInside.collectAsState()
                val stickToEdges by preferencesManager.stickToEdges.collectAsState()
                val dragToDismiss by preferencesManager.dragToDismiss.collectAsState()

                val screenWidth = resources.displayMetrics.widthPixels

                LaunchedEffect(size) {
                    windowManager.updateViewLayout(this@apply, params)
                }

                FloatingButton(
                    size = size,
                    opacity = opacity,
                    outsideColor = Color(outsideColor),
                    insideColor = Color(insideColor),
                    onClick = {
                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_SAME,
                            AudioManager.FLAG_SHOW_UI
                        )
                    },
                    onDragStart = {
                        if (isAtEdge) {
                            performHaptic()
                            isAtEdge = false
                        }
                    },
                    onDrag = { dx, dy ->
                        params.x += dx
                        params.y += dy
                        
                        // Prevent dragging off screen
                        params.x = params.x.coerceIn(0, screenWidth - this@apply.width)
                        params.y = params.y.coerceIn(0, screenHeight - this@apply.height)
                        
                        // Check if dragged to bottom
                        if (dragToDismiss && params.y > screenHeight * 0.85) {
                            performHaptic()
                            stopSelf()
                        } else {
                            windowManager.updateViewLayout(this@apply, params)
                        }
                    },
                    onDragEnd = {
                        if (stickToEdges) {
                            val viewWidth = this@apply.width
                            val distLeft = params.x
                            val distRight = screenWidth - params.x - viewWidth
                            val distTop = params.y

                            val oldX = params.x
                            val oldY = params.y

                            if (distTop < distLeft && distTop < distRight && distTop < 200) {
                                params.y = 0
                            } else if (distLeft < distRight) {
                                params.x = 0
                            } else {
                                params.x = screenWidth - viewWidth
                            }

                            val snapped = params.x != oldX || params.y != oldY
                            if (snapped) {
                                performHaptic()
                            }
                            isAtEdge = params.x == 0 || params.x == screenWidth - viewWidth || params.y == 0
                            
                            windowManager.updateViewLayout(this@apply, params)
                        } else {
                            isAtEdge = false
                        }
                    },
                    onDismiss = {
                        performHaptic()
                        stopSelf()
                    },
                    screenHeight = screenHeight
                )
            }
        }

        windowManager.addView(composeView, params)
    }

    private fun performHaptic() {
        composeView.performHapticFeedback(
            android.view.HapticFeedbackConstants.KEYBOARD_TAP
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "floating_volume",
                "Floating Volume Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "floating_volume")
            .setContentTitle("Floating Volume Button")
            .setContentText("Button is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::preferencesManager.isInitialized) {
            preferencesManager.setEnabled(false)
        }
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
