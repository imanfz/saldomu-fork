package com.sgo.saldomu.Beans;

/**
 * Created by thinkpad on 10/22/2015.
 */
public class ReportAskListModel {
    private String datetime;
    private String type;
    private String ccyID;
    private String amount;
    private String trxId;
    private String description;
    private String remark;
    private String detail;
    private String alias;
    private String status;
    private String reason;
    private String buss_scheme_code;
    private String buss_scheme_name;

    public ReportAskListModel(String _datetime, String _type, String _ccyID, String _amount, String _trxID, String _description,
                              String _remark, String _detail, String _alias, String _status, String _reason, String _buss_scheme_code,
                              String _buss_scheme_name){
        this.setDatetime(_datetime);
        this.setType(_type);
        this.setCcyID(_ccyID);
        this.setAmount(_amount);
        this.setTrxId(_trxID);
        this.setDescription(_description);
        this.setRemark(_remark);
        this.setDetail(_detail);
        this.setAlias(_alias);
        this.setStatus(_status);
        this.setReason(_reason);
        this.setBuss_scheme_code(_buss_scheme_code);
        this.setBuss_scheme_name(_buss_scheme_name);
    }

    public String getReason() {
        return reason;
    }

    private void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    public String getAlias() {
        return alias;
    }

    private void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAmount() {
        return amount;
    }

    private void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCcyID() {
        return ccyID;
    }

    private void setCcyID(String ccyID) {
        this.ccyID = ccyID;
    }

    public String getDatetime() {
        return datetime;
    }

    private void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public String getDetail() {
        return detail;
    }

    private void setDetail(String detail) {
        this.detail = detail;
    }

    public String getRemark() {
        return remark;
    }

    private void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTrxId() {
        return trxId;
    }

    private void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    public String getBuss_scheme_code() {return buss_scheme_code;}

    public void setBuss_scheme_code(String buss_scheme_code) {this.buss_scheme_code = buss_scheme_code;}

    public String getBuss_scheme_name() {return buss_scheme_name;}

    public void setBuss_scheme_name(String buss_scheme_name) {this.buss_scheme_name = buss_scheme_name;}
}
