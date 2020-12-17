package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.TokoEBDActivity
import com.sgo.saldomu.activities.TokoPurchaseOrderActivity
import com.sgo.saldomu.coreclass.CurrencyFormat
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.models.EBDConfirmModel
import com.sgo.saldomu.models.MappingItemsItem
import com.sgo.saldomu.models.PaymentMethods
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_order_confirm_toko.*

class FragOrderConfirmToko : BaseFragment() {
    var memberCode = ""
    var commCode = ""
    var paymentOption = ""
    var paymentMethodCode = ""

    var ebdConfirmModel = EBDConfirmModel()

    private val mappingItemList = ArrayList<MappingItemsItem>()
    private val paymentMethodList = ArrayList<PaymentMethods>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_order_confirm_toko, container, false)
        return v
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tokoPurchaseOrderActivity = activity as TokoPurchaseOrderActivity
        tokoPurchaseOrderActivity.initializeToolbar(getString(R.string.purchase_order_confirmation))

        if (arguments != null) {
            memberCode = arguments!!.getString(DefineValue.MEMBER_CODE, "")
            commCode = arguments!!.getString(DefineValue.COMMUNITY_CODE, "")
            paymentOption = arguments!!.getString(DefineValue.PAYMENT_OPTION, "")
            ebdConfirmModel = getGson().fromJson(arguments!!.getString(DefineValue.EBD_CONFIRM_DATA, ""), EBDConfirmModel::class.java)
        }

        member_code_field.text = memberCode
        comm_code_field.text = commCode
        amount_field.text = getString(R.string.currency) + " " + CurrencyFormat.format(ebdConfirmModel.amount)
        discount_field.text = getString(R.string.currency) + " " + CurrencyFormat.format(ebdConfirmModel.discount_amount)
        total_field.text = getString(R.string.currency) + " " + CurrencyFormat.format(ebdConfirmModel.total_amount)
        val docDetails = ebdConfirmModel.doc_details
        mappingItemList.addAll(docDetails[0].mapping_items)


        paymentMethodList.addAll(ebdConfirmModel.payment_methods)
        val paymentMethodNameList = ArrayList<String>()
        for (i in paymentMethodList.indices) {
            paymentMethodNameList.add(paymentMethodList[i].payment_name)
        }
        val paymentMethodAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, paymentMethodNameList)
        spinner_payment_method.adapter = paymentMethodAdapter
        if (paymentMethodList.size == 1)
            spinner_payment_method.isEnabled = false
        spinner_payment_method.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                paymentMethodCode = paymentMethodList[p2].payment_code
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        submit_btn.setOnClickListener { submitOrder() }
    }

    private fun submitOrder() {
        Toast.makeText(context, "$memberCode $commCode", Toast.LENGTH_SHORT).show()
    }
}
