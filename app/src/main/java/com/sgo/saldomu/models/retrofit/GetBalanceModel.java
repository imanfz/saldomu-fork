package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetBalanceModel extends jsonModel {
    @SerializedName("unread_notif")
    @Expose
    private
    String unread_notif;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("max_topup")
    @Expose
    private
    String max_topup;
    @SerializedName("ccy_id")
    @Expose
    private
    String ccy_id;
    @SerializedName("remain_limit")
    @Expose
    private
    String remain_limit;
    @SerializedName("period_limit")
    @Expose
    private
    String period_limit;
    @SerializedName("next_reset")
    @Expose
    private
    String next_reset;

    public String getUnread_notif() {
        return unread_notif;
    }

    public String getAmount() {
        return amount;
    }

    public String getMax_topup() {
        return max_topup;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public String getRemain_limit() {
        return remain_limit;
    }

    public String getPeriod_limit() {
        return period_limit;
    }

    public String getNext_reset() {
        return next_reset;
    }
}
