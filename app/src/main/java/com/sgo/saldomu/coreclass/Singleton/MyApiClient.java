package com.sgo.saldomu.coreclass.Singleton;

import android.content.Context;
import android.os.Looper;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 Created by Administrator on 7/14/2014.
 */
public class MyApiClient {

    private static MyApiClient singleton = null;
    private Context mContext;
//    public static final String idService = "dev.api.mobile";
//    public static final String passService = "590@dev.api.mobile!";
    private SecurePreferences sp;

    private Context getmContext() {
        return mContext;
    }

    private void setmContext(Context mContext) {
        this.mContext = mContext;
    }


    public MyApiClient(){

    }

    public MyApiClient(Context _context){
        this.setmContext(_context);
    }

    public static MyApiClient getInstance() {
        return singleton;
    }


    public static MyApiClient Initialize(Context _context) {
        if(singleton == null) {
            singleton = new MyApiClient(_context);
//            singleton.asyncHttpClient=new AsyncHttpClient();
//            singleton.asyncHttpClient_google=new AsyncHttpClient();
//            singleton.syncHttpClient_google=new SyncHttpClient();
//            singleton.asyncHttpClient_mnotif=new AsyncHttpClient();
//            singleton.syncHttpClient_mnotif=new SyncHttpClient();
//            singleton.syncHttpClient=new SyncHttpClient();
//            singleton.asyncHttpClientUnstrusted = new AsyncHttpClient();
//            singleton.asyncHttpClient.addHeader("Authorization", "Basic " + getBasicAuth());
//            singleton.asyncHttpClientUnstrusted.addHeader("Authorization", "Basic " + getBasicAuth());
//            singleton.syncHttpClient.addHeader("Authorization", "Basic " + getBasicAuth());
//
//            singleton.asyncHttpClient_mnotif.addHeader("Authorization", "Basic " + getBasicAuthForMNotif());
//            singleton.syncHttpClient_mnotif.addHeader("Authorization", "Basic " + getBasicAuthForMNotif());
            singleton.sp = CustomSecurePref.getInstance().getmSecurePrefs();
        }
        return singleton;
    }

    public static Boolean PROD_FAILURE_FLAG     = true;
    public static Boolean IS_PROD               = BuildConfig.IS_PROD_DOMAIN;
    public static Boolean PROD_FLAG_ADDRESS     = BuildConfig.IS_PROD_DOMAIN;
    public static Boolean IS_INTERNET_BANKING;
    public static String COMM_ID;
    public static String COMM_ID_PULSA;
    public static String COMM_ID_TAGIH;

//    public static final String headaddressDEV = "http://116.90.162.173:18080/akardaya/";
//    public static final String headaddressPROD = "https://mobile.goworld.asia/akardaya2/";
    public static String headaddressfinal       = BuildConfig.HEAD_ADDRESSS+"saldomu/";

    public static String headaodaddressfinal    = BuildConfig.HEAD_ADDRESSS+"saldomu/agentlocation/";
    public static String urlMNotif              = BuildConfig.URL_MNOTIF;

    //Link webservices Signature

    private static String LINK_REGISTRASI;
    private static String LINK_VALID_REGISTRASI;
    public static String LINK_LOGIN;
    public static String LINK_VALID_TOPUP;
    public static String LINK_LIST_MEMBER;
    public static String LINK_REQ_TOKEN_SGOL;
    public static String LINK_RESEND_TOKEN_SGOL;
    public static String LINK_INSERT_TRANS_TOPUP;
    public static String LINK_INSERT_TRANS_TOPUP_NEW;
    public static String LINK_SALDO;
    public static String LINK_SALDO_COLLECTOR;
    //public static final String LINK_BANK_LIST;
    public static String LINK_BANK_LIST;
    private static String LINK_REQ_TOKEN_REGIST;
    private static String LINK_GET_ALL_BANK;
    public static String LINK_TOPUP_PULSA_RETAIL;
    public static String LINK_UPDATE_PROFILE;
    public static String LINK_CHANGE_PASSWORD;
    public static String LINK_FORGOT_PASSWORD;
    public static String LINK_FORGOT_PIN;
    public static String LINK_MEMBER_PULSA;
    public static String LINK_USER_CONTACT_INSERT;
    public static String LINK_USER_CONTACT_UPDATE;

    public static String LINK_PROD_TOPUP_RETAIL;
    public static String LINK_GET_BILLER_TYPE;
    public static String LINK_LIST_BILLER;
    public static String LINK_DENOM_RETAIL;
    private static String LINK_REQ_TOKEN_BILLER;
    private static String LINK_CONFIRM_BILLER;
    public static String LINK_RESENT_TOKEN_BILLER;
    public static String LINK_RETRY_TOKEN;
    public static String LINK_UPLOAD_PROFILE_PIC;
    public static String LINK_UPLOAD_KTP;
    public static String LINK_UPLOAD_SIUP_NPWP;
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
    public static String LINK_CHANGE_EMAIL;

    public static String LINK_INQUIRY_BILLER;
    public static String LINK_PAYMENT_BILLER;
    public static String LINK_TRANSACTION_REPORT;
    public static String LINK_PROMO_LIST;

    public static String LINK_BANK_ACCOUNT_COLLECTION;
    public static String LINK_TOP_UP_ACCOUNT_COLLECTION;
    public static String LINK_COMM_ACCOUNT_COLLECTION;
    private static String LINK_COMM_ESPAY;

	public static String LINK_APP_VERSION;
    public static String LINK_HELP_LIST;

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
    public static String LINK_REPORT_COMM_FEE;
    public static String LINK_REPORT_COLLECTOR;
    public static String LINK_ASK4MONEY_REJECT;

    private static String LINK_INQUIRY_CUST;
    public static String LINK_EXEC_CUST;
    public static String LINK_EXEC_AGENT;

	public static String LINK_REQUEST_CASHOUT;
    public static String LINK_CONFIRM_CASHOUT;
    public static String LINK_REJECT_CONFIRM_CASHOUT;
    public static String LINK_HELP_PIN;

    public static String LINK_INQUIRY_WITHDRAW;
    public static String LINK_REQCODE_WITHDRAW;
    public static String LINK_DELTRX_WITHDRAW;
    public static String LINK_CREATE_PASS;
    public static String LINK_GET_FAILED_PIN;
    private static String LINK_ATMTOPUP;
    public static String LINK_BANKCASHOUT;
    public static String LINK_USER_PROFILE;
    public static String LINK_INQUIRY_SMS;
    public static String LINK_CLAIM_TRANSFER_NON_MEMBER;

    public static String LINK_RESEND_TOKEN_LKD;
    public static String LINK_BBS_CITY;
    public static String LINK_BBS_BIRTH_PLACE;
//    public static String LINK_GLOBAL_BBS_COMM;
//    public static String LINK_GLOBAL_BBS_BANK_C2A;
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
    public static String LINK_GLOBAL_BBS_INSERT_C2R;

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
    //list community scadm
    public static String LINK_GET_LIST_COMMUNITY_SCADM;
    public static String LINK_GET_LIST_COMMUNITY_TOPUP_SCADM;
    public static String LINK_GET_LIST_COMMUNITY_DENOM_SCADM;
    //menu join di scadm
    public static String LINK_GET_PREVIEW_COMMUNITY_SCADM;
    public static String LINK_CONFIRM_COMMUNITY_SCADM;
    //list bank scadm, untuk spinner produk bank
    public static String LINK_GET_LIST_BANK_TOPUP_SCADM;
    public static String LINK_CONFIRM_TOPUP_SCADM;
    public static String LINK_CONFIRM_TOPUP_SCADM_NEW;
    public static String LINK_GET_LIST_BANK_DENOM_SCADM;
    public static String LINK_GET_DENOM_LIST;
    public static String LINK_GET_DENOM_INVOKE;
    public static String LINK_GET_DENOM_INVOKE_NEW;
    public static String LINK_BBS_SEND_DATA_LKD;
    public static String LINK_BBS_MANDIRI_LKD;
    public static String LINK_CANCEL_TRANSACTION_DGI;
    public static String LINK_LIST_INVOICE_DGI;
    public static String LINK_CONFIRM_PAYMENT_DGI;
    public static String LINK_REQ_TOKEN_INVOICE_DGI;
    public static String LINK_CANCEL_SEARCH_DGI;
    public static String LINK_SET_MEMBER_LOC;

    public static String LINK_GOOGLE_MAPS_API_GEOCODE;
    public static String LINK_GOOGLE_MAPS_API_GEOCODE_BASE;

    public static String LINK_GET_OTP;
    public static String LINK_CONFIRM_OTP;
    public static String LINK_INQUIRY_SOF;
    public static String LINK_CANCEL_PAYMENT_SOF;
    public static String LINK_PAY_SOF;
    public static String LINK_FCM;
    public static String LINK_LIST_FILE;
    public static String LINK_DOWNLOAD_FILE;
    public static String LINK_HISTORY;
    public static String LINK_HISTORY_COLLECTOR;

    public static String LINK_TRX_FAVORITE_LIST;
    public static String LINK_TRX_FAVORITE_SAVE;
    public static String LINK_TRX_FAVORITE_SAVE_TRF;
    public static String LINK_TRX_FAVORITE_DELETE;
    public static String LINK_SEARCH_MEMBER;
    public static String LINK_EXEC_UPGRADE_MEMBER;
    public static String LINK_REQ_UPGRADE_MEMBER;
    public static String LINK_GET_BILLER_DENOM;
    public static String LINK_GET_ANCHOR_COMMUNITIES;
    public static String LINK_INQUIRY_CUSTOMER_ACCT;
    public static String LINK_CONFIRM_TOKEN_C2R;
    public static String LINK_RESEND_TOKEN_C2R;

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
        LINK_INSERT_TRANS_TOPUP_NEW  = headaddressfinal + "InsertTrxNew/Invoke";
        LINK_SALDO               = headaddressfinal + "Balance/Retrieve";
        //LINK_BANK_LIST           = headaddressfinal + "BankList/Retrieve";
        LINK_BANK_LIST           = headaddressfinal + "BankMember/Retrieve";
        LINK_REQ_TOKEN_REGIST    = headaddressfinal + "ResendTokenCust/Invoke";
        LINK_GET_ALL_BANK        = headaddressfinal + "ServiceBank/GetAllBank";
        LINK_TOPUP_PULSA_RETAIL  = headaddressfinal + "TopUpPulsa/Invoke";
        LINK_UPDATE_PROFILE      = headaddressfinal + "UserProfile/Update";
        LINK_CHANGE_PASSWORD     = headaddressfinal + "ChangePassword/Invoke";
        LINK_FORGOT_PASSWORD     = headaddressfinal + "ForgotPassword/Invoke";
        LINK_FORGOT_PIN          = headaddressfinal + "ForgotPIN/Invoke";
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
        LINK_RETRY_TOKEN         = headaddressfinal + "RetryToken/Invoke";
        LINK_LIST_BANK_BILLER    = headaddressfinal + "BankBiller/Retrieve";

        LINK_UPLOAD_PROFILE_PIC  = headaddressfinal + "UploadProfPic/Submit";
        LINK_UPLOAD_KTP          = headaddressfinal + "UploadKtp/Submit";
        LINK_UPLOAD_SIUP_NPWP    = headaddressfinal + "UploadNPWP/Submit";
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
        LINK_CHANGE_EMAIL        = headaddressfinal + "ChangeEmail/Invoke";
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
        LINK_REPORT_COMM_FEE    = headaddressfinal + "ReportCommFee/Retrieve";
        LINK_REPORT_COLLECTOR    = headaddressfinal + "ReportTrxCollector/Retrieve";
        LINK_ASK4MONEY_REJECT   = headaddressfinal + "Ask4Money/Decline";

        LINK_INQUIRY_CUST = headaddressfinal + "InquiryCustomer/Retrieve";
        LINK_EXEC_CUST   = headaddressfinal + "ExecCustomer/Invoke";
        LINK_EXEC_AGENT   = headaddressfinal + "ExecAgent/Invoke";

		LINK_REQUEST_CASHOUT    = headaddressfinal + "RequestCashout/Invoke";
        LINK_CONFIRM_CASHOUT    = headaddressfinal + "ConfirmCashout/Invoke";
        LINK_REJECT_CONFIRM_CASHOUT = headaddressfinal + "Rejectatc/Invoke";
        LINK_HELP_PIN           = headaddressfinal + "HelpPIN/Retrieve";

        LINK_INQUIRY_WITHDRAW    = headaddressfinal + "InquiryWithdraw/Retrieve";
        LINK_REQCODE_WITHDRAW    = headaddressfinal + "ReqCodeWithdraw/Invoke";
        LINK_DELTRX_WITHDRAW     = headaddressfinal + "DelWithdrawTrx/Invoke";

        LINK_CREATE_PASS    = headaddressfinal + "CreatePass/Invoke";
        LINK_GET_FAILED_PIN = headaddressfinal + "GetFailedPIN/Retrieve";
        LINK_ATMTOPUP       = headaddressfinal + "ATMTopUp/Retrieve";
        LINK_BANKCASHOUT    = headaddressfinal + "BankCashout/Retrieve";
        LINK_USER_PROFILE   = headaddressfinal + "UserProfile/Retrieve";
        if(BuildConfig.IS_PROD_DOMAIN)
            LINK_INQUIRY_SMS   = "https://mobile.saldomu.com/saldomu/" + "InquirySMS/Retrieve";
//            LINK_INQUIRY_SMS   = "https://mobile.espay.id/hpku/" + "InquirySMS/Retrieve";
        else
            LINK_INQUIRY_SMS   = headaddressfinal + "InquirySMS/Retrieve";
        LINK_CLAIM_TRANSFER_NON_MEMBER = headaddressfinal + "ClaimNonMbrTrf/Invoke";

        LINK_RESEND_TOKEN_LKD       = headaddressfinal + "ResendToken/Invoke";
        LINK_BBS_CITY               = headaddressfinal + "ServiceBBSCity/getAllBBSCity";
//        LINK_GLOBAL_BBS_COMM        = headaddressfinal + "GlobalBBSComm/Retrieve";
//        LINK_GLOBAL_BBS_BANK_C2A    = headaddressfinal + "GlobalBBSBankC2A/Retrieve";
        LINK_GLOBAL_BBS_INSERT_C2A  = headaddressfinal + "GlobalBBSInsertC2A/Invoke";
        LINK_GLOBAL_BBS_INSERT_C2R  = headaddressfinal + "GlobalBBSInsertC2R/Invoke";
//        LINK_BBS_BANK_ACCOUNT       = headaddressfinal + "BBSBankAccount/Retrieve";
        LINK_BBS_BANK_ACCOUNT_DELETE = headaddressfinal + "DelBBSBankAcct/Invoke";
        LINK_BBS_BANK_REG_ACCT      = headaddressfinal + "BBSBankRegAcct/Retrieve";
        LINK_BBS_CONFIRM_ACCT       = headaddressfinal + "BBSConfirmAcct/Invoke";
        LINK_BBS_JOIN_AGENT         = headaddressfinal + "BBSJoinAgent/Invoke";
        LINK_BBS_REQ_ACCT           = headaddressfinal + "BBSRegAcct/Invoke";
        LINK_BBS_GLOBAL_COMM        = headaddressfinal + "GlobalComm/Retrieve";
        LINK_BBS_BIRTH_PLACE        = headaddressfinal + "ServiceBBSBirthPlace/Retrieve";
        LINK_TRX_STATUS_BBS         = headaddressfinal + "TrxBBSStatus/Retrieve";
//        LINK_GLOBAL_BBS_BANK_A2C    = headaddressfinal + "GlobalBBSBankA2C/Retrieve";
        LINK_GLOBAL_BBS_INSERT_A2C  = headaddressfinal + "GlobalBBSInsertA2C/Invoke";
        LINK_BBS_LIST_MEMBER_A2C    = headaddressfinal + "BBSListMemberATC/Retrieve";
        LINK_BBS_OTP_MEMBER_A2C     = headaddressfinal + "BBSOTPMemberATC/Invoke";
        LINK_BBS_LIST_COMMUNITY_ALL = headaddressfinal + "ListCommunity/Retrieve";
        LINK_INQUIRY_TOKEN_ATC      = headaddressfinal + "InquiryTokenATC/Retrieve";
        LINK_INQUIRY_DATA_ATC       = headaddressfinal + "InquiryDataATC/Retrieve";
        LINK_CANCEL_ATC             = headaddressfinal + "CancelATC/Invoke";
        LINK_GET_LIST_COMMUNITY_SCADM         = headaddressfinal + "scadm/ListCommunity/RetrieveAll";
        LINK_GET_LIST_COMMUNITY_TOPUP_SCADM   = headaddressfinal + "scadm/ListCommunity/RetrieveTopup";
        LINK_GET_LIST_COMMUNITY_DENOM_SCADM   = headaddressfinal + "scadm/ListCommunity/RetrieveDenom";
        LINK_GET_PREVIEW_COMMUNITY_SCADM      = headaddressfinal + "scadm/JoinCommunity/Preview";
        LINK_CONFIRM_COMMUNITY_SCADM          = headaddressfinal + "scadm/JoinCommunity/Save";
        LINK_GET_LIST_BANK_TOPUP_SCADM        = headaddressfinal +"scadm/ListBank/Topup";
        LINK_CONFIRM_TOPUP_SCADM              = headaddressfinal +"scadm/Topup/Invoke";
        LINK_CONFIRM_TOPUP_SCADM_NEW          = headaddressfinal +"scadm/ReqTopup/Invoke";
        LINK_GET_LIST_BANK_DENOM_SCADM        = headaddressfinal +"scadm/ListBank/Denom";
        LINK_GET_DENOM_LIST         = headaddressfinal +"scadm/ListDenom/Retrieve";
        LINK_GET_DENOM_INVOKE       = headaddressfinal +"scadm/Denom/Invoke";
        LINK_GET_DENOM_INVOKE_NEW   = headaddressfinal +"scadm/ReqDenom/Invoke";
        LINK_BBS_SEND_DATA_LKD      = headaddressfinal +"BBSTrxCustomer/Submit";
        LINK_BBS_MANDIRI_LKD        = headaddressfinal +"RegAgentLKD/Invoke";

        LINK_REG_TOKEN_FCM = urlMNotif + "user/register";
//        LINK_REG_TOKEN_FCM = urlMNotif + "sendnotification/invoke";

        String googleMapsKey = getmContext().getString(R.string.google_maps_key_ws);
        LINK_GOOGLE_MAPS_API_GEOCODE = "https://maps.google.com/maps/api/geocode/json?sensor=false&key="+googleMapsKey+"&language=id";
        LINK_GOOGLE_MAPS_API_GEOCODE_BASE = "https://maps.google.com/maps/api/geocode/json";

        LINK_REQ_CHANGE_EMAIL       = headaddressfinal + "ReqChangeEmail/Invoke";
        LINK_CONFIRM_CHANGE_EMAIL   = headaddressfinal + "ConfirmChangeEmail/Invoke";

        //tagih
        LINK_LIST_INVOICE_DGI       = headaddressfinal +"invoice/Listinv/Retrieve";
        LINK_REQ_TOKEN_INVOICE_DGI  = headaddressfinal +"invoice/ReqToken/Retrieve";
        LINK_CANCEL_TRANSACTION_DGI = headaddressfinal + "invoice/Canceltrx/Invoke";
        LINK_CONFIRM_PAYMENT_DGI    = headaddressfinal + "invoice/Payment/Invoke";
        LINK_CANCEL_SEARCH_DGI      = headaddressfinal + "invoice/Payment/Reject";
        LINK_SALDO_COLLECTOR        = headaddressfinal + "Balancecollector/Retrieve";
        LINK_SET_MEMBER_LOC         = headaddressfinal + "invoice/Setmemberlocation/Invoke";

        //OTP
        LINK_GET_OTP                = headaddressfinal + "VerifySIMCardByOTP/Retrieve";
        LINK_CONFIRM_OTP            = headaddressfinal + "VerifySIMCardConfirmOTP/Retrieve";

        //SOF
        LINK_INQUIRY_SOF            = headaddressfinal + "InquiryPayment/Invoke";
        LINK_CANCEL_PAYMENT_SOF     = headaddressfinal + "CancelPayment/Invoke";
        LINK_PAY_SOF                = headaddressfinal + "InquiryMerchant/Invoke";
        LINK_FCM                    = headaddressfinal + "RegFcmRef/Invoke";

        LINK_LIST_FILE              = headaddressfinal + "DownloadList/Retrieve";
        LINK_DOWNLOAD_FILE          = headaddressfinal + "DownloadRequest/Invoke";
        LINK_HISTORY                = headaddressfinal + "History/Retrieve";
        LINK_HISTORY_COLLECTOR      = headaddressfinal + "HistoryCollector/Retrieve";

        LINK_TRX_FAVORITE_LIST      = headaddressfinal + "TrxFavorite/List";
        LINK_TRX_FAVORITE_SAVE      = headaddressfinal + "TrxFavorite/Save";
        LINK_TRX_FAVORITE_SAVE_TRF  = headaddressfinal + "TrxFavorite/SaveTrf";
        LINK_TRX_FAVORITE_DELETE    = headaddressfinal + "TrxFavorite/Delete";

        LINK_SEARCH_MEMBER          = headaddressfinal + "CheckMemberID/Retrieve";
        LINK_EXEC_UPGRADE_MEMBER    = headaddressfinal + "ExecUpgradeMember/Invoke";
        LINK_REQ_UPGRADE_MEMBER     = headaddressfinal + "InitiateUpgradeGold/Invoke";

        LINK_GET_BILLER_DENOM       = headaddressfinal + "BillerEspayNew/Retrieve";

        LINK_GET_ANCHOR_COMMUNITIES = headaddressfinal + "invoice/Getanchor/Communities";

        LINK_INQUIRY_CUSTOMER_ACCT  = headaddressfinal + "InquiryCustomerAcct/Invoke";

        LINK_CONFIRM_TOKEN_C2R      = headaddressfinal + "ConfirmTokenCTR/Invoke";
        LINK_RESEND_TOKEN_C2R       = headaddressfinal + "ResendTokenCTR/Invoke";
//        getInstance().syncHttpClient.setTimeout(TIMEOUT);
////        if(PROD_FLAG_ADDRESS)
//            getInstance().syncHttpClient.setSSLSocketFactory(getSSLSocketFactory());
//        getInstance().syncHttpClient.setMaxRetriesAndTimeout(2, 10000);
//
//        getInstance().asyncHttpClient.setTimeout(TIMEOUT);
////        if(PROD_FLAG_ADDRESS)
//            getInstance().asyncHttpClient.setSSLSocketFactory(getSSLSocketFactory());
//        getInstance().asyncHttpClient.setMaxRetriesAndTimeout(2, 10000);
//
//        getInstance().asyncHttpClient_google.setTimeout(TIMEOUT);
//        getInstance().asyncHttpClient_google.setMaxRetriesAndTimeout(2, 10000);
//        getInstance().syncHttpClient_google.setTimeout(TIMEOUT);
//        getInstance().syncHttpClient_google.setMaxRetriesAndTimeout(2, 10000);
//
//        //untrusted asynchttp
//        getInstance().asyncHttpClientUnstrusted.setTimeout(TIMEOUT);
//        if(PROD_FLAG_ADDRESS)
//            getInstance().asyncHttpClientUnstrusted.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
//        getInstance().asyncHttpClientUnstrusted.setMaxRetriesAndTimeout(2, 10000);
    }

    public HashMap<String, Object> googleQuery() {
        HashMap<String, Object> query = new HashMap<>();
        query.put("sensor", false);
        query.put("key", getmContext().getString(R.string.google_maps_key_ws));
        query.put("language", "id");
        return query;
    }

    public static HashMap<String, Object> googleDestination() {
        HashMap<String, Object> query = new HashMap<>();
        query.put("sensor", false);
        query.put("units", "metric");
        query.put("mode", DefineValue.GMAP_MODE);
        query.put("language", Locale.getDefault().getLanguage());
        query.put("key", getInstance().getmContext().getString(R.string.google_maps_key));
        return query;
    }

    public static String URL_HELP_DEV = "https://mobile-dev.saldomu.com/static/pages/help/";
    public static String URL_FAQ;
    public static String URL_FAQ_PROD           = "https://mobile.saldomu.com/static/pages/help/pin_faq_saldomu.html";
    public static String URL_FAQ_DEV            = URL_HELP_DEV +"pin_faq_saldomu.html";

    public static String URL_TERMS;
    public static String URL_TERMS_PROD         = "https://mobile.saldomu.com/static/pages/help/pin_terms_conditions_id_saldomu.html";
    public static String URL_TERMS_DEV          = URL_HELP_DEV +"pin_terms_conditions_id_saldomu.html";

    public static String LINK_SEARCH_AGENT_UPG  = headaodaddressfinal + "SearchAgentUpgrade/Retrieve";
    public static String LINK_SEARCH_AGENT      = headaodaddressfinal + "Search/NearestAgent";
    public static String LINK_CATEGORY_LIST     = headaodaddressfinal + "Category/Retrieve";
    public static String LINK_MEMBER_SHOP_LIST  = headaodaddressfinal + "Membershop/Retrieve";
    public static String LINK_MEMBER_SHOP_DETAIL        = headaodaddressfinal + "Membershop/Detailmember";
    public static String LINK_UPDATE_MEMBER_LOCATION    = headaodaddressfinal + "Manage/UpdateMemberLocation";
    public static String LINK_REGISTER_CATEGORY_SHOP    = headaodaddressfinal + "Category/Registercategoryshop";
    public static String LINK_SETUP_OPENING_HOUR        = headaodaddressfinal + "Manage/Insertopenhour";
    public static String LINK_SEARCH_TOKO       = headaodaddressfinal + "Agent/Retrieve";
    public static String LINK_REGISTER_OPEN_CLOSE_TOKO  = headaodaddressfinal + "Membershop/Registeropenclosed";
    public static String LINK_UPDATE_CLOSE_SHOP_TODAY   = headaodaddressfinal + "Manage/UpdateClosedShopToday";
    public static String LINK_GOOGLE_MAP_API_ROUTE      = "https://maps.googleapis.com/maps/api/directions/json";
    public static String LINK_TRANSACTION_AGENT         = headaodaddressfinal + "Transaction/Retrieve";
    public static String LINK_UPDATE_APPROVAL_TRX_AGENT = headaodaddressfinal + "Transaction/Updatetransaction";
    public static String LINK_UPDATE_LOCATION_AGENT     = headaodaddressfinal + "Transaction/Updateagent";
    public static String LINK_UPDATE_LOCATION_MEMBER    = headaodaddressfinal + "Transaction/Updatemember";
    public static String LINK_CHECK_TRANSACTION_MEMBER  = headaodaddressfinal + "Transaction/Checktransaction";
    public static String LINK_CONFIRM_TRANSACTION_MEMBER = headaodaddressfinal + "Transaction/Confirmtransaction";
    public static String LINK_CANCEL_TRANSACTION_MEMBER = headaodaddressfinal + "Transaction/Canceltransaction";
    public static String LINK_UPDATE_LOCATION           = headaodaddressfinal + "Location/Update";
    public static String LINK_BBS_NEW_SEARCH_AGENT      = headaodaddressfinal + "Search/Agent";
    public static String LINK_CONFIRM_TRANSACTION_BY_AGENT = headaodaddressfinal + "Transaction/Confirmtransactionbyagent";
    public static String LINK_TRX_ONPROGRESS_BY_AGENT   = headaodaddressfinal + "Report/Onprogressagent";
    public static String LINK_UPDATE_FEEDBACK           = headaodaddressfinal + "Transaction/Updatefeedback";
    public static String LINK_CANCEL_SEARCH_AGENT       = headaodaddressfinal + "Transaction/Cancelsearchagent";

    private static final int TIMEOUT = 600000; // 200 x 1000 = 3 menit
    public static String FLAG_OTP = "N";
    public static Boolean FLAG_SIG = true;
    public static String COMM_ID_DEV        = "EMOSALDOMU1500439694RS6DD"; //dev
    public static String COMM_ID_TAGIH_DEV  = "TAGIHSALDO1540982049APLC2"; //dev tagih
    public static String COMM_ID_PULSA_DEV  = "DAPMSCADM1458816850U9KR7"; //dev pulsa agent
    public static String COMM_ID_PULSA_PROD = "DAPHAH14992553291VINB"; //prod pulsa agent
    public static String COMM_ID_TAGIH_PROD = "TAGIHSALDO15435070661GSQN"; //prod pulsa agent
    public static String COMM_ID_PROD       = "SALDOMU1503988580RFVBK";  //prod

    public static String INCOMINGSMS_INFOBIP = "+6281350058801";
    public static String INCOMINGSMS_SPRINT = "+6281333332000";

    public static String APP_ID = BuildConfig.APP_ID;
    public static String CCY_VALUE                      = "IDR";
    public static String DEV_MEMBER_ID_PULSA_RETAIL     = "EFENDI1421144347BPFIM";
    public static String PROD_MEMBER_ID_PULSA_RETAIL    = "EFENDI1421205049F0018";
    public static String URL_INFO_HARGA_DEV             = "\"http://192.168.86.55:20080/misc/biller/index/\"";
    public static String URL_INFO_HARGA_PROD            = "https://go.saldomu.com/misc/biller/index/\"";
    public static String domainSgoPlusDev               = "\"https://sandbox-kit.espay.id/\"";
    public static String domainSgoPlusProd              = "\"https://kit.espay.id/\"";
    public static String domainPrivacyPolicy            = "\"http://saldomu.com/index.php/syarat-ketentuan/\"";

    public static UUID getUUID(){
        return UUID.randomUUID();
    }

    public String getAccessKey(){
        return getInstance().sp.getString(DefineValue.ACCESS_KEY,"");
    }

}

