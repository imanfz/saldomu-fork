package com.sgo.saldomu.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.fragments.PopUpNFC;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.CheckCardBalanceModel;
import com.sgo.saldomu.models.retrofit.UpdateCardModel;
import com.sgo.saldomu.utils.Converter;
import com.sgo.saldomu.widgets.BaseActivity;

import java.io.IOException;
import java.util.HashMap;

import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NFCActivity extends BaseActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter;

    private String cardSelect;
    private String cardAttribute;
    private String cardInfo;
    private String cardUid;
    private String cardBalance;
    private String saldo;
    private String numberCard;

    private String appletType;
    private String updateCardKey;
    private String session = "";
    private String institutionRef = "";
    private String sourceOfAccount = "";
    private String pendingAmount = "";
    private String merchantData = "";

    private IsoDep isoDep;
    private String cardMessage = "";
    private String mitraCode;
    private String merchantType;
    private String reverse_card_key;
    private boolean reversalSuccess = false;
    private Boolean testReversal = false;
    private Boolean cardShifted = false;
    private int count = 1;

    private TextView cardNumber, cardBalanceResult;
    private RelativeLayout lyt_gifNfc;
    private LinearLayout lyt_emonCard;

    public final static String TYPE_OLD_APPLET = "0";
    public final static String TYPE_NEW_APPLET = "1";


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_nfc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        nfcMger = new NFCManager(this);

        cardNumber = findViewById(R.id.cardNumber);
        cardBalanceResult = findViewById(R.id.cardBalanceResult);
        lyt_gifNfc = findViewById(R.id.lyt_gifNfc);
        lyt_emonCard = findViewById(R.id.lyt_emonCard);
    }

    private void initializeToolbar() {
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle("Cek Saldo");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() > 0)
                    getFragmentManager().popBackStack();
                else
                    finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            //Yes NFC available
            nfcAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);

        } else if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            PopUpNFC popUpNFC = new PopUpNFC();
            popUpNFC.setCancelable(false);
            popUpNFC.show(this.getSupportFragmentManager(), "PopUpNFC");
        } else {
            //                Toast.makeText(getActivity(), "Device Tidak Memiliki NFC", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();

            byte[] selectEmoneyResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00A40400080000000000000001"));
            byte[] cardAttirbuteResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00F210000B"));
            byte[] cardInfoResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00B300003F"));
            byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00B500000A"));


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("SELECT_RESPONSE : ", Converter.Companion.toHex(selectEmoneyResponse));
                    cardSelect = Converter.Companion.toHex(selectEmoneyResponse);

                    if (cardSelect.equals("9000")) {
                        Log.d("CARD_ATTRIBUTE : ", Converter.Companion.toHex(cardAttirbuteResponse));
                        cardAttribute = Converter.Companion.toHex(cardAttirbuteResponse);

                        Log.d("UUID : ", Converter.Companion.toHex(tag.getId()));
                        cardUid = Converter.Companion.toHex(tag.getId());

                        Log.d("CARD_INFO : ", Converter.Companion.toHex(cardInfoResponse));
                        cardInfo = Converter.Companion.toHex(cardInfoResponse);
                        cardNumber.setText(cardInfo.substring(0, 16));
                        numberCard = cardInfo.substring(0, 16);

                        Log.d("LAST_BALANCE : ", Converter.Companion.toHex(lastBalanceResponse));
                        cardBalance = Converter.Companion.toHex(lastBalanceResponse);
                        cardBalanceResult.setText("RP. " + CurrencyFormat.format(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));
                        Log.d("SALDO : ", String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8))));
                        saldo = String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));

                        lyt_gifNfc.setVisibility(View.GONE);
                        lyt_emonCard.setVisibility(View.VISIBLE);

                        if (cardShifted == true) {
                            getReversalUpdateCard();
                        } else {
                            getCheckCardBalance2();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Kartu anda salah", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            Timber.d("Kartu Geser....... ON TAGDISCOVERED " + e.getMessage());
            dismissProgressDialog();
            Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    private String getData(String session, String institutionReff, String source, String pendingAmount, String merchantDat) {

        String date = "310120041057"; // from 01-04-2019 11:54:57 ddmmyyhhmmss
        String counterCard = "0000000000000000"; // Constant
        String pin = "000000000000"; // constant
        String reffData = session;
        String instutionRef = institutionReff;
        String sourceAccount = source;
        String merchantData = merchantDat;

        byte[] PENDAMOUNTS = Converter.Companion.intToByteArray(Integer.parseInt(pendingAmount));
        Log.d("ISI PENDING AMOUNT5 : ", (Converter.Companion.toHex(PENDAMOUNTS)));

        String data = date + counterCard + pin + reffData + instutionRef + sourceAccount + Converter.Companion.toHex(PENDAMOUNTS) + merchantData;

        Log.d("LC & Data:", "DATA: " + data);


        String temp = "00E50000" + "46" /*hardcode*/ + data;

        Log.d("", "ISI DATA : " + temp);


        return temp;
    }

    private void getCheckCardBalance2() {
        try {
            showProgressDialog();

            extraSignature = numberCard;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.CHEK_CARD_BALANCE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.ORDER_ID, numberCard);
//            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);

            Timber.d("isi params ChekCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.CHEK_CARD_BALANCE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            CheckCardBalanceModel model = getGson().fromJson(object, CheckCardBalanceModel.class);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                Toast.makeText(getBaseContext(), "CHEK CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
                                session = model.getSession();
                                updateCardKey = model.getUpdateCardKey();
                                appletType = model.getAppletType();
                                mitraCode = model.getMitraCode();
                                merchantType = model.getMerchantType();
                                institutionRef = model.getInstitutionReff();
                                sourceOfAccount = model.getSourceOfAccount();
                                pendingAmount = model.getPendingAmount();
                                merchantData = model.getMerchantData();


                                if (appletType.equals(TYPE_OLD_APPLET)) {
                                    getUpdateOldCard(cardInfo);
                                } else {
                                    if (!pendingAmount.equals("0")) {
                                        getUpdateNewCard();
                                    } else {
                                        return;
                                    }
                                }

                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
                                dismissProgressDialog();
                                getFragmentManager().popBackStack();
                                return;
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();
                        }

                        @Override
                        public void onComplete() {
//                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
            return;
        }
    }

    private void getUpdateOldCard(String msg) {
        try {
            showProgressDialog();

            extraSignature = numberCard;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.UPDATE_CARD_BALANCE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.ORDER_ID, numberCard);
//            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.SESSION, session);
            params.put(WebParams.MESSAGE, msg);

            Timber.wtf("isi params UpdateOldCardBalance:" + params.toString());
            Timber.wtf("COUNT:%s", count);

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.UPDATE_CARD_BALANCE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response UpdateCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                Toast.makeText(getBaseContext(), "UPDATE CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
                                try {
                                    byte[] messageAPDU = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                                            model.getMessage()));

                                    Log.d("MESSAGE TO APDU : ", Converter.Companion.toHex(messageAPDU));
                                    if (model.getFlagFinish().equals("0")) {
                                        if (count <= 7) {
                                            count++;
                                            getUpdateOldCard(Converter.Companion.toHex(messageAPDU));
                                        } else {
                                            count = 1;
                                            if (!reversalSuccess) {
                                                getReversalUpdateCard();
                                            } else {
                                                Toast.makeText(getBaseContext(), "Mohon untuk dapat melakukan update card minimal 3 kali.", Toast.LENGTH_SHORT).show();
                                                getUpdateOldCard(Converter.Companion.toHex(messageAPDU));
                                            }
                                        }
                                    } else {
                                        count = 1;
                                        Toast.makeText(getBaseContext(), "FLAG FINISH SUDAH 1", Toast.LENGTH_SHORT).show();
                                        getConfirmCardBalance();
                                        byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                                                "00B500000A"));
                                        cardBalanceResult.setText("RP. " + CurrencyFormat.format(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));
                                        Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));

                                        dismissProgressDialog();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Timber.d("Kartu Geser......." + e.getMessage());
                                    dismissProgressDialog();
                                    Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                                    cardShifted = true;
//                                    getReversalUpdateCard();
                                    return;
                                }
                            } else if (code.equals("0031")) {
                                Dialog dialog = DefinedDialog.MessageDialog(NFCActivity.this, getString(R.string.remark),
                                        model.getErrorMessage(),
                                        (v, isLongClick) -> {
                                            dismissProgressDialog();
                                        });

                                dialog.show();
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
                                getReversalUpdateCard();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();
                            Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                            cardShifted = true;
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void getUpdateNewCard() {
        try {
            showProgressDialog();

            extraSignature = numberCard;


            //GET DATA FOR MESSAGE
            String getData = getData(session, institutionRef, sourceOfAccount, pendingAmount, merchantData);

            Timber.d("getData: " + getData);
            try {
                byte[] getDataByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(getData));

                byte[] certificateByte = isoDep.transceive(Converter.Companion.hexStringToByteArray("00E0000000"));

                String getDataWith9000 = Converter.Companion.toHex(getDataByte);
                String getCertiWith9000 = Converter.Companion.toHex(certificateByte);

                String getDataTransceive = getDataWith9000.substring(0, getDataWith9000.length() - 4);
                Log.d("GET_DATA_9000 : ", getDataWith9000);
                Log.d("GET_DATA : ", getDataTransceive);

                String getCerti = getCertiWith9000.substring(0, getCertiWith9000.length() - 4);
                Log.d("GET_CERTI_9000 : ", getCertiWith9000);
                Log.d("CARD_CERTIFICATE : ", getCerti);

                cardMessage = getDataTransceive + getCerti; // 149byte getData + 248byte getCertificate (without 9000)

            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("Kartu Geser....... GET DATA MESSAGE NEW APPLET" + e.getMessage());
                dismissProgressDialog();
                Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.UPDATE_CARD_BALANCE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.ORDER_ID, numberCard);
//            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.SESSION, session);
            params.put(WebParams.MESSAGE, cardMessage);


            Timber.wtf("isi params UpdateNewCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.UPDATE_CARD_BALANCE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response UpdateCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                Toast.makeText(getBaseContext(), "UPDATE CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
//                                getConfirmCardBalance();
                                Timber.d("LOGING NEW MESSAGE");

                                if (model.getFlagFinish().equals("0")) {
                                    if (!reversalSuccess) {
                                        getReversalUpdateCard();
                                    } else {
//                                        getUpdateNewCard(model.getMessage());
                                    }
                                } else {
                                    try {

                                        String tagMessage = String.valueOf(model.getMessage());
                                        byte[] msgByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(tagMessage));
                                        String msg = Converter.Companion.toHex(msgByte);
                                        Log.d("Written to msg : ", msg);

                                        getConfirmCardBalance();
//                                        Log.d("CARD_MESAE : ", cardMessage);
//                                        byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
//                                                "00B500000A"));
//                                        cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8)));
//                                        Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));

//                                        dismissProgressDialog();                                        /// flow confirm ();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        cardShifted = true;
                                        Timber.d("Kartu Geser....... WRITE TO NEW APPLET" + e.getMessage());
                                        dismissProgressDialog();
                                        Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
                                getReversalUpdateCard();

//                                dismissProgressDialog();
//                                getFragmentManager().popBackStack();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();
                        }

                        @Override
                        public void onComplete() {
//                            dismissProgressDialog();

                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void getConfirmCardBalance() {
        try {
            showProgressDialog();

            extraSignature = numberCard;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.CONFIRM_CARD_BALANCE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.ORDER_ID, numberCard);
//            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.SESSION, session);
            params.put(WebParams.MESSAGE, cardInfo);

            Timber.d("isi params Confirm Update Card:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.CONFIRM_CARD_BALANCE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response ConfirmCardBalance:" + model.toString());

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Toast.makeText(getBaseContext(), "CONFIRM CARD BERHASIL", Toast.LENGTH_SHORT).show();
                                try {
                                    byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                                            "00B500000A"));
                                    cardBalanceResult.setText("RP. " + CurrencyFormat.format(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));
                                    Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));
                                } catch (IOException e) {
                                    Timber.d("Kartu Geser....... CEK SALDO BARU" + e.getMessage());
                                    Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();
                        }

                        @Override
                        public void onComplete() {
                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void getReversalUpdateCard() {

        try {
            showProgressDialog();

            extraSignature = numberCard;
            String messageForNewApplet = "";
            String messageReversal = "";

            try {
                byte[] reversalMsg = isoDep.transceive(Converter.Companion.hexStringToByteArray("00E70000"));
                messageReversal = Converter.Companion.toHex(reversalMsg);
                Log.d("NFCACTIVITY", "MESSAGE FOR REVERSE : " + messageReversal);

                byte[] reversalCertificate = isoDep.transceive(Converter.Companion.hexStringToByteArray("00E0000000"));
                String certiReversal = Converter.Companion.toHex(reversalCertificate);
                Log.d("NFCACTIVITY", "CERTIFICATE FOR REVERSE : " + certiReversal);

                messageForNewApplet = messageReversal.substring(0, messageReversal.length() - 4) + certiReversal.substring(0, certiReversal.length() - 4);
            } catch (IOException e) {
                Timber.d("Kartu Geser....... REVERSAL UPDATE" + e.getMessage());
                dismissProgressDialog();
                Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                cardShifted = true;
                return;
            }


            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.REVERSE_UPDATE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.CARD_NO, numberCard);
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.MITRA_CODE, mitraCode);
            params.put(WebParams.MERCHANT_TYPE, merchantType);
            if (appletType.equals(TYPE_NEW_APPLET)) {
                params.put(WebParams.MESSAGE, messageForNewApplet);
            } else {
                params.put(WebParams.MESSAGE, messageReversal);
            }

            Timber.wtf("isi params ReverseCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.REVERSE_UPDATE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.wtf("isi response ReversalCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                Toast.makeText(getBaseContext(), "REVERSAL CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
                                reverse_card_key = model.getReverseCardKey();

                                if (model.getFlagFinish().equals("0")) {
                                    try {
                                        byte[] msgFromReversal = isoDep.transceive(Converter.Companion.hexStringToByteArray(model.getMessage()));
                                        Log.d("NFCACTIVITY", "MESSAGE REPONSE TRANSCEIVE : " + Converter.Companion.toHex(msgFromReversal));
                                        getReversalUpdateRepeat(Converter.Companion.toHex(msgFromReversal));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        dismissProgressDialog();
                                        Timber.d("Kartu Geser....... MESSAGE REPONSE TRANSCEIVE" + e.getMessage());
                                        Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kemabali !", Toast.LENGTH_SHORT).show();
                                        cardShifted = true;
                                    }
                                } else {
                                    reversalSuccess = true;
                                    cardShifted = false;
                                    testReversal = true;

                                    getCheckCardBalance2();
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
                                cardShifted = true;
                                dismissProgressDialog();
                                getFragmentManager().popBackStack();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();
//                            getReversalUpdateCard();
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
            dismissProgressDialog();
        }
    }

    private void getReversalUpdateRepeat(String msgReversal) {
        try {
            showProgressDialog();

            extraSignature = numberCard;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.REVERSE_UPDATE_REPEAT, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.CARD_NO, numberCard);
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.MITRA_CODE, mitraCode);
            params.put(WebParams.MERCHANT_TYPE, merchantType);
            params.put(WebParams.MESSAGE, msgReversal);
            params.put(WebParams.REVERSE_CARD_KEY, reverse_card_key);


            Timber.wtf("isi params ReverseCardBalanceRepeat:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.REVERSE_UPDATE_REPEAT, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.wtf("isi response ReversalCardBalanceRepeat :" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
//                                Toast.makeText(getBaseContext(), "REVERSAL CARD REPEAT BERHASIL", Toast.LENGTH_SHORT).show();
                                Timber.d("Message dari ReversCardRepeat : " + model.getMessage());

                                if (model.getFlagFinish().equals("0")) {
                                    try {
                                        byte[] msgFromReversal = isoDep.transceive(Converter.Companion.hexStringToByteArray(model.getMessage()));
                                        Timber.d("Transcive Message in ReverseRepeat : " + msgFromReversal);
                                        getReversalUpdateRepeat(Converter.Companion.toHex(msgFromReversal));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Timber.d("Kartu Geser......." + e.getMessage());
                                        dismissProgressDialog();
                                        Toast.makeText(getBaseContext(), "Tempelkan Kartu Anda Kembali !", Toast.LENGTH_SHORT).show();
                                        cardShifted = true;
                                    }
                                } else {
                                    Toast.makeText(getBaseContext(), "REVERSAL CARD REPEAT BERHASIL", Toast.LENGTH_SHORT).show();

                                    reversalSuccess = true;
                                    cardShifted = false;
                                    testReversal = true;

                                    getCheckCardBalance2();

                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
                                cardShifted = true;
                                dismissProgressDialog();
                                getFragmentManager().popBackStack();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            getFragmentManager().popBackStack();
                        }

                        @Override
                        public void onComplete() {
//                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

}
