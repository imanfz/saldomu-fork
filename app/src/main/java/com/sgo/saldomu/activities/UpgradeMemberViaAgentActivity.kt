package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_upgrade_member_via_agent.*
import java.util.*


class UpgradeMemberViaAgentActivity: BaseActivity() {

    override fun getLayoutResource(): Int {
        return R.layout.activity_upgrade_member_via_agent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize () {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_member)


        submit_button.setOnClickListener {
            startActivity(Intent(this, ProsesUpgradeMemberActivity::class.java))
            finish()
        }
        birthday_text_view.setOnClickListener {
            birthdayOnClick()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun birthdayOnClick() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
                DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    birthday_text_view.text = "$dayOfMonth / $monthOfYear / $year"

                }, year, month, day)
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
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