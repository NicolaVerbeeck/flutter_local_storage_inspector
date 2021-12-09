package com.chimerapps.storageinspector.util

import com.intellij.openapi.diagnostic.Logger

/**
 * @author Nicola Verbeeck
 */
val Any.classLogger : Logger
    get() = Logger.getInstance(this::class.java)