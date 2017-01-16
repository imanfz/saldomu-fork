package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sgo.orimakardaya.Beans.Account_Collection_Model;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.CollectionActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.EasyAdapter;
import com.sgo.orimakardaya.coreclass.DefineValue;

import java.util.List;

import io.realm.Realm;

/*
  Created by Administrator on 6/12/2015.
 */
public class ListCollection extends ListFragment {

    private View v;
    private Realm realm;
//    private JSONArray mData;
private List<Account_Collection_Model> refObj;

    public static ListCollection newInstance() {
        ListCollection mFrag = new ListCollection();
//        mFrag.mData= _data;
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

        realm = Realm.getDefaultInstance();

        refObj = realm.where(Account_Collection_Model.class).findAll();
        String[] _data = new String[refObj.size()];
        for(int i = 0; i < refObj.size() ; i++){
            _data[i] = refObj.get(i).getComm_name();
        }

        EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);
        ListView listView1 = (ListView) v.findViewById(android.R.id.list);
        listView1.setAdapter(adapter);

//        try {
//            String[] _data = new String[mData.length()];
//            for(int i = 0; i < mData.length() ; i++){
//                _data[i] = mData.getJSONObject(i).getString(WebParams.COMM_NAME);
//            }
//
//            EasyAdapter adapter = new EasyAdapter(getActivity(),R.layout.list_view_item_with_arrow, _data);
//            ListView listView1 = (ListView) v.findViewById(android.R.id.list);
//            listView1.setAdapter(adapter);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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


    private void openIntentBiller(int position){
        Intent i = new Intent(getActivity(), CollectionActivity.class);
//        try {
//            i.putExtra(DefineValue.COLLECTION_DATA,mData.getJSONObject(position).toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        i.putExtra(DefineValue.COMMUNITY_ID,refObj.get(position).getComm_id());
        i.putExtra(DefineValue.COMMUNITY_CODE,refObj.get(position).getComm_code());
        i.putExtra(DefineValue.COMMUNITY_API_KEY,refObj.get(position).getApi_key());
        i.putExtra(DefineValue.CALLBACK_URL,refObj.get(position).getCallback_url());
        i.putExtra(DefineValue.COMMUNITY_NAME,refObj.get(position).getComm_name());
        switchActivity(i);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed())
            realm.close();
        super.onDestroy();
    }
}