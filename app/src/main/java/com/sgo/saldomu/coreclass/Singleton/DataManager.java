package com.sgo.saldomu.coreclass.Singleton;

import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.DenomListModel;
import com.sgo.saldomu.Beans.DenomOrderListModel;
import com.sgo.saldomu.Beans.SCADMCommunityModel;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.models.InvoiceDGI;

import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static DataManager singleton;

    private SCADMCommunityModel item;
    private ArrayList<DenomListModel> itemList;
    private ArrayList<listBankModel> bankData;
    private BankDataTopUp temp_other_atm;
    private List<InvoiceDGI> listInvoice;
    private RequestParams invoiceParam;

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

    public BankDataTopUp getTemp_other_atm() {
        return temp_other_atm;
    }

    public void setTemp_other_atm(BankDataTopUp temp_other_atm) {
        this.temp_other_atm = temp_other_atm;
    }

    public List<InvoiceDGI> getListInvoice() {
        return listInvoice;
    }

    public void setListInvoice(List<InvoiceDGI> listInvoice) {
        this.listInvoice = listInvoice;
    }

    public RequestParams getInvoiceParam() {
        return invoiceParam;
    }

    public void setInvoiceParam(RequestParams invoiceParam) {
        this.invoiceParam = invoiceParam;
    }
}
