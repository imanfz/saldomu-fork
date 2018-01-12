package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.activeandroid.query.Update;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.receivers.LocalResultReceiver;
import com.sgo.saldomu.services.UpdateBBSData;

import org.joda.time.DateTime;
import org.joda.time.Days;

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

    public static void resetBBSData(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        bbsDataManager.setIsBBSDataUpdate(false);
    }

    private void setIsBBSDataUpdate(boolean toggle){
        sp.edit().putBoolean(DefineValue.IS_BBS_DATA_UPDATED,toggle).apply();
    }

    public static Boolean isDataCTANotValid(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        if(bbsDataManager.isMustUpdate())
            return true;

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if(sp.contains(DefineValue.UPDATE_TIME_BBS_CTA_DATA)){
            String curr_date = sp.getString(DefineValue.UPDATE_TIME_BBS_CTA_DATA,"");
//            Calendar checkCalendar = Calendar.getInstance();
            Date checkDate = DateTimeFormat.convertStringtoCustomDate(curr_date);
            //            return checkDate.compareTo(checkCalendar.getTime()) == 0;
            return DateTimeFormat.checkDateisMoreThan31Days(checkDate);
        }
        else {
            return false;
        }
    }

    public static Boolean isDataATCNotValid(){
        BBSDataManager bbsDataManager = new BBSDataManager();
        if(bbsDataManager.isMustUpdate())
            return true;

        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        if(sp.contains(DefineValue.UPDATE_TIME_BBS_ATC_DATA)){
            String curr_date = sp.getString(DefineValue.UPDATE_TIME_BBS_ATC_DATA,"");
//            Calendar checkCalendar = Calendar.getInstance();
            Date checkDate = DateTimeFormat.convertStringtoCustomDate(curr_date);
//            return checkDate.compareTo(checkCalendar.getTime()) == 0;
            return DateTimeFormat.checkDateisMoreThan31Days(checkDate);
        }
        else {
            return true;
        }
    }

    @NonNull
    public Boolean isValidToUpdate() {
        return isMustUpdate() || (isSameUser() && !isDataUpdated());
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
