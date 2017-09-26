package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 7/14/2015.
 */

public class NoHPFormat {

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

      return "";
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
                else result = "08" + noHP;
                return result.replaceAll("[\\s\\-\\.\\^:,]","");
            }
        }

        return "";
    }
}
