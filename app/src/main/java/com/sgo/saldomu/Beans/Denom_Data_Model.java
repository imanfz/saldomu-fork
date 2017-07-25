package com.sgo.saldomu.Beans;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by yuddistirakiki on 4/4/16.
 */
public class Denom_Data_Model extends RealmObject {

    @Required
    private String item_id;
    @Required
    private String item_name;
    @Required
    private String ccy_id;
    @Required
    private String item_price;

    private String last_update;

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public void setCcy_id(String ccy_id) {
        this.ccy_id = ccy_id;
    }

    public String getItem_price() {
        return item_price;
    }

    public void setItem_price(String item_price) {
        this.item_price = item_price;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }
}
