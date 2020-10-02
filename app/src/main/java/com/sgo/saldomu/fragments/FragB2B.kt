package com.sgo.saldomu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.B2BActivity
import com.sgo.saldomu.activities.DenomSCADMActivity
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.TopUpSCADMActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_b2b.*

class FragB2B : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.frag_b2b, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        if (sp.getBoolean(DefineValue.IS_AGENT_BDK, false) == true) {
            card_view_denom.visibility = View.VISIBLE
        }
        if (sp.getBoolean(DefineValue.IS_AGENT_TOP, false) == true) {
            card_view_topup.visibility = View.VISIBLE
        }

        card_view_topup.setOnClickListener {
            val i = Intent(activity, TopUpSCADMActivity::class.java)
            switchActivity(i)
        }
        card_view_denom.setOnClickListener {
            val i = Intent(activity, DenomSCADMActivity::class.java)
            switchActivity(i)
        }
    }

    private fun switchActivity(mIntent: Intent) {
        if (activity == null) return
        val fca = activity as B2BActivity?
        fca!!.switchActivity(mIntent, MainPage.ACTIVITY_RESULT)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.menuInflater.inflate(R.menu.ab_notification, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.favorite).isVisible = true
        menu.findItem(R.id.notifications).isVisible = false
        menu.findItem(R.id.settings).isVisible = false
        menu.findItem(R.id.search).isVisible = false
        menu.findItem(R.id.cancel).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.favorite) {

        }
        return super.onOptionsItemSelected(item)
    }

}