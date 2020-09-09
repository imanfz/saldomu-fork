package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BankListModel extends jsonModel {
    @SerializedName("other_atm")
    @Expose
    private
    String other_atm;
    @SerializedName("bank_data")
    @Expose
    private
    JsonObject bank_data;

    public String getOther_atm() {
        return other_atm;
    }

    public JsonObject getBank_data() {
        return bank_data;
    }
}
