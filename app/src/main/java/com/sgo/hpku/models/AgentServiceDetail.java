package com.sgo.hpku.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Lenovo on 27/02/2017.
 */

public class AgentServiceDetail extends RealmObject {

    @PrimaryKey
    private int businessServiceId;
    private int businessId;
    private int serviceId;
    private String services;
    private Double price;

    public AgentServiceDetail() {

    }

    public int getBusinessServiceId() {
        return this.businessServiceId;
    }

    public void setBusinessServiceId(int businessServiceId) {
        this.businessServiceId = businessServiceId;
    }

    public int getBusinessId() {
        return this.businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public int getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServices() {
        return this.services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}