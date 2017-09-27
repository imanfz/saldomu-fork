package com.sgo.saldomu.Beans;

/**
 * Created by yuddistirakiki on 3/21/17.
 */

public class ListBankDataTopup {
    private listBankModel listBankModel;
    private  Boolean isVisible;

    public ListBankDataTopup(listBankModel listBankModel){
        this.setListBankModel(listBankModel);
        this.setVisible(false);
    }

    public String getProductName(){
        return getListBankModel().getProduct_name();
    }

    public String getBankName(){
        return getListBankModel().getBank_name();
    }

    public String getProductH2H(){
        return getListBankModel().getProduct_h2h();
    }

    public String getProductType(){
        return getListBankModel().getProduct_type();
    }

    public String getBankCode(){
        return getListBankModel().getBank_code();
    }

    public String getProductCode(){
        return getListBankModel().getProduct_code();
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public listBankModel getListBankModel() {
        return listBankModel;
    }

    public void setListBankModel(listBankModel listBankModel) {
        this.listBankModel = listBankModel;
    }
}
