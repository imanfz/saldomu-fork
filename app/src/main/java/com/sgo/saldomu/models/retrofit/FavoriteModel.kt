package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class FavoriteModel : jsonModel(){
    @SerializedName("user_id")
    @Expose
    val user_id : String = ""

    @SerializedName("customer_id")
    @Expose
    val customer_id : String = ""

    @SerializedName("comm_id")
    @Expose
    val comm_id : String = ""

    @SerializedName("tx_favorite_type")
    @Expose
    val tx_favorite_type: String = ""

    @SerializedName("product_type")
    @Expose
    val product_type: String = ""

    @SerializedName("notes")
    @Expose
    val notes: String = ""

    @SerializedName("item_id")
    @Expose
    val item_id: String = ""

    @SerializedName("item_name")
    @Expose
    val item_name: String = ""

    @SerializedName("comm_name")
    @Expose
    val comm_name: String = ""

    @SerializedName("comm_code")
    @Expose
    val comm_code: String = ""

    @SerializedName("benef_bank_code")
    @Expose
    val benef_bank_code: String = ""
}