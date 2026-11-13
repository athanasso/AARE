package net.prototux.aare.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import net.prototux.aare.Settings
import net.prototux.aare.audio.MicRouter
import net.prototux.aare.host.CarRuntime
import net.prototux.aare.media.MediaHub
import net.prototux.aare.ui.car.CarScreen

/**
 * The car screen on the phone itself, without a car: to try apps, check the layout, or use
 * the phone as a car display. Hosted apps get the phone's microphone.
 */
class CarPreviewActivity : ComponentActivity() {
    private lateinit var runtime: CarRuntime
    private val mic = MicRouter({ null }, {})

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val settings = Settings(this)
        runtime = CarRuntime(this, lifecycleScope, settings, MediaHub(this, lifecycleScope), primary = false)
        val dm = resources.displayMetrics
        val dpi = settings.carDpi.takeIf { it > 0 } ?: dm.densityDpi
        val scale = dpi.toFloat() / 160f
        runtime.densityDpi = dpi
        runtime.screenWidthDp = (dm.widthPixels / scale).toInt()
        runtime.screenHeightDp = (dm.heightPixels / scale).toInt()
        runtime.microphone = { sink, stopped -> mic.open(sink, stopped) }
        runtime.start()
        setContent { runtime.Root(Modifier.fillMaxSize().systemBarsPadding()) }
        // Through the dispatcher: with predictive back (targetSdk 36+ on Android 16) onBackPressed
        // is no longer called. Back walks the car screen back to the dashboard, then leaves.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (runtime.ui.screen == CarScreen.Dashboard) finish() else runtime.back()
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> { runtime.actions.mediaPlayPause(); true }
            KeyEvent.KEYCODE_MEDIA_PLAY -> { runtime.actions.mediaPlayPause(); true }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> { runtime.actions.mediaPlayPause(); true }
            KeyEvent.KEYCODE_MEDIA_NEXT -> { runtime.actions.mediaNext(); true }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> { runtime.actions.mediaPrevious(); true }
            KeyEvent.KEYCODE_VOICE_ASSIST, KeyEvent.KEYCODE_SEARCH -> { runtime.actions.assistant(); true }
            KeyEvent.KEYCODE_CALL -> { runtime.actions.answerCall(); true }
            KeyEvent.KEYCODE_ENDCALL -> { runtime.actions.endCall(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        mic.shutdown()
        runtime.stop()
        super.onDestroy()
    }
}
