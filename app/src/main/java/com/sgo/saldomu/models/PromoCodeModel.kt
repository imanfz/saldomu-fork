package com.sgo.saldomu.models

open class PromoCodeModel(promoCodeName: String, promoCodeQty: String, status: String) : Comparable<PromoCodeModel> {
    var code: String = promoCodeName
    var qty: String = promoCodeQty
    var status: String = ""
    override fun compareTo(other: PromoCodeModel): Int {
        return if (code == other.code)
            0
        else
            1
    }
}