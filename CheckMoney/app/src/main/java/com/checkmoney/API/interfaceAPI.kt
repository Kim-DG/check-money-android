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

    @GET("api/accounts")
    fun getAccount(@Header("Authorization") access_token: String): Call<ResultAccountList>

    @POST("api/accounts")
    fun postAccount(@Header("Authorization") access_token: String, @Body account: Account): Call<ResultId>

    @PUT("api/accounts/{accountId}")
    fun putAccount(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int, @Body account: Account): Call<Result>

    @DELETE("api/accounts/{accountId}")
    fun deleteAccount(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int): Call<Result>

    @GET("api/accounts/{accountId}/transactions")
    fun getTransaction(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int): Call<ResultTransactions>

    @POST("api/transactions")
    fun postTransaction(@Header("Authorization") access_token: String, @Body transaction: Transaction): Call<ResultId>

    @PUT("api/transactions/{transactionId}")
    fun putTransaction(@Header("Authorization") access_token: String, @Path("transactionId") transactionId: Int, @Body transaction: EditTransaction): Call<Result>

    @DELETE ("api/transactions/{transactionId}")
    fun deleteTransaction(@Header("Authorization") access_token: String, @Path("transactionId") transactionId: Int): Call<Result>
}