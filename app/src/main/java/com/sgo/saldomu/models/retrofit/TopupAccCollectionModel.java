package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopupAccCollectionModel extends jsonModel {
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("product_code")
    @Expose
    private
    String product_code;
    @SerializedName("product_name")
    @Expose
    private
    String product_name;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("bank_name")
    @Expose
    private
    String bank_name;
    @SerializedName("bank_code")
    @Expose
    private
    String bank_code;
    @SerializedName("auth_type")
    @Expose
    private
    String auth_type;
    @SerializedName("fee")
    @Expose
    private
    String fee;

    public String getTx_id() {
        return tx_id;
    }

    public String getProduct_code() {
        return product_code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getAmount() {
        return amount;
    }

    public String getBank_name() {
        return bank_name;
    }

    public String getBank_code() {
        return bank_code;
    }

    public String getAuth_type() {
        return auth_type;
    }

    public String getFee() {
        return fee;
    }
}
