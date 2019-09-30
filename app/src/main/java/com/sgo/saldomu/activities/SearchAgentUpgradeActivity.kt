package com.sgo.saldomu.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseActivity

class SearchAgentUpgradeActivity : BaseActivity() {

    override fun getLayoutResource(): Int {
        return R.layout.activity_search_agent_upgrade
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_via_agent)
        setActionBarIcon(R.drawable.ic_arrow_left)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
    }
}
