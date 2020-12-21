package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.DenomSCADMActivity
import com.sgo.saldomu.adapter.PromoCodeAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.models.PromoCodeModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_denom_input_promo_code.*
import org.json.JSONArray
import timber.log.Timber

class FragmentDenomInputPromoCode : BaseFragment() {

    var promoCodeList: ArrayList<PromoCodeModel>? = ArrayList()
    var promoCodeAdapter: PromoCodeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_denom_input_promo_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        promoCodeList!!.add(PromoCodeModel("", "",""))
        promoCodeAdapter = PromoCodeAdapter(activity, promoCodeList, object : PromoCodeAdapter.Listener {
            override fun onChangePromoCode(position: Int, promoCode: String) {
                promoCodeList!![position].code = promoCode
            }

            override fun onChangePromoQty(position: Int, promoQty: String) {
                promoCodeList!![position].qty = promoQty
            }

            override fun onDelete(position: Int) {
                promoCodeList!!.removeAt(position)
                promoCodeAdapter!!.notifyDataSetChanged()
            }

        })
        frag_denom_input_promo_list_field.adapter = promoCodeAdapter
        frag_denom_input_promo_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        btn_add_promo.setOnClickListener {
            promoCodeList!!.add(PromoCodeModel("", "",""))
            promoCodeAdapter!!.notifyDataSetChanged()
        }

        frag_denom_input_promo_code_submit_btn.setOnClickListener { submitToConfirm() }
    }

    private fun submitToConfirm() {
        if (checkArrayPromo()) {
            val listString = Gson().toJson(promoCodeList, object : TypeToken<ArrayList<PromoCodeModel?>?>() {}.type)
            val jsonArray = JSONArray(listString)
            Timber.e(jsonArray.toString())

            val frag: Fragment = FragmentDenomConfirm()

            val bundle = Bundle()
            bundle.putString(WebParams.BANK_NAME, arguments!!.getString(WebParams.BANK_NAME, ""))
            bundle.putString(WebParams.BANK_GATEWAY, arguments!!.getString(WebParams.BANK_GATEWAY, ""))
            bundle.putString(WebParams.BANK_CODE, arguments!!.getString(WebParams.BANK_CODE, ""))
            bundle.putString(WebParams.PRODUCT_CODE, arguments!!.getString(WebParams.PRODUCT_CODE, ""))
            bundle.putString(WebParams.MEMBER_REMARK, arguments!!.getString(WebParams.MEMBER_REMARK, ""))
            bundle.putString(WebParams.STORE_NAME, arguments!!.getString(WebParams.STORE_NAME, ""))
            bundle.putString(WebParams.STORE_ADDRESS, arguments!!.getString(WebParams.STORE_ADDRESS, ""))
            bundle.putString(WebParams.PROMO_CODE, jsonArray.toString())
            if (arguments!!.getBoolean(DefineValue.IS_FAVORITE)) {
                bundle.putBoolean(DefineValue.IS_FAVORITE, true)
                bundle.putString(DefineValue.CUST_ID, arguments!!.getString(DefineValue.CUST_ID))
                bundle.putString(DefineValue.NOTES, arguments!!.getString(DefineValue.NOTES))
                bundle.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.B2B)
                bundle.putString(DefineValue.PRODUCT_TYPE, DefineValue.DENOM_B2B)
            }

            frag.arguments = bundle

            addFragment(frag, DenomSCADMActivity.DENOM_PAYMENT)
        }
    }

    private fun checkArrayPromo(): Boolean {
        for (i in promoCodeList!!.indices) {
            if (promoCodeList!![i].code != "" && promoCodeList!![i].qty == "" ||
                    promoCodeList!![i].code == "" && promoCodeList!![i].qty != "") {
                Toast.makeText(context, resources.getString(R.string.invalid_promo_code), Toast.LENGTH_SHORT).show()
                return false
            }

            if (promoCodeList!!.size > 1 && promoCodeList!![i].code == "" && promoCodeList!![i].qty == "") {
                promoCodeList!!.removeAt(i)
                promoCodeAdapter!!.notifyDataSetChanged()
                return true
            }
        }
        return true
    }
}