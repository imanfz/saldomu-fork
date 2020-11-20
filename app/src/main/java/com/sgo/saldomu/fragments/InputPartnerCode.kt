package com.sgo.saldomu.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.sgo.saldomu.BuildConfig
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

        when {
            arguments!!.containsKey(DefineValue.CUST_ID) -> et_partner_code.setText(arguments!!.getString(DefineValue.CUST_ID))
            BuildConfig.FLAVOR.equals("development", ignoreCase = true) -> et_partner_code.setText("toko 006")
            else -> et_partner_code.setText(userPhoneID)
        }

        et_partner_code.onRightDrawableClicked { it.text.clear() }

        favorite_switch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            notes_edit_text.visibility = if (isChecked) View.VISIBLE else View.GONE
            notes_edit_text.isEnabled = isChecked
        }

        btn_next.setOnClickListener {
//            if (et_partner_code.text!!.isNotEmpty()) {
            if (inputValidation()) {
                val bundle: Bundle = Bundle()
                bundle.putString(DefineValue.MEMBER_CODE, et_partner_code.text.toString())
                if (favorite_switch.isChecked) {
                    bundle.putBoolean(DefineValue.IS_FAVORITE, true)
                    bundle.putString(DefineValue.CUST_ID, et_partner_code.text.toString())
                    bundle.putString(DefineValue.NOTES, notes_edit_text.text.toString())
                    bundle.putString(DefineValue.TX_FAVORITE_TYPE, DefineValue.B2B)
                    bundle.putString(DefineValue.PRODUCT_TYPE, DefineValue.DENOM_B2B)
                }
                val frag: Fragment = FragmentDenom()
                frag.arguments = bundle
                SwitchFragment(frag, DenomSCADMActivity.DENOM_PAYMENT, true)
            }

//            } else
//                et_partner_code.error = getString(R.string.partner_code_required)
        }
    }

    private fun inputValidation(): Boolean {
        if (et_partner_code.text.isNullOrEmpty()) {
            et_partner_code.requestFocus()
            et_partner_code.error = getString(R.string.partner_code_required)
            return false
        } else if (favorite_switch.isChecked && notes_edit_text.text.toString().isEmpty()) {
            notes_edit_text.requestFocus()
            notes_edit_text.error = getString(R.string.payfriends_notes_zero)
            return false
        }
        return true
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
