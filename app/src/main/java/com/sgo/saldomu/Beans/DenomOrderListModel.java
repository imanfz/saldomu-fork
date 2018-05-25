package com.sgo.saldomu.Beans;

public class DenomOrderListModel {
    private String phoneNumber;
    private String pulsa;

    public DenomOrderListModel(String phoneNumber, String total){
        setPhoneNumber(phoneNumber);
        setPulsa(total);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPulsa() {
        return pulsa;
    }

    public void setPulsa(String pulsa) {
        this.pulsa = pulsa;
    }
}
