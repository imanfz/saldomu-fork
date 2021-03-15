package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.InsertPIN
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.PaymentTokoActivity
import com.sgo.saldomu.adapter.AdapterListDetailPO
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
import com.sgo.saldomu.models.DocDetailsItem
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel
import com.sgo.saldomu.models.retrofit.SentPaymentBillerModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_detail_po.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragPaymentByToko : BaseFragment() {
    var memberCode: String = ""
    var commCodeEspay: String = ""
    var commCode: String = ""
    var docNo: String = ""
    var txId: String = ""
    var productCode = ""
    var commId = ""
    var attempt = 0
    var failed = 0

    var isPIN = false
    private var adapterDetailPO: AdapterListDetailPO? = null
    private val itemList = ArrayList<MappingItemsItem>()
    private val bonusItemList = ArrayList<MappingItemsItem>()
    private var adapterDetailPOBonusItem: AdapterListDetailPO? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_detail_po, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay= arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            commCode= arguments!!.getString(DefineValue.COMMUNITY_CODE, "")
            commId= arguments!!.getString(DefineValue.COMMUNITY_ID, "")
            docNo = arguments!!.getString(DefineValue.DOC_NO, "")
            txId = arguments!!.getString(DefineValue.TX_ID, "")
        }

        productCode = "SCASH"
        val paymentTokoActivity = activity as PaymentTokoActivity
        paymentTokoActivity.initializeToolbar(getString(R.string.detail_document))

        adapterDetailPO = AdapterListDetailPO(context!!, itemList)
        recyclerViewList.adapter = adapterDetailPO
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)


        adapterDetailPOBonusItem = AdapterListDetailPO(context!!, bonusItemList)
        recyclerViewListBonus.adapter = adapterDetailPOBonusItem
        recyclerViewListBonus.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        getDocDetail()

        btn_payment.setOnClickListener { payInvoice() }
    }

    private fun getDocDetail() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, memberCode + commCodeEspay + docNo)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.DOC_NO] = docNo

        Timber.d("isi params get doc detail paymentByToko:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                object : ObjListeners {
                    @SuppressLint("SetTextI18n")
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                btn_payment.visibility = View.VISIBLE
                                tv_docNo.text = response.getString(WebParams.DOC_NO)
                                tv_totalAmount.text = getString(R.string.currency) + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_AMOUNT))
                                val bonusItem = response.getString(WebParams.BONUS_ITEMS)
                                val mArrayItem = JSONArray(response.getString(WebParams.ITEMS))
                                for (i in 0 until mArrayItem.length()) {
                                    val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
                                    val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
                                    val price = mArrayItem.getJSONObject(i).getString(WebParams.PRICE)
                                    val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
                                    val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
                                    val formatQtyItemList = ArrayList<FormatQtyItem>()
                                    for (j in 0 until formatQtyJsonArray.length()) {
                                        val mappingUnit = formatQtyJsonArray.getJSONObject(j).getString(WebParams.MAPPING_UNIT)
                                        val mappingQty = formatQtyJsonArray.getJSONObject(j).getInt(WebParams.MAPPING_QTY)
                                        val formatQtyItem = FormatQtyItem()
                                        formatQtyItem.mapping_unit = mappingUnit
                                        formatQtyItem.mapping_qty = mappingQty
                                        formatQtyItemList.add(formatQtyItem)
                                    }
                                    val mappingItemsItem = MappingItemsItem()
                                    mappingItemsItem.item_name = itemName
                                    mappingItemsItem.item_code = itemCode
                                    mappingItemsItem.price = price.toInt()
                                    mappingItemsItem.unit = unit
                                    mappingItemsItem.format_qty = formatQtyItemList
                                    itemList.add(mappingItemsItem)
                                }
                                adapterDetailPO!!.notifyDataSetChanged()

                                if (bonusItem != "") {
                                    val model = getGson().fromJson(response.toString(), DocDetailsItem::class.java)
                                    bonusItemList.addAll(model.bonus_items)
                                    layout_bonus_item.visibility = View.VISIBLE
                                } else
                                    layout_bonus_item.visibility = View.GONE

                                adapterDetailPOBonusItem!!.notifyDataSetChanged()
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

    private fun payInvoice() {
        showProgressDialog()
        extraSignature = txId + commCode
        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, extraSignature)
        params[WebParams.APP_ID] = BuildConfig.APP_ID
        params[WebParams.TX_ID] = txId
        params[WebParams.COMM_CODE] = commCode
        params[WebParams.USER_COMM_CODE] = sp.getString(DefineValue.COMMUNITY_CODE, "")
        params[WebParams.USER_ID] = userPhoneID
        Timber.d("params confirm payment invoice : $params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            dismissProgressDialog()
                            val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                            val code = response.getString(WebParams.ERROR_CODE)
                            val error_message = response.getString(WebParams.ERROR_MESSAGE)
                            Timber.d("response confirm payment invoice : $response")
                            if (code == WebParams.SUCCESS_CODE) {
                                sentInquiry()
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$response")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                Toast.makeText(activity, error_message, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(throwable: Throwable) {}
                    override fun onComplete() {}
                })
    }

    fun sentInquiry() {
        try {
            showProgressDialog()
            extraSignature = txId + commCode + productCode
            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)
            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            Timber.d("isi params InquiryTrx payment invoice:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                var code = response.getString(WebParams.ERROR_CODE)
                                val error_message = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response InquiryTrx payment invoice: $response")
                                if (code == WebParams.SUCCESS_CODE) {
                                        getFailedPin()
//                                        callPINInput(attempt)
                                } else if (code == WebParams.LOGOUT_CODE) {
                                    Timber.d("isi response autologout:$response")
                                    val message = response.getString(WebParams.ERROR_MESSAGE)
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                } else if (code == DefineValue.ERROR_9333) {
                                    Timber.d("isi response app data:" + model.app_data)
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                } else if (code == DefineValue.ERROR_0066) {
                                    Timber.d("isi response maintenance:$response")
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                } else {
                                    Timber.d("Error inquiryTrx payment invoice:$response")
                                    code = response.getString(WebParams.ERROR_MESSAGE)
                                    Toast.makeText(activity, code, Toast.LENGTH_SHORT).show()
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
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
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

    private fun callPINInput(_attempt: Int) {
        val i = Intent(activity, InsertPIN::class.java)
        if (_attempt == 1) i.putExtra(DefineValue.ATTEMPT, _attempt)
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

    fun sentInsertTrx(tokenValue: String) {
        try {
            showProgressDialog()
            val args = arguments
            val link = MyApiClient.LINK_INSERT_TRANS_TOPUP
            val subStringLink = link.substring(link.indexOf("saldomu/"))
            val uuid: String
            val dateTime: String
            extraSignature = txId + commCode + productCode + tokenValue
            params = RetrofitService.getInstance().getSignature(link, extraSignature)
            uuid = params[WebParams.RC_UUID].toString()
            dateTime = params[WebParams.RC_DTIME].toString()
            attempt = args!!.getInt(DefineValue.ATTEMPT, -1)
            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.COMM_ID] = MyApiClient.COMM_ID
            params[WebParams.MEMBER_ID] = sp.getString(DefineValue.MEMBER_ID, "")
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncryptCommID(commIDLogin, uuid, dateTime, userPhoneID, tokenValue, subStringLink)
            params[WebParams.USER_ID] = userPhoneID
            Timber.d("isi params insertTrxTOpupSGOL:$params")
            RetrofitService.getInstance().PostJsonObjRequest(link, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                dismissProgressDialog()
                                val model = getGson().fromJson(response.toString(), SentPaymentBillerModel::class.java)
                                var code = response.getString(WebParams.ERROR_CODE)
                                var message = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response insertTrxTOpupSGOL:$response")
                                if (code == WebParams.SUCCESS_CODE) {
                                    getTrxStatus()
                                } else if (code == WebParams.LOGOUT_CODE) {
                                    Timber.d("isi response autologout:$response")
                                    val message = response.getString(WebParams.ERROR_MESSAGE)
                                    val test = AlertDialogLogout.getInstance()
                                    test.showDialoginActivity(activity, message)
                                } else if (code == DefineValue.ERROR_9333) {
                                    Timber.d("isi response app data:" + model.app_data)
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                } else if (code == DefineValue.ERROR_0066) {
                                    Timber.d("isi response maintenance:$response")
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
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
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun getTrxStatus() {

        showProgressDialog()
        extraSignature = txId + commId
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

        params[WebParams.TX_ID] = txId
        params[WebParams.COMM_ID] = commId
        params[WebParams.PRIVACY] = ""
        params[WebParams.TX_TYPE] = DefineValue.ESPAY
        params[WebParams.USER_ID] = userPhoneID

        Timber.d("isi params trx status:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {

                        Timber.d("isi response trx status:$response")
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
        val dialog = ReportBillerDialog.newInstance { this }
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

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.nett_amount))
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
        args.putString(DefineValue.PARTNER_CODE_ESPAY, model.partner)
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
            activity!!.finish()
        }
        dialog.show()
    }
}