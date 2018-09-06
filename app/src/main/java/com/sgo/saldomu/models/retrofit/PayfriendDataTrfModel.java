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

    public String getMember_status() {
        return member_status;
    }

    public String getMember_remark() {
        return member_remark;
    }
}
