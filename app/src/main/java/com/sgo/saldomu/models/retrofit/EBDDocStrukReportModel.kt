package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.SerializedName

data class EBDDocStrukReportModel(

        @field:SerializedName("error_message")
        val errorMessage: String? = null,

        @field:SerializedName("amount")
        val amount: String? = null,

        @field:SerializedName("paid_status_remark")
        val paidStatusRemark: String? = null,

        @field:SerializedName("discount_amount")
        val discountAmount: String? = null,

        @field:SerializedName("doc_no")
        val docNo: String? = null,

        @field:SerializedName("payment_method_remark")
        val paymentMethodRemark: String? = null,

        @field:SerializedName("due_date")
        val dueDate: String? = null,

        @field:SerializedName("issue_date")
        val issue_date: String? = null,

        @field:SerializedName("ref")
        val ref: String? = null,

        @field:SerializedName("total_amount")
        val totalAmount: String? = null,

        @field:SerializedName("partner_code_espay")
        val partnerCodeEspay: String? = null,

        @field:SerializedName("paid_amount")
        val paidAmount: String? = null,

        @field:SerializedName("error_code")
        val errorCode: String? = null,

        @field:SerializedName("bonus_items")
        val bonusItems: List<ItemsItem?>? = null,

        @field:SerializedName("items")
        val items: List<ItemsItem?>? = null
)

data class ItemsItem(

        @field:SerializedName("item_code")
        val itemCode: String? = null,

        @field:SerializedName("price")
        val price: String? = null,

        @field:SerializedName("subtotal")
        val subtotal: Int? = null,

        @field:SerializedName("item_name")
        val itemName: String? = null,

        @field:SerializedName("formatted_qty")
        val formattedQty: String? = null,

        @field:SerializedName("doc_type_remark")
        val doc_type_remark: String? = ""
)
