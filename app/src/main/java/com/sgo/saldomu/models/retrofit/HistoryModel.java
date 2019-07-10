package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HistoryModel extends jsonModel{
    @SerializedName("amount")
    @Expose
    private
    String amount;

    @SerializedName("history_type")
    @Expose
    private
    String history_type;

    @SerializedName("history_detail_type")
    @Expose
    private
    String history_detail_type;

    @SerializedName("history_datetime")
    @Expose
    private
    String history_datetime;

    @SerializedName("tx_id_emo")
    @Expose
    private
    String tx_id_emo;

    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;

    @SerializedName("tx_type")
    @Expose
    private
    String tx_type;

    @SerializedName("comm_id")
    @Expose
    private
    String comm_id;

    @SerializedName("buss_scheme_code")
    @Expose
    private
    String buss_scheme_code;

    public String getAmount() {
        return amount;
    }

    public String getHistory_type() {
        return history_type;
    }

    public String getHistory_detail_type() {
        return history_detail_type;
    }

    public String getHistory_datetime() {
        return history_datetime;
    }

    public String getTx_id_emo() {
        return tx_id_emo;
    }

    public String getTx_id() {
        return tx_id;
    }

    public String getTx_type() {
        return tx_type;
    }

    public String getComm_id() {
        return comm_id;
    }

    public String getBuss_scheme_code() {
        return buss_scheme_code;
    }
}
