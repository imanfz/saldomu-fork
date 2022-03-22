package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class BBSTransModel extends jsonModel implements Serializable {
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("tx_product_code")
    @Expose
    private
    String tx_product_code;
    @SerializedName("tx_product_name")
    @Expose
    private
    String tx_product_name;
    @SerializedName("tx_bank_code")
    @Expose
    private
    String tx_bank_code;
    @SerializedName("amount")
    @Expose
    private
    String amount;
    @SerializedName("admin_fee")
    @Expose
    private
    String admin_fee;
    @SerializedName("total_amount")
    @Expose
    private
    String total_amount;
    @SerializedName("tx_bank_name")
    @Expose
    private
    String tx_bank_name;
    @SerializedName("max_resend_token")
    @Expose
    private
    String max_resend_token;
    @SerializedName("benef_product_value_code")
    @Expose
    private
    String benef_product_value_code;
    @SerializedName("benef_product_value_name")
    @Expose
    private
    String benef_product_value_name;
    @SerializedName("benef_acct_no")
    @Expose
    private
    String benef_acct_no;
    @SerializedName("benef_acct_name")
    @Expose
    private
    String benef_acct_name;
    @SerializedName("lkd_product_code")
    @Expose
    private
    String lkd_product_code;

    @SerializedName("benef_product_code")
    @Expose
    private
    String benef_product_code;

    @SerializedName("additional_fee")
    @Expose
    private
    String additional_fee;

    @SerializedName("source_of_fund")
    @Expose
    public
    ArrayList< String > source_of_fund = new ArrayList < String > ();

    @SerializedName("purpose_of_trx")
    @Expose
    public
    ArrayList < String > purpose_of_trx = new ArrayList < String > ();

    @SerializedName("cust_id_types")
    @Expose
    public
    ArrayList < String > cust_id_types = new ArrayList < String > ();

    @SerializedName("gender_types")
    @Expose
    private
    Gender_types gender_types;

    public String getTx_id() {
        if (tx_id == null)
            tx_id = "";
        return tx_id;
    }

    public String getTx_product_code() {
        if (tx_product_code == null)
            tx_product_code = "";
        return tx_product_code;
    }

    public String getTx_product_name() {
        if (tx_product_name == null)
            tx_product_name = "";
        return tx_product_name;
    }

    public String getTx_bank_code() {
        if (tx_bank_code == null)
            tx_bank_code = "";
        return tx_bank_code;
    }

    public String getAmount() {
        if (amount == null)
            amount = "";
        return amount;
    }

    public String getAdmin_fee() {
        if (admin_fee == null)
            admin_fee = "";
        return admin_fee;
    }

    public String getTotal_amount() {
        if (total_amount == null)
            total_amount = "";
        return total_amount;
    }

    public String getTx_bank_name() {
        if (tx_bank_name == null)
            tx_bank_name = "";
        return tx_bank_name;
    }

    public String getMax_resend_token() {
        if (max_resend_token == null)
            max_resend_token = "";
        return max_resend_token;
    }

    public String getBenef_product_value_code() {
        if (benef_product_value_code == null)
            benef_product_value_code = "";
        return benef_product_value_code;
    }

    public String getBenef_product_value_name() {
        if (benef_product_value_name == null)
            benef_product_value_name = "";
        return benef_product_value_name;
    }

    public String getBenef_acct_no() {
        if (benef_acct_no == null)
            benef_acct_no = "";
        return benef_acct_no;
    }

    public String getBenef_acct_name() {
        if (benef_acct_name == null)
            benef_acct_name = "";
        return benef_acct_name;
    }

    public String getLkd_product_code() {
        if (lkd_product_code == null)
            lkd_product_code = "";
        return lkd_product_code;
    }

    public String getBenef_product_code() {
        if (benef_product_code == null)
            benef_product_code = "";
        return benef_product_code;
    }

    public String getAdditional_fee() {
        if (additional_fee == null)
            additional_fee = "";
        return additional_fee;
    }

    public Gender_types getGender_types() {
        return gender_types;
    }

    public void setGender_types(Gender_types gender_typesObject) {
        this.gender_types = gender_typesObject;
    }
}

class Gender_types {
    @SerializedName("L")
    @Expose
    private String L;
    @SerializedName("P")
    @Expose
    private String P;


    // Getter Methods

    public String getL() {
        return L;
    }

    public String getP() {
        return P;
    }

    // Setter Methods

    public void setL(String L) {
        this.L = L;
    }

    public void setP(String P) {
        this.P = P;
    }
}
