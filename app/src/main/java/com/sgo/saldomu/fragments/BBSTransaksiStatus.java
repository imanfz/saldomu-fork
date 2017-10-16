package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;

/**
 * Created by thinkpad on 5/10/2017.
 */

public class BBSTransaksiStatus extends Fragment {
    public final static String TAG = "com.sgo.saldomu.fragments.BBSTransaksiStatus";
    private View v;
    private String transaksi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.bbs_transaksi_status, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null) {
            transaksi = bundle.getString(DefineValue.TRANSACTION);
            String txStatus = bundle.getString(DefineValue.TX_STATUS);

            TextView tvTitle = (TextView) v.findViewById(R.id.tv_title);
            ImageView imgStatus = (ImageView) v.findViewById(R.id.img_status);
            TextView tvStatus = (TextView) v.findViewById(R.id.tv_status);
            Button btnNextTrans = (Button) v.findViewById(R.id.next_transaction_btn);
            Button btnMenuUtama = (Button) v.findViewById(R.id.menu_utama_btn);
            tvTitle.setText(transaksi);

            String txMessage;
            if (txStatus.equals(DefineValue.SUCCESS)){
                imgStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_check_green_round, null));
                txMessage = getString(R.string.transaction_success);
            }else if(txStatus.equals(DefineValue.ONRECONCILED)){
                imgStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_process, null));
                txMessage = getString(R.string.transaction_pending);
            }else if(txStatus.equals(DefineValue.SUSPECT)){
                imgStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_fail_round, null));
                txMessage =  getString(R.string.transaction_suspect);
            }
            else if(!txStatus.equals(DefineValue.FAILED)){
                imgStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_fail_round, null));
                txMessage = getString(R.string.transaction)+" "+txStatus;
            }
            else {
                imgStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_fail_round, null));
                txMessage = getString(R.string.transaction_failed);
            }

            tvStatus.setText(txMessage);

            btnNextTrans.setOnClickListener(nextTransListener);
            btnMenuUtama.setOnClickListener(menuUtamaListener);
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    Button.OnClickListener nextTransListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment newFrag = new BBSTransaksiAmount();
            Bundle args = new Bundle();
            args.putString(DefineValue.TRANSACTION, transaksi);
            newFrag.setArguments(args);
            FragmentManager fm = getFragmentManager();
            fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getFragmentManager().beginTransaction().replace(R.id.bbsTransaksiFragmentContent , newFrag, BBSTransaksiAmount.TAG)
                    .addToBackStack(TAG).commit();
        }
    };

    Button.OnClickListener menuUtamaListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

}
