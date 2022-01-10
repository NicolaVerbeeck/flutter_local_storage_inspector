package com.chimerapps.storageinspector.util.analytics

import com.chimerapps.storageinspector.util.analytics.batching.BatchingEventTracker

/**
 * @author Nicola Verbeeck
 */
interface EventTracker {

    companion object {
        val default: EventTracker by lazy { BatchingEventTracker() }
    }

    fun logEvent(event: AnalyticsEvent, count: Int? = null)

}

enum class AnalyticsEvent {
    CONNECT,
}