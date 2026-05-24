package com.vehiclediag.app.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl = "http://192.168.4.1"
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    fun updateBaseUrl(url: String) {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        if (normalizedUrl != baseUrl) {
            baseUrl = normalizedUrl
            retrofit = null
            apiService = null
        }
    }

    fun getApiService(): ApiService {
        if (apiService == null) {
            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            retrofit = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService!!
    }

    suspend fun rawGet(endpoint: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = if (baseUrl.endsWith("/")) "$baseUrl${endpoint.removePrefix("/")}" else "$baseUrl/${endpoint.removePrefix("/")}"
            val request = okhttp3.Request.Builder()
                .url(url)
                .build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: "(empty body)"
            val headers = response.headers.joinToString("\n") { "${it.first}: ${it.second}" }
            Result.success("HTTP ${response.code()}\n${headers}\n\n${body}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
