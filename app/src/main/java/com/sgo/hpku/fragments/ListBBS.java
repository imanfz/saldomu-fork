package com.sgo.hpku.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.securepreferences.SecurePreferences;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.BBSActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.adapter.EasyAdapter;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;

/**
 * Created by thinkpad on 1/25/2017.
 */

public class ListBBS extends ListFragment {

    private View v;
    private boolean isJoin = false;
    String[] _data;
//    Boolean isAgent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
//        isAgent = sp.getBoolean(DefineValue.IS_AGENT,false);
//        if(isAgent)
//            _data = getResources().getStringArray(R.array.list_bbs_agent);
//        else
//            _data = getResources().getStringArray(R.array.list_bbs_member);
        _data = getResources().getStringArray(R.array.list_bbs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_bbs, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);

        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        int posIdx;
//        if(isAgent) {
            if (_data[position].equalsIgnoreCase(getString(R.string.title_bbs_list_account_bbs)))
                posIdx = BBSActivity.LISTACCBBS;
            else if (_data[position].equalsIgnoreCase(getString(R.string.transaction)))
                posIdx = BBSActivity.TRANSACTION;
            else if(_data[position].equalsIgnoreCase(getString(R.string.title_cash_out_member)))
                posIdx = BBSActivity.CONFIRMCASHOUT;
        else
            posIdx = -1;
//        } else
//            posIdx = BBSActivity.CONFIRMCASHOUT;

        if(posIdx !=-1){
            Intent i = new Intent(getActivity(), BBSActivity.class);
            i.putExtra(DefineValue.INDEX, posIdx);
            switchActivity(i,MainPage.ACTIVITY_RESULT);
        }

    }

    private void switchActivity(Intent mIntent, int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
    }

}
