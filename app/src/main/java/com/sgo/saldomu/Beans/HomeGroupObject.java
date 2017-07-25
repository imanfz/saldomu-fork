package com.sgo.saldomu.Beans;

/**
 * Created by thinkpad on 4/15/2015.
 */
public class HomeGroupObject {

    private int type;
    private String profpic;
    private String groupName;
    private String pay;
    private String getPaid;
    private String desc;
    private String date;
    private int sectionPosition;
    private int listPosition;

    public int getSectionPosition() {
        return sectionPosition;
    }

    public void setSectionPosition(int sectionPosition) {
        this.sectionPosition = sectionPosition;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGetPaid() {
        return getPaid;
    }

    public void setGetPaid(String getPaid) {
        this.getPaid = getPaid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public String getProfpic() {
        return profpic;
    }

    public void setProfpic(String profpic) {
        this.profpic = profpic;
    }
}
