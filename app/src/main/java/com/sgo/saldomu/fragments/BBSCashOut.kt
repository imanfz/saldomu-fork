package com.sgo.saldomu.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.CashOutHistoryModel
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.RegisterSMSBankingActivity
import com.sgo.saldomu.activities.TopUpActivity
import com.sgo.saldomu.activities.TutorialActivity
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.SMSclass.SMS_SIM_STATE
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.entityRealm.BBSAccountACTModel
import com.sgo.saldomu.entityRealm.BBSBankModel
import com.sgo.saldomu.entityRealm.BBSCommModel
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.AppDataModel
import com.sgo.saldomu.models.retrofit.BBSTransModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.utils.BbsUtil
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.bbs_cash_in_cash_out.*
import kotlinx.android.synthetic.main.dialog_notification.*
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*

class BBSCashOut : BaseFragment() {

    private val ATC = "ATC"
    private val SOURCE = "SOURCE"
    private val BENEF = "BENEF"
    private val SALDO_AGEN = "SALDO AGEN"
    private val MANDIRISMS = "MANDIRISMS"
    private val RC_READ_PHONE_STATE = 122
    private val RC_SEND_SMS = 123

    private var transaksi: String? = null
    private var type: String? = null
    private var noHpPengirim: String? = null
    private var amount: String? = null
    private var comm_id: String? = null
    private var comm_code: String? = null
    private var member_code: String? = null
    private var source_product_type: String? = null
    private var source_product_code: String? = null
    private var source_product_name: String? = null
    private var source_product_h2h: String? = null
    private var benef_product_type: String? = null
    private var benef_product_code: String? = null
    private var benef_product_name: String? = null
    private var defaultProductCode: String? = null
    private var productValue: String? = null
    private var noSource: String? = null
    private var paymentRemark: String? = null
    private var callbackURL: String? = null
    private var apiKey: String? = null
    private var lkd_product_code: String? = null
    private var cityName: String = ""

    private var isAgentLKD = false
    private var enabledAdditionalFee = false
    private var tcashValidation = false
    private var mandiriLKDValidation = false
    private var codeSuccess = false
    private var isSMSBanking = false
    private var isSimExist = false
    private var isOwner = false

    private var realm: Realm? = null
    private var realmBBS: Realm? = null

    private var dialog: Dialog? = null
    private var dialogBankList: DialogBankList? = null

    private var cashOutHistoryModel: CashOutHistoryModel? = null
    private var bbsCommModel: BBSCommModel? = null

    private var aListAgent: MutableList<HashMap<String, String>>? = null
    private var aListMember: MutableList<HashMap<String, String>>? = null

    private var listBankSource: List<BBSBankModel>? = null
    private var listBankBenef: List<BBSAccountACTModel>? = null

    private var smsClass: SMSclass? = null
    private var smsDialog: SMSDialog? = null

    var nominal = arrayOf("10000", "20000", "50000", "100000", "150000", "200000")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.bbs_cash_in_cash_out, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val bundle = arguments
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION)
            type = bundle.getString(DefineValue.TYPE, "")
            noHpPengirim = bundle.getString(DefineValue.KEY_CODE, "")
            enabledAdditionalFee = bundle.getString(DefineValue.ENABLED_ADDITIONAL_FEE) == "Y"
            noSource = bundle.getString(DefineValue.FAVORITE_CUSTOMER_ID, null)

            defaultProductCode = ""
            if (bundle.containsKey(DefineValue.PRODUCT_CODE)) {
                defaultProductCode = bundle.getString(DefineValue.PRODUCT_CODE, "")
            }

            val gson = Gson()
            val cashOut = sp.getString(DefineValue.CASH_OUT_HISTORY_TEMP, "")
            cashOutHistoryModel = gson.fromJson(cashOut, CashOutHistoryModel::class.java)
        } else {
            fragmentManager!!.popBackStack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapterNominal: ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, nominal)
        amount_transfer_edit_text.setAdapter(adapterNominal)
        amount_transfer_edit_text.threshold = 1
        amount_transfer_edit_text.setOnTouchListener { v, event ->
            amount_transfer_edit_text.showDropDown()
            false
        }
        isAgentLKD = sp.getString(DefineValue.COMPANY_TYPE, "").equals(getString(R.string.LKD), ignoreCase = true)

        aListAgent = ArrayList()
        aListMember = ArrayList()

        initializeDataBBSATC()

        if (cashOutHistoryModel != null && sp.getString(DefineValue.USERID_PHONE, "").equals(sp.getString(DefineValue.PREVIOUS_LOGIN_USER_ID, ""))) {
            amount_transfer_edit_text.setText(cashOutHistoryModel!!.amount)

            for (i in aListAgent!!.indices) {
                if (aListAgent!![i]["txt"]!!.contains(source_product_name!!)) {
                    changeSource(Integer.parseInt(aListAgent!![i]["flag"]!!),
                            cashOutHistoryModel!!.source_product_type,
                            cashOutHistoryModel!!.source_product_code,
                            cashOutHistoryModel!!.source_product_name,
                            cashOutHistoryModel!!.source_product_h2h)
                }
            }

            for (i in aListMember!!.indices) {
                if (aListMember!![i]["txt"]!!.contains(benef_product_name!!)) {
                    changeDestination(Integer.parseInt(aListMember!![i]["flag"]!!),
                            cashOutHistoryModel!!.benef_product_type,
                            cashOutHistoryModel!!.benef_product_code,
                            cashOutHistoryModel!!.benef_product_name)
                }
            }

            no_source_value.setText(cashOutHistoryModel!!.member_shop_phone)
            message_value.setText(cashOutHistoryModel!!.pesan)
        }
        no_benef_value.visibility = View.GONE
        city_benef_value.visibility = View.GONE
        if (noSource != "" && noSource != null)
            no_source_value.setText(noSource)

        if (isAgentLKD)
            no_source_value.hint = getString(R.string.number_hp_destination_hint)

        name_value.visibility = View.GONE
        no_OTP.visibility = View.GONE
        if (enabledAdditionalFee)
            layout_additionalFee.visibility = View.VISIBLE
        else
            layout_additionalFee.visibility = View.GONE

        btn_change_source.setOnClickListener { showDialogBankList(btn_change_source) }
        btn_change_destination.setOnClickListener { showDialogBankList(btn_change_destination) }

        back_btn.setOnClickListener { activity!!.finish() }
        proses_btn.setOnClickListener { if (inputValidation()) submitAction() }
        validasiTutorial()
    }

    private fun showDialogBankList(btnChange: Button) {
        if (btnChange == btn_change_source)
            dialogBankList = DialogBankList.newDialog(activity, aListMember) { position ->
                changeSource(Integer.parseInt(aListMember!![position]["flag"]!!),
                        listBankSource!![position].product_type,
                        listBankSource!![position].product_code,
                        listBankSource!![position].product_name,
                        listBankSource!![position].product_h2h)
                if (source_product_type.equals(DefineValue.EMO, ignoreCase = true) && !source_product_code.equals("MANDIRILKD", ignoreCase = true)) {
                    no_source_value.hint = getString(R.string.number_hp_destination_hint)
                } else {
                    if (source_product_code.equals("MANDIRILKD", ignoreCase = true)) {
                        no_source_value.setHint(R.string.nomor_rekening)
                    } else {
                        no_source_value.setHint(R.string.number_destination_hint)
                    }
                    no_source_value.setText("")
                }

                if (source_product_code.equals("tcash", ignoreCase = true))
                    no_OTP.visibility = View.VISIBLE
                else
                    no_OTP.visibility = View.GONE

                dialogBankList!!.dismiss()
            }
        else
            Toast.makeText(context, "Benef", Toast.LENGTH_SHORT).show()

        dialogBankList!!.show(fragManager, "")
    }

    private fun initializeDataBBSATC() {
        bbsCommModel = realmBBS!!.where(BBSCommModel::class.java)
                .equalTo(WebParams.SCHEME_CODE, ATC).findFirst()
        listBankBenef = realmBBS!!.where(BBSAccountACTModel::class.java).findAll()

        if (isAgentLKD) {
            defaultProductCode = if (BuildConfig.FLAVOR.equals("development", ignoreCase = true))
                "EMO SALDOMU"
            else
                getString(R.string.SALDOMU)
            btn_change_source.visibility = View.GONE
            listBankSource = realmBBS!!.where(BBSBankModel::class.java)
                    .equalTo(WebParams.SCHEME_CODE, ATC)
                    .equalTo(WebParams.COMM_TYPE, SOURCE)
                    .equalTo(WebParams.PRODUCT_NAME, defaultProductCode).findAll()
        } else {
            listBankSource = realmBBS!!.where(BBSBankModel::class.java)
                    .equalTo(WebParams.SCHEME_CODE, ATC)
                    .equalTo(WebParams.COMM_TYPE, SOURCE).findAll()
        }
        setMember(listBankSource)
        setAgent(listBankBenef)
        if (bbsCommModel == null) {
            Toast.makeText(activity, getString(R.string.bbstransaction_toast_not_registered, getString(R.string.cash_out)), Toast.LENGTH_LONG).show()
            val isUpdatingData = sp.getBoolean(DefineValue.IS_UPDATING_BBS_DATA, false)
            if (!isUpdatingData) checkAndRunServiceBBS()
        }
    }

    private fun setMember(bankMember: List<BBSBankModel>?) {
        aListMember!!.clear()
        aListMember!!.addAll(BbsUtil.mappingProductCodeIcons(bankMember))
        for (i in bankMember!!.indices) {
            if (bankMember[i].product_name.toLowerCase(Locale.getDefault()).contains("saldomu")) {
                changeSource(Integer.parseInt(aListMember!![i]["flag"]!!), bankMember[i].product_type, bankMember[i].product_code, bankMember[i].product_name, bankMember[i].product_h2h)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setAgent(bankAgen: List<BBSAccountACTModel>?) {
        aListAgent!!.clear()
        for (i in bankAgen!!.indices) {
            val hm = HashMap<String, String>()
            if (bankAgen[i].product_name.toLowerCase().contains("saldomu"))
                hm["txt"] = SALDO_AGEN
            else
                hm["txt"] = bankAgen[i].product_name
            if (bankAgen[i].product_name.toLowerCase().contains("mandiri"))
                hm["flag"] = R.drawable.logo_mandiri_bank_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("bri"))
                hm["flag"] = R.drawable.logo_bank_bri_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("permata"))
                hm["flag"] = R.drawable.logo_bank_permata_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("uob"))
                hm["flag"] = R.drawable.logo_bank_uob_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("maspion"))
                hm["flag"] = R.drawable.logo_bank_maspion_rev1_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("bii"))
                hm["flag"] = R.drawable.logo_bank_bii_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("jatim"))
                hm["flag"] = R.drawable.logo_bank_jatim_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("bca"))
                hm["flag"] = R.drawable.logo_bca_bank_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("nobu"))
                hm["flag"] = R.drawable.logo_bank_nobu.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("saldomu"))
                hm["flag"] = R.drawable.logo_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("linkaja"))
                hm["flag"] = R.drawable.linkaja.toString()
            else if (bankAgen[i].product_code.toLowerCase().contains("emoedikk"))
                hm["flag"] = R.drawable.dana_small.toString()
            else if (bankAgen[i].product_code.toLowerCase().contains("009"))
                hm["flag"] = R.drawable.logo_bank_bni_small.toString()
            else if (bankAgen[i].product_name.toLowerCase().contains("akardaya"))
                hm["flag"] = R.drawable.mad_small.toString()
            else
                hm["flag"] = R.drawable.ic_square_gate_one.toString()
            aListAgent!!.add(hm)
        }
        if (aListAgent!!.size == 1) {
            val position = 0
            btn_change_destination.visibility = View.GONE
            changeDestination(Integer.parseInt(aListAgent!![position]["flag"]!!),
                    bankAgen[position].product_type,
                    bankAgen[position].product_code,
                    bankAgen[position].product_name)
        }
    }

    private fun changeSource(id: Int, productType: String, productCode: String, productName: String, productH2h: String) {
        source_product_type = productType
        source_product_code = productCode
        source_product_name = productName
        source_product_h2h = productH2h
        tv_transfer_source.text = source_product_name
        iv_transfer_source.setImageResource(id)
    }

    private fun changeDestination(id: Int, productType: String, productCode: String, productName: String) {
        benef_product_type = productType
        benef_product_code = productCode
        benef_product_name = productName
        if (benef_product_name!!.toLowerCase(Locale.getDefault()).contains("saldomu")) benef_product_name = SALDO_AGEN
        tv_transfer_destination.text = benef_product_name
        iv_transfer_destination.setImageResource(id)
    }

    private fun inputValidation(): Boolean {
        if (amount_transfer_edit_text.text.isEmpty()) {
            amount_transfer_edit_text.requestFocus()
            amount_transfer_edit_text.error = getString(R.string.payfriends_amount_validation)
            return false
        }
        if (no_source_value.text.isEmpty()) {
            no_source_value.requestFocus()
            no_source_value.error = getString(R.string.forgetpass_edittext_validation)
            return false
        }
        if (no_OTP.visibility == View.VISIBLE) {
            if (no_OTP.text.isEmpty()) {
                no_OTP.requestFocus()
                no_OTP.error = getString(R.string.regist3_validation_otp)
                return false
            }
        }

        return true
    }

    private fun submitAction() {
        if (inputValidation()) {
            sentInsertA2C()
        }
    }

    private fun sentInsertA2C() {
        showProgressDialog()
        isSMSBanking = source_product_code.equals(MANDIRISMS, ignoreCase = true)
        comm_id = bbsCommModel!!.comm_id
        comm_code = bbsCommModel!!.comm_code
        member_code = bbsCommModel!!.member_code
        callbackURL = bbsCommModel!!.callback_url
        apiKey = bbsCommModel!!.api_key
        amount = amount_transfer_edit_text.text.toString()
        noSource = no_source_value.text.toString()

        paymentRemark = message_value.text.toString()
        extraSignature = comm_code + member_code + source_product_type + source_product_code + benef_product_type + benef_product_code + MyApiClient.CCY_VALUE + amount

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GLOBAL_BBS_INSERT_A2C, extraSignature)

        params[WebParams.COMM_ID] = comm_id
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_CODE] = comm_code
        params[WebParams.MEMBER_CODE] = member_code
        params[WebParams.SOURCE_PRODUCT_CODE] = source_product_code!!
        params[WebParams.SOURCE_PRODUCT_TYPE] = source_product_type!!
        params[WebParams.SOURCE_PRODUCT_VALUE] = noSource
        params[WebParams.BENEF_PRODUCT_CODE] = benef_product_code!!
        params[WebParams.BENEF_PRODUCT_TYPE] = benef_product_type!!
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.AMOUNT] = amount
        params[WebParams.PAYMENT_REMARK] = paymentRemark
        params[WebParams.USER_COMM_CODE] = BuildConfig.COMM_CODE_BBS_ATC
        params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
        params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)

        if (enabledAdditionalFee) {
            params[WebParams.ADDITIONAL_FEE] = et_additionalFee.text.toString()
        }

        Timber.d("params insert a2c $params")
        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GLOBAL_BBS_INSERT_A2C, params, object : ResponseListener {

            override fun onResponses(response: JsonObject?) {
                val model: BBSTransModel = getGson().fromJson<BBSTransModel>(response, BBSTransModel::class.java)
                val code = model.error_code
                val message = model.error_message
                if (code == WebParams.SUCCESS_CODE || code == "0282") {
                    val prefs = CustomSecurePref.getInstance().getmSecurePrefs()
                    val mEditor = prefs.edit()
                    mEditor.remove(DefineValue.AOD_TX_ID)
                    mEditor.apply()

                    if (code == "0282") {
                        if (source_product_code.equals("tcash", ignoreCase = true))
                            tcashValidation = true
                        else
                            mandiriLKDValidation = true
                    } else codeSuccess = true

                    if (isSMSBanking) {
                        if (smsDialog == null) {
                            smsDialog = SMSDialog()
                        }
                        smsDialog!!.setListener(object : SMSDialog.DialogButtonListener {
                            override fun onClickOkButton(v: View, isLongClick: Boolean) {
                                if (EasyPermissions.hasPermissions(activity!!, Manifest.permission.CAMERA)) {
                                    smsDialog!!.sentSms()
                                    regSimCardReceiver(true)
                                } else {
                                    EasyPermissions.requestPermissions(this@BBSCashOut, getString(R.string.rationale_send_sms),
                                            RC_SEND_SMS, Manifest.permission.CAMERA)
                                }
                            }

                            override fun onClickCancelButton(v: View, isLongClick: Boolean) {
                                dismissProgressDialog()
                            }

                            override fun onSuccess(user_is_new: Int) {}
                            override fun onSuccess(product_value: String) {
                                productValue = product_value
                                smsDialog!!.dismiss()
                                smsDialog!!.reset()
                                sentDataReqToken(model)
                            }
                        })
                        if (isSimExist) smsDialog!!.show(fragmentManager, "")
                    } else if (source_product_h2h.equals("Y", ignoreCase = true) && source_product_type.equals(DefineValue.EMO, ignoreCase = true)) {
                        if (code == WebParams.SUCCESS_CODE && !source_product_code.equals("tcash", ignoreCase = true)
                                && !source_product_code.equals("MANDIRILKD", ignoreCase = true)) {
                            sentDataReqToken(model)
                        } else {
                            sentDataReqToken(model)
                        }
                    } else {
                        sentDataReqToken(model)
                        isOwner = true
                    }
                } else if ((code == "0295")) {
                    showDialogLimit(message)
                } else if ((code == "0296")) {
                    lkd_product_code = model.lkd_product_code
                    dialogJoinLKD(message)
                } else if ((code == WebParams.LOGOUT_CODE)) {
                    val test = AlertDialogLogout.getInstance()
                    test.showDialoginActivity(activity, message)
                } else if ((code == DefineValue.ERROR_9333)) {
                    Timber.d("isi response app data:" + model.app_data)
                    val appModel: AppDataModel = model.app_data
                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                } else if ((code == DefineValue.ERROR_0066)) {
                    Timber.d("isi response maintenance:" + response.toString())
                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                    alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                } else {
                    val code_msg: String = model.error_message
                    Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
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

    fun showDialogLimit(message: String) {
        dialog = DefinedDialog.MessageDialog(activity, this.getString(R.string.limit_dialog_title),
                message
        ) { v, isLongClick -> dialog!!.dismiss() }
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    fun dialogJoinLKD(message: String) {
        val builder1 = android.support.v7.app.AlertDialog.Builder(activity!!)
        builder1.setTitle(R.string.join_lkd)
        builder1.setMessage(message)
        builder1.setCancelable(true)
        builder1.setPositiveButton(
                "Yes"
        ) { dialog, id -> joinMemberLKD() }
        builder1.setNegativeButton(
                "No"
        ) { dialog, id -> activity!!.finish() }
        val alert11 = builder1.create()
        alert11.show()
    }

    private fun joinMemberLKD() {
        try {
            showProgressDialog()
            extraSignature = memberIDLogin + lkd_product_code
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_MANDIRI_LKD, extraSignature)
            params[WebParams.COMM_ID] = comm_id!!
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_ID] = memberIDLogin
            params[WebParams.PRODUCT_CODE] = lkd_product_code!!
            Timber.d("params send data member mandiri LKD:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_MANDIRI_LKD, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                Timber.d("isi response sent data member mandiri lkd:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        sentInsertA2C()
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
                                        Timber.d("isi error send data member mandiri LKD:$response")
                                        val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {}
                        override fun onComplete() {
                            proses_btn.isEnabled = true
                            showProgressDialog()
                        }
                    })
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    private fun regSimCardReceiver(isReg: Boolean) {
        if (isSMSBanking) {
            if (isReg) {
                try {
                    activity!!.unregisterReceiver(customSimcardListener)
                } catch (ignored: Exception) {
                }
                activity!!.registerReceiver(customSimcardListener, SMSclass.simStateIntentFilter)
            } else {
                try {
                    activity!!.unregisterReceiver(customSimcardListener)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private val customSimcardListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action.equals("android.intent.action.SIM_STATE_CHANGED", ignoreCase = true)) {
                if (intent.getStringExtra("ss").equals("ABSENT", ignoreCase = true)) {
                    if (smsDialog != null) { //                    if (smsDialog != null && smsDialog.isShowing()) {
                        Toast.makeText(activity, R.string.smsclass_simcard_listener_absent_toast, Toast.LENGTH_LONG).show()
                        smsDialog!!.dismiss()
                        smsDialog!!.reset()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    fun onPermissionsGranted(requestCode: Int, perms: List<String?>?) {
        if (requestCode == RC_READ_PHONE_STATE) {
            initializeSmsClass()
            if (isSimExist) submitAction()
        } else if (requestCode == RC_SEND_SMS) {
            smsDialog!!.sentSms()
        }
    }

    fun onPermissionsDenied(requestCode: Int, perms: List<String?>?) {
        Toast.makeText(activity, getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show()
        if (requestCode == RC_SEND_SMS) {
            dismissProgressDialog()
            if (smsDialog != null) {
                smsDialog!!.dismiss()
                smsDialog!!.reset()
            }
        }
    }

    private fun initializeSmsClass() {
        if (smsClass == null) smsClass = SMSclass(activity, customSimcardListener)
        smsClass!!.isSimExists(SMS_SIM_STATE { isExist, msg ->
            if (!isExist) {
                isSimExist = false
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
                val alertDialog = builder.create()
                alertDialog.show()
            } else isSimExist = true
        })
    }

    fun sentDataReqToken(bbsTransModel: BBSTransModel?) {
        showProgressDialog()
        if (bbsTransModel != null) {
            extraSignature = bbsTransModel.tx_id + comm_code + bbsTransModel.tx_product_code

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REQ_TOKEN_SGOL, extraSignature)

            params[WebParams.COMM_CODE] = comm_code!!
            params[WebParams.TX_ID] = bbsTransModel.tx_id
            params[WebParams.PRODUCT_CODE] = bbsTransModel.tx_product_code
            if (source_product_code.equals("tcash", ignoreCase = true) || source_product_code.equals("MANDIRILKD", ignoreCase = true)) params[WebParams.PRODUCT_VALUE] = ""
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_ID] = comm_id!!

            if (isSMSBanking) params[WebParams.PRODUCT_VALUE] = productValue!!

            Timber.d("isi params regtoken Sgo+: $params")
            RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_REQ_TOKEN_SGOL, params, object : ResponseListener {

                override fun onResponses(response: JsonObject?) {
                    val model: BBSTransModel = getGson().fromJson<BBSTransModel>(response, BBSTransModel::class.java)
                    var code = model.error_code

                    if (code == WebParams.SUCCESS_CODE) {
                        changeToConfirm(bbsTransModel)
                    } else {
                        val codeMsg = model.error_code
                        if (code == "0059" || code == "0164") {
                            showDialogErrorSMS(model.tx_bank_name, code, model.error_message)
                        } else if (code == "0057") {
                            if (transaksi.equals(getString(R.string.cash_out), ignoreCase = true)) {
                                val builder = AlertDialog.Builder(activity)
                                builder.setTitle("Alert")
                                        .setMessage(getString(R.string.member_saldo_not_enough))
                                        .setPositiveButton("OK") { dialog, which -> activity!!.finish() }
                                val dialog = builder.create()
                                dialog.show()
                            } else {
                                val messageDialog = "\"" + "\" \n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname))
                                val dialogFrag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                        messageDialog, getString(R.string.ok), getString(R.string.cancel), false)
                                dialogFrag.okListener = DialogInterface.OnClickListener { dialog, which ->
                                    val mI = Intent(activity, TopUpActivity::class.java)
                                    mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true)
                                    activity!!.startActivityForResult(mI, MainPage.ACTIVITY_RESULT)
                                }
                                dialogFrag.setTargetFragment(this@BBSCashOut, 0)
                                dialogFrag.show(fragmentManager, AlertDialogFrag.TAG)
                            }
                        } else {
                            code = model.error_code + " : " + model.error_message
                            Toast.makeText(activity, code, Toast.LENGTH_LONG).show()
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

    @SuppressLint("SetTextI18n")
    private fun showDialog(model: BBSTransModel) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        title_dialog.text = resources.getString(R.string.regist1_notif_title_verification)
        message_dialog.visibility = View.VISIBLE
        message_dialog.text = getString(R.string.appname) + " " + getString(R.string.dialog_token_message_sms)
        btn_dialog_notification_ok.setOnClickListener {
            changeToConfirm(model)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun changeToConfirm(model: BBSTransModel) {
        val mArgs = Bundle()

        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h)
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type)
        mArgs.putString(DefineValue.PRODUCT_CODE, model.tx_product_code)
        mArgs.putString(DefineValue.BANK_CODE, model.tx_bank_code)
        mArgs.putString(DefineValue.BANK_NAME, model.tx_bank_name)
        mArgs.putString(DefineValue.PRODUCT_NAME, model.tx_product_name)
        mArgs.putString(DefineValue.FEE, model.admin_fee)
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code)
        mArgs.putString(DefineValue.TX_ID, model.tx_id)
        mArgs.putString(DefineValue.AMOUNT, model.amount)
        mArgs.putString(DefineValue.TOTAL_AMOUNT, model.total_amount)
        mArgs.putString(DefineValue.SHARE_TYPE, "1")
        mArgs.putString(DefineValue.CALLBACK_URL, callbackURL)
        mArgs.putString(DefineValue.API_KEY, apiKey)
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id)
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name)
        mArgs.putString(DefineValue.NAME_BENEF, model.benef_product_value_name)
        mArgs.putString(DefineValue.NO_BENEF, model.benef_product_value_code)
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type)
        mArgs.putString(DefineValue.REMARK, paymentRemark)
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name)
        mArgs.putString(DefineValue.MAX_RESEND, model.max_resend_token)
        mArgs.putString(DefineValue.TRANSACTION, transaksi)
        mArgs.putString(DefineValue.BENEF_BANK_CODE, benef_product_code)
        mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, tcashValidation)
        mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, mandiriLKDValidation)
        mArgs.putBoolean(DefineValue.CODE_SUCCESS, codeSuccess)
        mArgs.putString(DefineValue.USER_ID, noSource)
        proses_btn.isEnabled = true
        cashOutHistory()
        val mFrag: Fragment = BBSCashOutConfirm()
        mFrag.arguments = mArgs
        fragmentManager!!.beginTransaction().addToBackStack(BBSTransaksiInformasi.TAG)
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, BBSCashOutConfirm.TAG).commit()
        ToggleKeyboard.hide_keyboard(activity)
    }

    private fun changeToDataMandiriLKD(_tx_id: String, _product_code: String, _product_name: String, _bank_code: String,
                                       _amount: String, fee: String, totalAmount: String, _bank_name: String, _max_resend_token: String,
                                       _benef_acct_no: String, _benef_acct_name: String, no_benef: String, isOwner: Boolean) {
        val mArgs = Bundle()
        if (benef_product_type.equals(DefineValue.ACCT, ignoreCase = true)) {
            mArgs.putString(DefineValue.BENEF_CITY, cityName)
        }
        mArgs.putString(DefineValue.PRODUCT_H2H, source_product_h2h)
        mArgs.putString(DefineValue.PRODUCT_TYPE, source_product_type)
        mArgs.putString(DefineValue.PRODUCT_CODE, _product_code)
        mArgs.putString(DefineValue.BANK_CODE, _bank_code)
        mArgs.putString(DefineValue.BANK_NAME, _bank_name)
        mArgs.putString(DefineValue.PRODUCT_NAME, _product_name)
        mArgs.putString(DefineValue.FEE, fee)
        mArgs.putString(DefineValue.COMMUNITY_CODE, comm_code)
        mArgs.putString(DefineValue.TX_ID, _tx_id)
        mArgs.putString(DefineValue.AMOUNT, _amount)
        mArgs.putString(DefineValue.TOTAL_AMOUNT, totalAmount)
        mArgs.putString(DefineValue.SHARE_TYPE, "1")
        mArgs.putString(DefineValue.CALLBACK_URL, callbackURL)
        mArgs.putString(DefineValue.API_KEY, apiKey)
        mArgs.putString(DefineValue.COMMUNITY_ID, comm_id)
        mArgs.putString(DefineValue.BANK_BENEF, benef_product_name)
        mArgs.putString(DefineValue.NAME_BENEF, _benef_acct_name)
        mArgs.putString(DefineValue.NO_BENEF, no_benef)
        mArgs.putString(DefineValue.TYPE_BENEF, benef_product_type)
        mArgs.putString(DefineValue.REMARK, paymentRemark)
        mArgs.putString(DefineValue.SOURCE_ACCT, source_product_name)
        mArgs.putString(DefineValue.MAX_RESEND, _max_resend_token)
        mArgs.putString(DefineValue.TRANSACTION, transaksi)
        mArgs.putString(DefineValue.BENEF_PRODUCT_CODE, benef_product_code)
        mArgs.putBoolean(DefineValue.IS_OWNER, isOwner)
        mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, tcashValidation)
        mArgs.putBoolean(DefineValue.CODE_SUCCESS, codeSuccess)
        proses_btn.isEnabled = true
        cashOutHistory()
        val mFrag: Fragment = FragDataC2A()
        mFrag.arguments = mArgs
        fragmentManager!!.beginTransaction().addToBackStack(BBSTransaksiInformasi.TAG)
                .replace(R.id.bbsTransaksiFragmentContent, mFrag, FragDataC2A.TAG).commit()
        ToggleKeyboard.hide_keyboard(activity)
    }

    private fun cashOutHistory() {
        if (cashOutHistoryModel == null) {
            cashOutHistoryModel = CashOutHistoryModel()
        }
        cashOutHistoryModel!!.amount = amount
        cashOutHistoryModel!!.benef_product_code = benef_product_code
        cashOutHistoryModel!!.benef_product_name = benef_product_name
        cashOutHistoryModel!!.benef_product_type = benef_product_type
        cashOutHistoryModel!!.source_product_code = source_product_code
        cashOutHistoryModel!!.source_product_name = source_product_name
        cashOutHistoryModel!!.source_product_type = source_product_type
        cashOutHistoryModel!!.source_product_h2h = source_product_h2h
        cashOutHistoryModel!!.member_shop_phone = noSource
        cashOutHistoryModel!!.pesan = message_value.text.toString()

        val gson = Gson()
        val jsonObject = gson.toJson(cashOutHistoryModel, CashOutHistoryModel::class.java)
        val editor = sp.edit()
        editor.putString(DefineValue.CASH_OUT_HISTORY_TEMP, jsonObject)
        editor.apply()
    }

    fun dialogBenefLKD(_tx_id: String?, _product_code: String?, _product_name: String?, _bank_code: String?,
                       _amount: String?, _fee: String?, _totalAmount: String?, _bank_name: String?, _max_resend_token: String?,
                       _benef_acct_no: String?, _benef_acct_name: String?, no_benef: String?) {
        val builder1 = android.support.v7.app.AlertDialog.Builder(activity!!)
        builder1.setTitle(R.string.c2a_lkd)
        builder1.setMessage("Transfer ke : ")
        builder1.setCancelable(true)
        builder1.setPositiveButton(
                "Diri Sendiri"
        ) { dialog, id ->
            isOwner = true
            changeToDataMandiriLKD(_tx_id!!, _product_code!!, _product_name!!, _bank_code!!,
                    _amount!!, _fee!!, _totalAmount!!, _bank_name!!, _max_resend_token!!,
                    _benef_acct_no!!, _benef_acct_name!!, no_benef!!, isOwner)
        }
        builder1.setNegativeButton(
                "Orang Lain"
        ) { dialog, id ->
            isOwner = false
            changeToDataMandiriLKD(_tx_id!!, _product_code!!, _product_name!!, _bank_code!!,
                    _amount!!, _fee!!, _totalAmount!!, _bank_name!!, _max_resend_token!!,
                    _benef_acct_no!!, _benef_acct_name!!, no_benef!!, isOwner)
        }
        val alert11 = builder1.create()
        alert11.show()
    }

    fun showDialogErrorSMS(nama_bank: String?, error_code: String, error_msg: String?) { // Create custom dialog object
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification)
        // set values for custom dialog components - text, image and button
        message_dialog.visibility = View.VISIBLE
        title_dialog.text = getString(R.string.topup_dialog_not_registered)
        if (error_code == "0059") {
            message_dialog.text = error_msg
            btn_dialog_notification_ok.text = getString(R.string.firstscreen_button_daftar)
            btn_dialog_notification_ok.setOnClickListener {
                val newIntent = Intent(activity, RegisterSMSBankingActivity::class.java)
                newIntent.putExtra(DefineValue.BANK_NAME, nama_bank)
                startActivity(newIntent)
                dialog.dismiss()
            }
        } else if (error_code == "0164") {
            message_dialog.text = error_msg
            btn_dialog_notification_ok.text = getString(R.string.close)
            btn_dialog_notification_ok.setOnClickListener {
                dialog.dismiss()
                activity!!.finish()
            }
        }
        dialog.show()
    }

    private fun checkAndRunServiceBBS() {
        val bbsDataManager = BBSDataManager()
        if (!bbsDataManager.isDataUpdated) {
            bbsDataManager.runServiceUpdateData(context)
            Timber.d("Run Service update data BBS")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_information -> {
                if (transaksi.equals(getString(R.string.cash_out), ignoreCase = true))
                    showTutorialCashOut()
                return true
            }
            android.R.id.home -> {
                activity!!.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validasiTutorial() {
        if (sp.contains(DefineValue.TUTORIAL_CASHOUT)) {
            val isFirstTime = sp.getBoolean(DefineValue.TUTORIAL_CASHOUT, false)
            if (isFirstTime) showTutorialCashOut()
        } else
            showTutorialCashOut()
    }

    private fun showTutorialCashOut() {
        val intent = Intent(activity, TutorialActivity::class.java)
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_cash_out)
        startActivity(intent)
    }

    override fun onDestroy() {
        RealmManager.closeRealm(realm)
        RealmManager.closeRealm(realmBBS)
        super.onDestroy()
    }
}