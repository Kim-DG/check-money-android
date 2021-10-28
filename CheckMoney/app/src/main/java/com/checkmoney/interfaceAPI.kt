package com.checkmoney

import retrofit2.Call
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface API{
    @POST("/api/auth/request/email")
    fun postEmail(@Query("email") email: String): Call<String>

    @POST("api/auth/login/google")
    fun postGoogle(@Query("id_Token") id_token: String): Call<String>
}