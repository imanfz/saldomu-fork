package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetTrxStatusModel extends jsonModel{
    @SerializedName("tx_status")
    @Expose
    private
    String tx_status;
    @SerializedName("created")
    @Expose
    private
    String created;
    @SerializedName("tx_remark")
    @Expose
    private
    String tx_remark;
    @SerializedName("biller_detail")
    @Expose
    private
    String biller_detail;
    @SerializedName("merchant_type")
    @Expose
    private
    String merchant_type;
    @SerializedName("buss_scheme_code")
    @Expose
    private
    String buss_scheme_code;
    @SerializedName("buss_scheme_name")
    @Expose
    private
    String buss_scheme_name;
    @SerializedName("product_name")
    @Expose
    private
    String product_name;
    @SerializedName("detail")
    @Expose
    private
    String detail;

    public String getTx_status() {
        return tx_status;
    }

    public String getCreated() {
        return created;
    }

    public String getTx_remark() {
        return tx_remark;
    }

    public String getBiller_detail() {
        return biller_detail;
    }

    public String getMerchant_type() {
        return merchant_type;
    }

    public String getBuss_scheme_code() {
        return buss_scheme_code;
    }

    public String getBuss_scheme_name() {
        return buss_scheme_name;
    }

    public String getProduct_name() {
        return product_name;
    }
}
