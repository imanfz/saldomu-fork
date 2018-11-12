package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.receivers.LocalResultReceiver;
import com.sgo.saldomu.services.UpdateBBSData;

import java.util.Date;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/9/17.
 */

public class BBSDataManager {

    private SecurePreferences sp;

    public BBSDataManager(){
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    public static void resetBBSData(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        bbsDataManager.setIsBBSDataUpdate(false);
    }

    private void setIsBBSDataUpdate(boolean toggle){
        sp.edit().putBoolean(DefineValue.IS_BBS_DATA_UPDATED,toggle).apply();
    }

    public static Boolean isDataCTANotValid(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        boolean a = bbsDataManager.isMustUpdate();
        boolean b = bbsDataManager.isSameUser();
        Log.d("bbs data manager", "a: " + a + ", b: " + b);
        if(a || !b) {
            Timber.d("return true data cta must update");
            return true;
        }


        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if(sp.contains(DefineValue.UPDATE_TIME_BBS_CTA_DATA)){
            String curr_date = sp.getString(DefineValue.UPDATE_TIME_BBS_CTA_DATA,"");
//            Calendar checkCalendar = Calendar.getInstance();
            Date checkDate = DateTimeFormat.convertStringtoCustomDate(curr_date);
            //            return checkDate.compareTo(checkCalendar.getTime()) == 0;
            Timber.d("return dari checkDateismorethan31days data cta ");
            return DateTimeFormat.checkDateisMoreThan31Days(checkDate);
        }
        else {
            Timber.d("return true data cta not valid");
            return true;
        }
    }

    public static Boolean isDataATCNotValid(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        boolean a = bbsDataManager.isMustUpdate();
        boolean b = !bbsDataManager.isSameUser();
        Log.d("bbs data manager", "a: " + a + ", b: " + b);
        if(a || b) {
//        if(bbsDataManager.isMustUpdate() || !bbsDataManager.isSameUser()) {
            Timber.d("return true data atc must update");
            return true;
        }

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if(sp.contains(DefineValue.UPDATE_TIME_BBS_ATC_DATA)){
            String curr_date = sp.getString(DefineValue.UPDATE_TIME_BBS_ATC_DATA,"");
//            Calendar checkCalendar = Calendar.getInstance();
            Date checkDate = DateTimeFormat.convertStringtoCustomDate(curr_date);
//            return checkDate.compareTo(checkCalendar.getTime()) == 0;
            Timber.d("return dari checkDateismorethan31days data atc ");
            return DateTimeFormat.checkDateisMoreThan31Days(checkDate);
        }
        else {
            Timber.d("return true data atc not valid");
            return true;
        }
    }

    @NonNull
    public Boolean isValidToUpdate() {
        return  isMustUpdate() || ! isSameUser() || isRealmBBSVersionNotSame() || !isDataUpdated();
    }

    private Boolean isRealmBBSVersionNotSame(){
        long oldver = RealmManager.getCurrentVersionRealm(RealmManager.BBSConfiguration);
        long currver = BuildConfig.REALM_SCHEME_BBS_VERSION;
        return oldver != currver;
    }

    @NonNull
    private Boolean isMustUpdate(){
        return sp.getBoolean(DefineValue.IS_MUST_UPDATE_BBS_DATA,false);
    }

    @NonNull
    private Boolean isSameUser(){
        return sp.getBoolean(DefineValue.IS_SAME_PREVIOUS_USER,false);
    }

    public Boolean isDataUpdated(){
        return sp.getBoolean(DefineValue.IS_BBS_DATA_UPDATED,false);
    }

    public void runServiceUpdateData(Context mContext){
        runServiceUpdateData(mContext,null);
    }

    public void runServiceUpdateData(Context mContext, LocalResultReceiver localResultReceiver){
        Intent intent = new Intent(mContext, UpdateBBSData.class);
        if(localResultReceiver != null)
            intent.putExtra("receiverTag", localResultReceiver);
        mContext.startService(intent);
    }


}
