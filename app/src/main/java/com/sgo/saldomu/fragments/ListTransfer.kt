package com.sgo.saldomu.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.*
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CashoutActivity
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.PayFriendsActivity
import com.sgo.saldomu.activities.RegisterSMSBankingActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.LevelClass
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.dialogs.InformationDialog
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.list_transfer.*
import java.util.ArrayList

class ListTransfer : BaseFragment() {
    private var dialogI: InformationDialog? = null
    private var isLevel1: Boolean? = null
    private var levelClass: LevelClass? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.list_transfer, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        levelClass = LevelClass(activity, sp)
        levelClass!!.refreshData()
        isLevel1 = levelClass!!.isLevel1QAC
        dialogI = InformationDialog.newInstance(11)
        dialogI!!.setTargetFragment(this, 0)

        card_view1.setOnClickListener {
            val i = Intent(activity, PayFriendsActivity::class.java)
            switchActivity(i)
        }
        card_view2.setOnClickListener {
            if (sp.getBoolean(DefineValue.IS_AGENT, false) == false) {
                val i = Intent(activity, CashoutActivity::class.java)
                switchActivity(i)
            } else
                DialogWaitingUpgradeAgent()
        }
    }

    private fun DialogWaitingUpgradeAgent() {
        val dialognya = DefinedDialog.MessageDialog(activity, this.getString(R.string.alertbox_title_information),
                this.getString(R.string.cashout_dialog_message)
        ) { v, isLongClick ->  }

        dialognya.setCanceledOnTouchOutside(false)
        dialognya.setCancelable(false)
        dialognya.show()

    }

    private fun switchActivity(mIntent: Intent) {
        if (activity == null)
            return

        val fca = activity as MainPage?
        fca!!.switchActivity(mIntent, MainPage.ACTIVITY_RESULT)
    }

}





