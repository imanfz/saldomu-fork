package com.sgo.saldomu.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.adapter.PromoCodeAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.models.PromoCodeModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_po.*
import kotlinx.android.synthetic.main.frag_list_po.recyclerViewList
import kotlinx.android.synthetic.main.fragment_denom_input_promo_code.*
import kotlinx.android.synthetic.main.fragment_input_promo_code_toko.*
import kotlinx.android.synthetic.main.fragment_input_promo_code_toko.btn_add_promo
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragInputPromoCodeToko : BaseFragment() {

    var memberCode = ""
    var commCode = ""
    var docDetails = ""

    var promoCodeList: ArrayList<PromoCodeModel> = ArrayList()
    var promoCodeAdapter: PromoCodeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_input_promo_code_toko, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.promo_code))

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            docDetails = arguments!!.getString(DefineValue.DOC_DETAILS, "")
        }

        promoCodeList.add(PromoCodeModel("", "", ""))
        promoCodeAdapter = PromoCodeAdapter(activity, promoCodeList, object : PromoCodeAdapter.Listener {
            override fun onChangePromoCode(position: Int, promoCode: String) {
                promoCodeList[position].code = promoCode
            }

            override fun onChangePromoQty(position: Int, promoQty: String) {
                promoCodeList[position].qty = promoQty
            }

            override fun onDelete(position: Int) {
                promoCodeList.removeAt(position)
                promoCodeAdapter!!.notifyDataSetChanged()
            }

        })
        promo_list_field.adapter = promoCodeAdapter
        promo_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        btn_add_promo.setOnClickListener {
            promoCodeList.add(PromoCodeModel("", "", ""))
            promoCodeAdapter!!.notifyDataSetChanged()
        }

        promo_code_submit_btn.setOnClickListener {
            if (checkArrayPromo()) {
                val listString = Gson().toJson(promoCodeList, object : TypeToken<ArrayList<PromoCodeModel?>?>() {}.type)
                val jsonArray = JSONArray(listString)
                Timber.e(jsonArray.toString())

                showProgressDialog()

                val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRMATION_DOC, memberCode + userPhoneID)
                params[WebParams.USER_ID] = userPhoneID
                params[WebParams.MEMBER_CODE_ESPAY] = memberCode
                params[WebParams.COMM_CODE_ESPAY] = commCode
                params[WebParams.CUST_ID_ESPAY] = userPhoneID
                params[WebParams.CUST_ID] = userPhoneID
                params[WebParams.REFF_ID] = ""
                params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
                params[WebParams.DOC_DETAIL] = docDetails
                params[WebParams.TYPE_ID] = DefineValue.PO
                params[WebParams.CUST_TYPE] = DefineValue.TOKO
                params[WebParams.PROMO_CODE] = jsonArray.toString()

                Timber.d("isi params confirm doc :$params")
                RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRMATION_DOC, params,
                        object : ObjListeners {
                            override fun onResponses(response: JSONObject) {
                                val code = response.getString(WebParams.ERROR_CODE)
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        if (!checkPromoCodeIsValid(response)) {
                                            return
                                        }

                                        val frag = FragOrderConfirmToko()

                                        val bundle = Bundle()
                                        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, ""))
                                        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, ""))
                                        bundle.putString(DefineValue.PAYMENT_OPTION, arguments!!.getString(DefineValue.PAYMENT_OPTION, ""))
                                        bundle.putString(DefineValue.DOC_DETAILS, response.getString(WebParams.DOC_DETAILS))
                                        bundle.putString(DefineValue.PROMO_CODE, jsonArray.toString())
                                        bundle.putString(DefineValue.EBD_CONFIRM_DATA, response.toString())

                                        frag.arguments = bundle
                                        tokoPurchaseOrderActivity.addFragment(frag, getString(R.string.purchase_order_confirmation), tokoPurchaseOrderActivity.FRAG_INPUT_ITEM_TAG)
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        AlertDialogLogout.getInstance().showDialoginMain(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                        val appModel = model.app_data
                                        AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        AlertDialogMaintenance.getInstance().showDialogMaintenance(activity, message)
                                    }
                                    else -> {
                                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            override fun onError(throwable: Throwable?) {
                                dismissProgressDialog()
                            }

                            override fun onComplete() {
                                dismissProgressDialog()
                            }

                        })
            }
        }
    }

    private fun checkArrayPromo(): Boolean {
        for (i in promoCodeList.indices) {
            if (promoCodeList[i].code != "" && promoCodeList[i].qty == "" ||
                    promoCodeList[i].code == "" && promoCodeList[i].qty != "") {
                Toast.makeText(context, resources.getString(R.string.invalid_promo_code), Toast.LENGTH_SHORT).show()
                return false
            }

            if (promoCodeList.size > 1 && promoCodeList[i].code == "" && promoCodeList[i].qty == "") {
                promoCodeList.removeAt(i)
                promoCodeAdapter!!.notifyDataSetChanged()
                return true
            }
        }
        return true
    }

    private fun checkPromoCodeIsValid(response: JSONObject): Boolean {
        val promoCodesString = response.optString(WebParams.PROMO_CODE);
        val promoCodeJSONArray = JSONArray(promoCodesString)
        var isPromoCodeValid = true;

        promoCodeList.clear()

        for (i in 0 until promoCodeJSONArray.length()) {
            val status = promoCodeJSONArray.getJSONObject(i).getString(WebParams.STATUS)
            val code = promoCodeJSONArray.getJSONObject(i).getString(WebParams.CODE)
            val qty = promoCodeJSONArray.getJSONObject(i).getString(WebParams.QTY)
            if (status == "1") {
                isPromoCodeValid = false
            }
            promoCodeList.add(PromoCodeModel(code, qty, status))
            promoCodeAdapter!!.updateAdapter(promoCodeList)
        }

        return isPromoCodeValid
    }
}