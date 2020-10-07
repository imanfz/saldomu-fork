package com.sgo.saldomu.models

import io.realm.RealmObject

class EditFavoriteModel : RealmObject() {

    private var title: Int? = null
    private var img: Int? = null


    fun EditFavoriteModel(title: Int?, img: Int) {
        this.title = title
        this.img = img
    }

    fun getTitle(): Int? {
        return title
    }

    fun setTitle(title: Int?) {
        this.title = title
    }

    fun getImg(): Int? {
        return img
    }

    fun setImg(img: Int?) {
        this.img = img
    }
}