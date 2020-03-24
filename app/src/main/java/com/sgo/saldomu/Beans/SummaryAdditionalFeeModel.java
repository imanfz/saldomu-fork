package com.sgo.saldomu.Beans;

import com.sgo.saldomu.models.retrofit.ReportDataModel;

public class SummaryAdditionalFeeModel extends ReportDataModel {
    private String total_transaction;
    private String total_amount;

    public void setTotal_transaction(String total_transaction) {
        this.total_transaction = total_transaction;
    }

    public String getTotal_transaction() {
        return total_transaction;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getTotal_amount() {
        return total_amount;
    }
}
