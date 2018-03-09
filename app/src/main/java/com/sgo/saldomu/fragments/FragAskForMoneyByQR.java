package com.sgo.saldomu.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.AskForMoneyQRActivity;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

/**
 * Created by Denny on 9/30/2016.
 */

public class FragAskForMoneyByQR extends Fragment {
    SecurePreferences sp;
    EditText etAmount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_ask_for_money_byqr, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        etAmount = (EditText) getActivity().findViewById(R.id.askformoney_value_amount);
        final EditText askformoney_value_message = (EditText) getActivity().findViewById(R.id.askformoney_value_message);

        etAmount.requestFocus();

        Button btnRequestMoney = (Button) getActivity().findViewById(R.id.btn_generate_qr);
        btnRequestMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputValidation()) {
                    Fragment newFragment = new FragGenerateQR();
                    Bundle args = new Bundle();
                    args.putString("amount", String.valueOf(etAmount.getText()));
                    args.putString("message", String.valueOf(askformoney_value_message.getText()));
                    args.putString("benef", sp.getString(DefineValue.USERID_PHONE, ""));
                    args.putString("nama_benef", sp.getString(DefineValue.CUST_NAME, getString(R.string.text_strip)));
                    newFragment.setArguments(args);
                    switchFragment(newFragment, getString(R.string.toolbar_title_askbyqr));
                }
            }
        });
    }

    public boolean inputValidation(){
        if(etAmount.getText().toString().length()==0){
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_validation));
            return false;
        } else if(Long.parseLong(etAmount.getText().toString()) < 1){
            etAmount.requestFocus();
            etAmount.setError(getString(R.string.payfriends_amount_zero));
            return false;
        }
        return true;
    }

    private void switchFragment(Fragment i, String name){
        if (getActivity() == null)
            return;

        hiddenKeyboard(getView());
        AskForMoneyQRActivity fca = (AskForMoneyQRActivity) getActivity();
        fca.switchContent(i,name, true);
    }

    private void hiddenKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getFragmentManager().getBackStackEntryCount()>0)
                    getFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
