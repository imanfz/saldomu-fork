package com.sgo.orimakardaya.Beans;

import org.json.JSONObject;

/**
 * Created by thinkpad on 3/19/2015.
 */
public class NotificationModelClass {

    private int image;
    private String name;
    private String from_name;
    private String detail;
    private String time;
    private int notif_type;
    private String notif_id;
    private boolean read;
    private JSONObject notif_detail;

    public NotificationModelClass(String _notif_id,int _image, String _name, String _from_name, String _detail, String _time, int _notif_type, Boolean _read, JSONObject _notif_detail) {
        this.setNotif_id(_notif_id);
        this.setImage(_image);
        this.setName(_name);
        this.setFrom_name(_from_name);
        this.setDetail(_detail);
        this.setTime(_time);
        this.setNotif_type(_notif_type);
        this.setRead(_read);
        this.setNotif_detail(_notif_detail);
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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

    public void setNotif_type(int notif_type) {
        this.notif_type = notif_type;
    }

    public JSONObject getNotif_detail() {
        return notif_detail;
    }

    public void setNotif_detail(JSONObject notif_detail) {
        this.notif_detail = notif_detail;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getNotif_id() {
        return notif_id;
    }

    public void setNotif_id(String notif_id) {
        this.notif_id = notif_id;
    }
}
