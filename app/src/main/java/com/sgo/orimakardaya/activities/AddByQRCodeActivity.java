package com.sgo.orimakardaya.activities;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.friendModel;
import com.sgo.orimakardaya.Beans.myFriendModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.coreclass.BaseActivity;
import com.sgo.orimakardaya.coreclass.Contents;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DateTimeFormat;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.QRCodeEncoder;
import com.sgo.orimakardaya.coreclass.WebParams;
import com.sgo.orimakardaya.dialogs.AlertDialogLogout;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/*
  Created by thinkpad on 4/24/2015.
 */
public class AddByQRCodeActivity extends BaseActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private SecurePreferences sp;
    private int RESULT;

    private QRCodeReaderView mydecoderview;
    private ImageView imageBarcode;
    private AlertDialog dialogContact;
    private String _ownerID;
    private String custName;
    private String custPhone;
    private String custEmail;
    private String accessKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitializeToolbar();
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        custName = sp.getString(DefineValue.CUST_NAME,"");
        custPhone = sp.getString(DefineValue.USERID_PHONE,"");
        custEmail = sp.getString(DefineValue.PROFILE_EMAIL,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x/2;
        int height = size.y/3;

        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setOnQRCodeReadListener(this);

        imageBarcode = (ImageView) findViewById(R.id.image_barcode);
        TextView tvIDBarcode = (TextView) findViewById(R.id.idBarcode);

        FrameLayout flScanner = (FrameLayout) findViewById(R.id.llQRCodeScanner);
        mydecoderview.getLayoutParams().width = width;
        flScanner.getLayoutParams().height = height;

        imageBarcode.getLayoutParams().width = width;
        imageBarcode.getLayoutParams().height = height;

        tvIDBarcode.setText("ID : " + _ownerID);

        generateQRCode();

        RESULT = MainPage.RESULT_NORMAL;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_addbyqrcode;
    }


    private void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.title_add_friends));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertContact(List<friendModel> mfriendModel){
        try{

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_INSERT,
                    _ownerID,accessKey);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.registerTypeAdapter(friendModel.class, new friendAdapter()).create();
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.CONTACTS, gson.toJson(mfriendModel));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.i("request "+params.toString());

            JsonHttpResponseHandler mHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);

                        Timber.i("code "+code);
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            String arrayFriend = response.getString(WebParams.DATA_CONTACT);
                            String arrayMyFriend = response.getString(WebParams.DATA_FRIEND);
                            insertFriendToDB(new JSONArray(arrayFriend), new JSONArray(arrayMyFriend));

                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout "+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginActivity(AddByQRCodeActivity.this,message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getApplicationContext(),code,Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    Timber.d("onProgress insert contact:"+bytesWritten+" / "+totalSize);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }


                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(AddByQRCodeActivity.this, getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(AddByQRCodeActivity.this, throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi User contact insert "+throwable.toString());
                }

            };
            MyApiClient.sentInsertContact(this, params, mHandler);
        }catch (Exception e){
            Timber.d("httpclient"+e.getMessage());
        }
    }

    private void insertFriendToDB(JSONArray arrayFriend, JSONArray arrayMyfriend){
        ActiveAndroid.initialize(getApplicationContext());
        ActiveAndroid.beginTransaction();
        friendModel mFm;
        myFriendModel mMfm;
        String bucket;
        try {
            friendModel.deleteAll();
            myFriendModel.deleteAll();

            Timber.d("arrayfriend lenght "+String.valueOf(arrayFriend.length()));
            if(arrayFriend.length()>0){
                for (int i = 0; i < arrayFriend.length(); i++) {
                    mFm = new friendModel();
                    mFm.setContact_id(arrayFriend.getJSONObject(i).getInt(friendModel.CONTACT_ID));
                    mFm.setFull_name(arrayFriend.getJSONObject(i).getString(friendModel.FULL_NAME));
                    mFm.setMobile_number(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER));
                    mFm.setMobile_number2(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER2));
                    mFm.setMobile_number3(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER3));
                    mFm.setEmail(arrayFriend.getJSONObject(i).getString(friendModel.EMAIL));
                    mFm.setOwner_id(arrayFriend.getJSONObject(i).getString(friendModel.OWNER_ID));

                    bucket = arrayFriend.getJSONObject(i).getString(friendModel.IS_FRIEND);
                    if(!bucket.equals(""))mFm.setIs_friend(Integer.parseInt(bucket));

                    mFm.setCreated_date(DateTimeFormat.convertStringtoCustomDate(arrayFriend.getJSONObject(i).getString(friendModel.CREATED_DATE)));
                    mFm.save();
                    Timber.d("idx array friend", String.valueOf(i));
                }
            }

            Timber.d("arrayMyfriend lenght "+String.valueOf(arrayMyfriend.length()));
            if(arrayMyfriend.length()>0){
                for (int i = 0; i < arrayMyfriend.length(); i++) {
                    mMfm = new myFriendModel();
                    mMfm.setContact_id(arrayMyfriend.getJSONObject(i).getInt(myFriendModel.CONTACT_ID));
                    mMfm.setFull_name(arrayMyfriend.getJSONObject(i).getString(myFriendModel.FULL_NAME));
                    mMfm.setFriend_number(arrayMyfriend.getJSONObject(i).getString(myFriendModel.FRIEND_NUMBER));
                    mMfm.setEmail(arrayMyfriend.getJSONObject(i).getString(myFriendModel.EMAIL));
                    mMfm.setUser_id(arrayMyfriend.getJSONObject(i).getString(myFriendModel.USER_ID));
                    mMfm.setImg_url(arrayMyfriend.getJSONObject(i).getString(myFriendModel.IMG_URL));
                    mMfm.save();
                    Timber.d("idx array my friend:"+String.valueOf((int) ((i + 1) * (25.0 / (double) arrayMyfriend.length())) + 75));
                }
            }

            ActiveAndroid.setTransactionSuccessful();

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            ActiveAndroid.endTransaction();
            sp.edit().putString(DefineValue.CONTACT_FIRST_TIME, DefineValue.NO).apply();
        }

    }

    private class friendAdapter implements JsonSerializer<friendModel> {

        @Override
        public JsonElement serialize(friendModel _friendModel, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(friendModel.FULL_NAME, _friendModel.getFull_name());
            jsonObject.addProperty(friendModel.MOBILE_NUMBER, _friendModel.getMobile_number());
            jsonObject.addProperty(friendModel.MOBILE_NUMBER2, _friendModel.getMobile_number2());
            jsonObject.addProperty(friendModel.MOBILE_NUMBER3, _friendModel.getMobile_number3());
            jsonObject.addProperty(friendModel.EMAIL, _friendModel.getEmail());
            jsonObject.addProperty(friendModel.OWNER_ID, _friendModel.getOwner_id());
            return jsonObject;
        }
    }

    private void generateQRCode() {
        String qrInputText = "Name : " + custName + "\n" +
                "No HP : " + custPhone + "\n" +
                "Email : " + custEmail;

        //Find screen size
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        Bundle bundle = new Bundle();
        bundle.putString(Contacts.Intents.Insert.NAME, custName);
        bundle.putString(Contacts.Intents.Insert.PHONE, custPhone);
        bundle.putString(Contacts.Intents.Insert.EMAIL, custEmail);

        //Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                bundle,
                Contents.Type.CONTACT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            imageBarcode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(final String text, PointF[] points) {
        Timber.d("Result "+text);

        if( dialogContact != null && dialogContact.isShowing() ) return;

        if(text.contains("MECARD:")) {

            String[] separatedComa = text.split(";");
            String[] first = separatedComa[0].split(":");
            final String name = first[2];

            String[] second = separatedComa[1].split(":");
            final String phone = second[1];

            String[] third = separatedComa[2].split(":");
            final String email = third[1];

            String qrText = "Name : " + name + "\n" + "Phone : " + phone + "\n" + "Email : " + email;

            final AlertDialog.Builder builderDialog = new AlertDialog.Builder(this)
                    .setTitle("Add to Contacts")
                    .setMessage(qrText)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //masukin kontak ke hp
                            addContactToPhone(name, phone, email);
                            //kirim kontak ke server
                            List<friendModel> mfriendModel = new ArrayList<>();
                            mfriendModel.add(new friendModel(name, phone, "", "", email, _ownerID));
                            insertContact(mfriendModel);

                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);

            dialogContact = builderDialog.create();
            dialogContact.show();
        }
        else {
            final AlertDialog.Builder builderDialog = new AlertDialog.Builder(this)
                    .setTitle("Alert")
                    .setMessage("QR Code Invalid")
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);

            dialogContact = builderDialog.create();
            dialogContact.show();
        }
    }


    // Called when your device have no camera
    @Override
    public void cameraNotFound() {

    }

    // Called when there's no QR codes in the camera preview image
    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mydecoderview.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mydecoderview.getCameraManager().stopPreview();
    }

    private void addContactToPhone(String names,
                                   String phoneNumbers,
                                   String emails) {


        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        if (names != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            names).build());
        }

        //------------------------------------------------------ Mobile Number
        if (phoneNumbers != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumbers)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //------------------------------------------------------ Email
        if (emails != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emails)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        // Asking the Contact provider to create a new contact
        try {
            this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

