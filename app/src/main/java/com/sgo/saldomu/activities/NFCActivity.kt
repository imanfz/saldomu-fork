package com.sgo.saldomu.activities

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import com.sgo.saldomu.R
import com.sgo.saldomu.fragments.PopUpNFC
import com.sgo.saldomu.utils.Converter
import kotlinx.android.synthetic.main.activity_nfc.*

@RequiresApi(Build.VERSION_CODES.KITKAT)
class NFCActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    private var cardSelect: String? = ""
    private var cardAttribute: String? = ""
    private var cardInfo: String? = ""
    private var cardUid: String? = ""
    private var cardBalance: String? = ""
    private var saldo: String? = ""
    private var numberCard: String? = ""

    private var appletType: String? = ""
    private var updateCardKey: String? = ""
    private var session: String? = ""
    private var pendingAmount: String? = ""
    private var institutionReff: String? = ""
    private var sourceOfAccount: String? = ""
    private var merchantData: String? = ""


    private var DateTime: String? = ""
    private var pendingAmountPlus: String? = "0007A40B"
    private var CounterCard: String? = "0000000000000000"
    private var PinEmoney: String? = "000000000000"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

//        cardDetail.setOnClickListener {
//            if (lyt_cardDetail.visibility == View.GONE) {
//                lyt_cardDetail.visibility = View.VISIBLE
////                getCurrentDateTime()
//            } else {
//                lyt_cardDetail.visibility = View.GONE
//            }
//        }
    }



    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null && nfcAdapter!!.isEnabled()) {
            //Yes NFC available
            nfcAdapter?.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A or
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)

        } else if (nfcAdapter != null && !nfcAdapter!!.isEnabled()) {
            val popUpNFC = PopUpNFC()
            popUpNFC.isCancelable = false
            popUpNFC.show(this.getSupportFragmentManager(), "PopUpNFC")
        } else {
            //                Toast.makeText(getActivity(), "Device Tidak Memiliki NFC", Toast.LENGTH_SHORT).show();
        }

    }


    override fun onTagDiscovered(tag: Tag?) {

        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        val selectEmoneyResponse = isoDep.transceive(Converter.hexStringToByteArray(
                "00A40400080000000000000001"))
        runOnUiThread {
//            selectEmoneys.setText(Converter.toHex(selectEmoneyResponse))
            Log.d("SELECT_RESPONSE : ", Converter.toHex(selectEmoneyResponse))
            cardSelect = Converter.toHex(selectEmoneyResponse)
        }

        val cardAttirbuteResponse = isoDep.transceive(Converter.hexStringToByteArray(
                "00F210000B"))
        runOnUiThread {
//            cardAttributes.setText(Converter.toHex(cardAttirbuteResponse))
            Log.d("CARD_ATTRIBUTE : ", Converter.toHex(cardAttirbuteResponse))
            cardAttribute = Converter.toHex(cardAttirbuteResponse)
        }

        runOnUiThread {
//            cardUUID.text = Converter.toHex(tag!!.id)
            Log.d("UUID : ", Converter.toHex(tag!!.id))
            cardUid = Converter.toHex(tag!!.id)
        }

        val cardInfoResponse = isoDep.transceive(Converter.hexStringToByteArray(
                "00B300003F"))
        runOnUiThread {
//            cardInfos.setText(Converter.toHex(cardInfoResponse))
            Log.d("CARD_INFO : ", Converter.toHex(cardInfoResponse))
            cardInfo = Converter.toHex(cardInfoResponse)
            cardNumber.setText(cardInfo!!.substring(0, 16))
            numberCard = cardInfo!!.substring(0, 16)
        }

        val lastBalanceResponse = isoDep.transceive(Converter.hexStringToByteArray(
                "00B500000A"))
        runOnUiThread {
//            lastBalances.setText(Converter.toHex(lastBalanceResponse))
            Log.d("LAST_BALANCE : ", Converter.toHex(lastBalanceResponse))
            cardBalance = Converter.toHex(lastBalanceResponse)
            cardBalanceResult.setText("RP. " + Converter.toLittleEndian(cardBalance!!.substring(0, 8)))
            Log.d("SALDO : ", Converter.toLittleEndian(cardBalance!!.substring(0, 8)).toString())
            saldo = Converter.toLittleEndian(cardBalance!!.substring(0, 8)).toString()
        }

//        runOnUiThread {
//            getResponseCardBalance()
//        }

//        val LC = isoDep.transceive(Converter.hexStringToByteArray(
//                "00E50000462207191611130000000000000000000000000000C34DE2F5C542FA570000000000000000000000000000000000000007A40B0000000000000000000000000000000000000000"))
//        runOnUiThread {
//            Log.d("GetData " , Converter.toHex(LC))
//        }
    }
}
