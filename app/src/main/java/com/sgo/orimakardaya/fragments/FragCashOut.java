package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.NominalAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thinkpad on 3/18/2015.
 */
public class FragCashOut extends Fragment {

    ListView listNominal;
    List<String> nominal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_cash_out, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listNominal = (ListView) getActivity().findViewById(R.id.listNominal);

        nominal = new ArrayList<String>();
        nominal.add("50,000");
        nominal.add("100,000");
        nominal.add("150,000");
        nominal.add("200,000");
        nominal.add("250,000");
        nominal.add("300,000");
        nominal.add("350,000");
        nominal.add("400,000");
        nominal.add("450,000");
        nominal.add("500,000");

        NominalAdapter nominalAdapter = new NominalAdapter(getActivity().getApplicationContext(), nominal);
        listNominal.setAdapter(nominalAdapter);
        listNominal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent i = new Intent(getActivity(), CreatePIN.class);
//                i.putExtra(CoreApp.IS_FIRST_TIME, CoreApp.NO);
//                switchActivity(i);
            }
        });

    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }
}
