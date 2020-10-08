package com.sgo.saldomu.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var isAgentDGI: Boolean = false
    private var isAgentCTR: Boolean = false
    private var isFavB2B: Boolean = false
    private var isFavDGI: Boolean = false
    private var isFavCTR: Boolean = false
    private var PAGE_COUNTagent = 3
    private var PAGE_COUNT = 2
    private var PAGE_COUNT_FAV_B2B = 2
    private var PAGE_COUNT_FAV_DGI = 1
    private var PAGE_COUNT_FAV_CTR = 1
    var tabTitlesAgent = arrayOf("Biller", "Setor Dan Tarik", "Transfer")
    var tabTitlesAgentB2B = arrayOf("B2B")
    var tabTitlesAgentDGI = arrayOf("Tagihan")
    var tabTitlesAgentCTR = arrayOf("Cash Collection")


    var page: Int = 0
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
        isAgentDGI = sp.getBoolean(DefineValue.IS_AGENT_DGI, false)
        isAgentCTR = sp.getBoolean(DefineValue.IS_AGENT_CTR, false)

        isFavB2B = intent.getBooleanExtra(DefineValue.IS_FAV_B2B, false)
        isFavDGI = intent.getBooleanExtra(DefineValue.IS_FAV_DGI, false)
        isFavCTR = intent.getBooleanExtra(DefineValue.IS_FAV_CTR, false)

        countPageAgent()

        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress)
        dialog = builder.create()

        adapter = FavoritePagerAdapter(supportFragmentManager, this)
        viewPager.adapter = adapter

        // Give the TabLayout the ViewPager
        tab_layout.setupWithViewPager(viewPager)

    }

    fun countPageAgent() {
        if (isAgentTOP || isAgentBDK) {
            PAGE_COUNTagent = PAGE_COUNTagent + 1
            tabTitlesAgent = tabTitlesAgent + tabTitlesAgentB2B
        }
        if (isAgentDGI) {
            PAGE_COUNTagent = PAGE_COUNTagent + 1
            tabTitlesAgent = tabTitlesAgent + tabTitlesAgentDGI
        }
        if (isAgentCTR) {
            PAGE_COUNTagent = PAGE_COUNTagent + 1
            tabTitlesAgent = tabTitlesAgent + tabTitlesAgentCTR
        }

        Log.d(TAG,"count page agent : "+PAGE_COUNTagent)
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

        private val tabTitles = arrayOf("Biller", "Transfer")
        private val tabTitlesFavB2B = arrayOf(getString(R.string.scadm_topup), getString(R.string.scadm_denom))
        private val tabTitlesFavDGI = arrayOf(getString(R.string.menu_item_title_tagih_agent))
        private val tabTitlesFavCTR = arrayOf(getString(R.string.menu_title_cash_collection))
        private val bilFragment: FavoriteFragment = FavoriteFragment().newInstance("BIL")
        private val bbsFragment: FavoriteFragment = FavoriteFragment().newInstance("BBS")
        private val trfFragment: FavoriteFragment = FavoriteFragment().newInstance("TRF")
        private val b2bFragment: FavoriteFragment = FavoriteFragment().newInstance("B2B")
        private val dgiFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("DGI", "DGI")
        private val ctrFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("CTR", "CTR")
        private val b2bTopUpFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("B2B", "TOP")
        private val b2bDenomFragment: FavoriteFragment = FavoriteFragment().newInstanceB2B("B2B", "BDK")


        override fun getCount(): Int {
            return if (isAgent) {
                if (isAgentBDK || isAgentTOP) {
                    if (isFavB2B)
                        PAGE_COUNT_FAV_B2B
                    else {
                        PAGE_COUNTagent
                    }
                }else
                if (isAgentDGI) {
                    if (isFavDGI)
                        PAGE_COUNT_FAV_DGI
                    else
                        PAGE_COUNTagent
                }
                else
                if (isAgentCTR) {
                    if (isFavCTR)
                        PAGE_COUNT_FAV_CTR
                    else
                        PAGE_COUNTagent
                }
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
                    } else
                        when (position) {
                            0 -> bilFragment
                            1 -> bbsFragment
                            2 -> trfFragment
                            else -> b2bFragment
                        }
                }else
                if (isAgentDGI) {
                    if (isFavDGI) {
                        dgiFragment
                    } else
                        if ((isAgentBDK || isAgentTOP)) {
                            when (position) {
                                0 -> bilFragment
                                1 -> bbsFragment
                                2 -> trfFragment
                                3 -> b2bFragment
                                else -> dgiFragment
                            }
                        } else
                            when (position) {
                                0 -> bilFragment
                                1 -> bbsFragment
                                2 -> trfFragment
                                else -> dgiFragment
                            }
                }else
                if (isAgentCTR) {
                    if (isFavCTR) {
                        ctrFragment
                    } else if (PAGE_COUNTagent == 5) {
                        if (isAgentTOP || isAgentBDK) {
                            when (position) {
                                0 -> bilFragment
                                1 -> bbsFragment
                                2 -> trfFragment
                                3 -> b2bFragment
                                else -> ctrFragment
                            }
                        } else {
                            when (position) {
                                0 -> bilFragment
                                1 -> bbsFragment
                                2 -> trfFragment
                                3 -> dgiFragment
                                else -> ctrFragment
                            }
                        }
                    } else if (PAGE_COUNTagent == 6) {
                        when (position) {
                            0 -> bilFragment
                            1 -> bbsFragment
                            2 -> trfFragment
                            3 -> b2bFragment
                            4 -> dgiFragment
                            else -> ctrFragment
                        }
                    } else
                        when (position) {
                            0 -> bilFragment
                            1 -> bbsFragment
                            2 -> trfFragment
                            else -> ctrFragment
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
            return if (isAgent){
                if (isAgentTOP || isAgentBDK) {
                    if (isFavB2B) {
                        tabTitlesFavB2B[position]
                    }
                    else
                        tabTitlesAgent[position]
                } else if (isAgentDGI) {
                    if (isFavDGI) {
                        tabTitlesFavDGI[position]
//                    } else if (isAgentTOP || isAgentBDK) {
//                        tabTitlesAgent[position] + tabTitlesAgentB2B[position] + tabTitlesFavDGI[position]
                    } else
                        tabTitlesAgent[position]
                } else if (isAgentCTR) {
                    if (isFavCTR) {
                        tabTitlesFavCTR[position]
//                    } else if (PAGE_COUNTagent == 5) {
//                        if (isAgentBDK || isAgentTOP){
//                            tabTitlesAgent[position] + tabTitlesAgentB2B[position] + tabTitlesFavCTR[position]
//                        } else if(isAgentDGI){
//                            tabTitlesAgent[position] + tabTitlesFavDGI[position] + tabTitlesFavCTR[position]
//                        }
                    } else
                    {
                        tabTitlesAgent[position]
                    }
                } else
                    tabTitlesAgent[position]
            }

            else
                tabTitles[position]
        }
    }
}