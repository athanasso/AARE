package net.prototux.aare.host

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap

/** A Car App Library app installed on the phone. */
data class CarAppInfo(
    val service: ComponentName,
    val label: String,
    val icon: Bitmap?,
    val categories: Set<String>,
    val minApiLevel: Int,
) {
    val packageName: String get() = service.packageName
    /**
     * The NAVIGATION category is self-declared by the app, in its own manifest.
     * Trusted for known navigation packages or any Car App Library app declaring the NAVIGATION category.
     */
    val isNavigation: Boolean get() = CATEGORY_NAVIGATION in categories && packageName !in ROGUE_NAVIGATION_PACKAGES

    companion object {
        const val SERVICE_ACTION = "androidx.car.app.CarAppService"
        const val CATEGORY_NAVIGATION = "androidx.car.app.category.NAVIGATION"
        const val CATEGORY_POI = "androidx.car.app.category.POI"
        const val CATEGORY_IOT = "androidx.car.app.category.IOT"
        const val CATEGORY_MESSAGING = "androidx.car.app.category.MESSAGING"
        const val CATEGORY_SETTINGS = "androidx.car.app.category.SETTINGS"
        const val CATEGORY_CLUSTER = "androidx.car.app.category.FEATURE_CLUSTER"
        const val MIN_API_KEY = "androidx.car.app.minCarApiLevel"

        /** Non-navigation apps that falsely declare the NAVIGATION category to grab the dashboard map slot. */
        val ROGUE_NAVIGATION_PACKAGES = setOf(
            "com.nuvio.app",
        )

        /** Popular map/navigation apps allowed onto the dashboard's map widget. Ask to add more. */
        val KNOWN_NAVIGATION_PACKAGES = setOf(
            "com.google.android.apps.maps", // Google Maps
            "com.waze",                     // Waze
            "app.vela",                     // Vela Maps
            "sa.hudhud.maps",                // HUDHUD Maps
            "com.sygic.aura",               // Sygic
            "com.here.app.maps",            // HERE WeGo
            "net.osmand", "net.osmand.plus",// OsmAnd
            "com.tomtom.gplay.navapp",      // TomTom GO
            "ru.yandex.yandexnavi", "ru.yandex.yandexmaps", // Yandex Navigator / Maps
            "com.mapswithme.maps.pro",      // MAPS.ME
            "cz.seznam.mapy",               // Mapy.cz
            "com.citymapper.app.release",   // Citymapper
        )

        /** Packages that provide system car framework services and must never be listed as user car apps. */
        val IGNORED_PACKAGES = setOf(
            "com.google.android.projection.gearhead", // Android Auto host itself
            "com.google.android.gms",                 // Google Play Services
            "com.google.android.googlequicksearchbox",// Google App internal voice plate and dashboard services
            "com.android.systemui",                   // System UI
            "com.android.bluetooth",                  // Bluetooth stack
            "com.google.android.bluetooth",           // Google Bluetooth stack
            "com.android.bluetoothmidiservice",       // Bluetooth MIDI
            "com.android.shell",                      // Shell
            "com.android.settings",                   // Android Settings
            "com.google.android.settings",            // Pixel Settings
            "com.zhiliaoapp.musically",               // TikTok
            "com.ss.android.ugc.trill",               // TikTok
            "gr.cosmote.myaccount.widget",            // Cosmote carrier app
            "gr.cosmote.whatsup",                     // Cosmote What's Up carrier app
            "com.instagram.android",                  // Instagram
            "com.facebook.katana",                    // Facebook
        )
    }
}

/** Finds Car App Library apps: services answering `androidx.car.app.CarAppService`. */
object CarAppCatalog {

    fun query(context: Context, iconSizePx: Int = 128): List<CarAppInfo> {
        val pm = context.packageManager
        val intent = Intent(CarAppInfo.SERVICE_ACTION)
        val flags = PackageManager.GET_META_DATA or PackageManager.GET_RESOLVED_FILTER
        val found = if (Build.VERSION.SDK_INT >= 33) pm.queryIntentServices(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
            else @Suppress("DEPRECATION") pm.queryIntentServices(intent, flags)
        return found.mapNotNull { ri ->
            val si = ri.serviceInfo ?: return@mapNotNull null
            val appInfo = si.applicationInfo ?: return@mapNotNull null
            if (!si.isEnabled || !appInfo.enabled) return@mapNotNull null
            if (!si.exported) return@mapNotNull null
            if (si.packageName == context.packageName) return@mapNotNull null
            if (si.packageName in CarAppInfo.IGNORED_PACKAGES) return@mapNotNull null
            val categories = buildSet {
                ri.filter?.categoriesIterator()?.forEach { add(it) }
            }
            // Ignore settings-only sub-activities (e.g. YouTube Music's AndroidAutoCarAppServices)
            if (CarAppInfo.CATEGORY_SETTINGS in categories && CarAppInfo.CATEGORY_NAVIGATION !in categories && CarAppInfo.CATEGORY_POI !in categories && CarAppInfo.CATEGORY_IOT !in categories) {
                return@mapNotNull null
            }
            // Messaging apps belong under Messaging, not under CarAppCatalog
            if (CarAppInfo.CATEGORY_MESSAGING in categories && CarAppInfo.CATEGORY_NAVIGATION !in categories && CarAppInfo.CATEGORY_POI !in categories && CarAppInfo.CATEGORY_IOT !in categories) {
                return@mapNotNull null
            }
            // Real media apps (e.g. YouTube Music) belong in Media, not in CarAppCatalog
            if (AndroidAutoApps.isKnownMedia(si.packageName)) {
                return@mapNotNull null
            }
            val minApi = appInfo.metaData?.getInt(CarAppInfo.MIN_API_KEY, 0)?.takeIf { it > 0 }
                ?: si.metaData?.getInt(CarAppInfo.MIN_API_KEY, 0) ?: 0
            val label = appInfo.loadLabel(pm).toString()
            CarAppInfo(
                service = ComponentName(si.packageName, si.name),
                label = label,
                icon = runCatching { appInfo.loadIcon(pm).toBitmap(iconSizePx, iconSizePx) }.getOrNull()
                    ?: runCatching { si.loadIcon(pm).toBitmap(iconSizePx, iconSizePx) }.getOrNull(),
                categories = categories,
                minApiLevel = minApi,
            )
        }.distinctBy { it.packageName }
    }
}
