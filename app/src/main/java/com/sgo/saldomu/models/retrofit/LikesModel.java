package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LikesModel extends GetCountModel {

    @SerializedName("data_likes")
    @Expose
    private
    JsonArray data_likes;

    public JsonArray getData_likes() {
        return data_likes;
    }
}
