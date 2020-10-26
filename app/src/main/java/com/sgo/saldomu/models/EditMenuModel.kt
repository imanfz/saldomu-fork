package com.sgo.saldomu.models

import io.realm.RealmObject

open class EditMenuModel : RealmObject() {

    private var title: String? = null
    private var isShow: Boolean? = null

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getIsShow(): Boolean? {
        return isShow
    }

    fun setIsShow(isShow: Boolean?) {
        this.isShow = isShow
    }
}