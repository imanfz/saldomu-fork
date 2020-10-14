package com.sgo.saldomu.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.gson.Gson
import com.sgo.saldomu.Beans.DenomListModel
import com.sgo.saldomu.Beans.DenomOrderListModel
import com.sgo.saldomu.Beans.SCADMCommunityModel
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.DenomSCADMActivity
import com.sgo.saldomu.adapter.DenomItemListAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.DataManager
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_denom_input_item_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class FragmentDenomInputItemList : BaseFragment(), DenomItemListAdapter.listener {

    var memberCode: String? = null
    var memberIdSACDM: String? = null

    var itemList: ArrayList<DenomListModel>? = null
    var itemListString: ArrayList<String?>? = null
    var itemListAdapter: DenomItemListAdapter? = null

    var obj: SCADMCommunityModel? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_denom_input_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null)
            memberCode = arguments!!.getString(WebParams.MEMBER_REMARK, "")

        Timber.d("isi bundle : "+arguments.toString())
        itemList = ArrayList()
        itemListString = ArrayList()
        itemListAdapter = DenomItemListAdapter(activity, itemList, this, false)

        frag_denom_input_item_list_field.adapter = itemListAdapter
        frag_denom_input_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(frag_denom_input_item_list_field)

        obj = DataManager.getInstance().sacdmCommMod
        memberIdSACDM = obj!!.member_id_scadm

        getDenomList()

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                itemListAdapter!!.filter.filter(editable.toString())
            }
        })
        frag_denom_input_item_submit_btn.setOnClickListener {
            if (checkInput()) {
                frag_denom_input_item_list_field.scrollTo(0, 0)
                val frag: Fragment = FragmentDenomConfirm()

                var originalString = et_promo_code.text.toString();
                var splittedString = originalString.split(",")
                var jsonArray = JSONArray(splittedString)

                val bundle = Bundle()
                bundle.putString(WebParams.BANK_NAME, arguments!!.getString(WebParams.BANK_NAME, ""))
                bundle.putString(WebParams.BANK_GATEWAY, arguments!!.getString(WebParams.BANK_GATEWAY, ""))
                bundle.putString(WebParams.BANK_CODE, arguments!!.getString(WebParams.BANK_CODE, ""))
                bundle.putString(WebParams.PRODUCT_CODE, arguments!!.getString(WebParams.PRODUCT_CODE, ""))
                bundle.putString(WebParams.MEMBER_REMARK, memberCode)
                bundle.putString(WebParams.STORE_NAME, arguments!!.getString(WebParams.STORE_NAME, ""))
                bundle.putString(WebParams.STORE_ADDRESS, arguments!!.getString(WebParams.STORE_ADDRESS, ""))
                bundle.putString(WebParams.PROMO_CODE, jsonArray.toString())
                if (arguments!!.getBoolean(DefineValue.IS_FAVORITE) == true) {
                    bundle.putBoolean(DefineValue.IS_FAVORITE, true)
                    bundle.putString(DefineValue.CUST_ID, arguments!!.getString(DefineValue.CUST_ID))
                    bundle.putString(DefineValue.NOTES, arguments!!.getString(DefineValue.NOTES))
                    bundle.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.B2B)
                    bundle.putString(DefineValue.PRODUCT_TYPE, DefineValue.DENOM_B2B)
                }

                frag.arguments = bundle

                addFragment(frag, DenomSCADMActivity.DENOM_PAYMENT)
            } else
                Toast.makeText(activity, "Daftar Denom Kosong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDenomList() {
        try {
            showProgressDialog()

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_DENOM_LIST, memberIdSACDM)

            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_REMARK] = memberCode
            params[WebParams.MEMBER_ID_SCADM] = memberIdSACDM

            Timber.d("isi params sent get denom list:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_DENOM_LIST, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                Timber.d("isi response get denom list:$response")
                                when (response.getString(WebParams.ERROR_CODE)) {
                                    WebParams.SUCCESS_CODE -> {
                                        if (itemList!!.size > 0) {
                                            itemList!!.clear()
                                            itemListString!!.clear()
                                        }
                                        val dataArr = response.getJSONArray("item")
                                        for (i in 0 until dataArr.length()) {
                                            val dataObj = dataArr.getJSONObject(i)
                                            val denomObj = DenomListModel(dataObj)
                                            itemListString!!.add(denomObj.itemName)
                                            itemList!!.add(denomObj)
                                        }
                                        itemListAdapter!!.notifyDataSetChanged()
                                    }
                                    WebParams.LOGOUT_CODE -> {
                                        Timber.d("isi response autologout:$response")
                                        val message = response.getString(WebParams.ERROR_MESSAGE)
                                        val test = AlertDialogLogout.getInstance()
                                        test.showDialoginActivity(activity, message)
                                    }
                                    DefineValue.ERROR_9333 -> {
                                        Timber.d("isi response app data:" + model.app_data)
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
                                        val msg = response.getString(WebParams.ERROR_MESSAGE)
                                        showDialog(msg)
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(throwable: Throwable) {
                            dismissProgressDialog()
                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }
                    })
        } catch (e: Exception) {
            e.printStackTrace();
            Timber.d("httpclient:" + e.message)
        }

    }

    private fun showDialog(msg: String) {
        // Create custom dialog object
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_notification)

        // set values for custom dialog components - text, image and button
        val btnDialogOTP: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val Title: TextView = dialog.findViewById(R.id.title_dialog)
        val Message: TextView = dialog.findViewById(R.id.message_dialog)
        Message.visibility = View.VISIBLE
        Title.text = getString(R.string.error)
        Message.text = msg
        btnDialogOTP.setOnClickListener {
            dialog.dismiss()
            activity!!.onBackPressed()
        }
        dialog.show()
    }

    private fun checkInput(): Boolean {
        if (!itemList.isNullOrEmpty()) {
            for (obj in itemList!!) {
                if (obj.orderList.size > 0) {
                    DataManager.getInstance().itemList = itemList
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(pos: Int) {

    }

    override fun onChangeQty(itemId: String, qty: String?) {
        val orderList = ArrayList<DenomOrderListModel>()
        orderList.add(DenomOrderListModel(memberCode, qty))
        for (i in itemList!!.indices) {
            if (itemId == itemList!![i].itemID) {
                if (!qty.equals(""))
                    itemList!![i].orderList = orderList
                else
                    if (itemList!![i].orderList.size > 0)
                        itemList!![i].orderList.removeAt(0)
            }
        }
    }

    override fun onDelete(pos: Int) {

    }
}