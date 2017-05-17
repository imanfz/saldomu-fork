package com.sgo.hpku.Beans;

import org.json.JSONObject;

/**
 * Created by thinkpad on 3/19/2015.
 */
public class NotificationModelClass {

    private int image;
    private String title;
    private String from_name;
    private String detail;
    private String time;
    private String to_id;
    private String from_id;
    private int notif_type;
    private String notif_id;
    private boolean read;
    private JSONObject notif_detail;
    private String from_profile_picture;
    private String date_time;
    private String id_result;

    public NotificationModelClass(){

    }

    public NotificationModelClass(String _notif_id,int _image, String _title,String _to_id, String _from_name,
                                  String _from_id, String _detail, String _time, int _notif_type,
                                  Boolean _read, JSONObject _notif_detail, String _from_profile_picture,
                                  String _date_time, String _id_result) {
        this.setNotif_id(_notif_id);
        this.setImage(_image);
        this.setTitle(_title);
        this.setFrom_name(_from_name);
        this.setFrom_id(_from_id);
        this.setDetail(_detail);
        this.setTime(_time);
        this.setNotif_type(_notif_type);
        this.setRead(_read);
        this.setNotif_detail(_notif_detail);
        this.setFrom_profile_picture(_from_profile_picture);
        this.setDate_time(_date_time);
        this.setTo_id(_to_id);
        this.setId_result(_id_result);
    }

    public String getId_result() {
        return id_result;
    }

    private void setId_result(String id_result) {
        this.id_result = id_result;
    }

    public String getFrom_profile_picture() {
        return from_profile_picture;
    }

    private void setFrom_profile_picture(String from_profile_picture) {
        this.from_profile_picture = from_profile_picture;
    }

    public int getImage() {
        return image;
    }

    private void setImage(int image) {
        this.image = image;
    }

    public String getDetail() {
        return detail;
    }

    private void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    private void setTime(String time) {
        this.time = time;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public int getNotif_type() {
        return notif_type;
    }

    private void setNotif_type(int notif_type) {
        this.notif_type = notif_type;
    }

    public JSONObject getNotif_detail() {
        return notif_detail;
    }

    private void setNotif_detail(JSONObject notif_detail) {
        this.notif_detail = notif_detail;
    }

    public String getFrom_name() {
        return from_name;
    }

    private void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getNotif_id() {
        return notif_id;
    }

    private void setNotif_id(String notif_id) {
        this.notif_id = notif_id;
    }

    public String getFrom_id() {
        return from_id;
    }

    private void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getDate_time() {
        return date_time;
    }

    private void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getTo_id() {
        return to_id;
    }

    private void setTo_id(String to_id) {
        this.to_id = to_id;
    }
}
