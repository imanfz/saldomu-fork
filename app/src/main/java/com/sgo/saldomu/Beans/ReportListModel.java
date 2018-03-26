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
  private String admin_fee;
  private String total_amount;
  private String member_phone;
  private String member_name;


  public ReportListModel(String _datetime, String _type, String _ccyID, String _amount, String _trxID, String _description,
                         String _remark, String _detail,String _commId, String _alias, String _buss_scheme_code,
                         String _buss_scheme_name, String _admin_fee, String _total_amount, String _member_phone, String _member_name){
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
    this.setAdminFee(_admin_fee);
    this.setTotalAmount(_total_amount);
    this.setMember_phone(_member_phone);
    this.setMember_name(_member_name);
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

  public String getAdmin_fee() {return admin_fee;}

  public void setAdminFee(String admin_fee) {this.admin_fee = admin_fee;}

  public String getTotal_amount() {return total_amount;}

  public void setTotalAmount(String total_amount) {this.total_amount = total_amount;}

  public String getMember_phone() {
    return member_phone;
  }

  public void setMember_phone(String member_phone) {
    this.member_phone = member_phone;
  }

  public String getMember_name() {
    return member_name;
  }

  public void setMember_name(String member_name) {
    this.member_name = member_name;
  }
}
