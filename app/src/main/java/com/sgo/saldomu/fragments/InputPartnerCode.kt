package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.sgo.saldomu.R
import com.sgo.saldomu.activities.DenomSCADMActivity
import com.sgo.saldomu.coreclass.DefineValue
import com.sgo.saldomu.widgets.BaseFragment
import kotlinx.android.synthetic.main.frag_input_partner_code.*

class InputPartnerCode : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_input_partner_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_partner_code.setText(userPhoneID)
        et_partner_code.onRightDrawableClicked { it.text.clear() }

        btn_next.setOnClickListener {
            if (et_partner_code.text!!.isNotEmpty()) {
                val bundle: Bundle = Bundle()
                    bundle.putString(DefineValue.MEMBER_CODE, et_partner_code.text.toString())
                    val frag: Fragment = FragmentDenom()
                    frag.arguments = bundle
                    SwitchFragment(frag, DenomSCADMActivity.DENOM_PAYMENT, true)

            } else
                et_partner_code.error = getString(R.string.partner_code_required)
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}
