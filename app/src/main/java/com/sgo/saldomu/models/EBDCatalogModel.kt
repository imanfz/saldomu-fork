package com.sgo.saldomu.models

open class EBDCatalogModel(
        var itemImage: String,
        var itemCode: String,
        var itemName: String,
        var price: Int,
        var discAmount: Int,
        var nettPrice: Int,
        var unit: String,
        var minQty: Int,
        var maxQty: Int,
        var remarkMappingUnit: ArrayList<String> = ArrayList(),
        var isFavorite: Boolean,
        var formatQtyItem: ArrayList<FormatQtyItem> = ArrayList())
