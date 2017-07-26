package com.sgo.hpku.models;

import java.util.ArrayList;

/**
 * Created by Lenovo on 05/04/2017.
 */

public class ShopDetail {
    private String memberId;
    private String memberCode;
    private String memberName;
    private String memberType;
    private String commId;
    private String commCode;
    private String commName;
    private String shopId;
    private String shopName;
    private String shopMobility;
    private String shopBooking;
    private Double shopLatitude;
    private Double shopLongitude;
    private String shopAddress;
    private String shopDistrict;
    private String shopProvince;
    private String shopCountry;
    private String calculatedDistance;
    private String encodedPoints;
    private String isPolyline;
    private String stepApprove;

    private String shopFirstAddress;
    private String shopSecondAddress;
    private String shopClosed;
    private String shopValue;
    private String setupOpenHour;
    private String shopZipCode;

    //location service
    private String amount;
    private String categoryId;
    private String categoryName;
    private String categoryCode;
    private String txId;
    private String keyCode;
    private String keyName;
    private String keyAddress;
    private String keyDistrict;
    private String keyProvince;
    private String keyCountry;
    private String ccyId;
    private ArrayList<String> categories = new ArrayList<>();

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

    public String getCommId() {
        return commId;
    }

    public void setCommId(String commId) {
        this.commId = commId;
    }

    public String getCommCode() {
        return commCode;
    }

    public void setCommCode(String commCode) {
        this.commCode = commCode;
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

    public String getShopMobility() {
        return shopMobility;
    }

    public void setShopMobility(String shopMobility) {
        this.shopMobility = shopMobility;
    }

    public String getShopBooking() {
        return shopBooking;
    }

    public void setShopBooking(String shopBooking) {
        this.shopBooking = shopBooking;
    }

    public Double getShopLatitude() {
        return shopLatitude;
    }

    public void setShopLatitude(Double shopLatitude) {
        this.shopLatitude = shopLatitude;
    }

    public Double getShopLongitude() {
        return shopLongitude;
    }

    public void setShopLongitude(Double shopLongitude) {
        this.shopLongitude = shopLongitude;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopDistrict() {
        return shopDistrict;
    }

    public void setShopDistrict(String shopDistrict) {
        this.shopDistrict = shopDistrict;
    }

    public String getShopProvince() {
        return shopProvince;
    }

    public void setShopProvince(String shopProvince) {
        this.shopProvince = shopProvince;
    }

    public String getShopCountry() {
        return shopCountry;
    }

    public void setShopCountry(String shopCountry) {
        this.shopCountry = shopCountry;
    }

    public String getCalculatedDistance()
    {
        return calculatedDistance;
    }

    public void setCalculatedDistance(String calculatedDistance)
    {
        this.calculatedDistance = calculatedDistance;
    }

    public String getEncodedPoints() {
        return encodedPoints;
    }

    public void setEncodedPoints(String encodedPoints) {
        this.encodedPoints = encodedPoints;
    }

    public String getIsPolyline() {
        return isPolyline;
    }

    public void setIsPolyline(String polyline) {
        isPolyline = polyline;
    }

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getShopFirstAddress() {
        return shopFirstAddress;
    }

    public void setShopFirstAddress(String shopFirstAddress) {
        this.shopFirstAddress = shopFirstAddress;
    }

    public String getShopSecondAddress() {
        return shopSecondAddress;
    }

    public void setShopSecondAddress(String shopSecondAddress) {
        this.shopSecondAddress = shopSecondAddress;
    }

    public String getShopClosed() {
        return shopClosed;
    }

    public void setShopClosed(String shopClosed) {
        this.shopClosed = shopClosed;
    }

    public String getShopValue() {
        return shopValue;
    }

    public void setShopValue(String shopValue) {
        this.shopValue = shopValue;
    }

    public String getSetupOpenHour() {
        return setupOpenHour;
    }

    public void setSetupOpenHour(String setupOpenHour) {
        this.setupOpenHour = setupOpenHour;
    }

    public String getShopZipCode() {
        return shopZipCode;
    }

    public void setShopZipCode(String shopZipCode) {
        this.shopZipCode = shopZipCode;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyAddress() {
        return keyAddress;
    }

    public void setKeyAddress(String keyAddress) {
        this.keyAddress = keyAddress;
    }

    public String getKeyDistrict() {
        return keyDistrict;
    }

    public void setKeyDistrict(String keyDistrict) {
        this.keyDistrict = keyDistrict;
    }

    public String getKeyProvince() {
        return keyProvince;
    }

    public void setKeyProvince(String keyProvince) {
        this.keyProvince = keyProvince;
    }

    public String getKeyCountry() {
        return keyCountry;
    }

    public void setKeyCountry(String keyCountry) {
        this.keyCountry = keyCountry;
    }

    public String getCcyId() {
        return ccyId;
    }

    public void setCcyId(String ccyId) {
        this.ccyId = ccyId;
    }

    public void setCategories(String categoryName) {
        this.categories.add(categoryName);
    }

    public ArrayList<String> getCategories() {
        return this.categories;
    }

    public String getStepApprove() {
        return this.stepApprove;
    }

    public void setStepApprove(String stepApprove) {
        this.stepApprove = stepApprove;
    }
}
