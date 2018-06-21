package com.sgo.saldomu.coreclass.Singleton;

import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.Beans.listBankModel;

import java.util.ArrayList;

public class DataManager {

    private static DataManager singleton;

    private SCADMCommunityModel item;
    private ArrayList<DenomListModel> itemList;
    private ArrayList<listBankModel> bankData;

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

    public ArrayList<DenomListModel> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<DenomListModel> orderList) {
        this.itemList = orderList;
    }

    public ArrayList<listBankModel> getBankData() {
        return bankData;
    }

    public void setBankData(ArrayList<listBankModel> bankData) {
        this.bankData = bankData;
    }
}
