package com.sgo.saldomu.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.adapter.PromoCodeCanvasserAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.PromoCodeBATModel
import com.sgo.saldomu.models.PromoCodeModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_list_promo_code_eratel.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class FragInputPromoCodeGRCanvasser : BaseFragment() {

    var promoCodeList: ArrayList<PromoCodeBATModel> = ArrayList()
    var desirePromoCodeList: MutableList<PromoCodeModel> = ArrayList()
    var promoCodeAdapter: PromoCodeCanvasserAdapter? = null
    var bundle = Bundle()
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""
    var docDetails = ""
    var docNo = ""
    var bonusItems = ""
    var partner = ""
    var partnerCodeEspay = ""
    var isHavePromoCode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_promo_code_eratel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val canvasserGoodReceiptActivity = activity as CanvasserGoodReceiptActivity
        canvasserGoodReceiptActivity.initializeToolbar(getString(R.string.promo_code))

        bundle = arguments!!

        memberCodeEspay = bundle.getString(DefineValue.MEMBER_CODE_ESPAY, "")
        commCodeEspay = bundle.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
        custIdEspay = bundle.getString(DefineValue.CUST_ID_ESPAY, "")
        docNo = bundle.getString(DefineValue.DOC_NO, "")
        docDetails = bundle.getString(DefineValue.DOC_DETAILS, "")
        bonusItems = bundle.getString(DefineValue.BONUS_ITEMS, "")
        partner = bundle.getString(DefineValue.PARTNER, "")
        partnerCodeEspay = bundle.getString(DefineValue.PARTNER_CODE_ESPAY, "")
        desirePromoCodeList.clear()
        val comparator = Comparator<PromoCodeModel> { p0, p1 -> p0.code.compareTo(p1.code) }
        promoCodeAdapter = PromoCodeCanvasserAdapter(activity, promoCodeList, object : PromoCodeCanvasserAdapter.Listener {
            override fun onCheck(position: Int) {
                promoCodeList[position].checked = true
                promoCodeAdapter!!.notifyDataSetChanged()
            }

            override fun onUncheck(position: Int) {
                promoCodeList[position].checked = false
                promoCodeAdapter!!.notifyDataSetChanged()
                for (i in desirePromoCodeList.indices) {
                    if (promoCodeList[position].code == desirePromoCodeList[i].code)
                        desirePromoCodeList.removeAt(i)
                }
            }

            override fun onChangeQty(promoCode: String, qty: String) {
                for (i in promoCodeList.indices) {
                    if (promoCodeList[i].checked && promoCodeList[i].code == promoCode) {
                        if (qty != "0") {
                            val promoCodeModel = PromoCodeModel(promoCodeList[i].code, qty, "")
                            val position = Collections.binarySearch(desirePromoCodeList, promoCodeModel, comparator)
                            if (position >= 0)
                                desirePromoCodeList[position] = promoCodeModel
                            else
                                desirePromoCodeList.add(promoCodeModel)
                        } else
                            desirePromoCodeList.removeAt(i)
                    }
                }
            }
        })
        promo_list_field.adapter = promoCodeAdapter
        promo_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        getPromoList()
        promo_code_submit_btn.setOnClickListener {
            val listString = Gson().toJson(desirePromoCodeList, object : TypeToken<ArrayList<PromoCodeModel?>?>() {}.type)
            val jsonArray = JSONArray(listString)

            isHavePromoCode = desirePromoCodeList.isNotEmpty()

            Timber.e(jsonArray.toString())
            confirmDocument(isHavePromoCode, jsonArray.toString())
        }
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
            params[WebParams.PREV_BONUS_ITEMS] = bonusItems
            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
            params[WebParams.INVOICE_NOTE] = ""
            params[WebParams.PARTNER_CODE_ESPAY] = partnerCodeEspay
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
                                        bundle.putString(DefineValue.PARTNER, partner)
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

        for (i in 0 until promoCodeJSONArray.length()) {
            val status = promoCodeJSONArray.getJSONObject(i).getString(WebParams.STATUS)
            val code = promoCodeJSONArray.getJSONObject(i).getString(WebParams.CODE)
            if (status == "1") {
                isPromoCodeValid = false
            }
            for (j in promoCodeList.indices)
                if (promoCodeList[j].code == code)
                    promoCodeList[j].status = status
            promoCodeAdapter!!.updateAdapter(promoCodeList)
        }

        return isPromoCodeValid
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }

    private fun getPromoList() {
        promoCodeList.clear()
        try {
            showProgressDialog()
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_PROMO_EBD, memberCodeEspay + commCodeEspay)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER

            Timber.d("isi params get promo list:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_PROMO_EBD, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val code = response.getString(WebParams.ERROR_CODE)
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        promoCodeList.clear()
                                        val promoArray = response.getJSONArray(WebParams.PROMO)
                                        for (i in 0 until promoArray.length()) {
                                            val promoObject = promoArray.getJSONObject(i)
                                            val promoCode = promoObject.getString(WebParams.CODE)
                                            val promoDesc = promoObject.getString(WebParams.DESC)
                                            promoCodeList.add(PromoCodeBATModel(promoCode, promoDesc, false, ""))
                                        }
                                        promoCodeAdapter!!.notifyDataSetChanged()
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
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
                                        showDialog(message)
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {
                            dismissProgressDialog()
                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun showDialog(msg: String) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialog: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.remark)
        message.text = msg
        btnDialog.setOnClickListener {
            dialog.dismiss()
            requireActivity().onBackPressed()
        }
        dialog.show()
    }
}
