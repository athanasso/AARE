package net.prototux.aare.host

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.Telephony
import androidx.core.graphics.drawable.toBitmap
import net.prototux.aare.media.MediaDiscovery
import org.xmlpull.v1.XmlPullParser

/** An app that declares Android Auto support but that AARE doesn't show, and why. */
data class UnsupportedCarApp(
    val packageName: String,
    val label: String,
    val icon: Bitmap?,
    /** The `<uses name="…">` entries of its `automotive_app_desc`. */
    val uses: Set<String>,
    val reason: String,
)

/** An app on the phone listed in the car (a messaging or media app), by package. */
data class PhoneApp(val packageName: String, val label: String, val icon: Bitmap?)

/**
 * Finds every app that declares Android Auto support (`com.google.android.gms.car.application`
 * meta-data): the messaging apps among them, and the ones AARE can't show, to tell the user.
 */
object AndroidAutoApps {
    private const val META_CAR_APPLICATION = "com.google.android.gms.car.application"
    private val MESSAGING_USES = setOf("notification", "sms")

    /** Known audio, podcast, radio, and streaming apps. */
    fun isKnownMedia(pkg: String): Boolean {
        val p = pkg.lowercase()
        return p.contains("youtube.music") ||
               p.contains("spotify") ||
               p.contains("deezer") ||
               p.contains("tidal") ||
               p.contains("soundcloud") ||
               p.contains("tunein") ||
               p.contains("audible") ||
               p.contains("pocketcasts") ||
               p.contains("antennapod") ||
               p.contains("poweramp") ||
               p.contains("musicolet") ||
               p.contains("apple.android.music") ||
               p.contains("amazon.mp3") ||
               p.contains("qobuz") ||
               p.contains("pandora") ||
               p.contains("vlc") ||
               p.contains("plex") ||
               p.contains("jellyfin") ||
               p.contains("symfonium") ||
               p.contains("stremio") ||
               p.contains("audioplayer") ||
               p.contains("musicplayer")
    }

    /** Known messaging, chat, and SMS packages. */
    fun isKnownMessaging(pkg: String): Boolean {
        val p = pkg.lowercase()
        return p.contains("telegram") ||
               p.contains("nagram") ||
               p.contains("nekogram") ||
               p.contains("whatsapp") ||
               p.contains("signal") ||
               p.contains("messenger") ||
               p.contains("discord") ||
               p.contains("viber") ||
               p.contains("skype") ||
               p.contains("teams") ||
               p.contains("slack") ||
               p.contains("threema") ||
               p.contains("session") ||
               p.contains("element") ||
               p.contains("matrix") ||
               p.contains("line") ||
               p.contains("wechat") ||
               p.contains("kakao") ||
               p.contains("messaging") ||
               p.contains("messages")
    }

    /** Verifies that a package is genuinely a car media player, not a system background service or random app. */
    fun isCarMediaApp(pm: PackageManager, app: ApplicationInfo): Boolean {
        val pkg = app.packageName
        if (pkg in CarAppInfo.IGNORED_PACKAGES) return false
        if (pkg in CarAppInfo.KNOWN_NAVIGATION_PACKAGES) return false
        if (isKnownMessaging(pkg)) return false
        // Must be a launchable application
        if (pm.getLaunchIntentForPackage(pkg) == null) return false
        if (isKnownMedia(pkg)) return true
        val u = uses(pm, app)
        if (u.any { it in MESSAGING_USES }) return false
        if (Build.VERSION.SDK_INT >= 26 && app.category == ApplicationInfo.CATEGORY_AUDIO) return true
        return u.contains("media")
    }

    /**
     * Messaging apps, as Android Auto finds them: apps declaring `<uses name="notification"/>`
     * (or `sms`), plus the default SMS app and apps with `CarAppService` messaging category.
     */
    fun messagingApps(context: Context, iconSizePx: Int = 128): List<PhoneApp> {
        val pm = context.packageManager
        val sms = runCatching { Telephony.Sms.getDefaultSmsPackage(context) }.getOrNull()
        val carMessaging = runCatching {
            val intent = Intent(CarAppInfo.SERVICE_ACTION)
            pm.queryIntentServices(intent, 0).mapNotNull { ri ->
                val si = ri.serviceInfo ?: return@mapNotNull null
                if (si.packageName in CarAppInfo.IGNORED_PACKAGES) return@mapNotNull null
                val categories = buildSet { ri.filter?.categoriesIterator()?.forEach { add(it) } }
                if (CarAppInfo.CATEGORY_MESSAGING in categories) si.packageName else null
            }
        }.getOrDefault(emptyList())

        return (declaring(context)
            .filter { (app, uses) ->
                app.packageName !in CarAppInfo.IGNORED_PACKAGES &&
                app.packageName !in CarAppInfo.KNOWN_NAVIGATION_PACKAGES &&
                (uses.any { it in MESSAGING_USES } || isKnownMessaging(app.packageName) || app.packageName == sms || app.packageName in carMessaging)
            }
            .map { (app, _) -> PhoneApp(app.packageName, app.loadLabel(pm).toString(), runCatching { app.loadIcon(pm).toBitmap(iconSizePx, iconSizePx) }.getOrNull()) }
            + carMessaging.mapNotNull { pkg ->
                if (pkg in CarAppInfo.IGNORED_PACKAGES || pkg in CarAppInfo.KNOWN_NAVIGATION_PACKAGES) return@mapNotNull null
                runCatching {
                    val app = pm.getApplicationInfo(pkg, 0)
                    PhoneApp(pkg, app.loadLabel(pm).toString(), runCatching { app.loadIcon(pm).toBitmap(iconSizePx, iconSizePx) }.getOrNull())
                }.getOrNull()
            })
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    /** Media apps, as the car's launcher lists them (see MediaDiscovery): one per package. */
    fun mediaApps(context: Context, iconSizePx: Int = 128): List<PhoneApp> {
        val pm = context.packageManager
        return mediaPackages(pm).filter { it != context.packageName }.mapNotNull { pkg ->
            runCatching {
                val app = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
                if (!isCarMediaApp(pm, app)) return@mapNotNull null
                PhoneApp(pkg, app.loadLabel(pm).toString(), runCatching { app.loadIcon(pm).toBitmap(iconSizePx, iconSizePx) }.getOrNull())
            }.getOrNull()
        }.sortedBy { it.label.lowercase() }
    }

    /** Apps declaring Android Auto support that AARE lists neither as car apps ([carApps]) nor as media or messaging apps. */
    fun unsupported(context: Context, carApps: Collection<String>, iconSizePx: Int = 96): List<UnsupportedCarApp> {
        val pm = context.packageManager
        val shown = carApps.toSet() + mediaPackages(pm)
        val messaging = messagingApps(context).map { it.packageName }.toSet()
        return declaring(context)
            .filter { (app, _) -> app.packageName !in shown && app.packageName !in messaging }
            .map { (app, uses) ->
                UnsupportedCarApp(
                    packageName = app.packageName,
                    label = app.loadLabel(pm).toString(),
                    icon = runCatching { app.loadIcon(pm).toBitmap(iconSizePx, iconSizePx) }.getOrNull(),
                    uses = uses,
                    reason = reason(uses),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /** Enabled apps with `com.google.android.gms.car.application` meta-data, with their `<uses>` entries. */
    private fun declaring(context: Context): List<Pair<ApplicationInfo, Set<String>>> {
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.enabled && it.metaData?.containsKey(META_CAR_APPLICATION) == true && it.packageName != context.packageName && it.packageName !in CarAppInfo.IGNORED_PACKAGES }
            .map { it to uses(pm, it) }
    }

    /** Packages with a media browser service, which AARE lists as media apps (see MediaDiscovery). */
    private fun mediaPackages(pm: PackageManager): Set<String> =
        listOf(MediaDiscovery.ACTION_MEDIA3_LIBRARY, MediaDiscovery.ACTION_LEGACY_BROWSER).flatMap { action ->
            @Suppress("DEPRECATION") pm.queryIntentServices(Intent(action), 0)
                .mapNotNull { ri -> ri.serviceInfo?.takeIf { it.exported && it.isEnabled && it.applicationInfo?.enabled == true }?.packageName }
                .filter { it !in CarAppInfo.IGNORED_PACKAGES }
        }.toSet()

    private fun reason(uses: Set<String>): String = when {
        "media" in uses -> "Declares media support but has no media browser service AARE can use"
        "template" in uses -> "Declares a car app but has no Car App Library service AARE can use"
        uses.isEmpty() -> "Uses Google's private Android Auto interface, which only Android Auto can host"
        else -> "Uses Google's private Android Auto interface (${uses.joinToString()}), which only Android Auto can host"
    }

    /** The `<uses name="…">` entries of the app's `automotive_app_desc` XML. */
    private fun uses(pm: PackageManager, app: ApplicationInfo): Set<String> {
        val res = app.metaData?.getInt(META_CAR_APPLICATION, 0)?.takeIf { it != 0 } ?: return emptySet()
        return runCatching {
            pm.getResourcesForApplication(app).getXml(res).use { xml ->
                buildSet {
                    var type = xml.eventType
                    while (type != XmlPullParser.END_DOCUMENT) {
                        if (type == XmlPullParser.START_TAG && xml.name == "uses") xml.getAttributeValue(null, "name")?.let { add(it) }
                        type = xml.next()
                    }
                }
            }
        }.getOrDefault(emptySet())
    }
}
