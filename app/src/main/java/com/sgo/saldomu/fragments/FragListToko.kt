package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.adapter.AdapterEBDCommunityList
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.EBDCommunityModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list_item.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListToko : BaseFragment() {

    private val list = ArrayList<EBDCommunityModel>()
    private var adapter: AdapterEBDCommunityList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list_item, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        list.clear()
        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.store_list))

        adapter = AdapterEBDCommunityList(requireContext(), list, object : AdapterEBDCommunityList.OnClick {
            override fun onClick(pos: Int) {
                val fragment = FragListPurchaseOrder()
                val bundle = Bundle()
                bundle.putString(DefineValue.MEMBER_CODE_ESPAY, list[pos].member_code)
                bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, list[pos].comm_code)
                fragment.arguments = bundle
                tokoEBDActivity.switchContent(fragment, getString(R.string.list_po), true, "")
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        getList()
    }

    private fun getList() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_TOKO)
        params[WebParams.CUST_ID] = userPhoneID

        Timber.d("isi params get list toko:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_TOKO, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
//                        val jsonArray = response.getJSONArray(WebParams.MEMBER_DETAILS)
//                        for (i in 0 until jsonArray.length()) {
//                            val jsonObject = jsonArray.getJSONObject(i)
//                            val memberCode = jsonObject.getString(WebParams.MEMBER_CODE)
//                            val custID = jsonObject.getString(WebParams.CUST_ID)
//                            val custName = jsonObject.getString(WebParams.CUST_NAME)
//                            val commCode = jsonObject.getString(WebParams.COMM_CODE)
//                            val commName = jsonObject.getString(WebParams.COMM_NAME)
//                            val status = jsonObject.getString(WebParams.STATUS)
//                            val mobilePhoneNo = jsonObject.getString(WebParams.MOBILE_PHONE_NO)
//                            val email = jsonObject.getString(WebParams.EMAIL)
//                            list.add(EBDCommunityModel(memberCode, custID, custName, commCode, commName, status, mobilePhoneNo, email))
//                        }
//                        adapter!!.notifyDataSetChanged()
                    }
                    WebParams.LOGOUT_CODE -> {
                        AlertDialogLogout.getInstance().showDialoginMain(activity, message)
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


}