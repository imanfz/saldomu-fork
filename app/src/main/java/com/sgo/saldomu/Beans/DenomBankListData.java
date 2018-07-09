package com.sgo.saldomu.Beans;

import org.json.JSONException;
import org.json.JSONObject;

public class DenomBankListData {
    private String bankGateway;
    private String productCode;
    private String productName;
    private String bankCode;
    private String bankName;

    public DenomBankListData(JSONObject obj){

        try {
            setBankCode(obj.getString("bank_code"));
            setBankGateway(obj.getString("bank_gateway"));
            setProductCode(obj.getString("product_code"));
            setProductName(obj.getString("product_name"));
            setBankName(obj.getString("bank_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getBankGateway() {
        return bankGateway;
    }

    public void setBankGateway(String bankGateway) {
        this.bankGateway = bankGateway;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
