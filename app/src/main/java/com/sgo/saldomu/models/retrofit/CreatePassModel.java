package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreatePassModel extends jsonModel{
    @SerializedName("member_id")
    @Expose
    private
    String member_id;

    @SerializedName("flag_change_pwd")
    @Expose
    private
    String flag_change_pwd;

    public String getMember_id() {
        return member_id;
    }

    public String getFlag_change_pwd() {
        return flag_change_pwd;
    }
}
