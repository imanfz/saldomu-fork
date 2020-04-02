package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountsModel {
    @SerializedName("acct_no")
    @Expose
    private
    String acct_no;
    @SerializedName("acct_name")
    @Expose
    private
    String acct_name;

    public String getAcct_no() {
        return acct_no;
    }

    public String getAcct_name() {
        return acct_name;
    }
}
