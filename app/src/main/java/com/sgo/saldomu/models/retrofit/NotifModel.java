package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotifModel extends jsonModel {
    @SerializedName("data_user_notif")
    @Expose
    private
    JsonArray data_user_notif;

    public JsonArray getData_user_notif() {
        return data_user_notif;
    }
}
