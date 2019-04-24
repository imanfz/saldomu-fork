package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InqSOFModel extends jsonModel {
    @SerializedName("comm_name")
    @Expose
    private
    String comm_name;
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("payment_remark")
    @Expose
    private
    String payment_remark;
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
    @SerializedName("order_no")
    @Expose
    private
    String order_no;
    @SerializedName("comm_code")
    @Expose
    private
    String comm_code;
    @SerializedName("merchant_code")
    @Expose
    private
    String merchant_code;
    @SerializedName("product_code")
    @Expose
    private
    String product_code;
    @SerializedName("comm_id")
    @Expose
    private
    String comm_id;

    public String getComm_name() {
        return comm_name;
    }

    public String getTx_id() {
        return tx_id;
    }

    public String getPayment_remark() {
        return payment_remark;
    }

    public String getAmount() {
        return amount;
    }

    public String getAdmin_fee() {
        return admin_fee;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public String getOrder_no() {
        return order_no;
    }

    public String getComm_code() {
        return comm_code;
    }

    public String getMerchant_code() {
        return merchant_code;
    }

    public String getProduct_code() {
        return product_code;
    }

    public String getComm_id() {
        return comm_id;
    }
}
