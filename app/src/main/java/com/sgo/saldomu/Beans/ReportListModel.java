package com.sgo.saldomu.Beans;/*
  Created by Administrator on 5/19/2015.
 */

public class ReportListModel {

  private String datetime;
  private String type;
  private String ccyID;
  private String amount;
  private String trxId;
  private String commId;
  private String description;
  private String remark;
  private String detail;
  private String alias;
  private String buss_scheme_code;
  private String buss_scheme_name;


  public ReportListModel(String _datetime, String _type, String _ccyID, String _amount, String _trxID, String _description,
                         String _remark, String _detail,String _commId, String _alias, String _buss_scheme_code,
                         String _buss_scheme_name){
    this.setDatetime(_datetime);
    this.setType(_type);
    this.setCcyID(_ccyID);
    this.setAmount(_amount);
    this.setTrxId(_trxID);
    this.setDescription(_description);
    this.setRemark(_remark);
    this.setDetail(_detail);
    this.setCommId(_commId);
    this.setAlias(_alias);
    this.setBuss_scheme_code(_buss_scheme_code);
    this.setBuss_scheme_name(_buss_scheme_name);
  }


  public String getDatetime() {
    return datetime;
  }

  private void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  public String getType() {
    return type;
  }

  private void setType(String type) {
    this.type = type;
  }

  public String getCcyID() {
    return ccyID;
  }

  private void setCcyID(String ccyID) {
    this.ccyID = ccyID;
  }

  public String getAmount() {
    return amount;
  }

  private void setAmount(String amount) {
    this.amount = amount;
  }

  public String getTrxId() {
    return trxId;
  }

  private void setTrxId(String trxId) {
    this.trxId = trxId;
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

  public String getDetail() {
    return detail;
  }

  private void setDetail(String detail) {
    this.detail = detail;
  }

  public String getCommId() {
    return commId;
  }

  private void setCommId(String commId) {
    this.commId = commId;
  }

  public String getAlias() {
    return alias;
  }

  private void setAlias(String alias) {
    this.alias = alias;
  }

  public String getBuss_scheme_code() {return buss_scheme_code;}

  public void setBuss_scheme_code(String buss_scheme_code) {this.buss_scheme_code = buss_scheme_code;}

  public String getBuss_scheme_name() {return buss_scheme_name;}

  public void setBuss_scheme_name(String buss_scheme_name) {this.buss_scheme_name = buss_scheme_name;}
}
