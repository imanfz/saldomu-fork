package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.AdapterListItemConfirmPO
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.EBDConfirmModel
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.PaymentMethods
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_order_confirm_toko.*
import org.json.JSONObject
import timber.log.Timber

class FragOrderConfirmToko : BaseFragment() {
    var memberCode = ""
    var commCode = ""
    private var paymentOption = ""
    var paymentMethodCode = ""
    var docDetail = ""

    private var ebdConfirmModel = EBDConfirmModel()

    private val mappingItemList = ArrayList<MappingItemsItem>()
    private val paymentMethodList = ArrayList<PaymentMethods>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_order_confirm_toko, container, false)
        return v
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.purchase_order_confirmation))

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            paymentOption = arguments!!.getString(DefineValue.PAYMENT_OPTION, "")
            docDetail = arguments!!.getString(DefineValue.DOC_DETAILS, "")
            ebdConfirmModel = getGson().fromJson(arguments!!.getString(DefineValue.EBD_CONFIRM_DATA, ""), EBDConfirmModel::class.java)
        }

        member_code_field.text = memberCode
        comm_code_field.text = commCode
        amount_field.text = getString(R.string.currency) + " " + CurrencyFormat.format(ebdConfirmModel.amount)
        discount_field.text = getString(R.string.currency) + " " + CurrencyFormat.format(ebdConfirmModel.discount_amount)
        total_field.text = getString(R.string.currency) + " " + CurrencyFormat.format(ebdConfirmModel.total_amount)
        val docDetails = ebdConfirmModel.doc_details
        mappingItemList.addAll(docDetails[0].mapping_items)
        val adapterListItemConfirmPO = AdapterListItemConfirmPO(context!!, mappingItemList)
        item_list_field.adapter = adapterListItemConfirmPO
        item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        if (paymentOption == getString(R.string.pay_now)) {
            paymentMethodList.addAll(ebdConfirmModel.payment_methods)
            val paymentMethodNameList = ArrayList<String>()
            for (i in paymentMethodList.indices) {
                paymentMethodNameList.add(paymentMethodList[i].payment_name)
            }
            val paymentMethodAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentMethodNameList)
            spinner_payment_method.adapter = paymentMethodAdapter
            if (paymentMethodList.size == 1)
                spinner_payment_method.isEnabled = false
            spinner_payment_method.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    paymentMethodCode = paymentMethodList[p2].payment_code
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        } else if (paymentOption == getString(R.string.pay_later))
            layout_payment_method.visibility = View.GONE
        submit_btn.setOnClickListener { submitOrder() }
    }

    private fun submitOrder() {
        showProgressDialog()

        val amount = ebdConfirmModel.amount
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CREATE_PO, memberCode + commCode + amount)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.CUST_ID] = userPhoneID
        params[WebParams.AMOUNT] = amount
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.DOC_DETAIL] = docDetail
        params[WebParams.CUST_TYPE] = DefineValue.TOKO
        params[WebParams.ACTION_CODE] = "N"

        Timber.d("isi params create PO:$params")

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CREATE_PO, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                if (paymentOption == getString(R.string.pay_now)) {
                                    val docNo = response.getString(WebParams.PO_NO)
                                    val partnerCode = response.getString(WebParams.PARTNER_CODE_ESPAY)
                                    payment(docNo, partnerCode)
                                } else if (paymentOption == getString(R.string.pay_later))
                                    backToListPO()
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

    private fun payment(docNo: String, partnerCode: String) {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_PAYMENT_TOKO, memberCode + commCode + docNo)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.PARTNER_CODE_ESPAY] = partnerCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.PAYMENT_TYPE] = paymentMethodCode
        params[WebParams.DOC_NO] = docNo
        params[WebParams.TYPE_ID] = DefineValue.PO
        params[WebParams.AMOUNT] = ebdConfirmModel.amount
        params[WebParams.CUST_TYPE] = DefineValue.TOKO
        params[WebParams.SHOP_PHONE] = userPhoneID

        Timber.d("isi params request payment:$params")

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_PAYMENT_TOKO, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {

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

    private fun backToListPO() {
        val frags = fragmentManager!!.fragments
        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        for (f in frags) {
            if (f.tag == tokoPurchaseOrderActivity.FRAG_INPUT_ITEM_TAG) {
                val fragmentTransaction = fragmentManager!!.beginTransaction()
                fragmentTransaction.remove(f).commit()
            }
        }
        fragManager.popBackStack()
    }
}
