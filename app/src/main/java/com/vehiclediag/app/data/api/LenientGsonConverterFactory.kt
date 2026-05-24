package com.vehiclediag.app.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.vehiclediag.app.data.model.ApiResponse
import com.vehiclediag.app.data.model.MonitorResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class LenientGsonConverterFactory private constructor(
    private val gson: Gson,
) : Converter.Factory() {

    private val delegate: Converter.Factory = GsonConverterFactory.create(gson)

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? {
        val adapter = gson.getAdapter(com.google.gson.reflect.TypeToken.get(type))
        return GsonRequestBodyConverter(adapter)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        if (type == String::class.java) {
            return StringBodyConverter
        }
        val delegateConverter = delegate.responseBodyConverter(type, annotations, retrofit)
            ?: return null
        return SafeResponseBodyConverter(delegateConverter, type)
    }

    private object StringBodyConverter : Converter<ResponseBody, String> {
        override fun convert(value: ResponseBody): String {
            return value.string()
        }
    }

    private class GsonRequestBodyConverter<T>(
        private val adapter: com.google.gson.TypeAdapter<T>,
    ) : Converter<T, RequestBody> {
        override fun convert(value: T): RequestBody {
            val json = adapter.toJson(value)
            return json.toRequestBody(JSON_MEDIA_TYPE)
        }
    }

    private class SafeResponseBodyConverter(
        private val delegate: Converter<ResponseBody, *>,
        private val type: Type,
    ) : Converter<ResponseBody, Any> {
        override fun convert(value: ResponseBody): Any? {
            val raw = try { value.string() } catch (e: Exception) { null }
            if (raw.isNullOrBlank()) {
                throw JsonParseException("设备返回了空响应")
            }
            if (raw.trim() == "OK") {
                return defaultForType(type)
            }
            return try {
                delegate.convert(
                    ResponseBody.create(value.contentType(), raw)
                )
            } catch (e: JsonParseException) {
                val preview = raw.take(200).replace("\n", "\\n").replace("\r", "\\r")
                throw JsonParseException(
                    "设备返回了非JSON数据 (${raw.length}字节): $preview"
                )
            }
        }

        private fun defaultForType(type: Type): Any? {
            return when (type) {
                ApiResponse::class.java -> ApiResponse(success = true, message = "OK")
                MonitorResponse::class.java -> MonitorResponse(enabled = true, messages = emptyList())
                else -> {
                    val rawType = if (type is java.lang.reflect.ParameterizedType) type.rawType else type
                    if (rawType == ApiResponse::class.java || rawType == MonitorResponse::class.java) {
                        defaultForType(rawType)
                    } else {
                        throw JsonParseException("设备返回了 'OK'，但期望的是JSON数据: $type")
                    }
                }
            }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=UTF-8".toMediaType()

        fun create(): LenientGsonConverterFactory {
            val gson = GsonBuilder().setLenient().create()
            return LenientGsonConverterFactory(gson)
        }

        fun create(gson: Gson): LenientGsonConverterFactory {
            return LenientGsonConverterFactory(gson)
        }
    }
}
