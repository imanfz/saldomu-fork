package com.sgo.saldomu.activities;

import android.os.Bundle;
import android.util.Log;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.entityRealm.AgentDetail;

import io.realm.Realm;
import io.realm.RealmResults;

public class BookBbsActivity extends BaseActivity {

    Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_bbs);

        int businessId = getIntent().getIntExtra("businessId", -1);

        RealmResults<AgentDetail> results = realm.where(AgentDetail.class).equalTo("businessId", businessId).findAll();
        //int businessID = Integer.valueOf(SearchAgentActivity.business_id_arr.get(agentPosition));
        Log.d("TAG DEBUG ", String.valueOf(businessId));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_book_bbs;
    }
}
