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
    @SerializedName("merchant_name")
    @Expose
    val merchant_name: String? = ""
    @SerializedName("merchant_city")
    @Expose
    val merchant_city: String? = ""
    @SerializedName("merchant_pan")
    @Expose
    val merchant_pan: String? = ""
    @SerializedName("terminal_id")
    @Expose
    val terminal_id: String? = ""
    @SerializedName("trx_id_ref")
    @Expose
    val trx_id_ref: String? = ""
    @SerializedName("indicator_type")
    @Expose
    val indicator_type: String? = ""
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
    @SerializedName("tx_status_remark")
    @Expose
    val tx_status_remark: String? = ""

    @SerializedName("biller_type")
    @Expose
    val biller_type: String? = ""

    @SerializedName("doc_type_remark")
    @Expose
    val doc_type_remark: String? = ""

    @SerializedName("payment_remark")
    @Expose
    val payment_remark: String? = ""


}
