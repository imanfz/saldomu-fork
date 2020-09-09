package com.sgo.saldomu.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.BankHeaderTopUp;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TopUpActivity;
import com.sgo.saldomu.adapter.EasyAdapter;
import com.sgo.saldomu.adapter.SelectBankProductAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.widgets.BaseFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 11/5/2014.
 */
public class BankProductSelectTopUpFragment extends BaseFragment implements InformationDialog.OnDialogOkCallback {

    private final String DEFAULT_OTHER_BANK_CODE = "013"; //bank code permata
    View layout_list_view;
    EasyAdapter adapter;
    Boolean is_full_activity = false;
    LevelClass levelClass;
    private InformationDialog dialogI;
//    AnimatedExpandableListView productBankExLV;
    ExpandableListView productBankExLV;
    List<BankHeaderTopUp> listDataHeader;
    HashMap<String, BankDataTopUp> listDataChild;
    Toolbar toolbar;

    SelectBankProductAdapter bankProductAdapter;
    List<listBankModel> listDataHeader2;
    HashMap<String, listBankModel> listDataChild2;

    public BankProductSelectTopUpFragment newInstance(Boolean is_full_activity){
        BankProductSelectTopUpFragment fragment = new BankProductSelectTopUpFragment();
        Bundle mbun = new Bundle();
        mbun.putBoolean(DefineValue.IS_ACTIVITY_FULL,is_full_activity);
        fragment.setArguments(mbun);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_bank_product_list_topup, container, false);
        toolbar = v.findViewById(R.id.main_toolbar);
        layout_list_view = v.findViewById(R.id.layout_list);
        productBankExLV = v.findViewById(R.id.frag_product_bank_list_expandableListView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dialogI = InformationDialog.newInstance(this, 0);
        Bundle mArgs = getArguments();
        if(mArgs != null && !mArgs.isEmpty()) {
            is_full_activity = mArgs.getBoolean(DefineValue.IS_ACTIVITY_FULL, false);
            if(is_full_activity)
                setActionBarTitle(getString(R.string.toolbar_title_topup));
        }
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listDataChild2 = new HashMap<>();
        listDataHeader2 = new ArrayList<>();

        bankProductAdapter = new SelectBankProductAdapter(getActivity(), listDataHeader2, listDataChild2);

        levelClass = new LevelClass(getActivity(),sp);
        levelClass.refreshData();
        productBankExLV.setAdapter(bankProductAdapter);

//        productBankExLV.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                if (!listDataHeader2.get(groupPosition).getProduct_type().equals("ATM"))
//                    selectAction(listDataHeader2.get(groupPosition));
//            }
//        });

        productBankExLV.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (!listDataHeader2.get(groupPosition).getProduct_type().equals("ATM")) {
                    selectAction(listDataHeader2.get(groupPosition));
                }
//                else {
//                    if (productBankExLV.isGroupExpanded(groupPosition)) {
//                        productBankExLV.collapseGroupWithAnimation(groupPosition);
//                    } else {
//                        productBankExLV.expandGroupWithAnimation(groupPosition);
//                    }
//                }

                return false;
            }
        });

    }

    private void setActionBarTitle(String _title){
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.setToolbarTitle(_title);
    }

    public void parseProductBank(){
        try {
            if (listDataHeader2.size()>0) {
                listDataHeader2.clear();
                listDataChild2.clear();
            }

            listDataHeader2.addAll(DataManager.getInstance().getBankData());
            for (listBankModel obj: listDataHeader2) {
                if (obj.getProduct_type().equals("ATM")){
                    listDataChild2.put(obj.getProduct_code(), obj);
                }
            }

            bankProductAdapter.notifyDataSetChanged();
        }catch(Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

//    private void selectAction(int groupPos, int childPos){
//        ListBankDataTopup model = listDataChild.get(listDataHeader.get(groupPos).getHeader()).getBankData().get(childPos);
//        if(is_full_activity){
//            Fragment mFrag = new SgoPlus_input();
//            String titleToolbar = model.getBankName()+" - "+model.getProductName();
//            Bundle mBun = new Bundle();
//            mBun.putString(DefineValue.BANK_CODE,model.getBankCode());
//            mBun.putString(DefineValue.BANK_NAME,model.getBankName());
//            mBun.putString(DefineValue.PRODUCT_NAME,model.getProductName());
//            mBun.putString(DefineValue.PRODUCT_CODE,model.getProductCode());
//            mBun.putString(DefineValue.PRODUCT_TYPE,model.getProductType());
//            mBun.putString(DefineValue.PRODUCT_H2H,model.getProductH2H());
//            mFrag.setArguments(mBun);
//            if(model.getProductType().equals(DefineValue.BANKLIST_TYPE_SMS)) {
//                if (!levelClass.isLevel1QAC()) {
//                    switchFragmentTopUpActivity(mFrag,titleToolbar,true);
//                } else
//                    levelClass.showDialogLevel();
//            }
//            else
//                switchFragmentTopUpActivity(mFrag,titleToolbar,true);
//        }
//        else {
//            Intent i = new Intent(getActivity(), TopUpActivity.class);
//            i.putExtra(DefineValue.BANK_CODE,model.getBankCode());
//            i.putExtra(DefineValue.BANK_NAME,model.getBankName());
//            i.putExtra(DefineValue.PRODUCT_NAME,model.getProductName());
//            i.putExtra(DefineValue.PRODUCT_CODE,model.getProductCode());
//            i.putExtra(DefineValue.PRODUCT_TYPE,model.getProductType());
//            i.putExtra(DefineValue.PRODUCT_H2H,model.getProductH2H());
//            if(model.getProductType().equals(DefineValue.BANKLIST_TYPE_SMS)) {
//                if (!levelClass.isLevel1QAC()) {
//                    switchActivity(i);
//                }
//                else
//                    levelClass.showDialogLevel();
//            }
//            else
//                switchActivity(i);
//
//        }
//    }

    private void selectAction(listBankModel model){
//        ListBankDataTopup model = listDataChild.get(listDataHeader.get(groupPos).getHeader()).getBankData().get(childPos);
        if(is_full_activity){
            Fragment mFrag = new SgoPlus_input();
            String titleToolbar = model.getBank_name()+" - "+model.getProduct_name();
            Bundle mBun = new Bundle();
            mBun.putString(DefineValue.BANK_CODE,model.getBank_code());
            mBun.putString(DefineValue.BANK_NAME,model.getBank_name());
            mBun.putString(DefineValue.PRODUCT_NAME,model.getProduct_name());
            mBun.putString(DefineValue.PRODUCT_CODE,model.getProduct_code());
            mBun.putString(DefineValue.PRODUCT_TYPE,model.getProduct_type());
            mBun.putString(DefineValue.PRODUCT_H2H,model.getProduct_h2h());
            mFrag.setArguments(mBun);
            if(model.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS)) {
                if (!levelClass.isLevel1QAC()) {
                    switchFragmentTopUpActivity(mFrag,titleToolbar,true);
                } else
                    levelClass.showDialogLevel();
            }
            else
                switchFragmentTopUpActivity(mFrag,titleToolbar,true);
        }
        else {
            Intent i = new Intent(getActivity(), TopUpActivity.class);
            i.putExtra(DefineValue.BANK_CODE,model.getBank_code());
            i.putExtra(DefineValue.BANK_NAME,model.getBank_name());
            i.putExtra(DefineValue.PRODUCT_NAME,model.getProduct_name());
            i.putExtra(DefineValue.PRODUCT_CODE,model.getProduct_code());
            i.putExtra(DefineValue.PRODUCT_TYPE,model.getProduct_type());
            i.putExtra(DefineValue.PRODUCT_H2H,model.getProduct_h2h());
            if(model.getProduct_type().equals(DefineValue.BANKLIST_TYPE_SMS)) {
                if (!levelClass.isLevel1QAC()) {
                    switchActivity(i);
                }
                else
                    levelClass.showDialogLevel();
            }
            else
                switchActivity(i);

        }
    }

    private void switchFragmentTopUpActivity(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        TopUpActivity fca = (TopUpActivity) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, MainPage.ACTIVITY_RESULT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        parseProductBank();

        MainPage fca = (MainPage) getActivity();
        setHasOptionsMenu(true);
        fca.mDrawerToggle.setDrawerIndicatorEnabled(false);
        fca.setActionBarIcon(R.drawable.ic_arrow_left);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainPage fca = (MainPage) getActivity();
//        setHasOptionsMenu(true);
        fca.mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onOkButton() {

    }
}