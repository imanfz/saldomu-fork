package com.sgo.saldomu.models;

public class PaymentTypeDGIModel {
    String payment_code, payment_name;

    public PaymentTypeDGIModel()
    {

    }

    public String getPayment_code() {
        return payment_code;
    }

    public void setPayment_code(String payment_code) {
        this.payment_code = payment_code;
    }

    public String getPayment_name() {
        return payment_name;
    }

    public void setPayment_name(String payment_name) {
        this.payment_name = payment_name;
    }
}
