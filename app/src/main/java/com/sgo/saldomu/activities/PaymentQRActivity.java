package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.BaseActivity;
import com.sgo.saldomu.coreclass.DefineValue;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;

import static com.sgo.saldomu.fragments.FragGenerateQR.generateMySQLAESKey;

/**
 * Created by thinkpad on 11/1/2016.
 */

public class PaymentQRActivity extends BaseActivity implements QRCodeView.Delegate {

    private static final String TAG = PaymentQRActivity.class.getSimpleName();
    public static final int RESULT_PAYFRIEND_QR = 0x1212;
    private QRCodeView mQRCodeView;

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.toolbar_title_payfriendbyqr));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_payment_qr;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();

        mQRCodeView = (ZBarView) findViewById(R.id.zbarview);
        mQRCodeView.setDelegate(this);

        mQRCodeView.startSpot();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeView.setDelegate(this);
        mQRCodeView.startSpot();
        mQRCodeView.startCamera();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);

        Cipher decryptCipher;
        String keyPassEncrypt = "AskAndTrans";
        String _result;
        String[] resultArray = null;

        try {
            decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, generateMySQLAESKey(keyPassEncrypt, "UTF-8"));
            _result = new String(decryptCipher.doFinal(Hex.decodeHex(result.toCharArray())));
            resultArray = _result.split(";");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        try{
            String amount = resultArray[0]; //amount
            String message = resultArray[1]; // message
            Boolean recipientValidation = true;
            ArrayList<TempObjectData> mTempObjectDataList = new ArrayList<>();

            String finalName;

            List<String> listName = new ArrayList<>();
            finalName = resultArray[3];
            listName.add(finalName);
            mTempObjectDataList.add(new TempObjectData(resultArray[2], DefineValue.IDR, amount, finalName));

            if (recipientValidation) {
                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                final Gson gson = gsonBuilder.create();
                String testJson = gson.toJson(mTempObjectDataList);
                String nameJson = gson.toJson(listName);

                Intent data = new Intent();
                data.putExtra(DefineValue.RECIPIENTS, testJson);
                data.putExtra(DefineValue.NAME_RECIPIENT, nameJson);
                data.putExtra(DefineValue.AMOUNT, amount);
                data.putExtra(DefineValue.MESSAGE, message);
                setResult(RESULT_PAYFRIEND_QR, data);
                finish();
            }
        }
        catch (Exception ex) {
            Toast.makeText(this, getResources().getString(R.string.alert_scan_qr_code), Toast.LENGTH_SHORT).show();
        }
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "Camera Error");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class TempObjectData{
        private String member_code_to;
        private String ccy_id;
        private String amount;
        private String name;

        public TempObjectData(String _member_code_to, String _ccy_id, String _amount, String _name){
            this.setMember_code_to(_member_code_to);
            this.setCcy_id(_ccy_id);
            this.setAmount(_amount);
            this.setName(_name);
        }

        public String getMember_code_to() {
            return member_code_to;
        }

        public void setMember_code_to(String member_code_to) {
            this.member_code_to = member_code_to;
        }

        public String getCcy_id() {
            return ccy_id;
        }

        public void setCcy_id(String ccy_id) {
            this.ccy_id = ccy_id;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
