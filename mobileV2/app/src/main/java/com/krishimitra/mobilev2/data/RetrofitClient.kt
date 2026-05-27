package com.krishimitra.mobilev2.data

import com.krishimitra.mobilev2.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.krishimitra.mobilev2.data.api.AuthApi
import com.krishimitra.mobilev2.data.api.DiseaseApi
import com.krishimitra.mobilev2.data.api.AdvisoryApi
import com.krishimitra.mobilev2.data.api.WeatherApi
import com.krishimitra.mobilev2.data.api.MandiApi
import com.krishimitra.mobilev2.data.api.FarmerApi
import com.krishimitra.mobilev2.data.api.NotifyApi

/**
 * Singleton Retrofit client for API communication.
 * Configured with logging interceptor and base URL.
 */
object RetrofitClient {

    private val BASE_URL = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val sessionManager = SessionManager(com.krishimitra.mobilev2.KrishiMitraApp.getContext())
            val token = sessionManager.getAuthToken()
            
            val requestBuilder = original.newBuilder()
            if (token != null) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val farmerApi: FarmerApi by lazy {
        retrofit.create(FarmerApi::class.java)
    }

    val diseaseApi: DiseaseApi by lazy {
        retrofit.create(DiseaseApi::class.java)
    }

    val advisoryApi: AdvisoryApi by lazy {
        retrofit.create(AdvisoryApi::class.java)
    }

    val weatherApi: WeatherApi by lazy {
        retrofit.create(WeatherApi::class.java)
    }

    val mandiApi: MandiApi by lazy {
        retrofit.create(MandiApi::class.java)
    }

    val notifyApi: NotifyApi by lazy {
        retrofit.create(NotifyApi::class.java)
    }
}