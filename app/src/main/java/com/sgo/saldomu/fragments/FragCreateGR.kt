package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.CanvasserGoodReceiptActivity
import com.sgo.saldomu.adapter.AdapterListBonusItem
import com.sgo.saldomu.adapter.AdapterListItemConfirmGR
import com.sgo.saldomu.coreclass.CurrencyFormat
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
import kotlinx.android.synthetic.main.frag_confirm_gr.*
import kotlinx.android.synthetic.main.frag_create_gr.*
import kotlinx.android.synthetic.main.frag_create_gr.frag_gr_confirm_submit_btn
import kotlinx.android.synthetic.main.frag_list_po.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class FragCreateGR : BaseFragment(), AdapterListItemConfirmGR.ListItemConfirmGRListener {
    var memberCodeEspay: String = ""
    var commCodeEspay: String = ""
    var custIdEspay: String = ""
    var docNo: String = ""
    var amount: String = ""
    var docDetails: String = ""
    var promoCode: String = ""
    var isHaveBonusItem: Boolean = false

    private val itemArrayList = ArrayList<ItemModel>()
    private val itemBonusArrayList = ArrayList<ItemModel>()

    private var adapterListItemConfirmGR: AdapterListItemConfirmGR? = null
    private var adapterListBonusItem: AdapterListBonusItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_create_gr, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var bundle = arguments!!
        memberCodeEspay = bundle!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
        commCodeEspay = bundle!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
        custIdEspay = bundle!!.getString(DefineValue.CUST_ID_ESPAY, "")
        docNo = bundle!!.getString(DefineValue.DOC_NO, "")
        amount = bundle!!.getString(DefineValue.AMOUNT, "")
        promoCode = bundle!!.getString(DefineValue.PROMO_CODE, "")
        docDetails = bundle!!.getString(DefineValue.DOC_DETAILS, "")

        initalizeListItem()

        if (isHaveBonusItem == true) {
            initializeListBonusItem()
        }

        frag_gr_confirm_store_code.setText(memberCodeEspay)
        frag_gr_confirm_comm_code.setText(commCodeEspay)
        frag_gr_confirm_amount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(amount))
        frag_gr_confirm_discount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.TOTAL_DISC)))
        frag_gr_confirm_total_amount.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(bundle!!.getString(DefineValue.TOTAL_AMOUNT)))

        frag_gr_confirm_submit_btn.setOnClickListener { createGR() }
    }

    fun initalizeListItem() {
        adapterListItemConfirmGR = AdapterListItemConfirmGR(activity, itemArrayList, this)
        frag_gr_confirm_item_list_field.adapter = adapterListItemConfirmGR
        frag_gr_confirm_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val docDetailsJsonArray = JSONArray(docDetails)

        for (i in 0 until docDetailsJsonArray.length()) {
            val mappingItemJsonArray = docDetailsJsonArray.getJSONObject(i).getJSONArray(WebParams.MAPPING_ITEMS)
            if (docDetailsJsonArray.getJSONObject(i).getJSONArray(WebParams.BONUS_ITEMS).length()!=0) {
                layout_bonus_item.visibility = View.VISIBLE
                isHaveBonusItem = true
            }
            for (i in 0 until mappingItemJsonArray.length()) {
                val itemName = mappingItemJsonArray.getJSONObject(i).getString(WebParams.ITEM_NAME)
                val itemCode = mappingItemJsonArray.getJSONObject(i).getString(WebParams.ITEM_CODE)
                val price = mappingItemJsonArray.getJSONObject(i).getString(WebParams.PRICE)
                val unit = mappingItemJsonArray.getJSONObject(i).getString(WebParams.UNIT)
                val subtotal = mappingItemJsonArray.getJSONObject(i).getString(WebParams.SUBTOTAL_AMOUNT)
                val formatQtyJsonArray = mappingItemJsonArray.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
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
                itemModel.subtotal_amount = subtotal
                itemModel.format_qty = formatQtys
                itemArrayList.add(itemModel)
            }
        }


        adapterListItemConfirmGR!!.updateData(itemArrayList)
    }

    fun initializeListBonusItem()
    {

        itemArrayList.clear()
        adapterListBonusItem = AdapterListBonusItem(activity, itemBonusArrayList, this)
        frag_gr_confirm_bonus_item_list_field.adapter = adapterListBonusItem
        frag_gr_confirm_bonus_item_list_field.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val docDetailsJsonArray = JSONArray(docDetails)

        for (i in 0 until docDetailsJsonArray.length()) {
            val mappingBonusItemJsonArray = docDetailsJsonArray.getJSONObject(i).getJSONArray(WebParams.BONUS_ITEMS)
            for (i in 0 until mappingBonusItemJsonArray.length()) {
                val itemName = mappingBonusItemJsonArray.getJSONObject(i).getString(WebParams.ITEM_NAME)
                val itemCode = mappingBonusItemJsonArray.getJSONObject(i).getString(WebParams.ITEM_CODE)
                val price = mappingBonusItemJsonArray.getJSONObject(i).getString(WebParams.PRICE)
                val unit = mappingBonusItemJsonArray.getJSONObject(i).getString(WebParams.UNIT)
                val subtotal = mappingBonusItemJsonArray.getJSONObject(i).getString(WebParams.SUBTOTAL_AMOUNT)
                val formatQtyJsonArray = mappingBonusItemJsonArray.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
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
                itemModel.subtotal_amount = subtotal
                itemModel.format_qty = formatQtys
                itemBonusArrayList.add(itemModel)
            }
        }
        adapterListBonusItem!!.updateData(itemBonusArrayList)
    }


    override fun onClick(item: ItemModel?) {
        TODO("Not yet implemented")
    }

    fun createGR() {
        try {
            showProgressDialog()
            extraSignature = memberCodeEspay + commCodeEspay + amount
            val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CREATE_GR, extraSignature)
            params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
            params[WebParams.MEMBER_CODE_ESPAY] = memberCodeEspay
            params[WebParams.CUST_ID_ESPAY] = custIdEspay
            params[WebParams.CUST_ID] = userPhoneID
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.DOC_NO] = ""
            params[WebParams.REFF_NO] = docNo
            params[WebParams.INVOICE_NOTE] = ""
            params[WebParams.NOTES_NO] = ""
            params[WebParams.NOTES_ID] = ""
            params[WebParams.TYPE_ID] = DefineValue.GR
            params[WebParams.CUST_TYPE] = DefineValue.CANVASSER
            params[WebParams.DOC_DETAIL] = docDetails
            params[WebParams.CCY_ID] = MyApiClient.CCY_VALUE
            params[WebParams.AMOUNT] = amount
            params[WebParams.PROMO] = promoCode
            params[WebParams.ACTION_CODE] = "N"
            Timber.d("params create GR:$params")
            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CREATE_GR, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject) {
                            try {
                                val gson = Gson()
                                val model = gson.fromJson(response.toString(), jsonModel::class.java)
                                val code = response.getString(WebParams.ERROR_CODE)
                                val code_msg = response.getString(WebParams.ERROR_MESSAGE)
                                Timber.d("isi response create GR:$response")
                                when (code) {
                                    WebParams.SUCCESS_CODE -> {
                                        var bundle = arguments!!
                                        bundle.putString(DefineValue.TX_ID, response.getString(WebParams.TX_ID))
                                        val frag: Fragment = FragConfirmCreateGR()
                                        frag.arguments = bundle
                                        switchFragment(frag, "", "", true, "")
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
                                        Timber.d("isi error create GR:$response")
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


    private fun switchFragment(i: Fragment, name: String, next_name: String, isBackstack: Boolean, tag: String) {
        if (activity == null) return
        val fca = activity as CanvasserGoodReceiptActivity?
        fca!!.switchContent(i, name, next_name, isBackstack, tag)
    }
}
