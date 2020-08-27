package com.sgo.saldomu.models;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 2/20/2018.
 */

public class ReportListCommFeeModel {

    private String datetime;
    private String type;
    private String description;
    private String ccyID;
    private String amount;
    private String status;

    public ReportListCommFeeModel(String _datetime, String _type, String _description, String _ccyID, String _amount, String _status)
    {
        this.setDatetime(_datetime);
        this.setType(_type);
        this.setDescription(_description);
        this.setCcyID(_ccyID);
        this.setAmount(_amount);
        this.setStatus(_status);
    }

    public ReportListCommFeeModel(FragmentActivity activity, int list_report_comm_fee, ArrayList<ReportListCommFeeModel> mData) {
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    private void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCcyID() {
        return ccyID;
    }

    private void setCcyID(String ccyID) {
        this.ccyID = ccyID;
    }

    public String getDatetime() {
        return datetime;
    }

    private void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }
}
