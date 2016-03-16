package com.sgo.orimakardaya.coreclass;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.sgo.orimakardaya.BuildConfig;

import org.apache.commons.codec.binary.Base64;

import java.util.StringTokenizer;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 Created by Administrator on 7/14/2014.
 */
public class MyApiClient {

    private static MyApiClient singleton = null;
    private Context mContext;

    public MyApiClient(){

    }
    public MyApiClient(Context _context){
        this.setmContext(_context);
    }

    public static MyApiClient getInstance( ) {
        return singleton;
    }

    public static void initialize(Context _context) {
        if(singleton == null) {
            singleton = new MyApiClient(_context);
        }
    }
    public static Boolean PROD_FAILURE_FLAG = true;
    public static Boolean IS_PROD = BuildConfig.isProdDomain;
    public static Boolean PROD_FLAG_ADDRESS = BuildConfig.isProdDomain;
    public static Boolean IS_INTERNET_BANKING;
    public static String COMM_ID;
    public static String COMM_ID_PULSA;

//    public static final String headaddressDEV = "http://116.90.162.173:18080/akardaya/";
//    public static final String headaddressPROD = "https://mobile.goworld.asia/akardaya2/";
    public static String headaddressfinal = BuildConfig.HeadAddress;

    //Link webservices Signature

    public static String LINK_REGISTRASI;
    public static String LINK_VALID_REGISTRASI;
    public static String LINK_LOGIN;
    public static String LINK_VALID_TOPUP;
    public static String LINK_LIST_MEMBER;
    public static String LINK_REQ_TOKEN_SGOL;
    public static String LINK_RESEND_TOKEN_SGOL;
    public static String LINK_INSERT_TRANS_TOPUP;
    public static String LINK_SALDO;
    //public static final String LINK_BANK_LIST;
    public static String LINK_BANK_LIST;
    public static String LINK_REQ_TOKEN_REGIST;
    public static String LINK_GET_ALL_BANK;
    public static String LINK_TOPUP_PULSA_RETAIL;
    public static String LINK_UPDATE_PROFILE;
    public static String LINK_CHANGE_PASSWORD;
    public static String LINK_FORGOT_PASSWORD;
    public static String LINK_MEMBER_PULSA;
    public static String LINK_USER_CONTACT_INSERT;
    public static String LINK_USER_CONTACT_UPDATE;

    public static String LINK_PROD_TOPUP_RETAIL;
    public static String LINK_GET_BILLER_TYPE;
    public static String LINK_LIST_BILLER;
    public static String LINK_DENOM_RETAIL;
    public static String LINK_REQ_TOKEN_BILLER;
    public static String LINK_CONFIRM_BILLER;
    public static String LINK_RESENT_TOKEN_BILLER;
    public static String LINK_UPLOAD_PROFILE_PIC;
    public static String LINK_LIST_BANK_BILLER;

    public static String LINK_REQ_TOKEN_P2P;
    public static String LINK_CONFIRM_TRANS_P2P;
    public static String LINK_RESENT_TOKEN_P2P;

    public static String LINK_ASKFORMONEY_SUBMIT;
    public static String LINK_NOTIF_RETRIEVE;
    public static String LINK_NOTIF_READ;

    public static String LINK_REQ_TOKEN_P2P_NOTIF;
    public static String LINK_CONFIRM_TRANS_P2P_NOTIF;

    public static String LINK_GET_TRX_STATUS;

	public static String LINK_GROUP_LIST;
    public static String LINK_ADD_GROUP;
    public static String LINK_TIMELINE_LIST;

    public static String LINK_COMMENT_LIST;
    public static String LINK_ADD_COMMENT;
    public static String LINK_REMOVE_COMMENT;

    public static String LINK_LIKE_LIST;
    public static String LINK_ADD_LIKE;
    public static String LINK_REMOVE_LIKE;

    public static String LINK_CREATE_PIN;
    public static String LINK_CHANGE_PIN;

    public static String LINK_INQUIRY_BILLER;
    public static String LINK_PAYMENT_BILLER;
    public static String LINK_TRANSACTION_REPORT;
    public static String LINK_PROMO_LIST;

    public static String LINK_BANK_ACCOUNT_COLLECTION;
    public static String LINK_TOP_UP_ACCOUNT_COLLECTION;
    public static String LINK_COMM_ACCOUNT_COLLECTION;
    public static String LINK_COMM_ESPAY;

	public static String LINK_APP_VERSION;
    public static String LINK_HELP_LIST;

    public static String LINK_INQUIRY_MOBILE;
    public static String LINK_REQUEST_TOKEN_SB;
    public static String LINK_CONFIRM_TOKEN_SB;

    public static String LINK_INSERT_PASSWORD;
    public static String LINK_REPORT_ESPAY;

    public static String LINK_INQUIRY_MOBILE_JATIM;
    public static String LINK_CONFIRM_TOKEN_JATIM;
    public static String LINK_LIST_BANK_SMS_REGIST;

    public static String LINK_DENOM_DAP;
    public static String LINK_BANK_DAP;
    public static String LINK_PAYMENT_DAP;
	
	public static String LINK_LOGOUT;
    public static String LINK_CREATE_PIN_PASS;
    public static String LINK_REPORT_MONEY_REQUEST;
    public static String LINK_ASK4MONEY_REJECT;
    public static String LINK_HELP_PIN;

    public static void initializeAddress(){
        LINK_REGISTRASI          = headaddressfinal + "RegisterCustomer/Invoke";
        LINK_VALID_REGISTRASI    = headaddressfinal + "InsertCustomer/Invoke";
        LINK_LOGIN               = headaddressfinal + "MemberLogin/SignIn";
        LINK_VALID_TOPUP         = headaddressfinal + "TopUp/Invoke";
        LINK_LIST_MEMBER         = headaddressfinal + "Member/Retrieve";
        LINK_REQ_TOKEN_SGOL      = headaddressfinal + "InquiryTrx/Retrieve";
        LINK_RESEND_TOKEN_SGOL   = headaddressfinal + "InquiryResendToken/Invoke";
        LINK_INSERT_TRANS_TOPUP  = headaddressfinal + "InsertTrx/Invoke";
        LINK_SALDO               = headaddressfinal + "Balance/Retrieve";
        //LINK_BANK_LIST           = headaddressfinal + "BankList/Retrieve";
        LINK_BANK_LIST           = headaddressfinal + "BankMember/Retrieve";
        LINK_REQ_TOKEN_REGIST    = headaddressfinal + "ResendTokenCust/Invoke";
        LINK_GET_ALL_BANK        = headaddressfinal + "ServiceBank/GetAllBank";
        LINK_TOPUP_PULSA_RETAIL  = headaddressfinal + "TopUpPulsa/Invoke";
        LINK_UPDATE_PROFILE      = headaddressfinal + "UserProfile/Update";
        LINK_CHANGE_PASSWORD     = headaddressfinal + "ChangePassword/Invoke";
        LINK_FORGOT_PASSWORD     = headaddressfinal + "ForgotPassword/Invoke";
        LINK_MEMBER_PULSA        = headaddressfinal + "MemberPulsa/Retrieve";
        LINK_USER_CONTACT_INSERT = headaddressfinal + "UserContact/Insert";
        LINK_USER_CONTACT_UPDATE = headaddressfinal + "UserContact/Update";
        LINK_PROD_TOPUP_RETAIL =   headaddressfinal + "TopUpPulsaProd/Invoke";
        LINK_GET_BILLER_TYPE     = headaddressfinal + "ServiceBillerType/getBillerType";
        LINK_LIST_BILLER         = headaddressfinal + "BillerEspay/Retrieve";
        LINK_DENOM_RETAIL        = headaddressfinal + "DenomBiller/Retrieve";
        LINK_REQ_TOKEN_BILLER    = headaddressfinal + "RequestBiller/Invoke";
        LINK_CONFIRM_BILLER      = headaddressfinal + "ConfirmTokenBiller/Invoke";
        LINK_RESENT_TOKEN_BILLER = headaddressfinal + "ResendToken/Invoke";
        LINK_LIST_BANK_BILLER    = headaddressfinal + "BankBiller/Retrieve";

        LINK_UPLOAD_PROFILE_PIC  = headaddressfinal + "UploadProfPic/Submit";
        LINK_REQ_TOKEN_P2P       = headaddressfinal + "TransferP2P/Invoke";
        LINK_CONFIRM_TRANS_P2P   = headaddressfinal + "ConfirmTransfer/Invoke";
        LINK_RESENT_TOKEN_P2P    = headaddressfinal + "ResendTransfer/Invoke";

        LINK_ASKFORMONEY_SUBMIT  = headaddressfinal + "Ask4Money/Submit";
        LINK_NOTIF_RETRIEVE      = headaddressfinal + "UserNotif/Retrieve";
        LINK_NOTIF_READ          = headaddressfinal + "UserNotif/isRead";

        LINK_REQ_TOKEN_P2P_NOTIF = headaddressfinal + "PayFriend/Invoke";
        LINK_CONFIRM_TRANS_P2P_NOTIF = headaddressfinal + "ConfirmPayFriend/Invoke";

        LINK_GET_TRX_STATUS      = headaddressfinal + "TrxStatus/Retrieve";
        LINK_GROUP_LIST          = headaddressfinal + "UserGroup/Retrieve";
        LINK_ADD_GROUP           = headaddressfinal + "UserGroup/Insert";
        LINK_TIMELINE_LIST       = headaddressfinal + "UserPosts/Retrieve";
        LINK_COMMENT_LIST        = headaddressfinal + "UserComments/Retrieve";
        LINK_ADD_COMMENT         = headaddressfinal + "UserComments/Insert";
        LINK_REMOVE_COMMENT      = headaddressfinal + "UserComments/Remove";
        LINK_LIKE_LIST           = headaddressfinal + "UserLikes/Retrieve";
        LINK_ADD_LIKE            = headaddressfinal + "UserLikes/Insert";
        LINK_REMOVE_LIKE         = headaddressfinal + "UserLikes/Remove";
        LINK_CREATE_PIN          = headaddressfinal + "CreatePIN/Invoke";
        LINK_CHANGE_PIN          = headaddressfinal + "ChangePIN/Invoke";
        LINK_PAYMENT_BILLER      = headaddressfinal + "PaymentBiller/Invoke";
        LINK_TRANSACTION_REPORT  = headaddressfinal +"ReportTrx/Retrieve";

        LINK_PROMO_LIST          = headaddressfinal + "ServicePromo/PromoList";
        LINK_INQUIRY_BILLER      = headaddressfinal + "InquiryBiller/Invoke";

        LINK_BANK_ACCOUNT_COLLECTION      = headaddressfinal + "BankCollect/Retrieve";
        LINK_TOP_UP_ACCOUNT_COLLECTION    = headaddressfinal + "TopUpCollect/Invoke";
        LINK_COMM_ACCOUNT_COLLECTION      = headaddressfinal + "CommAcct/Retrieve";
		LINK_APP_VERSION         = headaddressfinal + "ServiceApp/getAppVersion";
        LINK_HELP_LIST           = headaddressfinal + "ContactCenter/Retrieve";

        LINK_INQUIRY_MOBILE      = headaddressfinal + "InquiryMobile/Invoke";
        LINK_REQUEST_TOKEN_SB    = headaddressfinal + "SendTokenSMS/Invoke";
        LINK_CONFIRM_TOKEN_SB    = headaddressfinal + "ConfirmTokenSMS/Invoke";
		
		LINK_COMM_ESPAY          = headaddressfinal + "CommEspay/Retrieve";
        LINK_INSERT_PASSWORD     = headaddressfinal + "InsertPassword/Invoke";

        LINK_REPORT_ESPAY        = headaddressfinal + "ReportEspay/Retrieve";
        LINK_INQUIRY_MOBILE_JATIM= headaddressfinal + "InquiryMobileJTM/Invoke";
        LINK_CONFIRM_TOKEN_JATIM = headaddressfinal + "ConfirmTokenSMSJTM/Invoke";
        LINK_LIST_BANK_SMS_REGIST= headaddressfinal + "BankRegisSMS/Retrieve";

        LINK_DENOM_DAP          = headaddressfinal + "DenomDAP/Retrieve";
        LINK_BANK_DAP           = headaddressfinal + "BankDAP/Retrieve";
        LINK_PAYMENT_DAP        = headaddressfinal + "PaymentDAP/Invoke";
		
		LINK_LOGOUT             = headaddressfinal + "ServiceLogout/SignOut";
        LINK_CREATE_PIN_PASS    = headaddressfinal + "CreatePinPass/Invoke";
        LINK_REPORT_MONEY_REQUEST = headaddressfinal + "ReportMoneyReq/Retrieve";
        LINK_ASK4MONEY_REJECT   = headaddressfinal + "Ask4Money/Decline";
        LINK_HELP_PIN           = headaddressfinal + "HelpPIN/Retrieve";
    }




    //-----------------------------------------------------------------------------------------------------------------




    public static final int TIMEOUT = 200000; // 200 x 1000 = 3 menit
    public static String FLAG_OTP = "N";
    public static Boolean FLAG_SIG = true;
    public static String COMM_ID_DEV = "EMONEYMAKA1435249840AWAL0"; //dev
    public static String COMM_ID_PULSA_DEV = "COMMUNITYD14418665666HTKR"; //dev pulsa agent
    public static String COMM_ID_PULSA_PROD = "DAPMAKARDA1443547914WO0NU"; //prod pulsa agent

    public static String COMM_ID_PROD = "EMONEYMAKA1429005701H921A";  //prod


    public static String APP_ID = BuildConfig.AppID;
    public static String CCY_VALUE = "IDR";
    public static String DEV_MEMBER_ID_PULSA_RETAIL = "EFENDI1421144347BPFIM";
    public static String PROD_MEMBER_ID_PULSA_RETAIL = "EFENDI1421205049F0018";

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    public AsyncHttpClient syncHttpClient= new SyncHttpClient();

    public static UUID getUUID(){
        return UUID.randomUUID();
    }

    public static String getWebserviceName(String link){
        StringTokenizer tokens = new StringTokenizer(link, "/");
        int index = 0;
        while(index<3) {
            tokens.nextToken();
            index++;
        }
        return tokens.nextToken();
    }
    public static String getSignature(UUID uuidnya, String date, String WebServiceName, String noID, String apinya){
        String msgnya = uuidnya+date+APP_ID+WebServiceName+noID;

        String hash = null;
        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apinya.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hmacData = sha256_HMAC.doFinal(msgnya.getBytes("UTF-8"));

            hash = new String(encodeUrlSafe(hmacData));

        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return hash;
    }

    /*public static RequestParams getSignatureWithParams(String commID, String linknya, Context context){

        String webServiceName = getWebserviceName(linknya);
        SecurePreferences sp = new SecurePreferences(context);
        String user_id = sp.getString(CoreApp.USERID_PHONE,"");
        String access_key = sp.getString(CoreApp.ACCESS_KEY,"");
        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String noID = commID + user_id;

        String msgnya = uuidnya+dtime+APP_ID+webServiceName+noID;

        String hash = null;
        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(access_key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hmacData = sha256_HMAC.doFinal(msgnya.getBytes("UTF-8"));

            hash = new String(encodeUrlSafe(hmacData));

        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        RequestParams params = new RequestParams();
        params.put(WebParams.RC_UUID, uuidnya);
        params.put(WebParams.RC_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);

        return params;
    }*/

    public static RequestParams getSignatureWithParams(String commID, String linknya, String user_id,String access_key){

        String webServiceName = getWebserviceName(linknya);
        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = uuidnya+dtime+APP_ID+webServiceName+ commID + user_id;

//        Timber.d("isisnya signature",  webServiceName +" / "+commID+" / " +user_id);

        String hash = null;
        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(access_key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hmacData = sha256_HMAC.doFinal(msgnya.getBytes("UTF-8"));

            hash = new String(encodeUrlSafe(hmacData));

        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        RequestParams params = new RequestParams();
        params.put(WebParams.RC_UUID, uuidnya);
        params.put(WebParams.RC_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);

        return params;
    }

    public static byte[] encodeUrlSafe(byte[] data) {
        byte[] encode = Base64.encodeBase64(data);
        for (int i = 0; i < encode.length; i++) {
            if (encode[i] == '+') {
                encode[i] = '-';
            } else if (encode[i] == '=') {
                encode[i] = '_';
            } else if (encode[i] == '/') {
                encode[i] = '~';
            }
        }
        return encode;
    }

    public static void setCookieStore(PersistentCookieStore cookieStore) {
        getClient().setCookieStore(cookieStore);
    }

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        if(PROD_FLAG_ADDRESS)getClient().setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        getClient().setURLEncodingEnabled(true);
        getClient().setMaxRetriesAndTimeout(1, 10000);
        getClient().get(getInstance().getmContext(),url, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if(PROD_FLAG_ADDRESS)getClient().setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        getClient().setTimeout(TIMEOUT);

        getClient().setMaxRetriesAndTimeout(1, 10000);
        getClient().post(getInstance().getmContext(),url, params, responseHandler);
    }

    public static AsyncHttpClient getClient()
    {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return getInstance().syncHttpClient;
        return getInstance().asyncHttpClient;
    }

    public static void CancelRequestWS(Context _context,Boolean interruptIfRunning)
    {
        getClient().cancelRequests(_context, interruptIfRunning);
    }
    //----------------------------------------------------------------------------------------------------

    public static void sentDataRegister( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Register Customer:" + LINK_REGISTRASI);
        post(LINK_REGISTRASI, params, responseHandler);
    }

    public static void sentValidRegister( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Insert Customer:"+LINK_VALID_REGISTRASI);
        post(LINK_VALID_REGISTRASI, params, responseHandler);
    }

    public static void sentDataLogin( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Login:" + LINK_LOGIN);
        post(LINK_LOGIN, params, responseHandler);
    }

    public static void sentDataListMember( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Member Retrieve:"+LINK_LIST_MEMBER);
        post(LINK_LIST_MEMBER, params, responseHandler);
    }

    public static void sentDataReqTokenSGOL( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Inquiry Trx:"+LINK_REQ_TOKEN_SGOL);
        post(LINK_REQ_TOKEN_SGOL, params, responseHandler);
    }

    public static void sentResendTokenSGOL( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address InquiryResendToken:"+LINK_RESEND_TOKEN_SGOL);
        post(LINK_RESEND_TOKEN_SGOL, params, responseHandler);
    }

    public static void sentInsertTransTopup( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Insert Trx:"+LINK_INSERT_TRANS_TOPUP);
        post(LINK_INSERT_TRANS_TOPUP, params, responseHandler);
    }

    public static void getSaldo( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Saldo:"+LINK_SALDO);
        post(LINK_SALDO, params, responseHandler);
    }

    public static void getBankList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Bank list:"+LINK_BANK_LIST);
        post(LINK_BANK_LIST, params, responseHandler);
    }

    public static void sentReqTokenRegister( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Resend token Register:"+LINK_REQ_TOKEN_REGIST);
        post(LINK_REQ_TOKEN_REGIST, params, responseHandler);
    }

    public static void sentTopupPulsaRetailValidation( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String linknya = LINK_TOPUP_PULSA_RETAIL ;
        if(IS_INTERNET_BANKING){
            if(IS_PROD)linknya = LINK_PROD_TOPUP_RETAIL;
        }

        Timber.wtf("address TOPUP Pulsa:"+linknya);
        post(linknya, params, responseHandler);
    }

    public static void sentUpdateProfile( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Update Profile:"+LINK_UPDATE_PROFILE);
        post(LINK_UPDATE_PROFILE, params, responseHandler);
    }

    public static void sentValidTopUp( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Validation Topup:"+LINK_VALID_TOPUP);
        post(LINK_VALID_TOPUP, params, responseHandler);
    }

    public static void sentChangePassword( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address CHange Password:"+LINK_CHANGE_PASSWORD);
        post(LINK_CHANGE_PASSWORD, params, responseHandler);
    }

    public static void sentForgotPassword( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Forget Password:"+LINK_FORGOT_PASSWORD);
        post(LINK_FORGOT_PASSWORD, params, responseHandler);
    }

    public static void sentMemberPulsa( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Member Pulsa:"+LINK_MEMBER_PULSA);
        post(LINK_MEMBER_PULSA, params, responseHandler);
    }

    public static void sentInsertContact( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address InquiryResendToken:"+LINK_USER_CONTACT_INSERT);
        post(LINK_USER_CONTACT_INSERT, params, responseHandler);
    }

    public static void sentUpdateContact( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Update Contact:"+LINK_USER_CONTACT_UPDATE);
        post(LINK_USER_CONTACT_UPDATE, params, responseHandler);
    }

    public static void sentListBiller( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address List Biller:"+LINK_LIST_BILLER);
        post(LINK_LIST_BILLER, params, responseHandler);
    }

    public static void sentDenomRetail( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Denom Retail:"+LINK_DENOM_RETAIL);
        post(LINK_DENOM_RETAIL, params, responseHandler);
    }

    public static void sentReqTokenBiller( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Req Token Biller:"+LINK_REQ_TOKEN_BILLER);
        post(LINK_REQ_TOKEN_BILLER, params, responseHandler);
    }

    public static void sentConfirmBiller( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Confirm Biller:"+LINK_CONFIRM_BILLER);
        post(LINK_CONFIRM_BILLER, params, responseHandler);
    }

    public static void sentResendToken( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Resent Token :"+LINK_RESENT_TOKEN_BILLER);
        post(LINK_RESENT_TOKEN_BILLER, params, responseHandler);
    }

    public static void sentProfilePicture( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Upload Profile Picture:"+LINK_UPLOAD_PROFILE_PIC);
        post(LINK_UPLOAD_PROFILE_PIC, params, responseHandler);
    }

    public static void sentReqTokenP2P( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address ReqToken P2P:"+LINK_REQ_TOKEN_P2P);
        post(LINK_REQ_TOKEN_P2P, params, responseHandler);
    }

    public static void sentConfirmTransP2P( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Confirm Trans P2P:"+LINK_CONFIRM_TRANS_P2P);
        post(LINK_CONFIRM_TRANS_P2P, params, responseHandler);
    }

    public static void sentResentTokenP2P( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Resent Token P2P:"+LINK_RESENT_TOKEN_P2P);
        post(LINK_RESENT_TOKEN_P2P, params, responseHandler);
    }

    public static void sentSubmitAskForMoney( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent AskForMoneySubmit:"+LINK_ASKFORMONEY_SUBMIT);
        post(LINK_ASKFORMONEY_SUBMIT, params, responseHandler);
    }

    public static void sentRetrieveNotif( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Notif Retrieve:"+LINK_NOTIF_RETRIEVE);
        post(LINK_NOTIF_RETRIEVE, params, responseHandler);
    }

    public static void sentReadNotif( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Notif Read:"+LINK_NOTIF_READ);
        post(LINK_NOTIF_READ, params, responseHandler);
    }

    public static void sentReqTokenP2PNotif( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Req Token p2p Notif:"+LINK_REQ_TOKEN_P2P_NOTIF);
        post(LINK_REQ_TOKEN_P2P_NOTIF, params, responseHandler);
    }

    public static void sentConfirmTransP2PNotif( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent confirm trans p2p notif:"+LINK_CONFIRM_TRANS_P2P_NOTIF);
        post(LINK_CONFIRM_TRANS_P2P_NOTIF, params, responseHandler);
    }

    public static void sentGetTRXStatus( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent get trx status:"+LINK_GET_TRX_STATUS);
        post(LINK_GET_TRX_STATUS, params, responseHandler);
    }

    public static void sentGetTrxReport( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent transaction report:"+LINK_TRANSACTION_REPORT);
        post(LINK_TRANSACTION_REPORT, params, responseHandler);
    }

    public static void sentPaymentBiller( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent link payment biller:"+LINK_PAYMENT_BILLER);
        post(LINK_PAYMENT_BILLER, params, responseHandler);
    }
	
	public static void getGroupList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent group list:"+LINK_GROUP_LIST);
        post(LINK_GROUP_LIST, params, responseHandler);
    }

    public static void sentAddGroup( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent add grup:"+LINK_ADD_GROUP);
        post(LINK_ADD_GROUP, params, responseHandler);
    }

    public static void getTimelineList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent timeline list:"+LINK_TIMELINE_LIST);
        post(LINK_TIMELINE_LIST, params, responseHandler);
    }

    public static void getCommentList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent comment list:"+LINK_COMMENT_LIST);
        post(LINK_COMMENT_LIST, params, responseHandler);
    }

    public static void sentAddComment( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent add coment:"+LINK_ADD_COMMENT);
        post(LINK_ADD_COMMENT, params, responseHandler);
    }

    public static void sentRemoveComment( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent remove comment:"+LINK_REMOVE_COMMENT);
        post(LINK_REMOVE_COMMENT, params, responseHandler);
    }

    public static void getLikeList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent like list:"+LINK_LIKE_LIST);
        post(LINK_LIKE_LIST, params, responseHandler);
    }

    public static void sentAddLike( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent add like:"+LINK_ADD_LIKE);
        post(LINK_ADD_LIKE, params, responseHandler);
    }

    public static void sentRemoveLike( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent remove like:"+LINK_REMOVE_LIKE);
        post(LINK_REMOVE_LIKE, params, responseHandler);
    }

    public static void sentCreatePin( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent create pin:"+LINK_CREATE_PIN);
        post(LINK_CREATE_PIN, params, responseHandler);
    }

    public static void sentChangePin( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent change pin:"+LINK_CHANGE_PIN);
        post(LINK_CHANGE_PIN, params, responseHandler);
    }

    public static void getPromoList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent promo list:"+LINK_PROMO_LIST);
        post(LINK_PROMO_LIST, params, responseHandler);
    }

    public static void sentInquiryBiller( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Inquiry Biller:"+LINK_INQUIRY_BILLER);
        post(LINK_INQUIRY_BILLER, params, responseHandler);
    }

    public static void sentBankAccountCollection( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Bank Account Collect:"+LINK_BANK_ACCOUNT_COLLECTION);
        post(LINK_BANK_ACCOUNT_COLLECTION, params, responseHandler);
    }

    public static void sentTopUpAccountCollection( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Top up Account collect:"+LINK_TOP_UP_ACCOUNT_COLLECTION);
        post(LINK_TOP_UP_ACCOUNT_COLLECTION, params, responseHandler);
    }

    public static void sentCommAccountCollection( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent comm account collect:"+LINK_COMM_ACCOUNT_COLLECTION);
        post(LINK_COMM_ACCOUNT_COLLECTION, params, responseHandler);
    }

    public static void sentListBankBiller( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent list bank biller:"+LINK_LIST_BANK_BILLER);
        post(LINK_LIST_BANK_BILLER, params, responseHandler);
    }

    public static void sentCommEspay( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent list bank biller:"+LINK_COMM_ESPAY);
        post(LINK_COMM_ESPAY, params, responseHandler);
	}
		
	public static void getHelpList( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Help List:"+LINK_HELP_LIST);
        post(LINK_HELP_LIST, params, responseHandler);
    }

    public static void getDataSB( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get Data SB:"+LINK_INQUIRY_MOBILE);
        post(LINK_INQUIRY_MOBILE, params, responseHandler);
    }

    public static void sentReqTokenSB( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Req Token SB:"+LINK_REQUEST_TOKEN_SB);
        post(LINK_REQUEST_TOKEN_SB, params, responseHandler);
    }

    public static void sentConfTokenSB( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent confirm token SB:"+LINK_CONFIRM_TOKEN_SB);
        post(LINK_CONFIRM_TOKEN_SB, params, responseHandler);
    }

    public static void sentInsertPassword( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent InsertPassword:"+LINK_INSERT_PASSWORD);
        post(LINK_INSERT_PASSWORD, params, responseHandler);
    }

    public static void sentReportEspay( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent ReportEspay:"+LINK_REPORT_ESPAY);
        post(LINK_REPORT_ESPAY, params, responseHandler);
    }

    public static void sentInquiryMobileJatim( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Inquiry mobile Jatim:"+LINK_INQUIRY_MOBILE_JATIM);
        post(LINK_INQUIRY_MOBILE_JATIM, params, responseHandler);
    }

    public static void sentConfirmTokenJatim( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Confirm token Jatim:"+LINK_CONFIRM_TOKEN_JATIM);
        post(LINK_CONFIRM_TOKEN_JATIM, params, responseHandler);
    }

    public static void sentListBankSMSRegist( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent list bank sms regist:"+LINK_LIST_BANK_SMS_REGIST);
        post(LINK_LIST_BANK_SMS_REGIST, params, responseHandler);
    }

    public static void getDenomDAP( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get denom DAP:"+LINK_DENOM_DAP);
        post(LINK_DENOM_DAP, params, responseHandler);
    }

    public static void getBankDAP( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get bank DAP:"+LINK_BANK_DAP);
        post(LINK_BANK_DAP, params, responseHandler);
    }

    public static void sentPaymentDAP( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent payment DAP:"+ LINK_PAYMENT_DAP);
        post(LINK_PAYMENT_DAP, params, responseHandler);
    }
		
	public static void sentLogout( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent logout:"+LINK_LOGOUT);
        post(LINK_LOGOUT, params, responseHandler);
    }

    public static void sentCreatePinPass( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent create pin pass:"+LINK_CREATE_PIN_PASS);
        post(LINK_CREATE_PIN_PASS, params, responseHandler);
    }

    public static void sentReportAsk( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent report ask:"+LINK_REPORT_MONEY_REQUEST);
        post(LINK_REPORT_MONEY_REQUEST, params, responseHandler);
    }

    public static void sentAsk4MoneyReject( RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent ask for money reject:"+LINK_ASK4MONEY_REJECT);
        post(LINK_ASK4MONEY_REJECT, params, responseHandler);
    }
    //get Data------------------------------------------------------------------------------------------


    public static void getBillerType( AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Biller Type:"+LINK_GET_BILLER_TYPE);
        get(LINK_GET_BILLER_TYPE, responseHandler);
    }

    public static void getAllBank( AsyncHttpResponseHandler responseHandler) {
        get(LINK_GET_ALL_BANK, responseHandler);
    }

    public static void getAppVersion( AsyncHttpResponseHandler responseHandler) {
        get(LINK_APP_VERSION, responseHandler);
    }

    public static void getHelpPIN( AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address getHelpPIN:"+LINK_HELP_PIN);
        get(LINK_HELP_PIN, responseHandler);
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }
}
