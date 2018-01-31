package com.sgo.saldomu.Beans;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by yuddistirakiki on 4/4/16.
 */
public class Biller_Data_Model extends RealmObject {

    @Required
    private String comm_id;
    @Required
    private String comm_code;
    @Required
    private String comm_name;
    @Required
    private String api_key;
    @Required
    private String biller_type;
    @Required
    private String item_id;
    @Required
    private String manual_advice;
    @Required
    private String callback_url;

    private RealmList<Denom_Data_Model> denom_data_models;

    private RealmList<bank_biller_model> bank_biller_models;

    private String last_update;

    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
    }

    public String getComm_code() {
        return comm_code;
    }

    public void setComm_code(String comm_code) {
        this.comm_code = comm_code;
    }

    public String getComm_name() {
        return comm_name;
    }

    public void setComm_name(String comm_name) {
        this.comm_name = comm_name;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }


    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getCallback_url() {
        return callback_url;
    }

    public void setCallback_url(String callback_url) {
        this.callback_url = callback_url;
    }

    public String getBiller_type() {
        return biller_type;
    }

    public void setBiller_type(String biller_type) {
        this.biller_type = biller_type;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public RealmList<Denom_Data_Model> getDenom_data_models() {
        return denom_data_models;
    }

    public void setDenom_data_models(RealmList<Denom_Data_Model> denom_data_models) {
        this.denom_data_models = denom_data_models;
    }

    public RealmList<bank_biller_model> getBank_biller_models() {
        return bank_biller_models;
    }

    public void setBank_biller_models(RealmList<bank_biller_model> bank_biller_models) {
        this.bank_biller_models = bank_biller_models;
    }

    public String getManual_advice() {
        return manual_advice;
    }

    public void setManual_advice(String manual_advice) {
        this.manual_advice = manual_advice;
    }
}
