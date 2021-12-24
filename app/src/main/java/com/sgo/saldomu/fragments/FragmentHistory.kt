package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.HistoryAdapter
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
import com.sgo.saldomu.interfaces.ResponseListener
import com.sgo.saldomu.models.retrofit.GetTrxStatusReportModel
import com.sgo.saldomu.models.retrofit.HistoryModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.utils.PaginationScrollListener
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_swipe_refresh_list.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragmentHistory : BaseFragment(), HistoryAdapter.HistoryListener, SwipeRefreshLayout.OnRefreshListener, ReportBillerDialog.OnDialogOkCallback {

    private var mLayoutManager: LinearLayoutManager? = null
    internal lateinit var params: HashMap<String, Any>
    internal var currentPage: Int = 0
    internal var isLoading: Boolean = false
    internal var isLastPage: Boolean = false
    internal lateinit var adapter: HistoryAdapter
    internal lateinit var dialog: AlertDialog
    private var isMemberCTA: Boolean? = false
    private var isReport: Boolean? = false
    private var next: String = "0"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_swipe_refresh_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setupRecycler()

        getHistory()
    }

    private fun getHistory() {
        showProgressDialog()

        extraSignature = memberIDLogin
        val url = MyApiClient.LINK_HISTORY

        params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_ID] = memberIDLogin
        params[WebParams.PAGE] = currentPage
        Timber.tag(TAG).e("params history : %s", params)
        RetrofitService.getInstance().PostObjectRequest(url, params, object : ResponseListener {
            override fun onResponses(jsonObject: JsonObject) {
                Timber.tag(TAG).e("onResponses: %s", jsonObject)
                val model = getGson().fromJson(jsonObject, jsonModel::class.java)
                val code = model.error_code
                val message = model.error_message

                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        next = jsonObject.get("next").toString()
                        val type = object : TypeToken<List<HistoryModel>>() {
                        }.type
                        val list =
                            gson.fromJson<List<HistoryModel>>(jsonObject.get("report_data"), type)

                        if (next == "" || next == "0") {
                            isLastPage = true
                        }

                        adapter.updateAdapter(list)
                        isLoading = false
                    }
                    WebParams.LOGOUT_CODE -> {
                        AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                    }
                    WebParams.ERROR_9333 -> {
                        Timber.d("isi response app data:%s", model.app_data)
                        val appModel = model.app_data
                        AlertDialogUpdateApp.getInstance().showDialogUpdate(
                            activity,
                            appModel.type,
                            appModel.packageName,
                            appModel.downloadUrl
                        )
                    }
                    WebParams.ERROR_0066 -> {
                        Timber.d("isi response maintenance:$jsonObject")
                        AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                    }
                }
            }

            override fun onError(throwable: Throwable) {
//                setDialog(false)
                dismissProgressDialog()
                Toast.makeText(activity, throwable.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onComplete() {
//                setDialog(false)
                dismissProgressDialog()
                recycler_view.visibility = View.VISIBLE
            }
        })
    }

    private fun initialize() {
        currentPage = 1
        mLayoutManager = LinearLayoutManager(activity)
        adapter = HistoryAdapter(this)
        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)

        val builder = AlertDialog.Builder(context!!)
        builder.setView(R.layout.progress)
        dialog = builder.create()
    }

    private fun setupRecycler() {
        recycler_view.adapter = adapter
        recycler_view.layoutManager = mLayoutManager
        recycler_view.setHasFixedSize(true)
        recycler_view.run {
            recycler_view.itemAnimator = DefaultItemAnimator()
            mLayoutManager?.let {
                addOnScrollListener(object : PaginationScrollListener(it) {
                    override fun isLastPage(): Boolean {
                        return isLastPage
                    }

                    override fun isLoading(): Boolean {
                        return isLoading
                    }

                    override fun loadMoreItems() {
                        isLoading = true
                        currentPage++
                        getHistory()
                    }
                })
            }
        }
    }

    override fun showErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(model: HistoryModel) {
        getTrxStatus(model)
    }

    fun getTrxStatus(historyModel: HistoryModel) {
//        dialog.show()
        showProgressDialog()

        extraSignature = historyModel.tx_id + historyModel.comm_id
        val params = RetrofitService.getInstance()
                .getSignature(MyApiClient.LINK_GET_TRX_STATUS, extraSignature)

        params[WebParams.TX_ID] = historyModel.tx_id
        params[WebParams.COMM_ID] = historyModel.comm_id
        params[WebParams.USER_ID] = sp.getString(DefineValue.USERID_PHONE, "")
        params[WebParams.TX_TYPE] = historyModel.tx_type
        params[WebParams.IS_DETAIL] = DefineValue.STRING_YES
        Timber.d("isi params sent get Trx Status:$params")

        RetrofitService.getInstance().PostObjectRequest(MyApiClient.LINK_GET_TRX_STATUS, params,
                object : ResponseListener {
                    override fun onResponses(jsonObject: JsonObject) {
                        Timber.e("gettrx : " + jsonObject)

                        val model =
                            getGson().fromJson(jsonObject, GetTrxStatusReportModel::class.java)

                        when (model.error_code) {
                            WebParams.SUCCESS_CODE -> showDialog(historyModel, model)
                            WebParams.LOGOUT_CODE -> {
                                val message = model.error_message
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
                            }
                            WebParams.ERROR_9333 -> {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                AlertDialogUpdateApp.getInstance().showDialogUpdate(
                                    activity,
                                    appModel.type,
                                    appModel.packageName,
                                    appModel.downloadUrl
                                )
                            }
                            WebParams.ERROR_0066 -> {
                                AlertDialogMaintenance.getInstance().showDialogMaintenance(activity)
                            }
                            else -> {
                                showErrorMessage(model.error_message)
                            }
                        }
                    }

                    override fun onError(throwable: Throwable) {
//                        setDialog(false)
                        dismissProgressDialog()
                    }

                    override fun onComplete() {
//                        setDialog(false)
                        dismissProgressDialog()
                    }
                })
    }

    private fun showDialog(_object: HistoryModel, response: GetTrxStatusReportModel) {
        if (_object.buss_scheme_code == DefineValue.BIL) {
            showReportEspayBillerDialog(sp.getString(DefineValue.USER_NAME, ""), response)
            //
        } else if (_object.buss_scheme_code == DefineValue.CTA || _object.buss_scheme_code == DefineValue.CTR) {
            if (sp.getString(DefineValue.USERID_PHONE, "") == response.member_phone) {
                showReportCTADialog(response)
            } else {
                isMemberCTA = true
                showReportCTADialog(response)
            }

        } else if (_object.buss_scheme_code == DefineValue.ATC) {
            Timber.d(sp.getString(DefineValue.USERID_PHONE, "")!! + "user_id")
            if (sp.getString(DefineValue.USERID_PHONE, "") == response.member_phone) {
                showReportATCAgentDialog(response)
            } else {
                isReport = true
                showReportATCMemberDialog(response)
            }
        } else if (_object.buss_scheme_code == DefineValue.EMO || _object.buss_scheme_code == DefineValue.TOPUP_B2B) {
            showReportEMODialog(response)
        } else if (_object.buss_scheme_code == DefineValue.DENOM_B2B || _object.buss_scheme_code == DefineValue.EBD) {
            showReportBDKDialog(response)
        } else if (_object.buss_scheme_code == DefineValue.DGI) {
            showReportCollectorDialog(response)
        } else if (_object.buss_scheme_code == DefineValue.SG3) {
            showReportSOFDialog(response)
        } else if (_object.buss_scheme_code == DefineValue.OR || _object.buss_scheme_code == DefineValue.ORP || _object.buss_scheme_code == DefineValue.IR || _object.buss_scheme_code == DefineValue.OC || _object.buss_scheme_code == DefineValue.AJC || _object.buss_scheme_code == DefineValue.AJD) run {
            showReportBillerDialog(response)
        } else if (_object.buss_scheme_code == DefineValue.QRS) {
            showReportQRSDialog(response)
        }
    }

    private fun showReportQRSDialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(Objects.requireNonNull(response.created)))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))
        args.putString(DefineValue.TIPS_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tips_amount))
        val amount = Objects.requireNonNull(response.tx_amount)!!.toDouble()
        val fee = Objects.requireNonNull(response.admin_fee)!!.toDouble()
        val tipFee = Objects.requireNonNull(response.tips_amount)!!.toDouble()
        val totalAmount = amount + fee + tipFee
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(totalAmount))
        var txStat = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.MERCHANT_NAME, response.merchant_name)
        args.putString(DefineValue.MERCHANT_CITY, response.merchant_city)
        args.putString(DefineValue.MERCHANT_PAN, response.merchant_pan)
        args.putString(DefineValue.TERMINAL_ID, response.terminal_id)
        args.putString(DefineValue.TRX_ID_REF, response.trx_id_ref)
        args.putString(DefineValue.INDICATOR_TYPE, response.indicator_type)
        dialog.arguments = args
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.add(dialog, ReportBillerDialog.TAG)
        ft.commitAllowingStateLoss()
    }

    private fun showReportBillerDialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USERID_PHONE, response.member_cust_id)
        args.putString(DefineValue.USER_NAME, response.member_cust_name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.DETAIL, response.detail)
        args.putString(DefineValue.REMARK, response.payment_remark)
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TRANSACTION)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.MEMBER_PHONE, response.member_phone)
        args.putString(DefineValue.MEMBER_NAME, response.member_name)
        args.putString(DefineValue.PAYMENT_PHONE, response.payment_phone)
        args.putString(DefineValue.PAYMENT_NAME, response.payment_name)
        args.putString(DefineValue.PAYMENT_BANK, response.payment_bank)
        args.putString(DefineValue.NO_BENEF, response.benef_acct_no)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_amount))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))
        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)
        dialog.arguments = args
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.add(dialog, ReportBillerDialog.TAG)
        ft.commitAllowingStateLoss()
    }

    private fun showReportEspayBillerDialog(name: String?, response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.USERID_PHONE, response.member_cust_id)
        args.putString(DefineValue.DENOM_DATA, response.payment_name)
        var amount = response.total_amount!!.toDouble() - response.admin_fee!!.toDouble() - response.additional_fee!!.toDouble()
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount))
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BILLER)
        args.putString(DefineValue.PRODUCT_NAME, response.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.additional_fee))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
        }

        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)


        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_amount))


        args.putString(DefineValue.DETAILS_BILLER, response.detail)

        if (!response.biller_detail.toString().equals("", ignoreCase = true)) {
            args.putString(DefineValue.BILLER_DETAIL, JsonParser().parse(Gson().toJson(response.biller_detail)).toString())
        }
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.BILLER_TYPE, response.biller_type)
        args.putString(DefineValue.PAYMENT_REMARK, response.payment_remark)
        dialog.arguments = args
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.add(dialog, ReportBillerDialog.TAG)
        ft.commitAllowingStateLoss()
    }

    private fun showReportCTADialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, response.member_name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHIN)
        args.putString(DefineValue.USERID_PHONE, response.member_phone)
        args.putString(DefineValue.BANK_NAME, response.tx_bank_name)
        args.putString(DefineValue.BANK_PRODUCT, response.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.additional_fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_amount))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)
        args.putString(DefineValue.MEMBER_NAME, response.member_name)
        args.putString(DefineValue.SOURCE_ACCT, response.source_bank_name)
        args.putString(DefineValue.SOURCE_ACCT_NO, response.source_acct_no)
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.source_acct_name)
        args.putString(DefineValue.BANK_BENEF, response.benef_bank_name)
        args.putString(DefineValue.TYPE_BENEF, response.benef_acct_type)
        args.putString(DefineValue.NO_BENEF, response.benef_acct_no)
        args.putString(DefineValue.NAME_BENEF, response.benef_acct_name)
        args.putString(DefineValue.PRODUCT_NAME, response.product_name)
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.member_shop_phone)
        args.putString(DefineValue.MEMBER_SHOP_NAME, response.member_shop_name)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putBoolean(DefineValue.IS_MEMBER_CTA, this.isMemberCTA!!)

        dialog.arguments = args
        //        dialog.setTargetFragment(this,0);
        dialog.show(requireActivity().supportFragmentManager, ReportBillerDialog.TAG)
    }

    private fun showReportATCAgentDialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, response.member_name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT)
        args.putString(DefineValue.USERID_PHONE, response.member_phone)
        args.putString(DefineValue.BANK_NAME, response.tx_bank_name)
        args.putString(DefineValue.BANK_PRODUCT, response.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.additional_fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_amount))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)
        args.putString(DefineValue.MEMBER_SHOP_NAME, response.member_shop_name)
        args.putString(DefineValue.MEMBER_SHOP_NO, response.member_shop_phone)


        args.putString(DefineValue.MEMBER_NAME, response.member_name)
        args.putString(DefineValue.SOURCE_ACCT, response.source_bank_name)
        args.putString(DefineValue.SOURCE_ACCT_NO, response.source_acct_no)
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.source_acct_name)
        args.putString(DefineValue.BANK_BENEF, response.benef_bank_name)
        args.putString(DefineValue.NO_BENEF, response.benef_acct_no)
        args.putString(DefineValue.NAME_BENEF, response.benef_acct_name)
        args.putString(DefineValue.PRODUCT_NAME, response.product_name)
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.member_shop_phone)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)

        dialog.arguments = args
        //        dialog.setTargetFragment(this,0);
        dialog.show(requireActivity().supportFragmentManager, ReportBillerDialog.TAG)
    }

    private fun showReportATCMemberDialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)

        args.putString(DefineValue.USER_NAME, response.member_name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.BBS_CASHOUT)
        args.putString(DefineValue.USERID_PHONE, response.member_phone)
        args.putString(DefineValue.BANK_NAME, response.tx_bank_name)
        args.putString(DefineValue.BANK_PRODUCT, response.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))
        args.putString(DefineValue.ADDITIONAL_FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.additional_fee))
        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_amount))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        args.putString(DefineValue.OTP_MEMBER, response.otp_member)
        args.putString(DefineValue.MEMBER_PHONE, response.member_phone)
        args.putBoolean(DefineValue.IS_REPORT, isReport!!)

        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)
        args.putString(DefineValue.MEMBER_SHOP_NAME, response.member_shop_name)
        args.putString(DefineValue.MEMBER_SHOP_NO, response.member_shop_phone)

        args.putString(DefineValue.MEMBER_NAME, response.member_name)
        args.putString(DefineValue.SOURCE_ACCT, response.source_bank_name)
        args.putString(DefineValue.SOURCE_ACCT_NAME, response.source_acct_name)
        args.putString(DefineValue.BANK_BENEF, response.benef_bank_name)
        args.putString(DefineValue.NO_BENEF, response.benef_acct_no)
        args.putString(DefineValue.NAME_BENEF, response.benef_acct_name)
        args.putString(DefineValue.MEMBER_SHOP_PHONE, response.member_shop_phone)
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)

        dialog.arguments = args
        dialog.show(requireActivity().supportFragmentManager, ReportBillerDialog.TAG)
    }

    private fun showReportEMODialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)
        args.putString(DefineValue.USER_NAME, response.member_name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP)
        args.putString(DefineValue.USERID_PHONE, response.member_phone)
        args.putString(DefineValue.BANK_PRODUCT, response.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))

        val dAmount = java.lang.Double.valueOf(response.tx_amount!!)
        val dFee = java.lang.Double.valueOf(response.admin_fee!!)
        val total_amount = dAmount + dFee

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.COMMUNITY_CODE, response.comm_code)
        args.putString(DefineValue.MEMBER_CODE, response.member_code)
        args.putString(DefineValue.MEMBER_CUST_NAME, response.member_cust_name)
        args.putString(DefineValue.MEMBER_ID_CUST, response.member_cust_id)
        args.putString(DefineValue.STORE_NAME, response.store_name)
        args.putString(DefineValue.STORE_ADDRESS, response.store_address)
        args.putString(DefineValue.STORE_CODE, response.store_code)

        dialog.arguments = args
        dialog.show(requireActivity().supportFragmentManager, ReportBillerDialog.TAG)
    }

    private fun showReportBDKDialog(response: GetTrxStatusReportModel) {
        val args = Bundle()
        val dialog = ReportBillerDialog.newInstance(this)

        args.putString(DefineValue.USER_NAME, response.member_name)
        args.putString(DefineValue.DATE_TIME, DateTimeFormat.formatToID(response.created!!))
        args.putString(DefineValue.TX_ID, response.tx_id)
        args.putString(DefineValue.REPORT_TYPE, DefineValue.TOPUP)
        args.putString(DefineValue.USERID_PHONE, response.member_phone)
        args.putString(DefineValue.BANK_PRODUCT, response.product_name)
        args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.admin_fee))
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_gross))

        val dAmount = java.lang.Double.valueOf(response.tx_amount!!)
        val dFee = java.lang.Double.valueOf(response.admin_fee!!)
        val total_amount = dAmount + dFee

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == DefineValue.SUCCESS) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        args.putString(DefineValue.TRX_STATUS_REMARK, response.tx_status_remark)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.COMMUNITY_CODE, response.comm_code)
        args.putString(DefineValue.MEMBER_CODE, response.member_code)
        args.putString(DefineValue.DENOM_DETAIL, getGson().toJson(response.denom_detail))
        args.putString(DefineValue.ORDER_ID, response.order_id)
        args.putString(DefineValue.STORE_CODE, response.store_code)
        args.putString(DefineValue.STORE_NAME, response.store_name)
        args.putString(DefineValue.STORE_ADDRESS, response.store_address)
        args.putString(DefineValue.AGENT_NAME, response.member_cust_name)
        args.putString(DefineValue.AGENT_PHONE, response.member_cust_id)
        args.putString(DefineValue.PARTNER_CODE_ESPAY, response.partner)
        args.putString(DefineValue.TOTAL_DISC, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_disc))

        dialog.arguments = args
        dialog.show(requireActivity().supportFragmentManager, ReportBillerDialog.TAG)
    }

    private fun showReportCollectorDialog(resp: GetTrxStatusReportModel) {

        try {
            val response = JSONObject(getGson().toJson(resp))

            val args = Bundle()
            val txStatus = response.optString(WebParams.TX_STATUS)
            val dialog = ReportBillerDialog.newInstance(this)
            args.putString(DefineValue.USER_NAME, resp.member_cust_name)
            args.putString(DefineValue.DATE_TIME, response.optString(WebParams.CREATED))
            args.putString(DefineValue.TX_ID, response.optString(WebParams.TX_ID))
            args.putString(DefineValue.REPORT_TYPE, DefineValue.DGI)

            args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT)))

            args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.ADMIN_FEE)))
            args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT)))
            args.putBoolean(DefineValue.IS_SHOW_DESCRIPTION, true)

            var txStat: Boolean? = false
            if (txStatus == DefineValue.SUCCESS) {
                txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            } else if (txStatus == DefineValue.ONRECONCILED) {
                txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
            args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
            args.putString(DefineValue.TRX_STATUS_REMARK, response.optString(WebParams.TX_STATUS_REMARK))
            if (!txStat)
                args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK))


            args.putString(DefineValue.DETAILS_BILLER, response.optString(WebParams.DETAIL, ""))


            args.putString(DefineValue.INVOICE, resp.invoice)
            args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE))
            args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME))
            args.putString(DefineValue.PRODUCT_NAME, resp.product_name)
            args.putString(DefineValue.PAYMENT_TYPE_DESC, resp.payment_type_desc)
            args.putString(DefineValue.DGI_MEMBER_NAME, resp.dgi_member_name)
            args.putString(DefineValue.DGI_ANCHOR_NAME, resp.dgi_anchor_name)
            args.putString(DefineValue.DGI_COMM_NAME, resp.dgi_comm_name)
            args.putString(DefineValue.TRX_ID_REF, response.optString(WebParams.TRX_ID_REF));

            dialog.arguments = args
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.add(dialog, ReportBillerDialog.TAG)
            ft.commitAllowingStateLoss()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun showReportSOFDialog(resp: GetTrxStatusReportModel) {
        try {
            val response = JSONObject(getGson().toJson(resp))

            val txStatus = response.optString(WebParams.TX_STATUS)

            val args = Bundle()
            val dialog = ReportBillerDialog.newInstance(this)
            args.putString(DefineValue.TX_ID, response.optString(WebParams.TX_ID))
            args.putString(DefineValue.REPORT_TYPE, DefineValue.SG3)
            args.putString(DefineValue.DATE_TIME, response.optString(WebParams.CREATED))
            args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TX_AMOUNT)))
            args.putString(DefineValue.FEE, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.ADMIN_FEE)))
            args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.optString(WebParams.TOTAL_AMOUNT)))

            var txStat: Boolean? = false
            if (txStatus == DefineValue.SUCCESS) {
                txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            } else if (txStatus == DefineValue.ONRECONCILED) {
                txStat = true
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            }
//        else if (txStatus == DefineValue.SUSPECT) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
//        } else if (txStatus != DefineValue.FAILED) {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
//        } else {
//            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
//        }
            args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
            args.putString(DefineValue.TRX_STATUS_REMARK, response.optString(WebParams.TX_STATUS_REMARK))
            if (!txStat)
                args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK))


            args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE))
            args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME))
            args.putString(DefineValue.COMMUNITY_NAME, response.optString(WebParams.COMM_NAME))
            args.putString(DefineValue.REMARK, response.optString(WebParams.PAYMENT_REMARK))

            dialog.arguments = args
            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.add(dialog, ReportBillerDialog.TAG)
            ft.commitAllowingStateLoss()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun onRefresh() {
        isLastPage = false
        currentPage = 1
        adapter.clearAdapter()
        getHistory()
        swipeRefresh.isRefreshing = false
    }

    override fun onOkButton() {

    }

    companion object {
        internal const val TAG = "HistoryActivity"
    }
}
