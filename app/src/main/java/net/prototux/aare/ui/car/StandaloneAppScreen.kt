package net.prototux.aare.ui.car

import android.graphics.SurfaceTexture
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.prototux.aare.R
import net.prototux.aare.host.CarRuntime
import net.prototux.aare.launch.AppLauncher
import net.prototux.aare.media.Placeholder

/**
 * Hosts an ordinary phone app on the car screen (Google Maps, Waze, NewPipe, an emulator…). A
 * [TextureView] gives its surface to the privileged [AppLauncher], which puts a virtual display
 * there, launches [packageName] on it, and receives raw multi-touch events we forward.
 *
 * A [navigation] app keeps running while driving without an overlay banner.
 */
@Composable
internal fun StandaloneAppScreen(
    runtime: CarRuntime,
    driving: Boolean,
    packageName: String,
    navigation: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val launcher = remember(packageName) { AppLauncher(context) }
    var started by remember(packageName) { mutableStateOf(false) }
    val paused = driving && !navigation

    DisposableEffect(packageName) {
        // Hardware / head-unit Back goes to the launched app.
        val back: () -> Boolean = {
            val t = android.os.SystemClock.uptimeMillis()
            launcher.key(t, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)
            launcher.key(t, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK)
            launcher.connected
        }
        runtime.screenBack = back
        onDispose {
            if (runtime.screenBack === back) runtime.screenBack = null
            launcher.release()
        }
    }

    // "Navigate to…" from the dashboard: hand the geo: link to the app once it is on the display.
    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        val uri = runtime.pendingNavigateUri ?: return@LaunchedEffect
        if (launcher.view(uri, packageName)) runtime.pendingNavigateUri = null
    }

    Box(modifier.background(Color.Black)) {
        if (!paused) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                                val surface = Surface(st)
                                // connect() blocks (binds Shizuku / spawns su), so do it off the main thread.
                                Thread {
                                    if (launcher.connect()) {
                                        val id = launcher.start(surface, w, h, runtime.densityDpi, packageName)
                                        started = id >= 0
                                    }
                                }.apply { isDaemon = true }.start()
                            }
                            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {
                                launcher.resize(Surface(st), w, h, runtime.densityDpi)
                            }
                            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean = true
                            override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                        }
                        // Raw touch events with multi-pointer support: pinch-to-zoom on maps works natively.
                        setOnTouchListener { v, event ->
                            launcher.motion(event)
                            if (event.actionMasked == MotionEvent.ACTION_UP) v.performClick()
                            true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            if (!navigation) {
                Text(
                    stringResource(R.string.standalone_not_native),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 12.dp, vertical = 3.dp),
                )
            }
            if (!started) Placeholder(stringResource(R.string.standalone_starting), loading = true)
        } else {
            Placeholder(stringResource(R.string.standalone_paused_driving), loading = false, icon = Icons.Rounded.PauseCircle)
        }
    }
}
