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
import com.google.gson.JsonObject
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.InsertPIN
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.entityRealm.BBSAccountACTModel
import com.sgo.saldomu.entityRealm.BBSBankModel
import com.sgo.saldomu.entityRealm.BBSCommModel
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.OnLoadDataListener
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.loader.UtilsLoader
import com.sgo.saldomu.models.retrofit.*
import com.sgo.saldomu.securities.RSA
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_cash_collection.*
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_biller_input_new.*
import kotlinx.android.synthetic.main.frag_cash_collection.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragCashCollection : BaseFragment(), ReportBillerDialog.OnDialogOkCallback {

    private val CTR = "CTR"
    private val BENEF = "BENEF"
    private val SOURCE = "SOURCE"
    private var bankCode: String? = null
    private var customerId: String? = null
    private var accNo: String? = null
    private var accName: String? = null
    private var amount: String? = null
    private var commId: String? = null
    private var commCode: String? = null
    private var memberCode: String? = null
    private var sourceProductType: String? = null
    private var sourceProductCode: String? = null
    private var benefProductType: String? = null
    private var benefProductCode: String? = null
    private var cityId: String? = null
    private var txId: String? = null
    private var productCode: String? = null
    private var memberId: String? = null
    private var isPIN: Boolean? = false
    private var attempt = 0
    private var failed = 0

    private var comm: BBSCommModel? = null
    private var realmBBS: Realm? = null
    private var accountList = ArrayList<AccountsModel>()
    private var accountListData = ArrayList<String>()
    private var listbankBenef: List<BBSBankModel>? = null
    private var listbankSource: List<BBSBankModel>? = null
    private lateinit var viewLayout: View
    private var dialog: Dialog? = null
    private lateinit var dialogReport: ReportBillerDialog
    private lateinit var cashCollectionModel: CashCollectionModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLayout = inflater.inflate(R.layout.frag_cash_collection, container, false)
        return viewLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration)

        memberId = sp.getString(DefineValue.MEMBER_ID, "")
        bankCode = arguments!!.getString(DefineValue.BANK_CODE, "022")

        comm = realmBBS?.where(BBSCommModel::class.java)?.equalTo(WebParams.SCHEME_CODE, CTR)?.findFirst()
        listbankBenef = realmBBS?.where(BBSBankModel::class.java)?.equalTo(WebParams.SCHEME_CODE, CTR)?.equalTo(WebParams.COMM_TYPE, BENEF)?.findAll()
        listbankSource = realmBBS?.where(BBSBankModel::class.java)?.equalTo(WebParams.SCHEME_CODE, CTR)?.equalTo(WebParams.COMM_TYPE, SOURCE)?.findAll()

        commId = comm?.comm_id
        commCode = comm?.comm_code
        memberCode = comm?.member_code

        sourceProductType = listbankSource?.get(0)?.product_type
        sourceProductCode = listbankSource?.get(0)?.product_code

        benefProductType = listbankBenef?.get(0)?.product_type
        benefProductCode = listbankBenef?.get(0)?.product_code

        cityId = "KOTAJAKARTA"
        initlayout()

        btn_search.setOnClickListener {
            if (inputValidation()) {
                et_no_acct.visibility = View.GONE
                searchMember()
            }
        }
        spinner_no_acc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinner_no_acc.getItemAtPosition(position).toString().equals("Rekening Lainnya")) {
                    divider_acc.visibility = View.VISIBLE
                    et_no_acct.visibility = View.VISIBLE
                } else {
                    accNo = accountList[position].acct_no
                    accName = accountList[position].acct_name
                    et_no_acct.visibility = View.GONE
                }
            }

        }
        btn_submit.setOnClickListener { showDialogConfirmation() }
    }

    private fun initlayout() {
        detail_cash_collection.visibility = View.GONE
        layout_acc_amount.visibility = View.GONE
    }

    private fun searchMember() {
        showProgressDialog()

        customerId = NoHPFormat.formatTo62(et_id_member.text.toString())
        extraSignature = bankCode + customerId

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INQUIRY_CUSTOMER_ACCT, extraSignature)
        params[WebParams.USER_ID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.CUSTOMER_ID] = customerId
        params[WebParams.BANK_CODE] = bankCode

        Timber.d("isi params sent cust acct:$params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INQUIRY_CUSTOMER_ACCT, params, object : ResponseListener {
            override fun onResponses(response: JsonObject?) {

                Timber.d("isi response sent cust acct:$response")
                cashCollectionModel = getGson().fromJson(response, CashCollectionModel::class.java)
                when (cashCollectionModel.error_code) {
                    WebParams.SUCCESS_CODE -> {

                        divider_id.visibility = View.VISIBLE
                        tv_name.text = cashCollectionModel.customer_name
                        tv_business_name.text = cashCollectionModel.business_name
                        tv_address.text = cashCollectionModel.cust_address
                        accountListData.clear()
                        accountList.clear()
                        var i = 0
                        cashCollectionModel.accounts.forEach { result ->
                            accountList.add(result)
                            accountListData.add(accountList[i].acct_no)
                            i++
                        }
                        if (accountList.size != 0) {
                            accountListData.add(getString(R.string.other_acct))
                            spinner_no_acc.visibility = View.VISIBLE
                            spinner_no_acc.adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, accountListData)
                        } else {
                            spinner_no_acc.visibility = View.GONE
                            et_no_acct.visibility = View.VISIBLE
                        }

                        detail_cash_collection.visibility = View.VISIBLE
                        layout_acc_amount.visibility = View.VISIBLE
                        btn_submit.visibility=View.VISIBLE
                    }
                    WebParams.LOGOUT_CODE -> {
                        val message = cashCollectionModel.error_message
                        val test = AlertDialogLogout.getInstance()
                        test.showDialoginMain(activity, message)
                    }
                    WebParams.ERROR_9333 -> {
                        Timber.d("isi response app data:" + cashCollectionModel.app_data)
                        val appModel = cashCollectionModel.app_data
                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                    }
                    WebParams.ERROR_0066 -> {
                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                        alertDialogMaintenance.showDialogMaintenance(activity, cashCollectionModel.error_message)
                    }
                    else -> {
                        val msg = cashCollectionModel.error_message
                        showDialog(msg)
                    }
                }
            }

            override fun onError(throwable: Throwable?) {

            }

            override fun onComplete() {
                dismissProgressDialog()
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun showDialogConfirmation() {
        dialog = Dialog(context!!)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.dialog_cash_collection)
        dialog?.setTitle(getString(R.string.are_you_sure))

        amount = et_amount_deposit.text.toString()
        if (et_no_acct.visibility == View.VISIBLE) {
            dialog?.dialog_cash_collection_tv_acc_no?.text = et_no_acct.text
        } else {
            dialog?.dialog_cash_collection_tv_acc_no?.text = accNo
        }
        if (!accName.isNullOrEmpty()) {
            dialog?.dialog_cash_collection_tv_name?.text = accName
        } else
            dialog?.dialog_cash_collection_tv_name?.text = tv_name.text.toString()
        if (amount == "")
            amount = "0"
        dialog?.dialog_cash_collection_tv_amount_deposit?.text = getString(R.string.rp_) + " " + CurrencyFormat.format(amount)

        dialog?.dialog_cash_collection_btn_cancel!!.setOnClickListener { view -> dialog?.dismiss() }
        dialog?.dialog_cash_collection_btn_ok!!.setOnClickListener { view -> sentInsertC2R() }

        dialog?.show()
    }

    private fun sentInsertC2R() {
        try {
            showProgressDialog()

            extraSignature = commCode + memberCode + sourceProductType + sourceProductCode + benefProductType + benefProductCode + MyApiClient.CCY_VALUE + amount

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GLOBAL_BBS_INSERT_C2R, extraSignature)
            params[WebParams.COMM_ID] = commId
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.MEMBER_CODE] = memberCode
            params[WebParams.MEMBER_SHOP_PHONE] = customerId

            params[WebParams.SOURCE_PRODUCT_CODE] = sourceProductCode
            params[WebParams.SOURCE_PRODUCT_TYPE] = sourceProductType

            params[WebParams.BENEF_PRODUCT_CODE] = benefProductCode
            params[WebParams.BENEF_PRODUCT_TYPE] = benefProductType
            if (et_no_acct.visibility == View.GONE) {
                params[WebParams.BENEF_PRODUCT_VALUE_CODE] = accNo
                params[WebParams.BENEF_PRODUCT_VALUE_NAME] = accName
            } else {
                params[WebParams.BENEF_PRODUCT_VALUE_CODE] = et_no_acct.text.toString()
                params[WebParams.BENEF_PRODUCT_VALUE_NAME] = tv_name.text.toString()
            }
            params[WebParams.BENEF_PRODUCT_VALUE_CITY] = cityId

            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
            params[WebParams.AMOUNT] = amount
            params[WebParams.PAYMENT_REMARK] = ""
            params[WebParams.ADDITIONAL_FEE] = ""

            params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
            params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)

            Timber.d("params insert c2r : $params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GLOBAL_BBS_INSERT_C2R, params, object : ResponseListener {
                override fun onResponses(response: JsonObject?) {
                    Timber.d("response insert c2r : $response")
                    val model = getGson().fromJson<BBSTransModel>(response, BBSTransModel::class.java)

                    when (model.error_code) {
                        WebParams.SUCCESS_CODE -> {
                            txId = model.tx_id
                            productCode = model.tx_product_code
                            confirmToken()
                        }
                        WebParams.LOGOUT_CODE -> {
                            val message = model.error_message
                            val test = AlertDialogLogout.getInstance()
                            test.showDialoginMain(activity, message)
                        }
                        WebParams.ERROR_9333 -> {
                            Timber.d("isi response app data:" + model.app_data)
                            val appModel = model.app_data
                            val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                            alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                        }
                        WebParams.ERROR_0066 -> {
                            val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                            alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                        }
                        else -> {
                            val msg = model.error_message
                            showDialog(msg)
                            initlayout()
                        }
                    }

                }

                override fun onError(throwable: Throwable?) {

                }

                override fun onComplete() {
                    dismissProgressDialog()
                    dialog?.dismiss()
                }

            })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun confirmToken() {
        showProgressDialog()

        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CONFIRM_PAYMENT_DGI)

        params[WebParams.APP_ID] = BuildConfig.APP_ID
        params[WebParams.TX_ID] = txId
        params[WebParams.COMM_CODE] = commCode
        params[WebParams.USER_COMM_CODE] = sp.getString(DefineValue.COMMUNITY_CODE, "")
        params[WebParams.USER_ID] = userPhoneID
        Timber.d("params confirm payment : $params")

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CONFIRM_PAYMENT_DGI, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        try {
                            dismissProgressDialog()

                            val model = gson.fromJson(response.toString(), jsonModel::class.java)
                            val code = response.getString(WebParams.ERROR_CODE)
                            val error_message = response.getString(WebParams.ERROR_MESSAGE)
                            Timber.d("response confirm payment topup scadm : $response")
                            when (code) {
                                WebParams.SUCCESS_CODE -> sentInquiryTrx()
                                DefineValue.ERROR_9333 -> {
                                    Timber.d("isi response app data:" + model.app_data)
                                    val appModel = model.app_data
                                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                                }
                                DefineValue.ERROR_0066 -> {
                                    Timber.d("isi response maintenance:$response")
                                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                }
                                else -> Toast.makeText(activity, error_message, Toast.LENGTH_LONG).show()
                            }


                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {

                    }

                    override fun onComplete() {
                        dismissProgressDialog()
                    }
                })
    }

    private fun sentInquiryTrx() {
        try {
            showProgressDialog()

            extraSignature = txId + commCode + productCode

            params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)

            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = commIDLogin
            Timber.d("isi params InquiryTrx topup scadm:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                var code = response.getString(WebParams.ERROR_CODE)
                                Timber.d("isi response InquiryTrx topup scadm: $response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> inputPIN()
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:" + model.app_data)
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
                                        Timber.d("Error resendTokenSGOL:$response")
                                        code = response.getString(WebParams.ERROR_MESSAGE)

                                        Toast.makeText(activity, code, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                            btn_submit.isEnabled = true
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }

    }

    private fun inputPIN() {
        isPIN = true
        UtilsLoader(activity, sp).getFailedPIN(userPhoneID, object : OnLoadDataListener {
            override fun onSuccess(deData: Any) {

            }

            override fun onFail(message: Bundle) {

            }

            override fun onFailure(message: String) {

            }
        })

        if (isPIN!!) {
            callPINInput(-1)
        }
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
            if (resultCode == InsertPIN.RESULT_PIN_VALUE)
                sentInsertTransTopUp(data?.getStringExtra(DefineValue.PIN_VALUE))
        }
    }

    private fun sentInsertTransTopUp(tokenValue: String?) {
        try {
            showProgressDialog()

            extraSignature = txId + commCode + productCode + tokenValue

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_INSERT_TRANS_TOPUP, extraSignature)

            params[WebParams.TX_ID] = txId
            params[WebParams.PRODUCT_CODE] = productCode
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.COMM_ID] = commId
            params[WebParams.MEMBER_ID] = memberId
            params[WebParams.PRODUCT_VALUE] = RSA.opensslEncrypt(tokenValue)
            params[WebParams.USER_ID] = userPhoneID

            Timber.d("params insert trx : $params")
            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_INSERT_TRANS_TOPUP, params,
                    object : ResponseListener {
                        override fun onResponses(response: JsonObject) {
                            val model = getGson().fromJson(response, FailedPinModel::class.java)

                            var code = model.error_code
                            if (code == WebParams.SUCCESS_CODE) {
                                activity!!.setResult(MainPage.RESULT_BALANCE)

                                getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID)

                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == "0061") {
                                val code_msg = model.error_message
                                Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
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
                                val message = model.error_message
                                if (isPIN!!) {
                                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                                    //pin tidak sesuai errorcode 0097
                                    if (code == "0097") {
                                        val i = Intent(activity, InsertPIN::class.java)

                                        attempt = model.failed_attempt
                                        failed = model.max_failed

                                        if (attempt != -1)
                                            i.putExtra(DefineValue.ATTEMPT, failed - attempt)

                                        startActivityForResult(i, MainPage.REQUEST_FINISH)
                                    } else {
                                        activity!!.setResult(MainPage.RESULT_BALANCE)
                                        getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID)
                                        //                                    onOkButton();
                                    }
                                } else {
                                    activity!!.setResult(MainPage.RESULT_BALANCE)
                                    getTrxStatus(sp.getString(DefineValue.USER_NAME, ""), txId, userPhoneID)
                                }
                            }
                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                            btn_submit.isEnabled = true
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun getTrxStatus(userName: String?, txId: String?, userId: String?) {
        try {
            val progressDialog = DefinedDialog.CreateProgressDialog(activity, getString(R.string.check_status))
            progressDialog.show()

            extraSignature = txId + commCode
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRX_STATUS_BBS, extraSignature)
            params[WebParams.TX_ID] = txId
            params[WebParams.COMM_ID] = commId
            params[WebParams.COMM_CODE] = commCode
            params[WebParams.USER_ID] = userId

            Timber.d("isi params sent get Trx Status bbs:$params")

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_TRX_STATUS_BBS, params,
                    object : ResponseListener {
                        override fun onResponses(`object`: JsonObject) {
                            val model = getGson().fromJson(`object`, GetTrxStatusReportModel::class.java)

                            val code = model.error_code
                            if (code == WebParams.SUCCESS_CODE || code == "0003") {
                                showReportBillerDialog(userName, txId, userId, model)
                            } else if (code == "0288") {
                                val codeMsg = model.error_message
                                Toast.makeText(activity, codeMsg, Toast.LENGTH_LONG).show()
                            } else if (code == WebParams.LOGOUT_CODE) {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginActivity(activity, message)
                            } else if (code == DefineValue.ERROR_9333) {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            } else if (code == DefineValue.ERROR_0066) {
                                Timber.d("isi response maintenance:$`object`")
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                            } else {
                                val msg = model.error_message
                                showDialog(msg)
                            }

                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onComplete() {
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()
                            btn_submit.isEnabled = true
                        }
                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun showReportBillerDialog(userName: String?, txId: String?, userId: String?, response: GetTrxStatusReportModel?) {
        val args = Bundle()
        dialogReport = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, userName)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response?.created))
        args.putString(DefineValue.TX_ID, txId)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN)
        args.putString(DefineValue.USERID_PHONE, userId)
        args.putString(DefineValue.BANK_NAME, response?.tx_bank_name)
        args.putString(DefineValue.BANK_PRODUCT, response?.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.tx_amount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.additional_fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response?.total_amount))

        var txStat: Boolean? = false
        val txStatus = response?.tx_status
        when {
            txStatus == DefineValue.SUCCESS -> {
                txStat = true
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            }
            txStatus == DefineValue.ONRECONCILED -> {
                txStat = true
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            }
            txStatus == DefineValue.SUSPECT -> args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
            txStatus != DefineValue.FAILED -> args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
            else -> args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response?.tx_remark)
        args.putString(DefineValue.MEMBER_NAME, response?.member_name)
        args.putString(DefineValue.SOURCE_ACCT, response?.source_bank_name)
        args.putString(DefineValue.SOURCE_ACCT_NO, response?.source_acct_no)
        args.putString(DefineValue.SOURCE_ACCT_NAME, response?.source_acct_name)
        args.putString(DefineValue.BANK_BENEF, response?.benef_bank_name)
        args.putString(DefineValue.TYPE_BENEF, response?.benef_acct_type)
        args.putString(DefineValue.NO_BENEF, response?.benef_acct_no)
        args.putString(DefineValue.NAME_BENEF, response?.benef_acct_name)
        args.putString(DefineValue.PRODUCT_NAME, response?.product_name)
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response?.member_shop_phone)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response?.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response?.buss_scheme_name)
        args.putString(DefineValue.BENEF_PRODUCT_CODE, benefProductCode)

        dialogReport.arguments = args
        dialogReport.show(activity!!.supportFragmentManager, ReportBillerDialog.TAG)
    }

    override fun onOkButton() {
        dialogReport.dismiss()
        activity!!.finish()
    }

    private fun inputValidation(): Boolean {
        if (et_id_member.text.length == 0) {
            et_id_member.requestFocus()
            et_id_member.error = getString(R.string.no_member_validation)
            return false
        }
        return true
    }

    private fun showDialog(msg: String?) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = getString(R.string.error)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = msg

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}