package com.sgo.saldomu.models

open class PromoCodeModel(promoCodeName: String, promoCodeQty: String, status: String) {
    var code: String = promoCodeName
    var qty: String = promoCodeQty
    var status: String = ""
}