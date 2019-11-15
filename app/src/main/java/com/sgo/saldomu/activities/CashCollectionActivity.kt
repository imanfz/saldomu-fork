package com.sgo.saldomu.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import android.view.View
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.ToggleKeyboard
import com.sgo.saldomu.fragments.FragCashCollection
import com.sgo.saldomu.fragments.FragTagihInput
import com.sgo.saldomu.widgets.BaseActivity
import timber.log.Timber

class CashCollectionActivity : BaseActivity() {

    internal lateinit var fragmentManager: FragmentManager
    internal lateinit var mContent: Fragment
    internal lateinit var newFragment: Fragment
    lateinit var bankCode: String
    lateinit var amount: String

    override fun getLayoutResource(): Int {
        return R.layout.activity_cash_collection
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        initializeToolbar()

        val intent = intent
        val is_search = intent.getBooleanExtra(DefineValue.IS_SEARCH_CTR, false)
        if (is_search) {
            bankCode = intent.getStringExtra(DefineValue.BANK_CODE)
            amount = intent.getStringExtra(DefineValue.AMOUNT)
        }

        if (findViewById<View>(R.id.layout_cashcollection) != null) {
            if (savedInstanceState != null) {
                return
            }

            newFragment = FragCashCollection()
            val bundle = Bundle()
            bundle.putBoolean(DefineValue.IS_SEARCH_CTR, is_search)
            if (is_search)
            {
                bundle.putString(DefineValue.BANK_CODE, bankCode)
                bundle.putString(DefineValue.AMOUNT, amount)
            }
            newFragment.arguments = bundle
        }

        mContent = newFragment

        fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.layout_cashcollection, newFragment)
        fragmentTransaction.commitAllowingStateLoss()
        setResult(MainPage.RESULT_NORMAL)
    }

    fun initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_title_ctr)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else
                    finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun switchContent(mFragment: Fragment, fragName: String, isBackstack: Boolean) {
        ToggleKeyboard.hide_keyboard(this)
        if (isBackstack) {
            Timber.d("backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.layout_cashcollection, mFragment, fragName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        } else {
            Timber.d("bukan backstack")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.layout_cashcollection, mFragment, fragName)
                    .commitAllowingStateLoss()

        }
        actionBarTitle = fragName
    }

    fun setResultActivity(result: Int) {
        setResult(MainPage.RESULT_BALANCE)
    }

}