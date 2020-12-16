package com.sgo.saldomu.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.adapter.UpdateProductGoodReceiptAdapter
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.FormatQty
import com.sgo.saldomu.models.retrofit.ItemModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_list.*
import kotlinx.android.synthetic.main.frag_list_po.recyclerViewList
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FragInputQtyGoodReceipt : BaseFragment(), UpdateProductGoodReceiptAdapter.UpdateProductGoodReceiptListener {

    var memberCodeEspay: String = ""
    var commCodeEspay: String = ""
    var custIdEspay: String = ""
    var docNo: String = ""

    private val itemArrayList = ArrayList<ItemModel>()

    private var updateProductGoodReceiptAdapter: UpdateProductGoodReceiptAdapter? = null
            ;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_list, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments

        tv_item_list.visibility = View.VISIBLE
        btn_proses_gr.visibility = View.VISIBLE

        memberCodeEspay = bundle!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
        commCodeEspay = bundle!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
        custIdEspay = bundle!!.getString(DefineValue.CUST_ID_ESPAY, "")
        docNo = bundle!!.getString(DefineValue.DOC_NO, "")

        getDetail()

        btn_proses_gr.setOnClickListener {
            confirmDocument()
        }
    }

    private fun getDetail() {
        try {
            showProgressDialog()
            extraSignature = memberCodeEspay + commCodeEspay + docNo
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, extraSignature)
            params[WebParams.DOC_NO] = docNo
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.USER_ID] = userPhoneID
            Timber.d("params inquiry doc detail:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response inquiry doc detail:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        val items = response.optString(WebParams.ITEMS)
                                        itemArrayList.clear()
                                        initializeListProduct(items)
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
                                        Timber.d("isi error inquiry doc detail:$response")
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

    fun initializeListProduct(items: String) {
        updateProductGoodReceiptAdapter = UpdateProductGoodReceiptAdapter(activity, itemArrayList, this)
        recyclerViewList.adapter = updateProductGoodReceiptAdapter
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val mArrayItem = JSONArray(items)

        for (i in 0 until mArrayItem.length()) {
            val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
            val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
            val price = mArrayItem.getJSONObject(i).getString(WebParams.PRICE)
            val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
            val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
            var formatQtys = ArrayList<FormatQty>()
            for (i in 0 until formatQtyJsonArray.length()) {
                var mappingUnit = formatQtyJsonArray.getJSONObject(i).getString(WebParams.MAPPING_UNIT)
                var mappingQty = formatQtyJsonArray.getJSONObject(i).getInt(WebParams.MAPPING_QTY)
                var formatQty = FormatQty()
                formatQty.mapping_unit = mappingUnit;
                formatQty.mapping_qty = mappingQty;
                formatQtys.add(formatQty)
            }
            val itemModel = ItemModel()
            itemModel.item_name = itemName
            itemModel.item_code = itemCode
            itemModel.price = price
            itemModel.unit = unit
            itemModel.format_qty = formatQtys
            itemArrayList.add(itemModel)
        }

        updateProductGoodReceiptAdapter!!.updateData(itemArrayList)
    }

    fun getDocDetail(temp: ArrayList<ItemModel>): JSONArray? {
        val jsonArray = JSONArray()
        try {
            for (obj in itemArrayList) {
                val jsonObject = JSONObject()
                jsonObject.put("item_name", obj.item_name)
                jsonObject.put("item_code", obj.item_code)
                jsonObject.put("price", obj.price)
                jsonObject.put("unit", obj.unit)

                val formatQtyJsonArray = JSONArray()
                for (objs in obj.format_qty!!) {
                    Log.d("leot", objs.toString())
                    var maps = HashMap<String, Object>()
//                    maps.put(WebParams.MAPPING_UNIT, objs.mapping_unit)
//                    maps.put(WebParams.MAPPING_QTY, objs.mapping_qty)
//                    formatQtyJsonArray.put(formatQtyJsonObject)
                    Log.d("leot", formatQtyJsonArray.toString())
                }

                jsonObject.put("format_qty", formatQtyJsonArray)

                jsonArray.put(jsonObject)

                temp.add(obj)
            }
        } catch (e: JSONException) {
            Log.d("leot", e.localizedMessage)
            e.printStackTrace()
        }
        return jsonArray
    }

    private fun confirmDocument() {
        try {
            showProgressDialog()

            val temp: ArrayList<ItemModel> = ArrayList()

            val docArrayList: JSONArray? = getDocDetail(temp)

            extraSignature = memberCodeEspay + custIdEspay
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_CONFIRM, extraSignature)
            params[WebParams.DOC_NO] = docNo
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.TYPE_ID] = DefineValue.GR
            params[WebParams.DOC_DETAIL] = docArrayList
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
            Timber.d("params confirm doc:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_CONFIRM, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response confirm doc:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {

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
                                        Timber.d("isi error confirm doc:$response")
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

    override fun onClick(item: ItemModel?) {
        TODO("Not yet implemented")
    }
}