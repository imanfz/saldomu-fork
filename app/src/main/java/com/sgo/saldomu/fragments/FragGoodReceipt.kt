package com.sgo.saldomu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_good_receipt.*

class FragGoodReceipt : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.frag_good_receipt, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        iv_clear.setOnClickListener(View.OnClickListener { v: View? -> et_store_phone.setText("") })

        btn_submit.setOnClickListener {
            if(inputValidation())
            {

            }
        }
    }

    fun inputValidation():Boolean{
        if (et_store_phone == null || et_store_phone.getText().toString().isEmpty()) {
            et_store_phone.requestFocus()
            et_store_phone.setError(getString(R.string.store_phone_vaidation))
            return false
        }
        return true
    }


}

