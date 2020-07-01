package com.sgo.saldomu.utils;

import android.content.Context;

public class CustomStringUtil {
    private static CustomStringUtil instance = null;
    Context context;


    public static String filterPhoneNo(String phoneNo){

        if(phoneNo.length() > 5){


            if(phoneNo.contains(" ")){
                phoneNo = phoneNo.replace(" ","");
            }


            if(phoneNo.contains("-")){
                phoneNo = phoneNo.replace("-","");
            }
            if(phoneNo.contains("+62")){
                phoneNo = phoneNo.replace("+62","0");
            }

            return phoneNo;
        }

        return phoneNo;
    }
}
