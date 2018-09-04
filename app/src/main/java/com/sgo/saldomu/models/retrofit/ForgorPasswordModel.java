package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForgorPasswordModel extends jsonModel{
    @SerializedName("failed_attempt")
    @Expose
    private
    int failed_attempt;
    @SerializedName("max_failed")
    @Expose
    private
    int max_failed;

    public int getFailed_attempt() {
        return failed_attempt;
    }

    public int getMax_failed() {
        return max_failed;
    }
}
