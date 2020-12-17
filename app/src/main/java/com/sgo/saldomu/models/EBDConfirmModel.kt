package com.sgo.saldomu.models

data class EBDConfirmModel(
        val errorMessage: String = "",
        val paymentMethods: PaymentMethods = PaymentMethods(),
        val amount: Int = 0,
        val ref: String = "",
        val totalAmount: Int = 0,
        val discountAmount: Int = 0,
        val docDetails: List<DocDetailsItem>,
        val errorCode: String = ""
)

data class PaymentMethods(
        val saldomu: String = "saldomu"
)

data class DocDetailsItem(
        val reffNo: String = "",
        val mappingItems: List<MappingItemsItem> = ArrayList()
)

