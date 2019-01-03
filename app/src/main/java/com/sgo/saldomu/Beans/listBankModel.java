package com.sgo.saldomu.Beans;/*
  Created by Administrator on 12/15/2014.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sgo.saldomu.coreclass.DefineValue;

public class listBankModel {

    @SerializedName("bank_name")
    @Expose
    private String bank_name;
    private String bank_code;
    @SerializedName("product_code")
    @Expose
    private String product_code;
    @SerializedName("product_name")
    @Expose
    private String product_name;
    @SerializedName("product_type")
    @Expose
    private String product_type;
    @SerializedName("product_h2h")
    @Expose
    private String product_h2h;
    private String bank_gateway;
    private String noVA;
    private String fee;

    public listBankModel() {
        super();
    }

    public listBankModel(String _bank_code, String _bank_name, String _product_code, String _product_name,
                         String _product_type, String _product_h2h) {
        super();
        this.setBank_code(_bank_code);
        this.setBank_name(_bank_name);
        this.setProduct_code(_product_code);
        this.setProduct_name(_product_name);
        this.setProduct_type(_product_type);
        this.setProduct_h2h(_product_h2h);
    }

    public listBankModel(String _bank_code, String _bank_name, String _product_code, String _product_name,
                         String _product_type, String _product_h2h, String nova, String fee) {
        super();
        this.setBank_code(_bank_code);
        this.setBank_name(_bank_name);
        this.setProduct_code(_product_code);
        this.setProduct_name(_product_name);
        this.setProduct_type(_product_type);
        this.setProduct_h2h(_product_h2h);
        this.setNoVA(nova);
        this.setFee(fee);
    }

    public listBankModel(String _bank_name, String _product_name) {
        super();
        this.setBank_code(DefineValue.BankMandiri);
        this.setBank_name(_bank_name);
        this.setProduct_code("MANDIRI_ONLINE");
        this.setProduct_name(_product_name);
        this.setProduct_type("MANDIRI_ATM");
        this.setProduct_h2h("");
        this.setNoVA("");
        this.setFee("");
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getBank_code() {
        return bank_code;
    }

    public void setBank_code(String bank_code) {
        this.bank_code = bank_code;
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

    public String getBank_gateway() {
        return bank_gateway;
    }

    public void setBank_gateway(String bank_gateway) {
        this.bank_gateway = bank_gateway;
    }

    public String getNoVA() {
        return noVA;
    }

    public void setNoVA(String noVA) {
        this.noVA = noVA;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }
}
