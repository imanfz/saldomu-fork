package com.sgo.saldomu.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.securepreferences.SecurePreferences
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.EasyAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.LevelClass
import com.sgo.saldomu.dialogs.InformationDialog
import com.sgo.saldomu.widgets.BaseActivity
import java.util.*

//class ActivityListTransfer : BaseActivity() {
//    private var dialogI: InformationDialog? = null
//    private var isLevel1: Boolean? = null
//    private var isAgent: Boolean? = null
//    private var levelClass: LevelClass? = null
//
//    internal var list = ArrayList<String>()
//
//    override fun getLayoutResource(): Int {
//        return R.layout.list_transfer
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        InitializeToolbar()
//
//        sp = CustomSecurePref.getInstance().getmSecurePrefs()
//        levelClass = LevelClass(this, sp)
//        levelClass!!.refreshData()
//        isLevel1 = levelClass!!.isLevel1QAC
//        dialogI = InformationDialog.newInstance(11)
//
//        isAgent = sp!!.getBoolean(DefineValue.IS_AGENT, false)
//
//        var _data: Array<String>
//
//        _data = resources.getStringArray(R.array.transfer_list)
//        list.addAll(Arrays.asList(*_data))
//        val adapter = EasyAdapter(this, R.layout.list_view_item_with_arrow, list)
//
//        val listView1 = findViewById<ListView>(R.id.list_trf)
//        listView1.adapter = adapter
//        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//            val i: Intent
//
//            when (list[position]) {
//                getString(R.string.p2p_transfer) -> {
//                    i = Intent(this, PayFriendsActivity::class.java)
//                    startActivity(i)
//                }
//                getString(R.string.bank_transfer) -> {
//                    i = Intent(this, CashoutActivity::class.java)
//                    startActivity(i)
//                }
//            }
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        super.onCreateOptionsMenu(menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_information -> {
//                if (!dialogI!!.isAdded)
//                    dialogI!!.show(this.supportFragmentManager, InformationDialog.TAG)
//                return true
//            }
//            android.R.id.home -> {
//                finish()
//                return true
//            }
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }
//
//    fun InitializeToolbar() {
//        setActionBarIcon(R.drawable.ic_arrow_left)
//        actionBarTitle = getString(R.string.toolbar_title_pay_friends)
//    }
//
//    private fun switchLogout() {
//        setResult(MainPage.RESULT_LOGOUT)
//        finish()
//    }
//}