package com.sgo.saldomu.models;

public class InvoiceDGI {

        String docNo;
        String docId;
        String docDesc;
        String amount;
        String remainAmount;
        String holdAmount;
        String ccy;
        String dueDate;
        String inputAmount;
        String sessionId;

        public InvoiceDGI(String docNo, String docId, String docDesc, String amount, String remainAmount, String holdAmount, String ccy, String dueDate, String inputAmount, String sessionId) {
            this.docNo = docNo;
            this.docId = docId;
            this.docDesc = docDesc;
            this.amount = amount;
            this.remainAmount = remainAmount;
            this.holdAmount = holdAmount;
            this.ccy = ccy;
            this.dueDate = dueDate;
            this.inputAmount = inputAmount;
            this.sessionId = sessionId;
        }

        public String getDocNo() {
            return docNo;
        }

        public void setDocNo(String docNo) {
            this.docNo = docNo;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocDesc() {
            return docDesc;
        }

        public void setDocDesc(String docDesc) {
            this.docDesc = docDesc;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getRemainAmount() {
            return remainAmount;
        }

        public void setRemainAmount(String remainAmount) {
            this.remainAmount = remainAmount;
        }

        public String getHoldAmount() {
            return holdAmount;
        }

        public void setHoldAmount(String holdAmount) {
            this.holdAmount = holdAmount;
        }

        public String getCcy() {
            return ccy;
        }

        public void setCcy(String ccy) {
            this.ccy = ccy;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getInputAmount() {
            return inputAmount;
        }

        public void setInputAmount(String inputAmount) {
            this.inputAmount = inputAmount;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

}
