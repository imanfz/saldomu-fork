package com.sgo.saldomu.Beans;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DenomListModel {
    private String itemName;
    private String itemID;
    private ArrayList<DenomOrderListModel> orderList;

    public DenomListModel(JSONObject obj){
        try {
            setItemName(obj.getString("item_name"));
            setItemID(obj.getString("item_id"));
            setOrderList(new ArrayList<DenomOrderListModel>());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public ArrayList<DenomOrderListModel> getOrderList() {
        return orderList;
    }

    public void setOrderList(ArrayList<DenomOrderListModel> orderList) {
        this.orderList = orderList;
    }
}
