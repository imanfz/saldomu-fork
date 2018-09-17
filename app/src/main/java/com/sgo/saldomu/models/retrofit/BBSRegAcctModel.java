package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BBSRegAcctModel extends GetTrxStatusReportModel {
    @SerializedName("benef_bank_code")
    @Expose
    private
    String benef_bank_code;
    @SerializedName("benef_city_name")
    @Expose
    private
    String benef_city_name;
    @SerializedName("benef_city_code")
    @Expose
    private
    String benef_city_code;

    public String getBenef_bank_code() {
        return benef_bank_code;
    }

    public String getBenef_city_name() {
        return benef_city_name;
    }

    public String getBenef_city_code() {
        return benef_city_code;
    }
}
