package com.sgo.saldomu.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.HashMap;

/**
 * Created by thinkpad on 4/26/2017.
 */

public class CustomAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showDropDown();
        }
    }

//    @Override
//    public void dismissDropDown() {
//        if(isFocused())
//            showDropDown();
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isFocused())
            showDropDown();
        return super.onTouchEvent(event);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        if(isFocused())
            showDropDown();
    }

    /** Returns the country name corresponding to the selected item */
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        /** Each item in the autocompetetextview suggestion list is a hashmap object */
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("txt");
    }
}