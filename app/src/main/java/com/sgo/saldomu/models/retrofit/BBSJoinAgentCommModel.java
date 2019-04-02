package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BBSJoinAgentCommModel extends jsonModel{
    @SerializedName("community")
    @Expose
    private
    List<CommunityModel> community;

    public List<CommunityModel> getCommunity() {
        if (community == null)
            community = new ArrayList<>();
        return community;
    }
}
