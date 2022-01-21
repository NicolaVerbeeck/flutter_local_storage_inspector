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
    LIST_KEY_VALUES,
    CLEAR_KEY_VALUES,
    REMOVE_KEY_VALUES,
    WRITE_KEY_VALUES,
    READ_KEY_VALUES,
    KEY_VALUE_SEARCH,
    LIST_FILES,
    GET_FILE_CONTENT,
    WRITE_FILE_CONTENT,
    REMOVE_FILE,
}