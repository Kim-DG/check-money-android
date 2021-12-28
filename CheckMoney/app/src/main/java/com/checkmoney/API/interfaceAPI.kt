package com.checkmoney

import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface API{
    @POST("api/auth/request/email")
    fun postEmail(@Body email: Email): Call<Result>

    @POST("api/auth/login/google")
    fun postGoogle(@Body idToken: IdToken): Call<ResultAndToken>

    @POST("api/auth/request/email-for-pwd")
    fun postEmailForPwd(@Body email: Email): Call<Result>

    @POST("api/auth/confirm")
    fun postAuth(@Body authConfirm: AuthConfirm): Call<Result>

    @POST("api/auth/join")
    fun postJoin(@Body join: Join): Call<Result>

    @POST("api/auth/login/email")
    fun postLogin(@Body userInfo: UserInfo): Call<ResultAndToken>

    @POST("api/auth/refresh")
    fun postRefresh(@Body refreshToken: RefreshToken): Call<ResultAndToken>

    @POST("api/auth/find-pwd")
    fun postFindPwd(@Body emailPwd: EmailPwd): Call<Result>

    @GET("api/accounts")
    fun getAccount(@Header("Authorization") access_token: String, @QueryMap page: Map<String, Int>): Call<ResultAccountList>
    //@QueryMap page: Map<String, String>

    @POST("api/accounts")
    fun postAccount(@Header("Authorization") access_token: String, @Body account: Account): Call<ResultId>

    @PUT("api/accounts/{accountId}")
    fun putAccount(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int, @Body account: Account): Call<Result>

    @DELETE("api/accounts/{accountId}")
    fun deleteAccount(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int): Call<Result>

    @GET("api/accounts/{accountId}/transactions")
    fun getTransaction(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int, @QueryMap page: Map<String, Int>): Call<ResultTransactions>

    @GET("api/transactions")
    fun getAllTransaction(@Header("Authorization") access_token: String, @QueryMap page: Map<String, Int>): Call<ResultTransactions>

    @POST("api/transactions")
    fun postTransaction(@Header("Authorization") access_token: String, @Body transaction: Transaction): Call<ResultId>

    @PUT("api/transactions/{transactionId}")
    fun putTransaction(@Header("Authorization") access_token: String, @Path("transactionId") transactionId: Int, @Body transaction: EditTransaction): Call<Result>

    @DELETE ("api/transactions/{transactionId}")
    fun deleteTransaction(@Header("Authorization") access_token: String, @Path("transactionId") transactionId: Int): Call<Result>

    @GET("api/users/my-info")
    fun getMyInfo(@Header("Authorization") access_token: String): Call<ResultMyInfo>

    @PUT("api/users/my-info")
    fun putMyInfo(@Header("Authorization") access_token: String, @Body editMyInfo: EditMyInfo): Call<Result>

    @Multipart
    @POST("api/users/img")
    fun postImage(@Header("Authorization") access_token: String, @Part file: MultipartBody.Part): Call<ResultImageUrl>

    @GET("api/accounts/{accountId}/subscriptions")
    fun getSubscriptions(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int, @QueryMap page: Map<String, Int>): Call<ResultTransactions>

    @POST("api/accounts/{accountId}/subscriptions")
    fun postSubscriptions(@Header("Authorization") access_token: String, @Body transaction: Transaction, @Path("accountId") accountId: Int): Call<ResultId>

    @PUT("api/accounts/{accountId}/subscriptions/{subscriptionId}")
    fun putSubscriptions(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int, @Path("subscriptionId") subscriptionId: Int, @Body subscription: EditTransaction): Call<Result>

    @DELETE ("api/accounts/{accountId}/subscriptions/{subscriptionId}")
    fun deleteSubscriptions(@Header("Authorization") access_token: String, @Path("accountId") accountId: Int, @Path("subscriptionId") subscriptionId: Int): Call<Result>
}