package com.sgo.saldomu.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import com.sgo.saldomu.Beans.Biller_Type_Data_Model
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.BillerActivity
import com.sgo.saldomu.adapter.GridBiller
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.RealmManager
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.frag_grid_biller.*

class FragGridBiller : BaseFragment() {

    private var realm: Realm? = null
    private var mBillerTypeData: RealmResults<Biller_Type_Data_Model>? = null
    private var mBillerType: Biller_Type_Data_Model? = null
    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var billerTypeName = ArrayList<String>()
    private var adapter: GridBiller? = null

    var billerType: String? = null

    companion object {
        fun newInstance(biller_type: String?): FragGridBiller? {
            val mFrag = FragGridBiller()
            val bundle = Bundle()
            bundle.putString(DefineValue.BILLER_TYPE, biller_type)
            mFrag.arguments = bundle
            return mFrag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_grid_biller, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getInstance(RealmManager.BillerConfiguration)
        billerType = arguments!!.getString(DefineValue.BILLER_TYPE)
        initializeData()
        setTitleandIcon()
    }

    private fun initializeData() {
        mBillerTypeData = realm!!.where(Biller_Type_Data_Model::class.java).equalTo(WebParams.BILLER_TYPE, billerType)
                .findAll()
        if (mBillerTypeData!!.size > 0) {
            for (i in mBillerTypeData!!.indices) {
                billerTypeName.add(mBillerTypeData!![i]!!.biller_type_name)
            }
        }

        adapter = GridBiller(context!!, menuStrings, menuDrawables)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val menuItemName = hardCodeMenuItemName((view!!.findViewById<View>(R.id.grid_text) as TextView).text.toString())
            mBillerType = realm!!.where(Biller_Type_Data_Model::class.java).equalTo(WebParams.BILLER_TYPE_NAME, menuItemName).findFirst()
            val i = Intent(activity, BillerActivity::class.java)
            i.putExtra(DefineValue.BILLER_TYPE, mBillerType!!.biller_type_code)
            i.putExtra(DefineValue.BILLER_NAME, mBillerType!!.biller_type_name)
            startActivity(i)
        }
    }
    
    private fun hardCodeMenuItemName(string: String): String {
        when (string) {
            getString(R.string.credit_card) -> return "Kartu Kredit"
            getString(R.string.installment) -> return "Cicilan"
            getString(R.string.flight_ticket) -> return "Tiket Pesawat Terbang"
            getString(R.string.train_ticket) -> return "Tiket Kereta Api"
            getString(R.string.insurance) -> return "Asuransi"
            getString(R.string.cable_tv) -> return "TV Cable"
        }
        return string
    }

    private fun setTitleandIcon() {
        menuStrings.add(resources.getString(R.string.credit_card))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_kartu_kredit))

        menuStrings.add(resources.getString(R.string.installment))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_tagihan_kartu_kredit))

        menuStrings.add(resources.getString(R.string.telkom))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_telkom))

        menuStrings.add(resources.getString(R.string.flight_ticket))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_pesawat))

        menuStrings.add(resources.getString(R.string.train_ticket))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_kereta))

        menuStrings.add(resources.getString(R.string.insurance))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_asuransi))

        menuStrings.add(resources.getString(R.string.internet))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_internet))

        menuStrings.add(resources.getString(R.string.cable_tv))
        menuDrawables.add(resources.getDrawable(R.drawable.icon_biller_tv))
    }
}