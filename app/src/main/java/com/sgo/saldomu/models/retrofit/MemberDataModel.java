package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MemberDataModel {
    @SerializedName("member_code")
    @Expose
    private String member_code;
    @SerializedName("member_id")
    @Expose
    private String member_id;
    @SerializedName("member_name")
    @Expose
    private String member_name;

    public String getMember_code() {
        return member_code;
    }

    public String getMember_id() {
        return member_id;
    }

    public String getMember_name() {
        return member_name;
    }
}
