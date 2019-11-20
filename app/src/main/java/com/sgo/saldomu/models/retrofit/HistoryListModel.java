package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HistoryListModel extends jsonModel {
    @SerializedName("count")
    @Expose
    private
    String count;
    @SerializedName("data_posts")
    @Expose
    private
    JsonArray data_posts;

    public String getCount() {
        return count;
    }

    public JsonArray getData_posts() {
        return data_posts;
    }
}
