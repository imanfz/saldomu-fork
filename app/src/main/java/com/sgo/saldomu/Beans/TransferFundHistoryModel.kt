package com.sgo.saldomu.Beans

class TransferFundHistoryModel {
    private var amount: String = ""
    private var benef_product_code: String = ""
    private var benef_product_name: String = ""
    private var benef_product_type: String = ""
    private var source_product_code //rekening agent
            : String = ""
    private var source_product_name: String = ""
    private var source_product_type: String = ""
    private var source_product_h2h: String = ""
    private var bank_account_destination: String = "" //nomor hp pengirim
    private var pesan: String = ""

    fun TransferFundHistoryModel() {}

    fun TransferFundHistoryModel(
        amount: String,
        benef_product_code: String,
        benef_product_name: String,
        benef_product_type: String,
        bank_account_destination: String,
        source_product_code: String,
        source_product_name: String,
        source_product_type: String,
        source_product_h2h: String,
        pesan: String
    ) {
        setAmount(amount)
        setBenef_product_code(benef_product_code)
        setBenef_product_name(benef_product_name)
        setBenef_product_type(benef_product_type)
        setBank_account_destination(bank_account_destination)
        setSource_product_code(source_product_code)
        setSource_product_name(source_product_name)
        setSource_product_type(source_product_type)
        setSource_product_h2h(source_product_h2h)
        setPesan(pesan)
    }

    fun setAmount(amount: String) {
        this.amount = amount
    }

    fun getAmount(): String {
        return amount
    }

    fun setBenef_product_code(benef_product_code: String) {
        this.benef_product_code = benef_product_code
    }

    fun getBenef_product_code(): String {
        return benef_product_code
    }

    fun setBenef_product_type(benef_product_type: String) {
        this.benef_product_type = benef_product_type
    }

    fun getBenef_product_type(): String {
        return benef_product_type
    }

    fun setSource_product_code(source_product_code: String) {
        this.source_product_code = source_product_code
    }

    fun getSource_product_code(): String {
        return source_product_code
    }

    fun setBank_account_destination(bank_account_destination: String) {
        this.bank_account_destination = bank_account_destination
    }

    fun getBank_account_destinaiton(): String {
        return bank_account_destination
    }

    fun setBenef_product_name(benef_product_name: String) {
        this.benef_product_name = benef_product_name
    }

    fun getBenef_product_name(): String {
        return benef_product_name
    }

    fun setSource_product_name(source_product_name: String) {
        this.source_product_name = source_product_name
    }

    fun getSource_product_name(): String {
        return source_product_name
    }

    fun setSource_product_h2h(source_product_h2h: String) {
        this.source_product_h2h = source_product_h2h
    }

    fun getSource_product_h2h(): String {
        return source_product_h2h
    }

    fun setSource_product_type(source_product_type: String) {
        this.source_product_type = source_product_type
    }

    fun getSource_product_type(): String {
        return source_product_type
    }

    fun setPesan(pesan: String) {
        this.pesan = pesan
    }

    fun getPesan(): String {
        return pesan
    }
}