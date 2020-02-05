package com.sgo.saldomu.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.fragments.PopUpNFC;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.models.retrofit.CheckCardBalanceModel;
import com.sgo.saldomu.models.retrofit.UpdateCardModel;
import com.sgo.saldomu.utils.Converter;
import com.sgo.saldomu.utils.NFCManager;
import com.sgo.saldomu.widgets.BaseActivity;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import timber.log.Timber;

import static org.apache.commons.io.Charsets.UTF_16BE;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class NFCActivity extends BaseActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter;
    private NFCManager nfcMger;
    private NdefMessage mMessage = null;
    Tag currentTag;

    private String cardSelect;
    private String cardAttribute;
    private String cardInfo;
    private String cardUid;
    private String cardBalance;
    private String saldo;
    private String numberCard;

    private String appletType;
    private String updateCardKey;
    private String session;
    private String message;
    private String pendingAmount;
    private String institutionReff;
    private String sourceOfAccount;
    private String merchantData;

    private String DateTime;
    private String pendingAmountPlus;
    private String CounterCard;
    private String PinEmoney;
    private IsoDep isoDep;

    Boolean updateFlag = false;

    private TextView cardNumber, cardBalanceResult;
    private RelativeLayout lyt_gifNfc;
    private LinearLayout lyt_emonCard;

    public final static String TYPE_OLD_APPLET = "0";
    public final static String TYPE_NEW_APPLET = "1";
    private String cardMessage = "";


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_nfc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        nfcMger = new NFCManager(this);

        cardNumber = findViewById(R.id.cardNumber);
        cardBalanceResult = findViewById(R.id.cardBalanceResult);
        lyt_gifNfc = findViewById(R.id.lyt_gifNfc);
        lyt_emonCard = findViewById(R.id.lyt_emonCard);
    }

    private void InitializeToolbar() {
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

            byte[] getDataNewApplet = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00E50000462207191611130000000000000000000000000000C34DE2F5C542FA570000000000000000000000000000000000000007A40B0000000000000000000000000000000000000000"));

            byte[] getCertificate = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00E0000000"));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("SELECT_RESPONSE : ", Converter.Companion.toHex(selectEmoneyResponse));
                    cardSelect = Converter.Companion.toHex(selectEmoneyResponse);

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
                    cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
                    Log.d("SALDO : ", String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8))));
                    saldo = String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));

                    Log.d("getDataNewApplet : ", Converter.Companion.toHex(getDataNewApplet));

                    Log.d("getCertificate : ", Converter.Companion.toHex(getCertificate));


                    Log.d("ISI PENDING AMOUNT1 : ", String.valueOf(Converter.Companion.littleEndianToBigEndian(70000))/*(Converter.Companion.toLittleEndian("70000"))*/);

                    Log.d("ISI PENDING AMOUNT2 : ", String.valueOf(Converter.Companion.littleEndianToBigEndian2(70000))/*(Converter.Companion.toLittleEndian("70000"))*/);

                    Log.d("ISI PENDING AMOUNT3 : ", Converter.Companion.hexToLittleEndianHexString("70000"));/*(Converter.Companion.toLittleEndian("70000"))*/
//
                    byte[] PENDAMOUNT = Converter.Companion.intToLittleEndian1("70000");
                    Log.d("ISI PENDING AMOUNT4 : ", (Converter.Companion.toHex(PENDAMOUNT)));/*(Converter.Companion.toLittleEndian("70000"))*/
                    /*(Converter.Companion.toLittleEndian("70000"))*/


//                    Log.d("ISI DATA : ", Converter.Companion.toHex(getDataNewApplet1));/*(Converter.Companion.toLittleEndian("70000"))*/

                    lyt_gifNfc.setVisibility(View.GONE);
                    lyt_emonCard.setVisibility(View.VISIBLE);


                    getCheckCardBalance2();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
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


//        Integer dataByte = String.valueOf(Converter.Companion.intToByteArray(Integer.parseInt(data))).length() / 2;

//        String LC = getLC(dataByte);
//        int LC = Converter.Companion.toHex(dataByte).length() / 2;
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
            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
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
                                Toast.makeText(getBaseContext(), "CHEK CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
                                session = model.getSession();
                                updateCardKey = model.getUpdateCardKey();
                                appletType = model.getAppletType();


                                if (!model.getPendingAmount().equals("0") && appletType.equals(TYPE_NEW_APPLET)) { // new applet

                                    String getData = getData(model.getSession(), model.getInstitutionReff(), model.getSourceOfAccount(),
                                            model.getPendingAmount(), model.getMerchantData());
//                                    String getData = "00E50000462207191611130000000000000000000000000000C34DE2F5C542FA570000000000000000000000000000000000000007A40B0000000000000000000000000000000000000000";
                                    Timber.d("getData: " + getData);

                                    try {
//
                                        byte[] getDataByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(getData));
                                        String getDataWith9000 = Converter.Companion.toHex(getDataByte);
                                        String getDataString = getDataWith9000.substring(0, getDataWith9000.length() - 4);
                                        Log.d("GET_DATA : ", getDataWith9000);
                                        Log.d("CARD_MESSAGE : ", getDataString);

                                        String tempCert = "00E0000000";
                                        byte[] certificateByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(tempCert));
//                                        String certificate = Converter.Companion.toHex(certificateByte);

                                        String crtWith9000 = Converter.Companion.toHex(certificateByte);
                                        String getCrt = crtWith9000.substring(0, crtWith9000.length() - 4);
                                        Log.d("GET_CERTI : ", crtWith9000);
                                        Log.d("CARD_CERTIFICATE : ", getCrt);

                                        cardMessage = getDataString + getCrt; // 149byte getData + 248byte getCertificate (without 9000)

//
//
                                    } catch (IOException e) {
                                        e.printStackTrace();

                                        return;
                                    }
                                }

                                if (appletType.equals(TYPE_OLD_APPLET)) {
                                    getUpdateOldCard(cardInfo);
                                } else {
                                    getUpdateNewCard(cardMessage);
                                }


                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
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

                            dismissProgressDialog();
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
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
            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.SESSION, session);
            params.put(WebParams.MESSAGE, msg);

            Timber.d("isi params UpdateCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.UPDATE_CARD_BALANCE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response UpdateCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Toast.makeText(getBaseContext(), "UPDATE CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();

                                try {
                                    byte[] messageAPDU = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                                            model.getMessage()));

                                    Log.d("MESSAGE TO APDU : ", Converter.Companion.toHex(messageAPDU));
                                    if (model.getFlagFinish().equals("0")) {
                                        byte[] reversalMsg = isoDep.transceive(Converter.Companion.hexStringToByteArray("00E70000"));
                                        String messageReversal = Converter.Companion.toHex(reversalMsg);
                                        Log.d("NFCACTIVITY", "MESSAGE FOR REVERSE : " + messageReversal);
                                        getReversalUpdateCard(messageReversal, model.getMitraCode(), model.getMerchantType());
                                    } else {
                                        Toast.makeText(getBaseContext(), "FLAG FINISH SUDAH 1", Toast.LENGTH_SHORT).show();
                                        getConfirmCardBalance();
                                        byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                                                "00B500000A"));
                                        cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8)));
                                        Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));

                                        dismissProgressDialog();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();
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
                        }
                    });
        } catch (Exception e) {
            Timber.d("httpclient:" + e.getMessage());
        }
    }

    private void getUpdateNewCard(String card) {
        try {
            showProgressDialog();

            extraSignature = numberCard;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.UPDATE_CARD_BALANCE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.ORDER_ID, numberCard);
            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
            params.put(WebParams.CARD_BALANCE, saldo);
            params.put(WebParams.CARD_ATTRIBUTE, cardAttribute);
            params.put(WebParams.CARD_INFO, cardInfo);
            params.put(WebParams.CARD_UUID, cardUid);
            params.put(WebParams.UPDATE_CARD_KEY, updateCardKey);
            params.put(WebParams.SESSION, session);
            params.put(WebParams.MESSAGE, card);


            Timber.d("isi params UpdateCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.UPDATE_CARD_BALANCE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response UpdateCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Toast.makeText(getBaseContext(), "UPDATE CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
//                                getConfirmCardBalance();
                                Timber.d("LOGING NEW MESSAGE");

                                if (model.getFlagFinish().equals("0")) {
                                    getUpdateNewCard(model.getMessage().toString());
                                    updateFlag = true;
                                } else {
                                    try {

                                        String tagMessage = String.valueOf(model.getMessage());
                                        byte[] msgByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(tagMessage));
                                        String msg = Converter.Companion.toHex(msgByte);
                                        Log.d("Written to msg : ", msg);


                                        getConfirmCardBalance();
//                                        Log.d("CARD_MESAE : ", cardMessage);
                                        byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                                                "00B500000A"));
                                        cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8)));
                                        Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));

                                        dismissProgressDialog();                                        /// flow confirm ();
                                    } catch (IOException e) {
                                        e.printStackTrace();

                                        return;
                                    }
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();

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

    private void getConfirmCardBalance() {
        try {
            showProgressDialog();

            extraSignature = numberCard;

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.CONFIRM_CARD_BALANCE, extraSignature);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.ORDER_ID, numberCard);
            params.put(WebParams.TX_ID, "BIL15663768983V9LL");
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

    private void getReversalUpdateCard(String msgReversal, String mitraCode, String merchantType) {

        try {
            showProgressDialog();

            extraSignature = numberCard;

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
            params.put(WebParams.MESSAGE, msgReversal);


            Timber.d("isi params ReverseCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.REVERSE_UPDATE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response ReversalCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Toast.makeText(getBaseContext(), "REVERSAL CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
////                                getConfirmCardBalance();
//                                Timber.d("LOGING NEW MESSAGE");
//
//                                if (model.getFlagFinish().equals("0")) {
//                                    getUpdateNewCard(model.getMessage().toString());
//                                    updateFlag = true;
//                                } else {
//                                    try {
//
//                                        String tagMessage = String.valueOf(model.getMessage());
//                                        byte[] msgByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(tagMessage));
//                                        String msg = Converter.Companion.toHex(msgByte);
//                                        Log.d("Written to msg : ", msg);
//
//
//                                        getConfirmCardBalance();
////                                        Log.d("CARD_MESAE : ", cardMessage);
//                                        byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
//                                                "00B500000A"));
//                                        cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8)));
//                                        Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));
//
//                                        dismissProgressDialog();                                        /// flow confirm ();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//
//                                        return;
//                                    }
//                                }
                                if (model.getFlagFinish().equals("0")) {
                                    try {
                                        byte[] msgFromReversal = isoDep.transceive(Converter.Companion.hexStringToByteArray(model.getMessage()));
                                        getReversalUpdateCard(Converter.Companion.toHex(msgFromReversal), model.getMitraCode(), model.getMerchantType());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Toast.makeText(getBaseContext(), "FLAG REVERSAL = 1", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();

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

    private void getReversalUpdateRepeat(String msgReversal, String mitraCode, String merchantType) {

        try {
            showProgressDialog();

            extraSignature = numberCard;

            byte[] reversalMsg = isoDep.transceive(Converter.Companion.hexStringToByteArray("00E70000"));
            String messageReversal = Converter.Companion.toHex(reversalMsg);

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
            params.put(WebParams.MESSAGE, msgReversal);


            Timber.d("isi params ReverseCardBalance:" + params.toString());

            RetrofitService.getInstance().PostObjectRequest(MyApiClient.REVERSE_UPDATE, params,
                    new ResponseListener() {
                        @Override
                        public void onResponses(JsonObject object) {
                            UpdateCardModel model = getGson().fromJson(object, UpdateCardModel.class);
                            Timber.d("isi response ReversalCardBalance:" + model);

                            String code = model.getErrorCode();
                            if (code.equals(WebParams.SUCCESS_CODE)) {
                                Toast.makeText(getBaseContext(), "REVERSAL CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();
////                                getConfirmCardBalance();
//                                Timber.d("LOGING NEW MESSAGE");
//
//                                if (model.getFlagFinish().equals("0")) {
//                                    getUpdateNewCard(model.getMessage().toString());
//                                    updateFlag = true;
//                                } else {
//                                    try {
//
//                                        String tagMessage = String.valueOf(model.getMessage());
//                                        byte[] msgByte = isoDep.transceive(Converter.Companion.hexStringToByteArray(tagMessage));
//                                        String msg = Converter.Companion.toHex(msgByte);
//                                        Log.d("Written to msg : ", msg);
//
//
//                                        getConfirmCardBalance();
////                                        Log.d("CARD_MESAE : ", cardMessage);
//                                        byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
//                                                "00B500000A"));
//                                        cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8)));
//                                        Log.d("SALDO BARU : ", String.valueOf(Converter.Companion.toLittleEndian(Converter.Companion.toHex(lastBalanceResponse).substring(0, 8))));
//
//                                        dismissProgressDialog();                                        /// flow confirm ();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//
//                                        return;
//                                    }
//                                }
                                if (model.getFlagFinish().equals("0")) {
                                    try {
                                        byte[] msgFromReversal = isoDep.transceive(Converter.Companion.hexStringToByteArray(model.getMessage()));
                                        getReversalUpdateCard(Converter.Companion.toHex(msgFromReversal), model.getMitraCode(), model.getMerchantType());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Toast.makeText(getBaseContext(), "FLAG REVERSAL = 1", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                code = model.getErrorCode() + " : " + model.getErrorMessage();
                                Toast.makeText(getBaseContext(), code, Toast.LENGTH_LONG).show();

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
