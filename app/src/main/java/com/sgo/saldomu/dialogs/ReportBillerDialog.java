package com.sgo.saldomu.dialogs;/*
  Created by Administrator on 3/6/2015.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BluetoothPrinter.zj.BluetoothService;
import com.sgo.saldomu.BluetoothPrinter.zj.DevicesList;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JsonSorting;
import com.sgo.saldomu.coreclass.LevelClass;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.ViewToBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class ReportBillerDialog extends DialogFragment implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    public static final String TAG = "reportBiller Dialog";
    private static final int RC_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    private static final int RC_REQUEST_WRITE_EXTERNAL_STORAGE_AND_PRINT = 112;
    private static final String SALDO_AGEN = "SALDO AGEN";

    private OnDialogOkCallback callback;
    private Boolean isActivity = false;
    private String trx_id, buss_scheme_code, type, imgFilename;
    private ViewToBitmap viewToBitmap;
    private LinearLayout contentInvoice;
    private TableLayout mTableLayout;
    private ImageView saveimage;
    private ImageView shareimage;
    private ImageView printStruk;
    private static final int recCodeShareImage = 11;
    private static final int recCodeSaveImage = 12;

    private LevelClass levelClass;
    private Boolean isAgent;
    SecurePreferences sp;

    Bundle args;

    byte FONT_TYPE;
    private static BluetoothSocket btsocket;
    private static OutputStream outputStream;

    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services
    private BluetoothService mService = null;
    private BluetoothDevice btDevice = null;

    private int maxRetry = 10;
    private int countRetry = 0;
    private int timeDelayed = 3000;
    // Init
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mService != null && btDevice != null) {
                //mService.stop();
                //mService = new BluetoothService(getContext(), mHandler);
                mService.connect(btDevice);
                countRetry++;
            }
            Log.d("Run Thread : ", "printbluetooth");

            //yessi, dibawah ini tuk cek max try berapa kali
            //if ( countRetry < maxRetry )
            handler.postDelayed(this, timeDelayed);
        }
    };

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            //case RC_LOCATION_PERM:
            case RC_REQUEST_WRITE_EXTERNAL_STORAGE_AND_PRINT:
                connect();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    public interface OnDialogOkCallback {
        void onOkButton();
    }

    public static ReportBillerDialog newInstance(OnDialogOkCallback listener) {
        ReportBillerDialog f = new ReportBillerDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(DefineValue.IS_ACTIVE, true);
        f.callback = listener;
        f.setArguments(bundle);
        return f;
    }

    public ReportBillerDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.isActivity = getArguments().getBoolean(DefineValue.IS_ACTIVE, false);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

//        try {
//            if (isActivity)
//                callback = (OnDialogOkCallback) getActivity();
//            else
//                callback = (OnDialogOkCallback) getTargetFragment();
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
//        }

        if (viewToBitmap == null)
            viewToBitmap = new ViewToBitmap(getContext());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.dismiss();
        callback.onOkButton();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_report_biller, container);
        ViewStub stub = view.findViewById(R.id.stub);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        levelClass = new LevelClass(getActivity(), sp);
        isAgent = sp.getBoolean(DefineValue.IS_AGENT, false);

        args = getArguments();
        Timber.d("isi args report:" + args.toString());

        type = args.getString(DefineValue.REPORT_TYPE, "");
        buss_scheme_code = args.getString(DefineValue.BUSS_SCHEME_CODE);

        LinearLayout layout_txId = view.findViewById(R.id.layout_tx_id);
        TextView tv_date_value = view.findViewById(R.id.dialog_reportbiller_date_time);
        TextView tv_txid_value = view.findViewById(R.id.dialog_reportbiller_tx_id);
        TextView tv_trans_remark = view.findViewById(R.id.dialog_report_transaction_remark);
        TextView tv_trans_remark_sub = view.findViewById(R.id.dialog_report_transaction_remark_sub);
        TextView tv_trans_remark_topup = view.findViewById(R.id.dialog_report_transaction_remark_topup);

        if (buss_scheme_code.equals(DefineValue.P2P) || type.equals(DefineValue.PAYFRIENDS)) {
            layout_txId.setVisibility(View.GONE);
        }


        if (buss_scheme_code.equalsIgnoreCase(DefineValue.TOPUP_B2B)) {
            tv_trans_remark_topup.setVisibility(View.VISIBLE);
        }

        tv_date_value.setText(args.getString(DefineValue.DATE_TIME));
        tv_txid_value.setText(args.getString(DefineValue.TX_ID));

        trx_id = args.getString(DefineValue.TX_ID);

        if (buss_scheme_code != null || type != null) {
            if (buss_scheme_code.equalsIgnoreCase(DefineValue.CTA)) {
                if (type.equals(DefineValue.BBS_CASHIN)) {
                    stub.setLayoutResource(R.layout.layout_dialog_report_bbs_cashin);
                    View inflated = stub.inflate();
                    inflated.setVisibility(View.VISIBLE);

                    TextView tvNoDestination = inflated.findViewById(R.id.tvNoDestination);

                    String benef_type = args.getString(DefineValue.TYPE_BENEF, "");
                    String benef_product_code = args.getString(DefineValue.BENEF_PRODUCT_CODE, "");
                    String benef_bank_name = args.getString(DefineValue.BANK_BENEF, "");
                    if (benef_type.equalsIgnoreCase(DefineValue.ACCT) || benef_product_code.equalsIgnoreCase("MANDIRILKD") || benef_bank_name.equalsIgnoreCase("Mandiri Laku Pandai"))
                        tvNoDestination.setText(R.string.number_destination);
                    else
                        tvNoDestination.setText(R.string.number_hp_destination);

                    Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                    tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                    if (!isSuccess) {
                        String transRemark = args.getString(DefineValue.TRX_REMARK);
                        tv_trans_remark_sub.setVisibility(View.VISIBLE);
                        tv_trans_remark_sub.setText(transRemark);
                    }
                    TextView tv_transaction_type = inflated.findViewById(R.id.tv_report_transaction_type);
                    TextView tv_useerid_value = inflated.findViewById(R.id.dialog_reportbbs_userid_value);
                    TextView tv_name_value = inflated.findViewById(R.id.dialog_reportbbs_membername_value);
                    TextView tv_member_shop_phone = inflated.findViewById(R.id.dialog_reportbs_member_shop_phone);
                    TextView tv_source_acc_name_value = inflated.findViewById(R.id.dialog_reportbs_bank_source_acc_name_value);
                    TextView tv_benef_bank_name_value = inflated.findViewById(R.id.dialog_reportbbs_benef_bank_value);
                    TextView tv_benef_acc_no_value = inflated.findViewById(R.id.dialog_reportbbs_bank_benef_acct_no_value);
                    TextView tv_benef_acc_name_value = inflated.findViewById(R.id.dialog_reportbs_bank_benef_acc_name_value);
                    TextView tv_amount_value = inflated.findViewById(R.id.dialog_reportbbs_amount_value);
                    TextView tv_fee_value = inflated.findViewById(R.id.dialog_reportbbs_fee_value);
                    TextView tv_total_amount_value = inflated.findViewById(R.id.dialog_reportbbs_totalamount_value);
                    TextView tv_produk_agent = inflated.findViewById(R.id.tv_produk_agen);
//                    TextView tv_additionalFee = inflated.findViewById(R.id.dialog_reportbbs_additionalFee);
                    View v_produk_agent = inflated.findViewById(R.id.view_produkAgen);

                    if (args.getBoolean(DefineValue.IS_MEMBER_CTA) == true) {
                        tv_produk_agent.setVisibility(View.GONE);
                        tv_source_acc_name_value.setVisibility(View.GONE);
                        v_produk_agent.setVisibility(View.GONE);
                    }

                    tv_transaction_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                    tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                    tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                    tv_source_acc_name_value.setText(args.getString(DefineValue.PRODUCT_NAME));
                    tv_member_shop_phone.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));
                    tv_benef_acc_no_value.setText(args.getString(DefineValue.NO_BENEF));
                    tv_benef_acc_name_value.setText(args.getString(DefineValue.NAME_BENEF));
                    String bankBenef = args.getString(DefineValue.BANK_BENEF);
                    if (bankBenef != null && bankBenef.toLowerCase().contains("saldomu"))
                        bankBenef = SALDO_AGEN;
                    tv_benef_bank_name_value.setText(bankBenef);
                    tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                    tv_fee_value.setText(args.getString(DefineValue.FEE));
//                    tv_additionalFee.setText(args.getString(DefineValue.ADDITIONAL_FEE));
                    tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                }
            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.CTR)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_ctr);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tvNoDestination = inflated.findViewById(R.id.tvNoDestination);

                String benef_type = args.getString(DefineValue.TYPE_BENEF, "");
                String benef_product_code = args.getString(DefineValue.BENEF_PRODUCT_CODE, "");
                String benef_bank_name = args.getString(DefineValue.BANK_BENEF, "");
                if (benef_type.equalsIgnoreCase(DefineValue.ACCT) || benef_product_code.equalsIgnoreCase("MANDIRILKD") || benef_bank_name.equalsIgnoreCase("Mandiri Laku Pandai"))
                    tvNoDestination.setText(R.string.number_destination);
                else
                    tvNoDestination.setText(R.string.number_hp_destination);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
                TextView tv_transaction_type = inflated.findViewById(R.id.tv_report_transaction_type);
                TextView tv_useerid_value = inflated.findViewById(R.id.dialog_reportbbs_userid_value);
                TextView tv_name_value = inflated.findViewById(R.id.dialog_reportbbs_membername_value);
                TextView tv_member_shop_phone = inflated.findViewById(R.id.dialog_reportbs_member_shop_phone);
                TextView tv_source_acc_name_value = inflated.findViewById(R.id.dialog_reportbs_bank_source_acc_name_value);
                TextView tv_benef_bank_name_value = inflated.findViewById(R.id.dialog_reportbbs_benef_bank_value);
                TextView tv_benef_acc_no_value = inflated.findViewById(R.id.dialog_reportbbs_bank_benef_acct_no_value);
                TextView tv_benef_acc_name_value = inflated.findViewById(R.id.dialog_reportbs_bank_benef_acc_name_value);
                TextView tv_amount_value = inflated.findViewById(R.id.dialog_reportbbs_amount_value);
                TextView tv_fee_value = inflated.findViewById(R.id.dialog_reportbbs_fee_value);
                TextView tv_total_amount_value = inflated.findViewById(R.id.dialog_reportbbs_totalamount_value);
                TextView tv_cust_name = inflated.findViewById(R.id.dialog_reportbbs_cust_name);
                TextView tv_cust_phone = inflated.findViewById(R.id.dialog_reportbbs_benef_cust_phone);

                tv_transaction_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_source_acc_name_value.setText(args.getString(DefineValue.PRODUCT_NAME));
                tv_member_shop_phone.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));
                tv_benef_acc_no_value.setText(args.getString(DefineValue.NO_BENEF));
                tv_benef_acc_name_value.setText(args.getString(DefineValue.NAME_BENEF));
                String bankBenef = args.getString(DefineValue.BANK_BENEF);
                if (bankBenef != null && bankBenef.toLowerCase().contains("saldomu"))
                    bankBenef = SALDO_AGEN;
                tv_benef_bank_name_value.setText(bankBenef);
                tv_cust_name.setText(args.getString(DefineValue.MEMBER_SHOP_NAME));
                tv_cust_phone.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.ATC)) {
                if (type.equals(DefineValue.BBS_MEMBER_OTP)) {
                    View inflated;
                    stub.setLayoutResource(R.layout.layout_dialog_report_bbs_member_confirm);
                    inflated = stub.inflate();
                    inflated.setVisibility(View.VISIBLE);

                    TextView tv_transaction_type = inflated.findViewById(R.id.tv_report_transaction_type);
                    TextView tv_userid_value = inflated.findViewById(R.id.dialog_reportbbs_userid_value);
                    TextView tv_name_value = inflated.findViewById(R.id.dialog_reportbbs_membername_value);
                    TextView tv_token_value = inflated.findViewById(R.id.dialog_reportbbs_token_value);
                    TextView tv_kode = inflated.findViewById(R.id.tv_kode);
                    TextView tv_source_bank_name_value = inflated.findViewById(R.id.dialog_reportbbs_source_bank_value);
                    TextView tv_source_acc_no_value = inflated.findViewById(R.id.dialog_reportbbs_bank_source_acct_no_value);
                    TextView tv_source_acc_name_value = inflated.findViewById(R.id.dialog_reportbs_bank_source_acc_name_value);
                    TextView tv_amount_value = inflated.findViewById(R.id.dialog_reportbbs_amount_value);
                    TextView tv_fee_value = inflated.findViewById(R.id.dialog_reportbbs_fee_value);
//                    TextView tv_additionalfee_value = inflated.findViewById(R.id.dialog_reportbbs_additionalfee_value);
                    TextView tv_total_amount_value = inflated.findViewById(R.id.dialog_reportbbs_totalamount_value);
                    TextView tv_member_shop_phone = inflated.findViewById(R.id.dialog_reportbbs_member_shop_phone);
                    Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                    tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                    if (!isSuccess) {
                        String transRemark = args.getString(DefineValue.TRX_REMARK);
                        tv_trans_remark_sub.setVisibility(View.VISIBLE);
                        tv_trans_remark_sub.setText(transRemark);
                    }
                    tv_transaction_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                    tv_userid_value.setText(args.getString(DefineValue.MEMBER_PHONE));
                    tv_name_value.setText(args.getString(DefineValue.MEMBER_NAME));
                    tv_token_value.setText(args.getString(DefineValue.OTP_MEMBER));
                    if (args.getBoolean(DefineValue.IS_REPORT) == false && !args.getString(DefineValue.OTP_MEMBER).isEmpty()) {
                        tv_kode.setVisibility(View.VISIBLE);
                        tv_token_value.setVisibility(View.VISIBLE);
                    } else if (args.getBoolean(DefineValue.IS_REPORT) == true && !args.getString(DefineValue.TOKEN_ID).isEmpty()) {
                        tv_kode.setVisibility(View.VISIBLE);
                        tv_token_value.setVisibility(View.VISIBLE);
                        tv_token_value.setText(args.getString(DefineValue.TOKEN_ID));
                    }
                    tv_source_bank_name_value.setText(args.getString(DefineValue.SOURCE_ACCT));
                    tv_source_acc_no_value.setText(args.getString(DefineValue.MEMBER_SHOP_NO));
                    tv_source_acc_name_value.setText(args.getString(DefineValue.SOURCE_ACCT_NAME));
                    tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                    tv_fee_value.setText(args.getString(DefineValue.FEE));
//                    tv_additionalfee_value.setText(args.getString(DefineValue.ADDITIONAL_FEE));
                    tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                    tv_member_shop_phone.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));


                } else if (type.equalsIgnoreCase(DefineValue.BBS_CASHOUT)) {

                    stub.setLayoutResource(R.layout.layout_dialog_report_bbs_cashout);
                    View inflated = stub.inflate();

                    inflated.setVisibility(View.VISIBLE);
                    Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                    tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                    if (!isSuccess) {
                        String transRemark = args.getString(DefineValue.TRX_REMARK);
                        tv_trans_remark_sub.setVisibility(View.VISIBLE);
                        tv_trans_remark_sub.setText(transRemark);
                    }
                    TextView tv_transaction_type = inflated.findViewById(R.id.tv_report_transaction_type);
                    TextView tv_useerid_value = inflated.findViewById(R.id.dialog_reportbbs_userid_value);
                    TextView tv_name_value = inflated.findViewById(R.id.dialog_reportbbs_membername_value);
                    TextView tv_member_shop_phone = inflated.findViewById(R.id.dialog_reportbbs_source_phone);
                    TextView tv_source_bank_name_value = inflated.findViewById(R.id.dialog_reportbbs_source_bank_value);
                    TextView tv_source_acc_no_value = inflated.findViewById(R.id.dialog_reportbbs_bank_source_acct_no_value);
                    TextView tv_source_acc_name_value = inflated.findViewById(R.id.dialog_reportbs_bank_source_acc_name_value);
                    TextView tv_benef_bank_name_value = inflated.findViewById(R.id.dialog_reportbbs_benef_bank_value);
                    TextView tv_amount_value = inflated.findViewById(R.id.dialog_reportbbs_amount_value);
                    TextView tv_fee_value = inflated.findViewById(R.id.dialog_reportbbs_fee_value);
//                    TextView tv_additionalFee = inflated.findViewById(R.id.dialog_reportbbs_additionalFee_value);
                    TextView tv_total_amount_value = inflated.findViewById(R.id.dialog_reportbbs_totalamount_value);

                    tv_transaction_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                    tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                    tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                    String bankBenef = args.getString(DefineValue.BANK_BENEF);
                    if (bankBenef != null && bankBenef.toLowerCase().contains("saldomu"))
                        bankBenef = SALDO_AGEN;
                    tv_benef_bank_name_value.setText(bankBenef);
                    tv_member_shop_phone.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));
                    tv_source_bank_name_value.setText(args.getString(DefineValue.SOURCE_ACCT));

                    tv_source_acc_no_value.setText(args.getString(DefineValue.MEMBER_SHOP_NO));
                    tv_source_acc_name_value.setText(args.getString(DefineValue.MEMBER_SHOP_NAME));

                    tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                    tv_fee_value.setText(args.getString(DefineValue.FEE));
//                    tv_additionalFee.setText(args.getString(DefineValue.ADDITIONAL_FEE));
                    tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                }
            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.TOPUP_B2B)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_topup_b2b);
                View inflated = stub.inflate();

                TextView tv_report_type = inflated.findViewById(R.id.dialog_topup_transaction_type);
                TextView tv_useerid_value = inflated.findViewById(R.id.dialog_topup_commcode_value);
                TextView tv_store_code_value = inflated.findViewById(R.id.dialog_topup_storecode_value);
                TextView tv_bank_product = inflated.findViewById(R.id.dialog_topup_productbank_value);
                TextView tv_fee = inflated.findViewById(R.id.dialog_topup_fee_value);
                TextView tv_amount = inflated.findViewById(R.id.dialog_topup_amount_value);
                TextView tv_total_amount = inflated.findViewById(R.id.dialog_topup_total_amount_value);
                TextView tv_agent_name = inflated.findViewById(R.id.dialog_topup_agent_name_value);
                TextView tv_agent_number = inflated.findViewById(R.id.dialog_topup_agent_number_value);
                TextView tv_store_name = inflated.findViewById(R.id.dialog_topup_store_name_value);
                TextView tv_store_address = inflated.findViewById(R.id.dialog_topup_store_address_value);
                inflated.setVisibility(View.VISIBLE);

                String amount = args.getString(DefineValue.AMOUNT);
                String fee = args.getString(DefineValue.FEE);
                String total_amount = args.getString(DefineValue.TOTAL_AMOUNT);
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);

                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_bank_product.setText(args.getString(DefineValue.BANK_PRODUCT));
                tv_fee.setText(fee);
                tv_amount.setText(amount);
                tv_total_amount.setText(total_amount);
                tv_useerid_value.setText(args.getString(DefineValue.COMMUNITY_CODE));
                tv_store_code_value.setText(args.getString(DefineValue.STORE_CODE));
                tv_agent_name.setText(args.getString(DefineValue.MEMBER_CUST_NAME));
                tv_agent_number.setText(args.getString(DefineValue.MEMBER_ID_CUST));
                tv_store_name.setText(args.getString(DefineValue.STORE_NAME));
                tv_store_address.setText(args.getString(DefineValue.STORE_ADDRESS));

            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.EMO)) {
//                if (type.equals(DefineValue.TOPUP) || type.equals(DefineValue.COLLECTION)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_topup);
                View inflated = stub.inflate();

                TextView tv_report_type = inflated.findViewById(R.id.dialog_topup_transaction_type);
                TextView tv_user_id = inflated.findViewById(R.id.tv_user_id);
                TextView tv_nama = inflated.findViewById(R.id.tv_name);
                TextView tv_useerid_value = inflated.findViewById(R.id.dialog_topup_userid_value);
                TextView tv_name_value = inflated.findViewById(R.id.dialog_topup_name_value);
                TextView tv_bank_product = inflated.findViewById(R.id.dialog_topup_productbank_value);
                TextView tv_fee = inflated.findViewById(R.id.dialog_topup_fee_value);
                TextView tv_amount = inflated.findViewById(R.id.dialog_topup_amount_value);
                TextView tv_total_amount = inflated.findViewById(R.id.dialog_topup_total_amount_value);
                inflated.setVisibility(View.VISIBLE);

                String amount = args.getString(DefineValue.AMOUNT);
                String fee = args.getString(DefineValue.FEE);
                String total_amount = args.getString(DefineValue.TOTAL_AMOUNT);
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);

                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_bank_product.setText(args.getString(DefineValue.BANK_PRODUCT));
                tv_fee.setText(fee);
                tv_amount.setText(amount);
                tv_total_amount.setText(total_amount);

//                if (buss_scheme_code.equalsIgnoreCase("TOP")) {
//                    tv_user_id.setText(getString(R.string.community_code));
//                    tv_nama.setText(getString(R.string.customer_code));
//                    tv_useerid_value.setText(args.getString(DefineValue.COMMUNITY_CODE));
//                    tv_name_value.setText(args.getString(DefineValue.MEMBER_CODE));
//                }

                if (type.equals(DefineValue.COLLECTION)) {
                    View layout_remark = inflated.findViewById(R.id.topup_remark_layout);
                    layout_remark.setVisibility(View.VISIBLE);
                    TextView tv_remark = layout_remark.findViewById(R.id.dialog_topup_message_value);
                    tv_remark.setText(args.getString(DefineValue.REMARK));
                }
//                }
            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.BIL)) {
//                if (type.equals(DefineValue.BILLER) || type.equals(DefineValue.BILLER_BPJS) || type.equals(DefineValue.BILLER_PLN)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_biller);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_report_type = inflated.findViewById(R.id.dialog_reportbiller_buss_scheme_name);
                TextView tv_useerid_value = inflated.findViewById(R.id.dialog_reportbiller_userid_value);
                TextView tv_name_value = inflated.findViewById(R.id.dialog_reportbiller_name_value);
                TextView tv_denom_value = inflated.findViewById(R.id.dialog_reportbiller_denomretail_value);
                TextView tv_amount_value = inflated.findViewById(R.id.dialog_reportbiller_amount_value);
                TextView tv_payment_options_text = inflated.findViewById(R.id.dialog_reportbiller_payment_options_value);
                TextView tv_fee_text = inflated.findViewById(R.id.dialog_reportbiller_fee_value);
                TextView tv_total_amount_text = inflated.findViewById(R.id.dialog_reportbiller_total_amount_value);
                TextView tv_additionalFee = inflated.findViewById(R.id.tv_additionalFee);
                TextView tv_additionalFeeValue = inflated.findViewById(R.id.dialog_reportbiller_additionalfee_value);
                TextView tv_destinationValue = inflated.findViewById(R.id.dialog_reportbiller_destination_value);
                TextView tv_destinationEmon = inflated.findViewById(R.id.tv_destination_no_emon);
                TextView tv_destinationEmonValue = inflated.findViewById(R.id.tv_destination_no_emon_value);
                View viewAdditional = inflated.findViewById(R.id.view_additionalFee);
                View viewEmon = inflated.findViewById(R.id.view_destination_no_emon);
                TableLayout tableLayoutDestination = inflated.findViewById(R.id.billertoken_layout_destination);

                TableLayout mTableLayout = inflated.findViewById(R.id.billertoken_layout_table);
                mTableLayout.setVisibility(View.VISIBLE);

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_denom_value.setText(args.getString(DefineValue.DENOM_DATA));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_payment_options_text.setText(args.getString(DefineValue.PRODUCT_NAME));
                tv_fee_text.setText(args.getString(DefineValue.FEE));
                tv_total_amount_text.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                if (args.getString(DefineValue.BUSS_SCHEME_NAME).equalsIgnoreCase("Pembelian Paket Data")) {
//                    tableLayoutDestination.setVisibility(View.VISIBLE);
                    tv_destinationValue.setText(args.getString(DefineValue.DESTINATION_REMARK));
                }

                if (args.containsKey(DefineValue.BILLER_TYPE))
                    if (args.getString(DefineValue.BILLER_TYPE).equalsIgnoreCase("EMON")) {
                        viewEmon.setVisibility(View.VISIBLE);
                        tv_destinationEmon.setVisibility(View.VISIBLE);
                        tv_destinationEmonValue.setVisibility(View.VISIBLE);
                        tv_destinationEmonValue.setText(args.getString(DefineValue.PAYMENT_REMARK));
                    }

                if (isAgent) {
                    viewAdditional.setVisibility(View.VISIBLE);
                    tv_additionalFee.setVisibility(View.VISIBLE);
                    tv_additionalFeeValue.setVisibility(View.VISIBLE);
                    tv_additionalFeeValue.setText(args.getString(DefineValue.ADDITIONAL_FEE));
                }

                createTableDesc(args.getString(DefineValue.BILLER_DETAIL, ""), mTableLayout, type);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

//                        Boolean isShowDescription = args.getBoolean(DefineValue.IS_SHOW_DESCRIPTION, false);

//                        if (isShowDescription) {
//                            tv_denom_text.setText(getString(R.string.billertoken_text_item_name));
//                            View desclayout = inflated.findViewById(R.id.dialog_reportbiller_layout_desc);
//                            RelativeLayout mDescLayout = (RelativeLayout) inflated.findViewById(R.id.billertoken_layout_deskripsi);
//
//                            if (!args.getString(DefineValue.DETAILS_BILLER, "").isEmpty()) {
//                                mDescLayout.setVisibility(View.VISIBLE);
//                                desclayout.setVisibility(View.VISIBLE);
//                                final TableLayout mTableLayout = (TableLayout) inflated.findViewById(R.id.billertoken_layout_table);
//                                final ImageView mIconArrow = (ImageView) inflated.findViewById(R.id.billertoken_arrow_desc);
//
//                                View.OnClickListener descriptionClickListener = new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Animation mRotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_arrow);
//                                        mRotate.setInterpolator(new LinearInterpolator());
//                                        mRotate.setAnimationListener(new Animation.AnimationListener() {
//                                            @Override
//                                            public void onAnimationStart(Animation animation) {
//
//                                            }
//
//                                            @Override
//                                            public void onAnimationEnd(Animation animation) {
//                                                mIconArrow.invalidate();
//                                                if (mTableLayout.getVisibility() == View.VISIBLE) {
//                                                    mIconArrow.setImageResource(R.drawable.ic_circle_arrow_down);
//                                                    mTableLayout.setVisibility(View.GONE);
//                                                } else {
//                                                    mIconArrow.setImageResource(R.drawable.ic_circle_arrow);
//                                                    mTableLayout.setVisibility(View.VISIBLE);
//                                                }
//                                                mIconArrow.invalidate();
//                                            }
//
//                                            @Override
//                                            public void onAnimationRepeat(Animation animation) {
//
//                                            }
//                                        });
//                                        mIconArrow.startAnimation(mRotate);
//                                    }
//                                };
//
//                                mDescLayout.setOnClickListener(descriptionClickListener);
//                                mIconArrow.setOnClickListener(descriptionClickListener);
//
//                                createTableDesc(args.getString(DefineValue.DETAILS_BILLER, ""), mTableLayout, type);
//                            }
//
//                            Timber.d("isi Amount desired:" + args.getString(DefineValue.AMOUNT_DESIRED));
//
//                            if (!args.getString(DefineValue.AMOUNT_DESIRED, "").isEmpty()) {
//                                View inputAmountLayout = inflated.findViewById(R.id.dialog_reportbiller_amount_desired_layout);
//                                inputAmountLayout.setVisibility(View.VISIBLE);
//                                TextView _desired_amount = (TextView) inputAmountLayout.findViewById(R.id.dialog_reportbiller_amount_desired_value);
//                                _desired_amount.setText(args.getString(DefineValue.AMOUNT_DESIRED));
//                            }
//                        }
//                }
            } else if (buss_scheme_code.equals(DefineValue.P2P) || type.equals(DefineValue.PAYFRIENDS)) {
                //payfriend

                stub.setLayoutResource(R.layout.layout_dialog_report_payfriends);
                View inflated = stub.inflate();
                //                LinearLayout mLayout = (LinearLayout) view.findViewById(R.id.report_payfriends);
                TextView tv_useerid_value = inflated.findViewById(R.id.dialog_reportpayfriends_userid_value);
                TextView tv_name_value = inflated.findViewById(R.id.dialog_reportpayfriends_name_value);
                TextView tv_amount_each_value = inflated.findViewById(R.id.dialog_reportpayfriends_amount_each_value);
                TextView tv_amount_value = inflated.findViewById(R.id.dialog_reportpayfriends_amount_value);
                TextView tv_fee_value = inflated.findViewById(R.id.dialog_reportpayfriends_fee_value);
                TextView tv_total_amount_value = inflated.findViewById(R.id.dialog_reportpayfriends_totalamount_value);
                TextView tv_message = inflated.findViewById(R.id.dialog_reportpayfriends_message_value);
                TextView tv_report_type = inflated.findViewById(R.id.dialog_report_type);

                mTableLayout = inflated.findViewById(R.id.transfer_data_layout_table);
                mTableLayout.setVisibility(View.VISIBLE);

                inflated.setVisibility(View.VISIBLE);
                tv_report_type.setText(getString(R.string.send_money));
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
//                tv_recipients_value.setText(args.getString(DefineValue.RECIPIENTS));
                tv_amount_each_value.setText(args.getString(DefineValue.AMOUNT_EACH));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                tv_message.setText(args.getString(DefineValue.MESSAGE));

                createTablePayFriend(args.getString(DefineValue.TRANSFER_DATA, ""), mTableLayout);

                if (args.getString(DefineValue.RECIPIENTS_ERROR) != null) {
                    LinearLayout mLayoutFailed = inflated.findViewById(R.id.dialog_reportpayfriends_failed_layout);
                    TextView tv_error_recipient_value = inflated.findViewById(R.id.dialog_reportpayfriends_errorrecipient_value);
                    mLayoutFailed.setVisibility(View.VISIBLE);
                    tv_error_recipient_value.setText(args.getString(DefineValue.RECIPIENTS_ERROR));
                }
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);
                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
            } else if (buss_scheme_code.equals(DefineValue.OR) || buss_scheme_code.equals(DefineValue.ORP) || buss_scheme_code.equals(DefineValue.AJD)) {
//                    laporan transfer yg out

                stub.setLayoutResource(R.layout.layout_dialog_report_transaction);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                LinearLayout trAlias = (TableRow) inflated.findViewById(R.id.trAlias);
                View lineAlias = inflated.findViewById(R.id.lineAlias);
                TextView tv_detail = inflated.findViewById(R.id.dialog_report_trans_detail_value);
                TextView tv_user_id = inflated.findViewById(R.id.dialog_report_trans_user_id);
                TextView tv_user_name = inflated.findViewById(R.id.dialog_report_trans_user_name);
                TextView tv_no_tujuan = inflated.findViewById(R.id.dialog_report_trans_alias_no);
                TextView tv_nama_tujuan = inflated.findViewById(R.id.dialog_report_trans_alias_name);
                TextView tv_amount = inflated.findViewById(R.id.dialog_report_trans_amount_value);
                TextView tv_remark = inflated.findViewById(R.id.dialog_report_trans_remark_value);
                TextView tv_fee = inflated.findViewById(R.id.dialog_report_trans_admin_fee);
                TextView tv_total = inflated.findViewById(R.id.dialog_report_trans_total);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
                String detail = args.getString(DefineValue.BUSS_SCHEME_NAME, "");
                tv_no_tujuan.setText(args.getString(DefineValue.PAYMENT_PHONE, ""));
                tv_nama_tujuan.setText(args.getString(DefineValue.PAYMENT_NAME, ""));
                tv_user_id.setText(args.getString(DefineValue.MEMBER_PHONE, ""));
                tv_user_name.setText(args.getString(DefineValue.MEMBER_NAME, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_fee.setText(args.getString(DefineValue.FEE, ""));
                tv_total.setText(args.getString(DefineValue.TOTAL_AMOUNT, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK, ""));

                tv_detail.setText(detail);

            } else if (buss_scheme_code.equals(DefineValue.IR) || buss_scheme_code.equals(DefineValue.AJC)) {
//                  transfer in
                stub.setLayoutResource(R.layout.layout_dialog_report_transaction);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                LinearLayout trAlias = (TableRow) inflated.findViewById(R.id.trAlias);
                View lineAlias = inflated.findViewById(R.id.lineAlias);
                TextView tv_detail = inflated.findViewById(R.id.dialog_report_trans_detail_value);
                TextView tv_user_id = inflated.findViewById(R.id.dialog_report_trans_user_id);
                TextView tv_user_name = inflated.findViewById(R.id.dialog_report_trans_user_name);
                TextView tv_nama = inflated.findViewById(R.id.tv_nama_alias);
                TextView tv_no = inflated.findViewById(R.id.tv_no_alias);
                TextView tv_no_tujuan = inflated.findViewById(R.id.dialog_report_trans_alias_no);
                TextView tv_nama_tujuan = inflated.findViewById(R.id.dialog_report_trans_alias_name);
                TextView tv_amount = inflated.findViewById(R.id.dialog_report_trans_amount_value);
                TextView tv_remark = inflated.findViewById(R.id.dialog_report_trans_remark_value);
                TextView tv_fee = inflated.findViewById(R.id.dialog_report_trans_admin_fee);
                TextView tv_total = inflated.findViewById(R.id.dialog_report_trans_total);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }


                String detail = args.getString(DefineValue.BUSS_SCHEME_NAME, "");

                tv_no.setText(getString(R.string.source_account_no));
                tv_nama.setText(getString(R.string.source_account_name));
                tv_nama_tujuan.setText(args.getString(DefineValue.PAYMENT_NAME, ""));
                tv_no_tujuan.setText(args.getString(DefineValue.PAYMENT_PHONE, ""));
                tv_user_id.setText(args.getString(DefineValue.MEMBER_PHONE, ""));
                tv_user_name.setText(args.getString(DefineValue.MEMBER_NAME, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_fee.setText(args.getString(DefineValue.FEE, ""));
                tv_total.setText(args.getString(DefineValue.TOTAL_AMOUNT, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK, ""));

                tv_detail.setText(detail);

            } else if (buss_scheme_code.equals(DefineValue.OC) || type.equals(DefineValue.CASHOUT)) {
//                    //cashout ke bank

//                      View report_layout = view.findViewById(R.id.report_cashout);
                stub.setLayoutResource(R.layout.layout_dialog_report_cashout);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_report_type = inflated.findViewById(R.id.tv_report_transaction_type);
                TextView tv_useerid_value = inflated.findViewById(R.id.dialog_reportcashout_userid_value);
                TextView tv_name_value = inflated.findViewById(R.id.dialog_reportcashout_name_value);
                TextView tv_bank_name_value = inflated.findViewById(R.id.dialog_reportcashout_bank_name_value);
                TextView tv_bank_acc_no_value = inflated.findViewById(R.id.dialog_reportcashout_bank_acc_no_value);
                TextView tv_bank_acc_name_value = inflated.findViewById(R.id.dialog_reportcashout_bank_acc_name_value);
                TextView tv_nominal_value = inflated.findViewById(R.id.dialog_reportcashout_nominal_value);
                TextView tv_fee_value = inflated.findViewById(R.id.dialog_reportcashout_fee_value);
                TextView tv_total_amount_value = inflated.findViewById(R.id.dialog_reportcashout_totalamount_value);

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_bank_name_value.setText(args.getString(DefineValue.PAYMENT_BANK));
                tv_bank_acc_no_value.setText(args.getString(DefineValue.NO_BENEF));
                tv_bank_acc_name_value.setText(args.getString(DefineValue.PAYMENT_NAME));
                tv_nominal_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);
                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
            } else if (buss_scheme_code.equals(DefineValue.DENOM_B2B)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_denom);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_report_type = inflated.findViewById(R.id.tv_report_transaction_type);
                TextView tv_comm_code = inflated.findViewById(R.id.dialog_denom_comm_code);
                TextView tv_store_code = inflated.findViewById(R.id.dialog_denom_store_code);
                TextView tv_bank_product = inflated.findViewById(R.id.dialog_denom_bank_product);
                TextView tv_bank_order_number = inflated.findViewById(R.id.dialog_denom_order_number);
//                RecyclerView rv_denom_item_list = inflated.findViewById(R.id.dialog_denom_item_list_recyclerview);
                TextView tv_amount = inflated.findViewById(R.id.dialog_denom_amount);
                TextView tv_fee = inflated.findViewById(R.id.dialog_denom_fee_value);
                TextView tv_total_amount = inflated.findViewById(R.id.dialog_denom_totalamount_value);
                TextView tv_store_name = inflated.findViewById(R.id.tv_report_store_name);
                TextView tv_store_address = inflated.findViewById(R.id.tv_report_store_address);
                TextView tv_agent_name = inflated.findViewById(R.id.dialog_denom_agent_name);
                TextView tv_agent_phone = inflated.findViewById(R.id.dialog_denom_agent_phone);

                TableLayout mTableLayout = inflated.findViewById(R.id.billertoken_layout_table);
                mTableLayout.setVisibility(View.VISIBLE);

                createTableDenom(args.getString(DefineValue.DENOM_DETAIL, ""), mTableLayout);
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);
                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME));
                tv_comm_code.setText(args.getString(DefineValue.COMMUNITY_CODE));
                tv_store_code.setText(args.getString(DefineValue.STORE_CODE));
                tv_bank_product.setText(args.getString(DefineValue.BANK_PRODUCT));
                tv_bank_order_number.setText(args.getString(DefineValue.ORDER_ID));
                tv_amount.setText(args.getString(DefineValue.AMOUNT));
                tv_fee.setText(args.getString(DefineValue.FEE));
                tv_total_amount.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                tv_store_name.setText(args.getString(DefineValue.STORE_NAME));
                tv_store_address.setText(args.getString(DefineValue.STORE_ADDRESS));
                tv_agent_name.setText(args.getString(DefineValue.AGENT_NAME));
                tv_agent_phone.setText(args.getString(DefineValue.AGENT_PHONE));
            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.DGI)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_dgi);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_report_type = inflated.findViewById(R.id.dialog_reportbiller_buss_scheme_name);
                TextView tv_agent_name = inflated.findViewById(R.id.dialog_report_agent_name_value);
                TextView tv_mitra_name = inflated.findViewById(R.id.dialog_report_mitra_name_value);
                TextView tv_community_name = inflated.findViewById(R.id.dialog_report_community_name_value);
                TextView tv_shop_name = inflated.findViewById(R.id.dialog_report_shop_name_value);
                TextView tv_payment_type = inflated.findViewById(R.id.dialog_report_payment_type_value);
                TextView tv_amount = inflated.findViewById(R.id.dialog_reportbiller_amount_value);
                TextView tv_fee = inflated.findViewById(R.id.dialog_reportbiller_fee_value);
                TextView tv_total_amount = inflated.findViewById(R.id.dialog_reportbiller_total_amount_value);

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME, ""));
                tv_agent_name.setText(args.getString(DefineValue.USER_NAME, ""));
                tv_mitra_name.setText(args.getString(DefineValue.DGI_ANCHOR_NAME, ""));
                tv_community_name.setText(args.getString(DefineValue.DGI_COMM_NAME, ""));
                tv_shop_name.setText(args.getString(DefineValue.DGI_MEMBER_NAME, ""));
                tv_payment_type.setText(args.getString(DefineValue.PAYMENT_TYPE_DESC, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT));
                tv_fee.setText(args.getString(DefineValue.FEE));
                tv_total_amount.setText(args.getString(DefineValue.TOTAL_AMOUNT));

                TableLayout mTableLayout = inflated.findViewById(R.id.billertoken_layout_table);
                mTableLayout.setVisibility(View.VISIBLE);

                createTableDGI(args.getString(DefineValue.INVOICE, ""), mTableLayout);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);
                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
            } else if (buss_scheme_code.equalsIgnoreCase(DefineValue.SG3)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_sof);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_report_type = inflated.findViewById(R.id.dialog_reportbiller_buss_scheme_name);
                TextView tv_comm_name = inflated.findViewById(R.id.dialog_report_merchant_name_value);
                TextView tv_payment_remark = inflated.findViewById(R.id.dialog_reportbiller_paymentremark_value);
                TextView tv_amount = inflated.findViewById(R.id.dialog_reportbiller_amount_value);
                TextView tv_fee = inflated.findViewById(R.id.dialog_reportbiller_fee_value);
                TextView tv_total_amount = inflated.findViewById(R.id.dialog_reportbiller_total_amount_value);

                tv_report_type.setText(args.getString(DefineValue.BUSS_SCHEME_NAME, ""));
                tv_comm_name.setText(args.getString(DefineValue.COMMUNITY_NAME, ""));
                tv_payment_remark.setText(args.getString(DefineValue.REMARK, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT));
                tv_fee.setText(args.getString(DefineValue.FEE));
                tv_total_amount.setText(args.getString(DefineValue.TOTAL_AMOUNT));

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);
                tv_trans_remark.setText(args.getString(DefineValue.TRX_STATUS_REMARK));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
            }
        }

        Button btn_ok = view.findViewById(R.id.dialog_reportbiller_btn_ok);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btn_ok.setOnClickListener(this);

        contentInvoice = view.findViewById(R.id.rlid);
        saveimage = view.findViewById(R.id.img_download);
        shareimage = view.findViewById(R.id.img_share);
        printStruk = view.findViewById(R.id.img_print);

        saveimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveimage.setEnabled(false);
                saveimage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveimage.setEnabled(true);
                    }
                }, 3000);
                reqPermissionSaveorShareImage(false);
            }
        });

        shareimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareimage.setEnabled(false);
                shareimage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shareimage.setEnabled(true);
                    }
                }, 4000);
                reqPermissionSaveorShareImage(true);
            }
        });

        printStruk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If Bluetooth is not on, request that it be enabled.
                // setupChat() will then be called during onActivityResult
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, DevicesList.REQUEST_ENABLE_BT);
                    // Otherwise, setup the session
                } else {
                    if (mService == null)
                        mService = new BluetoothService(getContext(), mHandler);//监听

                    printStruk.setEnabled(false);
                    printStruk.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            printStruk.setEnabled(true);
                        }
                    }, 4000);

                    String perms = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                    if (EasyPermissions.hasPermissions(getContext(), perms)) {
                        String[] separated = trx_id.split("\n");
                        imgFilename = separated[0];

                        countRetry = 0;

                        connect();

                    } else {
                        EasyPermissions.requestPermissions(getActivity(), getString(R.string.rationale_save_image_permission),
                                RC_REQUEST_WRITE_EXTERNAL_STORAGE_AND_PRINT, perms);
                    }
                }
            }
        });

        return view;
    }

    private void printStrukImage() {
        if (viewToBitmap.ConvertToPrint(contentInvoice, imgFilename, mService)) {

        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.failed_save_gallery), Toast.LENGTH_LONG).show();
        }
    }

    private void connect() {
        if (mService == null) {
            Intent BTIntent = new Intent(getActivity(), DevicesList.class);
            this.startActivityForResult(BTIntent, DevicesList.REQUEST_CONNECT_DEVICE);
        } else {
            if (mService.getState() != BluetoothService.STATE_CONNECTED) {
                Intent BTIntent = new Intent(getActivity(), DevicesList.class);
                this.startActivityForResult(BTIntent, DevicesList.REQUEST_CONNECT_DEVICE);
            } else {
                printStrukImage();
            }
        }
    }

    private void reqPermissionSaveorShareImage(Boolean isShareImage) {
        String perms = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            String[] separated = trx_id.split("\n");
            String filename = separated[0];

            if (isShareImage) {
                viewToBitmap.shareIntentApp(getActivity(), contentInvoice, filename);
            } else {
                if (viewToBitmap.Convert(contentInvoice, filename))
                    Toast.makeText(getContext(), getContext().getString(R.string.success_saved_gallery), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getContext(), getContext().getString(R.string.failed_save_gallery), Toast.LENGTH_LONG).show();
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_save_image_permission),
                    RC_REQUEST_WRITE_EXTERNAL_STORAGE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void createTableDenom(String jsonData, TableLayout mTableLayout) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;
            String value = "";

            int length = jsonArray.length();
            List<String> tempList = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                String tempData = jsonArray.getString(i);
                tempList.add(tempData);
            }

            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 8.0f);
            rowParams.setMargins(6, 6, 6, 6);
            TableRow.LayoutParams rowParams2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            rowParams2.setMargins(6, 6, 6, 6);

            for (int i = 0; i < tempList.size(); i++) {

                detail_field = new TextView(getActivity());
                detail_field.setGravity(Gravity.LEFT);
                detail_field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_field.setLayoutParams(rowParams2);
                detail_field.setTextColor(Color.parseColor("#757575"));
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                detail_value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_value.setLayoutParams(rowParams);
                detail_value.setPadding(6, 9, 3, 9);
                detail_value.setTextColor(Color.parseColor("#757575"));
                View line = new View(getActivity());
                line.setLayoutParams(new LinearLayout.LayoutParams((ViewGroup.LayoutParams.MATCH_PARENT), 1));
                line.setBackgroundColor(Color.parseColor("#e0e0e0"));
                line.setPadding(8, 3, 3, 3);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(tempList.get(i));
                detail_value.setText(value);
                mTableLayout.addView(layout_table_row);
                mTableLayout.addView(line);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createTablePayFriend(String jsonData, TableLayout mTableLayout) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;
            String value = "";

            Iterator keys = jsonObject.keys();
            List<String> tempList = new ArrayList<>();

            while (keys.hasNext()) {
                String temp = (String) keys.next();
                tempList.add(temp);
            }

            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 8.0f);
            rowParams.setMargins(6, 6, 6, 6);
            TableRow.LayoutParams rowParams2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            rowParams2.setMargins(6, 6, 6, 6);

            for (int i = 0; i < tempList.size(); i++) {

                JSONArray itemArray = new JSONArray(jsonObject.optString(tempList.get(i)));
                for (int j = 0; j < itemArray.length(); j++) {
                    value += itemArray.getString(j) + "\n";
                    Timber.d("json" + j + "=" + value);
                }

                detail_field = new TextView(getActivity());
                detail_field.setGravity(Gravity.LEFT);
                detail_field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_field.setLayoutParams(rowParams2);
                detail_field.setTextColor(Color.parseColor("#757575"));
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                detail_value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_value.setLayoutParams(rowParams);
                detail_value.setPadding(6, 9, 3, 9);
//                detail_value.setTypeface(Typeface.DEFAULT_BOLD);
                detail_value.setTextColor(Color.parseColor("#757575"));
                View line = new View(getActivity());
                line.setLayoutParams(new LinearLayout.LayoutParams((ViewGroup.LayoutParams.MATCH_PARENT), 1));
                line.setBackgroundColor(Color.parseColor("#e0e0e0"));
                line.setPadding(8, 3, 3, 3);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(tempList.get(i));
                detail_value.setText(value);
                mTableLayout.addView(layout_table_row);
                mTableLayout.addView(line);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createTableDesc(String jsonData, TableLayout mTableLayout, String billerType) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;

            Iterator keys = jsonObject.keys();
            List<String> tempList = new ArrayList<>();

            if (billerType.equals(DefineValue.BILLER_BPJS)) {
                tempList = JsonSorting.BPJSTrxStructSortingField();
            } else {
                while (keys.hasNext()) {
                    String temp = (String) keys.next();
                    tempList.add(temp);
                }
            }


            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 8.0f);
            rowParams.setMargins(6, 6, 6, 6);
            TableRow.LayoutParams rowParams2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            rowParams2.setMargins(6, 6, 6, 6);


            for (int i = 0; i < tempList.size(); i++) {
                detail_field = new TextView(getActivity());
                detail_field.setGravity(Gravity.LEFT);
                detail_field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_field.setLayoutParams(rowParams2);
                detail_field.setTextColor(Color.parseColor("#757575"));
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                detail_value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_value.setLayoutParams(rowParams);
                detail_value.setPadding(6, 9, 3, 9);
//                detail_value.setTypeface(Typeface.DEFAULT_BOLD);
                detail_value.setTextColor(Color.parseColor("#757575"));
                View line = new View(getActivity());
                line.setLayoutParams(new LinearLayout.LayoutParams((ViewGroup.LayoutParams.MATCH_PARENT), 1));
                line.setBackgroundColor(Color.parseColor("#e0e0e0"));
                line.setPadding(8, 3, 3, 3);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(tempList.get(i));
                detail_value.setText(jsonObject.optString(tempList.get(i)));
                mTableLayout.addView(layout_table_row);
                mTableLayout.addView(line);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createTableDGI(String jsonData, TableLayout mTableLayout) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;

            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 8.0f);
            rowParams.setMargins(6, 6, 6, 6);
            TableRow.LayoutParams rowParams2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            rowParams2.setMargins(6, 6, 6, 6);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject model = jsonArray.getJSONObject(i);

                detail_field = new TextView(getActivity());
                detail_field.setGravity(Gravity.LEFT);
                detail_field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_field.setLayoutParams(rowParams2);
                detail_field.setTextColor(Color.parseColor("#757575"));
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                detail_value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                detail_value.setLayoutParams(rowParams);
                detail_value.setPadding(6, 9, 3, 9);
                detail_value.setTextColor(Color.parseColor("#757575"));
                View line = new View(getActivity());
                line.setLayoutParams(new LinearLayout.LayoutParams((ViewGroup.LayoutParams.MATCH_PARENT), 1));
                line.setBackgroundColor(Color.parseColor("#e0e0e0"));
                line.setPadding(8, 3, 3, 3);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(model.getString("doc_no"));
                detail_value.setText(MyApiClient.CCY_VALUE + ". " + CurrencyFormat.format(model.getString("amount")));
                mTableLayout.addView(layout_table_row);
                mTableLayout.addView(line);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
        callback.onOkButton();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (runnable != null)
            handler.removeCallbacks(runnable);
        // Stop the Bluetooth services
        if (mService != null)
            mService.stop();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult Select Printer " + requestCode + "--" + resultCode);

        switch (requestCode) {
            case DevicesList.REQUEST_CONNECT_DEVICE: {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DevicesList.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    if (BluetoothAdapter.checkBluetoothAddress(address)) {
                        btDevice = mBluetoothAdapter.getRemoteDevice(address);
                        // Attempt to connect to the device
                        mService.connect(btDevice);

                        handler.postDelayed(runnable, timeDelayed);

                    }


                }
                break;
            }
            case DevicesList.REQUEST_ENABLE_BT: {
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    mService = new BluetoothService(getContext(), mHandler);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    //Toast.makeText(this, R.string.bt_not_enabled_leaving,
                    //Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                }
                break;
            }


        }
    }


    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mService != null) {

            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                mService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /****************************************************************************************************/
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DevicesList.MESSAGE_STATE_CHANGE:
//                    String message = "Yessi is doing research \n\n";
                    Log.d("arg1:", String.valueOf(msg.arg1));
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Log.d("arg - connected:", String.valueOf(msg.arg1));
                            if (runnable != null)
                                handler.removeCallbacks(runnable);
                            printStrukImage();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            if (countRetry > maxRetry) {
                                //toast ke user, minta restart bluetooth hp dan printer
                                handler.removeCallbacks(runnable);
                                Toast.makeText(getContext(), "Restart bluetooth Handphone dan Printer Anda", Toast.LENGTH_LONG);
                            }
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            if (countRetry > maxRetry) {
                                //toast ke user, minta restart bluetooth hp dan printer
                                handler.removeCallbacks(runnable);
                                Toast.makeText(getContext(), "Restart bluetooth Handphone dan Printer Anda", Toast.LENGTH_LONG);
                            }
                            break;
                    }
                    break;
                case DevicesList.MESSAGE_WRITE:

                    break;
                case DevicesList.MESSAGE_READ:

                    break;
                case DevicesList.MESSAGE_DEVICE_NAME:
//                    String message2 = "Yessi is doing research device \n\n";
                    Log.d("arg1 - device-name:", String.valueOf(msg.arg1));
                    if (runnable != null)
                        handler.removeCallbacks(runnable);
                    break;
                case DevicesList.MESSAGE_TOAST:

                    break;
                case DevicesList.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接

                    break;
                case DevicesList.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    Toast.makeText(getContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*****************************************************************************************************/
    /*
     * SendDataString
     */
    private void SendDataString(String data) {

        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Log.d("Srvc State Bluetooth :", String.valueOf(mService.getState()));
            Toast.makeText(getContext(), R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (data.length() > 0) {
            try {
                mService.write(data.getBytes("GBK"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}


