package com.sgo.orimakardaya.Beans;/*
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


  public ReportListModel(String _datetime, String _type, String _ccyID, String _amount, String _trxID, String _description, String _remark, String _detail,String _commId, String _alias){
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
  }


  public String getDatetime() {
    return datetime;
  }

  public void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCcyID() {
    return ccyID;
  }

  public void setCcyID(String ccyID) {
    this.ccyID = ccyID;
  }

  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public String getTrxId() {
    return trxId;
  }

  public void setTrxId(String trxId) {
    this.trxId = trxId;
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

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getCommId() {
    return commId;
  }

  public void setCommId(String commId) {
    this.commId = commId;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }
}
