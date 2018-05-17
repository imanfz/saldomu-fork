package com.sgo.saldomu.Beans;

/**
 * Created by Lenovo Thinkpad on 5/13/2018.
 */

public class SCADMCommunityModel {
    private String comm_id;

    private String comm_name;

    private String comm_code;

    private String member_code;

    private String member_id_scadm;

    private String type;

    public String getComm_id() {
        return comm_id;
    }

    public SCADMCommunityModel() {
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

    public String getMember_code() {
        return member_code;
    }

    public void setMember_code(String member_code) {
        this.member_code = member_code;
    }

    public String getMember_id_scadm() {
        return member_id_scadm;
    }

    public void setMember_id_scadm(String member_id_scadm) {
        this.member_id_scadm = member_id_scadm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
