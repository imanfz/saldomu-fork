package com.sgo.hpku.Beans;

/**
 * Created by thinkpad on 4/16/2015.
 */
public class MyGroupObject {
    private int type;
    private String groupID;
    private String groupName;
    private String memberName;
    private String memberProfilePicture;
    private int sectionPosition;
    private int listPosition;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberProfilePicture() {
        return memberProfilePicture;
    }

    public void setMemberProfilePicture(String memberProfilePicture) {
        this.memberProfilePicture = memberProfilePicture;
    }

    public int getSectionPosition() {
        return sectionPosition;
    }

    public void setSectionPosition(int sectionPosition) {
        this.sectionPosition = sectionPosition;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
