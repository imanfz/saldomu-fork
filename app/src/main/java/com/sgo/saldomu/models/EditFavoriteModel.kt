package com.sgo.saldomu.models

import android.graphics.drawable.Drawable
import io.realm.RealmObject

open class EditFavoriteModel : RealmObject() {

    private var title: String? = null
    private var img: Drawable? = null


    fun EditFavoriteModel(title: String, img: Drawable) {
        this.title = title
        this.img = img
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getImg(): Drawable? {
        return img
    }

    fun setImg(img: Drawable?) {
        this.img = img
    }
}