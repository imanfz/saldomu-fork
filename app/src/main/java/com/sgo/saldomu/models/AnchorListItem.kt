package com.sgo.saldomu.models

import com.google.gson.annotations.SerializedName

data class AnchorListItem(

	@field:SerializedName("anchor_name")
	val anchorName: String,

	@field:SerializedName("anchor_code")
	val anchorCode: String
)
