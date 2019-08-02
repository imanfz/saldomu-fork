package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BankCashoutModel {
    @SerializedName("bank_code")
    @Expose
    private
    String bank_code;

    @SerializedName("bank_name")
    @Expose
    private
    String bank_name;

    @SerializedName("bank_gateway")
    @Expose
    private
    String bank_gateway;

    public String getBank_code() {
        return bank_code;
    }

    public String getBank_name() {
        return bank_name;
    }

    public String getBank_gateway() {
        return bank_gateway;
    }
}
