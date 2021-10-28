package com.sgo.saldomu.models.retrofit

import com.sgo.saldomu.models.FormatQty

data class ItemModel(var itemName: String = "", var itemCode: String = "", var price: String = "", var unit: String = "", var subTotalAmount: String = "", var formatQty: ArrayList<FormatQty> = ArrayList())