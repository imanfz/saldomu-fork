package com.sgo.saldomu.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.DenomSCADMActivity
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_input_member_code.*

class InputMemberCode : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_input_member_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_member_code.setText(userPhoneID)
        btn_next.setOnClickListener {
            val bundle : Bundle = Bundle()
            bundle.putString(DefineValue.MEMBER_CODE, et_member_code.text.toString())
            val frag: Fragment = FragmentDenom()
            frag.arguments = bundle
            SwitchFragment(frag, DenomSCADMActivity.DENOM_PAYMENT, true)
        }
    }
}