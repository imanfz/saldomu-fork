package com.sgo.hpku.Beans;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yuddistirakiki on 4/4/16.
 */

public class Biller_Type_Data_Model extends RealmObject {


    @PrimaryKey
    @Required
    private String biller_type_id;
    @Required
    private String biller_type;
    @Required
    private String biller_type_code;
    @Required
    private String biller_type_name;

    private RealmList<Biller_Data_Model> biller_data_models;

    private String last_update;

    public String getBiller_type() {
        return biller_type;
    }

    public String getBiller_type_id() {
        return biller_type_id;
    }

    public void setBiller_type_id(String biller_type_id) {
        this.biller_type_id = biller_type_id;
    }

    public String getBiller_type_code() {
        return biller_type_code;
    }

    public void setBiller_type_code(String biller_type_code) {
        this.biller_type_code = biller_type_code;
    }

    public String getBiller_type_name() {
        return biller_type_name;
    }

    public void setBiller_type_name(String biller_type_name) {
        this.biller_type_name = biller_type_name;
    }


    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public RealmList<Biller_Data_Model> getBiller_data_models() {
        return biller_data_models;
    }

    public void setBiller_data_models(RealmList<Biller_Data_Model> biller_data_models) {
        this.biller_data_models = biller_data_models;
    }
}
