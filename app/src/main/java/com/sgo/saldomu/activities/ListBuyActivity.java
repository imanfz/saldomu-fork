package com.sgo.saldomu.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.Account_Collection_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BuyFragmentTabAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.fragments.ListBuyRF;
import com.sgo.saldomu.widgets.BaseActivity;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;

/**
 * Created by Lenovo Thinkpad on 12/11/2017.
 */

public class ListBuyActivity extends BaseActivity {

    private View v;
    private View layout_empty;
    private TabPageIndicator tabs;
    private ViewPager pager;
    private BuyFragmentTabAdapter adapternya;
    private ProgressDialog out;
    private ListBuyRF mWorkFragment;
    private RealmChangeListener realmListener;
    private ArrayList<String> Title_tab;
    private SecurePreferences sp;
    private InformationDialog dialogI;
    private Realm realm;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_list_buy;
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_buy));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());

        tabs = (TabPageIndicator)findViewById(R.id.buy_tabs);
        pager = (ViewPager) findViewById(R.id.buy_pager);
        layout_empty = findViewById(R.id.empty_layout);

        layout_empty.setVisibility(View.GONE);
        Button btn_refresh = (Button) layout_empty.findViewById(R.id.btnRefresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getDataBiller();
            }
        });

        pager.setPageMargin(pageMargin);
        dialogI = InformationDialog.newInstance(8);
//        dialogI.setTargetFragment(this,0);
        realm = Realm.getInstance(RealmManager.BillerConfiguration);
        initializeData();
    }

    private void initializeData(){

        List<Biller_Type_Data_Model> mListBillerTypeData = realm.where(Biller_Type_Data_Model.class).findAll();
        List<Account_Collection_Model> mListACL = realm.where(Account_Collection_Model.class).findAll();
        Title_tab = new ArrayList<>();
        Boolean isBuy = false;
        Boolean isPay = false;

        if(mListBillerTypeData.size() > 0) {
            for (int i = 0; i < mListBillerTypeData.size(); i++) {
                if (mListBillerTypeData.get(i).getBiller_type().equals(DefineValue.BIL_TYPE_BUY)) {
                    isBuy = true;
                } else {
                    isPay = true;
                }
            }

//            if (isBuy)
//                Title_tab.add(getString(R.string.purchase));
            if (isPay)
                Title_tab.add(getString(R.string.payment));
        }


//        if(mListACL.size() > 0) {
//            Title_tab.add(getString(R.string.collection));
//        }

        if(Title_tab.isEmpty())
            layout_empty.setVisibility(View.VISIBLE);
        else
            layout_empty.setVisibility(View.GONE);

        adapternya = new BuyFragmentTabAdapter(getSupportFragmentManager(),this,Title_tab);
        pager.setAdapter(adapternya);
        tabs.setViewPager(pager);
        if(out != null && out.isShowing())
            out.dismiss();
//        pager.setVisibility(View.VISIBLE);
//        tabs.setVisibility(View.VISIBLE);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        setTargetFragment(null, -1);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if(!realm.isInTransaction() && !realm.isClosed()) {
//            realm.removeChangeListener(realmListener);
            realm.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(this.getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
