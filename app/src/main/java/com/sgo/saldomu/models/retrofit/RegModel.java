package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegModel extends jsonModel{
    @SerializedName("flag_process")
    @Expose
    private
    String flag_process;
    @SerializedName("cust_name")
    @Expose
    private
    String cust_name;
    @SerializedName("cust_phone")
    @Expose
    private
    String cust_phone;
    @SerializedName("cust_email")
    @Expose
    private
    String cust_email;
    @SerializedName("flag_change_pwd")
    @Expose
    private
    String flag_change_pwd;
    @SerializedName("flag_change_pin")
    @Expose
    private
    String flag_change_pin;

    public String getFlag_process() {
        return flag_process;
    }

    public String getCust_name() {
        return cust_name;
    }

    public String getCust_phone() {
        return cust_phone;
    }

    public String getCust_email() {
        return cust_email;
    }

    public String getFlag_change_pwd() {
        return flag_change_pwd;
    }

    public String getFlag_change_pin() {
        return flag_change_pin;
    }
}
