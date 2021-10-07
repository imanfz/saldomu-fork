package com.sgo.saldomu.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.res.ResourcesCompat
import com.sgo.saldomu.Beans.Biller_Type_Data_Model
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BillerActivity
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_grid.*

class FragGridBiller : BaseFragment() {

    private val BILLER_TYPE_CODE_CC = "CC"
    private val BILLER_TYPE_CODE_CCL = "CCL"
    private val BILLER_TYPE_CODE_TLP = "TLP"
    private val BILLER_TYPE_CODE_PST = "PST"
    private val BILLER_TYPE_CODE_KA = "KA"
    private val BILLER_TYPE_CODE_ASU = "ASU"
    private val BILLER_TYPE_CODE_INT = "INT"
    private val BILLER_TYPE_CODE_TV = "TV"
    private val BILLER_TYPE_CODE_PBB = "PBB"
    private val BILLER_TYPE_CODE_SMST = "SMST"
    private val BILLER_TYPE_CODE_B2B = "RTU"

    private val billerTypeList = arrayOf(
            BILLER_TYPE_CODE_CC,
            BILLER_TYPE_CODE_CCL,
            BILLER_TYPE_CODE_TLP,
            BILLER_TYPE_CODE_PST,
            BILLER_TYPE_CODE_KA,
            BILLER_TYPE_CODE_ASU,
            BILLER_TYPE_CODE_INT,
            BILLER_TYPE_CODE_TV,
            BILLER_TYPE_CODE_PBB,
            BILLER_TYPE_CODE_SMST,
            BILLER_TYPE_CODE_B2B
    )
    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private val billerTypeDataList = ArrayList<Biller_Type_Data_Model>()
    private var adapter: GridMenu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GridMenu(context!!, menuStrings, menuDrawables)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val i = Intent(activity, BillerActivity::class.java)
            i.putExtra(DefineValue.BILLER_TYPE, billerTypeDataList[position].biller_type_code)
            i.putExtra(DefineValue.BILLER_NAME, billerTypeDataList[position].biller_type_name)
            startActivity(i)
        }
        initializeData()
        setTitleAndIcon()
    }

    private fun initializeData() {
        for (i in billerTypeList.indices) {
            val billerTypeData = Biller_Type_Data_Model()
            when (billerTypeList[i]) {
                BILLER_TYPE_CODE_CC -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_CC
                    billerTypeData.biller_type_name = getString(R.string.credit_card)
                }
                BILLER_TYPE_CODE_CCL -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_CCL
                    billerTypeData.biller_type_name = getString(R.string.installment)
                }
                BILLER_TYPE_CODE_TLP -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_TLP
                    billerTypeData.biller_type_name = getString(R.string.telkom)
                }
                BILLER_TYPE_CODE_PST -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_PST
                    billerTypeData.biller_type_name = getString(R.string.flight_ticket)
                }
                BILLER_TYPE_CODE_KA -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_KA
                    billerTypeData.biller_type_name = getString(R.string.train_ticket)
                }
                BILLER_TYPE_CODE_ASU -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_ASU
                    billerTypeData.biller_type_name = getString(R.string.insurance)
                }
                BILLER_TYPE_CODE_INT -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_INT
                    billerTypeData.biller_type_name = getString(R.string.internet)
                }
                BILLER_TYPE_CODE_TV -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_TV
                    billerTypeData.biller_type_name = getString(R.string.cable_tv)
                }
                BILLER_TYPE_CODE_PBB -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_PBB
                    billerTypeData.biller_type_name = getString(R.string.pbb)
                }
                BILLER_TYPE_CODE_SMST -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_SMST
                    billerTypeData.biller_type_name = getString(R.string.e_samsat)
                }
                BILLER_TYPE_CODE_B2B -> {
                    billerTypeData.biller_type_code = BILLER_TYPE_CODE_B2B
                    billerTypeData.biller_type_name = getString(R.string.b2b_telco)
                }
            }
            billerTypeDataList.add(billerTypeData)
        }
    }

    private fun setTitleAndIcon() {
        menuStrings.clear()
        menuDrawables.clear()
        for (i in billerTypeDataList.indices) {
            menuStrings.add(billerTypeDataList[i].biller_type_name)
        }
        for (i in menuStrings.indices) {
            menuDrawables.add(ResourcesCompat.getDrawable(resources, getImageMenu(menuStrings[i]), null)!!)
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun getImageMenu(titleMenu: String): Int {
        return when (titleMenu) {
            getString(R.string.credit_card) -> R.drawable.icon_biller_kartu_kredit
            getString(R.string.installment) -> R.drawable.icon_biller_tagihan_kartu_kredit
            getString(R.string.telkom) -> R.drawable.icon_biller_telkom
            getString(R.string.flight_ticket) -> R.drawable.icon_biller_pesawat
            getString(R.string.train_ticket) -> R.drawable.icon_biller_kereta
            getString(R.string.insurance) -> R.drawable.icon_biller_asuransi
            getString(R.string.internet) -> R.drawable.icon_biller_internet
            getString(R.string.cable_tv) -> R.drawable.icon_biller_tv
            getString(R.string.pbb) -> R.drawable.icon_biller_pbb
            getString(R.string.e_samsat) -> R.drawable.icon_biller_samsat
            getString(R.string.b2b_telco) -> R.drawable.ic_menu_b2b
            else -> R.drawable.ic_mandiri
        }
    }
}