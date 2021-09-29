package com.sgo.saldomu.entityRealm;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yuddistirakiki on 4/20/17.
 */

public class BBSCommModel extends RealmObject {
    @Ignore
    public final static String COMM_ID = "comm_id";
    @Ignore
    public final static String SCHEME_CODE = "scheme_code";

    @PrimaryKey
    private String comm_id;
    private String comm_code;
    private String comm_name;
    private String api_key;
    private String callback_url;
    private String scheme_code;
    private String member_id;
    private String member_code;
    private String last_update;

    public BBSCommModel(){}


    public BBSCommModel(String commId, String commCode, String commName, String apiKey,
                        String memberCode, String callbackUrl){
        this.setComm_id(commId);
        this.setComm_code(commCode);
        this.setComm_name(commName);
        this.setApi_key(apiKey);
        this.setMember_code(memberCode);
        this.setCallback_url(callbackUrl);
    }

    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
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

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getCallback_url() {
        return callback_url;
    }

    public void setCallback_url(String callback_url) {
        this.callback_url = callback_url;
    }

    public String getScheme_code() {
        return scheme_code;
    }

    public void setScheme_code(String scheme_code) {
        this.scheme_code = scheme_code;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getMember_code() {
        return member_code;
    }

    public void setMember_code(String member_code) {
        this.member_code = member_code;
    }
}
