package com.sgo.saldomu.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.fragments.FavoriteFragment
import com.sgo.saldomu.models.retrofit.FavoriteModel
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_favorite.*

private const val TAG = "FavoriteActivity"

class FavoriteActivity : BaseActivity() {
    private var isAgent: Boolean = false
    private var isAgentBDK: Boolean = false
    private var isAgentTOP: Boolean = false
    private var isFavB2B: Boolean = false
    private var isFavDGI: Boolean = false
    fun startBillerActivity(model: FavoriteModel) {
        val intent = Intent(this, BillerActivity::class.java)
        intent.putExtra(DefineValue.BILLER_TYPE, model.product_type)
        if (model.product_type == "DATA")
            intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.data_title))
        if (model.product_type == "PLS")
            intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.prepaid_title))
        if (model.product_type == "HP")
            intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.postpaid_title))
        if (model.product_type == "TKN" || model.product_type == "PLN")
            intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.pln_title))
        if (model.product_type == "VCHR")
            intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.newhome_voucher))
        if (model.product_type == "BPJS")
            intent.putExtra(DefineValue.BILLER_NAME, getString(R.string.bpjs))
        if (model.product_type == "EMON")
            intent.putExtra(DefineValue.BILLER_NAME, model.comm_name)
        intent.putExtra(DefineValue.COMMUNITY_ID, model.comm_id)
        intent.putExtra(DefineValue.ITEM_ID, model.item_id)
        if (model.comm_name.contains("OVO")) {
            intent.putExtra(DefineValue.COMMUNITY_NAME, model.comm_name)
        } else {
            intent.putExtra(DefineValue.COMMUNITY_NAME, model.item_name)
        }


        intent.putExtra(DefineValue.FAVORITE_CUSTOMER_ID, model.customer_id)
        intent.putExtra(DefineValue.BILLER_COMM_CODE, model.comm_code)

        startActivity(intent)
    }

    fun startBBSActivity(model: FavoriteModel) {
        val intent = Intent(this, BBSActivity::class.java)

        intent.putExtra(DefineValue.INDEX, BBSActivity.TRANSACTION)
        // CTA = cash in
        // ATC == cash out
        if (model.product_type.equals("CTA")) {
            intent.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHIN)
        } else {
            intent.putExtra(DefineValue.TYPE, DefineValue.BBS_CASHOUT)
        }
        intent.putExtra(DefineValue.PRODUCT_CODE, model.benef_bank_code)
        intent.putExtra(DefineValue.FAVORITE_CUSTOMER_ID, model.customer_id)

        startActivity(intent)
    }

    fun startTransferActivity(model: FavoriteModel) {
        val intent = Intent(this, PayFriendsActivity::class.java)
        intent.putExtra(DefineValue.FAVORITE_CUSTOMER_ID, model.customer_id)
        startActivity(intent)
    }

    fun startB2BActivity(model: FavoriteModel) {
        var intent = intent

        intent = when (model.product_type) {
            "TOP" -> Intent(this, TopUpSCADMActivity::class.java)
            "BDK" -> Intent(this, DenomSCADMActivity::class.java)
            else -> Intent(this, B2BActivity::class.java)
        }

        intent.putExtra(DefineValue.FAVORITE_CUSTOMER_ID, model.customer_id)
        intent.putExtra(DefineValue.COMMUNITY_NAME, model.comm_name)
        intent.putExtra(DefineValue.COMM_ID_SCADM, model.comm_id)
        intent.putExtra(DefineValue.COMMUNITY_CODE, model.comm_code)
        intent.putExtra(DefineValue.API_KEY, model.api_key)
        intent.putExtra(DefineValue.MEMBER_CODE, model.member_code)
        intent.putExtra(DefineValue.MEMBER_ID_SCADM, model.member_id)
        startActivity(intent)
    }

    internal lateinit var dialog: AlertDialog
    internal lateinit var adapter: FavoritePagerAdapter

    override fun getLayoutResource(): Int {
        return R.layout.activity_favorite
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = getString(R.string.menu_item_favorite)

        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false)
        isAgentBDK = sp.getBoolean(DefineValue.IS_AGENT_BDK, false)
        isAgentTOP = sp.getBoolean(DefineValue.IS_AGENT_TOP, false)

        isFavB2B = intent.getBooleanExtra(DefineValue.IS_FAV_B2B, false)
        isFavDGI = intent.getBooleanExtra(DefineValue.IS_FAV_DGI, false)

        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress)
        dialog = builder.create()

        adapter = FavoritePagerAdapter(supportFragmentManager, this)
        viewPager.adapter = adapter

        // Give the TabLayout the ViewPager
        tab_layout.setupWithViewPager(viewPager)
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

    inner class FavoritePagerAdapter(fm: FragmentManager, private val context: Context) : FragmentStatePagerAdapter(fm) {
        private val PAGE_COUNTagent = 3
        private val PAGE_COUNTagentB2B = 4
        private val PAGE_COUNT = 2
        private val PAGE_COUNT_FAV_B2B = 2
        private val PAGE_COUNT_FAV_DGI = 1
        private val tabTitlesAgent = arrayOf("Biller", "Setor Dan Tarik", "Transfer")
        private val tabTitlesAgentB2B = arrayOf("Biller", "Setor Dan Tarik", "Transfer", "B2B")
        private val tabTitles = arrayOf("Biller", "Transfer")
        private val tabTitlesFavB2B = arrayOf(getString(R.string.scadm_topup), getString(R.string.scadm_denom))
        private val tabTitlesFavDGI = arrayOf(getString(R.string.menu_item_title_tagih_agent))
        private val bilFragment: FavoriteFragment = FavoriteFragment().newInstance("BIL")
        private val bbsFragment: FavoriteFragment = FavoriteFragment().newInstance("BBS")
        private val trfFragment: FavoriteFragment = FavoriteFragment().newInstance("TRF")
        private val b2bFragment: FavoriteFragment = FavoriteFragment().newInstance("B2B")
        private val tagihFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("DGI", "DGI")
        private val b2bTopUpFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("B2B", "TOP")
        private val b2bDenomFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("B2B", "BDK")


        override fun getCount(): Int {
            return if (isAgent) {
                if (isAgentBDK || isAgentTOP)
                    if (isFavB2B)
                        PAGE_COUNT_FAV_B2B
                    else
                        PAGE_COUNTagentB2B
                else if (isFavDGI)
                    PAGE_COUNT_FAV_DGI
                else
                    PAGE_COUNTagent
            } else
                PAGE_COUNT
        }

        override fun getItem(position: Int): Fragment {
            return if (isAgent) {
                if (isAgentBDK || isAgentTOP) {
                    if (isFavB2B) {
                        when (position) {
                            0 -> b2bTopUpFragment
                            else -> b2bDenomFragment
                        }
                    } else if (isFavDGI) {
                        tagihFragment
                    } else
                        when (position) {
                            0 -> bilFragment
                            1 -> bbsFragment
                            2 -> trfFragment
                            else -> b2bFragment
                        }
                } else {
                    when (position) {
                        0 -> bilFragment
                        1 -> bbsFragment
                        else -> trfFragment
                    }
                }
            } else {
                when (position) {
                    0 -> bilFragment
                    else -> trfFragment
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            // Generate title based on item position
            return if (isAgent)
                if (isAgentTOP || isAgentBDK || isFavDGI) {
                    if (isFavB2B) {
                        tabTitlesFavB2B[position]
                    } else if (isFavDGI) {
                        tabTitlesFavDGI[position]
                    }
                        tabTitlesAgentB2B[position]
                } else
                    tabTitlesAgent[position]
            else
                tabTitles[position]
        }
    }
}