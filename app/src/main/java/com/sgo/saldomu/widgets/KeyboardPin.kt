package com.sgo.saldomu.widgets

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.sgo.saldomu.R
import kotlinx.android.synthetic.main.keyboard_pin.view.*

class KeyboardPin : FrameLayout, View.OnClickListener {

    var listener: KeyboardPinListener? = null

    interface KeyboardPinListener {
        fun getCharSequenceKeyboard(text: CharSequence)
        fun useFingerprint()
    }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.keyboard_pin, this)
        initViews()
    }

    private fun initViews() {
        t9_key_0.setOnClickListener(this)
        t9_key_1.setOnClickListener(this)
        t9_key_2.setOnClickListener(this)
        t9_key_3.setOnClickListener(this)
        t9_key_4.setOnClickListener(this)
        t9_key_5.setOnClickListener(this)
        t9_key_6.setOnClickListener(this)
        t9_key_7.setOnClickListener(this)
        t9_key_8.setOnClickListener(this)
        t9_key_9.setOnClickListener(this)
        t9_key_backspace.setOnClickListener(this)

        if (iv_key_fingerprint.visibility == View.VISIBLE)
            iv_key_fingerprint.setOnClickListener(this)

        iv_pin_indicator1.visibility = View.INVISIBLE
        iv_pin_indicator2.visibility = View.INVISIBLE
        iv_pin_indicator3.visibility = View.INVISIBLE
        iv_pin_indicator4.visibility = View.INVISIBLE
        iv_pin_indicator5.visibility = View.INVISIBLE
        iv_pin_indicator6.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        if (password_field.text.length < 6) {
            if (v!!.tag != null && "number_button" == v.tag) {
                password_field.append((v as TextView).text)
                listener!!.getCharSequenceKeyboard(password_field.text.toString())
            }
        }
        when (v!!.id) {
            R.id.t9_key_backspace -> {
                val editable: Editable = password_field.text
                val charCount = editable.length
                if (charCount > 0) {
                    editable.delete(charCount - 1, charCount)
                    password_field.text = editable
                    listener!!.getCharSequenceKeyboard(password_field.text.toString())
                }
            }
            R.id.iv_key_fingerprint -> {
                listener!!.useFingerprint()
            }
        }
        if (password_field.text.isNotEmpty())
            iv_pin_indicator1.visibility = View.VISIBLE
        else
            iv_pin_indicator1.visibility = View.INVISIBLE
        if (password_field.text.length >= 2)
            iv_pin_indicator2.visibility = View.VISIBLE
        else
            iv_pin_indicator2.visibility = View.INVISIBLE
        if (password_field.text.length >= 3)
            iv_pin_indicator3.visibility = View.VISIBLE
        else
            iv_pin_indicator3.visibility = View.INVISIBLE
        if (password_field.text.length >= 4)
            iv_pin_indicator4.visibility = View.VISIBLE
        else
            iv_pin_indicator4.visibility = View.INVISIBLE
        if (password_field.text.length >= 5)
            iv_pin_indicator5.visibility = View.VISIBLE
        else
            iv_pin_indicator5.visibility = View.INVISIBLE
        if (password_field.text.length == 6)
            iv_pin_indicator6.visibility = View.VISIBLE
        else
            iv_pin_indicator6.visibility = View.INVISIBLE
    }

    fun hideFingerprint() {
        iv_key_fingerprint.visibility = View.INVISIBLE
    }

    fun reset(){
        password_field.text.clear()
        iv_pin_indicator1.visibility = View.INVISIBLE
        iv_pin_indicator2.visibility = View.INVISIBLE
        iv_pin_indicator3.visibility = View.INVISIBLE
        iv_pin_indicator4.visibility = View.INVISIBLE
        iv_pin_indicator5.visibility = View.INVISIBLE
        iv_pin_indicator6.visibility = View.INVISIBLE
    }
}