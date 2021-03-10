package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.adapter.AdapterListToko
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.MemberListItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_list_item.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragListToko : BaseFragment() {

    private val list = ArrayList<MemberListItem>()
    private var adapter: AdapterListToko? = null

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

        adapter = AdapterListToko(requireContext(), list, object : AdapterListToko.OnClick {
            override fun onClick(pos: Int) {
                detail(list[pos].regId)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        getList()
    }

    private fun getList() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_TOKO, userPhoneID)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID] = userPhoneID

        Timber.d("isi params get list toko:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_TOKO, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        val jsonArray = response.getJSONArray(WebParams.MEMBER_LIST)
                        for (i in 0 until jsonArray.length()) {
                            val memberListItem = getGson().fromJson(jsonArray.getJSONObject(i).toString(), MemberListItem::class.java)
                            list.add(memberListItem)
                        }
                        adapter!!.notifyDataSetChanged()
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

    private fun detail(regID: String) {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_DETAIL_TOKO, userPhoneID + regID)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID] = userPhoneID
        params[WebParams.REG_ID] = regID

        Timber.d("isi params get detail toko:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_DETAIL_TOKO, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        showDialog(response)
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

    @SuppressLint("SetTextI18n")
    private fun showDialog(response: JSONObject) {
        val memberCode = response.getString(WebParams.MEMBER_CODE_ESPAY)
        val commCode = response.getString(WebParams.COMM_CODE_ESPAY)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.remark)
        dialog.message_dialog.visibility = View.VISIBLE
        if (memberCode != "" && commCode != "")
            dialog.message_dialog.text = getString(R.string.your_store_code) + " " + getString(R.string.titik_dua) + " " + memberCode + "\n" +
                    getString(R.string.your_community_code) + " " + getString(R.string.titik_dua) + " " + commCode
        else
//            dialog.message_dialog.text = getString(R.string.register_success_wait_for_verification)

            dialog.message_dialog.text = response.getString(WebParams.REMARK)
        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}