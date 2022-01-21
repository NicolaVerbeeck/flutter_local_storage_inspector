package com.chimerapps.storageinspector.ui.util.preferences

import com.intellij.ide.util.PropertiesComponent

object AppPreferences {

    const val PREFIX = "com.chimerapps.storageinspector."

    private val properties = PropertiesComponent.getInstance()

    fun get(key: String, default: Int): Int = properties.getInt("$PREFIX$key", default)
    fun put(key: String, value: Int, default: Int = Int.MIN_VALUE) = properties.setValue("$PREFIX$key", value, default)

    fun get(key: String, default: Float): Float = properties.getFloat("$PREFIX$key", default)
    fun put(key: String, value: Float, default: Float = Float.MIN_VALUE) =
        properties.setValue("$PREFIX$key", value, default)

    fun get(key: String, default: String): String = properties.getValue("$PREFIX$key", default)
    fun put(key: String, value: String, default: String = "") = properties.setValue("$PREFIX$key", value, default)

    fun get(key: String, default: Boolean): Boolean = properties.getBoolean("$PREFIX$key", default)
    fun put(key: String, value: Boolean, default: Boolean = false) = properties.setValue("$PREFIX$key", value, default)

}