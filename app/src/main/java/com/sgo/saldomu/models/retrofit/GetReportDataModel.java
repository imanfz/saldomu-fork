package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetReportDataModel extends jsonModel {
    @SerializedName("report_data")
    @Expose
    private
    List<ReportDataModel> report_data = new ArrayList<>();

    @SerializedName("next")
    @Expose
    private
    String next;

    public List<ReportDataModel> getReport_data() {
        return report_data;
    }

    public String getNext() {
        if (next == null || next.equals(""))
            next =  "0";
        return next;
    }
}
