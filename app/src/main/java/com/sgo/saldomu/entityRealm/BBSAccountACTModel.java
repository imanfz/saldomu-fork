package com.sgo.saldomu.entityRealm;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by yuddistirakiki on 4/21/17.
 */

public class BBSAccountACTModel extends RealmObject {
    @Ignore
    public static final String PRODUCT_CODE = "product_code";
    @Ignore
    public static final String ACCOUNT_NO = "account_no";


    private String comm_id;
    private String product_code;
    private String product_name;
    private String product_type;
    private String scheme_code;
    private String account_no;
    private String account_name;
    private String account_city;
    private String last_update;
    private String enabled_additional_fee;

    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
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

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public String getAccount_no() {
        return account_no;
    }

    public void setAccount_no(String account_no) {
        this.account_no = account_no;
    }

    public String getAccount_city() {
        return account_city;
    }

    public void setAccount_city(String account_city) {
        this.account_city = account_city;
    }
}
