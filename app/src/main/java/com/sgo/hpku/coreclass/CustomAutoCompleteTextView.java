package com.sgo.hpku.coreclass;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    private Button btn;
    private RadioGroup locationRadioGroup;
    private RadioButton searchLocationRadioBtn;


    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setButton(Button reqButton)
    {
        btn = reqButton;
    }

    public void setRadioGroup(RadioGroup radioGroup)
    {
        locationRadioGroup = radioGroup;
    }

    public void setRadioButton(RadioButton radioButton)
    {
        searchLocationRadioBtn = radioButton;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (listener != null)
            listener.onStateChanged(this, true);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event)
    {
        //Toast.makeText(getContext(), "on key pre ime" + getText(), Toast.LENGTH_LONG).show();
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            int checkedRadioBtnId = locationRadioGroup.getCheckedRadioButtonId();
            if(checkedRadioBtnId == searchLocationRadioBtn.getId())
            {
                //logic to enable and disable button request
                int length = getText().toString().trim().length();
                if (length > 0) btn.setEnabled(true);
                else btn.setEnabled(false);
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }

    /**
     * Keyboard Listener
     */
    KeyboardListener listener;

    public void setOnKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }

    public interface KeyboardListener {
        void onStateChanged(CustomAutoCompleteTextView CustomBbsAutoCompleteTextView, boolean showing);
    }
}
