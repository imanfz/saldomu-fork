package com.sgo.saldomu.Beans;

import java.util.Comparator;

/**
 * Created by yuddistirakiki on 3/22/17.
 */

public class BankHeaderTopUp {
    private String header;
    private Boolean expanded;

    public BankHeaderTopUp(){}
    public BankHeaderTopUp(String header){
        this.setHeader(header);
        this.setExpanded(false);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public static class CustomComparator implements Comparator<BankHeaderTopUp> {
        @Override
        public int compare(BankHeaderTopUp lhs, BankHeaderTopUp rhs) {
            return lhs.getHeader().compareTo(rhs.getHeader());
        }
    }

}
