package com.example.cargolive.data.api

import android.content.Context
import android.util.Log
import com.example.cargolive.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private lateinit var retrofit: Retrofit
    private lateinit var baseUrl: String

    fun init(context: Context) {
        baseUrl = context.getString(R.string.backend_url)
        Log.d(TAG, "Using BASE_URL: $baseUrl")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                try {
                    val request = chain.request()
                    Log.d(TAG, "Making request to: ${request.url}")
                    val response = chain.proceed(request)

                    if (!response.isSuccessful) {
                        Log.e(TAG, "Request failed: ${response.code} - ${response.message}")
                    }

                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Network error during request", e)
                    throw e
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}
