package com.sgo.saldomu.activities

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.HistoryAdapter
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DateTimeFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.DefineValue.SUCCESS
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
import com.sgo.saldomu.widgets.BaseActivity
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class HistoryActivity : BaseActivity(), HistoryAdapter.HistoryListener, SwipeRefreshLayout.OnRefreshListener, ReportBillerDialog.OnDialogOkCallback {
    internal lateinit var mRecyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
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
    private var agentCOL: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRecyclerView = findViewById(R.id.recycler_view)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        agentCOL = intent.getBooleanExtra(DefineValue.AGENT_COL, false)
        if (agentCOL==true)
        {
            sp.edit().putBoolean(DefineValue.AGENT_COL,true).commit()
        }


        initialize()
        setupRecycler()

        getHistory()
    }

    internal fun getHistory() {
        showProgressDialog()

        extraSignature = memberIDLogin
        var url = ""
        if (agentCOL) {
            url = MyApiClient.LINK_HISTORY_COLLECTOR
        } else {
            url = MyApiClient.LINK_HISTORY
        }
        params = RetrofitService.getInstance().getSignature(url, extraSignature)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_ID] = memberIDLogin
        params[WebParams.PAGE] = currentPage
        Log.e(TAG, "params history : $`params`")
        RetrofitService.getInstance().PostObjectRequest(url, params, object : ResponseListener {
            override fun onResponses(`object`: JsonObject) {
                Log.e(TAG, "onResponses: $`object`")
                val model = getGson().fromJson(`object`, jsonModel::class.java)
                next = `object`.get("next").toString()
                val code = model.error_code
                val message = model.error_message

                if (code == WebParams.SUCCESS_CODE) {
                    val type = object : TypeToken<List<HistoryModel>>() {
                    }.type
                    val list = gson.fromJson<List<HistoryModel>>(`object`.get("report_data"), type)

                    if (next == "" || next == "0") {
                        isLastPage = true
                    }

                    adapter.updateAdapter(list)
                    isLoading = false
                } else if (code == WebParams.LOGOUT_CODE) {
                    val test = AlertDialogLogout.getInstance()
                    test.showDialoginActivity(this@HistoryActivity, message)
                } else if (code == DefineValue.ERROR_9333) run {
                    Timber.d("isi response app data:" + model.app_data)
                    val appModel = model.app_data
                    val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                    alertDialogUpdateApp.showDialogUpdate(this@HistoryActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
                } else if (code == DefineValue.ERROR_0066) run {
                    Timber.d("isi response maintenance:$`object`")
                    val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                    alertDialogMaintenance.showDialogMaintenance(this@HistoryActivity, model.error_message)
                }
            }

            override fun onError(throwable: Throwable) {
//                setDialog(false)
                dismissProgressDialog()
                Toast.makeText(this@HistoryActivity, throwable.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onComplete() {
//                setDialog(false)
                dismissProgressDialog()
                mRecyclerView.visibility = View.VISIBLE
            }
        })
    }

    private fun setDialog(show: Boolean) {
        if (show)
            dialog.show()
        else
            dialog.dismiss()
    }

    private fun initialize() {
        currentPage = 1
        mLayoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(this)
        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = intent.getStringExtra(DefineValue.HISTORY_TITLE)

        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress)
        dialog = builder.create()
    }


    private fun setupRecycler() {
        mRecyclerView.adapter = adapter
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.run {
            mRecyclerView.itemAnimator = DefaultItemAnimator()
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

    override fun getLayoutResource(): Int {
        return R.layout.activity_history
    }

    override fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                        Log.e(TAG, "gettrx : $jsonObject")

                        val model = getGson().fromJson(jsonObject, GetTrxStatusReportModel::class.java)

                        when (model.error_code) {
                            WebParams.SUCCESS_CODE -> showDialog(historyModel, model)
                            WebParams.LOGOUT_CODE -> {
                                val message = model.error_message
                                val test = AlertDialogLogout.getInstance()
                                test.showDialoginMain(this@HistoryActivity, message)
                            }
                            WebParams.ERROR_9333 -> {
                                Timber.d("isi response app data:" + model.app_data)
                                val appModel = model.app_data
                                val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                                alertDialogUpdateApp.showDialogUpdate(this@HistoryActivity, appModel.type, appModel.packageName, appModel.downloadUrl)
                            }
                            WebParams.ERROR_0066 -> {
                                val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                                alertDialogMaintenance.showDialogMaintenance(this@HistoryActivity, model.error_message)
                            }
                            else -> {
                                val msg = model.error_message
                                showErrorMessage(msg)
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
        if (_object.buss_scheme_code == "BIL") {
            showReportEspayBillerDialog(sp.getString(DefineValue.USER_NAME, ""), response)
            //
        } else if (_object.buss_scheme_code == "CTA" || _object.buss_scheme_code == "CTR") {
            if (sp.getString(DefineValue.USERID_PHONE, "") == response.member_phone) {
                showReportCTADialog(response)
            } else {
                isMemberCTA = true
                showReportCTADialog(response)
            }

        } else if (_object.buss_scheme_code == "ATC") {
            Timber.d(sp.getString(DefineValue.USERID_PHONE, "")!! + "user_id")
            if (sp.getString(DefineValue.USERID_PHONE, "") == response.member_phone) {
                showReportATCAgentDialog(response)
            } else {
                isReport = true
                showReportATCMemberDialog(response)
            }
        } else if (_object.buss_scheme_code == "EMO" || _object.buss_scheme_code.equals("TOP", ignoreCase = true)) {
            showReportEMODialog(response)
        } else if (_object.buss_scheme_code == "BDK") {
            showReportBDKDialog(response)
        } else if (_object.buss_scheme_code == "DGI") {
            showReportCollectorDialog(response)
        } else if (_object.buss_scheme_code == "SG3") {
            showReportSOFDialog(response)
        } else if (_object.buss_scheme_code == "OR" || _object.buss_scheme_code == "ORP" || _object.buss_scheme_code == "IR" || _object.buss_scheme_code == "OC") run {
            showReportBillerDialog(response)
        }
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
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)
        dialog.arguments = args
        val ft = this.supportFragmentManager.beginTransaction()
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
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)


        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.total_amount))


        args.putString(DefineValue.DETAILS_BILLER, response.detail)

        if (!response.biller_detail.toString().equals("", ignoreCase = true)) {
            val jsonParser = JsonParser()
            val gson = Gson()
            args.putString(DefineValue.BILLER_DETAIL, jsonParser.parse(gson.toJson(response.biller_detail)).toString()
                    //                response.getBiller_detail().getPhoneNumber()
            )
        }
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)

        dialog.arguments = args
        val ft = this.supportFragmentManager.beginTransaction()
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
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
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
        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putBoolean(DefineValue.IS_MEMBER_CTA, this.isMemberCTA!!)

        dialog.arguments = args
        //        dialog.setTargetFragment(this,0);
        dialog.show(this.supportFragmentManager, ReportBillerDialog.TAG)
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
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
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
        dialog.show(this.supportFragmentManager, ReportBillerDialog.TAG)
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
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
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
        dialog.show(this.supportFragmentManager, ReportBillerDialog.TAG)
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
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.COMMUNITY_CODE, response.comm_code)
        args.putString(DefineValue.MEMBER_CODE, response.member_code)

        dialog.arguments = args
        dialog.show(this.supportFragmentManager, ReportBillerDialog.TAG)
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
        args.putString(DefineValue.AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(response.tx_amount))

        val dAmount = java.lang.Double.valueOf(response.tx_amount!!)
        val dFee = java.lang.Double.valueOf(response.admin_fee!!)
        val total_amount = dAmount + dFee

        args.putString(DefineValue.TOTAL_AMOUNT, MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(total_amount))

        var txStat: Boolean? = false
        val txStatus = response.tx_status
        if (txStatus == SUCCESS) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
        } else if (txStatus == DefineValue.ONRECONCILED) {
            txStat = true
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
        } else if (txStatus == DefineValue.SUSPECT) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
        } else if (txStatus != DefineValue.FAILED) {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
        } else {
            args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
        }
        args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
        if (!txStat) args.putString(DefineValue.TRX_REMARK, response.tx_remark)

        args.putString(DefineValue.BUSS_SCHEME_CODE, response.buss_scheme_code)
        args.putString(DefineValue.BUSS_SCHEME_NAME, response.buss_scheme_name)
        args.putString(DefineValue.COMMUNITY_CODE, response.comm_code)
        args.putString(DefineValue.MEMBER_CODE, response.member_code)
        args.putString(DefineValue.DENOM_DETAIL, getGson().toJson(response.denom_detail))
        args.putString(DefineValue.ORDER_ID, response.order_id)

        dialog.arguments = args
        dialog.show(this.supportFragmentManager, ReportBillerDialog.TAG)
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
            if (txStatus == SUCCESS) {
                txStat = true
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            } else if (txStatus == DefineValue.ONRECONCILED) {
                txStat = true
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            } else if (txStatus == DefineValue.SUSPECT) {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
            } else if (txStatus != DefineValue.FAILED) {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
            } else {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
            }
            args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
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

            dialog.arguments = args
            val ft = this.supportFragmentManager.beginTransaction()
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
            if (txStatus == SUCCESS) {
                txStat = true
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_success))
            } else if (txStatus == DefineValue.ONRECONCILED) {
                txStat = true
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_pending))
            } else if (txStatus == DefineValue.SUSPECT) {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_suspect))
            } else if (txStatus != DefineValue.FAILED) {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction) + " " + txStatus)
            } else {
                args.putString(DefineValue.TRX_MESSAGE, getString(R.string.transaction_failed))
            }
            args.putBoolean(DefineValue.TRX_STATUS, txStat!!)
            if (!txStat)
                args.putString(DefineValue.TRX_REMARK, response.optString(WebParams.TX_REMARK))


            args.putString(DefineValue.BUSS_SCHEME_CODE, response.optString(WebParams.BUSS_SCHEME_CODE))
            args.putString(DefineValue.BUSS_SCHEME_NAME, response.optString(WebParams.BUSS_SCHEME_NAME))
            args.putString(DefineValue.COMMUNITY_NAME, response.optString(WebParams.COMM_NAME))
            args.putString(DefineValue.REMARK, response.optString(WebParams.PAYMENT_REMARK))

            dialog.arguments = args
            val ft = this.supportFragmentManager.beginTransaction()
            ft.add(dialog, ReportBillerDialog.TAG)
            ft.commitAllowingStateLoss()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun onRefresh() {
        isLastPage = false
        currentPage = 0
        adapter.clearAdapter()
        getHistory()
        swipeRefresh.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOkButton() {

    }

    companion object {
        internal const val TAG = "HistoryActivity"
    }
}
