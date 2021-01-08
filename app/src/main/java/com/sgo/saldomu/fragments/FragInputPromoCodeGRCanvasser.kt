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
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.adapter.PromoCodeAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.PromoCodeModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_denom_input_promo_code.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragInputPromoCodeGRCanvasser : BaseFragment() {

    var promoCodeList: ArrayList<PromoCodeModel>? = ArrayList()
    var promoCodeAdapter: PromoCodeAdapter? = null
    var bundle = Bundle()
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""
    var docDetails = ""
    var docNo = ""
    var isHavePromoCode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_denom_input_promo_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bundle = arguments!!

        if (bundle != null) {
            memberCodeEspay = bundle!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay = bundle!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            custIdEspay = bundle!!.getString(DefineValue.CUST_ID_ESPAY, "")
            docNo = bundle!!.getString(DefineValue.DOC_NO, "")
            docDetails = bundle!!.getString(DefineValue.DOC_DETAILS, "")
        }

        promoCodeList!!.add(PromoCodeModel("", "", ""))
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
            promoCodeList!!.add(PromoCodeModel("", "", ""))
            promoCodeAdapter!!.notifyDataSetChanged()
        }

        frag_denom_input_promo_code_submit_btn.setOnClickListener {
            if (checkArrayPromo()) {
                val listString = Gson().toJson(promoCodeList, object : TypeToken<ArrayList<PromoCodeModel?>?>() {}.type)
                val jsonArray = JSONArray(listString)

                for (i in promoCodeList!!.indices) {
                    if (promoCodeList!![i].code == "" || promoCodeList!![i].qty == "" ) {
                        isHavePromoCode = false
                    } else
                        isHavePromoCode = true
                }
                Timber.e(jsonArray.toString())
                confirmDocument(isHavePromoCode, jsonArray.toString())
            }
        }
    }

    private fun checkArrayPromo(): Boolean {
        for (i in promoCodeList!!.indices) {
            if (promoCodeList!![i].code != "" && promoCodeList!![i].qty == "" ||
                    promoCodeList!![i].code == "" && promoCodeList!![i].qty != "") {
                Toast.makeText(context, resources.getString(R.string.invalid_promo_code), Toast.LENGTH_SHORT).show()
                return false
            }

            if (promoCodeList!!.size > 0 && promoCodeList!![i].code == "" && promoCodeList!![i].qty == "") {
                promoCodeList!!.removeAt(i)
                promoCodeAdapter!!.notifyDataSetChanged()
                return true
            }
        }
        return true
    }

    private fun confirmDocument(isHavePromoCode: Boolean, promoCode: String) {
        try {
            showProgressDialog()

            extraSignature = memberCodeEspay + custIdEspay
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRMATION_DOC, extraSignature)
            params[WebParams.DOC_NO] = docNo
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.CUST_ID] = userPhoneID
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.TYPE_ID] = DefineValue.GR
            params[WebParams.DOC_DETAIL] = docDetails
            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
            params[WebParams.INVOICE_NOTE] = ""
            if (isHavePromoCode)
                params[WebParams.PROMO_CODE] = promoCode
            Timber.d("params confirm doc:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRMATION_DOC, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response confirm doc:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        if (!checkPromoCodeIsValid(response)) {
                                            return
                                        }

                                        val bundle = Bundle()
                                        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
                                        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
                                        bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
                                        bundle.putString(DefineValue.DOC_DETAILS, response.optString(WebParams.DOC_DETAILS))
                                        bundle.putString(DefineValue.AMOUNT, response.optString(WebParams.AMOUNT))
                                        bundle.putString(DefineValue.TOTAL_AMOUNT, response.optString(WebParams.TOTAL_AMOUNT))
                                        bundle.putString(DefineValue.TOTAL_DISC, response.optString(WebParams.DISCOUNT_AMOUNT))
                                        bundle.putString(DefineValue.DOC_NO, docNo)
                                        if (isHavePromoCode)
                                            bundle.putString(DefineValue.PROMO_CODE, promoCode)
                                        val frag: Fragment = FragCreateGR()
                                        frag.arguments = bundle
                                        switchFragment(frag, "", "", true, "")

                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:%s", model.app_data)
                                        val appModel = model.app_data
                                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                    }
                                    DefineValue.ERROR_0066 -> {
                                        Timber.d("isi response maintenance:$response")
                                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                        alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                    }
                                    else -> {
                                        Timber.d("isi error confirm doc:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun checkPromoCodeIsValid(response: JSONObject): Boolean {
        val promoCodesString = response.optString(WebParams.PROMO_CODE);
        val promoCodeJSONArray = JSONArray(promoCodesString)
        var isPromoCodeValid = true;

        promoCodeList!!.clear()

        for (i in 0 until promoCodeJSONArray.length()) {
            val status = promoCodeJSONArray.getJSONObject(i).getString(WebParams.STATUS)
            val code = promoCodeJSONArray.getJSONObject(i).getString(WebParams.CODE)
            val qty = promoCodeJSONArray.getJSONObject(i).getString(WebParams.QTY)
            if (status.equals("1")) {
                isPromoCodeValid = false
            }
            promoCodeList!!.add(PromoCodeModel(code, qty, status))
            promoCodeAdapter!!.updateAdapter(promoCodeList!!)
        }

        return isPromoCodeValid
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }
}