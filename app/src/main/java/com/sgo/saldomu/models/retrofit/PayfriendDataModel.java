package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PayfriendDataModel {
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("tx_status")
    @Expose
    private
    String tx_status;
    @SerializedName("tx_remark")
    @Expose
    private
    String tx_remark;

    public String getTx_id() {
        return tx_id;
    }

    public String getTx_status() {
        return tx_status;
    }

    public String getTx_remark() {
        return tx_remark;
    }
}
