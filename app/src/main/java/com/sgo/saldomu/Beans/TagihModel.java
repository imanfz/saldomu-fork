package com.sgo.saldomu.Beans;

import com.sgo.saldomu.models.TagihCommunityModel;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TagihModel {
    @PrimaryKey
    private String id;
    private String anchor_name;
    private String anchor_cust;
    private ArrayList<TagihCommunityModel> listCommunity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnchor_name() {
        return anchor_name;
    }

    public void setAnchor_name(String anchor_name) {
        this.anchor_name = anchor_name;
    }

    public String getAnchor_cust() {
        return anchor_cust;
    }

    public void setAnchor_cust(String anchor_cust) {
        this.anchor_cust = anchor_cust;
    }

    public ArrayList<TagihCommunityModel> getListCommunity() {
        return listCommunity;
    }

    public void setListCommunity(ArrayList<TagihCommunityModel> listCommunity) {
        this.listCommunity = listCommunity;
    }
}
