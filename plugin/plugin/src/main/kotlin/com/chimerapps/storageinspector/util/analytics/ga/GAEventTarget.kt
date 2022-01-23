package com.chimerapps.storageinspector.util.analytics.ga

import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorSettings
import com.chimerapps.storageinspector.util.analytics.batching.EventTarget
import com.intellij.openapi.application.ApplicationInfo
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author Nicola Verbeeck
 */
class GAEventTarget private constructor() : EventTarget {

    companion object {
        val instance: GAEventTarget by lazy { GAEventTarget() }
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


    private suspend fun submitEvent(eventName: String, count: Int?): Boolean {
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
            .post(makeBody(eventName, userId, count))
            .build()

        val response = client.newCall(request).await()
        return response.isSuccessful
    }

    private fun makeBody(name: String, userId: String, count: Int?): RequestBody {
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
            .add("ea", name)
            .also {
                count?.let { count -> it.add("ev", count.toString()) }
            }
            .build()
    }

    override suspend fun sendEvent(eventName: String, count: Int?) : Boolean{
        return submitEvent(eventName, count)
    }

}

internal suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        val callback = ContinuationCallback(this, continuation)
        enqueue(callback)
        continuation.invokeOnCancellation(callback)
    }
}

private class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<Response>
) : Callback, CompletionHandler {

    override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
    }

    override fun onFailure(call: Call, e: IOException) {
        if (!call.isCanceled()) {
            continuation.resumeWithException(e)
        }
    }

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {}
    }
}