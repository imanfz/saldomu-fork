package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SentDataPayfriendModel extends jsonModel {
    @SerializedName("data_transfer")
    @Expose
    private
    List<PayfriendDataTrfModel> data_transfer;
    @SerializedName("message")
    @Expose
    private
    String message;
    @SerializedName("data_mapper")
    @Expose
    private
    String data_mapper;

    public List<PayfriendDataTrfModel> getData_transfer() {
        return data_transfer;
    }

    public String getMessage() {
        return message;
    }

    public String getData_mapper() {
        return data_mapper;
    }
}
