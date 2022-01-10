package com.chimerapps.storageinspector.util.analytics

/**
 * @author Nicola Verbeeck
 */
interface EventTracker {

    fun logEvent(event: AnalyticsEvent)

}

enum class AnalyticsEvent {
    CONNECT,
}