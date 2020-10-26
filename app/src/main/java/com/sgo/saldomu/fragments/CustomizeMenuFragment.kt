package com.sgo.saldomu.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.AdapterCurrentMenu
import com.sgo.saldomu.adapter.AdapterSubMenu
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.EditMenuModel
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.edit_menu_layout.*

class CustomizeMenuFragment(onClose: OnClose) : BottomSheetDialogFragment() {
    var v: View? = null
    var realm: Realm? = null
    var listener: OnClose? = onClose

    //    var editAdapter: AdapterEditFavorite? = null
    var currentMenuAdapter: AdapterCurrentMenu? = null
    var subMenuAdapter: AdapterSubMenu? = null

    var currentMenuList: RealmResults<EditMenuModel>? = null
    var subMenuList: RealmResults<EditMenuModel>? = null

    interface OnClose {
        fun onCloseFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.edit_menu_layout, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        edit_fav_root.layoutParams = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDeviceHeight())
    }

    private fun initAdapter() {
        currentMenuList = realm!!.where(EditMenuModel::class.java).equalTo(DefineValue.IS_SHOW, true).findAll()
        subMenuList = realm!!.where(EditMenuModel::class.java).equalTo(DefineValue.IS_SHOW, false).findAll()

        currentMenuAdapter = AdapterCurrentMenu(activity, currentMenuList, object : AdapterCurrentMenu.OnClick {
            override fun onTap(pos: Int) {
                showMenu(pos, false)
            }
        })

        subMenuAdapter = AdapterSubMenu(activity, subMenuList, object : AdapterSubMenu.OnClick {
            override fun onTap(pos: Int) {
                showMenu(pos, true)
                currentMenuAdapter!!.notifyDataSetChanged()
                subMenuAdapter!!.notifyDataSetChanged()
            }
        })

        current_menu.adapter = currentMenuAdapter
        current_menu.layoutManager = GridLayoutManager(activity, 4, GridLayoutManager.VERTICAL, false)
        current_menu.itemAnimator = DefaultItemAnimator()
        current_menu.isNestedScrollingEnabled = false

        sub_menu.adapter = subMenuAdapter
        sub_menu.layoutManager = GridLayoutManager(activity, 4, GridLayoutManager.VERTICAL, false)
        sub_menu.itemAnimator = DefaultItemAnimator()
        sub_menu.isNestedScrollingEnabled = false
    }

    private fun showMenu(pos: Int, show: Boolean) {
        realm!!.beginTransaction()

        if (show)
            subMenuList!![pos]!!.setIsShow(show)
        else
            currentMenuList!![pos]!!.setIsShow(show)

        currentMenuAdapter!!.notifyDataSetChanged()
        subMenuAdapter!!.notifyDataSetChanged()

        if (realm!!.isInTransaction) realm!!.commitTransaction()
    }

    private fun getDeviceHeight(): Int {
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels / 5 * 4
    }

    override fun onCancel(dialog: DialogInterface) {
        listener!!.onCloseFragment()
        super.onCancel(dialog)
    }
}