package com.sgo.saldomu.models.retrofit

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sgo.saldomu.models.FormatQty

class ItemModel() : Parcelable {
    @SerializedName("item_code")
    @Expose
    var item_code: String? = null

    @SerializedName("item_name")
    @Expose
    var item_name: String? = null

    @SerializedName("unit")
    @Expose
    var unit: String? = null

    @SerializedName("price")
    @Expose
    var price: String? = null

    @SerializedName("format_qty")
    @Expose
    var format_qty: List<FormatQty>? = null

    constructor(parcel: Parcel) : this() {
        item_code = parcel.readString()
        item_name = parcel.readString()
        unit = parcel.readString()
        price = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(item_code)
        parcel.writeString(item_name)
        parcel.writeString(unit)
        parcel.writeString(price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemModel> {
        override fun createFromParcel(parcel: Parcel): ItemModel {
            return ItemModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemModel?> {
            return arrayOfNulls(size)
        }
    }


}