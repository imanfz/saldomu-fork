package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProviderModel : jsonModel() {
    @SerializedName("lending_code")
    @Expose
    var lending_code: String? = null

    @SerializedName("lending_name")
    @Expose
    var lending_name: String? = null

    @SerializedName("review_url")
    @Expose
    var review_url: String? = null

}