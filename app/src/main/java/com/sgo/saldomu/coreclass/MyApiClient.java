package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.os.Build;
import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.securities.Md5;
import com.sgo.saldomu.securities.SHA;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
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
    private AsyncHttpClient asyncHttpClient;
    private AsyncHttpClient asyncHttpClient_google;
    private AsyncHttpClient syncHttpClient_google;
    private SyncHttpClient syncHttpClient;

    public MyApiClient(){

    }
    public MyApiClient(Context _context){
        this.setmContext(_context);
    }

    private static MyApiClient getInstance() {
        return singleton;
    }

    public static MyApiClient Initialize(Context _context) {
        if(singleton == null) {
            singleton = new MyApiClient(_context);
            singleton.asyncHttpClient=new AsyncHttpClient();
            singleton.asyncHttpClient.addHeader("Authorization", "Basic " + getBasicAuth());
            singleton.asyncHttpClient_google=new AsyncHttpClient();
            singleton.syncHttpClient_google=new SyncHttpClient();
            singleton.syncHttpClient=new SyncHttpClient();
            singleton.syncHttpClient.addHeader("Authorization", "Basic " + getBasicAuth());
        }
        return singleton;
    }
    public static Boolean PROD_FAILURE_FLAG = true;
    public static Boolean IS_PROD = BuildConfig.isProdDomain;
    public static Boolean PROD_FLAG_ADDRESS = BuildConfig.isProdDomain;
    public static Boolean IS_INTERNET_BANKING;
    public static final String PRIVATE_KEY = "590mobil3";
    public static String COMM_ID;
    public static String COMM_ID_PULSA;

//    public static final String headaddressDEV = "http://116.90.162.173:18080/akardaya/";
//    public static final String headaddressPROD = "https://mobile.goworld.asia/akardaya2/";
    public static String headaddressfinal = BuildConfig.HeadAddress+"hpku/";

    public static String headaodaddressfinal    = BuildConfig.HeadAddress+"agentlocation/";
    public static String urlMNotif              = BuildConfig.UrlMNotif;

    //Link webservices Signature

    private static String LINK_REGISTRASI;
    private static String LINK_VALID_REGISTRASI;
    private static String LINK_LOGIN;
    public static String LINK_VALID_TOPUP;
    public static String LINK_LIST_MEMBER;
    public static String LINK_REQ_TOKEN_SGOL;
    public static String LINK_RESEND_TOKEN_SGOL;
    public static String LINK_INSERT_TRANS_TOPUP;
    public static String LINK_SALDO;
    //public static final String LINK_BANK_LIST;
    public static String LINK_BANK_LIST;
    private static String LINK_REQ_TOKEN_REGIST;
    private static String LINK_GET_ALL_BANK;
    public static String LINK_TOPUP_PULSA_RETAIL;
    public static String LINK_UPDATE_PROFILE;
    public static String LINK_CHANGE_PASSWORD;
    private static String LINK_FORGOT_PASSWORD;
    public static String LINK_MEMBER_PULSA;
    public static String LINK_USER_CONTACT_INSERT;
    public static String LINK_USER_CONTACT_UPDATE;

    public static String LINK_PROD_TOPUP_RETAIL;
    private static String LINK_GET_BILLER_TYPE;
    public static String LINK_LIST_BILLER;
    public static String LINK_DENOM_RETAIL;
    private static String LINK_REQ_TOKEN_BILLER;
    private static String LINK_CONFIRM_BILLER;
    public static String LINK_RESENT_TOKEN_BILLER;
    public static String LINK_UPLOAD_PROFILE_PIC;
    public static String LINK_UPLOAD_KTP;
    public static String LINK_LIST_BANK_BILLER;

    public static String LINK_REQ_TOKEN_P2P;
    public static String LINK_CONFIRM_TRANS_P2P;
    public static String LINK_RESENT_TOKEN_P2P;

    public static String LINK_ASKFORMONEY_SUBMIT;
    public static String LINK_NOTIF_RETRIEVE;
    private static String LINK_NOTIF_READ;

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

    private static String LINK_CREATE_PIN;
    public static String LINK_CHANGE_PIN;

    public static String LINK_INQUIRY_BILLER;
    public static String LINK_PAYMENT_BILLER;
    public static String LINK_TRANSACTION_REPORT;
    public static String LINK_PROMO_LIST;

    public static String LINK_BANK_ACCOUNT_COLLECTION;
    public static String LINK_TOP_UP_ACCOUNT_COLLECTION;
    public static String LINK_COMM_ACCOUNT_COLLECTION;
    private static String LINK_COMM_ESPAY;

	private static String LINK_APP_VERSION;
    private static String LINK_HELP_LIST;

    public static String LINK_INQUIRY_MOBILE;
    public static String LINK_REQUEST_TOKEN_SB;
    public static String LINK_CONFIRM_TOKEN_SB;

    private static String LINK_INSERT_PASSWORD;
    public static String LINK_REPORT_ESPAY;

    public static String LINK_INQUIRY_MOBILE_JATIM;
    public static String LINK_CONFIRM_TOKEN_JATIM;
    public static String LINK_LIST_BANK_SMS_REGIST;

    public static String LINK_DENOM_DAP;
    public static String LINK_BANK_DAP;
    public static String LINK_PAYMENT_DAP;
	
	public static String LINK_LOGOUT;
    private static String LINK_CREATE_PIN_PASS;
    public static String LINK_REPORT_MONEY_REQUEST;
    public static String LINK_ASK4MONEY_REJECT;

    private static String LINK_INQUIRY_CUST;
    public static String LINK_EXEC_CUST;
	
	public static String LINK_REQUEST_CASHOUT;
    public static String LINK_CONFIRM_CASHOUT;
    private static String LINK_HELP_PIN;

    public static String LINK_INQUIRY_WITHDRAW;
    public static String LINK_REQCODE_WITHDRAW;
    public static String LINK_DELTRX_WITHDRAW;
    private static String LINK_CREATE_PASS;
    public static String LINK_GET_FAILED_PIN;
    private static String LINK_ATMTOPUP;
    public static String LINK_BANKCASHOUT;
    private static String LINK_USER_PROFILE;
    private static String LINK_INQUIRY_SMS;
    public static String LINK_CLAIM_TRANSFER_NON_MEMBER;

    public static String LINK_RESEND_TOKEN_LKD;
    public static String LINK_BBS_CITY;
    public static String LINK_GLOBAL_BBS_COMM;
    public static String LINK_GLOBAL_BBS_BANK_C2A;
    public static String LINK_GLOBAL_BBS_INSERT_C2A;
    public static String LINK_BBS_BANK_ACCOUNT;
    public static String LINK_BBS_BANK_ACCOUNT_DELETE;
    public static String LINK_BBS_BANK_REG_ACCT;
    public static String LINK_BBS_JOIN_AGENT;
    public static String LINK_BBS_CONFIRM_ACCT;
    public static String LINK_BBS_REQ_ACCT;
    public static String LINK_BBS_GLOBAL_COMM;
    public static String LINK_TRX_STATUS_BBS;
    public static String LINK_GLOBAL_BBS_BANK_A2C;
    public static String LINK_GLOBAL_BBS_INSERT_A2C;
    public static String LINK_BBS_LIST_MEMBER_A2C;
    public static String LINK_BBS_OTP_MEMBER_A2C;

    public static String LINK_BBS_LIST_COMMUNITY_ALL;
    public static String LINK_REG_TOKEN_FCM;

    public static String LINK_INQUIRY_TOKEN_ATC;
    public static String LINK_INQUIRY_DATA_ATC;
    public static String LINK_CANCEL_ATC;
    public static String LINK_REQ_CHANGE_EMAIL;
    public static String LINK_CONFIRM_CHANGE_EMAIL;
    public static String LINK_REG_STEP1;
    public static String LINK_REG_STEP2;
    public static String LINK_REG_STEP3;

    public static String LINK_GOOGLE_MAPS_API_GEOCODE;

    public void InitializeAddress(){
        LINK_REGISTRASI          = headaddressfinal + "RegisterCustomer/Invoke";
        LINK_REG_STEP1           = headaddressfinal + "RegStep1/Invoke";
        LINK_REG_STEP2           = headaddressfinal + "RegStep2/Invoke";
        LINK_REG_STEP3           = headaddressfinal + "RegStep3/Invoke";
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
        LINK_UPLOAD_KTP          = headaddressfinal + "UploadKtp/Submit";
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

        LINK_INQUIRY_CUST = headaddressfinal + "InquiryCustomer/Retrieve";
        LINK_EXEC_CUST   = headaddressfinal + "ExecCustomer/Invoke";
		
		LINK_REQUEST_CASHOUT    = headaddressfinal + "RequestCashout/Invoke";
        LINK_CONFIRM_CASHOUT    = headaddressfinal + "ConfirmCashout/Invoke";
        LINK_HELP_PIN           = headaddressfinal + "HelpPIN/Retrieve";

        LINK_INQUIRY_WITHDRAW    = headaddressfinal + "InquiryWithdraw/Retrieve";
        LINK_REQCODE_WITHDRAW    = headaddressfinal + "ReqCodeWithdraw/Invoke";
        LINK_DELTRX_WITHDRAW     = headaddressfinal + "DelWithdrawTrx/Invoke";

        LINK_CREATE_PASS    = headaddressfinal + "CreatePass/Invoke";
        LINK_GET_FAILED_PIN = headaddressfinal + "GetFailedPIN/Retrieve";
        LINK_ATMTOPUP       = headaddressfinal + "ATMTopUp/Retrieve";
        LINK_BANKCASHOUT    = headaddressfinal + "BankCashout/Retrieve";
        LINK_USER_PROFILE   = headaddressfinal + "UserProfile/Retrieve";
        if(BuildConfig.isProdDomain)
            LINK_INQUIRY_SMS   = "https://mobile.goworld.asia/hpku/" + "InquirySMS/Retrieve";
        else
            LINK_INQUIRY_SMS   = headaddressfinal + "InquirySMS/Retrieve";
        LINK_CLAIM_TRANSFER_NON_MEMBER = headaddressfinal + "ClaimNonMbrTrf/Invoke";

        LINK_RESEND_TOKEN_LKD  = headaddressfinal + "ResendToken/Invoke";
        LINK_BBS_CITY = headaddressfinal + "ServiceBBSCity/getAllBBSCity";
        LINK_GLOBAL_BBS_COMM = headaddressfinal + "GlobalBBSComm/Retrieve";
        LINK_GLOBAL_BBS_BANK_C2A = headaddressfinal + "GlobalBBSBankC2A/Retrieve";
        LINK_GLOBAL_BBS_INSERT_C2A = headaddressfinal + "GlobalBBSInsertC2A/Invoke";
        LINK_BBS_BANK_ACCOUNT = headaddressfinal + "BBSBankAccount/Retrieve";
        LINK_BBS_BANK_ACCOUNT_DELETE = headaddressfinal + "DelBBSBankAcct/Invoke";
        LINK_BBS_BANK_REG_ACCT = headaddressfinal + "BBSBankRegAcct/Retrieve";
        LINK_BBS_CONFIRM_ACCT = headaddressfinal + "BBSConfirmAcct/Invoke";
        LINK_BBS_JOIN_AGENT = headaddressfinal + "BBSJoinAgent/Invoke";
        LINK_BBS_REQ_ACCT = headaddressfinal + "BBSRegAcct/Invoke";
        LINK_BBS_GLOBAL_COMM = headaddressfinal + "GlobalComm/Retrieve";
        LINK_TRX_STATUS_BBS = headaddressfinal + "TrxBBSStatus/Retrieve";
        LINK_GLOBAL_BBS_BANK_A2C = headaddressfinal + "GlobalBBSBankA2C/Retrieve";
        LINK_GLOBAL_BBS_INSERT_A2C = headaddressfinal + "GlobalBBSInsertA2C/Invoke";
        LINK_BBS_LIST_MEMBER_A2C = headaddressfinal + "BBSListMemberATC/Retrieve";
        LINK_BBS_OTP_MEMBER_A2C = headaddressfinal + "BBSOTPMemberATC/Invoke";
        LINK_BBS_LIST_COMMUNITY_ALL   = headaddressfinal + "ListCommunity/Retrieve";
        LINK_INQUIRY_TOKEN_ATC  = headaddressfinal + "InquiryTokenATC/Retrieve";
        LINK_INQUIRY_DATA_ATC   = headaddressfinal + "InquiryDataATC/Retrieve";
        LINK_CANCEL_ATC         = headaddressfinal + "CancelATC/Invoke";
        LINK_REG_TOKEN_FCM = urlMNotif + "user/register";

        String googleMapsKey = getmContext().getString(R.string.google_maps_key);
        LINK_GOOGLE_MAPS_API_GEOCODE = "https://maps.google.com/maps/api/geocode/json?sensor=false&key="+googleMapsKey+"&language=id";

        LINK_REQ_CHANGE_EMAIL = headaddressfinal + "ReqChangeEmail/Invoke";
        LINK_CONFIRM_CHANGE_EMAIL = headaddressfinal + "ConfirmChangeEmail/Invoke";

        getInstance().syncHttpClient.setTimeout(TIMEOUT);
        if(PROD_FLAG_ADDRESS)
            getInstance().syncHttpClient.setSSLSocketFactory(getSSLSocketFactory());
        getInstance().syncHttpClient.setMaxRetriesAndTimeout(2, 10000);

        getInstance().asyncHttpClient.setTimeout(TIMEOUT);
        if(PROD_FLAG_ADDRESS)
            getInstance().asyncHttpClient.setSSLSocketFactory(getSSLSocketFactory());
        getInstance().asyncHttpClient.setMaxRetriesAndTimeout(2, 10000);

        getInstance().asyncHttpClient_google.setTimeout(TIMEOUT);
        getInstance().asyncHttpClient_google.setMaxRetriesAndTimeout(2, 10000);
        getInstance().syncHttpClient_google.setTimeout(TIMEOUT);
        getInstance().syncHttpClient_google.setMaxRetriesAndTimeout(2, 10000);
    }


    public static String URL_HELP_DEV = "https://mobile-dev.espay.id/static/pages/help/";
    public static String URL_FAQ;
    public static String URL_FAQ_PROD = "https://mobile.espay.id/static/pages/help/pin_faq_saldomu.html";
    public static String URL_FAQ_DEV = URL_HELP_DEV +"pin_faq_saldomu.html";

    public static String URL_TERMS;
    public static String URL_TERMS_PROD = "https://mobile.espay.id/static/pages/help/pin_terms_conditions_id_saldomu.html";
    public static String URL_TERMS_DEV = URL_HELP_DEV +"pin_terms_conditions_id_saldomu.html";




    public static String LINK_SEARCH_AGENT = "http://116.90.162.173:59088/aod/SearchAgent/Retrieve";
    public static String LINK_CATEGORY_LIST = headaodaddressfinal + "Category/Retrieve";
    public static String LINK_MEMBER_SHOP_LIST = headaodaddressfinal + "Membershop/Retrieve";
    public static String LINK_MEMBER_SHOP_DETAIL = headaodaddressfinal + "Membershop/Detailmember";
    public static String LINK_UPDATE_MEMBER_LOCATION = headaodaddressfinal + "Manage/UpdateMemberLocation";
    public static String LINK_REGISTER_CATEGORY_SHOP = headaodaddressfinal + "Category/Registercategoryshop";
    public static String LINK_SETUP_OPENING_HOUR = headaodaddressfinal + "Manage/Insertopenhour";
    public static String LINK_SEARCH_TOKO = headaodaddressfinal + "Agent/Retrieve";
    public static String LINK_REGISTER_OPEN_CLOSE_TOKO = headaodaddressfinal + "Membershop/Registeropenclosed";
    public static String LINK_UPDATE_CLOSE_SHOP_TODAY = headaodaddressfinal + "Manage/UpdateClosedShopToday";
    public static String LINK_GOOGLE_MAP_API_ROUTE = "http://maps.googleapis.com/maps/api/directions/json";
    public static String LINK_TRANSACTION_AGENT = headaodaddressfinal + "Transaction/Retrieve";
    public static String LINK_UPDATE_APPROVAL_TRX_AGENT = headaodaddressfinal + "Transaction/Updatetransaction";
    public static String LINK_UPDATE_LOCATION_AGENT = headaodaddressfinal + "Transaction/Updateagent";
    public static String LINK_UPDATE_LOCATION_MEMBER = headaodaddressfinal + "Transaction/Updatemember";
    public static String LINK_CHECK_TRANSACTION_MEMBER = headaodaddressfinal + "Transaction/Checktransaction";
    public static String LINK_CONFIRM_TRANSACTION_MEMBER = headaodaddressfinal + "Transaction/Confirmtransaction";
    public static String LINK_CANCEL_TRANSACTION_MEMBER = headaodaddressfinal + "Transaction/Canceltransaction";

    private static final int TIMEOUT = 600000; // 200 x 1000 = 3 menit
    public static String FLAG_OTP = "N";
    public static Boolean FLAG_SIG = true;
    public static String COMM_ID_DEV = "EMOSALDOMU1500439694RS6DD"; //dev
    public static String COMM_ID_PULSA_DEV = "DAPMSCADM1458816850U9KR7"; //dev pulsa agent
    public static String COMM_ID_PULSA_PROD = "DAPHAH14992553291VINB"; //prod pulsa agent
    public static String COMM_ID_PROD = "SALDOMU1503988580RFVBK";  //prod

    public static String INCOMINGSMS_INFOBIP = "+6281350058801";
    public static String INCOMINGSMS_SPRINT = "+6281333332000";

    public static String APP_ID = BuildConfig.AppID;
    public static String CCY_VALUE = "IDR";
    public static String DEV_MEMBER_ID_PULSA_RETAIL = "EFENDI1421144347BPFIM";
    public static String PROD_MEMBER_ID_PULSA_RETAIL = "EFENDI1421205049F0018";

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
        String msgnya = uuidnya+date+BuildConfig.AppID+WebServiceName+noID;
        String hash = SHA.SHA256(apinya,msgnya);
        return hash;
    }

    public static RequestParams getSignatureWithParams(String commID, String linknya, String user_id,String access_key){

        String webServiceName = getWebserviceName(linknya);
        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = uuidnya+dtime+BuildConfig.AppID+webServiceName+ commID + user_id;
//        Timber.d("isi access_key :" + access_key);
//
//        Timber.d("isisnya signature :"+  webServiceName +" / "+commID+" / " +user_id);

        String hash = SHA.SHA256(access_key,msgnya);

        RequestParams params = new RequestParams();
        params.put(WebParams.RC_UUID, uuidnya);
        params.put(WebParams.RC_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);
        return params;
    }

    public static RequestParams getSignatureWithParamsFCM(String gcmID, String deviceId, String appID){

        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = Md5.hashMd5(uuidnya+dtime+gcmID+deviceId+appID);
        Timber.d("isi messageSignatureFCM : " + msgnya);


        String hash = SHA.SHA1(msgnya);
        Timber.d("isi sha1 signatureFCM : " + hash);

        RequestParams params = new RequestParams();
        params.put(WebParams.RQ_UUID, uuidnya);
        params.put(WebParams.RQ_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);

        return params;
    }

    public static void setCookieStore(PersistentCookieStore cookieStore) {
        getClient().setCookieStore(cookieStore);
    }

    private static void get(Context mContext, String url, AsyncHttpResponseHandler responseHandler) {
        getClient().get(mContext, url, responseHandler);
    }

    private static void post(Context mContext, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(mContext, url, params, responseHandler);
        Timber.d("isis timeoutnya : %1$s ",String.valueOf(getClient().getConnectTimeout()));
    }

    public static void postByTag(Context mContext,String tag,String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(mContext, url, params, responseHandler).setTag(tag);
        Timber.d("isis timeoutnya : %1$s ", String.valueOf(getClient().getConnectTimeout()));
    }

    public static void postSync(Context mContext,String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getInstance().syncHttpClient.post(mContext, url, params, responseHandler);
    }

    public static void getSync(Context mContext,String url, AsyncHttpResponseHandler responseHandler) {
        getInstance().syncHttpClient.get(mContext, url, responseHandler);
    }

    public static void sentInquiryDataATC(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent inquiry data ATC:"+LINK_INQUIRY_DATA_ATC);
        post(mContext,LINK_INQUIRY_DATA_ATC, params, responseHandler);
    }

    public static void sentInquiryTokenATC(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent inquiry token ATC:"+LINK_INQUIRY_TOKEN_ATC);
        post(mContext,LINK_INQUIRY_TOKEN_ATC, params, responseHandler);
    }

    public static void sentCancelATC(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent cancel ATC:"+LINK_CANCEL_ATC);
        post(mContext,LINK_CANCEL_ATC, params, responseHandler);
    }

    public static void sentReqChangeEmail(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address req change email:"+ LINK_REQ_CHANGE_EMAIL);
        post(mContext, LINK_REQ_CHANGE_EMAIL, params, responseHandler);
    }

    private static AsyncHttpClient getClientGoogle(boolean isSync){
        if(isSync)
            return getInstance().syncHttpClient_google;

        return getInstance().asyncHttpClient_google;
    }

    public static AsyncHttpClient getClient()
    {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null) {
            return getInstance().syncHttpClient;
        }

        return getInstance().asyncHttpClient;
    }

    private static String getBasicAuth() {
        String stringEncode = "dev.api.mobile"+":"+"590@dev.api.mobile!";
        byte[] encodeByte = Base64.encodeBase64(stringEncode.getBytes());
        String encode = new String(encodeByte);
        return encode.replace('+','-').replace('/','_');
    }

    public SSLSocketFactory getSSLSocketFactory(){
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("BKS");
            // Get the raw resource, which contains the keystore with
            // your trusted certificates
            int bks_version;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                bks_version = R.raw.espayid; //The BKS file
            } else {
                bks_version = R.raw.espayidv1; //The BKS (v-1) file
            }
            InputStream in = getmContext().getResources().openRawResource(bks_version);
            try {
                // Initialize the keystore with the provided trusted certificates
                // Also provide the password of the keystore
                trusted.load(in, PRIVATE_KEY.toCharArray());
            } finally {
                in.close();
            }
            // Pass the keystore to the SSLSocketFactory. The factory is responsible
            // for the verification of the server certificate.
            SSLSocketFactory sf = new SSLSocketFactory(trusted);
            // Hostname verification from certificate
            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            return sf;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void CancelRequestWS(Context _context,Boolean interruptIfRunning)
    {
        getClient().cancelRequests(_context, interruptIfRunning);
    }

    public static void CancelRequestWSByTag(String tag,Boolean interruptIfRunning)
    {
        getClient().cancelRequestsByTAG(tag, interruptIfRunning);
    }
    //----------------------------------------------------------------------------------------------------

    public static void sentDataRegister(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Register Customer: %1$s ",LINK_REGISTRASI);
        post(mContext,LINK_REGISTRASI, params, responseHandler);
    }

    public static void sentValidRegister(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Insert Customer: %1$s ",LINK_VALID_REGISTRASI);
        post(mContext,LINK_VALID_REGISTRASI, params, responseHandler);
    }

    public static void sentDataLogin(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Login: %1$s ", LINK_LOGIN);
        post(mContext, LINK_LOGIN, params, responseHandler);
    }

    public static void sentDataListMember(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Member Retrieve: %1$s ",LINK_LIST_MEMBER);
        post(mContext,LINK_LIST_MEMBER, params, responseHandler);
    }

    public static void sentDataReqTokenSGOL(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Inquiry Trx: %1$s ",LINK_REQ_TOKEN_SGOL);
        post(mContext,LINK_REQ_TOKEN_SGOL, params, responseHandler);
    }

    public static void sentResendTokenSGOL(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address InquiryResendToken: %1$s ",LINK_RESEND_TOKEN_SGOL);
        post(mContext,LINK_RESEND_TOKEN_SGOL, params, responseHandler);
    }

    public static void sentInsertTransTopup(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Insert Trx: %1$s ",LINK_INSERT_TRANS_TOPUP);
        post(mContext,LINK_INSERT_TRANS_TOPUP, params, responseHandler);
    }

    public static void getSaldo(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Saldo: %1$s ",LINK_SALDO);
        post(mContext,LINK_SALDO, params, responseHandler);
    }

    public static void getBankList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Bank list: %1$s ",LINK_BANK_LIST);
        post(mContext,LINK_BANK_LIST, params, responseHandler);
    }

    public static void sentReqTokenRegister(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Resend token Register: %1$s ",LINK_REQ_TOKEN_REGIST);
        post(mContext,LINK_REQ_TOKEN_REGIST, params, responseHandler);
    }

    public static void sentTopupPulsaRetailValidation(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        String linknya = LINK_TOPUP_PULSA_RETAIL ;
        if(IS_INTERNET_BANKING){
            if(IS_PROD)linknya = LINK_PROD_TOPUP_RETAIL;
        }

        Timber.wtf("address TOPUP Pulsa: %1$s ",linknya);
        post(mContext,linknya, params, responseHandler);
    }

    public static void sentUpdateProfile(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Update Profile: %1$s ",LINK_UPDATE_PROFILE);
        post(mContext,LINK_UPDATE_PROFILE, params, responseHandler);
    }

    public static void sentValidTopUp(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Validation Topup: %1$s ",LINK_VALID_TOPUP);
        post(mContext,LINK_VALID_TOPUP, params, responseHandler);
    }

    public static void sentChangePassword(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address CHange Password: %1$s ",LINK_CHANGE_PASSWORD);
        post(mContext,LINK_CHANGE_PASSWORD, params, responseHandler);
    }

    public static void sentForgotPassword(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Forget Password: %1$s ",LINK_FORGOT_PASSWORD);
        post(mContext,LINK_FORGOT_PASSWORD, params, responseHandler);
    }

    public static void sentMemberPulsa(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Member Pulsa: %1$s ",LINK_MEMBER_PULSA);
        post(mContext,LINK_MEMBER_PULSA, params, responseHandler);
    }

    public static void sentInsertContact(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address InquiryResendToken: %1$s ",LINK_USER_CONTACT_INSERT);
        post(mContext,LINK_USER_CONTACT_INSERT, params, responseHandler);
    }

    public static void sentUpdateContact(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Update Contact: %1$s ",LINK_USER_CONTACT_UPDATE);
        post(mContext,LINK_USER_CONTACT_UPDATE, params, responseHandler);
    }

    public static void sentListBiller(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address List Biller: %1$s ",LINK_LIST_BILLER);
        post(mContext,LINK_LIST_BILLER, params, responseHandler);
    }

    public static void sentDenomRetail(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Denom Retail: %1$s ",LINK_DENOM_RETAIL);
        post(mContext,LINK_DENOM_RETAIL, params, responseHandler);
    }

    public static void sentReqTokenBiller(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Req Token Biller: %1$s ",LINK_REQ_TOKEN_BILLER);
        post(mContext,LINK_REQ_TOKEN_BILLER, params, responseHandler);
    }

    public static void sentConfirmBiller(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Confirm Biller: %1$s ",LINK_CONFIRM_BILLER);
        post(mContext,LINK_CONFIRM_BILLER, params, responseHandler);
    }

    public static void sentResendToken(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Resent Token : %1$s ",LINK_RESENT_TOKEN_BILLER);
        post(mContext,LINK_RESENT_TOKEN_BILLER, params, responseHandler);
    }

    public static void sentProfilePicture(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Upload Profile Picture: %1$s ",LINK_UPLOAD_PROFILE_PIC);
        post(mContext,LINK_UPLOAD_PROFILE_PIC, params, responseHandler);
    }

    public static void sentPhotoKTP(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Upload KTP: %1$s ",LINK_UPLOAD_KTP);
        post(mContext,LINK_UPLOAD_KTP, params, responseHandler);
    }

    public static void sentReqTokenP2P(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address ReqToken P2P: %1$s ",LINK_REQ_TOKEN_P2P);
        post(mContext,LINK_REQ_TOKEN_P2P, params, responseHandler);
    }

    public static void sentConfirmTransP2P(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Confirm Trans P2P: %1$s ",LINK_CONFIRM_TRANS_P2P);
        post(mContext,LINK_CONFIRM_TRANS_P2P, params, responseHandler);
    }

    public static void sentResentTokenP2P(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Resent Token P2P: %1$s ",LINK_RESENT_TOKEN_P2P);
        post(mContext,LINK_RESENT_TOKEN_P2P, params, responseHandler);
    }

    public static void sentSubmitAskForMoney(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent AskForMoneySubmit: %1$s ",LINK_ASKFORMONEY_SUBMIT);
        post(mContext,LINK_ASKFORMONEY_SUBMIT, params, responseHandler);
    }

    public static void sentRetrieveNotif(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Notif Retrieve: %1$s ",LINK_NOTIF_RETRIEVE);
        post(mContext,LINK_NOTIF_RETRIEVE, params, responseHandler);
    }

    public static void sentReadNotif(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Notif Read: %1$s ",LINK_NOTIF_READ);
        post(mContext,LINK_NOTIF_READ, params, responseHandler);
    }

    public static void sentReqTokenP2PNotif(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Req Token p2p Notif: %1$s ",LINK_REQ_TOKEN_P2P_NOTIF);
        post(mContext,LINK_REQ_TOKEN_P2P_NOTIF, params, responseHandler);
    }

    public static void sentConfirmTransP2PNotif(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent confirm trans p2p notif: %1$s ",LINK_CONFIRM_TRANS_P2P_NOTIF);
        post(mContext,LINK_CONFIRM_TRANS_P2P_NOTIF, params, responseHandler);
    }

    public static void sentGetTRXStatus(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent get trx status: %1$s ",LINK_GET_TRX_STATUS);
        post(mContext,LINK_GET_TRX_STATUS, params, responseHandler);
    }

    public static void sentGetTrxReport(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent transaction report: %1$s ",LINK_TRANSACTION_REPORT);
        post(mContext,LINK_TRANSACTION_REPORT, params, responseHandler);
    }

    public static void sentPaymentBiller(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent link payment biller: %1$s ",LINK_PAYMENT_BILLER);
        post(mContext,LINK_PAYMENT_BILLER, params, responseHandler);
    }
	
	public static void getGroupList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent group list: %1$s ",LINK_GROUP_LIST);
        post(mContext,LINK_GROUP_LIST, params, responseHandler);
    }

    public static void sentAddGroup(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent add grup: %1$s ",LINK_ADD_GROUP);
        post(mContext,LINK_ADD_GROUP, params, responseHandler);
    }

    public static void getTimelineList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent timeline list: %1$s ",LINK_TIMELINE_LIST);
        post(mContext,LINK_TIMELINE_LIST, params, responseHandler);
    }

    public static void getCommentList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent comment list: %1$s ",LINK_COMMENT_LIST);
        post(mContext,LINK_COMMENT_LIST, params, responseHandler);
    }

    public static void sentAddComment(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent add coment: %1$s ",LINK_ADD_COMMENT);
        post(mContext,LINK_ADD_COMMENT, params, responseHandler);
    }

    public static void sentRemoveComment(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent remove comment: %1$s ",LINK_REMOVE_COMMENT);
        post(mContext,LINK_REMOVE_COMMENT, params, responseHandler);
    }

    public static void getLikeList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent like list: %1$s ",LINK_LIKE_LIST);
        post(mContext,LINK_LIKE_LIST, params, responseHandler);
    }

    public static void sentAddLike(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent add like: %1$s ",LINK_ADD_LIKE);
        post(mContext,LINK_ADD_LIKE, params, responseHandler);
    }

    public static void sentRemoveLike(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent remove like: %1$s ",LINK_REMOVE_LIKE);
        post(mContext,LINK_REMOVE_LIKE, params, responseHandler);
    }

    public static void sentCreatePin(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent create pin: %1$s ",LINK_CREATE_PIN);
        post(mContext,LINK_CREATE_PIN, params, responseHandler);
    }

    public static void sentChangePin(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent change pin: %1$s ",LINK_CHANGE_PIN);
        post(mContext,LINK_CHANGE_PIN, params, responseHandler);
    }

    public static void getPromoList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent promo list: %1$s ",LINK_PROMO_LIST);
        post(mContext,LINK_PROMO_LIST, params, responseHandler);
    }

    public static void sentInquiryBiller(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Inquiry Biller: %1$s ",LINK_INQUIRY_BILLER);
        post(mContext,LINK_INQUIRY_BILLER, params, responseHandler);
    }

    public static void sentBankAccountCollection(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Bank Account Collect: %1$s ",LINK_BANK_ACCOUNT_COLLECTION);
        post(mContext,LINK_BANK_ACCOUNT_COLLECTION, params, responseHandler);
    }

    public static void sentTopUpAccountCollection(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Top up Account collect: %1$s ",LINK_TOP_UP_ACCOUNT_COLLECTION);
        post(mContext,LINK_TOP_UP_ACCOUNT_COLLECTION, params, responseHandler);
    }

    public static void sentCommAccountCollection(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent comm account collect: %1$s ",LINK_COMM_ACCOUNT_COLLECTION);
        post(mContext,LINK_COMM_ACCOUNT_COLLECTION, params, responseHandler);
    }

    public static void sentListBankBiller(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent list bank biller: %1$s ",LINK_LIST_BANK_BILLER);
        post(mContext,LINK_LIST_BANK_BILLER, params, responseHandler);
    }

    public static void sentCommEspay(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent list bank biller: %1$s ",LINK_COMM_ESPAY);
        post(mContext,LINK_COMM_ESPAY, params, responseHandler);
	}
		
	public static void getHelpList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Help List: %1$s ",LINK_HELP_LIST);
        post(mContext,LINK_HELP_LIST, params, responseHandler);
    }

    public static void getDataSB(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get Data SB: %1$s ",LINK_INQUIRY_MOBILE);
        post(mContext,LINK_INQUIRY_MOBILE, params, responseHandler);
    }

    public static void sentReqTokenSB(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Req Token SB: %1$s ",LINK_REQUEST_TOKEN_SB);
        post(mContext,LINK_REQUEST_TOKEN_SB, params, responseHandler);
    }

    public static void sentConfTokenSB(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent confirm token SB: %1$s ",LINK_CONFIRM_TOKEN_SB);
        post(mContext,LINK_CONFIRM_TOKEN_SB, params, responseHandler);
    }

    public static void sentInsertPassword(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent InsertPassword: %1$s ",LINK_INSERT_PASSWORD);
        post(mContext,LINK_INSERT_PASSWORD, params, responseHandler);
    }

    public static void sentReportEspay(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent ReportEspay: %1$s ",LINK_REPORT_ESPAY);
        post(mContext,LINK_REPORT_ESPAY, params, responseHandler);
    }

    public static void sentInquiryMobileJatim(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Inquiry mobile Jatim: %1$s ",LINK_INQUIRY_MOBILE_JATIM);
        post(mContext,LINK_INQUIRY_MOBILE_JATIM, params, responseHandler);
    }

    public static void sentConfirmTokenJatim(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent Confirm token Jatim: %1$s ",LINK_CONFIRM_TOKEN_JATIM);
        post(mContext,LINK_CONFIRM_TOKEN_JATIM, params, responseHandler);
    }

    public static void sentListBankSMSRegist(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent list bank sms regist: %1$s ",LINK_LIST_BANK_SMS_REGIST);
        post(mContext,LINK_LIST_BANK_SMS_REGIST, params, responseHandler);
    }

    public static void getDenomDAP(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get denom DAP: %1$s ",LINK_DENOM_DAP);
        post(mContext,LINK_DENOM_DAP, params, responseHandler);
    }

    public static void getBankDAP(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get bank DAP: %1$s ",LINK_BANK_DAP);
        post(mContext,LINK_BANK_DAP, params, responseHandler);
    }

    public static void sentPaymentDAP( Context mContext,RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent payment DAP: %1$s ", LINK_PAYMENT_DAP);
        post(mContext,LINK_PAYMENT_DAP, params, responseHandler);
    }
	
	public static void sentLogout(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent logout: %1$s ",LINK_LOGOUT);
        post(mContext,LINK_LOGOUT, params, responseHandler);
    }

    public static void sentCreatePinPass(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent create pin pass: %1$s ",LINK_CREATE_PIN_PASS);
        post(mContext,LINK_CREATE_PIN_PASS, params, responseHandler);
    }

    public static void sentReportAsk(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent report ask: %1$s ",LINK_REPORT_MONEY_REQUEST);
        post(mContext,LINK_REPORT_MONEY_REQUEST, params, responseHandler);
    }

    public static void sentAsk4MoneyReject(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent ask for money reject: %1$s ",LINK_ASK4MONEY_REJECT);
        post(mContext,LINK_ASK4MONEY_REJECT, params, responseHandler);
    }

    public static void inqCustomer(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent inquiry customer: %1$s ",LINK_INQUIRY_CUST);
        post(mContext,LINK_INQUIRY_CUST, params, responseHandler);
    }

    public static void sentExecCust(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent exec customer: %1$s ",LINK_EXEC_CUST);
        post(mContext,LINK_EXEC_CUST, params, responseHandler);
    }
	
	public static void sentReqCashout(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent req cashout: %1$s ",LINK_REQUEST_CASHOUT);
        post(mContext,LINK_REQUEST_CASHOUT, params, responseHandler);
    }

    public static void sentConfCashout(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent confirm cashout: %1$s ",LINK_CONFIRM_CASHOUT);
        post(mContext,LINK_CONFIRM_CASHOUT, params, responseHandler);
    }

    public static void sentInqWithdraw(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent inq withdraw: %1$s ",LINK_INQUIRY_WITHDRAW);
        post(mContext,LINK_INQUIRY_WITHDRAW, params, responseHandler);
    }

    public static void sentReqCodeWithdraw(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent reg code withdraw: %1$s ",LINK_REQCODE_WITHDRAW);
        post(mContext,LINK_REQCODE_WITHDRAW, params, responseHandler);
    }

    public static void sentDelTrxWithdraw(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent del trx withdraw: %1$s ",LINK_DELTRX_WITHDRAW);
        post(mContext,LINK_DELTRX_WITHDRAW, params, responseHandler);
    }

    public static void sentCreatePass(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent create pass: %1$s ",LINK_CREATE_PASS);
        post(mContext,LINK_CREATE_PASS, params, responseHandler);
    }

    public static void sentGetFailedPIN(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent get failed pin: %1$s ",LINK_GET_FAILED_PIN);
        post(mContext,LINK_GET_FAILED_PIN, params, responseHandler);
    }

    public static void getATMTopUp(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get ATM top up: %1$s ",LINK_ATMTOPUP);
        post(mContext,LINK_ATMTOPUP, params, responseHandler);
    }

    public static void getBankCashout(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address get bank cashout: %1$s ",LINK_BANKCASHOUT);
        post(mContext,LINK_BANKCASHOUT, params, responseHandler);
    }
    public static void sentUserProfile(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent user profile: %1$s ",LINK_USER_PROFILE);
        post(mContext,LINK_USER_PROFILE, params, responseHandler);
    }

    public static void sentInquirySMS(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent inquiry sms: %1$s ",LINK_INQUIRY_SMS);
        post(mContext,LINK_INQUIRY_SMS, params, responseHandler);
    }
    public static void sentClaimNonMemberTrf(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent claim non member transfer: %1$s ",LINK_CLAIM_TRANSFER_NON_MEMBER);
        post(mContext,LINK_CLAIM_TRANSFER_NON_MEMBER, params, responseHandler);
    }
    public static void sentResendTokenLKD(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent resend token LKD: %1$s ",LINK_RESEND_TOKEN_LKD);
        post(mContext,LINK_RESEND_TOKEN_LKD, params, responseHandler);
    }

    public static void getGlobalBBSComm(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address global bbs comm: %1$s ", LINK_GLOBAL_BBS_COMM);
        if(tag != null)
            postByTag(mContext,tag,LINK_GLOBAL_BBS_COMM,params,responseHandler);
        else
            post(mContext, LINK_GLOBAL_BBS_COMM, params, responseHandler);
    }

    public static void getGlobalBBSBankC2A(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address global bbs bank C2A: %1$s ", LINK_GLOBAL_BBS_BANK_C2A);
        post(mContext, LINK_GLOBAL_BBS_BANK_C2A, params, responseHandler);
    }

    public static void sentGlobalBBSInsertC2A(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address global bbs insert C2A: %1$s ", LINK_GLOBAL_BBS_INSERT_C2A);
        post(mContext, LINK_GLOBAL_BBS_INSERT_C2A, params, responseHandler);
    }

    public static void sentBBSBankAccountRetreive(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address bbs bank account retreive: %1$s ", LINK_BBS_BANK_ACCOUNT);
        if(tag != null)
            postByTag(mContext,tag, LINK_BBS_BANK_ACCOUNT, params, responseHandler);
        else
            post(mContext, LINK_BBS_BANK_ACCOUNT, params, responseHandler);
    }

    public static void sentBBSBankAccountDelete(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address bbs bank account delete: %1$s ", LINK_BBS_BANK_ACCOUNT_DELETE);
        post(mContext, LINK_BBS_BANK_ACCOUNT_DELETE, params, responseHandler);
    }

    public static void sentBBSBankRegAcct(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address bbs bank reg account: %1$s ", LINK_BBS_BANK_REG_ACCT);
        if(tag != null)
            postByTag(mContext,tag, LINK_BBS_BANK_REG_ACCT, params, responseHandler);
        else
            post(mContext, LINK_BBS_BANK_REG_ACCT, params, responseHandler);
    }

    public static void sentBBSJoinAgent(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address bbs join agent: %1$s ", LINK_BBS_JOIN_AGENT);
        if(tag != null)
            postByTag(mContext,tag, LINK_BBS_JOIN_AGENT, params, responseHandler);
        else
            post(mContext, LINK_BBS_JOIN_AGENT, params, responseHandler);
    }
    public static void sentBBSReqAcct(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address bbs req acct: %1$s ", LINK_BBS_REQ_ACCT);
        if(tag != null)
            postByTag(mContext,tag, LINK_BBS_REQ_ACCT, params, responseHandler);
        else
            post(mContext, LINK_BBS_REQ_ACCT, params, responseHandler);
    }
    public static void sentBBSConfirmAcct(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address bbs confirm acct: %1$s ", LINK_BBS_CONFIRM_ACCT);
        if(tag != null)
            postByTag(mContext,tag, LINK_BBS_CONFIRM_ACCT, params, responseHandler);
        else
            post(mContext, LINK_BBS_CONFIRM_ACCT, params, responseHandler);
    }

    public static void sentRetreiveGlobalComm(Context mContext,String tag, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address retreive global Comm: %1$s ", LINK_BBS_GLOBAL_COMM);
        if(tag != null)
            postByTag(mContext,tag, LINK_BBS_GLOBAL_COMM, params, responseHandler);
        else
            post(mContext, LINK_BBS_GLOBAL_COMM, params, responseHandler);
    }

    public static void sentGetTRXStatusBBS(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent get trx status bbs: %1$s ",LINK_TRX_STATUS_BBS);
        post(mContext,LINK_TRX_STATUS_BBS, params, responseHandler);
    }

    public static void getGlobalBBSBankA2C(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent global bbs bank a2c: %1$s ", LINK_GLOBAL_BBS_BANK_A2C);
        post(mContext, LINK_GLOBAL_BBS_BANK_A2C, params, responseHandler);
    }

    public static void sentGlobalBBSInsertA2C(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address global bbs insert A2C: %1$s ", LINK_GLOBAL_BBS_INSERT_A2C);
        post(mContext, LINK_GLOBAL_BBS_INSERT_A2C, params, responseHandler);
    }

    public static void sentBBSListMemberA2C(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address global bbs insert A2C: %1$s ", LINK_BBS_LIST_MEMBER_A2C);
        post(mContext, LINK_BBS_LIST_MEMBER_A2C, params, responseHandler);
    }

    public static void sentBBSOTPMemberA2C(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address global bbs insert A2C: %1$s ", LINK_BBS_OTP_MEMBER_A2C);
        post(mContext, LINK_BBS_OTP_MEMBER_A2C, params, responseHandler);
    }

    public static void sentBBSListCommunityAllSync(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address BBS list community all sync: %1$s ", LINK_BBS_LIST_COMMUNITY_ALL);
        postSync(mContext, LINK_BBS_LIST_COMMUNITY_ALL, params, responseHandler);
    }

    public static void sentReqTokenFCM(Context mContext,boolean isSync, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address req token fcm:"+ LINK_REG_TOKEN_FCM);
        if(isSync)
            postSync(mContext,LINK_REG_TOKEN_FCM,params,responseHandler);
        else
            post(mContext, LINK_REG_TOKEN_FCM, params, responseHandler);
    }

    public static void sentConfirmChangeEmail(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address confirm change email:"+ LINK_CONFIRM_CHANGE_EMAIL);
        post(mContext, LINK_CONFIRM_CHANGE_EMAIL, params, responseHandler);
    }

    public static void sentRegStep3(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address reg step 3:"+ LINK_REG_STEP3);
        post(mContext, LINK_REG_STEP3, params, responseHandler);
    }

    public static void sentRegStep1(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address reg step 1:"+ LINK_REG_STEP1);
        post(mContext, LINK_REG_STEP1, params, responseHandler);
    }

    public static void sentRegStep2(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address reg step 2:"+ LINK_REG_STEP2);
        post(mContext, LINK_REG_STEP2, params, responseHandler);
    }

    //get Data------------------------------------------------------------------------------------------


    public static void getBillerType(Context mContext, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address Get Biller Type: %1$s ",LINK_GET_BILLER_TYPE);
        get(mContext, LINK_GET_BILLER_TYPE, responseHandler);
    }

    public static void getAllBank(Context mContext, AsyncHttpResponseHandler responseHandler) {
        get(mContext,LINK_GET_ALL_BANK, responseHandler);
    }

    public static void getAppVersion(Context mContext, AsyncHttpResponseHandler responseHandler) {
        get(mContext,LINK_APP_VERSION, responseHandler);
    }
	
	public static void getHelpPIN(Context mContext, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address getHelpPIN: %1$s ",LINK_HELP_PIN);
        get(mContext,LINK_HELP_PIN, responseHandler);
    }

    public static void getBBSCity(Context mContext, Boolean isSync, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address getBBSCity: %1$s ",LINK_BBS_CITY);
        if(isSync)
            getSync(mContext,LINK_BBS_CITY,responseHandler);
        else
            get(mContext,LINK_BBS_CITY, responseHandler);
    }

    private Context getmContext() {
        return mContext;
    }

    private void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public static void updateMemberLocation(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address update member location: %1$s ",LINK_UPDATE_MEMBER_LOCATION);
        post(mContext,LINK_UPDATE_MEMBER_LOCATION, params, responseHandler);
    }

    public static void registerCategoryShop(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address register category shop: %1$s ",LINK_REGISTER_CATEGORY_SHOP);
        post(mContext,LINK_REGISTER_CATEGORY_SHOP, params, responseHandler);
    }

    public static void setupOpeningHour(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address register category shop: %1$s ",LINK_SETUP_OPENING_HOUR);
        post(mContext,LINK_SETUP_OPENING_HOUR, params, responseHandler);
    }

    public static void searchToko(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address search toko: %1$s ",LINK_SEARCH_TOKO);
        post(mContext,LINK_SEARCH_TOKO, params, responseHandler);
    }

    public static void getCategoryList(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address get category list: %1$s ",LINK_CATEGORY_LIST);
        post(mContext,LINK_CATEGORY_LIST, params, responseHandler);
    }


    public static void getMemberShopList(Context mContext, RequestParams params, Boolean isSync, AsyncHttpResponseHandler responseHandler) {
        if(isSync)
            postSync(mContext,LINK_MEMBER_SHOP_LIST, params, responseHandler);
        else
            post(mContext,LINK_MEMBER_SHOP_LIST, params, responseHandler);
    }

    public static void getMemberShopDetail(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address get member shop detail: %1$s ",LINK_MEMBER_SHOP_DETAIL);
        post(mContext,LINK_MEMBER_SHOP_DETAIL, params, responseHandler);
    }

    public static void searchAgent(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent search agent: %1$s ",LINK_SEARCH_AGENT);
        post(mContext,LINK_SEARCH_AGENT, params, responseHandler);
    }

    public static void registerOpenCloseShop(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent register open close shop: %1$s ",LINK_REGISTER_OPEN_CLOSE_TOKO);
        post(mContext,LINK_REGISTER_OPEN_CLOSE_TOKO, params, responseHandler);
    }

    public static void updateCloseShopToday(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent update close shop today: %1$s ",LINK_UPDATE_CLOSE_SHOP_TODAY);
        post(mContext,LINK_UPDATE_CLOSE_SHOP_TODAY, params, responseHandler);
    }

    public static void getListTransactionAgent(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address get trx agent list: %1$s ",LINK_TRANSACTION_AGENT);
        post(mContext,LINK_TRANSACTION_AGENT, params, responseHandler);
    }

    public static void updateTransactionAgent(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address update approval trx agent: %1$s ",LINK_UPDATE_APPROVAL_TRX_AGENT);
        post(mContext,LINK_UPDATE_APPROVAL_TRX_AGENT, params, responseHandler);
    }

    public static void getGoogleMapRoute(Context mContext, String queryString, AsyncHttpResponseHandler responseHandler) {
        Timber.wtf("address sent google maps route: %1$s ",LINK_GOOGLE_MAP_API_ROUTE);

        RequestParams params = new RequestParams();
        getClientGoogle(true).post(mContext,LINK_GOOGLE_MAP_API_ROUTE+"?"+queryString, params, responseHandler);
    }

    public static void updateLocationAgent(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address update location agent: %1$s ",LINK_UPDATE_LOCATION_AGENT);
        post(mContext,LINK_UPDATE_LOCATION_AGENT, params, responseHandler);
    }

    public static void updateLocationMember(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address update location member: %1$s ",LINK_UPDATE_LOCATION_MEMBER);
        post(mContext,LINK_UPDATE_LOCATION_MEMBER, params, responseHandler);
    }

    public static void checkTransactionMember(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address check transaction member: %1$s ",LINK_CHECK_TRANSACTION_MEMBER);
        post(mContext,LINK_CHECK_TRANSACTION_MEMBER, params, responseHandler);
    }

    public static void confirmTransactionMember(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address confirm transaction member: %1$s ",LINK_CONFIRM_TRANSACTION_MEMBER);
        post(mContext,LINK_CONFIRM_TRANSACTION_MEMBER, params, responseHandler);
    }

    public static void cancelTransactionMember(Context mContext, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        Timber.wtf("address cancel transaction member: %1$s ",LINK_CANCEL_TRANSACTION_MEMBER);
        post(mContext,LINK_CANCEL_TRANSACTION_MEMBER, params, responseHandler);
    }

    public static void getGoogleAPICoordinateByAddress(Context mContext, String address, AsyncHttpResponseHandler responseHandler) {

        try {
            String query = URLEncoder.encode(address, "utf-8");
            Timber.wtf("address google maps api geocode: %1$s ",LINK_GOOGLE_MAPS_API_GEOCODE);
            getClientGoogle(false).get(mContext,LINK_GOOGLE_MAPS_API_GEOCODE+"&address="+ query, responseHandler);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static void getGoogleAPIAddressByLatLng(Context mContext, Double latitude, Double longitude, AsyncHttpResponseHandler responseHandler) {

        try {
            Timber.wtf("address google maps api geocode: %1$s ",LINK_GOOGLE_MAPS_API_GEOCODE+"&latlng="+ latitude+","+longitude);
            getClientGoogle(false).get(mContext,LINK_GOOGLE_MAPS_API_GEOCODE+"&latlng="+ latitude+","+longitude, responseHandler);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
}

