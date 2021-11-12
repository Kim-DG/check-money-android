package com.checkmoney

import com.google.android.gms.common.internal.safeparcel.SafeParcelable
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

    @POST("api/auth/refresh")
    fun postRefresh(@Body refreshToken: RefreshToken): Call<ResultAndToken>

    @GET("accounts")
    fun getAccount(@Header("access_token") access_token: String): Call<ResultAccountList>

    @POST("accounts")
    fun postAccount(@Header("access_token") access_token: String, @Body account: Account): Call<ResultAccount>

    @PUT("accounts/{accountId}")
    fun putAccount(@Header("access_token") access_token: String, @Path("accountId") accountId: Int, @Body account: Account): Call<Result>

    @DELETE("accounts/{accountId}")
    fun deleteAccount(@Header("access_token") access_token: String, @Path("accountId") accountId: Int): Call<Result>
}