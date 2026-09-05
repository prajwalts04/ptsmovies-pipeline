package com.pts.suite.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PREFS_NAME = "pts_suite_prefs"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val DEFAULT_URL = "https://hub.ptsmovies.online"

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = DEFAULT_URL

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getServerUrl(context: Context): String {
        return getPrefs(context).getString(KEY_SERVER_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun setServerUrl(context: Context, url: String) {
        val cleanUrl = if (!url.endsWith("/")) "$url/" else url
        getPrefs(context).edit().putString(KEY_SERVER_URL, cleanUrl).apply()
        currentBaseUrl = cleanUrl
        retrofit = null // Recreate client with new base URL
    }

    fun getAuthToken(context: Context): String? {
        return getPrefs(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun setAuthToken(context: Context, token: String?) {
        getPrefs(context).edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getService(context: Context): ApiService {
        val targetUrl = getServerUrl(context)
        val cleanTargetUrl = if (!targetUrl.endsWith("/")) "$targetUrl/" else targetUrl

        if (retrofit == null || currentBaseUrl != cleanTargetUrl) {
            currentBaseUrl = cleanTargetUrl

            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val token = getAuthToken(context)
                val requestBuilder = original.newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}
