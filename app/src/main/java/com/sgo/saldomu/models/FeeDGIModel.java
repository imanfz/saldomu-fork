package com.sgo.saldomu.models;

public class FeeDGIModel {
    String ccy_id, buyer_fee, seller_fee, commission_fee, min_amount, max_amount;

    public FeeDGIModel()
    {

    }

    public String getCcy_id() {
        return ccy_id;
    }

    public void setCcy_id(String ccy_id) {
        this.ccy_id = ccy_id;
    }

    public String getBuyer_fee() {
        return buyer_fee;
    }

    public void setBuyer_fee(String buyer_fee) {
        this.buyer_fee = buyer_fee;
    }

    public String getSeller_fee() {
        return seller_fee;
    }

    public void setSeller_fee(String seller_fee) {
        this.seller_fee = seller_fee;
    }

    public String getCommission_fee() {
        return commission_fee;
    }

    public void setCommission_fee(String commission_fee) {
        this.commission_fee = commission_fee;
    }

    public String getMin_amount() {
        return min_amount;
    }

    public void setMin_amount(String min_amount) {
        this.min_amount = min_amount;
    }

    public String getMax_amount() {
        return max_amount;
    }

    public void setMax_amount(String max_amount) {
        this.max_amount = max_amount;
    }
}
