package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 12/15/2014.
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormat {

  public static String format(double number){
    DecimalFormat svSE = new DecimalFormat("#,###,###,##0.00");
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
    symbols.setDecimalSeparator(',');
    symbols.setGroupingSeparator('.');
    svSE.setDecimalFormatSymbols(symbols);

    return svSE.format(number);
  }

  public static String format(String number){
      double temp = Double.valueOf(number);
      return format(temp);
  }

  public static String format(int number){
    double temp = (double) number;
    return format(temp);
  }

}
