package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.sgo.saldomu.Beans.TransferFundHistoryModel
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.databinding.FragmentBBSTransferFundBinding
import com.sgo.saldomu.dialogs.DefinedDialog
import com.sgo.saldomu.dialogs.DialogBankList
import com.sgo.saldomu.dialogs.SMSDialog
import com.sgo.saldomu.entityRealm.BBSBankModel
import com.sgo.saldomu.entityRealm.BBSCommModel
import com.sgo.saldomu.entityRealm.List_BBS_City
import com.sgo.saldomu.utils.BbsUtil
import com.sgo.saldomu.utils.NumberTextWatcherForThousand
import com.sgo.saldomu.widgets.BaseFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.bbs_cash_in_cash_out.*
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
    private var benef_product_bank_gateaway: String? = null
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

    private var cityAutocompletePosition = -1

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

    private var list_bbs_cities: ArrayList<List_BBS_City>? = null
    private var list_name_bbs_cities: ArrayList<String>? = null

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

        Log.e("benef_product_type", "changeDestination: " + benef_product_type )
//        if (benef_product_type.equals(DefineValue.EMO, ignoreCase = true) && !benef_product_code.equals("MANDIRILKD", ignoreCase = true)) {
//            no_benef_value.hint = getString(R.string.number_hp_destination_hint)
//        } else {
//            if (benef_product_code.equals("MANDIRILKD", ignoreCase = true)) {
//                no_benef_value.setHint(R.string.nomor_rekening)
//            } else {
//                no_benef_value.setHint(R.string.number_destination_hint)
//            }
//            no_benef_value.setText("")
//        }
//
//        if (benef_product_code.equals("linkaja", ignoreCase = true))
//            name_value.visibility = View.VISIBLE
//        else
//            name_value.visibility = View.GONE
//
//        if (benef_product_code.equals("tcash", ignoreCase = true))
//            no_OTP.visibility = View.VISIBLE
//        else
//            no_OTP.visibility = View.GONE
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
        if (city_benef_value.visibility == View.VISIBLE) {
            if (city_benef_value.text.toString() == "") {
                city_benef_value.requestFocus()
                city_benef_value.error = getString(R.string.destination_city_empty_message)
                return false
            } else if (!list_name_bbs_cities!!.contains(city_benef_value.text.toString())) {
                city_benef_value.requestFocus()
                city_benef_value.error = getString(R.string.city_not_found_message)
                return false
            } else {
                cityAutocompletePosition = list_name_bbs_cities!!.indexOf(city_benef_value.text.toString())
                city_benef_value.error = null
            }
        }
        return true
    }

    private fun submitAction() {
        if (inputValidation()) {
//            sentInsertC2A()
            Log.e("TAG", "submitAction: " )
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
            .equalTo(WebParams.COMM_TYPE, "BENEF")
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
            if (bankMember[i].product_name.toLowerCase(Locale.getDefault()).contains("saldomu"))
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
            binding.btnChangeDestination.visibility = View.GONE
            changeDestination(
                Integer.parseInt(aListAgent!![position]["flag"]!!),
                bankAgen[position].product_type,
                bankAgen[position].product_code,
                bankAgen[position].product_name
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RealmManager.closeRealm(realm)
        RealmManager.closeRealm(realmBBS)
        _binding = null
    }

}