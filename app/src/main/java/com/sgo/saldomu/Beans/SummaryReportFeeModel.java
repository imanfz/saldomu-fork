package com.sgo.saldomu.Beans;

/**
 * Created by LENOVO on 12/03/2018.
 */

public class SummaryReportFeeModel {
    private int total_transaction;
    private int unreleased_trx;
    private int released_trx;
    private int unreleased_amount;
    private int released_amount;

    public SummaryReportFeeModel(){
        setReleased_trx(0);
        setReleased_amount(0);
        setUnreleased_amount(0);
        setUnreleased_trx(0);
    }

    public int getTotal_transaction() {
        return total_transaction;
    }

    public void setTotal_transaction(int total_transaction) {
        this.total_transaction = total_transaction;
    }

    public int getUnreleased_trx() {
        return unreleased_trx;
    }

    public void setUnreleased_trx(int unreleased_trx) {
        this.unreleased_trx = unreleased_trx;
    }

    public int getReleased_trx() {
        return released_trx;
    }

    public void setReleased_trx(int released_trx) {
        this.released_trx = released_trx;
    }

    public int getUnreleased_amount() {
        return unreleased_amount;
    }

    public void setUnreleased_amount(int unreleased_amount) {
        this.unreleased_amount = unreleased_amount;
    }

    public int getReleased_amount() {
        return released_amount;
    }

    public void setReleased_amount(int released_amount) {
        this.released_amount = released_amount;
    }
}
