package com.sgo.saldomu.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import com.google.gson.JsonObject
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CashCollectionActivity
import com.sgo.saldomu.activities.FavoriteActivity
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.dialogs.ReportBillerDialog
import com.sgo.saldomu.entityRealm.BBSBankModel
import com.sgo.saldomu.entityRealm.BBSCommModel
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.AccountsModel
import com.sgo.saldomu.models.retrofit.BBSTransModel
import com.sgo.saldomu.models.retrofit.CashCollectionModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.dialog_notification.*
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
    private var balanceCollector: String? = null
    private var cityId: String? = null
    private var txId: String? = null
    private var productCode: String? = null
    private var memberId: String? = null
    private var otp: String? = null
    private var benefProductValueCode: String? = ""
    private var benefProductValueName: String? = ""
    private var fee: String? = ""


    private var comm: BBSCommModel? = null
    private var realmBBS: Realm? = null
    private var accountList = ArrayList<AccountsModel>()
    private var accountListData = ArrayList<String>()
    private var listbankBenef: List<BBSBankModel>? = null
    private var listbankSource: List<BBSBankModel>? = null
    private lateinit var viewLayout: View
    private var dialog: Dialog? = null
    private lateinit var cashCollectionModel: CashCollectionModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
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


        if (arguments!!.containsKey(DefineValue.FAVORITE_CUSTOMER_ID) && arguments!!.getString(DefineValue.FAVORITE_CUSTOMER_ID, null) != null) {
            et_id_member.setText(arguments!!.getString(DefineValue.FAVORITE_CUSTOMER_ID))
            et_id_member.requestFocus()
        }

        if (sp.getString(DefineValue.USE_DEPOSIT_CCOL, "").equals("LIMIT"))
            getBalanceCollector()
        else if (sp.getString(DefineValue.USE_DEPOSIT_CCOL, "").equals("REG"))
        {
            val balance = sp.getString(DefineValue.BALANCE_AMOUNT, "0")
            tv_saldoCollector.setText(CurrencyFormat.format(balance))
        }
//        sourceProductType = listbankSource?.get(0)?.product_type
//        sourceProductCode = listbankSource?.get(0)?.product_code
        sourceProductType = getString(R.string.EMO)
        if (BuildConfig.FLAVOR.equals("development")) {
            sourceProductCode = getString(R.string.EMOSALDOMU)
        } else
            sourceProductCode = getString(R.string.SALDOMU)

        benefProductType = listbankBenef?.get(0)?.product_type
        benefProductCode = listbankBenef?.get(0)?.product_code

        cityId = "KOTAJAKARTA"
        initlayout()

        favorite_switch!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            notes_edit_text!!.visibility = if (isChecked) View.VISIBLE else View.GONE
            notes_edit_text.isEnabled = isChecked
        }

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
                if (spinner_no_acc.getItemAtPosition(position).toString().equals(getString(R.string.other_acct))) {
                    divider_acc.visibility = View.VISIBLE
                    et_no_acct.visibility = View.VISIBLE
                } else {
                    accNo = accountList[position].acct_no
                    accName = accountList[position].acct_name
                    et_no_acct.visibility = View.GONE
                }
            }

        }

        btn_submit.setOnClickListener {
            if (inputValidation()) {
                amount = et_amount_deposit.text.toString()
                sentInsertC2R()
            }
        }
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
//                            accountListData.add(getString(R.string.other_acct))
                            spinner_no_acc.visibility = View.VISIBLE
                            spinner_no_acc.adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_dropdown_item, accountListData)
                        } else {
                            spinner_no_acc.visibility = View.GONE
                            et_no_acct.visibility = View.VISIBLE
                        }

                        detail_cash_collection.visibility = View.VISIBLE
                        layout_acc_amount.visibility = View.VISIBLE
                        btn_submit.visibility = View.VISIBLE
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
                            benefProductValueCode = model.benef_product_value_code
                            benefProductValueName = model.benef_product_value_name
                            fee = model.admin_fee
                            confirmPayment()
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
                }

            })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun confirmPayment() {

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
                                else -> showDialog(error_message)
                            }


                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onError(throwable: Throwable) {

                    }

                    override fun onComplete() {
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
            Timber.d("isi params InquiryTrx cash collection:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                var code = response.getString(WebParams.ERROR_CODE)
                                var errormessage = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response InquiryTrx cash collection: $response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        dismissProgressDialog()
                                        val newFrag = FragCashCollectionConfirm()
                                        val bundle = Bundle()

                                        bundle.putString(DefineValue.TX_ID, txId)
                                        bundle.putString(DefineValue.COMMUNITY_ID, commId)
                                        bundle.putString(DefineValue.BENEF_PRODUCT_VALUE_CODE, benefProductValueCode)
                                        bundle.putString(DefineValue.BENEF_PRODUCT_VALUE_NAME, benefProductValueName)
                                        bundle.putString(DefineValue.COMMUNITY_CODE, commCode)
                                        bundle.putString(DefineValue.MEMBER_CODE, memberCode)
                                        bundle.putString(DefineValue.PRODUCT_CODE, productCode)
                                        bundle.putString(DefineValue.AMOUNT, amount)
                                        bundle.putString(DefineValue.FEE, fee)
                                        if (favorite_switch.isChecked()) {
                                            bundle.putBoolean(DefineValue.IS_FAVORITE, true)
                                            bundle.putString(DefineValue.CUST_ID, customerId)
                                            bundle.putString(DefineValue.NOTES, notes_edit_text.getText().toString())
                                            bundle.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.CTR)
                                            bundle.putString(DefineValue.PRODUCT_TYPE, DefineValue.CTR)
                                        }

                                        newFrag.arguments = bundle
                                        if (activity == null) {
                                            return
                                        }
                                        val ftf = activity as CashCollectionActivity?
                                        ftf!!.switchContent(newFrag, getString(R.string.cash_collection_confirmation), true)
                                    }
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
                                        Timber.d("Error inquirytrx cash collection:$response")
                                        showDialog(errormessage)
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

    private fun inputValidation(): Boolean {
        if (et_id_member.text.length == 0) {
            et_id_member.requestFocus()
            et_id_member.error = getString(R.string.no_member_validation)
            return false
        } else if (favorite_switch.isChecked() && notes_edit_text.getText().toString().length == 0) {
            notes_edit_text.requestFocus()
            notes_edit_text.setError(getString(R.string.payfriends_notes_zero))
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

    override fun onOkButton() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getBalanceCollector() {
        try {
            showProgressDialog()
            if (!memberIDLogin.isEmpty()) {
                extraSignature = memberIDLogin
                params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_SALDO_CASH_COLLECTOR, extraSignature)
                params[WebParams.MEMBER_ID] = memberIDLogin
                params[WebParams.USER_ID] = userPhoneID
                params[WebParams.COMM_ID] = MyApiClient.COMM_ID
                params[WebParams.IS_AUTO] = "Y"
                if (!memberIDLogin.isEmpty()) {
                    RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SALDO_CASH_COLLECTOR, params,
                            object : ObjListeners {
                                override fun onResponses(response: JSONObject) {
                                    try {
                                        val model = getGson().fromJson(response.toString(), jsonModel::class.java)
                                        var code = response.getString(WebParams.ERROR_CODE)
                                        if (code == WebParams.SUCCESS_CODE) {
                                            balanceCollector = response.getString(WebParams.AMOUNT)
                                            tv_saldoCollector.setText(CurrencyFormat.format(balanceCollector))
                                        } else if (code == WebParams.LOGOUT_CODE) {
                                            val message = response.getString(WebParams.ERROR_MESSAGE)
                                            val test = AlertDialogLogout.getInstance()
                                            test.showDialoginMain(activity, message)
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
                                            code = response.getString(WebParams.ERROR_MESSAGE)
                                            Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
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
                }
            }
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.menuInflater.inflate(R.menu.ab_notification, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.favorite).isVisible = true
        menu.findItem(R.id.notifications).isVisible = false
        menu.findItem(R.id.settings).isVisible = false
        menu.findItem(R.id.search).isVisible = false
        menu.findItem(R.id.cancel).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.favorite) {
            val i = Intent(activity, FavoriteActivity::class.java)
            i.putExtra(DefineValue.IS_FAV_CTR, true)
            switchActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun switchActivity(mIntent: Intent) {
        if (activity == null) return
        val fca = activity as CashCollectionActivity?
        fca!!.switchActivity(mIntent, MainPage.ACTIVITY_RESULT)
    }

}