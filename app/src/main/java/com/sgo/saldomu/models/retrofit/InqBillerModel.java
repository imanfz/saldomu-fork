package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InqBillerModel extends jsonModel {
    @SerializedName("biller_input_amount")
    @Expose
    private
    String biller_input_amount;
    @SerializedName("biller_display_amount")
    @Expose
    private
    String biller_display_amount;
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("item_id")
    @Expose
    private
    String item_id;
    @SerializedName("ccy_id")
    @Expose
    private
    String ccy_id;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("item_name")
    @Expose
    private
    String item_name;
    @SerializedName("description")
    @Expose
    private
    DescriptionModel description;
    @SerializedName("admin_fee")
    @Expose
    private
    String admin_fee;
    @SerializedName("auth_type")
    @Expose
    private
    String auth_type;

    public String getBiller_input_amount() {
        return biller_input_amount;
    }

    public String getBiller_display_amount() {
        return biller_display_amount;
    }

    public String getTx_id() {
        return tx_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public String getAmount() {
        return amount;
    }

    public String getItem_name() {
        return item_name;
    }

    public DescriptionModel getDescription() {
        return description;
    }

    public String getAdmin_fee() {
        return admin_fee;
    }

    public String getAuth_type() {
        return auth_type;
    }
}
