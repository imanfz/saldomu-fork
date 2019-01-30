package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 7/10/2015.
 */

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeFormat {

    private static final Long OneHourMs = 3600000L;
    public static Locale locale_id = new Locale("ID","INDONESIA");

    public static SimpleDateFormat getFormatYearHours(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
    }

    public static boolean checkDateisMoreThan31Days(Date date){
        return Days.daysBetween(new DateTime(date),new DateTime()).isGreaterThan(Days.days(31));
    }

    public static Long getCurrentDateTimeMillis(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static String getCurrentDateTime(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
        return df.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDateTimeSMS(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", new Locale("ID","INDONESIA"));
        return df.format(Calendar.getInstance().getTime());
    }

    public static String formatToID (String _date){
        if(_date.isEmpty())
            return null;

        DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
        DateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", new Locale("ID","INDONESIA"));
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

    public static String getCurrentDateMinus(int minus){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -minus);
        return df.format(calendar.getTime());
    }

    public static String getCurrentDatePlus(int plus){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, plus);
        return df.format(calendar.getTime());
    }

    public static java.util.Date convertStringtoCustomDateTime(String _date){
        if(_date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID", "INDONESIA"));
            try {
                return dateFormat.parse(_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return Calendar.getInstance().getTime();
    }

    public static String getCurrentDateTimePlusHour(int plus){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.HOUR, plus);
        return df.format(calendar.getTime());
    }

    public static java.util.Date convertStringtoCustomDate(String _date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID","INDONESIA"));
        try {
            return dateFormat.parse(_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertDatetoString(Date _date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ID","INDONESIA"));
        return dateFormat.format(_date);
    }

    public static String convertDatetoString(Date _date, String format){
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, new Locale("id","ID"));
        return dateFormat.format(_date);
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