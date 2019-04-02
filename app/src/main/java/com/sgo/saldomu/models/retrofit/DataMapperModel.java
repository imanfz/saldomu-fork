package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataMapperModel {
    @SerializedName("user_sender_id")
    @Expose
    private
    String user_sender_id;
    @SerializedName("user_receiver_id")
    @Expose
    private
    String user_receiver_id;
    @SerializedName("post_id")
    @Expose
    private
    String post_id;
    @SerializedName("payment_id")
    @Expose
    private
    String payment_id;
    @SerializedName("sent_id")
    @Expose
    private
    String sent_id;
    @SerializedName("trx_id")
    @Expose
    private
    String trx_id;
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("is_member_temp")
    @Expose
    private
    String is_member_temp;
}
