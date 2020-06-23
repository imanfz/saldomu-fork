package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.Invoice;
import com.sgo.saldomu.models.InvoiceDGI;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CancelInvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnItemClick {
        void onEdit(Invoice obj);
        void onCheck(Invoice obj);
        void onUncheck(Invoice obj);
    }

    OnItemClick listener;
    Context context;
    ArrayList<InvoiceDGI> invoiceDGIModelArrayList;
    private ArrayList<String> reasonNameArrayList = new ArrayList<>();
    String reason = "";

    public CancelInvoiceAdapter(ArrayList<InvoiceDGI> invoiceDGIModelArrayList, ArrayList<String> reasonNameArrayList, OnItemClick listener) {
        this.invoiceDGIModelArrayList = invoiceDGIModelArrayList;
        this.reasonNameArrayList = reasonNameArrayList;
        this.listener = listener;
    }

    @NonNull
    public CancelInvoiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new CancelInvoiceAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cancel_invoice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        InvoiceDGI item = invoiceDGIModelArrayList.get(position);

        ViewHolder holder = (ViewHolder) viewHolder;
        holder.title_video_text.setText( context.getString(R.string.invoice) +" " + item.getDoc_no());

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, reasonNameArrayList);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.sp_reason.setAdapter(spinAdapter);
        holder.reason_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 1) {
                    reason = "";
                } else {
                    reason = editable.toString();
                }

                listener.onEdit(new Invoice(String.valueOf(position), reason, item.getDoc_no()));
            }
        });

        holder.sp_reason.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(true);
                    }
                }
                return false;
            }
        });

        holder.sp_reason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (holder.checkBox.isChecked()) {
                    reason = holder.sp_reason.getItemAtPosition(i).toString();
                    if (reason.equalsIgnoreCase("LAINNYA")) {
                        holder.reason_et.setText("");
                        holder.reason_et.setVisibility(View.VISIBLE);
                    } else {
                        holder.reason_et.setVisibility(View.GONE);
                    }
                    listener.onEdit(new Invoice(String.valueOf(position), reason, item.getDoc_no()));
//                    listener.onCheck(new Invoice(String.valueOf(position), reason, item.getDoc_no()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                if (reason.equals("LAINNYA")) {
//                    if (holder.reason_et.getText().toString().isEmpty()) {
//                        return;
//                    }
//                    reason = holder.reason_et.getText().toString();
//                }

                if (!isChecked) {
                    listener.onUncheck(new Invoice(String.valueOf(position), reason, item.getDoc_no()));
                    holder.sp_reason.setVisibility(View.GONE);
                } else {
                    reason = holder.sp_reason.getItemAtPosition(0).toString();
                    listener.onCheck(new Invoice(String.valueOf(position), reason, item.getDoc_no()));
                    holder.sp_reason.setVisibility(View.VISIBLE);
//                    holder.sp_reason.performClick();
                }
//                else {
//                    listener.onCheck(new Invoice(String.valueOf(position), reason, item.getDoc_no()));
//                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return invoiceDGIModelArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView title_video_text;
        RelativeLayout content_rl;
        Spinner sp_reason;
        EditText reason_et;

        public ViewHolder(View itemView) {
            super(itemView);
            content_rl = itemView.findViewById(R.id.content_rl);
            checkBox = itemView.findViewById(R.id.checkBox);
            title_video_text = itemView.findViewById(R.id.invoice_tv);
            sp_reason = itemView.findViewById(R.id.sp_reason);
            reason_et = itemView.findViewById(R.id.reason_et);
        }
    }
}
