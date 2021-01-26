package com.sgo.saldomu.models

import com.google.gson.annotations.SerializedName

data class OrderSetting(

        @field:SerializedName("channel_group_code")
        val channelGroupCode: String,

        @field:SerializedName("doc_type")
        val docType: String,

        @field:SerializedName("unit")
        val unit: String,

        @field:SerializedName("min_cost")
        val minCost: String,

        @field:SerializedName("min_order_delivery")
        val minOrderDelivery: Int,

        @field:SerializedName("max_order_delivery")
        val maxOrderDelivery: Int
)

