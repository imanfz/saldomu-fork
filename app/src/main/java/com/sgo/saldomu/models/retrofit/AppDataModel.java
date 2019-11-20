package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppDataModel {
    @SerializedName("logo")
    @Expose
    private Object logo;
    @SerializedName("expiry_booking")
    @Expose
    private Object expiryBooking;
    @SerializedName("application_name")
    @Expose
    private String applicationName;
    @SerializedName("package_name")
    @Expose
    private String packageName;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("modified")
    @Expose
    private String modified;
    @SerializedName("package_version")
    @Expose
    private String packageVersion;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("days")
    @Expose
    private Object days;
    @SerializedName("base_color")
    @Expose
    private Object baseColor;
    @SerializedName("description")
    @Expose
    private Object description;
    @SerializedName("download_url")
    @Expose
    private String downloadUrl;
    @SerializedName("short_url")
    @Expose
    private String shortUrl;
    @SerializedName("active")
    @Expose
    private String active;
    @SerializedName("radius")
    @Expose
    private Object radius;
    @SerializedName("disable")
    @Expose
    private String disable;
    @SerializedName("app_id")
    @Expose
    private String appId;
    @SerializedName("package_version_code")
    @Expose
    private String packageVersionCode;

    public Object getLogo() {
        return logo;
    }

    public void setLogo(Object logo) {
        this.logo = logo;
    }

    public Object getExpiryBooking() {
        return expiryBooking;
    }

    public void setExpiryBooking(Object expiryBooking) {
        this.expiryBooking = expiryBooking;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getDays() {
        return days;
    }

    public void setDays(Object days) {
        this.days = days;
    }

    public Object getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(Object baseColor) {
        this.baseColor = baseColor;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Object getRadius() {
        return radius;
    }

    public void setRadius(Object radius) {
        this.radius = radius;
    }

    public String getDisable() {
        return disable;
    }

    public void setDisable(String disable) {
        this.disable = disable;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPackageVersionCode() {
        return packageVersionCode;
    }

    public void setPackageVersionCode(String packageVersionCode) {
        this.packageVersionCode = packageVersionCode;
    }
}
