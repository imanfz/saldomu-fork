package com.sgo.orimakardaya.Beans;

/**
 * Created by yuddistirakiki on 2/2/17.
 */

public class AccountBBS {
    private String product_name;
    private String product_code;
    private String product_type;
    private String benef_acct_no;
    private String benef_acct_name;
    private String benef_acct_city;

    public AccountBBS(String product_name, String product_code, String product_type, String benef_acct_no, String benef_acct_name, String benef_acct_city){
        this.setProduct_name(product_name);
        this.setProduct_code(product_code);
        this.setProduct_type(product_type);
        this.setBenef_acct_no(benef_acct_no);
        this.setBenef_acct_name(benef_acct_name);
        this.setBenef_acct_city(benef_acct_city);
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getBenef_acct_no() {
        return benef_acct_no;
    }

    public void setBenef_acct_no(String benef_acct_no) {
        this.benef_acct_no = benef_acct_no;
    }

    public String getBenef_acct_name() {
        return benef_acct_name;
    }

    public void setBenef_acct_name(String benef_acct_name) {
        this.benef_acct_name = benef_acct_name;
    }

    public String getBenef_acct_city() {
        return benef_acct_city;
    }

    public void setBenef_acct_city(String benef_acct_city) {
        this.benef_acct_city = benef_acct_city;
    }
}
