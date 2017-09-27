package com.sgo.saldomu.Beans;

import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.MyApiClient;

import java.util.ArrayList;

/**
 * Created by yuddistirakiki on 12/3/16.
 */

public class BankDataTopUp {

    private ArrayList<ListBankDataTopup> bankData;
    private String noVa;
    private String fee;
    private String bankCode;

    public BankDataTopUp(ArrayList<ListBankDataTopup> bankData,String bankCode, String noVa, String fee){
        this.setBankData(bankData);
        this.setNoVa(noVa);
        if (fee != null && !fee.isEmpty() && !fee.equals("null")) {
            if (!fee.equals("0.00"))
                this.setFee(MyApiClient.CCY_VALUE + " " + CurrencyFormat.format(fee));
        }
        this.setBankCode(bankCode);
    }



    public String getNoVa() {
        return noVa;
    }

    public void setNoVa(String noVa) {
        this.noVa = noVa;
    }


    public String getBankCode() {
        return bankCode;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public ArrayList<ListBankDataTopup> getBankData() {
        return bankData;
    }

    public void setBankData(ArrayList<ListBankDataTopup> bankData) {
        this.bankData = bankData;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
}
