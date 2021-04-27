package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.coreclass.CustomSecurePref
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.AnchorListItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_notification.*
import kotlinx.android.synthetic.main.frag_input_store_code.*
import org.json.JSONObject
import timber.log.Timber

class FragJoinCommunityToko : BaseFragment() {

    val anchorList = ArrayList<AnchorListItem>()
    var anchorCodeEspay = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_input_store_code, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sp = CustomSecurePref.getInstance().getmSecurePrefs()

        val tokoEBDActivity = activity as TokoEBDActivity
        tokoEBDActivity.initializeToolbar(getString(R.string.join_community))

        iv_clear.setOnClickListener { et_store_code.setText("") }
        btn_submit.setOnClickListener {
            if (inputValidation())
                submitRegMember()
        }
        getListAnchor()
    }

    private fun inputValidation(): Boolean {
        if (et_store_code.text.isEmpty()) {
            et_store_code.requestFocus()
            et_store_code.error = getString(R.string.store_code_validation)
            return false
        }
        return true
    }

    private fun getListAnchor() {
        showProgressDialog()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_ANCHOR)
        params[WebParams.USER_ID] = userPhoneID
        Timber.d("isi params list anchor:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_ANCHOR, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val jsonArray = response.getJSONArray(WebParams.ANCHOR_LIST)
                for (i in 0 until jsonArray.length()) {
                    val anchorListItem = getGson().fromJson(jsonArray.getJSONObject(i).toString(), AnchorListItem::class.java)
                    anchorList.add(anchorListItem)
                }
                layout_anchor.visibility = View.VISIBLE
                val anchorListOption = ArrayList<String>()
                for (i in anchorList.indices) {
                    anchorListOption.add(anchorList[i].anchorName)
                }
                val anchorOptionsAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, anchorListOption)
                anchorOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_anchor.adapter = anchorOptionsAdapter
                spinner_anchor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        anchorCodeEspay = anchorList[p2].anchorCode
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

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

    private fun submitRegMember() {
        showProgressDialog()
        val memberCode = et_store_code.text.toString()
        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_REGISTER_EBD, memberCode)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.MEMBER_CODE] = memberCode
        params[WebParams.LATITUDE] = sp.getDouble(DefineValue.LATITUDE_UPDATED, 0.0)
        params[WebParams.LONGITUDE] = sp.getDouble(DefineValue.LONGITUDE_UPDATED, 0.0)
        params[WebParams.ANCHOR_CODE_ESPAY] = anchorCodeEspay
        Timber.d("isi params register community:%s", params.toString())

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_REGISTER_EBD, params, object : ObjListeners {
            override fun onResponses(response: JSONObject) {
                val code = response.getString(WebParams.ERROR_CODE)
                val message = response.getString(WebParams.ERROR_MESSAGE)
                when (code) {
                    WebParams.SUCCESS_CODE -> {
                        showDialog()
                    }
                    WebParams.LOGOUT_CODE -> {
                        val alertDialogLogout = AlertDialogLogout.getInstance()
                        alertDialogLogout.showDialoginActivity(activity, message)
                    }
                    DefineValue.ERROR_9333 -> {
                        val model = gson.fromJson(response.toString(), jsonModel::class.java)
                        val appModel = model.app_data
                        val alertDialogUpdateApp = AlertDialogUpdateApp.getInstance()
                        alertDialogUpdateApp.showDialogUpdate(activity, appModel.type, appModel.packageName, appModel.downloadUrl)
                    }
                    DefineValue.ERROR_0066 -> {
                        val alertDialogMaintenance = AlertDialogMaintenance.getInstance()
                        alertDialogMaintenance.showDialogMaintenance(activity, message)
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
    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        dialog.title_dialog.text = resources.getString(R.string.remark)
        dialog.message_dialog.visibility = View.VISIBLE
        dialog.message_dialog.text = getString(R.string.join_community) + " " + getString(R.string.success)

        dialog.btn_dialog_notification_ok.setOnClickListener {
            dialog.dismiss()
            requireFragmentManager().popBackStack()
        }
        dialog.show()
    }
}