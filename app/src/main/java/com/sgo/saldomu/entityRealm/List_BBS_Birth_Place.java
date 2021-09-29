package com.sgo.saldomu.entityRealm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class List_BBS_Birth_Place extends RealmObject {

    @PrimaryKey
    private String birthplace_id;
    private String birthplace_city;

    public List_BBS_Birth_Place() {

    }

    public List_BBS_Birth_Place(String _birthplace_id, String _birthplace_city) {
        this.setBirthPlace_id(_birthplace_id);
        this.setBirthPlace_city(_birthplace_city);
    }

    public String getBirthPlace_id() {
        return birthplace_id;
    }

    public void setBirthPlace_id(String birthplace_id) {
        this.birthplace_id = birthplace_id;
    }

    public String getBirthPlace_city() {
        return birthplace_city;
    }

    public void setBirthPlace_city(String birthplace_city) {
        this.birthplace_city = birthplace_city;
    }
}
