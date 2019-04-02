package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommentModel extends GetCountModel {
    @SerializedName("data_comments")
    @Expose
    private
    List<CommentDataModel> data_comments;

    public List<CommentDataModel> getData_comments() {
        return data_comments;
    }
}
