package com.sgo.saldomu.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TagihCommunityModel extends RealmObject {
    @PrimaryKey
    private String id;
    private String comm_code;
    private String comm_name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComm_code() {
        return comm_code;
    }

    public void setComm_code(String comm_code) {
        this.comm_code = comm_code;
    }

    public String getComm_name() {
        return comm_name;
    }

    public void setComm_name(String comm_name) {
        this.comm_name = comm_name;
    }
}
