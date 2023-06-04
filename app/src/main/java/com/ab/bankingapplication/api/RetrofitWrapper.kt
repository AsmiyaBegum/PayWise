package com.ab.bankingapplication.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitWrapper {
    private const val BASE_URL = "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso/"

    // Create an instance of HttpLoggingInterceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set the desired log level
    }

    // Create an instance of OkHttpClient and add the logging interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val transactionService: TransactionApi by lazy {
        retrofit.create(TransactionApi::class.java)
    }
}