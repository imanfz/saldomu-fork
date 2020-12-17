package com.sgo.saldomu.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ListPOModel {
    @SerializedName("doc_no")
    @Expose
    var doc_no: String? = null

    @SerializedName("doc_status")
    @Expose
    var doc_status: String? = null

    @SerializedName("total_amount")
    @Expose
    var total_amount: String? = null

    @SerializedName("due_date")
    @Expose
    var due_date: String? = null

    @SerializedName("cust_id")
    @Expose
    var cust_id: String? = null

    @SerializedName("member_code")
    @Expose
    var member_code: String? = null

    @SerializedName("comm_code")
    @Expose
    var comm_code: String? = null

    @SerializedName("paid_status")
    @Expose
    var paid_status: String? = null
}