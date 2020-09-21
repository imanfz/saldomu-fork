package com.sgo.saldomu.Beans;

import com.sgo.saldomu.models.retrofit.ReportDataModel;

public class SummaryAdditionalFeeModel extends ReportDataModel {
    private String count_trx;
    private String total_trx;

    public String getCount_trx() {
        return count_trx;
    }

    public void setCount_trx(String count_trx) {
        this.count_trx = count_trx;
    }

    public String getTotal_trx() {
        return total_trx;
    }

    public void setTotal_trx(String total_trx) {
        this.total_trx = total_trx;
    }
}
