package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.InsertPIN
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.AdapterListItemConfirmPO
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DateTimeFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.ReportBillerDialog
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.OnLoadDataListener
import com.sgo.saldomu.loader.UtilsLoader
import com.sgo.saldomu.models.EBDConfirmModel
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.PaymentMethods
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_create_gr.*
import kotlinx.android.synthetic.main.frag_order_confirm_toko.*
import kotlinx.android.synthetic.main.frag_order_confirm_toko.layout_bonus_item
import kotlinx.android.synthetic.main.frag_order_confirm_toko.layout_payment_method
import kotlinx.android.synthetic.main.fragment_input_item_list.*
import org.json.JSONObject
import timber.log.Timber

class FragOrderConfirmToko : BaseFragment() {
    var memberCode = ""
    var shopName = ""
    var commID = ""
    var commCode = ""
    var paymentOption = ""
    var paymentMethodCode = ""
    var docDetail = ""
    var promoCode = ""
    var txID = ""
    var productCode = ""

    var attempt = 0
    var failed = 0

    var isPIN = false

    private var ebdConfirmModel = EBDConfirmModel()

    private val mappingItemList = ArrayList<MappingItemsItem>()
    private val paymentMethodList = ArrayList<PaymentMethods>()

    var tokoPurchaseOrderActivity: TokoPurchaseOrderActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_order_confirm_toko, container, false)
        return v
    }

    override fun onStop() {
        super.onStop()
        if (paymentOption == getString(R.string.pay_now))
            tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.promo_code))
        else
            tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.choose_catalog))
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity!!.initializeToolbar(getString(R.string.purchase_order_confirmation))

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            shopName = arguments!!.getString(DefineValue.MEMBER_SHOP_NAME, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            paymentOption = arguments!!.getString(DefineValue.PAYMENT_OPTION, "")
            docDetail = arguments!!.getString(DefineValue.DOC_DETAILS, "")
            promoCode = arguments!!.getString(DefineValue.PROMO_CODE, "")
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

        val bonusItemsList = docDetails[0].bonus_items
        if (bonusItemsList.isNotEmpty()) {
            layout_bonus_item.visibility = View.VISIBLE
            val adapterBonusItemConfirm = AdapterListItemConfirmPO(context!!, bonusItemsList)
            confirm_bonus_item_list_field.adapter = adapterBonusItemConfirm
            confirm_bonus_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
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

        submit_btn.setOnClickListener { createPO() }
    }

    private fun createPO() {
        showProgressDialog()

        val amount = ebdConfirmModel.amount
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CREATE_PO, memberCode + commCode + amount)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.CUST_ID] = userPhoneID
        params[WebParams.AMOUNT] = amount
        params[WebParams.DISCOUNT_AMOUNT] = ebdConfirmModel.discount_amount
        params[WebParams.TOTAL_AMOUNT] = ebdConfirmModel.total_amount
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.DOC_DETAIL] = docDetail
        params[WebParams.CUST_TYPE] = DefineValue.TOKO
        params[WebParams.ACTION_CODE] = "N"
        if (paymentOption == getString(R.string.pay_now))
            params[WebParams.IS_CHECK_BALANCE] = "Y"
        else if (paymentOption == getString(R.string.pay_later))
            params[WebParams.IS_CHECK_BALANCE] = "N"
        params[WebParams.PROMO_CODE] = promoCode

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
                                    showDialog(message)
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
        params[WebParams.AMOUNT] = ebdConfirmModel.total_amount
        params[WebParams.CUST_TYPE] = DefineValue.TOKO
        params[WebParams.SHOP_PHONE] = userPhoneID
        params[WebParams.SHOP_NAME] = shopName

        Timber.d("isi params payment toko:$params")

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_PAYMENT_TOKO, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                txID = response.getString(WebParams.TX_ID)
                                commCode = response.getString(WebParams.COMM_CODE)
                                productCode = response.getString(WebParams.PRODUCT_CODE)
                                commID = response.getString(WebParams.COMM_ID)
                                getFailedPin()
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
                            DefineValue.ERROR_57 -> {
                                showDialog(message)
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

                    }

                })
    }

    fun getFailedPin() {
        isPIN = true
        showProgressDialog()
        UtilsLoader(activity, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
            override fun onSuccess(deData: Any) {
                dismissProgressDialog()
                attempt = deData as Int
                if (isPIN)
                    callPINInput(attempt)
            }

            override fun onFail(message: Bundle) {}
            override fun onFailure(message: String) {}
        })
    }

    private fun callPINInput(attempt: Int) {
        val i = Intent(activity, InsertPIN::class.java)
        if (attempt == 1)
            i.putExtra(DefineValue.ATTEMPT, attempt)
        startActivityForResult(i, MainPage.REQUEST_FINISH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainPage.REQUEST_FINISH) {
            if (resultCode == InsertPIN.RESULT_PIN_VALUE) {
                val valuePIN = data?.getStringExtra(DefineValue.PIN_VALUE)

                //call insert trx
                sentInsertTrx(valuePIN!!)
            }
        }
    }

    private fun sentInsertTrx(pin: String) {
        showProgressDialog()
        val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
        val subStringLink = link.substring(link.indexOf("saldomu/"))
        extraSignature = txID + commCode + productCode + pin
        val params = RetrofitService.getInstance().getSignature(link, extraSignature)
        val uuid: String = params[WebParams.RC_UUID].toString()
        val dateTime: String = params[WebParams.RC_DTIME].toString()
        params[WebParams.TX_ID] = txID
        params[WebParams.PRODUCT_CODE] = productCode
        params[WebParams.COMM_CODE] = commCode
        params[WebParams.COMM_ID] = commID
        params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
        params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(commID, uuid, dateTime, userPhoneID, pin, subStringLink)
        params[WebParams.USER_ID] = userPhoneID

        Timber.d("isi params insertTrx : $params")
        RetrofitService.getInstance().PostJsonObjRequest(link, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val model = getGson().fromJson(response.toString(), SentPaymentBillerModel::class.java)
                        val code = model.error_code
                        val message = model.error_message
                        if (code == WebParams.SUCCESS_CODE) {
                            getTrxStatus()
                        } else if (code == WebParams.LOGOUT_CODE) {
                            AlertDialogLogout.getInstance().showDialoginActivity2(activity, message)
                        } else {
                            Toast.makeText(activity, "$code : $message", Toast.LENGTH_LONG).show()
                            if (isPIN && message == "PIN tidak sesuai") {
                                val i = Intent(activity, InsertPIN::class.java)

                                attempt = model.failed_attempt
                                failed = model.max_failed

                                if (attempt != -1)
                                    i.putExtra(DefineValue.ATTEMPT, failed - attempt)

                                startActivityForResult(i, MainPage.REQUEST_FINISH)
                            }
                        }
                    }

                    override fun onError(throwable: Throwable?) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {

                    }

                })
    }

    private fun getTrxStatus() {

        showProgressDialog()
        extraSignature = txID + commID
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

        params[WebParams.TX_ID] = txID
        params[WebParams.COMM_ID] = commID
        params[WebParams.PRIVACY] = ""
        params[WebParams.TX_TYPE] = DefineValue.ESPAY
        params[WebParams.USER_ID] = userPhoneID

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        if (code == WebParams.SUCCESS_CODE || code == "0003") {
                            val model = getGson().fromJson(response.toString(), GetTrxStatusReportModel::class.java)
                            showReportBillerDialog(model.tx_status!!, model)
                        } else if (code == WebParams.LOGOUT_CODE) {
                            AlertDialogLogout.getInstance().showDialoginActivity2(activity, message)
                        } else if (code == DefineValue.ERROR_9333) {
                            val model = gson.fromJson(response.toString(), jsonModel::class.java)
                            val appModel = model.app_data
                            AlertDialogUpdateApp.getInstance().showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                        } else if (code == DefineValue.ERROR_0066) {
                            AlertDialogMaintenance.getInstance().showDialogMaintenance(activity, message)
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        dismissProgressDialog()
                    }

                    override fun onComplete() {

                    }
                })

    }

    private fun showReportBillerDialog(txStatus: String, model: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance { backToListPO() }
        args.putString(DefineValue.USER_NAME, sp.getString(DefineValue.USER_NAME, ""))
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(model.created))
        args.putString(DefineValue.TX_ID, model.tx_id)
        args.putString(DefineValue.USERID_PHONE, sp.getString(DefineValue.USERID_PHONE, ""))
        args.putString(DefineValue.DENOM_DATA, "")
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.total_gross))
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.tx_fee))
        var txStat = false
        if (txStatus == DefineValue.SUCCESS)
            txStat = true
        else if (txStatus == DefineValue.ONRECONCILED)
            txStat = true

        args.putBoolean(DefineValue.TRX_STATUS, txStat)
        args.putString(DefineValue.TRX_STATUS_REMARK, model.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, model.tx_remark)

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.total_amount))
        args.putString(DefineValue.DENOM_DETAIL, getGson().toJson(model.denom_detail))
        args.putString(DefineValue.BUSS_SCHEME_CODE, model.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, model.buss_scheme_name)
        args.putString(DefineValue.BANK_PRODUCT, model.product_name)
        args.putString(DefineValue.ORDER_ID, model.order_id)
        args.putString(DefineValue.COMMUNITY_CODE, model.comm_code)
        args.putString(DefineValue.MEMBER_CODE, model.member_code)
        args.putString(DefineValue.STORE_NAME, model.store_name)
        args.putString(DefineValue.STORE_ADDRESS, model.store_address)
        args.putString(DefineValue.STORE_CODE, model.store_code)
        args.putString(DefineValue.AGENT_NAME, model.member_cust_name)
        args.putString(DefineValue.AGENT_PHONE, model.member_cust_id)
        args.putString(DefineValue.TOTAL_DISC, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.total_disc))
        dialog.arguments = args
        val ft = fragManager.beginTransaction()
        ft.add(dialog, ReportBillerDialog.TAG)
        ft.commitAllowingStateLoss()
    }

    private fun showDialog(msg: String) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialogOTP = dialog.findViewById<Button>(R.id.btn_dialog_notification_ok)
        val title = dialog.findViewById<TextView>(R.id.title_dialog)
        val message = dialog.findViewById<TextView>(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.remark)
        message.text = msg
        btnDialogOTP.setOnClickListener {
            dialog.dismiss()
            backToListPO()
        }
        dialog.show()
    }

    private fun backToListPO() {
        dismissProgressDialog()
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
