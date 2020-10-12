package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TagihActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.list_transfer.*

class ListCancelDGI : BaseFragment() {


    private var memberCode: String = ""
    private var commCode: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.list_cancel_dgi, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val args = arguments!!
        memberCode = args.getString(DefineValue.MEMBER_CODE)!!
        commCode = args.getString(DefineValue.COMMUNITY_CODE)!!

        card_view1.setOnClickListener {
            val newFrag: Fragment = CancelInvoiceFragment()
            val bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE, memberCode)
            bundle.putString(DefineValue.COMMUNITY_CODE, commCode)
            newFrag.arguments = bundle
            val ftf = activity as TagihActivity?
            ftf!!.switchContent(newFrag, getString(R.string.cancelation_transaction), true)
        }
        card_view2.setOnClickListener {
            val newFrag: Fragment = FragCancellationVisit()
            val bundle2 = Bundle()
            bundle2.putString(DefineValue.MEMBER_CODE, memberCode)
            bundle2.putString(DefineValue.COMMUNITY_CODE, commCode)
            newFrag.arguments = bundle2
            val ftf = activity as TagihActivity?
            ftf!!.switchContent(newFrag, getString(R.string.cancelation_visit), true)
        }
    }
}