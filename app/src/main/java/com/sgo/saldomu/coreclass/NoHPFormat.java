package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 7/14/2015.
 */

import com.sgo.saldomu.BuildConfig;

public class NoHPFormat {

    public static String getMNC(String iccid) {
        return iccid.substring(4, 6);
    }

    public static String getSMSVerifyDestination(String mobileNetworkCode) {
        String mobileDestination = "";
        switch(mobileNetworkCode) {
            case "11":  //XL, AXIS
                mobileDestination = BuildConfig.SMS_CENTER_XL;
                break;
            case "10":  //TELKOMSEL
                mobileDestination = BuildConfig.SMS_CENTER_TSEL;
                break;
            case "89":  //TRI
                mobileDestination = BuildConfig.SMS_CENTER_3;
                break;
            case "01":  //INDOSAT
                mobileDestination = BuildConfig.SMS_CENTER_IND;
                break;
            default:
                mobileDestination = BuildConfig.SMS_CENTER_TSEL;
                break;
        }
        return mobileDestination;
    }

  public static String formatTo62(String noHP){
      String result;

      if(!noHP.isEmpty()) {
          if (noHP.length()>5)
          {
              if (noHP.charAt(0) == '0') result = "62" + noHP.substring(1);
              else if (noHP.charAt(0) == '+') result = noHP.substring(1);
              else if (noHP.charAt(0) == '6') result = noHP;
              else if (noHP.charAt(0) == '6' && noHP.charAt(1) == '2') result = noHP;
              else if (noHP.charAt(0) == '9') result = noHP;
              else result = "62" + noHP;
              return result.replaceAll("[\\s\\-\\.\\^:,]","");
          }
      }

      return noHP;
  }

    public static String formatTo08(String noHP){
        String result;
        if(!noHP.isEmpty()) {
            if (noHP.length()>5)
            {
                if (noHP.charAt(0) == '+') result = "0" + noHP.substring(3);
                else if (noHP.charAt(0) == '6') result = "0" + noHP.substring(2);
                else if (noHP.charAt(0) == '2' && noHP.charAt(1) == '8') result = "0" +  noHP.substring(1);
                else if (noHP.charAt(0) == '9') result = noHP;
                else if (noHP.charAt(0) == '8') result = "0" + noHP;
                else if (noHP.charAt(0) == '0' && noHP.charAt(1) == '8') result = noHP;
                else result = noHP;
                return result.replaceAll("[\\s\\-\\.\\^:,]","");
            }
        }

        return noHP;
    }
}
