package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetMemberModel extends jsonModel {
    @SerializedName("member_data")
    @Expose
    private
    List<MemberDataModel> memberData;
    @SerializedName("member_dap")
    @Expose
    private
    List<MemberDapModel> member_dap;
    @SerializedName("non_member")
    @Expose
    private
    List<NonMemberModel> non_member;

    public List<MemberDataModel> getMemberData() {
        return memberData;
    }

    public List<MemberDapModel> getMember_dap() {
        return member_dap;
    }

    public List<NonMemberModel> getNon_member() {
        return non_member;
    }
}
