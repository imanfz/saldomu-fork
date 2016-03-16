package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.CollectionActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.WebParams;
import org.json.JSONArray;
import org.json.JSONException;

/*
  Created by Administrator on 6/12/2015.
 */
public class ListCollection extends ListFragment {

    View v;
    private JSONArray mData;

    public static ListCollection newInstance(JSONArray _data) {
        ListCollection mFrag = new ListCollection();
        mFrag.mData= _data;
        return mFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_collection, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            String[] _data = new String[mData.length()];
            for(int i = 0; i < mData.length() ; i++){
                _data[i] = mData.getJSONObject(i).getString(WebParams.COMM_NAME);
            }

            EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);
            ListView listView1 = (ListView) v.findViewById(android.R.id.list);
            listView1.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        openIntentBiller(position);
    }


    public void openIntentBiller(int position){
        Intent i = new Intent(getActivity(), CollectionActivity.class);
        try {
            i.putExtra(DefineValue.COLLECTION_DATA,mData.getJSONObject(position).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switchActivity(i);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }
}