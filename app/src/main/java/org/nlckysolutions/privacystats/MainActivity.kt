package org.nlckysolutions.privacystats

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import org.nlckysolutions.privacystats.ui.theme.PrivacyStatsTheme
import java.io.File
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.delay
import org.json.JSONObject
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val HORRIBLE = listOf(
        "com.google.android.gms",
        "com.google.android.googlequicksearchbox",
        "com.google.android.apps.photos",
        "com.google.android.gm",
        "com.google.android.apps.maps",
        "com.google.android.apps.messaging",
        "com.google.android.dialer",
        "com.google.android.apps.docs",
        "com.android.vending",
        "com.google.android.apps.chrome",
        "com.google.android.apps.nbu.files",
        "com.google.android.as",
        "com.facebook.katana",
        "com.facebook.orca",               // Messenger
        "com.facebook.appmanager",
        "com.instagram.android",
        "com.whatsapp",
        "com.snapchat.android",
        "com.twitter.android",
        "com.xiaomi.account",
        "com.miui.analytics",
        "com.miui.securitycenter",
        "com.miui.globalservice",
        "com.miui.weather2",
        "com.mi.globalbrowser",
        "com.huawei.hwid",
        "com.huawei.systemmanager",
        "com.huawei.appmarket",
        "com.samsung.android.spay",
        "com.samsung.android.location",
        "com.samsung.android.providers.context",
        "com.samsung.android.messaging",
        "com.samsung.android.email.provider",
        "com.amazon.mShop.android.shopping",
        "com.amazon.kindle",
        "com.amazon.avod.thirdpartyclient",
        "com.microsoft.office.word",
        "com.microsoft.teams",
        "com.microsoft.skype",
        "com.tencent.mobileqq",            // QQ
        "com.tencent.mm",                  // WeChat
        "com.zhiliaoapp.musically",        // TikTok (older common package)
        "com.ss.android.ugc.trill",        // TikTok variants
        "com.bytedance.labs.adsdk",        // Bytedance ad/telemetry bits
        "com.viber.voip",
        "com.linecorp.line",
        "com.linkedin.android",
        "com.pinterest",
        "com.reddit.frontpage",
        "com.yelp.android",
        "com.ubercab",
        "com.google.android.youtube",
        "com.google.android.apps.photos",
        "com.google.android.apps.tachyon"  // Google Duo/Meet variants
    )

    // SORTA_BAD (OEM helpers, vendor services, large advertising/platform partners — consider flagging)
    private val SORTA_BAD = listOf(
        "com.samsung.android.spay",
        "com.samsung.android.location",
        "com.samsung.android.knox",
        "com.samsung.android.sdk.accessory",
        "com.samsung.android.providers.context",
        "com.samsung.android.mobileservice",
        "com.samsung.android.lool",
        "com.samsung.android.email.provider",
        "com.samsung.android.app.watchmanager",
        "com.huawei.hidisk",
        "com.huawei.hwlaunchertheme",
        "com.huawei.systemmanager",
        "com.oppo.market",
        "com.oppo.safe",
        "com.oppo.push",
        "com.vivo.abe",
        "com.vivo.push",
        "com.oneplus.account",
        "com.oneplus.mms",
        "com.flyme.calendar",
        "com.mi.globalbrowser",
        "com.amazon.kms.vault",
        "com.amazon.device.sync",
        "com.android.providers.partnerbookmarks",
        "com.google.android.apps.enterprise.dmagent",
        "com.google.android.apps.enterprise.wm",
        "com.microsoft.launcher",
        "com.microsoft.office.officehub",
        "com.facebook.appmanager",
        "com.facebook.katana",
        "com.facebook.system",
        "com.facebook.services",
        "com.adobe.reader",
        "com.adobe.air",
        "com.sina.weibo",
        "com.huami.watch.hmwatchmanager",
        "com.netflix.mediaclient",
        "com.tesla.teslaapp", // example of OEM/car apps — often permission-heavy
        "com.qualcomm.qti.services.secureui"
    )

    // WATCH_OUT (analytics SDKs, ad networks, tracking libraries — often embedded in many apps)
    private val WATCH_OUT = listOf(
        // Google / Firebase measurement
        "com.google.firebase.analytics",
        "com.google.android.gms.measurement",
        "com.google.android.gms.analytics",
        // Facebook / Meta SDKs
        "com.facebook.ads",
        "com.facebook.appevents",
        // Common Mobile Attribution / Analytics SDKs
        "com.adjust.sdk",
        "com.appsflyer",
        "com.onesignal",
        "com.segment.analytics",
        "com.flurry.android",
        "com.crashlytics.android",      // Fabric / Crashlytics/ Firebase Crashlytics
        "com.bugsnag.android",
        "io.sentry",
        // Ad networks / monetization SDKs
        "com.mopub.mobileads",
        "com.adcolony.sdk",
        "com.chartboost.sdk",
        "com.unity3d.ads",
        "com.ironsource.mediationsdk",
        "com.vungle",
        "com.appnext",
        "com.inmobi.commons",
        "com.applovin.sdk",
        "com.smaato.soma",
        "com.tapjoy",
        "com.startapp",
        // Push / messaging SDKs
        "com.amazon.device.messaging",
        "com.huawei.hms.push",
        "com.xiaomi.mipush",
        "com.vivo.push",
        "com.oppo.push",
        // Other tracking libs / helper patterns
        "com.crittercism",
        "com.segment.analytics.android",
        "com.adjust",
        "com.appsflyer.androidsdk",
        "com.google.android.apps.gcs", // Google cloud services helper
        "com.google.android.gms.location", // location services (used by many apps)
        "com.google.android.gms.ads", // google ads module
        "com.google.android.gsf", // google services framework
        "com.spotify.music"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //val installedApps = getInstalledApps()
        val installedAppsState = mutableStateOf(getInstalledApps())

        val buildProps = readBuildProps()

        setContent {
            PrivacyStatsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivacyReportScreen(
                        installedApps = installedAppsState.value,
                        buildProps = buildProps,
                        horribleList = HORRIBLE,
                        sortaList = SORTA_BAD,
                        watchList = WATCH_OUT
                    )
                }
            }
        }
    }

    /*suspend fun scanInstalledAppsAndClassify(
        context: Context,
        packages: List<String>,
        localFallbackLists: Triple<Set<String>, Set<String>, Set<String>>, // horrible, sorta, watch
        cache: MutableMap<String, Pair<Int, Long>> // package -> (trackersCount, timestamp)
    ): Map<String, String> { // package -> category
        val result = mutableMapOf<String, String>()
        val online = context.isOnline()
        val (localH, localS, localW) = localFallbackLists

        for (pkg in packages) {
            // check cache (e.g., 7d expiry)
            val cached = cache[pkg]
            if (cached != null && System.currentTimeMillis() - cached.second < 7L*24*3600*1000) {
                result[pkg] = categorizeByTrackerCount(cached.first)
                continue
            }

            if (!online) {
                // offline => use fallback lists
                result[pkg] = when {
                    pkg in localH -> "horrible"
                    pkg in localS -> "sorta_bad"
                    pkg in localW -> "watch_out"
                    else -> "none"
                }
                continue
            }

            // online -> query Exodus (but be polite)
            val count = fetchTrackerCountForPackage(pkg)
            if (count != null) {
                cache[pkg] = Pair(count, System.currentTimeMillis())
                result[pkg] = categorizeByTrackerCount(count)
            } else {
                // fallback to local lists if exodus didn't have it
                result[pkg] = when {
                    pkg in localH -> "horrible"
                    pkg in localS -> "sorta_bad"
                    pkg in localW -> "watch_out"
                    else -> "none"
                }
            }

            // polite rate limit: small delay to avoid hammering public API
            delay(200L) // tune this as needed (e.g., 200-500ms)
        }

        return result
    }*/

    // add dependencies: implementation("com.squareup.okhttp3:okhttp:4.11.0")
// and coroutines
    /*fun Context.isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun fetchTrackerCountForPackage(pkg: String): Int? = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder().build()
        val url = "https://reports.exodus-privacy.eu.org/en/reports/${pkg}/latest/"

        val req = Request.Builder()
            .url(url)
            .header("Accept", "application/json, text/html")
            .header("User-Agent", "MyApp/1.0 (nlckysolutions)")
            .build()

        try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    // 404 or other -> no report
                    return@withContext null
                }
                val body = resp.body?.string() ?: return@withContext null

                // Try JSON parse first
                try {
                    val j = JSONObject(body)
                    // Exodus JSON shape varies; many dumps include trackers array or trackers object
                    val trackersArr = j.optJSONArray("trackers")
                    if (trackersArr != null) return@withContext trackersArr.length()

                    // Some responses wrap versions -> try to find most recent version's trackers
                    val reports = j.optJSONArray("reports") ?: j.optJSONArray("versions")
                    if (reports != null && reports.length() > 0) {
                        val latest = reports.getJSONObject(0)
                        val t = latest.optJSONArray("trackers")
                        if (t != null) return@withContext t.length()
                    }
                } catch (je: Exception) {
                    // not JSON — fall through to HTML parsing
                }

                // Fallback: quick-and-dirty HTML parse for "Trackers (X)" or "Found X trackers"
                val regex = Regex("""(\bTrackers\b[^0-9\n\r]*?)(\d{1,3})""", RegexOption.IGNORE_CASE)
                val match = regex.find(body)
                if (match != null) {
                    val n = match.groupValues[2].toIntOrNull()
                    if (n != null) return@withContext n
                }

                // Another fallback: search "found in X apps" lines or "We have found the following permissions"
                // if none found -> unknown
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/

    private fun getInstalledApps(): List<ApplicationInfo> {
        val pm = packageManager
        // Return all installed apps (system + user). UI decides what to show based on toggle.
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    private fun readBuildProps(): Map<String, String> {
        return try {
            val file = File("/system/build.prop")
            if (!file.canRead()) return emptyMap()
            file.readLines()
                .filter { it.contains("=") }
                .associate {
                    val (key, value) = it.split("=", limit = 2)
                    key to value
                }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

/** ---------- UI / Compose ---------- **/

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyReportScreen(
    installedApps: List<ApplicationInfo>,
    buildProps: Map<String, String>,
    horribleList: List<String>,
    sortaList: List<String>,
    watchList: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val pm = LocalContext.current.packageManager
    val resumeTick = remember { mutableStateOf(0) }
    // Toggle: show system apps
    var showSystemApps by rememberSaveable { mutableStateOf(false) }

    // Filtered list based on toggle
    val visibleApps = remember(installedApps, showSystemApps, resumeTick.value) {
    if (showSystemApps) {
            installedApps
        } else {
            installedApps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        }
    }

    // Categorize apps into three buckets using the lists provided
    val categorized = remember(visibleApps, horribleList, sortaList, watchList, resumeTick.value) {
    val horrible = mutableListOf<ApplicationInfo>()
        val sorta = mutableListOf<ApplicationInfo>()
        val watch = mutableListOf<ApplicationInfo>()

        visibleApps.forEach { ai ->
            val pkg = ai.packageName ?: return@forEach
            val lowered = pkg.lowercase()

            when {
                horribleList.any { lowered.contains(it.lowercase()) } -> horrible += ai
                sortaList.any { lowered.contains(it.lowercase()) } -> sorta += ai
                watchList.any { lowered.contains(it.lowercase()) } -> watch += ai
                // more heuristics can be added here
            }
        }

        Triple(horrible, sorta, watch)
    }

    val horribleApps = categorized.first
    val sortaApps = categorized.second
    val watchApps = categorized.third

    val privacyScore = remember(horribleApps, sortaApps, watchApps, buildProps) {
        calculatePrivacyScore(horribleApps.size, sortaApps.size, watchApps.size, buildProps)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeTick.value++ // Trigger recomposition
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(start=16.dp, end=16.dp, top=37.dp, bottom=60.dp)) {

        // Top row: toggle + info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Column {
                Text(
                    text = "PrivacyStats Score",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$privacyScore/100",
                    style = MaterialTheme.typography.titleLarge,
                    color = when {
                        privacyScore >= 85 -> Color(0xFF2E7D32)
                        privacyScore >= 60 -> Color(0xFFF9A825)
                        else -> Color(0xFFD32F2F)
                    },
                    fontWeight = FontWeight.Bold
                )
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("OK")
                            }
                        },
                        title = { Text("About") },
                        text = {
                            Text("Copyright (c) 2025 NlckySolutions")
                        }
                    )
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Show system apps", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = showSystemApps,
                                    onCheckedChange = { showSystemApps = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        },
                        onClick = { /* No-op because the Switch handles the interaction */ }
                    )
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            showDialog = true
                        },
                        text = { Text("About") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // If nothing flagged
        if (horribleApps.isEmpty() && sortaApps.isEmpty() && watchApps.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Private",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(if (showSystemApps) "Your phone is likely extremely private"
                        else "Your phone may be private.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (showSystemApps) "No known telemetry apps found from configured lists."
                        else "No known telemetry apps found among USER-installed apps. Toggle 'Show system apps' to include system apps (Recommended if you are comfortable uninstalling system apps).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            return
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Otherwise show categorized lists
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (horribleApps.isNotEmpty()) {
                item { CategoryHeader(title = "Spyware — Remove Now", color = Color(0xFFD32F2F)) }
                items(horribleApps) { app -> AppRow(app, pm, accentColor = Color(0xFFD32F2F)) }
            }
            if (sortaApps.isNotEmpty()) {
                item { CategoryHeader(title = "Horrible — Consider Removing", color = Color(0xFFFFA000)) }
                items(sortaApps) { app -> AppRow(app, pm, accentColor = Color(0xFFFFA000)) }
            }
            if (watchApps.isNotEmpty()) {
                item { CategoryHeader(title = "Caution — Monitor These", color = Color(0xFFFFD54F)) }
                items(watchApps) { app -> AppRow(app, pm, accentColor = Color(0xFFFFD54F)) }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

/*@Composable
fun AppRow(app: ApplicationInfo, pm: PackageManager, accentColor: Color) {
    val label = try { pm.getApplicationLabel(app).toString() } catch (e: Exception) { app.packageName }
    val iconDrawable = try { pm.getApplicationIcon(app) } catch (e: Exception) { null }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconDrawable != null) {
            val bmp = iconDrawable.toBitmap()
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = accentColor.copy(alpha = 0.25f), shape = MaterialTheme.shapes.small)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .background(color = accentColor.copy(alpha = 0.12f), shape = MaterialTheme.shapes.extraSmall)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = "!",
                color = accentColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}*/

/*@Composable
fun AppRow(app: ApplicationInfo, pm: PackageManager, accentColor: Color) {
    val context = LocalContext.current
    val label = try { pm.getApplicationLabel(app).toString() } catch (e: Exception) { app.packageName }
    val iconDrawable = try { pm.getApplicationIcon(app) } catch (e: Exception) { null }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconDrawable != null) {
            val bmp = iconDrawable.toBitmap()
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = accentColor.copy(alpha = 0.25f), shape = MaterialTheme.shapes.small)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${app.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK // <-- this line is essential
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor.copy(alpha = 0.15f),
                contentColor = accentColor
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Uninstall", style = MaterialTheme.typography.labelLarge)
        }
    }
}*/

@Composable
fun AppRow(app: ApplicationInfo, pm: PackageManager, accentColor: Color) {
    val context = LocalContext.current
    val label = try { pm.getApplicationLabel(app).toString() } catch (e: Exception) { app.packageName }
    val iconDrawable = try { pm.getApplicationIcon(app) } catch (e: Exception) { null }

    val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    val isDisabled = try {
        pm.getApplicationEnabledSetting(app.packageName) ==
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    } catch (e: Exception) {
        false
    }
    val isActuallyDisabled = try {
        !pm.getApplicationInfo(app.packageName, 0).enabled
    } catch (e: Exception) {
        false
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconDrawable != null) {
            val bmp = iconDrawable.toBitmap()
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.width(8.dp))
        // Decide whether to show a button at all
        if (!isSystemApp || (isSystemApp && !isActuallyDisabled)) {
            val buttonLabel = if (isSystemApp) "Disable" else "Uninstall"
            val intent = if (!isSystemApp) {
                /*Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${app.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }*/
                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${app.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${app.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }

            Button(
                onClick = { context.startActivity(intent) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(buttonLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}



/** ---------- Scoring logic ---------- **/
fun calculatePrivacyScore(horribleCount: Int, sortaCount: Int, watchCount: Int, props: Map<String, String>): Int {
    var score = 100
    score -= horribleCount * 10
    score -= sortaCount * 5
    score -= watchCount * 2

    if (props["ro.build.type"] == "userdebug") score -= 10
    if (props["ro.secure"] == "0") score -= 15

    return score.coerceIn(0, 100)
}
