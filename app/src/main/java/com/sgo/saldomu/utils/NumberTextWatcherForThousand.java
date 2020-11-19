package com.sgo.saldomu.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class NumberTextWatcherForThousand implements TextWatcher {

    EditText editText;

    public NumberTextWatcherForThousand(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            editText.removeTextChangedListener(this);
            String value = editText.getText().toString();
            if (!value.equals("")) {
                if (value.startsWith("0") && !value.startsWith("0."))
                    editText.setText("");
                else {
                    String str = trimCommaOfString(value);
                    editText.setText(getDecimalFormattedString(str));
                }
                editText.setSelection(editText.getText().toString().length());
            }
            editText.addTextChangedListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            editText.addTextChangedListener(this);
        }
    }

    public static String getDecimalFormattedString(String value) {
        String temp = "";
        int i = 0;
        int j = -1 + value.length();
        for (int k = j; ; k--) {
            if (k < 0)
                return temp;

            if (i == 3) {
                temp = "." + temp;
                i = 0;
            }
            temp = value.charAt(k) + temp;
            i++;
        }
    }

    public static String trimCommaOfString(String string) {
        if (string.contains(".")) {
            return string.replaceAll("\\.", "");
        } else {
            return string;
        }

    }
}
