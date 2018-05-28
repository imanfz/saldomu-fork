package com.sgo.saldomu.coreclass.Singleton;

import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;

import java.util.ArrayList;

public class DataManager {

    private static DataManager singleton;

    private SCADMCommunityModel item;
    private ArrayList<DenomListModel> itemList;

    public static DataManager getInstance(){
        if (singleton == null){
            singleton = new DataManager();
        }
        return singleton;
    }

    public void setSACDMCommMod(SCADMCommunityModel item){
        this.item = item;
    }

    public SCADMCommunityModel getSACDMCommMod(){
        return item;
    }

    public ArrayList<DenomListModel> getOrderList() {
        return itemList;
    }

    public void setOrderList(ArrayList<DenomListModel> orderList) {
        this.itemList = orderList;
    }
}
