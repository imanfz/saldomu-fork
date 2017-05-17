package com.sgo.orimakardaya.Beans;

/**
 * Created by thinkpad on 1/31/2017.
 */

public class BBSCommBenef {
    private String product_code;
    private String product_name;
    private String product_type;

    public BBSCommBenef(){

    }

    public BBSCommBenef(String product_code, String product_name, String product_type){
        this.setProduct_code(product_code);
        this.setProduct_name(product_name);
        this.setProduct_type(product_type);
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }
}
