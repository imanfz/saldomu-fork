package com.sgo.saldomu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BBSActivity
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.MandiriLPActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.list_mandiri_lp.*

class FragMandiriLP : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.list_mandiri_lp, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        if (sp.getString(DefineValue.IS_AGENT_TRX_CTA_MANDIRI_LP, "").equals("Y"))
            card_view_cash_in.setOnClickListener {
                val i = Intent(activity, BBSActivity::class.java)
                i.putExtra(DefineValue.INDEX, BBSActivity.TRANSACTION)
                i.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHIN)
                i.putExtra(DefineValue.TX_MANDIRI_LP, true)
                switchActivity(i)
            }
        else
            card_view_cash_in.visibility = View.GONE

        if (sp.getString(DefineValue.IS_AGENT_TRX_ATC_MANDIRI_LP, "").equals("Y"))
            card_view_cash_out.setOnClickListener {
                val i = Intent(activity, BBSActivity::class.java)
                i.putExtra(DefineValue.INDEX, BBSActivity.TRANSACTION)
                i.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHOUT)
                i.putExtra(DefineValue.TX_MANDIRI_LP, true)
                switchActivity(i)
            }
        else
            card_view_cash_out.visibility = View.GONE

    }

    private fun switchActivity(mIntent: Intent) {
        if (activity == null) return
        val fca = activity as MandiriLPActivity?
        fca!!.switchActivity(mIntent, MainPage.ACTIVITY_RESULT)
    }

}