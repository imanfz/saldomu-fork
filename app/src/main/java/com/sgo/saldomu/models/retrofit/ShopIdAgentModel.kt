package com.sgo.saldomu.models.retrofit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ShopIdAgentModel {
    @SerializedName("shop_id")
    @Expose
    var shop_id: String? = ""
    @SerializedName("member_id")
    @Expose
    var member_id: String? = ""
    @SerializedName("shop_name")
    @Expose
    var shop_name: String? = ""
    @SerializedName("member_type")
    @Expose
    var member_type: String? = ""
    @SerializedName("member_name")
    @Expose
    var member_name: String? = ""
    @SerializedName("comm_name")
    @Expose
    var comm_name: String? = ""
    @SerializedName("district")
    @Expose
    var district: String? = ""
    @SerializedName("province")
    @Expose
    var province: String? = ""
    @SerializedName("address1")
    @Expose
    var address1: String? = ""
    @SerializedName("is_mobility")
    @Expose
    var is_mobility: String? = ""
}