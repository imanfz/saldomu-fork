package com.sgo.saldomu.Beans;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TagihModel extends RealmObject {
    @PrimaryKey
    private String id;
    private String anchor_name;
    private String anchor_cust;
    private RealmList<com.sgo.saldomu.Beans.TagihCommunityModel> listCommunity;

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

    public RealmList<com.sgo.saldomu.Beans.TagihCommunityModel> getListCommunity() {
        return listCommunity;
    }

    public void setListCommunity(RealmList<com.sgo.saldomu.Beans.TagihCommunityModel> listCommunity) {
        this.listCommunity = listCommunity;
    }
}
