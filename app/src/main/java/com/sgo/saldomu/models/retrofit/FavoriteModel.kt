package com.sgo.saldomu.models.retrofit

class FavoriteModel(
        val user_id : String,
        val customer_id : String,
        val comm_id : String,
        val tx_favorite_type: String,
        val product_type: String,
        val notes: String,
        val item_id: String,
        val item_name: String,
        val comm_name: String,
        val benef_bank_code: String
): jsonModel()