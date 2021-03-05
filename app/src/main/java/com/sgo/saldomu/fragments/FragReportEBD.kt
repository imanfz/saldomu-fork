package com.sgo.saldomu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.ReportEBDActivity
import com.sgo.saldomu.activities.ReportEBDListActivity
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
import kotlinx.android.synthetic.main.frag_input_store_code.*
import kotlinx.android.synthetic.main.frag_list_item.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragReportEBD : BaseFragment() {

    private val ebdCommunityModelArrayList = ArrayList<EBDCommunityModel>()
    private var adapterEBDCommunityList: AdapterEBDCommunityList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments!!.getString(DefineValue.EBD) == DefineValue.TOKO)
            v = inflater.inflate(R.layout.frag_list_item, container, false)
        if (arguments!!.getString(DefineValue.EBD) == DefineValue.CANVASSER)
            v = inflater.inflate(R.layout.frag_input_store_code, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ebdCommunityModelArrayList.clear()
        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val reportEBDActivity = activity as ReportEBDActivity
        reportEBDActivity.initializeToolbar(getString(R.string.menu_item_title_report_ebd))
        if (arguments!!.getString(DefineValue.EBD) == DefineValue.TOKO) {
            adapterEBDCommunityList = AdapterEBDCommunityList(context!!, ebdCommunityModelArrayList, object : AdapterEBDCommunityList.OnClick {
                override fun onClick(pos: Int) {
                    val intent = Intent(activity, ReportEBDListActivity::class.java)
                    intent.putExtra(DefineValue.MEMBER_CODE_ESPAY, ebdCommunityModelArrayList[pos].member_code)
                    intent.putExtra(DefineValue.COMMUNITY_CODE_ESPAY, ebdCommunityModelArrayList[pos].comm_code)
                    startActivity(intent)
                }
            })
            recyclerView.adapter = adapterEBDCommunityList
            recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            getListCommunity()
        }
        if (arguments!!.getString(DefineValue.EBD) == DefineValue.CANVASSER){
            btn_submit.setOnClickListener {
                val intent = Intent(activity, ReportEBDListActivity::class.java)
                intent.putExtra(DefineValue.MEMBER_CODE_ESPAY, et_store_code.text.toString())
                intent.putExtra(DefineValue.COMMUNITY_CODE_ESPAY, "")
                startActivity(intent)
            }
        }
    }

    private fun getListCommunity() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_COMMUNITY_EBD)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_PHONE] = userPhoneID
        params[WebParams.MEMBER_CODE] = ""

        Timber.d("isi params get list community edb:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_COMMUNITY_EBD, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        val jsonArray = response.getJSONArray(WebParams.MEMBER_DETAILS)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val memberCode = jsonObject.getString(WebParams.MEMBER_CODE)
                            val custID = jsonObject.getString(WebParams.CUST_ID)
                            val custName = jsonObject.getString(WebParams.CUST_NAME)
                            val shopName = jsonObject.getString(WebParams.SHOP_NAME)
                            val commCode = jsonObject.getString(WebParams.COMM_CODE)
                            val commName = jsonObject.getString(WebParams.COMM_NAME)
                            val status = jsonObject.getString(WebParams.STATUS)
                            val mobilePhoneNo = jsonObject.getString(WebParams.MOBILE_PHONE_NO)
                            val email = jsonObject.getString(WebParams.EMAIL)
                            ebdCommunityModelArrayList.add(EBDCommunityModel(memberCode, custID, custName, commCode, commName, status, mobilePhoneNo, shopName, "", email))
                        }
                        adapterEBDCommunityList!!.notifyDataSetChanged()
                    }
                    WebParams.LOGOUT_CODE -> {
                        AlertDialogLogout.getInstance().showDialoginActivity2(activity, message)
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