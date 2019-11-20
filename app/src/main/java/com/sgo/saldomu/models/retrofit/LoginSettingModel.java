package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginSettingModel {
    @SerializedName("max_member_transfer")
    @Expose
    private String maxMemberTransfer;

    public String getMaxMemberTransfer() {
        return maxMemberTransfer;
    }

    public void setMaxMemberTransfer(String maxMemberTransfer) {
        this.maxMemberTransfer = maxMemberTransfer;
    }

}
