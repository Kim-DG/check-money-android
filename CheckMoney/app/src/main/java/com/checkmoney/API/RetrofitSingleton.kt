package com.checkmoney

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuild {

    val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val client: OkHttpClient = OkHttpClient.Builder() .addInterceptor(interceptor) .build()

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://ec2-3-38-105-161.ap-northeast-2.compute.amazonaws.com:3001")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(API::class.java)
}