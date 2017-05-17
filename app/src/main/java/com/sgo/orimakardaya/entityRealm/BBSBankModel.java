package com.sgo.orimakardaya.entityRealm;

import io.realm.RealmObject;

/**
 * Created by yuddistirakiki on 4/21/17.
 */

public class BBSBankModel extends RealmObject {

    private String comm_id;
    private String comm_type;
    private String product_code;
    private String product_name;
    private String product_type;
    private String product_h2h;
    private String scheme_code;
    private String bank_gateway;
    private String last_update;

    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
    }

    public String getComm_type() {
        return comm_type;
    }

    public void setComm_type(String comm_type) {
        this.comm_type = comm_type;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getProduct_h2h() {
        return product_h2h;
    }

    public void setProduct_h2h(String product_h2h) {
        this.product_h2h = product_h2h;
    }

    public String getScheme_code() {
        return scheme_code;
    }

    public void setScheme_code(String scheme_code) {
        this.scheme_code = scheme_code;
    }

    public String getBank_gateway() {
        return bank_gateway;
    }

    public void setBank_gateway(String bank_gateway) {
        this.bank_gateway = bank_gateway;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }
}
