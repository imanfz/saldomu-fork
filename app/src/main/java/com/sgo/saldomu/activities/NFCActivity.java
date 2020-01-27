package com.sgo.saldomu.activities;

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

    Boolean updateFlag = false;

    private TextView cardNumber, cardBalanceResult;
    private RelativeLayout lyt_gifNfc;
    private LinearLayout lyt_emonCard;



    @Override
    protected int getLayoutResource() {
        return R.layout.activity_nfc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

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
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();

            byte[] selectEmoneyResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00A40400080000000000000001"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("SELECT_RESPONSE : ", Converter.Companion.toHex(selectEmoneyResponse));
                    cardSelect = Converter.Companion.toHex(selectEmoneyResponse);
                }
            });

            byte[] cardAttirbuteResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00F210000B"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CARD_ATTRIBUTE : ", Converter.Companion.toHex(cardAttirbuteResponse));
                    cardAttribute = Converter.Companion.toHex(cardAttirbuteResponse);
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("UUID : ", Converter.Companion.toHex(tag.getId()));
                    cardUid = Converter.Companion.toHex(tag.getId());
                }
            });

            byte[] cardInfoResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00B300003F"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CARD_INFO : ", Converter.Companion.toHex(cardInfoResponse));
                    cardInfo = Converter.Companion.toHex(cardInfoResponse);
                    cardNumber.setText(cardInfo.substring(0, 16));
                    numberCard = cardInfo.substring(0, 16);
                }
            });


            byte[] lastBalanceResponse = isoDep.transceive(Converter.Companion.hexStringToByteArray(
                    "00B500000A"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("LAST_BALANCE : ", Converter.Companion.toHex(lastBalanceResponse));
                    cardBalance = Converter.Companion.toHex(lastBalanceResponse);
                    cardBalanceResult.setText("RP. " + Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
                    Log.d("SALDO : ", String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8))));
                    saldo = String.valueOf(Converter.Companion.toLittleEndian(cardBalance.substring(0, 8)));
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lyt_gifNfc.setVisibility(View.GONE);
                    lyt_emonCard.setVisibility(View.VISIBLE);
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getCheckCardBalance();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCheckCardBalance() {
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
                                getUpdateCardBalance();

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

    private void getUpdateCardBalance() {
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
            params.put(WebParams.MESSAGE, cardInfo);

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
                                getConfirmCardBalance();
                                Timber.d("LOGING NEW MESSAGE");

//                                if(updateFlag == false){
//                                    getUpdateCardBalance(model.getMessage().toString());
//                                    updateFlag = true;
//                                }
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
                                Toast.makeText(getBaseContext(), "CONFIRM CARD BALACE BERHASIL", Toast.LENGTH_SHORT).show();

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
}
