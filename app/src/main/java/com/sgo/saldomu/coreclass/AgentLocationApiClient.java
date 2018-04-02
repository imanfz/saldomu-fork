package com.sgo.saldomu.coreclass;

import com.loopj.android.http.RequestParams;
import com.sgo.saldomu.BuildConfig;

import java.util.UUID;

/**
 * Created by Lenovo on 02/04/2018.
 */

public class AgentLocationApiClient {
    private UUID rcUUID;
    private String rcDateTime;
    private String appID;
    private String senderID;
    private String receiverID;
    private Double latitude;
    private Double longitude;
    private int radius;
    private String webServicePath;
    private String signature;
    private String defaultSignature;
    private String categoryID;

    private RequestParams requestParams;


    public AgentLocationApiClient() {
        requestParams   = new RequestParams();

        rcUUID          = UUID.randomUUID();
        rcDateTime      = DateTimeFormat.getCurrentDateTime();
        appID           = BuildConfig.APP_ID;
        senderID        = DefineValue.BBS_SENDER_ID;
        receiverID      = DefineValue.BBS_RECEIVER_ID;

        requestParams.put(WebParams.RC_UUID, rcUUID);
        requestParams.put(WebParams.RC_DATETIME, rcDateTime);
        requestParams.put(WebParams.APP_ID, appID);
        requestParams.put(WebParams.SENDER_ID, senderID);
        requestParams.put(WebParams.RECEIVER_ID, receiverID);
    }

    public RequestParams webServiceSearchAgent() {
        webServicePath = MyApiClient.getWebserviceName(MyApiClient.LINK_BBS_NEW_SEARCH_AGENT);
        signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + rcDateTime +
                DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.APP_ID + categoryID
                + latitude + longitude));
        return requestParams;
    }

    public UUID getRcUUID() {
        return rcUUID;
    }

    public void setRcUUID(UUID rcUUID) {
        this.rcUUID = rcUUID;
    }

    public String getRcDateTime() {
        return rcDateTime;
    }

    public void setRcDateTime(String rcDateTime) {
        this.rcDateTime = rcDateTime;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getWebServicePath() {
        return webServicePath;
    }

    public void setWebServicePath(String webServicePath) {
        this.webServicePath = webServicePath;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }
}
