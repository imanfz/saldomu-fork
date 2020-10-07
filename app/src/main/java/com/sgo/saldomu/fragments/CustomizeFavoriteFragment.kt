package com.sgo.saldomu.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sgo.saldomu.R
import com.sgo.saldomu.models.EditFavoriteModel
import io.realm.Realm
import kotlinx.android.synthetic.main.edit_favorites_layout.*

class CustomizeFavoriteFragment(onClick: OnClick) : BottomSheetDialogFragment() {
    var v: View? = null
    var realm: Realm? = null
    var listener: OnClick? = onClick
    private var isEdit = false

//    var editAdapt: AdapterEditFavorite? = null
//    var mainAdapt: AdapterCurrFavorite? = null

    var itemList: List<EditFavoriteModel>? = null
    var editList: List<EditFavoriteModel>? = null
    var subtItemList: List<EditFavoriteModel>? = null

    interface OnClick {
        fun onEditTap()
        fun onFeatureTap(isEdit: Boolean, id: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        isEdit = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.edit_favorites_layout, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_fav_root.layoutParams = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDeviceHeight())
    }

    private fun getDeviceWidth(): Float {
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels.toFloat()
    }

    private fun getDeviceHeight(): Int {
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels / 5 * 4
    }
}