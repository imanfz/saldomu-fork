package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.MenuItem
import com.sgo.saldomu.R
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_proses_upgrade_member.*


class ProsesUpgradeMemberActivity : BaseActivity() {
    override fun getLayoutResource(): Int {
        return R.layout.activity_proses_upgrade_member
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_via_agent)

        back_button.setOnClickListener {
            finish()
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