package com.sgo.hpku.coreclass;/*
  Created by Administrator on 10/1/2015.
 */

import java.util.Locale;

class LocaleUtils {

  public static Locale fromString(String locale) {
    String parts[] = locale.split("_", -1);
    if (parts.length == 1) return new Locale(parts[0]);
    else if (parts.length == 2
            || (parts.length == 3 && parts[2].startsWith("#")))
      return new Locale(parts[0], parts[1]);
    else return new Locale(parts[0], parts[1], parts[2]);
  }
}
