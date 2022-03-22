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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sgo.saldomu.Beans.TransferFundHistoryModel
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.MainPage
import com.sgo.saldomu.activities.RegisterSMSBankingActivity
import com.sgo.saldomu.activities.TopUpActivity
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.databinding.FragmentBBSTransferFundBinding
import com.sgo.saldomu.dialogs.*
import com.sgo.saldomu.entityRealm.BBSBankModel
import com.sgo.saldomu.entityRealm.BBSCommModel
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.AppDataModel
import com.sgo.saldomu.models.retrofit.BBSTransModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.utils.BbsUtil
import com.sgo.saldomu.utils.NumberTextWatcherForThousand
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.bbs_cash_in_cash_out.*
import kotlinx.android.synthetic.main.dialog_notification.*
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*

class BBSTransferFund : BaseFragment() {

    private var _binding: FragmentBBSTransferFundBinding? = null
    private val binding get() = _binding!!

    private val TFD = "TFD"
    private val SOURCE = "SOURCE"
    private val BENEF = "BENEF"
    private val SALDO_AGEN = "Saldo Agen"
    private val MANDIRISMS = "MANDIRISMS"
    private val RC_READ_PHONE_STATE = 122
    private val RC_SEND_SMS = 123

    private var transaksi: String? = null
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
    private var noBenef: String? = null
    private var nameBenef: String? = null
    private var paymentRemark: String? = null
    private var callbackURL: String? = null
    private var apiKey: String? = null
    private var lkd_product_code: String? = null
    private var cityId: String = ""
    private var cityName: String = ""
    private var defaultAmount: String = ""

    private var isAgentLKD = false
    private var tcashValidation = false
    private var mandiriLKDValidation = false
    private var codeSuccess = false
    private var isSMSBanking = false
    private var isSimExist = false
    private var isOwner = false
    private var transferFundMandiriLP = false

    private var realm: Realm? = null
    private var realmBBS: Realm? = null

    private var dialog: Dialog? = null
    private var dialogBankList: DialogBankList? = null

    private var transferFundHistoryModel: TransferFundHistoryModel? = null
    private var bbsCommModel: BBSCommModel? = null

    private var aListAgent: MutableList<HashMap<String, String>>? = null
    private var aListMember: MutableList<HashMap<String, String>>? = null

    private var listBankSource: List<BBSBankModel>? = null
    private var listBankBenef: List<BBSBankModel>? = null

    private var smsClass: SMSclass? = null
    private var smsDialog: SMSDialog? = null

    var nominal = arrayOf("10000", "20000", "50000", "100000", "150000", "200000")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentBBSTransferFundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        val bundle = arguments
        if (bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION)
            defaultAmount = bundle.getString(DefineValue.AMOUNT, "")
            noHpPengirim = bundle.getString(DefineValue.KEY_CODE, "")
            noBenef = bundle.getString(DefineValue.FAVORITE_CUSTOMER_ID, "")

            defaultProductCode = ""
            if (bundle.containsKey(DefineValue.PRODUCT_CODE)) {
                defaultProductCode = bundle.getString(DefineValue.PRODUCT_CODE, "")
            }

            if (bundle.containsKey(DefineValue.TX_MANDIRI_LP)) {
                transferFundMandiriLP = bundle.getBoolean(DefineValue.TX_MANDIRI_LP, false)
            }

            val gson = Gson()
            val cashIn = sp.getString(DefineValue.TRANSFER_FUND_HISTORY_TEMP, "")
            transferFundHistoryModel = gson.fromJson(cashIn, TransferFundHistoryModel::class.java)

        } else {
            fragmentManager!!.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val adapterNominal: ArrayAdapter<String> =
                ArrayAdapter(context!!, android.R.layout.simple_list_item_1, nominal)
            amountTransferEditText.setAdapter(adapterNominal)
            amountTransferEditText.threshold = 1
            amountTransferEditText.addTextChangedListener(
                NumberTextWatcherForThousand(
                    amountTransferEditText
                )
            )

            amountTransferEditText.setOnTouchListener { _, _ ->
                amountTransferEditText.showDropDown()
                false
            }
            isAgentLKD = sp.getString(DefineValue.COMPANY_TYPE, "")
                .equals(getString(R.string.LKD), ignoreCase = true)

            aListAgent = ArrayList()
            aListMember = ArrayList()

            noSourceValue.visibility = View.GONE
            noOTP.visibility = View.GONE
            noBenefValue.hint = getString(R.string.number_destination_hint)

            initializeDataBBSTFD()

            if (transferFundHistoryModel != null) {
                amountTransferEditText.setText(transferFundHistoryModel!!.getAmount())

                for (i in aListAgent!!.indices) {
                    if (aListAgent!![i]["txt"]!!.contains(source_product_name!!)) {
                        changeSource(
                            Integer.parseInt(aListAgent!![i]["flag"]!!),
                            transferFundHistoryModel!!.getSource_product_type(),
                            transferFundHistoryModel!!.getSource_product_code(),
                            transferFundHistoryModel!!.getSource_product_name(),
                            transferFundHistoryModel!!.getSource_product_h2h()
                        )
                    }
                }

                for (i in aListMember!!.indices) {
                    if (aListMember!![i]["txt"]!!.contains(benef_product_name!!)) {
                        changeDestination(
                            Integer.parseInt(aListMember!![i]["flag"]!!),
                            transferFundHistoryModel!!.getBenef_product_type(),
                            transferFundHistoryModel!!.getBenef_product_code(),
                            transferFundHistoryModel!!.getBenef_product_name()
                        )
                    }
                }

                noSourceValue.setText(transferFundHistoryModel!!.getBank_account_destinaiton())
                messageValue.setText(transferFundHistoryModel!!.getPesan())
            }

            if (transferFundMandiriLP) {
                btnChangeSource.visibility = View.GONE
                for (i in aListMember!!.indices) {
                    if (aListMember!![i]["txt"]!!.contains(
                            "Mandiri Laku Pandai",
                            ignoreCase = true
                        )
                    )
                        changeSource(
                            Integer.parseInt(aListMember!![i]["flag"]!!),
                            listBankSource!![i].product_type,
                            listBankSource!![i].product_code,
                            listBankSource!![i].product_name,
                            listBankSource!![i].product_h2h
                        )
                }
            }

            if (isAgentLKD)
                noSourceValue.hint = getString(R.string.number_hp_destination_hint)

            btnChangeSource.setOnClickListener { showDialogBankList(btnChangeSource) }
            btnChangeDestination.setOnClickListener { showDialogBankList(btnChangeDestination) }

            backBtn.setOnClickListener { activity!!.finish() }
            prosesBtn.setOnClickListener { submitAction() }
        }
    }

    private fun changeSource(id: Int, productType: String, productCode: String, productName: String, productH2h: String) {
        source_product_type = productType
        source_product_code = productCode
        source_product_name = productName
        source_product_h2h = productH2h
        if (source_product_name!!.lowercase(Locale.getDefault()).contains("saldomu")) source_product_name = SALDO_AGEN
        binding.tvTransferSource.text = source_product_name
        binding.ivTransferSource.setImageResource(id)
    }

    private fun showDialogBankList(btnChange: Button) {
        if (btnChange == binding.btnChangeSource)
            dialogBankList = DialogBankList.newDialog(activity, aListMember) { position ->
                changeSource(
                    Integer.parseInt(aListMember!![position]["flag"]!!),
                    listBankSource!![position].product_type,
                    listBankSource!![position].product_code,
                    listBankSource!![position].product_name,
                    listBankSource!![position].product_h2h
                )
                dialogBankList!!.dismiss()
            }
        else
            dialogBankList = DialogBankList.newDialog(activity, aListAgent) { position ->
                changeDestination(
                    Integer.parseInt(aListAgent!![position]["flag"]!!),
                    listBankBenef!![position].product_type,
                    listBankBenef!![position].product_code,
                    listBankBenef!![position].product_name
                )
                dialogBankList!!.dismiss()
            }
        dialogBankList!!.show(fragManager, "")
    }

    private fun changeDestination(id: Int, productType: String, productCode: String, productName: String) {
        benef_product_type = productType
        benef_product_code = productCode
        benef_product_name = productName
        binding.tvTransferDestination.text = benef_product_name
        binding.ivTransferDestination.setImageResource(id)

        if (benef_product_type.equals(DefineValue.EMO, ignoreCase = true) && !benef_product_code.equals("MANDIRILKD", ignoreCase = true)) {
            no_benef_value.hint = getString(R.string.number_hp_destination_hint)
        } else {
            if (benef_product_code.equals("MANDIRILKD", ignoreCase = true)) {
                no_benef_value.setHint(R.string.nomor_rekening)
            } else {
                no_benef_value.setHint(R.string.number_destination_hint)
            }
            no_benef_value.setText("")
        }

//        if (benef_product_code.equals("linkaja", ignoreCase = true))
//            name_value.visibility = View.VISIBLE
//        else
//            name_value.visibility = View.GONE

        if (benef_product_code.equals("tcash", ignoreCase = true))
            no_OTP.visibility = View.VISIBLE
        else
            no_OTP.visibility = View.GONE
    }

    private fun inputValidation(): Boolean {
        binding.apply {
            amountTransferEditText.error = null
            noBenefValue.error = null
            nameValue.error = null
            if (amountTransferEditText.text.isEmpty()) {
                amountTransferEditText.requestFocus()
                amountTransferEditText.error = getString(R.string.payfriends_amount_validation)
                return false
            }
            if (noBenefValue.text?.isEmpty() == true || noBenefValue.text?.length!! < 10) {
                noBenefValue.requestFocus()
                noBenefValue.error = getString(R.string.cashout_accno_validation)
                return false
            }
            if (nameValue.text?.isEmpty() == true || nameValue.text?.length!! < 4) {
                nameValue.requestFocus()
                nameValue.error = getString(R.string.cashout_accname_validation)
                return false
            }
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
            sentInsertTFD()
        }
    }

    fun showDialogLimit(message: String) {
        dialog = DefinedDialog.MessageDialog(requireContext(), this.getString(R.string.error),
            message
        ) { dialog?.dismiss() }
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        dialog?.show()
    }

    private fun initializeDataBBSTFD() {
        bbsCommModel = realmBBS!!.where(BBSCommModel::class.java)
            .equalTo(WebParams.SCHEME_CODE, TFD).findFirst()
        listBankBenef = realmBBS!!.where(BBSBankModel::class.java)
            .equalTo(WebParams.COMM_TYPE, BENEF)
            .equalTo(WebParams.SCHEME_CODE, TFD)
            .findAll()

        if (isAgentLKD) {
            defaultProductCode = if (BuildConfig.FLAVOR.equals("development", ignoreCase = true))
                "EMO SALDOMU"
            else
                "SALDOMU"
            binding.btnChangeSource.visibility = View.GONE
            listBankSource = realmBBS!!.where(BBSBankModel::class.java)
                .equalTo(WebParams.SCHEME_CODE, TFD)
                .equalTo(WebParams.COMM_TYPE, SOURCE)
                .equalTo(WebParams.PRODUCT_NAME, defaultProductCode).findAll()
        } else {
            listBankSource =
                if (defaultProductCode != "")
                    realmBBS!!.where(BBSBankModel::class.java)
                        .equalTo(WebParams.SCHEME_CODE, TFD)
                        .equalTo(WebParams.COMM_TYPE, SOURCE)
                        .equalTo(WebParams.PRODUCT_CODE, defaultProductCode).findAll()
                else if (!transferFundMandiriLP)
                    realmBBS!!.where(BBSBankModel::class.java)
                        .equalTo(WebParams.SCHEME_CODE, TFD)
                        .equalTo(WebParams.COMM_TYPE, SOURCE)
                        .notEqualTo(WebParams.PRODUCT_CODE, "MANDIRILKD").findAll()
                else
                    realmBBS!!.where(BBSBankModel::class.java)
                        .equalTo(WebParams.SCHEME_CODE, TFD)
                        .equalTo(WebParams.COMM_TYPE, SOURCE).findAll()
        }

        setMember(listBankSource)
        setAgent(listBankBenef)
        if (bbsCommModel == null) {
            Toast.makeText(
                activity,
                getString(
                    R.string.bbstransaction_toast_not_registered,
                    getString(R.string.cash_out)
                ),
                Toast.LENGTH_LONG
            ).show()
            val isUpdatingData = sp.getBoolean(DefineValue.IS_UPDATING_BBS_DATA, false)
            if (!isUpdatingData) checkAndRunServiceBBS()
        }
    }

    private fun checkAndRunServiceBBS() {
        val bbsDataManager = BBSDataManager()
        if (!bbsDataManager.isDataUpdated) {
            bbsDataManager.runServiceUpdateData(context)
            Timber.d("Run Service update data BBS")
        }
    }

    private fun setMember(bankMember: List<BBSBankModel>?) {
        aListMember!!.clear()
        aListMember!!.addAll(BbsUtil.mappingProductCodeIcons(bankMember))
        for (i in bankMember!!.indices) {
            if (bankMember[i].product_name.lowercase(Locale.getDefault()).contains("saldomu"))
                changeSource(
                    Integer.parseInt(aListMember!![i]["flag"]!!),
                    bankMember[i].product_type,
                    bankMember[i].product_code,
                    bankMember[i].product_name,
                    bankMember[i].product_h2h
                )
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setAgent(bankAgen: List<BBSBankModel>?) {
        aListAgent!!.clear()
        for (i in bankAgen!!.indices) {
            val hm = HashMap<String, String>()
            if (bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("saldomu"))
                hm["txt"] = SALDO_AGEN
            else
                hm["txt"] = bankAgen[i].product_name
            when {
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("mandiri") -> hm["flag"] = R.drawable.logo_mandiri_bank_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("bri") -> hm["flag"] = R.drawable.logo_bank_bri_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("permata") -> hm["flag"] = R.drawable.logo_bank_permata_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("uob") -> hm["flag"] = R.drawable.logo_bank_uob_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("maspion") -> hm["flag"] = R.drawable.logo_bank_maspion_rev1_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("bii") -> hm["flag"] = R.drawable.logo_bank_bii_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("jatim") -> hm["flag"] = R.drawable.logo_bank_jatim_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("bca") -> hm["flag"] = R.drawable.logo_bca_bank_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("nobu") -> hm["flag"] = R.drawable.logo_bank_nobu.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("saldomu") -> hm["flag"] = R.drawable.logo_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("linkaja") -> hm["flag"] = R.drawable.linkaja.toString()
                bankAgen[i].product_code.lowercase(Locale.getDefault()).contains("emoedikk") -> hm["flag"] = R.drawable.dana_small.toString()
                bankAgen[i].product_code.lowercase(Locale.getDefault()).contains("009") -> hm["flag"] = R.drawable.logo_bank_bni_small.toString()
                bankAgen[i].product_name.lowercase(Locale.getDefault()).contains("akardaya") -> hm["flag"] = R.drawable.mad_small.toString()
                else -> hm["flag"] = R.drawable.ic_square_gate_one.toString()
            }
            aListAgent!!.add(hm)
        }
        if (aListAgent!!.size == 1) {
            val position = 0
            binding.btnChangeDestination.visibility = View.GONE
            changeDestination(
                Integer.parseInt(aListAgent!![position]["flag"]!!),
                bankAgen[position].product_type,
                bankAgen[position].product_code,
                bankAgen[position].product_name
            )
        }
    }

    private fun sentInsertTFD() {
        showProgressDialog()
        isSMSBanking = source_product_code.equals(MANDIRISMS, ignoreCase = true)
        comm_id = bbsCommModel!!.comm_id
        comm_code = bbsCommModel!!.comm_code
        member_code = bbsCommModel!!.member_code
        callbackURL = bbsCommModel!!.callback_url
        apiKey = bbsCommModel!!.api_key
        amount = NumberTextWatcherForThousand.trimCommaOfString(amount_transfer_edit_text.text.toString())
        noBenef = binding.noBenefValue.text.toString()
        nameBenef = if (name_value.visibility == View.VISIBLE)
            binding.nameValue.text.toString()
        else
            ""

        paymentRemark = message_value.text.toString()
        extraSignature = comm_code + member_code + source_product_type + source_product_code + benef_product_type + benef_product_code + MyApiClient.CCY_VALUE + amount

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GLOBAL_BBS_INSERT_TFD, extraSignature)

        params[WebParams.COMM_ID] = comm_id
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.COMM_CODE] = comm_code
        params[WebParams.MEMBER_CODE] = member_code
        params[WebParams.SOURCE_PRODUCT_CODE] = source_product_code!!
        params[WebParams.SOURCE_PRODUCT_TYPE] = source_product_type!!
        params[WebParams.BENEF_PRODUCT_CODE] = benef_product_code!!
        params[WebParams.BENEF_PRODUCT_TYPE] = benef_product_type!!
        if (benef_product_code!!.contains("SALDOMU")) {

            params[WebParams.BENEF_PRODUCT_VALUE_CODE] = NoHPFormat.formatTo62(noBenef)
        } else
            params[WebParams.BENEF_PRODUCT_VALUE_CODE] = noBenef
        params[WebParams.BENEF_PRODUCT_VALUE_NAME] = nameBenef
        params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
        params[WebParams.AMOUNT] = amount
        params[WebParams.PAYMENT_REMARK] = paymentRemark
        params[WebParams.USER_COMM_CODE] = BuildConfig.COMM_CODE_BBS_ATC
        params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
        params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)

        if (benef_product_code.equals("tcash", ignoreCase = true)) {
            params[WebParams.BENEF_PRODUCT_VALUE_TOKEN] = no_OTP.text.toString()
        }

        if (benef_product_type.equals(DefineValue.ACCT, ignoreCase = true)) {
            params[WebParams.BENEF_PRODUCT_VALUE_CITY] = cityId
        }

        Timber.d("params insert tfd $params")
        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GLOBAL_BBS_INSERT_TFD, params, object :
            ResponseListener {

            override fun onResponses(response: JsonObject?) {
                val model: BBSTransModel = getGson().fromJson(response, BBSTransModel::class.java)
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

                    dismissProgressDialog()
                    val mArgs = Bundle()
                    mArgs.putSerializable("data", model)
                    val mFrag: Fragment = BBSTransferFundFormFragment()
                    mFrag.arguments = mArgs
                    fragmentManager!!.beginTransaction().addToBackStack("")
                        .replace(R.id.bbs_content, mFrag, BBSTransferFundFormFragment().TAG)
                        .commit()

//                    if (isSMSBanking) {
//                        if (smsDialog == null) {
//                            smsDialog = SMSDialog()
//                        }
//                        smsDialog!!.setListener(object : SMSDialog.DialogButtonListener {
//                            override fun onClickOkButton(v: View, isLongClick: Boolean) {
//                                if (EasyPermissions.hasPermissions(activity!!, Manifest.permission.CAMERA)) {
//                                    smsDialog!!.sentSms()
//                                    regSimCardReceiver()
//                                } else {
//                                    EasyPermissions.requestPermissions(requireActivity(), getString(R.string.rationale_send_sms),
//                                        RC_SEND_SMS, Manifest.permission.CAMERA)
//                                }
//                            }
//
//                            override fun onClickCancelButton(v: View, isLongClick: Boolean) {
//                                dismissProgressDialog()
//                            }
//
//                            override fun onSuccess(user_is_new: Int) {}
//                            override fun onSuccess(product_value: String) {
//                                productValue = product_value
//                                smsDialog!!.dismiss()
//                                smsDialog!!.reset()
//                                sentDataReqToken(model)
//                            }
//                        })
//                        if (isSimExist) smsDialog!!.show(fragmentManager!!, "")
//                    } else if (source_product_h2h.equals(DefineValue.STRING_YES, ignoreCase = true) && source_product_type.equals(DefineValue.EMO, ignoreCase = true)) {
//                        if (code == WebParams.SUCCESS_CODE && !source_product_code.equals("tcash", ignoreCase = true)
//                            && !source_product_code.equals("MANDIRILKD", ignoreCase = true)) {
//                            sentDataReqToken(model)
//                        } else {
//                            sentDataReqToken(model)
//                        }
//                    } else {
//                        sentDataReqToken(model)
//                        isOwner = true
//                    }
                } else if (code == "0295") {
                    showDialogLimit(message)
                }
//                else if (code == "0296") {
//                    lkd_product_code = model.lkd_product_code
//                    dialogJoinLP(message)
//                } else if (code == "0306") {
//                    showDialogLP()
//                }
                else if (code == WebParams.LOGOUT_CODE) {
                    val test = AlertDialogLogout.getInstance()
                    test.showDialoginActivity(activity, message)
                } else if (code == DefineValue.ERROR_9333) {
                    Timber.d("isi response app data:%s", model.app_data)
                    val appModel: AppDataModel = model.app_data
                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                    alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                } else if (code == DefineValue.ERROR_0066) {
                    Timber.d("isi response maintenance:%s", response.toString())
                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                    alertDialogMaintenance.showDialogMaintenance(activity)
                } else {
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                    dismissProgressDialog()
                }
            }

            override fun onError(throwable: Throwable?) {
                dismissProgressDialog()
            }

            override fun onComplete() {
//                dismissProgressDialog()
            }
        })
    }

    fun showDialogLP() {
        dialog = DefinedDialog.MessageDialog(activity, this.getString(R.string.error),
            getString(R.string.agent_lp_dialog_message)
        ) { activity!!.finish() }
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    fun dialogJoinLP(message: String) {
        val builder1 = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        builder1.setTitle(R.string.join_lkd)
        builder1.setMessage(message)
        builder1.setCancelable(true)
        builder1.setPositiveButton(
            "Yes"
        ) { _, _ -> joinMemberLP() }
        builder1.setNegativeButton(
            "No"
        ) { _, _ -> activity!!.finish() }
        val alert11 = builder1.create()
        alert11.show()
    }

    private fun joinMemberLP() {
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
                                    sentInsertTFD()
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
                                    alertDialogMaintenance.showDialogMaintenance(activity)
                                }
                                else -> {
                                    Timber.d("isi error send data member mandiri LKD:$response")
                                    Toast.makeText(activity, response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG).show()
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
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun regSimCardReceiver() {
        if (isSMSBanking) {
            try {
                activity!!.unregisterReceiver(customSimcardListener)
            } catch (ignored: Exception) {
            }
            activity!!.registerReceiver(customSimcardListener, SMSclass.simStateIntentFilter)
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

    fun onPermissionsGranted(requestCode: Int) {
        if (requestCode == RC_READ_PHONE_STATE) {
            initializeSmsClass()
            if (isSimExist) submitAction()
        } else if (requestCode == RC_SEND_SMS) {
            smsDialog!!.sentSms()
        }
    }

    fun onPermissionsDenied(requestCode: Int) {
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
        smsClass!!.isSimExists { isExist, msg ->
            if (!isExist) {
                isSimExist = false
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                val alertDialog = builder.create()
                alertDialog.show()
            } else isSimExist = true
        }
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
                    val model: BBSTransModel = getGson().fromJson(response, BBSTransModel::class.java)
                    var code = model.error_code

                    if (code == WebParams.SUCCESS_CODE) {
                        if (isSMSBanking)
                            showDialog(model)
                        else if (benef_product_code.equals("MANDIRILKD", ignoreCase = true)) {
                            dialogBenefLP(bbsTransModel.tx_id, bbsTransModel.tx_product_code, bbsTransModel.tx_product_name, bbsTransModel.tx_bank_code,
                                bbsTransModel.amount, bbsTransModel.admin_fee, bbsTransModel.total_amount, bbsTransModel.tx_bank_name,
                                bbsTransModel.max_resend_token, bbsTransModel.benef_product_value_name, bbsTransModel.benef_product_value_code)
                        } else {
                            if (!isAgentLKD) {
                                isOwner = true
                                changeToDataMandiriLKD(bbsTransModel.tx_id, bbsTransModel.tx_product_code, bbsTransModel.tx_product_name, bbsTransModel.tx_bank_code,
                                    bbsTransModel.amount, bbsTransModel.admin_fee, bbsTransModel.total_amount, bbsTransModel.tx_bank_name,
                                    bbsTransModel.max_resend_token, bbsTransModel.benef_product_value_name, bbsTransModel.benef_product_value_code, isOwner)
                            } else changeToConfirm(bbsTransModel)
                        }
                    } else {
                        if (code == "0059" || code == "0164") {
                            showDialogErrorSMS(model.tx_bank_name, code, model.error_message)
                        } else if (code == "0057") {
                            if (transaksi.equals(getString(R.string.cash_out), ignoreCase = true)) {
                                val builder = AlertDialog.Builder(activity)
                                builder.setTitle("Alert")
                                    .setMessage(getString(R.string.member_saldo_not_enough))
                                    .setPositiveButton("OK") { _, _ -> activity!!.finish() }
                                val dialog = builder.create()
                                dialog.show()
                            } else {
                                val messageDialog = "\"" + "\" \n" + getString(R.string.dialog_message_less_balance, getString(R.string.appname))
                                val dialogFrag = AlertDialogFrag.newInstance(getString(R.string.dialog_title_less_balance),
                                    messageDialog, getString(R.string.ok), getString(R.string.cancel), false)
                                dialogFrag.okListener = DialogInterface.OnClickListener { _, _ ->
                                    val mI = Intent(activity, TopUpActivity::class.java)
                                    mI.putExtra(DefineValue.IS_ACTIVITY_FULL, true)
                                    activity!!.startActivityForResult(mI, MainPage.ACTIVITY_RESULT)
                                }
                                dialogFrag.setTargetFragment(requireParentFragment(), 0)
                                dialogFrag.show(fragmentManager!!, AlertDialogFrag.TAG)
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
        val dialog = Dialog(activity!!)
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
        if (benef_product_type.equals(DefineValue.ACCT, ignoreCase = true)) {
            mArgs.putString(DefineValue.BENEF_CITY, cityName)
        }
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
        mArgs.putString(DefineValue.BENEF_PRODUCT_CODE, benef_product_code)
        mArgs.putBoolean(DefineValue.TCASH_HP_VALIDATION, tcashValidation)
        mArgs.putBoolean(DefineValue.MANDIRI_LKD_VALIDATION, mandiriLKDValidation)
        mArgs.putBoolean(DefineValue.CODE_SUCCESS, codeSuccess)
        proses_btn.isEnabled = true
        transferFundHistory()
        val mFrag: Fragment = BBSTransferFundFormFragment()
        mFrag.arguments = mArgs
        fragmentManager!!.beginTransaction().addToBackStack("")
            .replace(R.id.bbs_content, mFrag, "com.sgo.saldomu.fragments.BBSTransferFundForm").commit()
        ToggleKeyboard.hide_keyboard(activity)
    }

    private fun changeToDataMandiriLKD(_tx_id: String, _product_code: String, _product_name: String, _bank_code: String,
                                       _amount: String, fee: String, totalAmount: String, _bank_name: String, _max_resend_token: String,
                                       _benef_acct_name: String, no_benef: String, isOwner: Boolean) {
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
        transferFundHistory()
        dismissProgressDialog()
//        val mFrag: Fragment = FragDataC2A()
//        mFrag.arguments = mArgs
//        fragmentManager!!.beginTransaction().addToBackStack("")
//            .replace(R.id.bbs_content, mFrag, FragDataC2A.TAG).commit()
        ToggleKeyboard.hide_keyboard(activity)
    }

    private fun transferFundHistory() {
        if (transferFundHistoryModel == null) {
            transferFundHistoryModel = TransferFundHistoryModel()
        }
        transferFundHistoryModel!!.setAmount(amount.toString())
        transferFundHistoryModel!!.setBenef_product_code(benef_product_code.toString())
        transferFundHistoryModel!!.setBenef_product_name(benef_product_name.toString())
        transferFundHistoryModel!!.setBenef_product_type(benef_product_type.toString())
        transferFundHistoryModel!!.setSource_product_code(source_product_code.toString())
        transferFundHistoryModel!!.setSource_product_name(source_product_name.toString())
        transferFundHistoryModel!!.setSource_product_type(source_product_type.toString())
        transferFundHistoryModel!!.setSource_product_h2h(source_product_h2h.toString())
        transferFundHistoryModel!!.setBank_account_destination(noBenef.toString())
        transferFundHistoryModel!!.setBank_account_destination_name(nameBenef.toString())
        transferFundHistoryModel!!.setPesan(binding.messageValue.text.toString())

        val gson = Gson()
        val jsonObject = gson.toJson(transferFundHistoryModel, TransferFundHistoryModel::class.java)
        val editor = sp.edit()
        editor.putString(DefineValue.TRANSFER_FUND_HISTORY_TEMP, jsonObject)
        editor.apply()
    }

    fun dialogBenefLP(_tx_id: String?, _product_code: String?, _product_name: String?, _bank_code: String?,
                      _amount: String?, _fee: String?, _totalAmount: String?, _bank_name: String?, _max_resend_token: String?,
                      _benef_acct_name: String?, no_benef: String?) {
        val builder1 = androidx.appcompat.app.AlertDialog.Builder(activity!!)
        builder1.setTitle(R.string.c2a_lkd)
        builder1.setMessage("Transfer ke : ")
        builder1.setCancelable(true)
        builder1.setPositiveButton(
            "Diri Sendiri"
        ) { _, _ ->
            isOwner = true
            changeToDataMandiriLKD(_tx_id!!, _product_code!!, _product_name!!, _bank_code!!,
                _amount!!, _fee!!, _totalAmount!!, _bank_name!!, _max_resend_token!!,
                _benef_acct_name!!, no_benef!!, isOwner)
        }
        builder1.setNegativeButton(
            "Orang Lain"
        ) { _, _ ->
            isOwner = false
            changeToDataMandiriLKD(_tx_id!!, _product_code!!, _product_name!!, _bank_code!!,
                _amount!!, _fee!!, _totalAmount!!, _bank_name!!, _max_resend_token!!,
                _benef_acct_name!!, no_benef!!, isOwner)
        }
        val alert11 = builder1.create()
        alert11.show()
    }

    fun showDialogErrorSMS(nama_bank: String?, error_code: String, error_msg: String?) { // Create custom dialog object
        val dialog = Dialog(activity!!)
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

    override fun onDestroyView() {
        super.onDestroyView()
        RealmManager.closeRealm(realm)
        RealmManager.closeRealm(realmBBS)
        _binding = null
    }

}