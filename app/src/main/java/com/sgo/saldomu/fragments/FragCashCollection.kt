package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.RealmManager
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.entityRealm.BBSCommModel
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.frag_cash_collection.*

class FragCashCollection : BaseFragment() {

    private var comm: BBSCommModel? = null
    private var realmBBS:Realm? = null
    private lateinit var viewLayout: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLayout = inflater.inflate(R.layout.frag_cash_collection, container, false)
        return viewLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration)

        comm = realmBBS?.where(BBSCommModel::class.java)?.equalTo(WebParams.SCHEME_CODE, "CTR")?.findFirst()

        detail_cash_collection.visibility = View.GONE
        btn_search.setOnClickListener { searchMember() }
    }

    private fun searchMember() {
        detail_cash_collection.visibility = View.VISIBLE
    }
}