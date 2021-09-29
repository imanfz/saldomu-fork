package com.sgo.saldomu.models;

public class InvoiceDGI {
    String buss_scheme_code;
    String member_code;
    String doc_no;
    String doc_id;
    String amount;
    String remain_amount;
    String hold_amount;
    String input_amount;
    String ccy;
    String doc_desc;
    String due_date;
    String datetime;
    String app_id;
    boolean selected;

    public InvoiceDGI() {

    }

    public String getBuss_scheme_code() {
        return buss_scheme_code;
    }

    public String getDoc_no() {
        return doc_no;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public String getAmount() {
        return amount;
    }

    public String getRemain_amount() {
        return remain_amount;
    }

    public String getHold_amount() {
        return hold_amount;
    }

    public String getInput_amount() {
        if (input_amount == null)
            input_amount = "0";
        return input_amount;
    }

    public String getCcy() {
        return ccy;
    }

    public String getDoc_desc() {
        return doc_desc;
    }

    public String getDue_date() {
        return due_date;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getApp_id() {
        return app_id;
    }

    public boolean getSelected() {
        return selected;
    }

    public String getMember_code() {
        return member_code;
    }

    public void setMember_code(String member_code) {
        this.member_code = member_code;
    }

    public void setBuss_scheme_code(String buss_scheme_code) {
        this.buss_scheme_code = buss_scheme_code;
    }

    public void setDoc_no(String doc_no) {
        this.doc_no = doc_no;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setRemain_amount(String remain_amount) {
        this.remain_amount = remain_amount;
    }

    public void setHold_amount(String hold_amount) {
        this.hold_amount = hold_amount;
    }

    public void setInput_amount(String input_amount) {
        this.input_amount = input_amount;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    public void setDoc_desc(String doc_desc) {
        this.doc_desc = doc_desc;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
