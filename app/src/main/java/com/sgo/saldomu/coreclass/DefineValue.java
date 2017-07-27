package com.sgo.saldomu.coreclass;

/*
  Created by Administrator on 11/6/2015.
 */

import com.sgo.saldomu.BuildConfig;

public class DefineValue {

    //Final Value
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String SEC_PREF_NAME = BuildConfig.APPLICATION_ID+"_pref";
    public static final String INDONESIA = "indonesia";


    //Static Value
    public static String DEVELOPMENT = "development" ;
    public static String PRODUCTION = "production" ;
    public static String language = null;
    public static String VERSION_CODE = "" ;
    public static String VERSION_NAME = "" ;
    public static String TOPUP_IB_TYPE = "1";
    public static String TOPUP_SMS_TYPE = "2";
    public static String TRANSFER_TYPE = "3";
    public static String BIL_PURCHASE_TYPE = "4";
    public static String BIL_PAYMENT_TYPE = "5";
    public static String TOPUP_ACL_TYPE = "6";
    public static String CASHOUT_TUNAI_TYPE = "7";
    public static String YES = "1";
    public static String NO = "0";
    public static String SUCCESS = "S";
    public static String ONRECONCILED = "OR";
    public static String SUSPECT = "SP";
    public static String FAILED = "F";
    public static String IDR = "IDR";
    public static String PAYFRIENDS = "pf";
    public static String BILLER = "bl";
    public static String BILLER_PLN = "pl";
    public static String BILLER_ESPAY = "blep";
    public static String TOPUP = "tp";
    public static String CASHOUT = "ct";
    public static String CASHOUT_TUNAI = "ctt";
    public static String COLLECTION = "cl";
    public static String TRANSACTION = "ts";
    public static String TRANSACTION_ESPAY = "tsesp";
    public static String BIL_TYPE_BUY = "BUY";
    public static String BIL_TYPE_PAY = "PAY";
    public static String BANKLIST_TYPE_ALL = "ALL" ;
    public static String BANKLIST_TYPE_SMS = "SMS" ;
    public static String BANKLIST_TYPE_IB = "IB" ;
    public static String BANKLIST_TYPE_EMO = "EMO" ;
    public static String AUTH_TYPE_PIN = "PIN" ;
    public static String AUTH_TYPE_OTP = "OTP" ;
    public static String TOKEN = "toKen" ;
    public static String EMO = "EMO" ;
    public static String ESPAY = "ESPAY" ;
    public static String ERROR_0002 = "0002" ;
    public static String ERROR_0004 = "0004" ;
    public static String ERROR_0042 = "0042" ;
    public static String ERROR_0018 = "0018" ;
    public static String ERROR_0017 = "0017" ;
    public static String ERROR_0126 = "0126" ;
    public static String ERROR_0127 = "0127" ;
    public static String SCASH = "SCASH" ;
    public static String PRIVATE = "1" ;
    public static String PUBLIC = "2" ;

    public static boolean NOBACK = false;
    public static String NP_DATA_PREF = "NpDataPref"; //Data Login,User Dan flag Register
    public static String FLAG_LOGIN = "flag_login";
    public static String USER_NAME = "userName";
    public static String USERID_PHONE = "userIDPhone";
    public static String MIN_PASS = "minPass";
    public static String MAX_PASS = "maxPass";
    public static String CUST_ID = "custID";
    public static String CUST_NAME = "custNAME";
    public static String CUST_PHONE = "custPhone";
    public static String CUST_EMAIL = "custEmail";
    public static String COMMUNITY_LENGTH = "communityLength";
    public static String BUSS_SCHEME_NAME = "bussSchemeName";
    public static String BUSS_SCHEME_CODE = "bussSchemeCode";
    public static String COMMUNITY_ID = "communityID";
    public static String COMMUNITY_CODE = "communityCode";
    public static String COMMUNITY_NAME = "communityName";
    public static String COMMUNITY_API_KEY = "communityAPIKey";
    public static String MEMBER_CODE = "memberCode";
    public static String MEMBER_ID = "memberID";
    public static String MEMBER_NAME = "memberName";
    public static String ACCESS_KEY = "accessKey";
    public static String TRANSACTION_TYPE = "transactionType";
    public static String TOPUP_TYPE = "topupType";
    public static String CALLBACK_URL = "callBackUrl";
    public static String CALLBACK_URL_TOPUP = "callBackUrlTopup";
    public static String STRING_YES = "Y";
    public static String STRING_NO = "N";
    public static String INTERNET_BANKING = "internetBanking";
    public static String SMS_BANKING = "smsBanking";
    public static String EMONEY = "emoney";
    public static String PULSA = "pulsa";
//    public static String IS_FIRST_TIME = "isFirstTime";
    public static String IS_FIRST = "isFirst";
    public static String BANK_CHANNEL = "bankChannel";
    public static String CONTACT_FIRST_TIME = "contactFirstTime";
    public static String DATE_TIME = "dateTime";
    public static String TX_ID = "txId";
    public static String TX_TYPE = "txType";
    public static String API_KEY = "apiKey";
    public static String API_KEY_TOPUP = "apiKeyTopUp";
    public static String IMG_URL = "imgURL";
    public static String IMG_SMALL_URL = "imgSmallURL";
    public static String IMG_MEDIUM_URL = "imgMediumURL";
    public static String IMG_LARGE_URL = "imgLargeURL";
    public static String PROFILE_DOB = "profileDob";
    public static String PROFILE_SOCIAL_ID = "profileSocialId";
    public static String PROFILE_COUNTRY = "profileCountry";
    public static String PROFILE_BIO = "profileBio";
    public static String PROFILE_BOM = "profileBoM";
    public static String PROFILE_ADDRESS = "profileAddress";
    public static String PROFILE_EMAIL = "profileEmail";
    public static String PROFILE_FULL_NAME = "profileFullName";
    public static String PROFILE_HOBBY = "profileHobby";
    public static String PROFILE_VERIFIED = "profileVerified";
    public static String PROFILE_POB = "profilePOB";
    public static String PROFILE_GENDER = "profileGender";
    public static String PROFILE_ID_TYPE = "profileIDType";
    public static String PREVIOUS_LOGIN_USER_ID = "prevLoginUserId";
    public static String PREVIOUS_BALANCE = "prevBalance";
    public static String PREVIOUS_CONTACT_FIRST_TIME = "prevContactFirstTime";

    public static String BILLER_DATA = "billerData";
    public static String BILLER_NAME = "billerName";
    public static String BILLER_TYPE = "billerType";
    public static String BILLER_ID = "billerID";
    public static String BILLER_ITEM_ID = "billerItemID";
    public static String BILLER_COMM_ID = "billerCommID";
    public static String BILLER_COMM_CODE = "billerCommCode";
    public static String BILLER_API_KEY = "billerAPIKey";
    public static String DENOM_DATA = "denomData";
    public static String RECIPIENTS = "recipients";
    public static String RECIPIENTS_ERROR = "recipientError";
    public static String AMOUNT_EACH = "amountEach";
    public static String AMOUNT = "amount";
    public static String TOTAL_AMOUNT = "totalAmount";
    public static String MESSAGE = "message";
    public static String PAYMENT_NAME = "paymentName";
    public static String IS_SMS_BANKING = "isSMSBanking";
    public static String REPORT_TYPE = "reportType";
    public static String BUY_TYPE = "buyType";
    public static String BUY_TYPE_NAME = "buyTypeName";
    public static String AMOUNT_DESIRED = "amountDesired";
    public static String DESC_FIELD = "isDesField";
    public static String DESC_VALUE = "isDesValue";
    public static String BANK_NAME = "bankName";
    public static String BANK_PRODUCT = "bankProduct";
    public static String FEE = "fee";
    public static String TRX = "trx";
    public static String REQUEST_ID = "requestID";
    public static String TRX_STATUS = "transStatus";
    public static String TRX_MESSAGE = "transMessage";
    public static String TRX_REMARK = "transRemark";
    public static String COUNT = "10";
    public static String TIMELINE_FIRST_TIME = "timelineFirstTime";
    public static String DETAIL = "detail";
    public static String TYPE = "type";
    public static String DESCRIPTION = "description";
    public static String REMARK = "remark";
    public static String AUTHENTICATION_TYPE = "authenticationType";
    public static String LENGTH_AUTH = "lengthAuth";
    public static String IS_HAVE_PIN = "isHavePin";
    public static String IS_SGO_PLUS = "isSGOPlus";
    public static String PIN_CODE = "pinCode";
    public static String PIN_VALUE = "pinValue";
    public static String BANK_CODE = "bankCode";
    public static String PRODUCT_CODE = "productCode";
    public static String PRODUCT_NAME = "productName";
    public static String PRODUCT_TYPE = "productType";
    public static String PRODUCT_H2H = "productH2h";
    public static String BANK_ATM_CODE = "bankATMCode";
    public static String BANK_ATM_NAME = "bankATMName";
    public static String NO_VA = "noVA";
    public static String CCY_ID = "ccyID";
    public static String COLLECTION_DATA = "collectData";
    public static String ITEM_ID = "itemID";
    public static String ITEM_NAME = "itemName";
    public static String IS_INPUT = "isInput";
    public static String IS_DISPLAY = "isDisplay";
    public static String SHARE_TYPE = "shareType";
    public static String PRODUCT_VALUE = "productValue";
    public static String PRODUCT_PAYMENT_TYPE = "productPaymentType";
    public static String BANKLIST_DATA = "banklistData";
    public static String MAX_MEMBER_TRANS = "maxMemberTrans";
    public static String IS_MD5 = "isMD5";
    public static String BILLER_ID_NUMBER = "billerIdNumber";
    public static String DENOM_ITEM_ID = "denomItemID";
    public static String DENOM_ITEM_NAME = "denomItemName";
    public static String PHONE_NUMBER = "phoneNumber";
    public static String PULSA_AGENT = "pa";
    public static String MEMBER_DAP = "memberDAP";
    public static String OPERATOR_ID = "operatorID";
    public static String OPERATOR_NAME = "operatorName";

    public static String IS_FORGOT_PASSWORD = "isForgotPassword";
    public static String MAX_RESEND = "maxResend";
    public static String IS_FACEBOOK = "isFacebook";
    public static String DATA_FACEBOOK = "dataFacebook";
    public static String IS_ACTIVE = "isActive";
    public static String IS_ACTIVITY_FULL = "isActivityFull";
    public static String ACCESS_SECRET = "accessSecret";
    public static String NEW_PASSWORD = "newPassword";
    public static String CONFIRM_PASSWORD = "confirmPassword";
    public static String REGISTRATION = "registration";
    public static String CONTACT_ALIAS = "contactAlias";
    public static String CONF_PIN = "confPin";
    public static String REQUEST = "req";
    public static String TRX_ID = "trxId";
    public static String FROM = "from";
    public static String STATUS = "status";
    public static String REASON = "reason";
	public static String BALANCE_AMOUNT = "balanceAmount";
    public static String BALANCE_MAX_TOPUP = "balanceMaxTopup";
    public static String BALANCE_CCYID = "balaceCcyid";
    public static String BALANCE_REMAIN_LIMIT = "balanceRemainLimit";
    public static String BALANCE_PERIOD_LIMIT = "balancePeriodLimit";
    public static String BALANCE_NEXT_RESET = "balanceNextReset";
    public static String NOTIF_TYPE = "notifType";
    public static String POST_ID = "postID";
    public static String sDefSystemLanguage;

    public static String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static String BR_REGISTRATION_COMPLETE = "brRegistrationComplete";
    public static String LEVEL_VALUE = "levelValue";
    public static String IS_REGISTERED_LEVEL = "isRegisteredLevel";
    public static String LIST_ID_TYPES = "listIDTypes";

	public static String ACCOUNT_NUMBER = "accountNumber";
    public static String NOMINAL = "nominal";

    public static String BANK_CASHOUT = "bankCashout";
    public static String ACCT_NAME = "acctName";
    public static String ACCT_TYPE = "acctType";
    public static String ACCT_NO = "acctNo";
    public static String ACCT_CITY_NAME = "acctCityName";
    public static String ACCT_CITY_CODE = "acctCityCode";

    public static String FROM_NAME = "fromName";
    public static String FROM_ID = "fromID";
    public static String TO_NAME = "toName";
    public static String TO_ID = "toID";
    public static String PROF_PIC = "profPIC";
    public static String TX_STATUS = "txStatus";
    public static String WITH_PROF_PIC = "withProfPic";
    public static String POST_TYPE = "postType";

    public static String LIST_CONTACT_CENTER = "listContactCenter";
    public static String CASHOUT_TYPE = "cashoutType";
    public static int CASHOUT_BANK = 0;
    public static int CASHOUT_AGEN = 1;
    public static String IS_CHANGED_PASS = "isChangePass";
    public static String ATTEMPT = "attempt";
    public static String OTP_MEMBER = "otpMember";
    public static String DATA = "data";
    public static String ID_ADMIN = "idAdmin";
    public static String NAME_ADMIN = "nameAdmin";
    public static String CASH_OUT = "cash out";
    public static String MAX_TOPUP = "max_topup";
    public static String ALLOW_MEMBER_LEVEL = "allowMemberLevel";
    public static String CAN_TRANSFER = "canTransfer";
    public static String TYPE_POST = "typePost";

    public static String DESTINATION_REMARK = "destinationRemark";
    public static String LINK_APP = "linkApp";

    public static String SENDER_ID = "SenderID";
    public static String DEIMEI = "deimei";
    public static String DEICCID = "deiccid";

    public static String WAITING_CLAIM = "WC";
    public static String USER_IS_NEW = "userIsNew";

    public static String INDEX = "index";
    public static String BILLER_TYPE_BPJS = "bpjs";
    public static String BILLER_TYPE_PLN_TKN = "tkn";
    public static String BILLER_TYPE_NON_TAG = "non";
    public static String BILLER_TYPE_PLN = "pln";
    public static String VALUE_ITEM_DATA = "valueItemData";
    public static String IS_SHOW_DESCRIPTION = "isDescription";
    public static String IS_PLN = "isPLN";

    public static String SHOP_ID = "shop_id";
    public static String CLOSED_TYPE_NONE   = "NONE";
    public static String CLOSED_TYPE_DATE   = "DATE";
    public static String CLOSED_TYPE_DAY    = "DAY";

    public static String LAST_CURRENT_LATITUDE      = "lastCurrentLatitude";
    public static String LAST_CURRENT_LONGITUDE     = "lastCurrentLongitude";

    public static String SHOP_MERCHANT  = "M";
    public static String SHOP_AGENT     = "A";

    public static String SHOP_OPEN      = "O";
    public static String SHOP_CLOSE     = "C";

    public static String CATEGORY_ID    = "category_id";
    public static String CATEGORY_NAME  = "category_name";
    public static String CATEGORY_CODE  = "category_code";

    //GOOGLE MAP DEFINE PARAMETERS
    public static float ZOOM_CAMERA_POSITION                = 14.0f; //16.0f
    public static int REQUEST_CODE_RECOVER_PLAY_SERVICES    = 200;
    public static long INTERVAL_LOCATION_REQUEST            = 1000; //15 seconds
    public static long FASTEST_INTERVAL_LOCATION_REQUEST    = 1000; //10 seconds
    public static int DISPLACEMENT                          = 1;
    public static int REQUEST_CODE_AUTOCOMPLETE             = 1;

    public static long AGENT_INTERVAL_LOCATION_REQUEST              = 1000; //60 seconds
    public static long AGENT_FASTEST_INTERVAL_LOCATION_REQUEST      = 1000; //58 seconds
    public static int AGENT_DISPLACEMENT                            = 1; //30meter

    public static long MEMBER_INTERVAL_LOCATION_REQUEST             = 1000; //60 seconds
    public static long MEMBER_FASTEST_INTERVAL_LOCATION_REQUEST     = 1000; //58 seconds
    public static int MEMBER_DISPLACEMENT                           = 1; //30meter

    public static String DEFAULT_LANGUAGE_CODE              = "id";
    public static String GMAP_MODE                          = "driving";
	public static String RECEIVER_ID						= "GOMOBILE";

    public static String IS_AGENT = "isAgent";

    public static String IS_BBS = "isBBS";
    //BBS
    public static String BBS_COMM_ATC = "ATC";
    public static String IS_JOIN_AGENT = "isJoinAgent";
    public static String ATC = "ATC";
    public static String CTA = "CTA";
    public static String IS_UPDATE = "isUpdate";
    public static String BENEF_CITY = "benefCity";
    public static String BENEF_CITY_CODE = "benefCityCode";
    public static String BANK_BENEF = "bankBenef";
    public static String NAME_BENEF = "nameBenef";
    public static String NO_BENEF = "noBenef";
    public static String TYPE_BENEF = "typeBenef";
    public static String NO_HP_BENEF = "noHPBenef";
    public static String SOURCE_ACCT = "sourceAcct";
    public static String SOURCE_ACCT_NO = "sourceAcctNo";
    public static String SOURCE_ACCT_NAME = "sourceAcctName";
    public static String BBS = "bbs";
    public static String BBS_CASHIN = "bbsCashin";
    public static String BBS_MEMBER_OTP = "bbsMemberOtp";
    public static String ACCT = "ACCT";
    public static String USER_ID = "userId";
    public static String BBS_CASHOUT = "bbsCashout";
    public static String SOURCE_PRODUCT_CODE = "sourceProductCode";
    public static String SOURCE_PRODUCT_TYPE = "sourceProductType";
    public static String SOURCE_PRODUCT_H2H = "sourceProductH2h";
    public static String SOURCE_PRODUCT_NAME = "sourceProductName";
    public static String BENEF_PRODUCT_CODE = "benefProductCode";
    public static String BENEF_PRODUCT_TYPE = "benefProductType";
    public static String BENEF_PRODUCT_NAME = "benefProductName";

    public static String BBS_SENDER_ID							= "GOAPK";
    public static String BBS_RECEIVER_ID						= "GOMOBILE";
    public static String BBS_AGENT_MOBILITY                     = "mobility";

    public static String TX_ID2             = "tx_id";
    public static String KEY_CODE           = "key_code";
    public static String KEY_NAME           = "key_name";
    public static String KEY_ADDRESS        = "key_address";
    public static String KEY_DISTRICT       = "key_district";
    public static String KEY_PROVINCE       = "key_province";
    public static String KEY_COUNTRY        = "key_country";
    public static String KEY_CCY            = "key_ccy";
    public static String KEY_AMOUNT         = "key_amount";
    public static String KEY_LATITUDE       = "key_latitude";
    public static String KEY_LONGITUDE      = "key_longitude";
    public static String KEY_TX_STATUS      = "tx_status";
    public static String CATEGORY_SCHEME_CODE   = "scheme_code";

    public static String STRING_ACCEPT          = "S";
    public static String STRING_CANCEL          = "F";

    public static String BBS_TX_ID          = "BbsTxId";
    public static String BBS_MEMBER_ID      = "BbsMemberId";
    public static String BBS_SHOP_ID        = "BbsShopId";
    public static String LAST_LATITUDE      = "LastLatitude";
    public static String LAST_LONGITUDE     = "LastLongitude";
    public static String AGENT_LATITUDE         = "AgentLatitude";
    public static String AGENT_LONGITUDE        = "AgentLongitude";
    public static String MEMBER_LATITUDE        = "MemberLatitude";
    public static String MEMBER_LONGITUDE       = "MemberLongitude";
    public static String BENEF_LATITUDE         = "BenefLatitude";
    public static String BENEF_LONGITUDE        = "BenefLongitude";
    public static String TX_STATUS_OP           = "OP";
    public static String TX_STATUS_RJ           = "RJ";
    public static int IDX_CATEGORY_SEARCH_AGENT = 1111;
    public static String MSG_NOTIF              = "MsgNotif";

    public static String DEFAULT_RADIUS         = "10";

    public static String TUTORIAL_CASHIN             = "tutorialCashIn";
    public static String TUTORIAL_IMAGE            = "tutorialImage";
    public static String TUTORIAL_TAMBAH_REKENING        = "tutorialDaftarRekening";
    public static String TUTORIAL_CASHOUT              = "tutorialCashOut";
    public static String TUTORIAL_KONFIRMASI_CASHOUT_BBS              = "tutorialKonfirmasiCashOutBBS";
    public static String TUTORIAL_REGISTER_AGEN             = "tutorialRegisterAgen";
    public static String TUTORIAL_KELOLA_AGENT            = "tutorialKelolaAgent";


}

