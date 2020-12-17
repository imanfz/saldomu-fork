package com.sgo.saldomu.models

data class EBDConfirmModel(
        var payment_methods: List<PaymentMethods> = ArrayList(),
        var amount: Int = 0,
        var ref: String = "",
        var total_amount: Int = 0,
        var discount_amount: Int = 0,
        var doc_details: List<DocDetailsItem> = ArrayList()
)

data class PaymentMethods(
        val payment_name: String = "",
        val payment_code: String = ""
)

