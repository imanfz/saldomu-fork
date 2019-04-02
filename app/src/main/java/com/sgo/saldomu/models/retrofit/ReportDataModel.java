package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReportDataModel  {
//    REPORT_SCASH
    @SerializedName("datetime")
    @Expose
    private
    String datetime;
    @SerializedName("type")
    @Expose
    private
    String type;
    @SerializedName("ccy_id")
    @Expose
    private
    String ccy_id;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("trx_id")
    @Expose
    private
    String trx_id;
    @SerializedName("description")
    @Expose
    private
    String description;
    @SerializedName("remark")
    @Expose
    private
    String remark;
    @SerializedName("detail")
    @Expose
    private
    String detail;
    @SerializedName("to_alias")
    @Expose
    private
    String to_alias;
    @SerializedName("buss_scheme_code")
    @Expose
    private
    String buss_scheme_code;
    @SerializedName("buss_scheme_name")
    @Expose
    private
    String buss_scheme_name;


//    REPORT_ESPAY
    @SerializedName("created")
    @Expose
    private
    String created;
    @SerializedName("buss_scheme_title")
    @Expose
    private
    String buss_scheme_title;
    @SerializedName("comm_name")
    @Expose
    private
    String comm_name;
    @SerializedName("admin_fee")
    @Expose
    private
    String admin_fee;
    @SerializedName("tx_description")
    @Expose
    private
    String tx_description;
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("comm_id")
    @Expose
    private
    String comm_id;
    @SerializedName("bank_name")
    @Expose
    private
    String bank_name;
    @SerializedName("product_name")
    @Expose
    private
    String product_name;
    @SerializedName("tx_status")
    @Expose
    private
    String tx_status;


//    REPORT_ASK
    @SerializedName("status")
    @Expose
    private
    String status;
    @SerializedName("reason")
    @Expose
    private
    String reason;


//    REPORT_FEE
    @SerializedName("bbs_name")
    @Expose
    private
    String bbs_name;

    public String getDatetime() {
        if (datetime == null)
            datetime = "";
        return datetime;
    }

    public String getType() {
        return type;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public String getAmount() {
        return amount;
    }

    public String getTrx_id() {
        return trx_id;
    }

    public String getDescription() {
        return description;
    }

    public String getRemark() {
        return remark;
    }

    public String getDetail() {
        return detail;
    }

    public String getTo_alias() {
        return to_alias;
    }

    public String getBuss_scheme_code() {
        return buss_scheme_code;
    }

    public String getBuss_scheme_name() {
        return buss_scheme_name;
    }

    public String getCreated() {
        return created;
    }

    public String getBuss_scheme_title() {
        return buss_scheme_title;
    }

    public String getComm_name() {
        return comm_name;
    }

    public String getAdmin_fee() {
        return admin_fee;
    }

    public String getTx_description() {
        return tx_description;
    }

    public String getTx_id() {
        return tx_id;
    }

    public String getComm_id() {
        return comm_id;
    }

    public String getBank_name() {
        return bank_name;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getTx_status() {
        return tx_status;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getBbs_name() {
        return bbs_name;
    }
}
