package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreatePinModel extends jsonModel {
    @SerializedName("flag_change_pin")
    @Expose
    private
    String flag_change_pin;

    public String getFlag_change_pin() {
        return flag_change_pin;
    }
}
