package com.sgo.saldomu.coreclass;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuddistirakiki on 10/27/17.
 */

public class JsonSorting {

    public static List<String> BPJSInquirySortingField(){
        List<String> jsonObject = new ArrayList<>();

        String field1 = "VA NUMBER";
        String field2 = "CUSTOMER ID";
        String field3 = "CUSTOMER NAME";
        String field4 = "FAMILY NUMBER";
        String field5 = "BILL PERIOD";
        String field6 = "BILL AMOUNT";
        String field7 = "ADMIN FEE";
        String field8 = "AMOUNT";

        jsonObject.add(field1);
        jsonObject.add(field2);
        jsonObject.add(field3);
        jsonObject.add(field4);
        jsonObject.add(field5);
        jsonObject.add(field6);
        jsonObject.add(field7);
        jsonObject.add(field8);

        return jsonObject;
    }

    public static List<String> BPJSTrxStructSortingField(){
        List<String> jsonObject = new ArrayList<>();

        String field1 = "reff";
        String field2 = "product";
        String field3 = "va_number";
        String field4 = "ID Pelanggan";
        String field5 = "customer_name";
        String field6 = "family_number";
        String field7 = "bill_period";
        String field8 = "bill_amount";
        String field9 = "admin_fee";
        String field10 = "amount";
        String field11 = "message";

        jsonObject.add(field1);
        jsonObject.add(field2);
        jsonObject.add(field3);
        jsonObject.add(field4);
        jsonObject.add(field5);
        jsonObject.add(field6);
        jsonObject.add(field7);
        jsonObject.add(field8);
        jsonObject.add(field9);
        jsonObject.add(field10);
        jsonObject.add(field11);

        return jsonObject;
    }
}
