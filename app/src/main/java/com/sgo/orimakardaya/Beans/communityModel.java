package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 12/3/2014.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Community")
public class communityModel extends Model {

    @Column
    private String comm_id;

    @Column
    private String comm_name;

    @Column
    private String comm_code;

    @Column
    private String buss_scheme_code;

    public communityModel(){
        super();
    }
    public communityModel(String _comm_id, String _comm_name,String _comm_code, String _buss_scheme_code ){
        super();
        this.comm_id = _comm_id;
        this.comm_name = _comm_name;
        this.comm_code = _comm_code;
        this.buss_scheme_code = _buss_scheme_code;
    }


    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
    }

    public String getComm_name() {
        return comm_name;
    }

    public void setComm_name(String comm_name) {
        this.comm_name = comm_name;
    }

    public String getComm_code() {
        return comm_code;
    }

    public void setComm_code(String comm_code) {
        this.comm_code = comm_code;
    }

    public String getBuss_scheme_code() {
        return buss_scheme_code;
    }

    public void setBuss_scheme_code(String buss_scheme_code) {
        this.buss_scheme_code = buss_scheme_code;
    }

    public static List<communityModel> getAll() {
        // This is how you execute a query
        return new Select()
                .all()
                .from(communityModel.class)
                .execute();
    }

    public static List<communityModel> getItem(String comm_id) {
        // This is how you execute a query
        return new Select()
                .from(communityModel.class)
                .where("comm_id = "+comm_id)
                .execute();
    }

    public static List<Model> deleteAll() {
        // This is how you execute a query
        return new Delete().from(communityModel.class).execute();
    }
}
