package com.sgo.orimakardaya.Beans;

/**
 * Created by yuddistirakiki on 2/2/17.
 */

public class BBSComm {
    private String commId;
    private String commCode;
    private String commName;
    private String apiKey;
    private String memberCode;
    private String callbackUrl;

    public BBSComm(){}

    public BBSComm(String commId, String commCode, String commName, String apiKey, String memberCode, String callbackUrl){
        this.setCommId(commId);
        this.setCommCode(commCode);
        this.setCommName(commName);
        this.setApiKey(apiKey);
        this.setMemberCode(memberCode);
        this.setCallbackUrl(callbackUrl);
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

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
