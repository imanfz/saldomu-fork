package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PayfriendDataTrfModel {
    @SerializedName("member_status")
    @Expose
    private
    String member_status;
    @SerializedName("member_remark")
    @Expose
    private
    String member_remark;
    @SerializedName("member_code_to")
    @Expose
    private
    String member_code_to;
    @SerializedName("member_name_to")
    @Expose
    private
    String member_name_to;
    @SerializedName("member_phone")
    @Expose
    private
    String member_phone;
    @SerializedName("ccy_id")
    @Expose
    private
    String ccy_id;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("total")
    @Expose
    private
    String total;
    @SerializedName("is_member_temp")
    @Expose
    private
    String is_member_temp;
    @SerializedName("fee")
    @Expose
    private
    String fee;
    @SerializedName("claimed_exp")
    @Expose
    private
    String claimed_exp;
    @SerializedName("exp_duration_hour")
    @Expose
    private
    String exp_duration_hour;
    @SerializedName("max_trf_to_this_hp")
    @Expose
    private
    String max_trf_to_this_hp;
    @SerializedName("qty_trf_to_this_hp")
    @Expose
    private
    String qty_trf_to_this_hp;

    public String getMember_status() {
        return member_status;
    }

    public String getMember_remark() {
        return member_remark;
    }

    public String getMember_code_to() {
        return member_code_to;
    }

    public String getMember_name_to() {
        return member_name_to;
    }

    public String getMember_phone() {
        return member_phone;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public String getAmount() {
        return amount;
    }

    public String getTx_id() {
        return tx_id;
    }

    public String getTotal() {
        return total;
    }

    public String getIs_member_temp() {
        return is_member_temp;
    }

    public String getFee() {
        return fee;
    }

    public String getClaimed_exp() {
        return claimed_exp;
    }

    public String getExp_duration_hour() {
        return exp_duration_hour;
    }

    public String getMax_trf_to_this_hp() {
        return max_trf_to_this_hp;
    }

    public String getQty_trf_to_this_hp() {
        return qty_trf_to_this_hp;
    }
}
