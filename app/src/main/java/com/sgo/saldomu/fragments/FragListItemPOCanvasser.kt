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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserPOActivity
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.adapter.AdapterEBDCatalogList
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.DocDetailsItem
import com.sgo.saldomu.models.EBDCatalogModel
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.dialog_biller_confirm.*
import kotlinx.android.synthetic.main.fragment_input_item_list.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragListItemPOCanvasser : BaseFragment() {
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""

    val itemList = ArrayList<EBDCatalogModel>()
    private val order = DocDetailsItem()
    private val mappingItemList = ArrayList<MappingItemsItem>()
    var itemListAdapter: AdapterEBDCatalogList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input_item_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val canvasserPOActivity = activity as CanvasserPOActivity
        canvasserPOActivity.initializeToolbar(getString(R.string.choose_catalog))
        if (arguments != null) {
            memberCodeEspay = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            custIdEspay = arguments!!.getString(DefineValue.CUST_ID_ESPAY, "")
        }

        getCatalogList()

        itemListAdapter = AdapterEBDCatalogList(context!!, itemList, object : AdapterEBDCatalogList.Listener {
            override fun onChangeQty(itemCode: String, qty: Int, qtyType: String) {

                for (i in itemList.indices) {
                    if (itemCode == itemList[i].itemCode) {
                        val mappingItemsItem = MappingItemsItem()
                        mappingItemsItem.item_code = itemList[i].itemCode
                        mappingItemsItem.item_name = itemList[i].itemName
                        mappingItemsItem.price = itemList[i].price
                        mappingItemsItem.unit = itemList[i].unit
                        val formatQtyItemList = ArrayList<FormatQtyItem>()

                        when {
                            qtyType == DefineValue.BAL -> formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, itemList[i].formatQtyItem[0].mapping_qty))
                            else -> formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, 0))
                        }

                        when {
                            qtyType == DefineValue.SLOP -> formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, itemList[i].formatQtyItem[1].mapping_qty))
                            else -> formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, 0))
                        }

                        when {
                            qtyType == DefineValue.PACK -> formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, itemList[i].formatQtyItem[2].mapping_qty))
                            else -> formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, 0))
                        }

                        val qtyBAL = formatQtyItemList[0].mapping_qty
                        val qtySLOP = formatQtyItemList[1].mapping_qty
                        val qtyPACK = formatQtyItemList[2].mapping_qty
                        mappingItemsItem.format_qty = formatQtyItemList


                        if (qtyBAL == 0 && qtySLOP == 0 && qtyPACK == 0) {
                            itemList[i].formatQtyItem.clear()
                        } else {
                            mappingItemList.add(mappingItemsItem)
                            itemList[i].formatQtyItem = formatQtyItemList
                        }


//                        for (j in mappingItemList.indices) {
//                            if (mappingItemList[j].item_code == itemCode) {
//                                val mappingItemFormatQty = mappingItemList[j].format_qty
//                                val itemListFormatQty = itemList[i].formatQtyItem
//                                if (qty != 0) {
//                                    when (qtyType) {
//                                        DefineValue.BAL -> {
//                                            mappingItemFormatQty[0].mapping_qty = qty
//                                            itemListFormatQty[0].mapping_qty = qty
//                                        }
//                                        DefineValue.SLOP -> {
//                                            mappingItemFormatQty[1].mapping_qty = qty
//                                            itemListFormatQty[1].mapping_qty = qty
//                                        }
//                                        DefineValue.PACK -> {
//                                            mappingItemFormatQty[2].mapping_qty = qty
//                                            itemListFormatQty[2].mapping_qty = qty
//                                        }
//                                    }
//                                } else
//                                    mappingItemList.removeAt(j)
//                                break
//                            }
//
//                        }
                    }
                }
            }
        })

        frag_input_item_list_field.adapter = itemListAdapter
        frag_input_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(frag_input_item_list_field)

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                itemListAdapter!!.filter.filter(editable.toString())
            }
        })
        frag_input_item_submit_btn.setOnClickListener {
//            confirmationDoc()
        }

    }

    private fun addOrder(itemCode: String, itemName: String, price: Int, qty: Int, unit: String, qtyType: String) {
        if (qty != 0) {
            val mappingItem = MappingItemsItem()
            mappingItem.item_code = itemCode
            mappingItem.item_name = itemName
            mappingItem.price = price
            mappingItem.unit = unit
            val formatQtyItemList = ArrayList<FormatQtyItem>()
            formatQtyItemList.add(0, FormatQtyItem(DefineValue.BAL, 0))
            formatQtyItemList.add(1, FormatQtyItem(DefineValue.SLOP, 0))
            formatQtyItemList.add(2, FormatQtyItem(DefineValue.PACK, 0))
            when (qtyType) {
                DefineValue.BAL -> formatQtyItemList[0].mapping_qty = qty
                DefineValue.SLOP -> formatQtyItemList[1].mapping_qty = qty
                DefineValue.PACK -> formatQtyItemList[2].mapping_qty = qty
            }
            mappingItem.format_qty = formatQtyItemList
            mappingItemList.add(mappingItem)
            order.reff_no = ""
            order.mapping_items = mappingItemList
        }
    }

    private fun getCatalogList() {
        try {
            showProgressDialog()

            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_GET_LIST_CATALOG_EBD, memberCodeEspay + commCodeEspay)
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay

            Timber.d("isi params get catalog list:$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_GET_LIST_CATALOG_EBD, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val code = response.getString(WebParams.ERROR_CODE)
                                val message = response.getString(WebParams.ERROR_MESSAGE)
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        val jsonArray = response.getJSONArray(WebParams.ITEMS)
                                        for (i in 0 until jsonArray.length()) {
                                            val jsonObject = jsonArray.getJSONObject(i)
                                            val itemCode = jsonObject.getString(WebParams.ITEM_CODE)
                                            val itemName = jsonObject.getString(WebParams.ITEM_NAME)
                                            val price = jsonObject.getInt(WebParams.PRICE)
                                            val unit = jsonObject.getString(WebParams.UNIT)
                                            val minQty = jsonObject.getInt(WebParams.MIN_QTY)
                                            val maxQty = jsonObject.getInt(WebParams.MAX_QTY)
                                            itemList.add(EBDCatalogModel(itemCode, itemName, price, unit, minQty, maxQty))
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
                                        showDialog(message)
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
            Timber.d("httpclient:%s", e.message)
        }
    }

    private fun showDialog(msg: String) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_notification)

        val btnDialogOTP: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.error)
        message.text = msg
        btnDialogOTP.setOnClickListener {
            dialog.dismiss()
            fragmentManager!!.popBackStack()
        }
        dialog.show()
    }
}