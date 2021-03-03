package com.sgo.saldomu.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.PromoCodeTokoAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
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


class FragListPromoCodeToko : BaseFragment() {

    var memberCode = ""
    var commCode = ""
    var docDetails = ""
    var orderSetting = ""
    var partner = ""

    var promoCodeList: ArrayList<PromoCodeBATModel> = ArrayList()
    var desirePromoCodeList: ArrayList<PromoCodeModel> = ArrayList()
    var promoCodeAdapter: PromoCodeTokoAdapter? = null
    var tokoPurchaseOrderActivity: TokoPurchaseOrderActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_list_promo_code_eratel, container, false)
        return v
    }

    override fun onStop() {
        super.onStop()
        tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.choose_catalog))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.promo_code))

        if (arguments != null) {
            memberCode = requireArguments().getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = requireArguments().getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            docDetails = requireArguments().getString(DefineValue.DOC_DETAILS, "")
            orderSetting = requireArguments().getString(DefineValue.ORDER_SETTING, "")
            partner = requireArguments().getString(DefineValue.PARTNER, "")
        }

        promoCodeAdapter = PromoCodeTokoAdapter(activity, promoCodeList, object : PromoCodeTokoAdapter.Listener {
            override fun onCheck(position: Int) {
                promoCodeList[position].checked = true
            }

            override fun onUncheck(position: Int) {
                promoCodeList[position].checked = false
            }
        })
        getPromoList()
        promo_list_field.adapter = promoCodeAdapter
        promo_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        promo_code_submit_btn.setOnClickListener {
            var jsonArray = ""
            desirePromoCodeList.clear()
            for (i in promoCodeList.indices) {
                if (promoCodeList[i].checked)
                    desirePromoCodeList.add(PromoCodeModel(promoCodeList[i].code, "1", ""))
            }
            if (desirePromoCodeList.isNotEmpty()) {
                val listString = Gson().toJson(desirePromoCodeList, object : TypeToken<ArrayList<PromoCodeModel?>?>() {}.type)
                jsonArray = JSONArray(listString).toString()
                Timber.e(jsonArray)
            }

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
            params[WebParams.PROMO_CODE] = jsonArray
            params[WebParams.ORDER_SETTING] = orderSetting
            params[WebParams.PARTNER_CODE_ESPAY] = partner

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
                                    bundle.putString(DefineValue.MEMBER_SHOP_NAME, arguments!!.getString(DefineValue.MEMBER_SHOP_NAME, ""))
                                    bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, ""))
                                    bundle.putString(DefineValue.PAYMENT_OPTION, arguments!!.getString(DefineValue.PAYMENT_OPTION, ""))
                                    bundle.putString(DefineValue.DOC_DETAILS, response.getString(WebParams.DOC_DETAILS))
                                    bundle.putString(DefineValue.PROMO_CODE, jsonArray)
                                    bundle.putString(DefineValue.EBD_CONFIRM_DATA, response.toString())

                                    frag.arguments = bundle
                                    tokoPurchaseOrderActivity!!.addFragment(frag, getString(R.string.purchase_order_confirmation), tokoPurchaseOrderActivity!!.FRAG_INPUT_ITEM_TAG)
                                }
                                WebParams.LOGOUT_CODE -> {
                                    AlertDialogLogout.getInstance().showDialoginActivity2(activity, message)
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

    private fun getPromoList() {
        promoCodeList.clear()
        try {
            showProgressDialog()
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_PROMO_EBD, memberCode + commCode)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_CODE_ESPAY] = memberCode
            params[WebParams.COMM_CODE_ESPAY] = commCode
            params[WebParams.CUST_ID_ESPAY] = userPhoneID
            params[WebParams.CUST_TYPE] = DefineValue.TOKO

            Timber.d("isi params get promo list:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_PROMO_EBD, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val code = response.getString(WebParams.ERROR_CODE)
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
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
                                        AlertDialogLogout.getInstance().showDialoginActivity2(activity, message)
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
            promoCodeAdapter!!.notifyDataSetChanged()
        }

        return isPromoCodeValid
    }
}