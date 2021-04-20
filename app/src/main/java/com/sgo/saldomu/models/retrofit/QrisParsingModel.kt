package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.SerializedName

data class QrisParsingModel(

        @field:SerializedName("error_message")
        val errorMessage: String? = null,

        @field:SerializedName("merchant_pan")
        val merchantPan: String? = null,

        @field:SerializedName("merchant_details")
        val merchantDetails: String? = null,

        @field:SerializedName("merchant_city")
        val merchantCity: String? = null,

        @field:SerializedName("transaction_amount")
        val transactionAmount: String? = null,

        @field:SerializedName("additional_field")
        val additionalField: String? = null,

        @field:SerializedName("merchant_qris_type")
        val merchantQrisType: String? = null,

        @field:SerializedName("merchant_name")
        val merchantName: String? = null,

        @field:SerializedName("nmid")
        val nmid: String? = null,

        @field:SerializedName("ref")
        val ref: String? = null,

        @field:SerializedName("merchant_criteria")
        val merchantCriteria: String? = null,

        @field:SerializedName("merchant_country")
        val merchantCountry: String? = null,

        @field:SerializedName("error_code")
        val errorCode: String? = null,

        @field:SerializedName("postal_code")
        val postalCode: String? = null,

        @field:SerializedName("terminal_id")
        val terminalId: String? = null,

        @field:SerializedName("merchant_type")
        val merchantType: String? = null,

        @field:SerializedName("comm_id")
        val commId: String? = null,

        @field:SerializedName("member_id")
        val memberId: String? = null,

        @field:SerializedName("merchant_id")
        val merchantId: String? = null,

        @field:SerializedName("merchant_store_id")
        val merchantStoreId: String? = null,

        @field:SerializedName("indicator_type")
        val indicatorType: String? = null,

        @field:SerializedName("fee_amount")
        val feeAmount: String? = null,

        @field:SerializedName("admin_fee")
        val adminFee: String? = null,

        @field:SerializedName("nns_member_name")
        val nnsMemberName: String? = null
)
