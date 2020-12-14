package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.AdapterEBDCatalogList
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.EBDOrderModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import java.util.*

class FragListPurchaseOrder : BaseFragment() {

    var memberCode: String? = null
    var commCode: String? = null

    var itemList = ArrayList<EBDCatalogModel>()
    var orderList = ArrayList<EBDOrderModel>()
    var itemListAdapter: AdapterEBDCatalogList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.list_po))
        btn_create_po.visibility = View.VISIBLE

    }
}