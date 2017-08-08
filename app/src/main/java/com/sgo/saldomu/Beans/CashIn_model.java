package com.sgo.saldomu.Beans;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;

/**
 * Created by Lenovo Thinkpad on 8/2/2017.
 */

public class CashIn_model {
    private String amount;
    private String benef_product_code;
    private String benef_product_type;
    private String benef_product_value_code;
    private String benef_product_value_city; //kota jakarta
    private String source_product_code; //rekening agent
    private String member_shop_phone; //nomor hp pengirim
    private String pesan;
    private String status;

    public  CashIn_model ()
    {

    }

    public CashIn_model(String amount, String benef_product_code, String benef_product_type, String benef_product_value_code, String benef_product_value_city, String member_shop_phone, String source_product_code, String pesan, Boolean staus)
    {
        this.setAmount(amount);
        this.setBenef_product_code(benef_product_code);
        this.setBenef_product_type(benef_product_type);
        this.setBenef_product_value_code(benef_product_value_code);
        this.setBenef_product_value_city(benef_product_value_city);
        this.setMember_shop_phone(member_shop_phone);
        this.setSource_product_code(source_product_code);
        this.setPesan(pesan);
        this.setStatus(status);
    }

    public JSONObject convertModelToJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (amount != null) {
                jsonObject.put("amount", amount);
            }
            if (benef_product_code != null) {
                jsonObject.put("benef_product_code", benef_product_code);
            }
            if (benef_product_type != null) {
                jsonObject.put("benef_product_type", benef_product_type);
            }
            if (benef_product_value_code!=null)
            {
                jsonObject.put("benef_product_value_code", benef_product_value_code);
            }
            if (benef_product_value_city!=null)
            {
                jsonObject.put("benef_product_value_city", benef_product_value_city);
            }
            if (source_product_code !=null)
            {
                jsonObject.put("source_product_code", source_product_code);
            }
            if (member_shop_phone!=null)
            {
                jsonObject.put("member_shop_phone", member_shop_phone);
            }
            if (pesan !=null)
            {
                jsonObject.put("pesan", pesan);
            }
            if (status !=null)
            {
                jsonObject.put("status", status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {return status;}


}
