package com.sgo.saldomu.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.sgo.saldomu.Beans.Biller_Data_Model
import com.sgo.saldomu.Beans.Biller_Type_Data_Model
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.GridBillerActivity
import com.sgo.saldomu.adapter.GridMenu
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.RealmManager
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.frag_grid.*

class FragGridEmoney : BaseFragment() {

    private val TAG: String? = "FragGridEmoney"

    private var realm: Realm? = null
    private var mBillerType: Biller_Type_Data_Model? = null
    private val menuStrings = ArrayList<String>()
    private val menuDrawables = ArrayList<Drawable>()
    private var mListBillerData: List<Biller_Data_Model>? = null
    private var adapter: GridMenu? = null

    private var billerTypeCode: String? = null
    var gridBillerActivity : GridBillerActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.frag_grid, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridBillerActivity = activity as GridBillerActivity
        gridBillerActivity!!.setToolbarTitle(getString(R.string.newhome_emoney))
        realm = Realm.getInstance(RealmManager.BillerConfiguration)
        billerTypeCode = arguments!!.getString(DefineValue.BILLER_TYPE)
        initializeData()
        setTitleandIcon()
    }

    private fun initializeData() {
        mBillerType = realm!!.where(Biller_Type_Data_Model::class.java).equalTo(WebParams.BILLER_TYPE_CODE, billerTypeCode).findFirst()

        if (mBillerType != null) {
            mListBillerData = mBillerType!!.biller_data_models
        }

        adapter = GridMenu(context!!, menuStrings, menuDrawables)
        grid.adapter = adapter
        grid.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            changeToInputBiller(mListBillerData!![position].comm_id,
                    mListBillerData!![position].comm_name,
                    mListBillerData!![position].item_id,
                    mListBillerData!![position].comm_code,
                    mListBillerData!![position].api_key,
                    mBillerType!!.biller_type)
        }
    }

    private fun setTitleandIcon() {
        menuStrings.clear()
        menuDrawables.clear()
        for (i in mListBillerData!!.indices) {
            menuStrings.add(mListBillerData!![i].comm_name)
            if (mListBillerData!![i].comm_name.contains("LinkAja"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_linkaja, null)!!)

            if (mListBillerData!![i].comm_name.contains("Emoney Mandiri") ||
                    mListBillerData!![i].comm_name.contains("Mandiri E-Money"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_mandiri, null)!!)

            if (mListBillerData!![i].comm_name.contains("OVO"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_ovo, null)!!)

            if (mListBillerData!![i].comm_name.contains("DANA"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_dana, null)!!)

            if (mListBillerData!![i].comm_name.contains("Gopay"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.icon_emoney_gopay, null)!!)

            if (mListBillerData!![i].comm_name.contains("ShopeePay"))
                menuDrawables.add(ResourcesCompat.getDrawable(resources, R.drawable.ic_shopeepay, null)!!)
        }
    }

    private fun changeToInputBiller(commId: String?, commName: String?, itemId: String?, commCode: String?, apiKey: String?, billerType: String?) {
        val bundle = Bundle()
        bundle.putString(DefineValue.COMMUNITY_ID, commId)
        bundle.putString(DefineValue.COMMUNITY_NAME, commName)
        bundle.putString(DefineValue.BILLER_ITEM_ID, itemId)
        bundle.putString(DefineValue.BILLER_COMM_CODE, commCode)
        bundle.putString(DefineValue.BILLER_API_KEY, apiKey)
        bundle.putString(DefineValue.BILLER_TYPE, billerTypeCode)
        bundle.putString(DefineValue.BUY_TYPE, billerType)

        val fragment = BillerInputEmoney()
        fragment.arguments = bundle
        gridBillerActivity!!.switchContent(fragment, commName, TAG)
    }
}