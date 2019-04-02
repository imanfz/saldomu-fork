package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MemberDapModel {
    @SerializedName("member_code")
    @Expose
    private String member_code;
    @SerializedName("member_id")
    @Expose
    private String member_id;
}
