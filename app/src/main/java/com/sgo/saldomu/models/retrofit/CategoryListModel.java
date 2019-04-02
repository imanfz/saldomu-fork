package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryListModel extends jsonModel {
    @SerializedName("category")
    @Expose
    private
    List<CategoriesModel> categories;

    public List<CategoriesModel> getCategories() {
        return categories;
    }
}
