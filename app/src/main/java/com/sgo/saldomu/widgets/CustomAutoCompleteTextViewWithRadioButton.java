package com.sgo.saldomu.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class CustomAutoCompleteTextViewWithRadioButton extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

    private Button btn;
    private RadioGroup locationRadioGroup;
    private RadioButton searchLocationRadioBtn;


    public CustomAutoCompleteTextViewWithRadioButton(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextViewWithRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomAutoCompleteTextViewWithRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
            try {
                int checkedRadioBtnId = locationRadioGroup.getCheckedRadioButtonId();
                if (checkedRadioBtnId == searchLocationRadioBtn.getId()) {
                    //logic to enable and disable button request
                    int length = getText().toString().trim().length();
                    btn.setEnabled(length > 0);
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }

    /**
     * Keyboard Listener
     */
    KeyboardListener listener;

    public interface KeyboardListener {
        void onStateChanged(CustomAutoCompleteTextViewWithRadioButton CustomBbsAutoCompleteTextView, boolean showing);
    }
}
