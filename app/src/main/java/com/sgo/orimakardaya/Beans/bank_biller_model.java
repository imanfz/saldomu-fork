package com.sgo.orimakardaya.Beans;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yuddistirakiki on 4/4/16.
 */

public class bank_biller_model extends RealmObject{

    @PrimaryKey
    @Required
    private String product_code;
    @Required
    private String bank_code;
    @Required
    private String bank_name;
    @Required
    private String product_name;
    @Required
    private String product_h2h;
    @Required
    private String product_type;

    private String last_update;

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getBank_code() {
        return bank_code;
    }

    public void setBank_code(String bank_code) {
        this.bank_code = bank_code;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_h2h() {
        return product_h2h;
    }

    public void setProduct_h2h(String product_h2h) {
        this.product_h2h = product_h2h;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }
}
