package com.sgo.saldomu.models;

import java.util.Objects;

public class Invoice {
    String reason_code;
    String reason_description;
    String invoice_number;

    public Invoice(String reason_code, String reason_description, String invoice_number) {
        this.reason_code = reason_code;
        this.reason_description = reason_description;
        this.invoice_number = invoice_number;
    }

    public String getReason_code() {
        return reason_code;
    }

    public void setReason_code(String reason_code) {
        this.reason_code = reason_code;
    }

    public String getReason_description() {
        return reason_description;
    }

    public void setReason_description(String reason_description) {
        this.reason_description = reason_description;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(reason_code, invoice.reason_code) &&
                Objects.equals(invoice_number, invoice.invoice_number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason_code, invoice_number);
    }
}
