package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 12/15/2014.
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormat {


    private static final CharSequence symbols = ",";

    public static String format(double number) {
        DecimalFormat svSE = new DecimalFormat("#,###,###,##0.00");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        svSE.setDecimalFormatSymbols(symbols);

        return svSE.format(number);
    }

    public static String format1(double number) {
        DecimalFormat svSE = new DecimalFormat("#,###,###,##0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        svSE.setDecimalFormatSymbols(symbols);

        return svSE.format(number);
    }

    public static String format(String number) {
        if (number == null)
            return "";

        if (number.contains(symbols) || number.isEmpty())
            return number;
        double temp = Double.valueOf(number);
        return format(temp);
    }

    public static String format1(String number) {
        if (number == null)
            return "";

        if (number.contains(symbols) || number.isEmpty())
            return number;
        double temp = Double.valueOf(number);
        return format(temp);
    }

    public static String format(int number) {
        double temp = (double) number;
        return format(temp);
    }

    public static String format1(int number) {
        double temp = (double) number;
        return format(temp);
    }

    public static String deleteDecimal(String number) {
        if (!number.isEmpty()) {
            double temp1 = Double.valueOf(number);
            if (temp1 == (long) temp1)
                return String.valueOf((int) temp1);
        }
        return number;
    }


}
