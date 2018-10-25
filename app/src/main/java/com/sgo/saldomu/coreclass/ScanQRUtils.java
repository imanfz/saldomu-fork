package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import timber.log.Timber;

public class ScanQRUtils  {

    private Context mContext;

    private static ScanQRUtils instance = null;

    private ScanQRUtils(Context mContext){

        this.mContext = mContext;
    }

    public static ScanQRUtils getInstance(Context mContext){

        if(instance == null){
            instance = new ScanQRUtils(mContext);
        }
        return instance;
    }

    public static String SCAN_QR_SEPARATOR = "%";
    public static String EQUALS_SEPARATOR = "=";


    public Bitmap generateQRCode(String codeQr, String sourceAcct, String sourceName){


        Bitmap bitmap = null;
        String value = encryptedValue(codeQr,sourceAcct,sourceName);
        Timber.d("isi qr value:"+value);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(value, BarcodeFormat.QR_CODE,600,600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            bitmap  = barcodeEncoder.createBitmap(bitMatrix);


        } catch (WriterException e) {
            e.printStackTrace();
        }
        return  bitmap;
    }
    
    private String encryptedValue(String qrTypeValue, String sourceAcctValue, String sourceNameValue){

        String value = "";
        if(qrTypeValue.equalsIgnoreCase(DefineValue.QR_TYPE_ONLY_SOURCE)){
            value =
                    DefineValue.QR_TYPE + EQUALS_SEPARATOR + qrTypeValue + SCAN_QR_SEPARATOR +
                    DefineValue.SOURCE_ACCT + EQUALS_SEPARATOR + sourceAcctValue + SCAN_QR_SEPARATOR +
                    DefineValue.SOURCE_ACCT_NAME + EQUALS_SEPARATOR + sourceNameValue;
        }
        return value;
    }




}
