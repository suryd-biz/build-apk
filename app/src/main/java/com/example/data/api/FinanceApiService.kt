package com.example.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FinanceApiService {

    // --- WALLET ENDPOINTS ---
    @FormUrlEncoded
    @POST
    suspend fun postWalletAction(
        @Url url: String,
        @FieldMap fields: Map<String, String>
    ): Response<ResponseBody>

    @GET
    suspend fun getWalletAction(
        @Url url: String,
        @QueryMap queries: Map<String, String>
    ): Response<ResponseBody>

    // --- ASSET ENDPOINTS ---
    @Headers("Content-Type: application/json")
    @POST
    suspend fun postAssetAction(
        @Url url: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ResponseBody>

    @GET
    suspend fun getAssetAction(
        @Url url: String,
        @QueryMap queries: Map<String, String>
    ): Response<ResponseBody>
}
