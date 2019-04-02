package com.sgo.saldomu.widgets;

import java.util.UUID;

/**
 * Created by SGOUser on 29/11/2017.
 */

public class Signature {

//    @SerializedName(WebParams.RC_UUID)
    private UUID RC_UUID;
//    @SerializedName(WebParams.RC_DTIME)
    private String RC_DTIME;
//    @SerializedName(WebParams.SIGNATURE)
    private String SIGNATURE;
//    @SerializedName(WebParams.RQ_UUID)
    private String RQ_UUID;
//    @SerializedName(WebParams.RQ_DTIME)
    private String RQ_DTIME;

    public UUID getRC_UUID() {
        return RC_UUID;
    }

    public void setRC_UUID(UUID RC_UUID) {
        this.RC_UUID = RC_UUID;
    }

    public String getRC_DTIME() {
        return RC_DTIME;
    }

    public void setRC_DTIME(String RC_DTIME) {
        this.RC_DTIME = RC_DTIME;
    }

    public String getSIGNATURE() {
        return SIGNATURE;
    }

    public void setSIGNATURE(String SIGNATURE) {
        this.SIGNATURE = SIGNATURE;
    }

    public String getRQ_UUID() {
        return RQ_UUID;
    }

    public void setRQ_UUID(String RQ_UUID) {
        this.RQ_UUID = RQ_UUID;
    }

    public String getRQ_DTIME() {
        return RQ_DTIME;
    }

    public void setRQ_DTIME(String RQ_DTIME) {
        this.RQ_DTIME = RQ_DTIME;
    }
}
