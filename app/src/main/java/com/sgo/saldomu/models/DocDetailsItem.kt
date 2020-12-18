package com.sgo.saldomu.models

data class DocDetailsItem(
        var reff_no: String = "",
        var mapping_items: List<MappingItemsItem> = ArrayList()
)

data class MappingItemsItem(
        var item_code: String = "",
        var unit: String = "",
        var price: Int = 0,
        var item_name: String = "",
        var format_qty: List<FormatQtyItem> = ArrayList(),
        val subtotal_amount: Int = 0
)

data class FormatQtyItem(
        var mapping_unit: String = "",
        var mapping_qty: Int = 0
)