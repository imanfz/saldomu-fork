package com.sgo.saldomu.Beans;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by yuddistirakiki on 3/22/17.
 */

public class BankHeaderTopUp {
    private String bankName;
    private String bankCode;
    private String otherAtmVa;
    private ArrayList<listBankModel> bankData;

    public BankHeaderTopUp(String header){
        this.setHeader(header);
    }

    public BankHeaderTopUp(String header, String bankCode, String noVA){
        this.setHeader(header);
        this.setBankCode(bankCode);
        this.setOtherAtmVa(noVA);
    }

    public String getHeader() {
        return bankName;
    }

    public void setHeader(String bankName) {
        this.bankName = bankName;
    }

    public ArrayList<listBankModel> getBankData() {
        return bankData;
    }

    public void setBankData(ArrayList<listBankModel> bankData) {
        this.bankData = bankData;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getOtherAtmVa() {
        return otherAtmVa;
    }

    public void setOtherAtmVa(String otherAtmVa) {
        this.otherAtmVa = otherAtmVa;
    }

    public static class CustomComparator implements Comparator<BankHeaderTopUp> {
        @Override
        public int compare(BankHeaderTopUp lhs, BankHeaderTopUp rhs) {
            return lhs.getHeader().compareTo(rhs.getHeader());
        }
    }

}
