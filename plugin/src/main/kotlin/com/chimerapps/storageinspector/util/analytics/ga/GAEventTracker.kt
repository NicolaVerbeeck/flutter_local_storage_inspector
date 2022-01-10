package com.chimerapps.storageinspector.util.analytics.ga

import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorSettings
import com.chimerapps.storageinspector.util.analytics.AnalyticsEvent
import com.chimerapps.storageinspector.util.analytics.EventTracker
import com.intellij.openapi.application.ApplicationInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.Locale
import java.util.UUID

/**
 * @author Nicola Verbeeck
 */
class GAEventTracker private constructor() : EventTracker {

    companion object {
        val instance: GAEventTracker by lazy { GAEventTracker() }
        private val osName: String by lazy {
            System.getProperty("os.name")
        }
        private val userAgent: String by lazy {
            "Mozilla/5.0 ($osName};)"
        }
    }

    private val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("User-Agent", userAgent)
                    .build()
            )
        }
        .build()
    private val trackingId = "UA-216564456-1"
    private val scope = CoroutineScope(SupervisorJob())

    override fun logEvent(event: AnalyticsEvent) {
        scope.launch {
            if (submitEvent(event)) {
                println("Event submit complete!")
            }
        }
    }

    private fun submitEvent(data: AnalyticsEvent): Boolean {
        val state = StorageInspectorSettings.instance.state
        if (state.analyticsStatus != true) return false
        var userId = state.analyticsUserId
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            StorageInspectorSettings.instance.updateState {
                copy(analyticsUserId = userId)
            }
        }

        val request = Request.Builder()
            .url("https://www.google-analytics.com/collect")
            .post(makeBody(data, userId))
            .build()

        val response = client.newCall(request).execute()
        return response.isSuccessful
    }

    private fun makeBody(data: AnalyticsEvent, userId: String): RequestBody {
        return FormBody.Builder()
            .add("v", "1")
            .add("tid", trackingId)
            .add("cid", userId)
            .add("aip", "1") //Anonymize ip
            .add("npa", "1") //No advertisements
            .add("an", ApplicationInfo.getInstance().versionName)
            .add("av", ApplicationInfo.getInstance().fullVersion)
            .add("ul", Locale.getDefault().language)
            .add("t", "event")
            .add("ec", "event")
            .add("ds", osName)
            .add("ea", data.name)
            .build()
    }

}

