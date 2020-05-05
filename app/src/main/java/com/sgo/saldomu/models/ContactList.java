package com.sgo.saldomu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactList  implements Parcelable {


    String name = "";
    String phoneNo = "";

    public ContactList(String name, String phoneNo) {
        this.name = name;
        this.phoneNo = phoneNo;
    }

    protected ContactList(Parcel in) {
        name = in.readString();
        phoneNo = in.readString();
    }

    public static final Creator<ContactList> CREATOR = new Creator<ContactList>() {
        @Override
        public ContactList createFromParcel(Parcel in) {
            return new ContactList(in);
        }

        @Override
        public ContactList[] newArray(int size) {
            return new ContactList[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phoneNo);
    }
}

