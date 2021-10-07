package com.sgo.saldomu.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.databinding.FragGridBinding
import com.sgo.saldomu.widgets.BaseFragment

class FragRegisterEBD : BaseFragment() {

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
        tokoEBDActivity!!.initializeToolbar(getString(R.string.store_registration))

        menuStrings.clear()
        menuDrawables.clear()
        menuStrings.add(getString(R.string.new_store))
        menuStrings.add(getString(R.string.existing_store))
        menuStrings.add(getString(R.string.store_list))
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_register_new_store, null)!!)
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_register_existing_store, null)!!)
        menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_list_store, null)!!)

        adapter = GridMenu(requireContext(), menuStrings, menuDrawables)
        binding!!.grid.adapter = adapter
        binding!!.grid.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            when {
                menuStrings[i] == getString(R.string.new_store) -> tokoEBDActivity!!.switchContent(FragRegisterNewMember(), getString(R.string.new_store), true, "FragRegisterNewMember")
                menuStrings[i] == getString(R.string.existing_store) -> tokoEBDActivity!!.switchContent(FragJoinCommunityToko(), getString(R.string.join_community), true, "FragJoinCommunityToko")
                menuStrings[i] == getString(R.string.store_list) -> tokoEBDActivity!!.switchContent(FragListToko(), getString(R.string.store_list), true, "FragListToko")
            }
        }

    }


}