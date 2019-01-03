package com.sgo.saldomu.Beans

import com.sgo.saldomu.entityRealm.List_BBS_Birth_Place

class CustomAdapterModel(var model : List_BBS_Birth_Place) {
    var name : String? = model.birthPlace_city
    var id : String? = model.birthPlace_id


}