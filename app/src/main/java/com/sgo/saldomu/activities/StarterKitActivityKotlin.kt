package com.sgo.saldomu.activities

import android.os.Bundle
import android.view.Menu
import com.securepreferences.SecurePreferences
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.LevelClass
import com.sgo.saldomu.widgets.BaseActivity

class StarterKitActivityKotlin : BaseActivity(){
    private lateinit var levelClass: LevelClass
    private var isLevel1: Boolean? = null
    private var isAgent:Boolean? = false

    override fun getLayoutResource(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        R.layout.starterkit_activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InitializeToolbar()
        sp = CustomSecurePref . getInstance ().getmSecurePrefs()

        levelClass = LevelClass(this, sp)
        levelClass.refreshData()
        isLevel1 = levelClass.isLevel1QAC()
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)

        getListFile()
    }

    fun getListFile() {

    }

    fun InitializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_title_setting)
    }

    private fun switchLogout() {
        setResult(MainPage.RESULT_LOGOUT)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}