package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.BankHeaderTopUp;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;

import java.util.ArrayList;
import java.util.List;

public class BankListTopupAdapter extends RecyclerView.Adapter<BankListTopupAdapter.holder> {

    Context context;
    List<BankHeaderTopUp> listDataHeader;
    OnClick listener;
    BankDataTopUp temp_other_atm;

    public interface OnClick{
        void onClick(ArrayList<listBankModel> bankData);
    }

    public BankListTopupAdapter(Context _context, BankDataTopUp temp_other_atm, List<BankHeaderTopUp> listDataHeader, OnClick listener){
        context = _context;
        this.listDataHeader = listDataHeader;
        this.listener = listener;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.list_topup_group_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final holder holder, final int position) {
        holder.bankName.setText(listDataHeader.get(position).getHeader());

        int logoId = 0;
        String bankCode = listDataHeader.get(position).getBankCode();
        if (bankCode != null) {

            switch (bankCode) {
                case DefineValue.BankJatim:
                    logoId = R.drawable.logo_bank_jatim_small;
                    break;
                case DefineValue.BankBCA:
                    logoId = R.drawable.logo_bca_bank_small;
                    break;
                case DefineValue.BankMandiri:
                    logoId = R.drawable.logo_mandiri_bank_small;
                    break;
                case DefineValue.BankMaspion:
                    logoId = R.drawable.logo_bank_maspion_rev1_small;
                    break;
                case DefineValue.BankPermata:
                    logoId = R.drawable.logo_bank_permata;
                    break;
                case DefineValue.BankBII:
                    logoId = R.drawable.logo_maybank;
                    break;
                case DefineValue.BankUOB:
                    logoId = R.drawable.logo_bank_uob_small;
                    break;
                case DefineValue.BankBRI:
                    logoId = R.drawable.logo_bank_bri_small;
                    break;
                case DefineValue.BankBNI:
                    logoId = R.drawable.logo_bank_bni;
                    break;
                case DefineValue.BankDanamon:
                    logoId = R.drawable.danamon_small;
                    break;
                case DefineValue.BankCIMB:
                    logoId = R.drawable.cimb_small;
                    break;
            }

            holder.logoHolder.setImageResource(logoId);
            holder.logoHolder.setVisibility(View.VISIBLE);
        }else holder.logoHolder.setVisibility(View.GONE);

//        if (listDataHeader.get(position).getHeader().equals(context.getString(R.string.other_bank)))
//            holder.logoHolder.setVisibility(View.GONE);
//        else {
//            holder.logoHolder.setImageResource(logoId);
//            holder.logoHolder.setVisibility(View.VISIBLE);
//        }

        temp_other_atm = DataManager.getInstance().getTemp_other_atm();
        if (temp_other_atm != null) {
            if (listDataHeader.get(position).getHeader().equals(context.getString(R.string.other_bank))) {
                if (temp_other_atm.getFee() == null) {
                    holder.feeDescOthers.setText("");
                } else
                    holder.feeDescOthers.setText(context.getString(R.string.listatm_topup_deskripsi_fee, temp_other_atm.getFee()));
            }

            holder.pinAccOthers.setText(temp_other_atm.getNoVa());
        }




        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listDataHeader.get(position).getHeader().equals(context.getString(R.string.other_bank))) {
                    if (holder.otherATM.getVisibility() == View.GONE){
                        holder.otherATM.setVisibility(View.VISIBLE);
                    }else holder.otherATM.setVisibility(View.GONE);
                }else {
                    listener.onClick(listDataHeader.get(position).getBankData());
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return listDataHeader.size();
    }

    class holder extends ViewHolder{

        LinearLayout parent;
        TextView bankName, feeDescOthers, pinAccOthers;
        ImageView logoHolder;
        View otherATM;

        public holder(View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.adapter_bank_list_parent);
            logoHolder = itemView.findViewById(R.id.adapter_bank_list_logo);
            bankName = itemView.findViewById(R.id.adapter_bank_list_group_title);
            otherATM = itemView.findViewById(R.id.adapter_bank_list_otheratm);
            feeDescOthers = itemView.findViewById(R.id.fee_deskripsi);
            pinAccOthers = itemView.findViewById(R.id.pin_account);
        }
    }
}
