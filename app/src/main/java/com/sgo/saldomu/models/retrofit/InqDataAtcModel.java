package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InqDataAtcModel extends jsonModel {
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("agent_member_name")
    @Expose
    private
    String agent_member_name;
    @SerializedName("fee")
    @Expose
    private
    String fee;
    @SerializedName("total")
    @Expose
    private
    String total;
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("ccy_id")
    @Expose
    private
    String ccy_id;

    public String getAmount() {
        return amount;
    }

    public String getAgent_member_name() {
        return agent_member_name;
    }

    public String getFee() {
        return fee;
    }

    public String getTotal() {
        return total;
    }

    public String getTx_id() {
        return tx_id;
    }

    public String getCcy_id() {
        return ccy_id;
    }
}
