package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sgo.saldomu.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FragCancelTrxRequest#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragCancelTrxRequest extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "FragCancelTrxRequest";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnProses, btnCancel;
    private EditText etReason;
    private String txId;

    private CancelTrxRequestListener cpl;

    public interface CancelTrxRequestListener {
        public void onSuccessCancelTrx(String txId);
        public void onFailedCancelTrx(String txId);
    }

    public FragCancelTrxRequest() {
        // Required empty public constructor
        super();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragCancelTrxRequest.
     */
    // TODO: Rename and change types and number of parameters
    public static FragCancelTrxRequest newInstance(String param1, String param2) {
        FragCancelTrxRequest fragment = new FragCancelTrxRequest();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_cancel_trx_request, container, false);

        btnProses   = (Button) v.findViewById(R.id.btnProses);
        btnCancel   = (Button) v.findViewById(R.id.btnCancel);
        etReason    = (EditText) v.findViewById(R.id.etReason);

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason       = etReason.getText().toString();
                Boolean hasError    = false;

                if ( reason.equals("") ) {
                    hasError = true;
                    etReason.setError(getString(R.string.err_empty_cancel_reason));
                }

                if ( !hasError ) {
                    //call webservice
                }
                //getDialog().dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
