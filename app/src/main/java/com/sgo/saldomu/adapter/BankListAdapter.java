package com.sgo.saldomu.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BankListAdapter extends RecyclerView.Adapter<BankListAdapter.BankViewHolder> {

    Context context;
    private List<HashMap<String, String>> bankList;
    OnClick listener;

    public BankListAdapter(Context _context, List<HashMap<String, String>> bankList, OnClick listener){
        context = _context;
        this.bankList = bankList;
        this.listener = listener;
    }

    public interface OnClick{
        void onClick(int position);
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BankViewHolder(LayoutInflater.from(context).inflate(R.layout.bbs_autocomplete_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BankViewHolder holder, final int position) {
        holder.txtBank.setText(bankList.get(position).get("txt"));
        int id = Integer.parseInt(Objects.requireNonNull(bankList.get(position).get("flag")));
        holder.imgBank.setImageResource(id);
        holder.layout.setOnClickListener(v -> {
            listener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

    class BankViewHolder extends RecyclerView.ViewHolder{
        private TextView txtBank;
        private ImageView imgBank;
        private LinearLayout layout;

        BankViewHolder(View itemView) {
            super(itemView);
            txtBank = itemView.findViewById(R.id.txt);
            imgBank = itemView.findViewById(R.id.flag);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
