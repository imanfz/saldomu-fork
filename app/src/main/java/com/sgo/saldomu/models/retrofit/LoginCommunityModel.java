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

    @SerializedName("agent_biller_codes")
    @Expose
    private List<String> agent_biller_codes;

    @SerializedName("agent_trx_codes")
    @Expose
    private List<String> agent_trx_codes;

    @SerializedName("agent_type")
    @Expose
    private String agent_type;

    @SerializedName("is_agent_trx_request")
    @Expose
    private String is_agent_trx_request;
    @SerializedName("comm_upgrade_member")
    @Expose
    private String comm_upgrade_member;
    @SerializedName("member_created")
    @Expose
    private String member_created;
    @SerializedName("company_type")
    @Expose
    private String company_type;
    @SerializedName("force_change_pin")
    @Expose
    private String force_change_pin;

    @SerializedName("is_agent_cta_mandirilkd")
    @Expose
    private String is_agent_cta_mandirilkd;

    @SerializedName("is_agent_atc_mandirilkd")
    @Expose
    private String is_agent_atc_mandirilkd;

    @SerializedName("use_deposit_ccol")
    @Expose
    private String use_deposit_ccol;

    @SerializedName("use_deposit_col")
    @Expose
    private String use_deposit_col;


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

    public String getIs_agent_trx_request() {
        return is_agent_trx_request;
    }

    public void setComm_upgrade_member(String comm_upgrade_member) {
        this.comm_upgrade_member = comm_upgrade_member;
    }

    public String getComm_upgrade_member() {return comm_upgrade_member;}

    public void setMember_created(String member_created) {
        this.member_created = member_created;
    }

    public String getMember_created() {
        return member_created;
    }

    public void setCompany_type(String company_type) {
        this.company_type = company_type;
    }

    public String getCompany_type() {
        return company_type;
    }

    public void setForce_change_pin(String force_change_pin) {
        this.force_change_pin = force_change_pin;
    }

    public String getForce_change_pin() {
        return force_change_pin;
    }

    public List<String> getAgent_biller_codes() {
        return agent_biller_codes;
    }

    public List<String> getAgent_trx_codes() {
        return agent_trx_codes;
    }

    public String getIs_agent_atc_mandirilkd() {
        return is_agent_atc_mandirilkd;
    }

    public String getIs_agent_cta_mandirilkd() {
        return is_agent_cta_mandirilkd;
    }

    public String getUse_deposit_ccol() {
        return use_deposit_ccol;
    }

    public void setUse_deposit_ccol(String use_deposit_ccol) {
        this.use_deposit_ccol = use_deposit_ccol;
    }

    public String getUse_deposit_col() {
        return use_deposit_col;
    }

    public void setUse_deposit_col(String use_deposit_col) {
        this.use_deposit_col = use_deposit_col;
    }
}
