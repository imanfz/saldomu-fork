package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginCommunityModel {
    @SerializedName("comm_id")
    @Expose
    private String commId;
    @SerializedName("comm_code")
    @Expose
    private String commCode;
    @SerializedName("comm_name")
    @Expose
    private String commName;
    @SerializedName("api_key")
    @Expose
    private String apiKey;
    @SerializedName("callback_url")
    @Expose
    private String callbackUrl;
    @SerializedName("buss_scheme_code")
    @Expose
    private String bussSchemeCode;
    @SerializedName("authentication_type")
    @Expose
    private String authenticationType;
    @SerializedName("length_auth")
    @Expose
    private String lengthAuth;
    @SerializedName("is_have_pin")
    @Expose
    private String isHavePin;
    @SerializedName("member_level")
    @Expose
    private String memberLevel;
    @SerializedName("allow_member_level")
    @Expose
    private String allowMemberLevel;
    @SerializedName("can_transfer")
    @Expose
    private String canTransfer;
    @SerializedName("enable_agent")
    @Expose
    private String enableAgent;
    @SerializedName("agent_h2h")
    @Expose
    private String agentH2h;
    @SerializedName("is_new_bulk")
    @Expose
    private String isNewBulk;
    @SerializedName("subcategory_code")
    @Expose
    private String subcategoryCode;
    @SerializedName("report_range")
    @Expose
    private String reportRange;
    @SerializedName("report_latest")
    @Expose
    private String reportLatest;
    @SerializedName("enable_merchant")
    @Expose
    private String enableMerchant;
    @SerializedName("is_merchant")
    @Expose
    private String isMerchant;
    @SerializedName("is_agent")
    @Expose
    private int isAgent;
    @SerializedName("merchant_subcategory")
    @Expose
    private String merchantSubcategory;
    @SerializedName("flow_agent_type")
    @Expose
    private Object flowAgentType;
    @SerializedName("unregister_member")
    @Expose
    private String unregisterMember;
    @SerializedName("agent_scheme_codes")
    @Expose
    private List<SchemeCodeModel> agent_scheme_codes;

    @SerializedName("agent_type")
    @Expose
    private String agent_type;

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

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getBussSchemeCode() {
        return bussSchemeCode;
    }

    public void setBussSchemeCode(String bussSchemeCode) {
        this.bussSchemeCode = bussSchemeCode;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getLengthAuth() {
        return lengthAuth;
    }

    public void setLengthAuth(String lengthAuth) {
        this.lengthAuth = lengthAuth;
    }

    public String getIsHavePin() {
        return isHavePin;
    }

    public void setIsHavePin(String isHavePin) {
        this.isHavePin = isHavePin;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
    }

    public String getAllowMemberLevel() {
        return allowMemberLevel;
    }

    public void setAllowMemberLevel(String allowMemberLevel) {
        this.allowMemberLevel = allowMemberLevel;
    }

    public String getCanTransfer() {
        return canTransfer;
    }

    public void setCanTransfer(String canTransfer) {
        this.canTransfer = canTransfer;
    }

    public String getEnableAgent() {
        return enableAgent;
    }

    public void setEnableAgent(String enableAgent) {
        this.enableAgent = enableAgent;
    }

    public String getAgentH2h() {
        return agentH2h;
    }

    public void setAgentH2h(String agentH2h) {
        this.agentH2h = agentH2h;
    }

    public String getIsNewBulk() {
        return isNewBulk;
    }

    public void setIsNewBulk(String isNewBulk) {
        this.isNewBulk = isNewBulk;
    }

    public String getSubcategoryCode() {
        return subcategoryCode;
    }

    public void setSubcategoryCode(String subcategoryCode) {
        this.subcategoryCode = subcategoryCode;
    }

    public String getReportRange() {
        return reportRange;
    }

    public void setReportRange(String reportRange) {
        this.reportRange = reportRange;
    }

    public String getReportLatest() {
        return reportLatest;
    }

    public void setReportLatest(String reportLatest) {
        this.reportLatest = reportLatest;
    }

    public String getEnableMerchant() {
        return enableMerchant;
    }

    public void setEnableMerchant(String enableMerchant) {
        this.enableMerchant = enableMerchant;
    }

    public String getIsMerchant() {
        return isMerchant;
    }

    public void setIsMerchant(String isMerchant) {
        this.isMerchant = isMerchant;
    }

    public int getIsAgent() {
        return isAgent;
    }

    public String getMerchantSubcategory() {
        return merchantSubcategory;
    }

    public Object getFlowAgentType() {
        return flowAgentType;
    }

    public String getUnregisterMember() {
        return unregisterMember;
    }

    public List<SchemeCodeModel> getAgent_scheme_codes() {
        return agent_scheme_codes;
    }

    public String getAgent_type() {
        if (agent_type == null)
            agent_type = "";
        return agent_type;
    }
}
