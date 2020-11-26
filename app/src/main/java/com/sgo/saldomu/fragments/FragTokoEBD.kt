package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_toko_ebd.*

class FragTokoEBD : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_toko_ebd, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.menu_item_title_ebd))
        card_view_store_registration.setOnClickListener {
            tokoEBDActivity.switchContent(FragListCommunityToko(),getString(R.string.menu_item_title_ebd),true,"FragListCommunityToko")
        }

        card_view_create_po.setOnClickListener {

        }

        card_view_list_po.setOnClickListener {

        }
    }


}