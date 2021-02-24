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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.adapter.AdapterListAddItemGRCanvasser
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.*
import com.sgo.saldomu.models.retrofit.ItemModel
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.fragment_input_item_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragListAddItemGRCanvasser : BaseFragment() {
    var memberCodeEspay = ""
    var commCodeEspay = ""
    var custIdEspay = ""
    var docNo = ""
    var docDetails = ""

    val itemList = ArrayList<EBDCatalogModel>()
    private val order = DocDetailsItem()
    private val mappingItemList = ArrayList<MappingItemsItem>()
    var itemListAdapter: AdapterListAddItemGRCanvasser? = null
    var tempIdHashSet = HashSet<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input_item_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val canvasserGoodReceiptActivity = activity as CanvasserGoodReceiptActivity
        canvasserGoodReceiptActivity.initializeToolbar(getString(R.string.choose_catalog))
        if (arguments != null) {
            memberCodeEspay = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay = arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            custIdEspay = arguments!!.getString(DefineValue.CUST_ID_ESPAY, "")
            docDetails = arguments!!.getString(DefineValue.DOC_DETAILS, "")
            docNo = arguments!!.getString(DefineValue.DOC_NO, "")
        }

        itemListAdapter = AdapterListAddItemGRCanvasser(context!!, object : AdapterListAddItemGRCanvasser.Listener {
            override fun onChangeQty(itemCode: String, qty: Int, qtyType: String) {

                for (i in itemList.indices) {
                    if (itemCode == itemList[i].itemCode) {
                        val itemModel = MappingItemsItem()
                        itemModel.item_code = itemList[i].itemCode
                        itemModel.item_name = itemList[i].itemName
                        itemModel.price = itemList[i].price
                        itemModel.unit = itemList[i].unit
                        val formatQtys = ArrayList<FormatQtyItem>()

                        when {
                            qtyType == DefineValue.BAL -> formatQtys.add(0, FormatQtyItem(DefineValue.BAL, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtys.add(0, FormatQtyItem(DefineValue.BAL, itemList[i].formatQtyItem[0].mapping_qty))
                            else -> formatQtys.add(0, FormatQtyItem(DefineValue.BAL, 0))
                        }

                        when {
                            qtyType == DefineValue.SLOP -> formatQtys.add(1, FormatQtyItem(DefineValue.SLOP, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtys.add(1, FormatQtyItem(DefineValue.SLOP, itemList[i].formatQtyItem[1].mapping_qty))
                            else -> formatQtys.add(1, FormatQtyItem(DefineValue.SLOP, 0))
                        }

                        when {
                            qtyType == DefineValue.PACK -> formatQtys.add(2, FormatQtyItem(DefineValue.PACK, qty))
                            itemList[i].formatQtyItem.isNotEmpty() -> formatQtys.add(2, FormatQtyItem(DefineValue.PACK, itemList[i].formatQtyItem[2].mapping_qty))
                            else -> formatQtys.add(2, FormatQtyItem(DefineValue.PACK, 0))
                        }

                        val qtyBAL = formatQtys[0].mapping_qty
                        val qtySLOP = formatQtys[1].mapping_qty
                        val qtyPACK = formatQtys[2].mapping_qty
                        itemModel.format_qty = formatQtys

//                        if (qtyBAL == 0 && qtySLOP == 0 && qtyPACK == 0) {
//                            itemList[i].formatQty.clear()
//                        } else {
//                            mappingItemList.add(mappingItemsItem)
                        itemList[i].formatQtyItem = formatQtys
//                        }
                    }
                }
            }
        })

        getCatalogList()

        frag_input_item_list_field.adapter = itemListAdapter
        frag_input_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                itemListAdapter!!.filter.filter(editable.toString())
            }
        })
        frag_input_item_submit_btn.setOnClickListener {
            val temp = ArrayList<HashMap<String, Any>>()

            temp.add(setMappingItemsHashMap())

            val gson = Gson()
            val tempGson = gson.toJson(temp)

            val bundle = Bundle()
            bundle.putString(DefineValue.COMMUNITY_CODE_ESPAY, commCodeEspay)
            bundle.putString(DefineValue.MEMBER_CODE_ESPAY, memberCodeEspay)
            bundle.putString(DefineValue.CUST_ID_ESPAY, custIdEspay)
            bundle.putString(DefineValue.DOC_DETAILS, tempGson)
            bundle.putString(DefineValue.DOC_NO, docNo)
            val frag: Fragment = FragInputPromoCodeGRCanvasser()
            frag.arguments = bundle
            switchFragment(frag, "", "", true, "")
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
                                        val selectedProductJsonArray = JSONArray(docDetails)
                                        for (i in 0 until selectedProductJsonArray.length()) {
                                            val jsonArray = selectedProductJsonArray.getJSONObject(i).getJSONArray(WebParams.MAPPING_ITEMS)
                                            for (j in 0 until jsonArray.length()) {
                                                val jsonObject = jsonArray.getJSONObject(j)
                                                val itemCode = jsonObject.getString(WebParams.ITEM_CODE)
                                                val itemName = jsonObject.getString(WebParams.ITEM_NAME)
                                                val price = jsonObject.getInt(WebParams.PRICE)
                                                val unit = jsonObject.getString(WebParams.UNIT)
                                                val description = jsonObject.getString(WebParams.ITEM_DESCRIPTION)
                                                val itemImage = jsonObject.getString(WebParams.IMAGE_URL)
                                                val discAmount = Integer.parseInt(jsonObject.getString(WebParams.DISC_AMOUNT))
                                                val nettPrice = Integer.parseInt(jsonObject.getString(WebParams.NETT_PRICE))
                                                val minQty = 0
                                                val maxQty = 0

                                                val remarkMappingUnit = jsonObject.getJSONArray(WebParams.REMARK_MAPPING_UNITS)
                                                val listRemarkMappingUnit = ArrayList<String>()
                                                for (j in 0 until remarkMappingUnit.length()) {
                                                    listRemarkMappingUnit.add(remarkMappingUnit[j].toString())
                                                }
                                                var isFavorite = false
                                                if (jsonObject.getString(WebParams.IS_FAVORITE) == DefineValue.Y)
                                                    isFavorite = true

                                                val formatQtyJsonArray = jsonObject.getJSONArray(WebParams.FORMAT_QTY)
                                                var formatQtys = ArrayList<FormatQtyItem>()
                                                for (i in 0 until formatQtyJsonArray.length()) {
                                                    var mappingUnit = formatQtyJsonArray.getJSONObject(i).getString(WebParams.MAPPING_UNIT)
                                                    var mappingQty = formatQtyJsonArray.getJSONObject(i).getInt(WebParams.MAPPING_QTY)
                                                    var formatQty = FormatQtyItem(mappingUnit, mappingQty)
                                                    formatQtys.add(formatQty)
                                                }

                                                itemList.add(EBDCatalogModel(itemImage, itemCode, itemName, description, price, discAmount, nettPrice, unit, minQty, maxQty, listRemarkMappingUnit, isFavorite, formatQtys))

                                                tempIdHashSet.add(itemCode)
                                            }
                                        }

                                        val jsonArray = response.getJSONArray(WebParams.ITEMS)
                                        for (i in 0 until jsonArray.length()) {
                                            val jsonObject = jsonArray.getJSONObject(i)
                                            val itemCode = jsonObject.getString(WebParams.ITEM_CODE)
                                            val itemName = jsonObject.getString(WebParams.ITEM_NAME)
                                            val price = jsonObject.getInt(WebParams.PRICE)
                                            val unit = jsonObject.getString(WebParams.UNIT)
                                            val minQty = jsonObject.getInt(WebParams.MIN_QTY)
                                            val maxQty = jsonObject.getInt(WebParams.MAX_QTY)
                                            val description = jsonObject.getString(WebParams.DESCRIPTION)
                                            val itemImage = jsonObject.getString(WebParams.IMAGE_URL)
                                            val discAmount = Integer.parseInt(jsonObject.getString(WebParams.DISC_AMOUNT))
                                            val nettPrice = Integer.parseInt(jsonObject.getString(WebParams.NETT_PRICE))

                                            val remarkMappingUnit = jsonObject.getJSONArray(WebParams.REMARK_MAPPING_UNITS)
                                            val listRemarkMappingUnit = ArrayList<String>()
                                            for (j in 0 until remarkMappingUnit.length()) {
                                                listRemarkMappingUnit.add(remarkMappingUnit[j].toString())
                                            }
                                            var isFavorite = false
                                            if (jsonObject.getString(WebParams.IS_FAVORITE) == DefineValue.Y)
                                                isFavorite = true

                                            if (!tempIdHashSet.contains(itemCode)) {
                                                itemList.add(EBDCatalogModel(itemImage, itemCode, itemName, description, price, discAmount, nettPrice, unit, minQty, maxQty, listRemarkMappingUnit, isFavorite))
                                            }
                                        }
                                        itemListAdapter!!.updateAdapter(itemList)
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

        val btnDialog: Button = dialog.findViewById(R.id.btn_dialog_notification_ok)
        val title: TextView = dialog.findViewById(R.id.title_dialog)
        val message: TextView = dialog.findViewById(R.id.message_dialog)
        message.visibility = View.VISIBLE
        title.text = getString(R.string.remark)
        message.text = msg
        btnDialog.setOnClickListener {
            dialog.dismiss()
            fragmentManager!!.popBackStack()
        }
        dialog.show()
    }

    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }

    fun setMappingItemsHashMap(): HashMap<String, Any> {
        val finalMappingItemsHashMap = HashMap<String, Any>()
        val mappingItemsArrayList = ArrayList<HashMap<String, Any>>()
        try {
            for (obj in itemList) {
                if (!obj.formatQtyItem.isEmpty()) {
                    var eligibleCount = 0

                    val mappingItemsHashMap = HashMap<String, Any>()
                    mappingItemsHashMap["item_name"] = obj.itemName
                    mappingItemsHashMap["item_code"] = obj.itemCode
                    mappingItemsHashMap["price"] = obj.price
                    mappingItemsHashMap["unit"] = obj.unit

                    val formatQtyArrayList = ArrayList<HashMap<String, Any>>()
                    for (formatQty in obj.formatQtyItem) {
                        val formatQtyHashMap = HashMap<String, Any>()
                        formatQtyHashMap["mapping_qty"] = formatQty.mapping_qty
                        formatQtyHashMap["mapping_unit"] = formatQty.mapping_unit
                        formatQtyArrayList.add(formatQtyHashMap)
                        if (formatQty.mapping_qty.equals(0)) {
                            eligibleCount++
                        }
                    }
                    if (eligibleCount < 3) {
                        mappingItemsHashMap["format_qty"] = formatQtyArrayList
                        mappingItemsArrayList.add(mappingItemsHashMap)
                    }
                }
            }
            finalMappingItemsHashMap.put("mapping_items", mappingItemsArrayList)
            finalMappingItemsHashMap.put("reff_no", docNo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return finalMappingItemsHashMap
    }

}