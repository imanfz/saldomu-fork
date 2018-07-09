package com.sgo.saldomu.Beans;

import org.json.JSONException;
import org.json.JSONObject;

public class DenomOrderListModel {
    private String phoneNumber;
    private String pulsa;
    private String itemID;

    public DenomOrderListModel(String phoneNumber, String total){
        setPhoneNumber(phoneNumber);
        setPulsa(total);
    }

    public DenomOrderListModel(JSONObject obj){
        try {
            setPhoneNumber(obj.getString("item_phone"));
            setPulsa(obj.getString("item_qty"));
            setItemID(obj.getString("item_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPulsa() {
        return pulsa;
    }

    public void setPulsa(String pulsa) {
        this.pulsa = pulsa;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
}
