package com.sgo.saldomu.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_grid.*

class FragTokoEBD : BaseFragment() {

    private var tokoEBDActivity: TokoEBDActivity? = null
    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var adapter: GridMenu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity!!.initializeToolbar(getString(R.string.menu_item_title_ebd))

        menuStrings.clear()
        menuDrawables.clear()
        menuStrings.add(getString(R.string.new_store_registration))
        menuStrings.add(getString(R.string.purchase_order))
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_register_new_store, null)!!)
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_biller, null)!!)

        adapter = GridMenu(context!!, menuStrings, menuDrawables)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (menuStrings[i] == getString(R.string.new_store_registration))
                tokoEBDActivity!!.switchContent(FragRegisterEBD(), getString(R.string.shop_registration), true, "FragRegisterEBD")
            else if (menuStrings[i] == getString(R.string.purchase_order))
                tokoEBDActivity!!.startActivity(Intent(activity, TokoPurchaseOrderActivity::class.java))
        }

    }


}