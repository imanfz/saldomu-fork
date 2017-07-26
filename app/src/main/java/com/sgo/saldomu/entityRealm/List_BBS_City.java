package com.sgo.saldomu.entityRealm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by thinkpad on 1/26/2017.
 */

public class List_BBS_City extends RealmObject {

    public static final String FIELD_ID = "id";
    public static final String FIELD_CITY_ID = "city_id";
    public static final String FIELD_CITY_NAME = "city_name";

    @PrimaryKey
    private String id;
    private String city_id;
    private String city_name;

    public List_BBS_City() {

    }

    public List_BBS_City(String _id, String _city_id, String _city_name) {
        this.setId(_id);
        this.setCity_id(_city_id);
        this.setCity_name(_city_name);
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
