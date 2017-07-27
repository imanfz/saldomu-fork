package com.sgo.saldomu.Beans;/*
  Created by Administrator on 2/3/2015.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;


@Table(name = "MyFriend")
public class myFriendModel extends Model {

    final public static String CONTACT_ID = "contact_id";
    final public static String FULL_NAME = "full_name";
    final public static String FRIEND_NUMBER = "friend_number";
    final public static String EMAIL = "email";
    final public static String USER_ID = "user_id";
    final public static String IMG_URL = "img_url";

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int contact_id;

    @Column
    private String full_name;

    @Column
    private String friend_number;

    @Column
    private String email;

    @Column
    private String user_id;

    @Column
    private String img_url;

    public myFriendModel(){
        super();
    }

    public myFriendModel(int _contact_id, String _full_name, String _friend_number, String _email, String _user_id, String _img_url){
        super();
        this.setContact_id(_contact_id);
        this.setFull_name(_full_name);
        this.setFriend_number(_friend_number);
        this.setEmail(_email);
        this.setUser_id(_user_id);
        this.setImg_url(_img_url);
    }



    public static List<myFriendModel> getAll() {
        // This is how you execute a query
        return new Select()
                .all()
                .from(myFriendModel.class)
                .execute();
    }

    public static void deleteAll() {
        // This is how you execute a query
        new Delete().from(myFriendModel.class).execute();
        SQLiteUtils.execSql("DELETE FROM SQLITE_SEQUENCE WHERE name='MyFriend';");
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

    public String getFriend_number() {
        return friend_number;
    }

    public void setFriend_number(String friend_number) {
        this.friend_number = friend_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
