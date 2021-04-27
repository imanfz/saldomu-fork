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
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_list_promo_code_eratel.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListPromo : BaseFragment() {
    var memberCode = ""
    var commCode = ""

    var promoCodeListPayNow: ArrayList<PromoCodeBATModel> = ArrayList()
    var promoCodeListPayLater: ArrayList<PromoCodeBATModel> = ArrayList()
    var promoCodeAdapter: PromoCodeTokoAdapter? = null
    var tokoPurchaseOrderActivity: TokoPurchaseOrderActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_list_promo_code_eratel, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        promo_code_submit_btn.visibility = View.GONE
        toggle.visibility = View.VISIBLE

        tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.list_promo))

        if (arguments != null) {
            memberCode = requireArguments().getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = requireArguments().getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
        }

        promoCodeAdapter = PromoCodeTokoAdapter(activity, promoCodeListPayNow, object : PromoCodeTokoAdapter.Listener {
            override fun onCheck(position: Int) {

            }

            override fun onUncheck(position: Int) {

            }
        })
        getPromoList()
        promo_list_field.adapter = promoCodeAdapter
        promo_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        toggle.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == R.id.radio_promo_pay_now)
                promoCodeAdapter!!.updateAdapter(promoCodeListPayNow)
            else if (checkedId == R.id.radio_promo_pay_later)
                promoCodeAdapter!!.updateAdapter(promoCodeListPayLater)
        }
    }

    private fun getPromoList() {
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
                                            if (promoObject.getInt(WebParams.PAID_OPTION) == 1)
                                                promoCodeListPayNow.add(PromoCodeBATModel(promoCode, promoDesc, false, ""))
                                            else if (promoObject.getInt(WebParams.PAID_OPTION) == 2)
                                                promoCodeListPayLater.add(PromoCodeBATModel(promoCode, promoDesc, false, ""))
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

        val btnDialogOTP: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = ""
        message.text = msg
        btnDialogOTP.setOnClickListener {
            dialog.dismiss()
            requireActivity().onBackPressed()
        }
        dialog.show()
    }
}