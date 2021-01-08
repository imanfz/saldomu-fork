package com.sgo.saldomu.models

open class EBDCatalogModel(
        var itemCode: String,
        var itemName: String,
        var price: Int,
        var unit: String,
        var minQty: Int,
        var maxQty: Int,
        var remarkMappingUnit: ArrayList<String> = ArrayList(),
        var formatQtyItem: ArrayList<FormatQtyItem> = ArrayList())
