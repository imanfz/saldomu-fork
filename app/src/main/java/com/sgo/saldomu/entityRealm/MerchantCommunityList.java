package com.sgo.saldomu.entityRealm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Lenovo on 07/03/2017.
 */

public class MerchantCommunityList extends RealmObject {

    @PrimaryKey
    private String memberId;
    private String memberCode;
    private String memberName;
    private String memberType;
    private String shopId;
    private String shopName;
    private String commName;
    private String commCode;
    private String address1;
    private String district;
    private String province;
    private String country;
    private String setupOpenHour;
    private String memberCust;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getCommCode() {
        return commCode;
    }

    public void setCommCode(String commCode) {
        this.commCode = commCode;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSetupOpenHour() {
        return setupOpenHour;
    }

    public void setSetupOpenHour(String setupOpenHour) {
        this.setupOpenHour = setupOpenHour;
    }

    public String getMemberCust() {
        return memberCust;
    }

    public void setMemberCust(String memberCust) {
        this.memberCust = memberCust;
    }
}
