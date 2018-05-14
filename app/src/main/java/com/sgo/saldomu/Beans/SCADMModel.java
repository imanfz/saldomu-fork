package com.sgo.saldomu.Beans;

/**
 * Created by Lenovo Thinkpad on 5/13/2018.
 */

public class SCADMModel {
    private String comm_id;

    private String comm_name;

    private String comm_code;

    private String api_key;

    private String member_code;

    private String member_id;

    public String getComm_id() {
        return comm_id;
    }

    public SCADMModel() {
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
    }

    public String getComm_name() {
        return comm_name;
    }

    public void setComm_name(String comm_name) {
        this.comm_name = comm_name;
    }

    public String getComm_code() {
        return comm_code;
    }

    public void setComm_code(String comm_code) {
        this.comm_code = comm_code;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getMember_code() {
        return member_code;
    }

    public void setMember_code(String member_code) {
        this.member_code = member_code;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }
}
