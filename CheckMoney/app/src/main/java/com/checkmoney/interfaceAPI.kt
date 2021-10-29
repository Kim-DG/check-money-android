package com.checkmoney

import retrofit2.Call
import retrofit2.http.*

interface API{
    @POST("api/auth/request/email")
    fun postEmail(@Body email: Email): Call<Result>

    @POST("api/auth/login/google")
    fun postGoogle(@Body idToken: IdToken): Call<IdToken>

    @POST("api/auth/confirm")
    fun postAuth(@Body authConfirm: AuthConfirm): Call<Result>
}