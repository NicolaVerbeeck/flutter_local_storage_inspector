package com.chimerapps.storageinspector.ui.util.json

import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithTypeTypeAdapter
import com.google.gsonpackaged.Gson
import com.google.gsonpackaged.GsonBuilder

/**
 * @author Nicola Verbeeck
 */
object GsonCreator {

    fun newGsonInstance(): Gson {
        val builder = GsonBuilder()

        builder.registerTypeAdapter(ValueWithType::class.java, ValueWithTypeTypeAdapter())

        return builder.create()
    }

}