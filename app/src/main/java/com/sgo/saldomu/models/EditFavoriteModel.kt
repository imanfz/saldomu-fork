package com.sgo.saldomu.models

import android.graphics.drawable.Drawable
import io.realm.RealmObject

open class EditFavoriteModel : RealmObject() {

    private var title: String? = null
    private var img: Int? = null


    fun EditFavoriteModel(title: String, img: Int) {
        this.title = title
        this.img = img
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getImg(): Int? {
        return img
    }

    fun setImg(img: Int?) {
        this.img = img
    }
}