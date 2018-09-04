package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SentExecCustModel extends jsonModel {
    @SerializedName("allow_member_level")
    @Expose
    private
    String allow_member_level;

    public String getAllow_member_level() {
        return allow_member_level;
    }
}
