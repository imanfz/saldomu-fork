package com.sgo.orimakardaya.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.navdrawmainmenuModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.AboutAppsActivity;
import com.sgo.orimakardaya.activities.HelpActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.NavDrawMainMenuAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.dialogs.ReportBillerDialog;

import java.util.ArrayList;

/*
  Created by Administrator on 12/8/2014.
 */
public class NavigationDrawMenu extends ListFragment{

    ListView mListView;
    public View layoutContainer;
    public NavDrawMainMenuAdapter mAdapter;
    Bundle _SaveInstance;
    SecurePreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutContainer = inflater.inflate(R.layout.frag_navigation_draw_menu_main, container, false);
        return layoutContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _SaveInstance = savedInstanceState;

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        mAdapter = new NavDrawMainMenuAdapter(getActivity(), generateData());
        mListView = (ListView) layoutContainer.findViewById(android.R.id.list);

        mListView.setAdapter(mAdapter);

        selectItem(1,null);

    }

    public Boolean checkLogin(){
    
        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);

        return !flagLogin.equals("N");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectItem(position,null);
    }

    private ArrayList<navdrawmainmenuModel> generateData(){
        ArrayList<navdrawmainmenuModel> models = new ArrayList<navdrawmainmenuModel>();
        models.add(new navdrawmainmenuModel(0,getString(R.string.menu_group_title_main_menu),true));                                        //0
        models.add(new navdrawmainmenuModel(R.drawable.ic_home_icon_color,getString(R.string.menu_item_title_home),false));              //1
//        models.add(new navdrawmainmenuModel(R.drawable.ic_accounts_icon_color,getString(R.string.menu_item_title_accounts),false));        //
        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_icon_color,getString(R.string.menu_item_title_topup),false));              //2
        models.add(new navdrawmainmenuModel(R.drawable.ic_topup_pulsa,getString(R.string.menu_item_title_pulsa_agent), false));        //3
        models.add(new navdrawmainmenuModel(R.drawable.ic_payfriends_icon_color,getString(R.string.menu_item_title_pay_friends),false));    //4
        models.add(new navdrawmainmenuModel(R.drawable.ic_ask_icon_color,getString(R.string.menu_item_title_ask_for_money),false));            //5
        models.add(new navdrawmainmenuModel(R.drawable.ic_buy_icon_color,getString(R.string.menu_item_title_buy),false));                //6
//        models.add(new navdrawmainmenuModel(R.drawable.ic_cashout_icon_color,getString(R.string.menu_item_title_cash_out),false));       //7
        models.add(new navdrawmainmenuModel(R.drawable.ic_report,getString(R.string.menu_item_title_report),false));                       //8

        models.add(new navdrawmainmenuModel(0,getString(R.string.menu_group_title_account),true));                                              //9
        models.add(new navdrawmainmenuModel(R.drawable.ic_profile_icon_color,getString(R.string.menu_item_title_my_profile),false));       //10
        models.add(new navdrawmainmenuModel(R.drawable.ic_friends_icon_color,getString(R.string.menu_item_title_my_friends),false));        //11
//        models.add(new navdrawmainmenuModel(R.drawable.ic_groups_icon_color,getString(R.string.menu_item_title_my_groups),false));          //12

        models.add(new navdrawmainmenuModel(0,getString(R.string.menu_group_title_supports),true));                                           //13
        models.add(new navdrawmainmenuModel(R.drawable.ic_setting,getString(R.string.menu_item_title_setting),false));                   //14
        models.add(new navdrawmainmenuModel(R.drawable.ic_help,getString(R.string.menu_item_title_help),false));                         //15
        models.add(new navdrawmainmenuModel(R.drawable.ic_about,getString(R.string.menu_item_title_about),false));               //16

        models.add(new navdrawmainmenuModel(0,getString(R.string.menu_group_title_logout),true));                                            //17
        models.add(new navdrawmainmenuModel(R.drawable.ic_logout_icon,getString(R.string.menu_item_title_logout),false));                   //18

        return models;
    }

    public void selectItem(int position, Bundle data){
        Fragment newFragment;
        Intent newIntent;
        mAdapter.setSelectedItem(position);
        mAdapter.notifyDataSetChanged();
//        switch (position) {
//            case 1:
//                newFragment = new FragMainPage();
//                switchFragment(newFragment, getString(R.string.toolbar_title_home));
//                break;
//            case 2:
//                newFragment = new ListTopUp();
//                switchFragment(newFragment, getString(R.string.toolbar_title_topup));
//                break;
//            case 3:
//                newFragment = new FragPulsaAgent();
//                switchFragment(newFragment, getString(R.string.toolbar_title_pulsa_agent));
//                break;
//            case 4:
//                newFragment = new FragPayFriends();
//                if(data != null && !data.isEmpty())newFragment.setArguments(data);
//                switchFragment(newFragment, getString(R.string.menu_item_title_pay_friends));
//                break;
//            case 5:
//                newFragment = new FragAskForMoney();
//                switchFragment(newFragment, getString(R.string.menu_item_title_ask_for_money));
//                break;
//            case 6:
//                newFragment = new ListBuy();
//                switchFragment(newFragment, getString(R.string.toolbar_title_purchase));
//                break;
//            case 7:
//                newFragment = new FragCashOut();
//                switchFragment(newFragment, "Cash Out");
//                break;
//
//            case 8:
//                newFragment = new ReportTab();
//                switchFragment(newFragment, getString(R.string.menu_item_title_report));
//                break;
//
//            case 10:
//                newFragment = new ListMyProfile();
//                switchFragment(newFragment, getString(R.string.myprofilelist_ab_title));
//                break;
//            case 11:
//                newFragment = new ListMyFriends();
//                switchFragment(newFragment, getString(R.string.toolbar_title_myfriends));
//                break;
//            case 12:
//                newFragment = new FragMyGroup();
//                switchFragment(newFragment, getString(R.string.toolbar_title_mygroup));
//                break;
//            case 14:
//                newFragment = new ListSettings();
//                switchFragment(newFragment, getString(R.string.menu_item_title_setting));
//                break;
//            case 15:
//                newIntent = new Intent(getActivity(), HelpActivity.class);
//                switchActivity(newIntent, MainPage.ACTIVITY_RESULT);
//                break;
//            case 16:
//                newIntent = new Intent(getActivity(), AboutAppsActivity.class);
//                switchActivity(newIntent, MainPage.ACTIVITY_RESULT);
//                break;
//            case 18:
//                AlertDialog.Builder alertbox=new AlertDialog.Builder(getActivity());
//                alertbox.setTitle(getString(R.string.warning));
//                alertbox.setMessage(getString(R.string.exit_message));
//                alertbox.setPositiveButton(getString(R.string.ok), new
//                        DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface arg0, int arg1) {
//                                switchLogout();
//                            }
//                        });
//                alertbox.setNegativeButton(getString(R.string.cancel), new
//                        DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface arg0, int arg1) {}
//                        });
//                alertbox.show();
//                break;
//        }
        switch (position) {
            case 1:
                newFragment = new FragMainPage();
                switchFragment(newFragment, getString(R.string.toolbar_title_home));
                break;
            case 2:
                newFragment = new ListTopUp();
                switchFragment(newFragment, getString(R.string.toolbar_title_topup));
                break;
            case 3:
                newFragment = new FragPulsaAgent();
                switchFragment(newFragment, getString(R.string.toolbar_title_pulsa_agent));
                break;
            case 4:
                newFragment = new FragPayFriends();
                if(data != null && !data.isEmpty())newFragment.setArguments(data);
                switchFragment(newFragment, getString(R.string.menu_item_title_pay_friends));
                break;
            case 5:
                newFragment = new FragAskForMoney();
                switchFragment(newFragment, getString(R.string.menu_item_title_ask_for_money));
                break;
            case 6:
                newFragment = new ListBuy();
                switchFragment(newFragment, getString(R.string.toolbar_title_purchase));
                break;

            case 7:
                newFragment = new ReportTab();
                switchFragment(newFragment, getString(R.string.menu_item_title_report));
                break;

            case 9:
                newFragment = new ListMyProfile();
                switchFragment(newFragment, getString(R.string.myprofilelist_ab_title));
                break;
            case 10:
                newFragment = new ListMyFriends();
                switchFragment(newFragment, getString(R.string.toolbar_title_myfriends));
                break;
            case 12:
                newFragment = new ListSettings();
                switchFragment(newFragment, getString(R.string.menu_item_title_setting));
                break;
            case 13:
                newIntent = new Intent(getActivity(), HelpActivity.class);
                switchActivity(newIntent, MainPage.ACTIVITY_RESULT);
                break;
            case 14:
                newIntent = new Intent(getActivity(), AboutAppsActivity.class);
                switchActivity(newIntent, MainPage.ACTIVITY_RESULT);
                break;
            case 16:
                AlertDialog.Builder alertbox=new AlertDialog.Builder(getActivity());
                alertbox.setTitle(getString(R.string.warning));
                alertbox.setMessage(getString(R.string.exit_message));
                alertbox.setPositiveButton(getString(R.string.ok), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                switchLogout();
                            }
                        });
                alertbox.setNegativeButton(getString(R.string.cancel), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {}
                        });
                alertbox.show();
                break;
        }
    }

    private void showMyCustomDialog() {
        ReportBillerDialog dialog = new ReportBillerDialog();
        dialog.show(getActivity().getSupportFragmentManager(), "asfasfaf");
    }

    private void switchFragment(Fragment i, String name){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i,name);
    }

    private void switchLogout(){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchLogout();
    }


    private void switchActivity(Intent mIntent,int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,j);
    }

}