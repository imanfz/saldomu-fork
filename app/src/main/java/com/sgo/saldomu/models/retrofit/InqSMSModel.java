package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InqSMSModel extends jsonModel{
    @SerializedName("sender_id")
    @Expose
    private
    String sender_id;
    @SerializedName("is_new_user")
    @Expose
    private
    Integer is_new_user;

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public Integer getIs_new_user() {
        return is_new_user;
    }

    public void setIs_new_user(Integer is_new_user) {
        this.is_new_user = is_new_user;
    }
}
