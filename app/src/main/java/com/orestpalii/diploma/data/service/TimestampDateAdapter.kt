package com.orestpalii.diploma.data.service

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.util.Date

class TimestampDateAdapter : JsonDeserializer<Date> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Date {
        return try {
            Date((json.asDouble * 1000).toLong())
        } catch (e: Exception) {
            Date()
        }
    }
}
