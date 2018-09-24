package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommentModel extends jsonModel {
    @SerializedName("count")
    @Expose
    private
    String count;
    @SerializedName("data_comments")
    @Expose
    private
    List<CommentDataModel> data_comments;

    public String getCount() {
        return count;
    }

    public List<CommentDataModel> getData_comments() {
        return data_comments;
    }
}
