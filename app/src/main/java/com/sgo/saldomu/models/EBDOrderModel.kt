package com.sgo.saldomu.models

data class EBDOrderModel(
        var reff_no: String = "",
        var mapping_items: List<MappingItemsItem> = ArrayList()
)

data class MappingItemsItem(
        var item_code: String = "",
        var unit: String = "",
        var price: Int = 0,
        var item_name: String = "",
        var format_qty: List<FormatQtyItem> = ArrayList()
)

data class FormatQtyItem(
        var mapping_unit: String = "",
        var mapping_qty: Int = 0
)