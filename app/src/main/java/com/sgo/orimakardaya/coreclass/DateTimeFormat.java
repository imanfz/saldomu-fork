package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 7/10/2015.
 */

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeFormat {

    private static final Long OneHourMs = 3600000L;

    public static SimpleDateFormat getFormatYearHours(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
    }


    public static Long getCurrentDateTimeMillis(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }


    public static String getCurrentDateTime(){
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", new Locale("ID","INDONESIA"));
      return df.format(Calendar.getInstance().getTime());
    }

    public static String formatToID (String _date){
      if(_date.isEmpty())
          return null;

      DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", new Locale("ID","INDONESIA"));
      DateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss", new Locale("ID","INDONESIA"));
      String newDate = "";
      try {
        newDate = toFormat.format(fromFormat.parse(_date));
      } catch (ParseException e) {
        e.printStackTrace();
      }
      return newDate;
    }

    public static String getCurrentDateTimeScalable(Context mContext){
      Boolean is24hours = android.text.format.DateFormat.is24HourFormat(mContext);
      DateFormat df ;
      if (is24hours) {
          df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", new Locale("ID","INDONESIA"));
          return df.format(Calendar.getInstance().getTime());
      }else{
          df = new SimpleDateFormat("yyyy-MM-dd hh:mm a", new Locale("ID","INDONESIA"));
          return df.format(Calendar.getInstance().getTime());
      }
    }

    public static String getCurrentDate(){
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
      return df.format(Calendar.getInstance().getTime());
    }

    public static Date getCurrDate(){
        return Calendar.getInstance().getTime();
    }

    public static String getCurrentDate(int minus){
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
      Calendar calendar=Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_YEAR, -minus);
      return df.format(calendar.getTime());
    }

    public static java.util.Date convertCustomDateTime(String _date){
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
      try {
          return dateFormat.parse(_date);
      } catch (ParseException e) {
          e.printStackTrace();
      }
      return null;
    }

    public static java.util.Date convertCustomDate(String _date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
        try {
            return dateFormat.parse(_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertMilisToMinute(Long _milisecond){
        SimpleDateFormat dateFormat;
        if(_milisecond < OneHourMs) {
            dateFormat = new SimpleDateFormat("m:ss", new Locale("ID", "INDONESIA"));

        }
        else {
            dateFormat = new SimpleDateFormat("HH:mm:ss", new Locale("ID", "INDONESIA"));

        }
        return dateFormat.format(new Date(_milisecond));
    }

}
