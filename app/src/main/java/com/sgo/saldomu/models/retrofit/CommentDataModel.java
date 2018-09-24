package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommentDataModel {
    @SerializedName("id")
    @Expose
    private
    String id;
    @SerializedName("post_id")
    @Expose
    private
    String post_id;
    @SerializedName("from")
    @Expose
    private
    String from;
    @SerializedName("from_name")
    @Expose
    private
    String from_name;
    @SerializedName("from_profile_picture")
    @Expose
    private
    String from_profile_picture;
    @SerializedName("to")
    @Expose
    private
    String to;
    @SerializedName("to_name")
    @Expose
    private
    String to_name;
    @SerializedName("to_profile_picture")
    @Expose
    private
    String to_profile_picture;
    @SerializedName("reply")
    @Expose
    private
    String reply;
    @SerializedName("datetime")
    @Expose
    private
    String datetime;

    public String getId() {
        return id;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getFrom() {
        return from;
    }

    public String getFrom_name() {
        return from_name;
    }

    public String getFrom_profile_picture() {
        return from_profile_picture;
    }

    public String getTo() {
        return to;
    }

    public String getTo_name() {
        return to_name;
    }

    public String getTo_profile_picture() {
        return to_profile_picture;
    }

    public String getReply() {
        return reply;
    }

    public String getDatetime() {
        return datetime;
    }
}
