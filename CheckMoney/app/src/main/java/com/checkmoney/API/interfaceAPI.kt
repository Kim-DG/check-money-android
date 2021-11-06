package com.checkmoney

import retrofit2.Call
import retrofit2.http.*

interface API{
    @POST("api/auth/request/email")
    fun postEmail(@Body email: Email): Call<Result>

    @POST("api/auth/login/google")
    fun postGoogle(@Body idToken: IdToken): Call<ResultAndToken>

    @POST("api/auth/confirm")
    fun postAuth(@Body authConfirm: AuthConfirm): Call<Result>

    @POST("api/auth/join")
    fun postJoin(@Body join: Join): Call<Result>

    @POST("api/auth/login/email")
    fun postLogin(@Body userInfo: UserInfo): Call<ResultAndToken>
}