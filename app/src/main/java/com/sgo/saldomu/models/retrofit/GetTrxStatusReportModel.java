package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetTrxStatusReportModel extends GetTrxStatusModel{
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("ccy_id")
    @Expose
    private
    String ccy_id;
    @SerializedName("payment_bank")
    @Expose
    private
    String payment_bank;
    @SerializedName("payment_phone")
    @Expose
    private
    String payment_phone;
    @SerializedName("payment_name")
    @Expose
    private
    String payment_name;
    @SerializedName("payment_remark")
    @Expose
    private
    String payment_remark;
    @SerializedName("tx_amount")
    @Expose
    private
    String tx_amount;
    @SerializedName("admin_fee")
    @Expose
    private
    String admin_fee;
    @SerializedName("fee_amount")
    @Expose
    private
    String fee_amount;
    @SerializedName("total_amount")
    @Expose
    private
    String total_amount;
    @SerializedName("member_phone")
    @Expose
    private
    String member_phone;
    @SerializedName("member_name")
    @Expose
    private
    String member_name;
    @SerializedName("tx_bank_name")
    @Expose
    private
    String tx_bank_name;
    @SerializedName("source_bank_name")
    @Expose
    private
    String source_bank_name;
    @SerializedName("source_acct_no")
    @Expose
    private
    String source_acct_no;
    @SerializedName("source_acct_name")
    @Expose
    private
    String source_acct_name;
    @SerializedName("benef_bank_name")
    @Expose
    private
    String benef_bank_name;

    @SerializedName("benef_acct_no")
    @Expose
    private
    String benef_acct_no;
    @SerializedName("benef_acct_name")
    @Expose
    private
    String benef_acct_name;
    @SerializedName("benef_acct_type")
    @Expose
    private
    String benef_acct_type;
    @SerializedName("member_shop_phone")
    @Expose
    private
    String member_shop_phone;
    @SerializedName("member_shop_name")
    @Expose
    private
    String member_shop_name;
    @SerializedName("otp_member")
    @Expose
    private
    String otp_member;
    @SerializedName("comm_code")
    @Expose
    private
    String comm_code;
    @SerializedName("member_code")
    @Expose
    private
    String member_code;
    @SerializedName("denom_detail")
    @Expose
    private
    String denom_detail;
    @SerializedName("order_id")
    @Expose
    private
    String order_id;
    @SerializedName("member_shop_no")
    @Expose
    private
    String member_shop_no;
    @SerializedName("product_h2h")
    @Expose
    private
    String product_h2h;
    @SerializedName("product_code")
    @Expose
    private
    String product_code;
    @SerializedName("comm_id")
    @Expose
    private
    String comm_id;

    public String getTx_id() {
        return tx_id;
    }

    public String getCcy_id() {
        return ccy_id;
    }

    public String getPayment_bank() {
        return payment_bank;
    }

    public String getPayment_phone() {
        return payment_phone;
    }

    public String getPayment_name() {
        return payment_name;
    }

    public String getTx_amount() {
        return tx_amount;
    }

    public String getAdmin_fee() {
        return admin_fee;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public String getPayment_remark() {
        return payment_remark;
    }

    public String getMember_phone() {
        return member_phone;
    }

    public String getMember_name() {
        return member_name;
    }

    public String getTx_bank_name() {
        return tx_bank_name;
    }

    public String getSource_bank_name() {
        return source_bank_name;
    }

    public String getSource_acct_no() {
        return source_acct_no;
    }

    public String getSource_acct_name() {
        return source_acct_name;
    }

    public String getBenef_bank_name() {
        return benef_bank_name;
    }

    public String getBenef_acct_no() {
        return benef_acct_no;
    }

    public String getBenef_acct_name() {
        return benef_acct_name;
    }

    public String getBenef_acct_type() {
        return benef_acct_type;
    }

    public String getMember_shop_phone() {
        return member_shop_phone;
    }

    public String getMember_shop_name() {
        return member_shop_name;
    }

    public String getOtp_member() {
        return otp_member;
    }

    public String getComm_code() {
        return comm_code;
    }

    public String getMember_code() {
        return member_code;
    }

    public String getDenom_detail() {
        return denom_detail;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getMember_shop_no() {
        return member_shop_no;
    }

    public String getFee_amount() {
        return fee_amount;
    }

    public String getProduct_h2h() {
        return product_h2h;
    }

    public String getProduct_code() {
        return product_code;
    }

    public String getComm_id() {
        return comm_id;
    }
}
