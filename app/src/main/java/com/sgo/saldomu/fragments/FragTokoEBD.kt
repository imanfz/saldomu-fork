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
import com.sgo.saldomu.databinding.FragGridBinding
import com.sgo.saldomu.widgets.BaseFragment

class FragTokoEBD : BaseFragment() {

    private var tokoEBDActivity: TokoEBDActivity? = null
    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var adapter: GridMenu? = null

    private var binding: FragGridBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragGridBinding.inflate(inflater, container, false)
        v = binding!!.root
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity!!.initializeToolbar(getString(R.string.menu_item_title_b2b_eratel))

        menuStrings.clear()
        menuDrawables.clear()
        menuStrings.add(getString(R.string.store_registration))
        menuStrings.add(getString(R.string.purchase_order))
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_register_store, null)!!)
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_biller, null)!!)

        adapter = GridMenu(context!!, menuStrings, menuDrawables)
        binding!!.grid.adapter = adapter
        binding!!.grid.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (menuStrings[i] == getString(R.string.store_registration))
                tokoEBDActivity!!.switchContent(FragRegisterEBD(), getString(R.string.store_registration), true, "FragRegisterEBD")
            else if (menuStrings[i] == getString(R.string.purchase_order))
                tokoEBDActivity!!.startActivity(Intent(activity, TokoPurchaseOrderActivity::class.java))
        }

    }


}