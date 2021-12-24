package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SentPaymentBillerModel extends jsonModel {
    @SerializedName("merchant_type")
    @Expose
    private
    String merchant_type;
    @SerializedName("failed_attempt")
    @Expose
    private
    int failed_attempt;
    @SerializedName("max_failed")
    @Expose
    private
    int max_failed;
    @SerializedName("additional_fee")
    @Expose
    private
    String additional_fee;
    @SerializedName("total_amount")
    @Expose
    private
    String total_amount;
    @SerializedName("fee")
    @Expose
    private
    String fee;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("callback_url")
    @Expose
    private
    String callback_url;

    public String getMerchant_type() {
        return merchant_type;
    }

    public String getAdditional_fee() {
        return additional_fee;
    }

    public String getTotal_amount()
    {
        return total_amount;
    }

    public int getFailed_attempt() {
        return failed_attempt;
    }

    public int getMax_failed() {
        return max_failed;
    }

    public String getFee() {
        return fee;
    }

    public String getAmount() {
        return amount;
    }

    public String getCallback_url() {
        return callback_url;
    }
}
