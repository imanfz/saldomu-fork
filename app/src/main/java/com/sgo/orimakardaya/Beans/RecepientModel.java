package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 3/25/2015.
 */

public class RecepientModel {

    private String tx_id;
    private String name;
    private String number;
    private String status;

    public RecepientModel(String _tx_id, String _name, String _number, String _status){
        this.tx_id = _tx_id;
        this.name = _name;
        this.number = _number;
        this.status = _status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTx_id() {
        return tx_id;
    }

    public void setTx_id(String tx_id) {
        this.tx_id = tx_id;
    }
}
