package com.sgo.hpku.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.Beans.AccountBBS;
import com.sgo.hpku.Beans.BBSComm;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.adapter.ListAccountBBSAdapter;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.coreclass.MyApiClient;
import com.sgo.hpku.coreclass.WebParams;
import com.sgo.hpku.dialogs.DefinedDialog;
import com.sgo.hpku.securities.Md5;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import timber.log.Timber;


public class ListAccountBBS extends Fragment implements View.OnClickListener {

    public final static String TAG = "com.sgo.hpku.fragments.ListAccountBBS";

    private View v;
    private ListView lv;
    private ListAccountBBSAdapter listAccountBBSAdapter;
    private ArrayList<BBSComm> listDataComm;
    private ArrayList<AccountBBS> listDataAccount;
    private PtrFrameLayout ptrFrameLayout;
    private ProgressBar loadProgSpin;
    private String userID;
    private String accessKey;
    private ArrayAdapter<String> adapterDataComm;
    private Spinner spinCommunity;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialogDelete;
    private ActionListener actionListener;
    private TextView tvCommName;

    public interface ActionListener{
        void OnAddAccountListener();
        void OnUpdateAccountListener(Bundle data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurePreferences sp = CustomSecurePref.getInstance().getmSecurePrefs();
        userID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");

        listDataComm = new ArrayList<>();
        listDataAccount = new ArrayList<>();
        progressDialog = DefinedDialog.CreateProgressDialog(getContext(),"");
        progressDialog.dismiss();


        if(listAccountBBSAdapter == null) {
            listAccountBBSAdapter = new ListAccountBBSAdapter(getContext(), R.layout.list_account_bbs_item, listDataAccount);
            listAccountBBSAdapter.setDeleteListener(new ListAccountBBSAdapter.OnDeleteListener() {
                @Override
                public void onCLick(int position, View view) {
                    showDialogDelete(position);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_account_bbs, container, false);
        lv = (ListView) v.findViewById(R.id.rek_listview);
        spinCommunity = (Spinner) v.findViewById(R.id.spinner_community);
        loadProgSpin = (ProgressBar) v.findViewById(R.id.loading_progres_comm);
        tvCommName = (TextView) v.findViewById(R.id.tv_comm_value);
        View layout_empty = v.findViewById(R.id.empty_layout);
        layout_empty.findViewById(R.id.btnRefresh).setOnClickListener(this);
        lv.setEmptyView(layout_empty);
        ptrFrameLayout = (PtrFrameLayout) v.findViewById(R.id.rotate_header_list_view_frame);

        final MaterialHeader header = new MaterialHeader(getActivity());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, 15, 0, 10);
        header.setPtrFrameLayout(ptrFrameLayout);

        ptrFrameLayout.setHeaderView(header);
        ptrFrameLayout.addPtrUIHandler(header);
        ptrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                refreshDataList();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return !(adapterDataComm == null || lv == null || spinCommunity.getCount() == 0) && !(lv.getAdapter() == null && lv.getChildAt(0) == null) && ((adapterDataComm.getCount() == 0 || lv.getAdapter().getCount() == 0) || lv.getFirstVisiblePosition() == 0 && lv.getChildAt(0).getTop() == 0);

            }
        });

        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_account);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragmentAdd();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() instanceof ActionListener) {
            actionListener = (ActionListener) getTargetFragment();
        } else {
            if(context instanceof ActionListener){
                actionListener = (ActionListener) context;
            }
            else {
                throw new RuntimeException(context.toString()
                        + " must implement ActionListener");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainPage.ACTIVITY_RESULT) {
            if (resultCode == MainPage.RESULT_OK) {
                ptrFrameLayout.autoRefresh();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated");

        ArrayList<String> spinDataComm = new ArrayList<>();
        adapterDataComm = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinDataComm);
        adapterDataComm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCommunity.setAdapter(adapterDataComm);
        spinCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Timber.wtf("masuk select disni");
                if(listDataComm.size()>0)
                    ptrFrameLayout.autoRefresh();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(lv.getAdapter() == null) {
            lv.setAdapter(listAccountBBSAdapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ToUpdateFragment(position);
                }
            });
        }

        retrieveComm();
    }

    private void ToUpdateFragment(int positionAcct){
        Bundle data = new Bundle();
        BBSComm bbsComm = listDataComm.get(spinCommunity.getSelectedItemPosition());
        AccountBBS accountBBS = listDataAccount.get(positionAcct);
        data.putBoolean(DefineValue.IS_UPDATE,true);
        data.putString(DefineValue.COMMUNITY_NAME,bbsComm.getCommName());
        data.putString(DefineValue.COMMUNITY_CODE,bbsComm.getCommCode());
        data.putString(DefineValue.COMMUNITY_ID,bbsComm.getCommId());
        data.putString(DefineValue.MEMBER_CODE,bbsComm.getMemberCode());
        data.putString(DefineValue.PRODUCT_TYPE,accountBBS.getProduct_type());
        data.putString(DefineValue.PRODUCT_CODE,accountBBS.getProduct_code());
        data.putString(DefineValue.PRODUCT_NAME,accountBBS.getProduct_name());
        data.putString(DefineValue.NO_BENEF,accountBBS.getBenef_acct_no());
        data.putString(DefineValue.NAME_BENEF,accountBBS.getBenef_acct_name());
        if(!accountBBS.getBenef_acct_city().isEmpty())
            data.putString(DefineValue.BENEF_CITY,accountBBS.getBenef_acct_city());

        actionListener.OnUpdateAccountListener(data);
    }


    private void refreshDataList(){
        retrieveAccountBBS(listDataComm.get(spinCommunity.getSelectedItemPosition()).getCommCode(),
                            listDataComm.get(spinCommunity.getSelectedItemPosition()).getMemberCode());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.delete_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_item_delete:
                listAccountBBSAdapter.toggleButtonDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeFragmentAdd(){
        actionListener.OnAddAccountListener();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnRefresh:
                if(listDataComm.size() > 0)
                    ptrFrameLayout.autoRefresh();
                break;
        }
    }

    @Override
    public void onDestroy() {
        MyApiClient.CancelRequestWSByTag(TAG,true);
        super.onDestroy();
    }

    private void CommunityUIRefresh(){
        if(listDataComm.size() < 1) {
            Toast.makeText(getActivity(), R.string.joinagentbbs_toast_empty_comm, Toast.LENGTH_LONG).show();
        }

        if(listDataComm.size() == 1) {
            tvCommName.setText(listDataComm.get(0).getCommName());
            tvCommName.setVisibility(View.VISIBLE);
            spinCommunity.setVisibility(View.INVISIBLE);
        }
        else {
            spinCommunity.setVisibility(View.VISIBLE);
        }
    }

    private void retrieveComm(){
        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_GLOBAL_BBS_COMM,
                    userID,accessKey);
            params.put(WebParams.CUSTOMER_ID, userID);
            params.put(WebParams.SCHEME_CODE, DefineValue.ATC);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params retreiveComm:" + params.toString());

            tvCommName.setVisibility(View.GONE);
            spinCommunity.setVisibility(View.GONE);
            loadProgSpin.setVisibility(View.VISIBLE);
            MyApiClient.getGlobalBBSComm(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response retreiveComm: "+response.toString());
                        listDataComm.clear();
                        adapterDataComm.clear();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            JSONArray comm = response.optJSONArray(WebParams.COMMUNITY);
                            if(comm != null && comm.length() > 0) {
                                BBSComm bbsComm;
                                for (int i = 0; i < comm.length(); i++) {
                                    bbsComm = new BBSComm(comm.getJSONObject(i).optString(WebParams.COMM_ID),
                                            comm.getJSONObject(i).optString(WebParams.COMM_CODE),
                                            comm.getJSONObject(i).optString(WebParams.COMM_NAME),
                                            comm.getJSONObject(i).optString(WebParams.API_KEY),
                                            comm.getJSONObject(i).optString(WebParams.MEMBER_CODE),
                                            comm.getJSONObject(i).optString(WebParams.CALLBACK_URL));
                                    listDataComm.add(bbsComm);
                                    adapterDataComm.add(bbsComm.getCommName());
                                }
                            }
                        }
                       else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                        adapterDataComm.notifyDataSetChanged();
                        CommunityUIRefresh();
                        loadProgSpin.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    Timber.w("Error Koneksi retreiveComm:"+throwable.toString());
                    loadProgSpin.setVisibility(View.GONE);
                }
            });
        }catch (Exception e){
            Timber.d("httpclient: "+e.getMessage());
        }
    }

    private void retrieveAccountBBS(String comm_code, String member_code){
//        if(ptrFrameLayout.isRefreshing()) {
//            Timber.d("sedang refresh");
//            MyApiClient.CancelRequestWSByTag(TAG, true);
//        }

        try{
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_BANK_ACCOUNT,
                    userID,accessKey);
            params.put(WebParams.COMM_CODE, comm_code);
            params.put(WebParams.MEMBER_CODE, member_code);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params retreiveAccountBBS:" + params.toString());

            MyApiClient.sentBBSBankAccountRetreive(getActivity(),TAG, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response retreiveAccountBBS: "+response.toString());
                        listDataAccount.clear();
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            JSONArray bank = response.optJSONArray(WebParams.BANK_ACCOUNT);
                            if(bank != null && bank.length() > 0) {
                                AccountBBS accountBBS;
                                for (int i = 0; i < bank.length(); i++) {
                                    accountBBS = new AccountBBS(bank.getJSONObject(i).optString(WebParams.PRODUCT_NAME),
                                            bank.getJSONObject(i).optString(WebParams.PRODUCT_CODE),
                                            bank.getJSONObject(i).optString(WebParams.PRODUCT_TYPE),
                                            bank.getJSONObject(i).optString(WebParams.BENEF_ACCT_NO),
                                            bank.getJSONObject(i).optString(WebParams.BENEF_ACCT_NAME),
                                            bank.getJSONObject(i).optString(WebParams.BENEF_ACCT_CITY));
                                    listDataAccount.add(accountBBS);
                                }
                            }
                        }else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        listAccountBBSAdapter.notifyDataSetChanged();
                        ptrFrameLayout.refreshComplete();

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    ptrFrameLayout.refreshComplete();
                    Timber.w("Error Koneksi retreiveAccountBBS:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void deleteBBSAccount(final int position, String tokenId){
        try{
            progressDialog.show();
            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_BBS_BANK_ACCOUNT_DELETE,
                    userID,accessKey);

            params.put(WebParams.COMM_CODE, listDataComm.get(spinCommunity.getSelectedItemPosition()).getCommCode());
            params.put(WebParams.MEMBER_CODE, listDataComm.get(spinCommunity.getSelectedItemPosition()).getMemberCode());
            params.put(WebParams.PRODUCT_CODE, listDataAccount.get(position).getProduct_code());
            params.put(WebParams.PRODUCT_TYPE, listDataAccount.get(position).getProduct_type());
            params.put(WebParams.BENEF_ACCT_NO, listDataAccount.get(position).getBenef_acct_no());
            params.put(WebParams.TOKEN_ID, Md5.hashMd5(tokenId));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userID);
            Timber.d("isi params deleteAccountList:" + params.toString());

            MyApiClient.sentBBSBankAccountDelete(getActivity(), params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        Timber.d("Isi response deleteAccountList: "+response.toString());
                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            if(listDataAccount.size() == 1)
                                listDataAccount.clear();
                            else
                                listDataAccount.remove(position);
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        progressDialog.dismiss();
                        listAccountBBSAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    failure(throwable);
                }

                private void failure(Throwable throwable) {
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    Timber.w("Error Koneksi deleteAccountList:"+throwable.toString());
                }
            });
        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }


    private void showDialogDelete(final int position){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(25, 0, 25, 0);
            input.setHint(R.string.login_edithint_pass);
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            linearLayout.addView(input,params);

            alertDialogBuilder.setTitle(getString(R.string.dialog_title_delete_bbs_acct));
            alertDialogBuilder.setMessage(getString(R.string.dialog_msg_delete_bbs_acct,
                    listDataAccount.get(position).getBenef_acct_name(),
                    listDataAccount.get(position).getBenef_acct_no(),
                    listDataAccount.get(position).getProduct_name()));
            alertDialogBuilder.setView(linearLayout);
            alertDialogBuilder.setPositiveButton(getString(R.string.yes), null);
            alertDialogBuilder.setNegativeButton(getString(R.string.no), null);
            alertDialogDelete = alertDialogBuilder.create();
            alertDialogDelete.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if(input.getText().toString().length() == 0 ) {
                                input.requestFocus();
                                input.setError(getString(R.string.login_validation_pass));
                            }
                            else {
                                dialog.dismiss();
                                deleteBBSAccount(position, input.getText().toString());
                            }
                        }
                    });
                }
            });
        alertDialogDelete.show();
    }


}
