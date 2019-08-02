package com.sgo.saldomu.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.sgo.saldomu.R
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_search_member_to_verifiy.*

class SearchMemberToVerifyActivity : BaseActivity(){
    override fun getLayoutResource(): Int {
        return R.layout.activity_search_member_to_verifiy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)

        submit_button.setOnClickListener {
            startActivity(Intent(this, ResultMemberToVerifyActivity::class.java))
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