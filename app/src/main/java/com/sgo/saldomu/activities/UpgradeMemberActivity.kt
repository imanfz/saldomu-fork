package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.securepreferences.SecurePreferences
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_upgrade_member.*

class UpgradeMemberActivity : BaseActivity() {


    override fun getLayoutResource(): Int {
        return R.layout.activity_upgrade_member
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_via_agent)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()


        if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "") != null)
            if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "") == "O") {
                upgrade_via_agent_button.isEnabled == false
            } else if (sp.getString(DefineValue.COMM_UPGRADE_MEMBER, "") == "A") {
                upgrade_online_button.isEnabled == false
            }

        upgrade_online_button.setOnClickListener {
            startActivity(Intent(this, MyProfileNewActivity::class.java))
        }

        upgrade_via_agent_button.setOnClickListener {
            startActivity(Intent(this, UpgradeMemberViaAgentActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}