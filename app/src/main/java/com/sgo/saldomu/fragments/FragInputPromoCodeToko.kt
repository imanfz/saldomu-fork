package com.sgo.saldomu.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
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
    var orderSetting = ""

    var promoCodeList: ArrayList<PromoCodeModel> = ArrayList()
    var promoCodeAdapter: PromoCodeTokoAdapter? = null

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
            memberCode = requireArguments().getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = requireArguments().getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            docDetails = requireArguments().getString(DefineValue.DOC_DETAILS, "")
            orderSetting = requireArguments().getString(DefineValue.ORDER_SETTING, "")
        }

        promoCodeList.add(PromoCodeModel("", "1", ""))
        promoCodeAdapter = PromoCodeTokoAdapter(activity, promoCodeList, object : PromoCodeTokoAdapter.Listener {
            override fun onChangePromoCode(position: Int, promoCode: String) {
                promoCodeList[position].code = promoCode
            }

            override fun onDelete(position: Int) {
                promoCodeList.removeAt(position)
                promoCodeAdapter!!.notifyDataSetChanged()
            }

        })
        promo_list_field.adapter = promoCodeAdapter
        promo_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        btn_add_promo.setOnClickListener {
            promoCodeList.add(PromoCodeModel("", "1", ""))
            promoCodeAdapter!!.notifyDataSetChanged()
        }

        promo_code_submit_btn.setOnClickListener {
            if (checkArrayPromo()) {
                var jsonArray = ""
                if (promoCodeList.isNotEmpty()) {
                    val listString = Gson().toJson(promoCodeList, object : TypeToken<ArrayList<PromoCodeModel?>?>() {}.type)
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
    }

    private fun showDialog(msg: String) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialogOTP: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.error)
        message.text = msg
        btnDialogOTP.setOnClickListener {
            dialog.dismiss()
            requireFragmentManager().popBackStack()
        }
        dialog.show()
    }

    private fun checkArrayPromo(): Boolean {
        for (i in promoCodeList.indices) {
            if (promoCodeList.size > 0 && promoCodeList[i].code == "") {
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