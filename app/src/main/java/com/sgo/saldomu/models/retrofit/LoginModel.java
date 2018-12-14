package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginModel extends jsonModel {
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("cust_id")
    @Expose
    private String custId;
    @SerializedName("cust_name")
    @Expose
    private String custName;
    @SerializedName("is_member_shop_dgi")
    @Expose
    private String is_member_shop_dgi;
    @SerializedName("community")
    @Expose
    private List<LoginCommunityModel> community = null;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("date_of_birth")
    @Expose
    private String dateOfBirth;
    @SerializedName("birth_place")
    @Expose
    private String birthPlace;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("social_id")
    @Expose
    private String socialId;
    @SerializedName("idtype")
    @Expose
    private String idtype;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("hobby")
    @Expose
    private String hobby;
    @SerializedName("mother_name")
    @Expose
    private String motherName;
    @SerializedName("contact_email")
    @Expose
    private String contactEmail;
    @SerializedName("last_login")
    @Expose
    private String lastLogin;
    @SerializedName("user_is_new")
    @Expose
    private String userIsNew;
    @SerializedName("verified")
    @Expose
    private String verified;
    @SerializedName("pin_mode")
    @Expose
    private String pinMode;
    @SerializedName("user_is_synced")
    @Expose
    private String userIsSynced;
    @SerializedName("last_synced")
    @Expose
    private String lastSynced;
    @SerializedName("category_id")
    @Expose
    private String categoryId;
    @SerializedName("category_name")
    @Expose
    private String categoryName;
    @SerializedName("is_registered")
    @Expose
    private String isRegistered;
    @SerializedName("changed_pass")
    @Expose
    private String changedPass;
    @SerializedName("img_url")
    @Expose
    private String imgUrl;
    @SerializedName("img_small_url")
    @Expose
    private String imgSmallUrl;
    @SerializedName("img_medium_url")
    @Expose
    private String imgMediumUrl;
    @SerializedName("img_large_url")
    @Expose
    private String imgLargeUrl;
    @SerializedName("settings")
    @Expose
    private List<LoginSettingModel> settings = null;
    @SerializedName("failed_attempt")
    @Expose
    private String failedAttempt;
    @SerializedName("max_failed")
    @Expose
    private String maxFailed;
    @SerializedName("id_types")
    @Expose
    private List<LoginIDTypeModel> idTypes = null;
    @SerializedName("social_signature")
    @Expose
    private String socialSignature;
    @SerializedName("shop_id_agent")
    @Expose
    private String shopIdAgent;
    @SerializedName("get_notification")
    @Expose
    private String getNotification;
    @SerializedName("kyc_status")
    @Expose
    private String kycStatus;
    @SerializedName("reject_ktp")
    @Expose
    private String rejectKtp;
    @SerializedName("remark_ktp")
    @Expose
    private String remarkKtp;
    @SerializedName("reject_foto")
    @Expose
    private String rejectFoto;
    @SerializedName("remark_foto")
    @Expose
    private String remarkFoto;
    @SerializedName("reject_ttd")
    @Expose
    private String rejectTtd;
    @SerializedName("remark_ttd")
    @Expose
    private String remarkTtd;
    @SerializedName("ref")
    @Expose
    private String ref;
    @SerializedName("access_key")
    @Expose
    private String accessKey;
    @SerializedName("access_secret")
    @Expose
    private String accessSecret;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public List<LoginCommunityModel> getCommunity() {
        return community;
    }

    public void setCommunity(List<LoginCommunityModel> community) {
        this.community = community;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public String getIdtype() {
        return idtype;
    }

    public void setIdtype(String idtype) {
        this.idtype = idtype;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getUserIsNew() {
        return userIsNew;
    }

    public void setUserIsNew(String userIsNew) {
        this.userIsNew = userIsNew;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getPinMode() {
        return pinMode;
    }

    public void setPinMode(String pinMode) {
        this.pinMode = pinMode;
    }

    public String getUserIsSynced() {
        return userIsSynced;
    }

    public void setUserIsSynced(String userIsSynced) {
        this.userIsSynced = userIsSynced;
    }

    public String getLastSynced() {
        return lastSynced;
    }

    public void setLastSynced(String lastSynced) {
        this.lastSynced = lastSynced;
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

    public String getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(String isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getChangedPass() {
        return changedPass;
    }

    public void setChangedPass(String changedPass) {
        this.changedPass = changedPass;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgSmallUrl() {
        return imgSmallUrl;
    }

    public void setImgSmallUrl(String imgSmallUrl) {
        this.imgSmallUrl = imgSmallUrl;
    }

    public String getImgMediumUrl() {
        return imgMediumUrl;
    }

    public void setImgMediumUrl(String imgMediumUrl) {
        this.imgMediumUrl = imgMediumUrl;
    }

    public String getImgLargeUrl() {
        return imgLargeUrl;
    }

    public void setImgLargeUrl(String imgLargeUrl) {
        this.imgLargeUrl = imgLargeUrl;
    }

    public List<LoginSettingModel> getSettings() {
        return settings;
    }

    public void setSettings(List<LoginSettingModel> settings) {
        this.settings = settings;
    }

    public String getFailedAttempt() {
        if (failedAttempt.equals(""))
            failedAttempt = "0";
        return failedAttempt;
    }

    public void setFailedAttempt(String failedAttempt) {
        this.failedAttempt = failedAttempt;
    }

    public String getMaxFailed() {
        return maxFailed;
    }

    public void setMaxFailed(String maxFailed) {
        this.maxFailed = maxFailed;
    }

    public List<LoginIDTypeModel> getIdTypes() {
        return idTypes;
    }

    public void setIdTypes(List<LoginIDTypeModel> idTypes) {
        this.idTypes = idTypes;
    }

    public String getSocialSignature() {
        return socialSignature;
    }

    public void setSocialSignature(String socialSignature) {
        this.socialSignature = socialSignature;
    }

    public String getShopIdAgent() {
        return shopIdAgent;
    }

    public void setShopIdAgent(String shopIdAgent) {
        this.shopIdAgent = shopIdAgent;
    }

    public String getGetNotification() {
        return getNotification;
    }

    public void setGetNotification(String getNotification) {
        this.getNotification = getNotification;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getRejectKtp() {
        return rejectKtp;
    }

    public void setRejectKtp(String rejectKtp) {
        this.rejectKtp = rejectKtp;
    }

    public String getRemarkKtp() {
        return remarkKtp;
    }

    public void setRemarkKtp(String remarkKtp) {
        this.remarkKtp = remarkKtp;
    }

    public String getRejectFoto() {
        return rejectFoto;
    }

    public void setRejectFoto(String rejectFoto) {
        this.rejectFoto = rejectFoto;
    }

    public String getRemarkFoto() {
        return remarkFoto;
    }

    public void setRemarkFoto(String remarkFoto) {
        this.remarkFoto = remarkFoto;
    }

    public String getRejectTtd() {
        return rejectTtd;
    }

    public void setRejectTtd(String rejectTtd) {
        this.rejectTtd = rejectTtd;
    }

    public String getRemarkTtd() {
        return remarkTtd;
    }

    public void setRemarkTtd(String remarkTtd) {
        this.remarkTtd = remarkTtd;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getIs_member_shop_dgi() {
        return is_member_shop_dgi;
    }
}
