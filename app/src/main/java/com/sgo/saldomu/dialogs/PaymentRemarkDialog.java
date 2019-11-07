package com.sgo.saldomu.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class PaymentRemarkDialog extends DialogFragment {
    View v;

    Button ok, cancel;
    EditText inputMsg;
    static String paymentCode;
    LinearLayout layoutNoId, layoutDueDate;
    Realm realmBBS;
    onTap listener;
    BBSCommModel comm;
    private RealmResults<BBSBankModel> listbank;
    private ArrayAdapter<String> bankAdapter;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;
    private String dateNow;
    private String dedate="";
    private String due_date;
    private String noId="";
    private DateFormat fromFormat;
    private DateFormat toFormat2;
    private TextView tvDueDate;
    EditText etNoId;
    TextView tvLabelId;

    public interface onTap {
        void onOK(String msg, String s, String dedate);
    }

    public static PaymentRemarkDialog newDialog(onTap listener, String payment_code) {
        PaymentRemarkDialog dialog = new PaymentRemarkDialog();
        paymentCode = payment_code;
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window dialog = getDialog().getWindow();
        if (dialog != null) {
            dialog.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            dialog.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.payment_remark_dialog_layout, container, false);

        realmBBS = Realm.getInstance(RealmManager.BBSConfiguration);

        ok = v.findViewById(R.id.pay_remark_dialog_ok_btn);
        cancel = v.findViewById(R.id.pay_remark_dialog_cancel_btn);
        inputMsg = v.findViewById(R.id.pay_remark_dialog_input_msg);
        layoutNoId = v.findViewById(R.id.layout_id_no);
        etNoId = v.findViewById(R.id.et_id_no);
        layoutDueDate = v.findViewById(R.id.layout_duedate);
        tvDueDate = v.findViewById(R.id.tv_due_date);
        tvLabelId = v.findViewById(R.id.lbl_noId);

        if (!paymentCode.equalsIgnoreCase(DefineValue.CT_CODE)){
            if (paymentCode.equalsIgnoreCase("BG")) {
                layoutNoId.setVisibility(View.VISIBLE);
                layoutDueDate.setVisibility(View.VISIBLE);
            } else if (paymentCode.equalsIgnoreCase("TS")) {
                layoutNoId.setVisibility(View.VISIBLE);
                tvLabelId.setText("No. Slip");
            }
        }


//        initBankList();

        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        toFormat2 = new SimpleDateFormat("dd-M-yyyy", new Locale("ID", "INDONESIA"));

        Calendar c = Calendar.getInstance();
        dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:" + dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        tvDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getActivity().getFragmentManager(), "asd");
            }
        });

        return v;
    }

//    public void initBankList() {
//        comm = realmBBS.where(BBSCommModel.class)
//                .equalTo(WebParams.SCHEME_CODE, DefineValue.CTA).findFirst();
//
//        listbank = realmBBS.where(BBSBankModel.class)
//                .equalTo(WebParams.SCHEME_CODE, DefineValue.CTA)
//                .equalTo(WebParams.COMM_TYPE, DefineValue.BENEF).findAll();
//
//        ArrayList<String> productNameArrayList = new ArrayList<>();
//        for (int i = 0; i < listbank.size(); i++) {
//            productNameArrayList.add(listbank.get(i).getProduct_name());
//        }
//
//        bankAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_text_primary_dark, productNameArrayList);
//        spBank.setAdapter(bankAdapter);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValidation())
                {
                    listener.onOK(inputMsg.getText().toString(), etNoId.getText().toString(), dedate);
                    dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public boolean inputValidation() {
        int compare = 100;
        if (due_date != null) {
            Date duedate = null;
            Date now = null;
            try {
                duedate = fromFormat.parse(due_date);
                now = fromFormat.parse(dateNow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (duedate != null) {
                if (now != null) {
                    compare = duedate.compareTo(now);
                }
            }
            Timber.d("compare date:" + Integer.toString(compare));
        }

        if (!paymentCode.equalsIgnoreCase("CT")) {
            if (etNoId.getText().toString().length() == 0) {
                etNoId.requestFocus();
                etNoId.setError("Nomor harus diisi!");
                return false;
            }

            if (paymentCode.equalsIgnoreCase("BG")) {
                if (tvDueDate.getText().toString().equalsIgnoreCase("Tekan untuk memilih tanggal")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Alert")
                            .setMessage("Tanggal efektif harus diisi")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return false;
                }
                if (compare <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Alert")
                            .setMessage("Tanggal tidak boleh kurang dari hari ini")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return false;
                }
            }
        }


        return true;
    }

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker duedate");
            try {
                due_date = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker duedate masuk tanggal : " + due_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvDueDate.setText(dedate);
        }
    };
}
