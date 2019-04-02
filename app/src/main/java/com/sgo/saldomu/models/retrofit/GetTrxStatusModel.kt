package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class GetTrxStatusModel : jsonModel() {
    @SerializedName("tx_status")
    @Expose
    val tx_status: String? = ""
    @SerializedName("created")
    @Expose
    val created: String? = ""
    @SerializedName("tx_remark")
    @Expose
    val tx_remark: String? = ""
    @SerializedName("biller_detail")
    @Expose
    val biller_detail: Any? = null
    @SerializedName("merchant_type")
    @Expose
    val merchant_type: String? = ""
    @SerializedName("buss_scheme_code")
    @Expose
    val buss_scheme_code: String? = ""
    @SerializedName("buss_scheme_name")
    @Expose
    val buss_scheme_name: String? = ""
    @SerializedName("product_name")
    @Expose
    val product_name: String? = ""
    @SerializedName("detail")
    @Expose
    val detail: String? = null
}
