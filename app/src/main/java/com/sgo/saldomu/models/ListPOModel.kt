package com.sgo.saldomu.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ListPOModel() : Parcelable {
    @SerializedName("doc_no")
    @Expose
    var doc_no: String? = ""

    @SerializedName("doc_status")
    @Expose
    var doc_status: String? = ""

    @SerializedName("total_amount")
    @Expose
    var total_amount: String? = ""

    @SerializedName("due_date")
    @Expose
    var due_date: String? = ""

    @SerializedName("cust_id")
    @Expose
    var cust_id: String? = ""

    @SerializedName("member_code")
    @Expose
    var member_code: String? = ""

    @SerializedName("comm_code")
    @Expose
    var comm_code: String? = ""


    @SerializedName("type_id")
    @Expose
    var type_id: String? = ""


    @SerializedName("reff_no")
    @Expose
    var reff_no: String? = ""


    @SerializedName("reff_id")
    @Expose
    var reff_id: String? = ""

    constructor(parcel: Parcel) : this() {
        doc_no = parcel.readString()
        doc_status = parcel.readString()
        total_amount = parcel.readString()
        due_date = parcel.readString()
        cust_id = parcel.readString()
        member_code = parcel.readString()
        comm_code = parcel.readString()
        type_id = parcel.readString()
        reff_no = parcel.readString()
        reff_id = parcel.readString()
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ListPOModel> {
        override fun createFromParcel(parcel: Parcel): ListPOModel {
            return ListPOModel(parcel)
        }

        override fun newArray(size: Int): Array<ListPOModel?> {
            return arrayOfNulls(size)
        }
    }
}