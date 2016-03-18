package com.sgo.mdevcash.fragments;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.securepreferences.SecurePreferences;
import com.sgo.mdevcash.Beans.BalanceModel;
import com.sgo.mdevcash.R;
import com.sgo.mdevcash.activities.MainPage;
import com.sgo.mdevcash.coreclass.BaseFragmentMainPage;
import com.sgo.mdevcash.coreclass.CurrencyFormat;
import com.sgo.mdevcash.coreclass.CustomSecurePref;
import com.sgo.mdevcash.services.BalanceService;
import com.txusballesteros.widgets.FitChart;
import com.txusballesteros.widgets.FitChartValue;

import java.util.ArrayList;
import java.util.Collection;

import in.srain.cube.views.ptr.PtrFrameLayout;
import timber.log.Timber;

/*
  Created by Lenovo Thinkpad on 12/22/2015.
 */
public class Home extends BaseFragmentMainPage implements View.OnClickListener {
    private TextView balanceCurrency,balanceValue, currencyLimit, limitValue, type_periode_limit;
    private String balance = "0", mlimit = "0", ccy_id = "",type_periode = "";
    private Integer slimit = 1000000, ibalance = 0;
    private Float stemp, total_temp;
    private FitChart fitChart;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActiveAndroid.initialize(getActivity());

        fitChart = (FitChart) mView.findViewById(R.id.fitChart);
        fitChart.setMinValue(0f);
        fitChart.setMaxValue(100f);

        balanceValue = (TextView) mView.findViewById(R.id.balance_value);
        balanceCurrency = (TextView) mView.findViewById(R.id.currency_value);
        currencyLimit = (TextView) mView.findViewById(R.id.currency_limit_value);
        limitValue = (TextView) mView.findViewById(R.id.limit_value);
        type_periode_limit = (TextView) mView.findViewById(R.id.periode_limit_value);
        Button payfriend = (Button) mView.findViewById(R.id.payfriend);
        payfriend.setOnClickListener(this);
        Button askformoney = (Button) mView.findViewById(R.id.askformoney);
        askformoney.setOnClickListener(this);

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
//        balance = sp.getString(DefineValue.BALANCE,"0");

        BalanceModel mBal = BalanceModel.load(BalanceModel.class,1);
        if(mBal != null) {
            balance = mBal.getAmount();
            ccy_id = mBal.getCcy_id();
            mlimit = mBal.getRemain_limit();
            type_periode = mBal.getPeriod_limit();
        }
        setUIBalance();

    }

    public void setUIBalance(){
        ibalance = Integer.parseInt(balance.replaceAll("[^0-9]", ""));
        ibalance = ibalance / 100;
        total_temp = (float)ibalance / (float)slimit;
        total_temp = total_temp * 100;

        balanceCurrency.setText(ccy_id);
        balanceValue.setText(CurrencyFormat.format(balance));
        currencyLimit.setText(ccy_id);
        limitValue.setText(CurrencyFormat.format(mlimit));

        if (type_periode.equals("Monthly"))
            type_periode_limit.setText(R.string.header_monthly_limit);
        else
            type_periode_limit.setText(R.string.header_daily_limit);

        Collection<FitChartValue> values = new ArrayList<>();
        if(total_temp > 99)
        {
            values.add(new FitChartValue(25f, R.color.chart_value_1));
            values.add(new FitChartValue(25f, R.color.chart_value_2));
            values.add(new FitChartValue(25f, R.color.chart_value_3));
            values.add(new FitChartValue(25f, R.color.chart_value_4));
            fitChart.setValues(values);
        }
        else if(total_temp > 75 && total_temp < 100)
        {
            stemp = total_temp - 75;
            values.add(new FitChartValue(25f, R.color.chart_value_1));
            values.add(new FitChartValue(25f, R.color.chart_value_2));
            values.add(new FitChartValue(25f, R.color.chart_value_3));
            values.add(new FitChartValue(stemp, R.color.chart_value_4));
            fitChart.setValues(values);
        }
        else if(total_temp > 50 && total_temp < 75)
        {
            stemp = total_temp - 50;
            values.add(new FitChartValue(25f, R.color.chart_value_1));
            values.add(new FitChartValue(25f, R.color.chart_value_2));
            values.add(new FitChartValue(stemp, R.color.chart_value_3));
            fitChart.setValues(values);
        }
        else if(total_temp > 25 && total_temp < 50)
        {
            stemp = total_temp - 25;
            values.add(new FitChartValue(25f, R.color.chart_value_1));
            values.add(new FitChartValue(stemp, R.color.chart_value_2));
            fitChart.setValues(values);
        }
        else if(total_temp > 0 && total_temp < 25)
        {
            stemp = total_temp - 0;
            values.add(new FitChartValue(stemp, R.color.chart_value_1));
            fitChart.setValues(values);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("receiver service balance");
            Bundle extras=intent.getExtras();
            BalanceModel msg=extras.getParcelable(BalanceModel.BALANCE_PARCELABLE);

            if (msg != null) {
                balance = msg.getAmount();
                mlimit = msg.getRemain_limit();
                ccy_id = msg.getCcy_id();
                type_periode = msg.getPeriod_limit();
                setUIBalance();
            }

        }
    };

    private IntentFilter filter = new IntentFilter(BalanceService.INTENT_ACTION_BALANCE);

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.payfriend:
                switchMenu(NavigationDrawMenu.MPAYFRIENDS,null);
                break;
            case R.id.askformoney:
                switchMenu(NavigationDrawMenu.MASK4MONEY,null);
                break;
        }
    }



    private void switchMenu(int idx_menu,Bundle data){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(idx_menu, data);
    }

    @Override
    protected int getInflateFragmentLayout() {
        return R.layout.frag_home;
    }

    @Override
    public boolean checkCanDoRefresh() {
        return false;
    }

    @Override
    public void refresh(PtrFrameLayout frameLayout) {
        frameLayout.refreshComplete();
    }

    @Override
    public void goToTop() {

    }


}
