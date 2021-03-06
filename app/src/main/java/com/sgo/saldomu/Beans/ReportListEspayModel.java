package com.sgo.saldomu.Beans;/*
  Created by Administrator on 5/19/2015.
 */

public class ReportListEspayModel {

    private String datetime;
    private String buss_scheme_name;
    private String comm_name;
    private String ccy_id;
    private String amount;
    private String admin_fee;
    private String description;
    private String remark;
    private String tx_id;
    private String comm_id;
    private String bank_name;
    private String product_name;
    private String tx_status;
    private String type_desc;
    private String buss_scheme_code;


    public ReportListEspayModel(String _datetime, String _buss_scheme_name,  String _comm_name, String _ccy_id, String _amount,
                              String _admin_fee, String _description, String _remark, String _tx_id, String _comm_id,
                              String _bank_name, String _product_name, String _tx_status, String _buss_scheme_code){
    this.setDatetime(_datetime);
    this.setBuss_scheme_name(_buss_scheme_name);
    this.setComm_name(_comm_name);
    this.setCcy_id(_ccy_id);
    this.setAmount(_amount);
    this.setAdmin_fee(_admin_fee);
    this.setDescription(_description);
    this.setRemark(_remark);
    this.setTx_id(_tx_id);
    this.setComm_id(_comm_id);
    this.setBank_name(_bank_name);
    this.setProduct_name(_product_name);
    this.setTx_status(_tx_status);
        this.setType_desc("");
        this.setBuss_scheme_code(_buss_scheme_code);
    }


    public String getDatetime() {
    return datetime;
    }

    private void setDatetime(String datetime) {
    this.datetime = datetime;
    }

    public String getBuss_scheme_name() {
    return buss_scheme_name;
    }

    private void setBuss_scheme_name(String buss_scheme_name) {
    this.buss_scheme_name = buss_scheme_name;
    }

    public String getComm_name() {
    return comm_name;
    }

    private void setComm_name(String comm_name) {
    this.comm_name = comm_name;
    }

    public String getCcy_id() {
    return ccy_id;
    }

    private void setCcy_id(String ccy_id) {
    this.ccy_id = ccy_id;
    }

    public String getAmount() {
    return amount;
    }

    private void setAmount(String amount) {
    this.amount = amount;
    }

    public String getAdmin_fee() {
    return admin_fee;
    }

    private void setAdmin_fee(String admin_fee) {
    this.admin_fee = admin_fee;
    }

    public String getDescription() {
    return description;
    }

    private void setDescription(String description) {
    this.description = description;
    }

    public String getRemark() {
    return remark;
    }

    private void setRemark(String remark) {
    this.remark = remark;
    }

    public String getTx_id() {
    return tx_id;
    }

    private void setTx_id(String tx_id) {
    this.tx_id = tx_id;
    }

    public String getComm_id() {
    return comm_id;
    }

    private void setComm_id(String comm_id) {
    this.comm_id = comm_id;
    }

    public String getBank_name() {
    return bank_name;
    }

    private void setBank_name(String bank_name) {
    this.bank_name = bank_name;
    }

    public String getProduct_name() {
    return product_name;
    }

    private void setProduct_name(String product_name) {
    this.product_name = product_name;
    }

    public String getTx_status() {
    return tx_status;
    }

    private void setTx_status(String tx_status) {
    this.tx_status = tx_status;
    }

    public String getType_desc() {
        return type_desc;
    }

    public void setType_desc(String type_desc) {
        this.type_desc = type_desc;
    }

    public String getBuss_scheme_code() {return buss_scheme_code;}

    public void setBuss_scheme_code(String buss_scheme_code) {this.buss_scheme_code = buss_scheme_code;}
}
