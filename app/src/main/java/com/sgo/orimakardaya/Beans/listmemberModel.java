package com.sgo.orimakardaya.Beans;/*
  Created by Administrator on 12/3/2014.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Member")
class listmemberModel extends Model {

    @Column
    private String member_id;

    @Column
    private String member_code;

    public listmemberModel(){
        super();
    }
    public listmemberModel(String _member_id, String _member_code){
        super();
        this.setMember_id(_member_id);
        this.setMember_code(_member_code);

    }


    public String getMember_id() {
        return member_id;
    }

    private void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getMember_code() {
        return member_code;
    }

    private void setMember_code(String member_code) {
        this.member_code = member_code;
    }
}
