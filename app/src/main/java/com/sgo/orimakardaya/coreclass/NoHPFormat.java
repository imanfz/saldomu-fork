package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 7/14/2015.
 */

public class NoHPFormat {

  public static String editNoHP (String noHP){
      String result = null;
      if(noHP.charAt(0) == '0')result = "62"+noHP.substring(1);
      else if(noHP.charAt(0) == '+' )result = noHP.substring(1);
      else if(noHP.charAt(0) == '6') result = noHP;
      else if(noHP.charAt(0) == '6' && noHP.charAt(1) == '2') result = noHP;
      else if(noHP.charAt(0) == '9') result = noHP;
      else result = "62"+noHP;

      return result.replaceAll("[\\s\\-\\.\\^:,]","");
  }
}
