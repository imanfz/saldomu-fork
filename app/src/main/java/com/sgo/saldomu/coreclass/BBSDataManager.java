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

/**
 * Created by yuddistirakiki on 8/9/17.
 */

public class BBSDataManager {

    Realm realm;
    SecurePreferences sp;

    public BBSDataManager(){
        realm = Realm.getInstance(RealmManager.BBSConfiguration);
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
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
