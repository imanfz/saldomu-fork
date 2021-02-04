package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.DocDetailActivity
import com.sgo.saldomu.adapter.AdapterListDetailPO
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.coreclass.WebParams
import com.sgo.saldomu.dialogs.AlertDialogLogout
import com.sgo.saldomu.dialogs.AlertDialogMaintenance
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.FormatQtyItem
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.retrofit.jsonModel
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_detail_po.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class FragDocDetail : BaseFragment() {
    var memberCode: String = ""
    var commCodeEspay: String = ""
    var commCode: String = ""
    var docNo: String = ""
    var txId: String = ""
    var productCode = ""
    var commId = ""
    private var adapterDetailPO: AdapterListDetailPO? = null
    private val itemList = ArrayList<MappingItemsItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_detail_po, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE_ESPAY, "")
            commCodeEspay= arguments!!.getString(DefineValue.COMMUNITY_CODE_ESPAY, "")
            docNo = arguments!!.getString(DefineValue.DOC_NO, "")
        }

        val docDetailActivity = activity as DocDetailActivity
        docDetailActivity.initializeToolbar(getString(R.string.detail_document))

        adapterDetailPO = AdapterListDetailPO(context!!, itemList)
        recyclerViewList.adapter = adapterDetailPO
        recyclerViewList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        getDocDetail()

        btn_payment.visibility = View.VISIBLE
        btn_payment.setText(getString(R.string.ok))
        btn_payment.setOnClickListener { (activity as DocDetailActivity).finish() }
    }

    private fun getDocDetail() {
        showProgressDialog()

        val params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_DOC_DETAIL, memberCode + commCodeEspay + docNo)
        params[WebParams.USER_ID] = userPhoneID
        params[WebParams.MEMBER_CODE_ESPAY] = memberCode
        params[WebParams.COMM_CODE_ESPAY] = commCodeEspay
        params[WebParams.CUST_ID_ESPAY] = userPhoneID
        params[WebParams.DOC_NO] = docNo

        Timber.d("isi params get doc detail paymentByToko:$params")
        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_DOC_DETAIL, params,
                object : ObjListeners {
                    @SuppressLint("SetTextI18n")
                    override fun onResponses(response: JSONObject) {
                        val code = response.getString(WebParams.ERROR_CODE)
                        val message = response.getString(WebParams.ERROR_MESSAGE)
                        when (code) {
                            WebParams.SUCCESS_CODE -> {
                                btn_payment.visibility = View.VISIBLE
                                tv_docNo.text = response.getString(WebParams.DOC_NO)
                                tv_totalAmount.text = getString(R.string.currency) + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_AMOUNT))
                                val mArrayItem = JSONArray(response.getString(WebParams.ITEMS))
                                for (i in 0 until mArrayItem.length()) {
                                    val itemName = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_NAME)
                                    val itemCode = mArrayItem.getJSONObject(i).getString(WebParams.ITEM_CODE)
                                    val price = mArrayItem.getJSONObject(i).getString(WebParams.PRICE)
                                    val unit = mArrayItem.getJSONObject(i).getString(WebParams.UNIT)
                                    val formatQtyJsonArray = mArrayItem.getJSONObject(i).getJSONArray(WebParams.FORMAT_QTY)
                                    val formatQtyItemList = ArrayList<FormatQtyItem>()
                                    for (j in 0 until formatQtyJsonArray.length()) {
                                        val mappingUnit = formatQtyJsonArray.getJSONObject(j).getString(WebParams.MAPPING_UNIT)
                                        val mappingQty = formatQtyJsonArray.getJSONObject(j).getInt(WebParams.MAPPING_QTY)
                                        val formatQtyItem = FormatQtyItem()
                                        formatQtyItem.mapping_unit = mappingUnit
                                        formatQtyItem.mapping_qty = mappingQty
                                        formatQtyItemList.add(formatQtyItem)
                                    }
                                    val mappingItemsItem = MappingItemsItem()
                                    mappingItemsItem.item_name = itemName
                                    mappingItemsItem.item_code = itemCode
                                    mappingItemsItem.price = price.toInt()
                                    mappingItemsItem.unit = unit
                                    mappingItemsItem.format_qty = formatQtyItemList
                                    itemList.add(mappingItemsItem)
                                }
                                adapterDetailPO!!.notifyDataSetChanged()
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