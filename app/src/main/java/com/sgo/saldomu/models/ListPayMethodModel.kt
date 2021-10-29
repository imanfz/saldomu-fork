package com.sgo.saldomu.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ListPayMethodModel() {
    @SerializedName("payment_code")
    @Expose
    var payment_code: String? = ""

    @SerializedName("payment_name")
    @Expose
    var payment_name: String? = ""
    
}