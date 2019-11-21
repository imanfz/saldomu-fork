package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CashCollectionModel extends jsonModel {

    @SerializedName("customer_name")
    @Expose
    private
    String customer_name;
    @SerializedName("business_name")
    @Expose
    private
    String business_name;
    @SerializedName("cust_address")
    @Expose
    private
    String cust_address;
    @SerializedName("customer_code")
    @Expose
    private
    String customer_code;
    @SerializedName("accounts")
    @Expose
    private
    List<AccountsModel> accounts;

    public String getCustomer_name() {
        return customer_name;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public String getCust_address() {
        return cust_address;
    }

    public String getCustomer_code() {
        return customer_code;
    }

    public List<AccountsModel> getAccounts() {
        return accounts;
    }
}
