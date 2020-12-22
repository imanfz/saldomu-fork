package com.sgo.saldomu.models.retrofit

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sgo.saldomu.models.FormatQty
import com.sgo.saldomu.models.FormatQtyItem

data class ItemModel(var itemName: String = "", var itemCode: String = "", var price: String = "", var unit: String = "", var subTotalAmount: String = "", var formatQty: ArrayList<FormatQty> = ArrayList())