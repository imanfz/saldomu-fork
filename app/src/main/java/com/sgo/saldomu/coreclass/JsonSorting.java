package com.sgo.saldomu.coreclass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuddistirakiki on 10/27/17.
 */

public class JsonSorting {

    public static List<String> BPJSTrxStructSortingField(){
        List<String> jsonObject = new ArrayList<>();

        String field1 = "reff";
        String field2 = "va_number";
        String field3 = "customer_name";
        String field4 = "family_number";
        String field5 = "remain_payment";
        String field6 = "bill_period";
        String field7 = "bill_amount";
        String field8 = "catatan";
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
