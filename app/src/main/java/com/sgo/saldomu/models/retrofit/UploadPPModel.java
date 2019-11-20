package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadPPModel extends jsonModel{
    @SerializedName("img_url")
    @Expose
    private
    String img_url;
    @SerializedName("img_small_url")
    @Expose
    private
    String img_small_url;
    @SerializedName("img_medium_url")
    @Expose
    private
    String img_medium_url;
    @SerializedName("img_large_url")
    @Expose
    private
    String img_large_url;

    public String getImg_url() {
        return img_url;
    }

    public String getImg_small_url() {
        return img_small_url;
    }

    public String getImg_medium_url() {
        return img_medium_url;
    }

    public String getImg_large_url() {
        return img_large_url;
    }
}
