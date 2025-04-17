package com.orestpalii.diploma.data.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.internal.bind.util.ISO8601Utils
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.ParsePosition
import java.util.Date

object JsonParser {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): Date {
                return when {
                    json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> {
                        // якщо число — в секундах (з дробами)
                        val seconds = json.asDouble
                        Date((seconds * 1000).toLong())
                    }
                    json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                        // якщо рядок — парсимо ISO‑8601
                        ISO8601Utils.parse(json.asString, ParsePosition(0))
                    }
                    else -> throw JsonParseException("Cannot parse Date from $json")
                }
            }
        })
        .create()

    inline fun <reified T> fromJson(json: String): T {
        val type: Type = object : TypeToken<T>() {}.type
        return gson.fromJson(json, type)
    }

    inline fun <reified T> toJson(obj: T): String =
        gson.toJson(obj)
}
