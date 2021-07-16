package com.sgo.saldomu.fragments

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserPOActivity
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.InetHandler
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.ListPOModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_input_store_code.*
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListPOCanvasser : BaseFragment(), ListPOAdapter.listener {
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""
    var partner = ""
    var partnerCodeEspay = ""
    var anchorCodeEspay = ""
    var bundle = Bundle()
    var isDisconnected: Boolean = false

    private val docListArrayList = ArrayList<ListPOModel>()

    private var listPOAdapter: ListPOAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bundle = arguments!!

        val canvasserPOActivity = activity as CanvasserPOActivity
        canvasserPOActivity.initializeToolbar(getString(R.string.list_po))


        isDisconnected = !InetHandler.isNetworkAvailable(activity)

        btn_create_po.visibility = View.VISIBLE

        if (bundle != null) {
            memberCodeEspay = bundle.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay = bundle.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            custIdEspay = bundle.getString(DefineValue.CUST_ID_ESPAY, "")
            partner = bundle.getString(DefineValue.PARTNER, "")
            partnerCodeEspay = bundle.getString(DefineValue.PARTNER_CODE_ESPAY, "")
            anchorCodeEspay = bundle.getString(DefineValue.ANCHOR_COMPANY, "")
        }

        initializeListPO()

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                listPOAdapter!!.filter.filter(editable.toString())
            }
        })

        btn_create_po.setOnClickListener {
//            val fragment = FragListItemPOCanvasser()
//            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
//            bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
//            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
//            bundle.putString(DefineValue.PARTNER, partner)
//            bundle.putString(DefineValue.PARTNER_CODE_ESPAY, partnerCodeEspay)
//            fragment.arguments = bundle
//            switchFragment(fragment, "", "", true, "")
            generatingURL()
        }

    }

    private fun generatingURL() {
        try {
            showProgressDialog()
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GENERATE_URL, commCodeEspay + memberCodeEspay)
            params[WebParams.COMM_ID] = commIDLogin
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.CANVASSER_ID] = userPhoneID
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            Timber.d("params generate url bat:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GENERATE_URL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response generate url bat:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        val url = response.getString(WebParams.URL)
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        startActivity(browserIntent)
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
                                        alertDialogMaintenance.showDialogMaintenance(activity, model.error_message)
                                    }
                                    else -> {
                                        Timber.d("isi error generate url bat:$response")
                                        Toast.makeText(activity, code_msg, Toast.LENGTH_LONG).show()
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
        } catch (e: java.lang.Exception) {
            Timber.d("httpclient:%s", e.message)
        }
    }


    private fun initializeListPO() {

        listPOAdapter = ListPOAdapter(docListArrayList, activity, this)
        recyclerViewList.adapter = listPOAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val bundle = arguments!!

        docListArrayList.clear()

        if (bundle.getString(WebParams.DOC_LIST) != "") {
            val mArrayDoc = JSONArray(bundle.getString(WebParams.DOC_LIST))

            for (i in 0 until mArrayDoc.length()) {
                val docNo = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_NO)
                val docStatus = mArrayDoc.getJSONObject(i).getString(WebParams.DOC_STATUS)
                val nettAmount = mArrayDoc.getJSONObject(i).getString(WebParams.NETT_AMOUNT)
                val dueDate = mArrayDoc.getJSONObject(i).getString(WebParams.DUE_DATE)
                val custID = mArrayDoc.getJSONObject(i).getString(WebParams.CUST_ID)
                val memberCode = mArrayDoc.getJSONObject(i).getString(WebParams.MEMBER_CODE)
                val commCode = mArrayDoc.getJSONObject(i).getString(WebParams.COMM_CODE)
                val paidStatus = mArrayDoc.getJSONObject(i).getString(WebParams.PAID_STATUS_REMARK)
                val listPOModel = ListPOModel()
                listPOModel.doc_no = docNo
                listPOModel.doc_status = docStatus
                listPOModel.nett_amount = nettAmount
                listPOModel.due_date = dueDate
                listPOModel.cust_id = custID
                listPOModel.comm_code = commCode
                listPOModel.member_code = memberCode
                listPOModel.paid_status = paidStatus
                listPOModel.partner = partner
                docListArrayList.add(listPOModel)
            }
        }

        listPOAdapter!!.updateData(docListArrayList)
    }

    override fun onClick(item: ListPOModel?) {
        val bundle = Bundle()
        bundle.putString(DefineValue.DOC_NO, item!!.doc_no)
        bundle.putString(DefineValue.MEMBER_CODE_ESPAY, item!!.member_code)
        bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, item!!.comm_code)
        bundle.putString(DefineValue.TYPE, DefineValue.CANVASSER)
        bundle.putString(DefineValue.PARTNER_CODE_ESPAY, partnerCodeEspay)

        val fragment = FragPurchaseOrderDetail()
        fragment.arguments = bundle
        switchFragment(fragment, "", "", true, "")
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserPOActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }
}