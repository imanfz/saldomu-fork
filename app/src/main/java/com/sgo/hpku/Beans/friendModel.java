package com.sgo.hpku.Beans;/*
  Created by Administrator on 2/3/2015.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.Date;
import java.util.List;


@Table(name = "Friend")
public class friendModel extends Model {

    final public static String CONTACT_ID = "contact_id";
    final public static String FULL_NAME = "full_name";
    final public static String MOBILE_NUMBER = "mobile_number";
    final public static String MOBILE_NUMBER2 = "mobile_number2";
    final public static String MOBILE_NUMBER3 = "mobile_number3";
    final public static String EMAIL = "email";
    final public static String OWNER_ID = "owner_id";
    final public static String IS_FRIEND = "is_friend";
    final public static String CREATED_DATE = "created_date";
    final public static String UPDATED_DATE = "updated_date";

    @Column
    private int contact_id;

    @Column
    private String full_name;

    @Column
    private String mobile_number;

    @Column
    private String mobile_number2;

    @Column
    private String mobile_number3;

    @Column
    private String email;

    @Column
    private String owner_id;

    @Column
    private int is_friend;

    @Column
    private Date created_date;

    @Column
    private Date update_date;

    public friendModel(){
        super();
    }

    public friendModel(int _contact_id, String _full_name, String _mobile_number, String _mobile_number2,
                       String _mobile_number3, String _email, String _owner_id, int _is_friend, Date _create_date,
                       Date _update_date){
        super();
        this.setContact_id(_contact_id);
        this.setFull_name(_full_name);
        this.setMobile_number(_mobile_number);
        this.setMobile_number2(_mobile_number2);
        this.setMobile_number3(_mobile_number3);
        this.setEmail(_email);
        this.setOwner_id(_owner_id);
        this.setIs_friend(_is_friend);
        this.setCreated_date(_create_date);
        this.setUpdate_date(_update_date);
    }

    public friendModel(String _full_name, String _mobile_number, String _mobile_number2,String _mobile_number3,
                       String _email, String _owner_id){
        super();
        this.setFull_name(_full_name);
        this.setMobile_number(_mobile_number);
        this.setMobile_number2(_mobile_number2);
        this.setMobile_number3(_mobile_number3);
        this.setEmail(_email);
        this.setOwner_id(_owner_id);
    }

    public static List<friendModel> getAll() {
        // This is how you execute a query
        return new Select()
                .all()
                .from(friendModel.class)
                .execute();
    }

    public static void deleteAll() {
        // This is how you execute a query
        new Delete().from(friendModel.class).execute();
        SQLiteUtils.execSql("DELETE FROM SQLITE_SEQUENCE WHERE name='Friend';");
    }

    public int getContact_id() {
        return contact_id;
    }

    public void setContact_id(int contact_id) {
        this.contact_id = contact_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getMobile_number2() {
        return mobile_number2;
    }

    public void setMobile_number2(String mobile_number2) {
        this.mobile_number2 = mobile_number2;
    }

    public String getMobile_number3() {
        return mobile_number3;
    }

    public void setMobile_number3(String mobile_number3) {
        this.mobile_number3 = mobile_number3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public Date getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(Date update_date) {
        this.update_date = update_date;
    }

    public int getIs_friend() {
        return is_friend;
    }

    public void setIs_friend(int is_friend) {
        this.is_friend = is_friend;
    }
}
