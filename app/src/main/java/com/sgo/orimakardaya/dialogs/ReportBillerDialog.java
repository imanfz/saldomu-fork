package com.sgo.orimakardaya.dialogs;/*
  Created by Administrator on 3/6/2015.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.BillerActivity;
import com.sgo.orimakardaya.coreclass.DefineValue;

import org.json.JSONArray;
import org.json.JSONException;

import timber.log.Timber;

public class ReportBillerDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "reportBiller Dialog";

    private OnDialogOkCallback callback;
    private Activity mContext;
    private Boolean isActivty = false;


    public interface OnDialogOkCallback {
        void onOkButton();
    }

    public static ReportBillerDialog newInstance(Activity _context) {
        ReportBillerDialog f = new ReportBillerDialog();
        f.mContext = _context;
        f.isActivty = true;
        return f;
    }


    public ReportBillerDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if(isActivty)
                callback = (OnDialogOkCallback) getActivity();
            else
                callback = (OnDialogOkCallback) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
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

        Bundle args = getArguments();
        Timber.d("isi args report:"+args.toString());

        String type = args.getString(DefineValue.REPORT_TYPE);

        TextView tv_date_value = (TextView) view.findViewById(R.id.dialog_reportbiller_date_time);
        TextView tv_txid_value = (TextView) view.findViewById(R.id.dialog_reportbiller_tx_id);
        TextView tv_trans_remark = (TextView) view.findViewById(R.id.dialog_report_transaction_remark);
        TextView tv_trans_remark_sub = (TextView) view.findViewById(R.id.dialog_report_transaction_remark_sub);
        tv_date_value.setText(args.getString(DefineValue.DATE_TIME));
        tv_txid_value.setText(args.getString(DefineValue.TX_ID));

        if (type != null) {
            if(type.equals(DefineValue.BILLER)){
                View mLayout = view.findViewById(R.id.report_biller);
                mLayout.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_userid_value);
                TextView tv_name_value = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_name_value);
                TextView tv_denom_value = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_denomretail_value);
                TextView tv_amount_value = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_amount_value);
                TextView tv_denom_text = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_text_denom);
                TextView tv_payment_options_text = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_payment_options_value);
                TextView tv_fee_text = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_fee_value);
                TextView tv_total_amount_text = (TextView) mLayout.findViewById(R.id.dialog_reportbiller_total_amount_value);


                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_denom_value.setText(args.getString(DefineValue.DENOM_DATA));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_payment_options_text.setText(args.getString(DefineValue.PAYMENT_NAME));
                tv_fee_text.setText(args.getString(DefineValue.FEE));
                tv_total_amount_text.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if(!isSuccess){
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }


                if(args.getInt(DefineValue.BUY_TYPE, 0) == BillerActivity.PAYMENT_TYPE){
                    tv_denom_text.setText(getString(R.string.billertoken_text_item_name));
                    View desclayout = mLayout.findViewById(R.id.dialog_reportbiller_layout_desc);
                    RelativeLayout mDescLayout = (RelativeLayout) mLayout.findViewById(R.id.billertoken_layout_deskripsi);

                    if(!args.getString(DefineValue.DESC_FIELD,"").isEmpty()){
                        mDescLayout.setVisibility(View.VISIBLE);
                        desclayout.setVisibility(View.VISIBLE);
                        final TableLayout mTableLayout = (TableLayout) mLayout.findViewById(R.id.billertoken_layout_table);
                        final ImageView mIconArrow = (ImageView) mLayout.findViewById(R.id.billertoken_arrow_desc);

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
                                        if(mTableLayout.getVisibility() == View.VISIBLE){
                                            mIconArrow.setImageResource(R.drawable.ic_circle_arrow_down);
                                            mTableLayout.setVisibility(View.GONE);
                                        }
                                        else {
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

                        createTableDesc(args.getString(DefineValue.DESC_FIELD, ""),args.getString(DefineValue.DESC_VALUE, ""),mTableLayout);
                    }

                    Timber.d("isi Amount desired:" + args.getString(DefineValue.AMOUNT_DESIRED));

                    if(!args.getString(DefineValue.AMOUNT_DESIRED,"").isEmpty()){
                        View inputAmountLayout = mLayout.findViewById(R.id.dialog_reportbiller_amount_desired_layout);
                        inputAmountLayout.setVisibility(View.VISIBLE);
                        TextView _desired_amount = (TextView) inputAmountLayout.findViewById(R.id.dialog_reportbiller_amount_desired_value);
                        _desired_amount.setText(args.getString(DefineValue.AMOUNT_DESIRED));
                    }
                }

            }
            else if(type.equals(DefineValue.PAYFRIENDS)){

                LinearLayout mLayout = (LinearLayout) view.findViewById(R.id.report_payfriends);
                TextView tv_useerid_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_userid_value);
                TextView tv_name_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_name_value);
                TextView tv_recipients_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_recipients_value);
                TextView tv_amount_each_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_amount_each_value);
                TextView tv_amount_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_amount_value);
                TextView tv_fee_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_fee_value);
                TextView tv_total_amount_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_totalamount_value);
                TextView tv_message = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_message_value);

                mLayout.setVisibility(View.VISIBLE);
                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_recipients_value.setText(args.getString(DefineValue.RECIPIENTS));
                tv_amount_each_value.setText(args.getString(DefineValue.AMOUNT_EACH));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                tv_message.setText(args.getString(DefineValue.MESSAGE));

                if(args.getString(DefineValue.RECIPIENTS_ERROR) != null){
                    LinearLayout mLayoutFailed = (LinearLayout) mLayout.findViewById(R.id.dialog_reportpayfriends_failed_layout);
                    TextView tv_error_recipient_value = (TextView) mLayout.findViewById(R.id.dialog_reportpayfriends_errorrecipient_value);
                    mLayoutFailed.setVisibility(View.VISIBLE);
                    tv_error_recipient_value.setText(args.getString(DefineValue.RECIPIENTS_ERROR));
                }
            }
            else if(type.equals(DefineValue.TOPUP)||type.equals(DefineValue.COLLECTION)){
                View topup_layout = view.findViewById(R.id.report_topup);
                TextView tv_useerid_value = (TextView) topup_layout.findViewById(R.id.dialog_topup_userid_value);
                TextView tv_name_value = (TextView) topup_layout.findViewById(R.id.dialog_topup_name_value);
                TextView tv_bank_name = (TextView) topup_layout.findViewById(R.id.dialog_topup_bankname_value);
                TextView tv_bank_product = (TextView) topup_layout.findViewById(R.id.dialog_topup_productbank_value);
                TextView tv_fee = (TextView) topup_layout.findViewById(R.id.dialog_topup_fee_value);
                TextView tv_amount = (TextView) topup_layout.findViewById(R.id.dialog_topup_amount_value);
                TextView tv_total_amount = (TextView) topup_layout.findViewById(R.id.dialog_topup_total_amount_value);
                topup_layout.setVisibility(View.VISIBLE);

                String amount = args.getString(DefineValue.AMOUNT);
                String fee = args.getString(DefineValue.FEE);
                String total_amount = args.getString(DefineValue.TOTAL_AMOUNT);
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if(!isSuccess){
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

                if(type.equals(DefineValue.COLLECTION)){
                    View layout_remark = topup_layout.findViewById(R.id.topup_remark_layout);
                    layout_remark.setVisibility(View.VISIBLE);
                    TextView tv_remark = (TextView) layout_remark.findViewById(R.id.dialog_topup_message_value);
                    tv_remark.setText(args.getString(DefineValue.REMARK));
                }

            }
            else if(type.equals(DefineValue.TRANSACTION)){
                View report_layout = view.findViewById(R.id.report_dialog);
                report_layout.setVisibility(View.VISIBLE);

                LinearLayout trAlias = (TableRow) report_layout.findViewById(R.id.trAlias);
                View lineAlias = report_layout.findViewById(R.id.lineAlias);
                TextView tv_detail = (TextView) report_layout.findViewById(R.id.dialog_report_trans_detail_value);
                TextView tv_type = (TextView) report_layout.findViewById(R.id.dialog_report_trans_type_value);
                TextView tv_desc = (TextView) report_layout.findViewById(R.id.dialog_report_trans_description_value);
                TextView tv_alias = (TextView) report_layout.findViewById(R.id.dialog_report_trans_alias_value);
                TextView tv_amount = (TextView) report_layout.findViewById(R.id.dialog_report_trans_amount_value);
                TextView tv_remark = (TextView) report_layout.findViewById(R.id.dialog_report_trans_remark_value);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if(!isSuccess){
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

                String detail = args.getString(DefineValue.DETAIL, "");
                if(detail.equalsIgnoreCase(DefineValue.CASH_OUT)) {
                    trAlias.setVisibility(View.GONE);
                    lineAlias.setVisibility(View.GONE);
                }
                else {
                    trAlias.setVisibility(View.VISIBLE);
                    lineAlias.setVisibility(View.VISIBLE);
                    tv_alias.setText(args.getString(DefineValue.CONTACT_ALIAS, ""));
                }

                tv_type.setText(args.getString(DefineValue.TYPE, ""));
                tv_desc.setText(args.getString(DefineValue.DESCRIPTION, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK, ""));

                tv_detail.setText(detail);
            }
            else if(type.equals(DefineValue.TRANSACTION_ESPAY)){
                View report_layout = view.findViewById(R.id.report_dialog_espay);
                report_layout.setVisibility(View.VISIBLE);

                TextView tv_buss_scheme_name = (TextView) report_layout.findViewById(R.id.dialog_report_buss_scheme_name_value);
                TextView tv_comm_name = (TextView) report_layout.findViewById(R.id.dialog_report_community_value);
                TextView tv_amount = (TextView) report_layout.findViewById(R.id.dialog_report_trans_amount_value);
                TextView tv_fee = (TextView) report_layout.findViewById(R.id.dialog_report_fee_value);
                TextView tv_total_amount = (TextView) report_layout.findViewById(R.id.dialog_report_total_amount_value);
                TextView tv_desc = (TextView) report_layout.findViewById(R.id.dialog_report_trans_description_value);
                TextView tv_remark = (TextView) report_layout.findViewById(R.id.dialog_report_trans_remark_value);
                TextView tv_bank_name = (TextView) report_layout.findViewById(R.id.dialog_report_bank_name_value);
                TextView tv_product_name = (TextView) report_layout.findViewById(R.id.dialog_report_product_name_value);

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if(!isSuccess){
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
                tv_remark.setText(args.getString(DefineValue.REMARK,""));
                tv_bank_name.setText(args.getString(DefineValue.BANK_NAME,""));
                tv_product_name.setText(args.getString(DefineValue.PRODUCT_NAME,""));
            }
            else if(type.equals(DefineValue.PULSA_AGENT)){
                View mLayout = view.findViewById(R.id.report_dialog_dap);
                mLayout.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) view.findViewById(R.id.dialog_report_userid_value);
                TextView tv_name_value = (TextView) view.findViewById(R.id.dialog_report_name_value);
                TextView tv_operator_value = (TextView) view.findViewById(R.id.dialog_report_operator_value);
                TextView tv_nominal_value = (TextView) view.findViewById(R.id.dialog_report_nominal_value);
                TextView tv_amount_value = (TextView) view.findViewById(R.id.dialog_report_amount_value);
                TextView tv_payment_options_text = (TextView) view.findViewById(R.id.dialog_report_payment_options_value);
                TextView tv_fee_value = (TextView) view.findViewById(R.id.dialog_reportdap_fee_value);
                TextView tv_total_amount_value = (TextView) view.findViewById(R.id.dialog_reportdap_total_amount_value);

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_operator_value.setText(args.getString(DefineValue.OPERATOR_NAME));
                tv_nominal_value.setText(args.getString(DefineValue.DENOM_DATA));
                tv_amount_value.setText(args.getString(DefineValue.AMOUNT));
                tv_payment_options_text.setText(args.getString(DefineValue.PAYMENT_NAME));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS);

                tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE));
                if(!isSuccess){
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }

            }else if(type.equals(DefineValue.REQUEST)){
                View report_layout = view.findViewById(R.id.report_dialog_request);
                report_layout.setVisibility(View.VISIBLE);

                TextView tv_detail = (TextView) report_layout.findViewById(R.id.dialog_report_req_detail_value);
                TextView tv_type = (TextView) report_layout.findViewById(R.id.dialog_report_req_type_value);
                TextView tv_desc = (TextView) report_layout.findViewById(R.id.dialog_report_req_description_value);
                TextView tv_alias = (TextView) report_layout.findViewById(R.id.dialog_report_req_alias_value);
                TextView tv_amount = (TextView) report_layout.findViewById(R.id.dialog_report_req_amount_value);
                TextView tv_remark = (TextView) report_layout.findViewById(R.id.dialog_report_req_remark_value);
                TextView tv_status = (TextView) report_layout.findViewById(R.id.dialog_report_req_status_value);
                TextView tv_reason = (TextView) report_layout.findViewById(R.id.dialog_report_req_reason_value);


                String detail = args.getString(DefineValue.DETAIL);

                tv_trans_remark.setText(getString(R.string.request));
                tv_type.setText(args.getString(DefineValue.TYPE, ""));
                tv_desc.setText(args.getString(DefineValue.DESCRIPTION, ""));
                tv_alias.setText(args.getString(DefineValue.CONTACT_ALIAS, ""));
                tv_amount.setText(args.getString(DefineValue.AMOUNT, ""));
                tv_remark.setText(args.getString(DefineValue.REMARK,""));
                tv_detail.setText(detail);
                tv_status.setText(args.getString(DefineValue.STATUS,""));
                tv_reason.setText(args.getString(DefineValue.REASON,""));
            }

            else if(type.equals(DefineValue.CASHOUT)){
                View report_layout = view.findViewById(R.id.report_cashout);
                report_layout.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_userid_value);
                TextView tv_name_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_name_value);
                TextView tv_bank_name_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_bank_name_value);
                TextView tv_bank_acc_no_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_bank_acc_no_value);
                TextView tv_bank_acc_name_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_bank_acc_name_value);
                TextView tv_nominal_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_nominal_value);
                TextView tv_fee_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_fee_value);
                TextView tv_total_amount_value = (TextView) report_layout.findViewById(R.id.dialog_reportcashout_totalamount_value);

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_name_value.setText(args.getString(DefineValue.USER_NAME));
                tv_bank_name_value.setText(args.getString(DefineValue.BANK_NAME));
                tv_bank_acc_no_value.setText(args.getString(DefineValue.ACCOUNT_NUMBER));
                tv_bank_acc_name_value.setText(args.getString(DefineValue.ACCT_NAME));
                tv_nominal_value.setText(args.getString(DefineValue.NOMINAL));
                tv_fee_value.setText(args.getString(DefineValue.FEE));
                tv_total_amount_value.setText(args.getString(DefineValue.TOTAL_AMOUNT));
            }

            else if(type.equals(DefineValue.CASHOUT_TUNAI)){
                View report_layout = view.findViewById(R.id.report_cashout_tunai);
                report_layout.setVisibility(View.VISIBLE);

                TextView tv_useerid_value = (TextView) report_layout.findViewById(R.id.dialog_report_userid_value);
                TextView tv_nameadmin_value = (TextView) report_layout.findViewById(R.id.dialog_report_adminname_value);
                TextView tv_amount = (TextView) report_layout.findViewById(R.id.dialog_report_amount_value);
                TextView tv_fee = (TextView) report_layout.findViewById(R.id.dialog_report_fee_value);
                TextView tv_totalamount = (TextView) report_layout.findViewById(R.id.dialog_report_total_amount_value);

                tv_useerid_value.setText(args.getString(DefineValue.USERID_PHONE));
                tv_nameadmin_value.setText(args.getString(DefineValue.NAME_ADMIN));
                tv_amount.setText(args.getString(DefineValue.AMOUNT));
                tv_fee.setText(args.getString(DefineValue.FEE));
                tv_totalamount.setText(args.getString(DefineValue.TOTAL_AMOUNT));

                Boolean isSuccess = args.getBoolean(DefineValue.TRX_STATUS,false);
                if(!isSuccess){
                    tv_trans_remark.setText(args.getString(DefineValue.TRX_MESSAGE,""));
                    String transRemark = args.getString(DefineValue.TRX_REMARK);
                    tv_trans_remark_sub.setVisibility(View.VISIBLE);
                    tv_trans_remark_sub.setText(transRemark);
                }
            }
        }

        Button btn_ok = (Button) view.findViewById(R.id.dialog_reportbiller_btn_ok);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btn_ok.setOnClickListener(this);

        return view;
    }

    public void createTableDesc(String _desc_field,String _desc_value,TableLayout mTableLayout){
        try {
            JSONArray desc_field = new JSONArray(_desc_field);
            JSONArray desc_value = new JSONArray(_desc_value);
            TextView detail_field;
            TextView detail_value;
            TableRow layout_table_row;

            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT,8f);
            rowParams.setMargins(6,6,6,6);

            for (int i =0 ; i < desc_field.length();i++) {
                detail_field = new TextView(getActivity());
                detail_field.setGravity(Gravity.LEFT);
                detail_field.setLayoutParams(rowParams);
                detail_value = new TextView(getActivity());
                detail_value.setGravity(Gravity.RIGHT);
                detail_value.setLayoutParams(rowParams);
                detail_value.setTypeface(Typeface.DEFAULT_BOLD);
                layout_table_row = new TableRow(getActivity());
                layout_table_row.setLayoutParams(tableParams);
                layout_table_row.addView(detail_field);
                layout_table_row.addView(detail_value);
                detail_field.setText(desc_field.getString(i));
                detail_value.setText(desc_value.getString(i));
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
