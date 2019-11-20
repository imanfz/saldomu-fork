package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CategoriesModel {
    @SerializedName("category_id")
    @Expose
    private
    String category_id;
    @SerializedName("scheme_code")
    @Expose
    private
    String scheme_code;
    @SerializedName("category_name")
    @Expose
    private
    String category_name;

    public String getCategory_id() {
        return category_id;
    }

    public String getScheme_code() {
        return scheme_code;
    }

    public String getCategory_name() {
        return category_name;
    }
}
