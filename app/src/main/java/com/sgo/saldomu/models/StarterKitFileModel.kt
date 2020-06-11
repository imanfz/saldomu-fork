package com.sgo.saldomu.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class StarterKitFileModel() {
    @SerializedName("starter_id")
    @Expose
    var starter_id: String? = null

    @SerializedName("file_title")
    @Expose
    var file_title: String? = null
}