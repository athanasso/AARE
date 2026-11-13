package net.prototux.aare.car

import android.view.KeyEvent
import net.prototux.aare.Settings
import net.prototux.aare.host.CarRuntime
import net.prototux.aare.media.MediaHub

/**
 * Intercepts steering wheel controls and physical hardware buttons from the head unit
 * or Bluetooth media remotes, applying user-customized action bindings.
 */
object KeyRemapper {

    const val ACTION_DEFAULT = "default"
    const val ACTION_PLAY_PAUSE = "play_pause"
    const val ACTION_NEXT = "next"
    const val ACTION_PREV = "prev"
    const val ACTION_VOICE = "voice"
    const val ACTION_TOGGLE_SHELL = "toggle_shell"
    const val ACTION_HOME = "home"
    const val ACTION_MAPS = "maps"
    const val ACTION_PHONE = "phone"
    const val ACTION_MUTE = "mute"

    /**
     * Attempts to process a hardware key event. Returns true if handled by a custom remap,
     * or false to let standard CarSession key dispatch proceed.
     */
    fun handle(code: Int, down: Boolean, longPress: Boolean, runtime: CarRuntime, media: MediaHub, settings: Settings): Boolean {
        if (down) return false // Execute action on key release for crisp single-press semantics

        val customAction = settings.keyRemapFor(code) ?: return false
        when (customAction) {
            ACTION_PLAY_PAUSE -> media.playPause()
            ACTION_NEXT -> media.next()
            ACTION_PREV -> media.previous()
            ACTION_VOICE -> runtime.assistant()
            ACTION_TOGGLE_SHELL -> {
                // Instantly toggle between Dusk and Hybrid layouts on the fly!
                val current = settings.carLayout
                settings.carLayout = if (current == Settings.LAYOUT_DUSK) Settings.LAYOUT_CLASSIC else Settings.LAYOUT_DUSK
            }
            ACTION_HOME -> runtime.actions.goHome()
            ACTION_MAPS -> runtime.ui.navApp()?.let(runtime.actions::openApp)
            ACTION_PHONE -> runtime.ui.app(CarRuntime.PHONE_ID)?.let(runtime.actions::openApp)
            ACTION_MUTE -> media.pause()
            else -> return false
        }
        return true
    }
}

