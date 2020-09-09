package com.sgo.saldomu.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.utils.NumberTextWatcherForThousand;

public class InputAmountTagihBillerDialog extends DialogFragment {

    View view;
    OnTap listener;
    InvoiceDGI obj;
    int pos;
    String partialPayment;

    EditText inpAmount;
    TextView lbl_partial, lbl_remain_amount;
    Button btnDone, btnCancel;
    TableRow inputInvLayout;

    public interface OnTap {
        void onTap(int pos, String value);
    }

    public static InputAmountTagihBillerDialog newDialog(int pos, InvoiceDGI obj, String partialPayment, OnTap listener) {
        InputAmountTagihBillerDialog dialog = new InputAmountTagihBillerDialog();
        dialog.partialPayment = partialPayment;
        dialog.obj = obj;
        dialog.listener = listener;
        dialog.pos = pos;

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window dialog = getDialog().getWindow();
        if (dialog != null) {
            dialog.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_input_invoicedgi, container, false);

        getDialog().setTitle(getString(R.string.invoice_dgi_payment));
        inpAmount = view.findViewById(R.id.inpAmount);
        inpAmount.addTextChangedListener(new NumberTextWatcherForThousand(inpAmount));
        btnDone = view.findViewById(R.id.btnDone);
        btnCancel = view.findViewById(R.id.btnCancel);
        inputInvLayout = view.findViewById(R.id.tableInvoiceAmount);


        TextView lbl_doc_no = view.findViewById(R.id.lbl_doc_no);
        lbl_doc_no.setText(obj.getDoc_no());

        TextView lbl_doc_desc = view.findViewById(R.id.lbl_doc_desc);
        if (obj.getDoc_desc() == null)
            lbl_doc_desc.setText("-");
        else
            lbl_doc_desc.setText(obj.getDoc_desc());

        TextView lbl_amount = view.findViewById(R.id.lbl_amount);
        lbl_amount.setText(CurrencyFormat.format(obj.getAmount()));

        lbl_remain_amount = view.findViewById(R.id.lbl_remain_amount);
        lbl_remain_amount.setText(CurrencyFormat.format(obj.getRemain_amount()));

        lbl_partial = view.findViewById(R.id.lbl_partial);
//        lbl_partial.setText(obj.getInput_amount());


//        else if(str_partial.equalsIgnoreCase("N")){
//            partial = "Tidak";
//        }
//        else{
//            partial = "Bisa Lebih";
//        }
//        if(partial.equalsIgnoreCase("Ya") || partial.equalsIgnoreCase("Bisa Lebih")){
//            showInvoiceAmount(view);
//        }else{
//            hideInvoiceAmount(view);
//        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (partialPayment.equalsIgnoreCase("Y")) {
            showInvoiceAmount();
            lbl_partial.setText("Yes");
        } else if (partialPayment.equalsIgnoreCase("O")) {
            showInvoiceAmount();
            lbl_partial.setText("Over/Bisa Lebih");
        } else lbl_partial.setText("No");

        btnDone.setOnClickListener(v -> {
            String input = NumberTextWatcherForThousand.trimCommaOfString(inpAmount.getText().toString());

            if (input.equalsIgnoreCase("") || inpAmount.getVisibility() == View.GONE) {
                input = obj.getRemain_amount();
            }

            if (checkInput(input)) {
                listener.onTap(pos, input);

                dismiss();
            } else
                Toast.makeText(getActivity(), "Jumlah input tidak sesuai", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    boolean checkInput(String input) {
        return Integer.valueOf(input) <= Integer.valueOf(obj.getRemain_amount()) &&
                !input.substring(0, 1).equalsIgnoreCase("0");
    }

    void showInvoiceAmount() {
        inputInvLayout.setVisibility(View.VISIBLE);
        inpAmount.setText(obj.getInput_amount());
        if (inpAmount.getText().toString().equalsIgnoreCase("0")) {
            inpAmount.setText("");
        }
    }
}
