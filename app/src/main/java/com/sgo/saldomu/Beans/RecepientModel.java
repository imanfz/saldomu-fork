package com.sgo.saldomu.Beans;/*
  Created by Administrator on 3/25/2015.
 */

import com.sgo.saldomu.models.retrofit.PayfriendDataTrfModel;

public class RecepientModel {

    private String tx_id;
    private String name;
    private String number;
    private String status;
    private String is_member_temp;

    public RecepientModel(String _tx_id, String _name, String _number, String _status, String _is_member_temp){
        setTx_id(_tx_id);
        setName(_name);
        setNumber(_number);
        setStatus(_status);
        setIs_member_temp(_is_member_temp);
    }

    public RecepientModel(String _tx_id, String _name, PayfriendDataTrfModel obj){
        setTx_id(_tx_id);
        setName(_name);
        setNumber(obj.getMember_phone());
        setStatus(obj.getMember_status());
        setIs_member_temp(obj.getIs_member_temp());
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    private void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getTx_id() {
        return tx_id;
    }

    private void setTx_id(String tx_id) {
        this.tx_id = tx_id;
    }

    public String getIs_member_temp() {
        return is_member_temp;
    }

    private void setIs_member_temp(String is_member_temp) {
        this.is_member_temp = is_member_temp;
    }
}
