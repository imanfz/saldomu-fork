package com.sgo.saldomu.models

import com.google.gson.annotations.SerializedName

data class MemberListItem(

	@field:SerializedName("created")
	val created: String,

	@field:SerializedName("cust_name")
	val custName: String,

	@field:SerializedName("shop_name")
	val shopName: String,

	@field:SerializedName("cust_id")
	val custId: String,

	@field:SerializedName("reg_id")
	val regId: String,

	@field:SerializedName("status")
	val status: String
)
