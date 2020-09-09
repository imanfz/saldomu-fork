package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BBSJoinAgentModel extends jsonModel{
    @SerializedName("member_code")
    @Expose
    private
    String member_code;

    public String getMember_code() {
        return member_code;
    }
}
