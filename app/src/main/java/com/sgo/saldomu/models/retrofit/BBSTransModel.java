package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BBSTransModel extends jsonModel{
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("tx_product_code")
    @Expose
    private
    String tx_product_code;
    @SerializedName("tx_product_name")
    @Expose
    private
    String tx_product_name;
    @SerializedName("tx_bank_code")
    @Expose
    private
    String tx_bank_code;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("admin_fee")
    @Expose
    private
    String admin_fee;
    @SerializedName("total_amount")
    @Expose
    private
    String total_amount;
    @SerializedName("tx_bank_name")
    @Expose
    private
    String tx_bank_name;
    @SerializedName("max_resend_token")
    @Expose
    private
    String max_resend_token;
    @SerializedName("benef_product_value_code")
    @Expose
    private
    String benef_product_value_code;
    @SerializedName("benef_product_value_name")
    @Expose
    private
    String benef_product_value_name;

    public String getTx_id() {
        return tx_id;
    }

    public void setTx_id(String tx_id) {
        this.tx_id = tx_id;
    }

    public String getTx_product_code() {
        return tx_product_code;
    }

    public void setTx_product_code(String tx_product_code) {
        this.tx_product_code = tx_product_code;
    }

    public String getTx_product_name() {
        return tx_product_name;
    }

    public void setTx_product_name(String tx_product_name) {
        this.tx_product_name = tx_product_name;
    }

    public String getTx_bank_code() {
        return tx_bank_code;
    }

    public void setTx_bank_code(String tx_bank_code) {
        this.tx_bank_code = tx_bank_code;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAdmin_fee() {
        return admin_fee;
    }

    public void setAdmin_fee(String admin_fee) {
        this.admin_fee = admin_fee;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getTx_bank_name() {
        return tx_bank_name;
    }

    public void setTx_bank_name(String tx_bank_name) {
        this.tx_bank_name = tx_bank_name;
    }

    public String getMax_resend_token() {
        return max_resend_token;
    }

    public void setMax_resend_token(String max_resend_token) {
        this.max_resend_token = max_resend_token;
    }

    public String getBenef_product_value_code() {
        return benef_product_value_code;
    }

    public void setBenef_product_value_code(String benef_product_value_code) {
        this.benef_product_value_code = benef_product_value_code;
    }

    public String getBenef_product_value_name() {
        return benef_product_value_name;
    }

    public void setBenef_product_value_name(String benef_product_value_name) {
        this.benef_product_value_name = benef_product_value_name;
    }
}
