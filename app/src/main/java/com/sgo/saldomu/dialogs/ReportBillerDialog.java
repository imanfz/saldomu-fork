package com.sgo.saldomu.dialogs;/*
  Created by Administrator on 3/6/2015.
 */

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.JsonSorting;
import com.sgo.saldomu.coreclass.ViewToBitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class ReportBillerDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "reportBiller Dialog";
    private static final int RC_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    private OnDialogOkCallback callback;
    private Boolean isActivity = false;
    private String trx_id;
    private ViewToBitmap viewToBitmap;
    private LinearLayout contentInvoice;
    private ImageView saveimage;
    private ImageView shareimage;
    private static final int recCodeShareImage = 11;
    private static final int recCodeSaveImage = 12;

    public interface OnDialogOkCallback {
        void onOkButton();
    }

    public static ReportBillerDialog newInstance() {
        ReportBillerDialog f = new ReportBillerDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(DefineValue.IS_ACTIVE,true);
        f.setArguments(bundle);
        return f;
    }


    public ReportBillerDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.isActivity = getArguments().getBoolean(DefineValue.IS_ACTIVE,false);

        try {
            if (isActivity)
                callback = (OnDialogOkCallback) getActivity();
            else
                callback = (OnDialogOkCallback) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }

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
        ViewStub stub = (ViewStub) view.findViewById(R.id.stub);

        Bundle args = getArguments();
        Timber.d("isi args report:" + args.toString());

        String type = args.getString(DefineValue.REPORT_TYPE);

        TextView tv_date_value = (TextView) view.findViewById(R.id.dialog_reportbiller_date_time);
        TextView tv_txid_value = (TextView) view.findViewById(R.id.dialog_reportbiller_tx_id);
        TextView tv_trans_remark = (TextView) view.findViewById(R.id.dialog_report_transaction_remark);
        TextView tv_trans_remark_sub = (TextView) view.findViewById(R.id.dialog_report_transaction_remark_sub);

        tv_date_value.setText(args.getString(DefineValue.DATE_TIME));
        tv_txid_value.setText(args.getString(DefineValue.TX_ID));

        trx_id = args.getString(DefineValue.TX_ID);


        if (type != null) {
            if (type.equals(DefineValue.BILLER_PLN)||type.equals(DefineValue.BILLER_BPJS)) {
//                View mLayout = view.findViewById(R.id.report_biller_pln);
                stub.setLayoutResource(R.layout.layout_dialog_report_biller_pln);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
                TableLayout mTableLayout = (TableLayout) inflated.findViewById(R.id.billertoken_layout_table);
                String source = args.getString(DefineValue.DETAILS_BILLER, "");
                Timber.d("isi source : \n", source);
                if(!source.isEmpty() && !source.equalsIgnoreCase("")) {
                    source = source.replace("customer_id", getString(R.string.customer_id));
                    createTableDesc(source, mTableLayout,type);
                }
            } else if (type.equals(DefineValue.BILLER)) {
//                View mLayout = view.findViewById(R.id.report_biller);
                stub.setLayoutResource(R.layout.layout_dialog_report_biller);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) inflated.findViewById(R.id.dialog_reportbiller_userid_value);
                TextView tv_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbiller_name_value);
                TextView tv_denom_value = (TextView) inflated.findViewById(R.id.dialog_reportbiller_denomretail_value);
                TextView tv_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportbiller_amount_value);
                TextView tv_denom_text = (TextView) inflated.findViewById(R.id.dialog_reportbiller_text_denom);
                TextView tv_payment_options_text = (TextView) inflated.findViewById(R.id.dialog_reportbiller_payment_options_value);
                TextView tv_fee_text = (TextView) inflated.findViewById(R.id.dialog_reportbiller_fee_value);
                TextView tv_total_amount_text = (TextView) inflated.findViewById(R.id.dialog_reportbiller_total_amount_value);
                TextView tv_dest_remark_text = (TextView) inflated.findViewById(R.id.dialog_reportbiller_dest_remark_value);


                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_denom_value.setText(args.getString(DefineValue.DENOM_DATA));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_payment_options_text.setText(args.getString(DefineValue.PAYMENT_NAME));
                tv_fee_text.setText(args.getString(DefineValue.FEE));
                tv_total_amount_text.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                tv_dest_remark_text.setText(args.getString(DefineValue.DESTINATION_REMARK));
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                Boolean isShowDescription = args.getBoolean(DefineValue.IS_SHOW_DESCRIPTION, false);

                if (isShowDescription) {
                    tv_denom_text.setText(getString(R.string.billertoken_text_item_name));
                    View desclayout = inflated.findViewById(R.id.dialog_reportbiller_layout_desc);
                    RelativeLayout mDescLayout = (RelativeLayout) inflated.findViewById(R.id.billertoken_layout_deskripsi);

                    if (!args.getString(DefineValue.DETAILS_BILLER, "").isEmpty()) {
                        mDescLayout.setVisibility(View.VISIBLE);
                        desclayout.setVisibility(View.VISIBLE);
                        final TableLayout mTableLayout = (TableLayout) inflated.findViewById(R.id.billertoken_layout_table);
                        final ImageView mIconArrow = (ImageView) inflated.findViewById(R.id.billertoken_arrow_desc);

                        View.OnClickListener descriptionClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Animation mRotate = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_arrow);
                                mRotate.setInterpolator(new LinearInterpolator());
                                mRotate.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        mIconArrow.invalidate();
                                        if (mTableLayout.getVisibility() == View.VISIBLE) {
                                            mIconArrow.setImageResource(R.drawable.ic_circle_arrow_down);
                                            mTableLayout.setVisibility(View.GONE);
                                        } else {
                                            mIconArrow.setImageResource(R.drawable.ic_circle_arrow);
                                            mTableLayout.setVisibility(View.VISIBLE);
                                        }
                                        mIconArrow.invalidate();
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                mIconArrow.startAnimation(mRotate);
                            }
                        };

                        mDescLayout.setOnClickListener(descriptionClickListener);
                        mIconArrow.setOnClickListener(descriptionClickListener);

                        createTableDesc(args.getString(DefineValue.DETAILS_BILLER, ""), mTableLayout,type);
                    }

                    Timber.d("isi Amount desired:" + args.getString(DefineValue.AMOUNT_DESIRED));

                    if (!args.getString(DefineValue.AMOUNT_DESIRED, "").isEmpty()) {
                        View inputAmountLayout = inflated.findViewById(R.id.dialog_reportbiller_amount_desired_layout);
                        inputAmountLayout.setVisibility(View.VISIBLE);
                        TextView _desired_amount = (TextView) inputAmountLayout.findViewById(R.id.dialog_reportbiller_amount_desired_value);
                        _desired_amount.setText(args.getString(DefineValue.AMOUNT_DESIRED));
                    }
                }

            } else if (type.equals(DefineValue.PAYFRIENDS)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_payfriends);
                View inflated = stub.inflate();
//                LinearLayout mLayout = (LinearLayout) view.findViewById(R.id.report_payfriends);
                TextView tv_useerid_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_userid_value);
                TextView tv_name_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_name_value);
                TextView tv_recipients_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_recipients_value);
                TextView tv_amount_each_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_amount_each_value);
                TextView tv_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_amount_value);
                TextView tv_fee_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_fee_value);
                TextView tv_total_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_totalamount_value);
                TextView tv_message = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_message_value);

                inflated.setVisibility(View.VISIBLE);
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_recipients_value.setText(args.getString(DefineValue.RECIPIENTS));
                tv_amount_each_value.setText(args.getString(DefineValue.AMOUNT_EACH));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                tv_message.setText(args.getString(DefineValue.MESSAGE));

                if (args.getString(DefineValue.RECIPIENTS_ERROR) != null) {
                    LinearLayout mLayoutFailed = (LinearLayout) inflated.findViewById(R.id.dialog_reportpayfriends_failed_layout);
                    TextView tv_error_recipient_value = (TextView) inflated.findViewById(R.id.dialog_reportpayfriends_errorrecipient_value);
                    mLayoutFailed.setVisibility(View.VISIBLE);
                    tv_error_recipient_value.setText(args.getString(DefineValue.RECIPIENTS_ERROR));
                }
            } else if (type.equals(DefineValue.TOPUP) || type.equals(DefineValue.COLLECTION)) {
                stub.setLayoutResource(R.layout.layout_dialog_report_topup);
                View inflated = stub.inflate();
//                View topup_layout = view.findViewById(R.id.report_topup);

                TextView tv_useerid_value = (TextView) inflated.findViewById(R.id.dialog_topup_userid_value);
                TextView tv_name_value = (TextView) inflated.findViewById(R.id.dialog_topup_name_value);
                TextView tv_bank_name = (TextView) inflated.findViewById(R.id.dialog_topup_bankname_value);
                TextView tv_bank_product = (TextView) inflated.findViewById(R.id.dialog_topup_productbank_value);
                TextView tv_fee = (TextView) inflated.findViewById(R.id.dialog_topup_fee_value);
                TextView tv_amount = (TextView) inflated.findViewById(R.id.dialog_topup_amount_value);
                TextView tv_total_amount = (TextView) inflated.findViewById(R.id.dialog_topup_total_amount_value);
                inflated.setVisibility(View.VISIBLE);

                String amount = args.getString(DefineValue.AMOUNT);
                String fee = args.getString(DefineValue.FEE);
                String total_amount = args.getString(DefineValue.TOTAL_AMOUNT);
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);

                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_bank_name.setText(args.getString(DefineValue.BANK_NAME));
                tv_bank_product.setText(args.getString(DefineValue.BANK_PRODUCT));
                tv_fee.setText(fee);
                tv_amount.setText(amount);
                tv_total_amount.setText(total_amount);

                if (type.equals(DefineValue.COLLECTION)) {
                    View layout_remark = inflated.findViewById(R.id.topup_remark_layout);
                    layout_remark.setVisibility(View.VISIBLE);
                    TextView tv_remark = (TextView) layout_remark.findViewById(R.id.dialog_topup_message_value);
                    tv_remark.setText(args.getString(DefineValue.REMARK));
                }

            } else if (type.equals(DefineValue.TRANSACTION)) {
//                View report_layout = view.findViewById(R.id.report_dialog);
                stub.setLayoutResource(R.layout.layout_dialog_report_transaction);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                LinearLayout trAlias = (TableRow) inflated.findViewById(R.id.trAlias);
                View lineAlias = inflated.findViewById(R.id.lineAlias);
                TextView tv_detail = (TextView) inflated.findViewById(R.id.dialog_report_trans_detail_value);
                TextView tv_type = (TextView) inflated.findViewById(R.id.dialog_report_trans_type_value);
                TextView tv_desc = (TextView) inflated.findViewById(R.id.dialog_report_trans_description_value);
                TextView tv_alias = (TextView) inflated.findViewById(R.id.dialog_report_trans_alias_value);
                TextView tv_amount = (TextView) inflated.findViewById(R.id.dialog_report_trans_amount_value);
                TextView tv_remark = (TextView) inflated.findViewById(R.id.dialog_report_trans_remark_value);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                String detail = args.getString(DefineValue.DETAIL, "");
                if (detail.equalsIgnoreCase(DefineValue.CASH_OUT)) {
                    trAlias.setVisibility(View.GONE);
                    lineAlias.setVisibility(View.GONE);
                } else {
                    trAlias.setVisibility(View.VISIBLE);
                    lineAlias.setVisibility(View.VISIBLE);
                    tv_alias.setText(args.getString(DefineValue.CONTACT_ALIAS, ""));
                }

                tv_type.setText(args.getString(DefineValue.TYPE, ""));
                tv_desc.setText(args.getString(DefineValue.DESCRIPTION, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK, ""));

                tv_detail.setText(detail);
            } else if (type.equals(DefineValue.TRANSACTION_ESPAY)) {
//                View report_layout = view.findViewById(R.id.report_dialog_espay);
                stub.setLayoutResource(R.layout.layout_dialog_report_espay_transaction);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_buss_scheme_name = (TextView) inflated.findViewById(R.id.dialog_report_buss_scheme_name_value);
                TextView tv_comm_name = (TextView) inflated.findViewById(R.id.dialog_report_community_value);
                TextView tv_amount = (TextView) inflated.findViewById(R.id.dialog_report_trans_amount_value);
                TextView tv_fee = (TextView) inflated.findViewById(R.id.dialog_report_fee_value);
                TextView tv_total_amount = (TextView) inflated.findViewById(R.id.dialog_report_total_amount_value);
                TextView tv_desc = (TextView) inflated.findViewById(R.id.dialog_report_trans_description_value);
                TextView tv_remark = (TextView) inflated.findViewById(R.id.dialog_report_trans_remark_value);
                TextView tv_bank_name = (TextView) inflated.findViewById(R.id.dialog_report_bank_name_value);
                TextView tv_product_name = (TextView) inflated.findViewById(R.id.dialog_report_product_name_value);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                tv_buss_scheme_name.setText(args.getString(DefineValue.BUSS_SCHEME_NAME, ""));
                tv_comm_name.setText(args.getString(DefineValue.COMMUNITY_NAME, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_fee.setText(args.getString(DefineValue.FEE, ""));
                tv_total_amount.setText(args.getString(DefineValue.TOTAL_AMOUNT, ""));
                tv_desc.setText(args.getString(DefineValue.DESCRIPTION, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK, ""));
                tv_bank_name.setText(args.getString(DefineValue.BANK_NAME, ""));
                tv_product_name.setText(args.getString(DefineValue.PRODUCT_NAME, ""));

                if(args.getString(DefineValue.PRODUCT_NAME).equalsIgnoreCase("UNIK"))
                {
                    tv_product_name.setText(getContext().getString(R.string.appname));
                }else {
                    tv_product_name.setText(args.getString(DefineValue.PRODUCT_NAME, ""));
                }
            } else if (type.equals(DefineValue.PULSA_AGENT)) {
//                View mLayout = view.findViewById(R.id.report_dialog_dap);
                stub.setLayoutResource(R.layout.layout_dialog_dap);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) view.findViewById(R.id.dialog_report_userid_value);
                TextView tv_name_value = (TextView) view.findViewById(R.id.dialog_report_name_value);
                TextView tv_operator_value = (TextView) view.findViewById(R.id.dialog_report_operator_value);
                TextView tv_nominal_value = (TextView) view.findViewById(R.id.dialog_report_nominal_value);
                TextView tv_amount_value = (TextView) view.findViewById(R.id.dialog_report_amount_value);
                TextView tv_payment_options_text = (TextView) view.findViewById(R.id.dialog_report_payment_options_value);
                TextView tv_fee_value = (TextView) view.findViewById(R.id.dialog_reportdap_fee_value);
                TextView tv_total_amount_value = (TextView) view.findViewById(R.id.dialog_reportdap_total_amount_value);
                TextView tv_dest_remark_text = (TextView) inflated.findViewById(R.id.dialog_reportbiller_dest_remark_value);


                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_operator_value.setText(args.getString(DefineValue.OPERATOR_NAME));
                tv_nominal_value.setText(args.getString(DefineValue.DENOM_DATA));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_payment_options_text.setText(args.getString(DefineValue.PAYMENT_NAME));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                tv_dest_remark_text.setText(args.getString(DefineValue.DESTINATION_REMARK));
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
                if(args.getString(DefineValue.PRODUCT_NAME).equalsIgnoreCase("UNIK"))
                {
                    tv_payment_options_text.setText(getContext().getString(R.string.appname));
                }else {
                    tv_payment_options_text.setText(args.getString(DefineValue.PRODUCT_NAME, ""));
                }

            } else if (type.equals(DefineValue.REQUEST)) {
//                View report_layout = view.findViewById(R.id.report_dialog_request);
                stub.setLayoutResource(R.layout.layout_dialog_request);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_detail = (TextView) inflated.findViewById(R.id.dialog_report_req_detail_value);
                TextView tv_type = (TextView) inflated.findViewById(R.id.dialog_report_req_type_value);
                TextView tv_desc = (TextView) inflated.findViewById(R.id.dialog_report_req_description_value);
                TextView tv_alias = (TextView) inflated.findViewById(R.id.dialog_report_req_alias_value);
                TextView tv_amount = (TextView) inflated.findViewById(R.id.dialog_report_req_amount_value);
                TextView tv_remark = (TextView) inflated.findViewById(R.id.dialog_report_req_remark_value);
                TextView tv_status = (TextView) inflated.findViewById(R.id.dialog_report_req_status_value);
                TextView tv_reason = (TextView) inflated.findViewById(R.id.dialog_report_req_reason_value);


                String detail = args.getString(DefineValue.DETAIL);

                tv_trans_remark.setText(getString(R.string.request));
                tv_type.setText(args.getString(DefineValue.TYPE, ""));
                tv_desc.setText(args.getString(DefineValue.DESCRIPTION, ""));
                tv_alias.setText(args.getString(DefineValue.CONTACT_ALIAS, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK, ""));
                tv_detail.setText(detail);
                tv_status.setText(args.getString(DefineValue.STATUS, ""));
                tv_reason.setText(args.getString(DefineValue.REASON, ""));
            } else if (type.equals(DefineValue.CASHOUT)) {
//                View report_layout = view.findViewById(R.id.report_cashout);
                stub.setLayoutResource(R.layout.layout_dialog_report_cashout);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_userid_value);
                TextView tv_name_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_name_value);
                TextView tv_bank_name_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_bank_name_value);
                TextView tv_bank_acc_no_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_bank_acc_no_value);
                TextView tv_bank_acc_name_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_bank_acc_name_value);
                TextView tv_nominal_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_nominal_value);
                TextView tv_fee_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_fee_value);
                TextView tv_total_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportcashout_totalamount_value);

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_bank_name_value.setText(args.getString(DefineValue.BANK_NAME));
                tv_bank_acc_no_value.setText(args.getString(DefineValue.ACCOUNT_NUMBER));
                tv_bank_acc_name_value.setText(args.getString(DefineValue.ACCT_NAME));
                tv_nominal_value.setText(args.getString(DefineValue.NOMINAL));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
            } else if (type.equals(DefineValue.CASHOUT_TUNAI)) {
//                View report_layout = view.findViewById(R.id.report_cashout_tunai);
                stub.setLayoutResource(R.layout.layout_dialog_report_cashouttunai);
                View inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) inflated.findViewById(R.id.dialog_report_userid_value);
                TextView tv_nameadmin_value = (TextView) inflated.findViewById(R.id.dialog_report_adminname_value);
                TextView tv_amount = (TextView) inflated.findViewById(R.id.dialog_report_amount_value);
                TextView tv_fee = (TextView) inflated.findViewById(R.id.dialog_report_fee_value);
                TextView tv_totalamount = (TextView) inflated.findViewById(R.id.dialog_report_total_amount_value);

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_nameadmin_value.setText(args.getString(DefineValue.NAME_ADMIN));
                tv_amount.setText(args.getString(DefineValue.AMOUNT));
                tv_fee.setText(args.getString(DefineValue.FEE));
                tv_totalamount.setText(args.getString(DefineValue.TOTAL_AMOUNT));

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS, false);
                if (!isSuccess) {
                    tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE, ""));
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
            } else if (type.equals(DefineValue.BBS_CASHIN) || type.equals(DefineValue.BBS_CASHOUT)) {
                View inflated;
                if (type.equals(DefineValue.BBS_CASHIN)) {
//                    report_layout = view.findViewById(R.id.report_bbs_cashin);
                    stub.setLayoutResource(R.layout.layout_dialog_report_bbs_cashin);
                    inflated = stub.inflate();
                    TextView tvNoDestination = (TextView) inflated.findViewById(R.id.tvNoDestination);

                    String benef_type = args.getString(DefineValue.TYPE_BENEF, "");
                    if (benef_type.equalsIgnoreCase(DefineValue.ACCT))
                        tvNoDestination.setText(R.string.number_destination);
                    else
                        tvNoDestination.setText(R.string.number_hp_destination);
                }
                else {
//                    inflated = view.findViewById(R.id.report_bbs_cashout);
                    stub.setLayoutResource(R.layout.layout_dialog_report_bbs_cashout);
                    inflated = stub.inflate();
                }
                inflated.setVisibility(View.VISIBLE);
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if (!isSuccess) {
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                TextView tv_useerid_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_userid_value);
                TextView tv_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_membername_value);
                TextView tv_source_bank_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_source_bank_value);
                TextView tv_source_acc_no_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_bank_source_acct_no_value);
                TextView tv_source_acc_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbs_bank_source_acc_name_value);
                TextView tv_benef_bank_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_benef_bank_value);
                TextView tv_benef_acc_no_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_bank_benef_acct_no_value);
                TextView tv_benef_acc_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbs_bank_benef_acc_name_value);
//                TextView tv_product_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_product_name_value);
                TextView tv_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_amount_value);
                TextView tv_fee_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_fee_value);
                TextView tv_total_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_totalamount_value);

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_source_acc_name_value.setText(args.getString(DefineValue.PRODUCT_NAME));
                if (type.equals(DefineValue.BBS_CASHIN))
                {
//                    tv_benef_bank_name_value.setText(args.getString(DefineValue.BANK_NAME));
                    tv_benef_acc_no_value.setText(args.getString(DefineValue.NO_BENEF));
                    tv_benef_acc_name_value.setText(args.getString(DefineValue.NAME_BENEF));
                }
                if (type.equals(DefineValue.BBS_CASHOUT)) {
//                    tv_source_bank_name_value.setText(args.getString(DefineValue.SOURCE_ACCT));
                    tv_source_bank_name_value.setText(args.getString(DefineValue.PRODUCT_NAME));
                    tv_source_acc_no_value.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));
                    tv_source_acc_name_value.setText(args.getString(DefineValue.MEMBER_SHOP_NAME));
                }
                tv_benef_bank_name_value.setText(args.getString(DefineValue.BANK_BENEF));
//              tv_product_name_value.setText(args.getString(DefineValue.BANK_PRODUCT));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
            } else if (type.equals(DefineValue.BBS_MEMBER_OTP)) {
                View inflated;
                stub.setLayoutResource(R.layout.layout_dialog_report_bbs_member_confirm);
                inflated = stub.inflate();
                inflated.setVisibility(View.VISIBLE);

                TextView tv_userid_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_userid_value);
                TextView tv_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_membername_value);
                TextView tv_token_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_token_value);
                TextView tv_source_bank_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_source_bank_value);
                TextView tv_source_acc_no_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_bank_source_acct_no_value);
                TextView tv_source_acc_name_value = (TextView) inflated.findViewById(R.id.dialog_reportbs_bank_source_acc_name_value);
                TextView tv_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_amount_value);
                TextView tv_fee_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_fee_value);
                TextView tv_total_amount_value = (TextView) inflated.findViewById(R.id.dialog_reportbbs_totalamount_value);

                tv_userid_value.setText(args.getString(DefineValue.MEMBER_SHOP_PHONE));
                tv_name_value.setText(args.getString(DefineValue.MEMBER_SHOP_NAME));
                tv_token_value.setText(args.getString(DefineValue.OTP_MEMBER));
                tv_source_bank_name_value.setText(args.getString(DefineValue.SOURCE_ACCT));
                tv_source_acc_no_value.setText(args.getString(DefineValue.SOURCE_ACCT_NO));
                tv_source_acc_name_value.setText(args.getString(DefineValue.SOURCE_ACCT_NAME));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
            }
        }

        Button btn_ok = (Button) view.findViewById(R.id.dialog_reportbiller_btn_ok);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btn_ok.setOnClickListener(this);

        contentInvoice = (LinearLayout) view.findViewById(R.id.rlid);
        saveimage = (ImageView) view.findViewById(R.id.img_download);
        shareimage = (ImageView) view.findViewById(R.id.img_share);

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

        return view;
    }

    private void reqPermissionSaveorShareImage(Boolean isShareImage){
        String perms = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            String[] separated = trx_id.split("\n");
            String filename = separated[0];

            if(isShareImage){
                viewToBitmap.shareIntentApp(contentInvoice, filename);
            }
            else {
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


    private void createTableDesc(String jsonData, TableLayout mTableLayout, String billerType) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;

            Iterator keys = jsonObject.keys();
            List<String> tempList = new ArrayList<>();

            if(billerType.equals(DefineValue.BILLER_BPJS)){
                tempList = JsonSorting.BPJSTrxStructSortingField();
            }
            else {
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
                detail_field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                detail_field.setLayoutParams(rowParams2);
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                detail_value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                detail_value.setLayoutParams(rowParams);
                detail_value.setPadding(6, 0, 0, 0);
                detail_value.setTypeface(Typeface.DEFAULT_BOLD);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(tempList.get(i));
                detail_value.setText(jsonObject.optString(tempList.get(i)));
                mTableLayout.addView(layout_table_row);
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
}
