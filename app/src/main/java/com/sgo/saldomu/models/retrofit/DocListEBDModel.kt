package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.SerializedName

data class DocListEBDModel(

        @field:SerializedName("error_message")
        val errorMessage: String,

        @field:SerializedName("payment_methods")
        val paymentMethods: List<PaymentMethodsItem>,

        @field:SerializedName("total")
        val total: Int,

        @field:SerializedName("ref")
        val ref: String,

        @field:SerializedName("member_code_espay")
        val memberCodeEspay: String,

        @field:SerializedName("cust_id_espay")
        val custIdEspay: String,

        @field:SerializedName("doc_list")
        val docList: List<DocListItem>,

        @field:SerializedName("perPage")
        val perPage: String,

        @field:SerializedName("comm_code_espay")
        val commCodeEspay: String,

        @field:SerializedName("lastPage")
        val lastPage: Int,

        @field:SerializedName("partner_code_espay")
        val partnerCodeEspay: String,

        @field:SerializedName("error_code")
        val errorCode: String
)

data class DocListItem(

        @field:SerializedName("reff_no")
        val reffNo: String,

        @field:SerializedName("paid_status")
        val paidStatus: String,

        @field:SerializedName("paid_status_remark")
        val paidStatusRemark: String,

        @field:SerializedName("member_code")
        val memberCode: String,

        @field:SerializedName("type_id")
        val typeId: String,

        @field:SerializedName("doc_no")
        val docNo: String,

        @field:SerializedName("due_date")
        val dueDate: String,

        @field:SerializedName("created_at")
        val createdAt: String,

        @field:SerializedName("comm_code")
        val commCode: String,

        @field:SerializedName("reff_id")
        val reffId: String,

        @field:SerializedName("promo")
        val promo: List<PromoItem>,

        @field:SerializedName("issue_date")
        val issueDate: String,

        @field:SerializedName("total_amount")
        val totalAmount: String,

        @field:SerializedName("doc_status")
        val docStatus: String,

        @field:SerializedName("cust_id")
        val custId: String,

        @field:SerializedName("nett_amount")
        val nett_amount: String
)

data class PaymentMethodsItem(

        @field:SerializedName("payment_name")
        val paymentName: String,

        @field:SerializedName("payment_code")
        val paymentCode: String
)

data class PromoItem(

        @field:SerializedName("total_disc")
        val totalDisc: Int
)
