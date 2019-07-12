package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment

class BillerInputData : BaseFragment() {

//    private lateinit var v: View
    private lateinit var tv_payment_remark: TextView
    private lateinit var et_payment_remark: EditText
    private lateinit var img_operator: ImageView
    private lateinit var tv_denom: TextView
    private lateinit var spin_denom: Spinner
    private lateinit var layoutMonth: View
    private lateinit var btn_submit: Button

    private var biller_type_code: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_biller_input, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        var args:Bundle? = getArguments()
        biller_type_code=arguments?.getString(DefineValue.BILLER_TYPE,"")

        tv_payment_remark=v.findViewById(R.id.billerinput_text_payment_remark)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}

