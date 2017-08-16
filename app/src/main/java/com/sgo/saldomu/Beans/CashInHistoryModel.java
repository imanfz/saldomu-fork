package com.sgo.saldomu.Beans;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;

/**
 * Created by Lenovo Thinkpad on 8/2/2017.
 */

public class CashInHistoryModel {
    private String amount;
    private String benef_product_code;
    private String benef_product_name;
    private String benef_product_type;
    private String benef_product_value_code;
    private String benef_product_value_city; //kota jakarta
    private String source_product_code; //rekening agent
    private String source_product_name;
    private String source_product_type;
    private String source_product_h2h;
    private String member_shop_phone; //nomor hp pengirim
    private String pesan;

    public CashInHistoryModel()
    {

    }

    public CashInHistoryModel(String amount, String benef_product_code, String benef_product_name, String benef_product_type, String benef_product_value_code, String benef_product_value_city, String member_shop_phone, String source_product_code, String source_product_name, String source_product_type, String source_product_h2h, String pesan)
    {
        this.setAmount(amount);
        this.setBenef_product_code(benef_product_code);
        this.setBenef_product_name(benef_product_name);
        this.setBenef_product_type(benef_product_type);
        this.setBenef_product_value_code(benef_product_value_code);
        this.setBenef_product_value_city(benef_product_value_city);
        this.setMember_shop_phone(member_shop_phone);
        this.setSource_product_code(source_product_code);
        this.setSource_product_name(source_product_name);
        this.setSource_product_type(source_product_type);
        this.setSource_product_h2h(source_product_h2h);
        this.setPesan(pesan);
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setBenef_product_code(String benef_product_code) {
        this.benef_product_code = benef_product_code;
    }

    public String getBenef_product_code() {return benef_product_code;    }

    public void setBenef_product_value_code(String benef_product_value_code) {
        this.benef_product_value_code = benef_product_value_code;
    }

    public String getBenef_product_value_code() {return benef_product_value_code;    }

    public void setBenef_product_type(String benef_product_type) {
        this.benef_product_type = benef_product_type;
    }

    public String getBenef_product_type(){return benef_product_type;}

    public void setBenef_product_value_city(String benef_product_value_city) {
        this.benef_product_value_city = benef_product_value_city;
    }

    public String getBenef_product_value_city() {return benef_product_value_city;}

    public void setSource_product_code(String source_product_code) {
        this.source_product_code = source_product_code;
    }

    public String getSource_product_code() {return source_product_code;}

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    public String getPesan() {return pesan;}

    public void setMember_shop_phone(String member_shop_phone) {
        this.member_shop_phone = member_shop_phone;
    }

    public String getMember_shop_phone() {return member_shop_phone;}

    public void setBenef_product_name(String benef_product_name) {
        this.benef_product_name = benef_product_name;
    }

    public String getBenef_product_name(){return benef_product_name;}

    public void setSource_product_name(String source_product_name) {
        this.source_product_name = source_product_name;
    }

    public String getSource_product_name(){return source_product_name;}

    public void setSource_product_h2h(String source_product_h2h) {
        this.source_product_h2h = source_product_h2h;
    }

    public String getSource_product_h2h(){return source_product_h2h;}

    public void setSource_product_type(String source_product_type) {
        this.source_product_type = source_product_type;
    }

    public String getSource_product_type(){return source_product_type;}
}
