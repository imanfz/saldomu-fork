package com.sgo.orimakardaya.Beans;/*
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
    private Boolean is_pln = false;


    public ReportListEspayModel(String _datetime, String _buss_scheme_name,  String _comm_name, String _ccy_id, String _amount,
                              String _admin_fee, String _description, String _remark, String _tx_id, String _comm_id,
                              String _bank_name, String _product_name, String _tx_status){
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
    }


    public String getDatetime() {
    return datetime;
    }

    public void setDatetime(String datetime) {
    this.datetime = datetime;
    }

    public String getBuss_scheme_name() {
    return buss_scheme_name;
    }

    public void setBuss_scheme_name(String buss_scheme_name) {
    this.buss_scheme_name = buss_scheme_name;
    }

    public String getComm_name() {
    return comm_name;
    }

    public void setComm_name(String comm_name) {
    this.comm_name = comm_name;
    }

    public String getCcy_id() {
    return ccy_id;
    }

    public void setCcy_id(String ccy_id) {
    this.ccy_id = ccy_id;
    }

    public String getAmount() {
    return amount;
    }

    public void setAmount(String amount) {
    this.amount = amount;
    }

    public String getAdmin_fee() {
    return admin_fee;
    }

    public void setAdmin_fee(String admin_fee) {
    this.admin_fee = admin_fee;
    }

    public String getDescription() {
    return description;
    }

    public void setDescription(String description) {
    this.description = description;
    }

    public String getRemark() {
    return remark;
    }

    public void setRemark(String remark) {
    this.remark = remark;
    }

    public String getTx_id() {
    return tx_id;
    }

    public void setTx_id(String tx_id) {
    this.tx_id = tx_id;
    }

    public String getComm_id() {
    return comm_id;
    }

    public void setComm_id(String comm_id) {
    this.comm_id = comm_id;
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

    public String getTx_status() {
    return tx_status;
    }

    public void setTx_status(String tx_status) {
    this.tx_status = tx_status;
    }

    public Boolean getIs_pln() {
        return is_pln;
    }

    public void setIs_pln(Boolean is_pln) {
        this.is_pln = is_pln;
    }
}
