package com.sgo.saldomu.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.ListPOAdapter
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
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
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListPurchaseOrder : BaseFragment() {

    private val docTypeID = "PO"
    var memberCode: String? = null
    var commCode: String? = null

    var itemList = ArrayList<ListPOModel>()
    var itemListAdapter: ListPOAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_po, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.list_po))
        btn_create_po.visibility = View.VISIBLE
        btn_create_po.setOnClickListener {
            val fragment = FragListItemToko()
            val bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE, memberCode)
            bundle.putString(DefineValue.COMMUNITY_CODE, commCode)
            fragment.arguments = bundle
            tokoPurchaseOrderActivity.switchContent(fragment, getString(R.string.choose_catalog), true, "FragListItemToko")
        }

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE, "")
        }

        itemListAdapter = ListPOAdapter(itemList, activity) {
            val fragment = FragPurchaseOrderDetail()
            val bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE, memberCode)
            bundle.putString(DefineValue.COMMUNITY_CODE, commCode)
            bundle.putString(DefineValue.DOC_NO, it.doc_no)
            fragment.arguments = bundle
            tokoPurchaseOrderActivity.switchContent(fragment, getString(R.string.detail_document), true, "FragPurchaseOrderDetail")
        }
        recyclerViewList.adapter = itemListAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerViewList)

        getPOList()

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                itemListAdapter!!.filter.filter(editable.toString())
            }
        })
    }

    private fun getPOList() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_LIST_PO, memberCode + commCode)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCode
        params[WebParams.CUST_ID_ESPAY] = userPhoneID

        Timber.d("isi params get $docTypeID list:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_LIST_PO, params,
                object : ObjListeners {
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                val jsonArray = response.getJSONArray(WebParams.DOC_LIST)
                                itemList.clear()
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val docNo = jsonObject.getString(WebParams.DOC_NO)
                                    val docStatus = jsonObject.getString(WebParams.DOC_STATUS)
                                    val totalAmount = jsonObject.getString(WebParams.TOTAL_AMOUNT)
                                    val dueDate = jsonObject.getString(WebParams.DUE_DATE)
                                    val custID = jsonObject.getString(WebParams.CUST_ID)
                                    val memberCode = jsonObject.getString(WebParams.MEMBER_CODE)
                                    val commCode = jsonObject.getString(WebParams.COMM_CODE)
                                    val paidStatus = jsonObject.getString(WebParams.PAID_STATUS)
                                    val listPOModel = ListPOModel()
                                    listPOModel.doc_no = docNo
                                    listPOModel.doc_status = docStatus
                                    listPOModel.total_amount = totalAmount
                                    listPOModel.due_date = dueDate
                                    listPOModel.cust_id = custID
                                    listPOModel.member_code = memberCode
                                    listPOModel.comm_code = commCode
                                    listPOModel.paid_status = paidStatus
                                    itemList.add(listPOModel)
                                }
                                itemListAdapter!!.notifyDataSetChanged()
                            }
                            WebParams.LOGOUT_CODE -> {
                                AlertDialogLogout.getInstance().showDialoginActivity(activity, message)
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

    private fun docDetail() {
        Toast.makeText(context, "Belummm", Toast.LENGTH_SHORT).show()
    }
}