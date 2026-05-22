package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalletListResponse(
    @Json(name = "wallets") val wallets: List<WalletApiModel>? = null
)

@JsonClass(generateAdapter = true)
data class WalletApiModel(
    @Json(name = "id") val id: String,
    @Json(name = "nama") val nama: String,
    @Json(name = "hasPin") val hasPin: Boolean
)

@JsonClass(generateAdapter = true)
data class TransactionListResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: List<TransactionApiModel>? = null
)

@JsonClass(generateAdapter = true)
data class TransactionApiModel(
    @Json(name = "id") val id: String,
    @Json(name = "tanggal") val tanggal: String,
    @Json(name = "jenis") val jenis: String,
    @Json(name = "kategori") val kategori: String,
    @Json(name = "nominal") val nominal: Double,
    @Json(name = "keterangan") val keterangan: String? = null,
    @Json(name = "wallet") val wallet: String
)

@JsonClass(generateAdapter = true)
data class AssetProfileListResponse(
    @Json(name = "assets") val assets: List<AssetApiModel>? = null
)

@JsonClass(generateAdapter = true)
data class AssetApiModel(
    @Json(name = "id") val id: String,
    @Json(name = "nama") val nama: String,
    @Json(name = "hasPin") val hasPin: Boolean
)

@JsonClass(generateAdapter = true)
data class AssetHistoryResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: List<AssetSnapshotApiModel>? = null
)

@JsonClass(generateAdapter = true)
data class AssetSnapshotApiModel(
    @Json(name = "raw_tanggal") val raw_tanggal: String? = null,
    @Json(name = "tanggal") val tanggal: String? = null,
    @Json(name = "bni") val bni: Double? = 0.0,
    @Json(name = "seabank") val seabank: Double? = 0.0,
    @Json(name = "bibit") val bibit: Double? = 0.0,
    @Json(name = "tunai") val tunai: Double? = 0.0,
    @Json(name = "flip") val flip: Double? = 0.0,
    @Json(name = "ovo") val ovo: Double? = 0.0,
    @Json(name = "shopeepay") val shopeepay: Double? = 0.0,
    @Json(name = "dana") val dana: Double? = 0.0,
    @Json(name = "jago") val jago: Double? = 0.0,
    @Json(name = "lain") val lain: Double? = 0.0,
    @Json(name = "BNI") val BNI: Double? = null,
    @Json(name = "SEABANK") val SEABANK: Double? = null,
    @Json(name = "BIBIT") val BIBIT: Double? = null,
    @Json(name = "TUNAI") val TUNAI: Double? = null,
    @Json(name = "FLIP") val FLIP: Double? = null,
    @Json(name = "OVO") val OVO: Double? = null,
    @Json(name = "SHOPEEPAY") val SHOPEEPAY: Double? = null,
    @Json(name = "DANA") val DANA: Double? = null,
    @Json(name = "JAGO") val JAGO: Double? = null,
    @Json(name = "LAINNYA") val LAINNYA: Double? = null,
    @Json(name = "total") val total: Double? = 0.0,
    @Json(name = "keterangan") val keterangan: String? = null
) {
    // Getter wrappers to map either lowercase or UPPERCASE API keys gracefully
    fun getBniVal() : Double = BNI ?: bni ?: 0.0
    fun getSeabankVal() : Double = SEABANK ?: seabank ?: 0.0
    fun getBibitVal() : Double = BIBIT ?: bibit ?: 0.0
    fun getTunaiVal() : Double = TUNAI ?: tunai ?: 0.0
    fun getFlipVal() : Double = FLIP ?: flip ?: 0.0
    fun getOvoVal() : Double = OVO ?: ovo ?: 0.0
    fun getShopeepayVal() : Double = SHOPEEPAY ?: shopeepay ?: 0.0
    fun getDanaVal() : Double = DANA ?: dana ?: 0.0
    fun getJagoVal() : Double = JAGO ?: jago ?: 0.0
    fun getLainVal() : Double = LAINNYA ?: lain ?: 0.0
}
