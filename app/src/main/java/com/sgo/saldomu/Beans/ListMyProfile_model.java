package com.sgo.saldomu.Beans;

/**
 * Created by Lenovo Thinkpad on 10/2/2017.
 */

public class ListMyProfile_model {
    String noHP;
    String nama;
    String email;
    String dob;
    Boolean isMemberBasic;
    Boolean isNext;

    public ListMyProfile_model(String noHP, String nama, String email, String dob, Boolean isMemberBasic, Boolean isNext)
    {
        this.setNoHP(noHP);
        this.setNama(nama);
        this.setEmail(email);
        this.setDob(dob);
        this.setMemberBasic(isMemberBasic);
        this.setNext (isNext);
    }

    public void setNoHP(String noHP) { this.noHP = noHP;    }

    public String getNoHP() {        return noHP;    }

    public void setNama(String nama) {        this.nama = nama;    }

    public String getNama() {        return nama;    }

    public void setEmail(String email) {        this.email = email;    }

    public String getEmail() {        return email;    }

    public void setDob(String dob) {        this.dob = dob;    }

    public String getDob() {        return dob;    }

    public void setMemberBasic(Boolean memberBasic) { isMemberBasic = memberBasic;    }

    public Boolean getMemberBasic() {        return isMemberBasic;    }

    public void setNext(Boolean next) {
        isNext = next;
    }

    public Boolean getNext() {        return isNext;    }
}
