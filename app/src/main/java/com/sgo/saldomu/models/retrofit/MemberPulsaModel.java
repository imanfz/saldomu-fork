package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MemberPulsaModel extends jsonModel {
    @SerializedName("member_data")
    @Expose
    private
    JsonArray member_data;

    public JsonArray getMember_data() {
        return member_data;
    }
}
