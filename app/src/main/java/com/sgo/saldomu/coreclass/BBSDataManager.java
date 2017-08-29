package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.content.Intent;

import com.activeandroid.query.Update;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.receivers.LocalResultReceiver;
import com.sgo.saldomu.services.UpdateBBSData;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import timber.log.Timber;
/**
 * Created by yuddistirakiki on 8/9/17.
 */

public class BBSDataManager {

    private SecurePreferences sp;

    public BBSDataManager(){
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
    }

    public static BBSDataManager checkAndRunService(Context context){
        BBSDataManager bbsDataManager = new BBSDataManager();
        if(!bbsDataManager.isDataUpdated()) {
            bbsDataManager.runServiceUpdateData(context);
            Timber.d("Run Service update data BBS");
        }
        return bbsDataManager;
    }

    public static void resetBBSData(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        bbsDataManager.setIsBBSDataUpdate(false);
    }

    private void setIsBBSDataUpdate(boolean toggle){
        sp.edit().putBoolean(DefineValue.IS_BBS_DATA_UPDATED,toggle).commit();
    }

    public Boolean isDataCTAValid(){
        if(sp.contains(DefineValue.UPDATE_TIME_BBS_CTA_DATA)){
            String curr_date = sp.getString(DefineValue.UPDATE_TIME_BBS_CTA_DATA,"");
            Calendar checkCalendar = Calendar.getInstance();
            Date checkDate = DateTimeFormat.convertStringtoCustomDateTime(curr_date);
            return checkDate.compareTo(checkCalendar.getTime()) == 0;
        }
        else {
            return false;
        }
    }

    public Boolean isDataATCValid(){
        if(sp.contains(DefineValue.UPDATE_TIME_BBS_ATC_DATA)){
            String curr_date = sp.getString(DefineValue.UPDATE_TIME_BBS_ATC_DATA,"");
            Calendar checkCalendar = Calendar.getInstance();
            Date checkDate = DateTimeFormat.convertStringtoCustomDateTime(curr_date);
            return checkDate.compareTo(checkCalendar.getTime()) == 0;
        }
        else {
            return false;
        }
    }

    public Boolean isDataUpdated(){
        return sp.getBoolean(DefineValue.IS_BBS_DATA_UPDATED,false);
    }

    public void runServiceUpdateData(Context mContext, LocalResultReceiver localResultReceiver){
        Intent intent = new Intent(mContext, UpdateBBSData.class);
        intent.putExtra("receiverTag", localResultReceiver);
        mContext.startService(intent);
    }

    public void runServiceUpdateData(Context mContext){
        Intent intent = new Intent(mContext, UpdateBBSData.class);
        mContext.startService(intent);
    }
}
