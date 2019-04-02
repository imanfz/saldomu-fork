package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BankCashoutModel extends jsonModel {
    @SerializedName("bank_cashout")
    @Expose
    private
    String bank_cashout;

    public String getBank_cashout() {
        return bank_cashout;
    }
}
