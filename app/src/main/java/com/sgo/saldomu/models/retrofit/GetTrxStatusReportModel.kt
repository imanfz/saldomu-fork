package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class GetTrxStatusReportModel : GetTrxStatusModel() {
    @SerializedName("tx_id")
    @Expose
    val tx_id: String? = ""
    @SerializedName("ccy_id")
    @Expose
    val ccy_id: String? = ""
    @SerializedName("payment_bank")
    @Expose
    val payment_bank: String? = ""
    @SerializedName("payment_phone")
    @Expose
    val payment_phone: String? = ""
    @SerializedName("payment_name")
    @Expose
    val payment_name: String? = ""
    @SerializedName("payment_remark")
    @Expose
    val payment_remark: String? = ""
    @SerializedName("tx_amount")
    @Expose
    val tx_amount: String? = ""
    @SerializedName("admin_fee")
    @Expose
    val admin_fee: String? = ""
    @SerializedName("tx_fee")
    @Expose
    val tx_fee: String? = ""
    @SerializedName("fee_amount")
    @Expose
    val fee_amount: String? = ""
    @SerializedName("total_amount")
    @Expose
    val total_amount: String? = ""
    @SerializedName("member_phone")
    @Expose
    val member_phone: String? = ""
    @SerializedName("member_cust_id")
    @Expose
    val member_cust_id: String? = ""
    @SerializedName("member_name")
    @Expose
    val member_name: String? = ""
    @SerializedName("tx_bank_name")
    @Expose
    val tx_bank_name: String? = ""
    @SerializedName("source_bank_name")
    @Expose
    val source_bank_name: String? = ""
    @SerializedName("source_acct_no")
    @Expose
    val source_acct_no: String? = ""
    @SerializedName("source_acct_name")
    @Expose
    val source_acct_name: String? = ""
    @SerializedName("benef_bank_name")
    @Expose
    val benef_bank_name: String? = ""
    @SerializedName("benef_acct_no")
    @Expose
    val benef_acct_no: String? = ""
    @SerializedName("benef_acct_name")
    @Expose
    val benef_acct_name: String? = ""
    @SerializedName("benef_acct_type")
    @Expose
    val benef_acct_type: String? = ""
    @SerializedName("member_shop_phone")
    @Expose
    val member_shop_phone: String? = ""
    @SerializedName("member_shop_name")
    @Expose
    val member_shop_name: String? = ""
    @SerializedName("otp_member")
    @Expose
    val otp_member: String? = ""
    @SerializedName("comm_code")
    @Expose
    val comm_code: String? = ""
    @SerializedName("member_code")
    @Expose
    val member_code: String? = ""
    @SerializedName("denom_detail")
    @Expose
    val denom_detail: List<String>? = ArrayList()
    @SerializedName("order_id")
    @Expose
    val order_id: String? = ""
    @SerializedName("member_shop_no")
    @Expose
    val member_shop_no: String? = ""
    @SerializedName("product_h2h")
    @Expose
    val product_h2h: String? = ""
    @SerializedName("product_code")
    @Expose
    val product_code: String? = ""
    @SerializedName("comm_id")
    @Expose
    val comm_id: String? = ""
    @SerializedName("productName")
    @Expose
    val productName: String? = ""
    @SerializedName("payment_type_desc")
    @Expose
    val payment_type_desc: String? = ""
    @SerializedName("dgi_member_name")
    @Expose
    val dgi_member_name: String? = ""
    @SerializedName("dgi_anchor_name")
    @Expose
    val dgi_anchor_name: String? = ""
    @SerializedName("dgi_comm_name")
    @Expose
    val dgi_comm_name: String? = ""
    @SerializedName("member_cust_name")
    @Expose
    val member_cust_name: String? = ""
    @SerializedName("invoice")
    @Expose
    val invoice: String? = ""
    @SerializedName("benef_product_code")
    @Expose
    val benef_product_code: String? = ""
    @SerializedName("additional_fee")
    @Expose
    val additional_fee: String? = ""
    @SerializedName("comm_name")
    @Expose
    val comm_name: String? = ""
    @SerializedName("store_name")
    @Expose
    val store_name: String? = ""
    @SerializedName("store_address")
    @Expose
    val store_address: String? = ""
}
