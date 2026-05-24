package com.vehiclediag.app.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import okhttp3.RequestBody
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
        return delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    override fun responseBodyConverter(
        val delegateConverter = delegate.responseBodyConverter(type, annotations, retrofit)
            ?: return null
        return SafeResponseBodyConverter(delegateConverter, type)
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
    }

    companion object {
        fun create(): LenientGsonConverterFactory {
            val gson = GsonBuilder().setLenient().create()
            return LenientGsonConverterFactory(gson)
        }

        fun create(gson: Gson): LenientGsonConverterFactory {
            return LenientGsonConverterFactory(gson)
        }
    }
}
