package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.sgo.saldomu.R
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_result_member_to_verify.*

class ResultMemberToVerifyActivity : BaseActivity() {


    override fun getLayoutResource(): Int {
        return R.layout.activity_result_member_to_verify
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        next_button.setOnClickListener {
            var intent = Intent(this, DetailMemberToVerifyActivity::class.java)
            startActivity(intent)
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