package com.sgo.saldomu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.activities.TutorialActivity;
import com.sgo.saldomu.adapter.ListAccountBBSAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.RealmManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.securities.RSA;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;


public class ListAccountBBS extends BaseFragment implements View.OnClickListener {

    public final static String TAG = "com.sgo.saldomu.fragments.ListAccountBBS";

    private View v;
    private ListView lv;
    private ListAccountBBSAdapter listAccountBBSAdapter;
    private BBSCommModel dataComm;
    private RealmResults<BBSAccountACTModel> listDataAccount;
    private ArrayAdapter<String> adapterDataComm;
    private Spinner spinCommunity;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialogDelete;
    private ActionListener actionListener;
    private TextView tvCommName;
    Realm realm;

    public interface ActionListener{
        void OnAddAccountListener(Bundle data);
        void OnUpdateAccountListener(Bundle data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = DefinedDialog.CreateProgressDialog(getContext(),"");
        progressDialog.dismiss();

        realm = Realm.getInstance(RealmManager.BBSConfiguration);

        initializeData();
        if(listAccountBBSAdapter == null) {

            listAccountBBSAdapter = new ListAccountBBSAdapter(getContext(),
                    R.layout.list_account_bbs_item, listDataAccount);
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
        tvCommName = (TextView) v.findViewById(R.id.tv_comm_value);
        View layout_empty = v.findViewById(R.id.empty_layout);
        layout_empty.findViewById(R.id.btnRefresh).setOnClickListener(this);
        lv.setEmptyView(layout_empty);
        View footerView =  ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_list_view, null, false);
        lv.addFooterView(footerView);


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
                refreshDataList();
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

        checkingData();

    }

    private void validasiTutorial()
    {
        if(sp.contains(DefineValue.TUTORIAL_REGISTER_AGEN))
        {
            Boolean is_first_time = sp.getBoolean(DefineValue.TUTORIAL_REGISTER_AGEN,false);
            if(is_first_time)
                showTutorial();
        }
        else {
            showTutorial();
        }
    }

    private void showTutorial()
    {
        Intent intent = new Intent(getActivity(), TutorialActivity.class);
        intent.putExtra(DefineValue.TYPE, TutorialActivity.tutorial_registerAgen);
        startActivity(intent);
    }

    private void ToUpdateFragment(int positionAcct){
        Bundle data = new Bundle();
        BBSAccountACTModel accountBBS = listDataAccount.get(positionAcct);
        data.putBoolean(DefineValue.IS_UPDATE,true);
        data.putString(DefineValue.COMMUNITY_NAME,dataComm.getComm_name());
        data.putString(DefineValue.COMMUNITY_CODE,dataComm.getComm_code());
        data.putString(DefineValue.COMMUNITY_ID,dataComm.getComm_id());
        data.putString(DefineValue.MEMBER_CODE,dataComm.getMember_code());
        data.putString(DefineValue.PRODUCT_TYPE,accountBBS.getProduct_type());
        data.putString(DefineValue.PRODUCT_CODE,accountBBS.getProduct_code());
        data.putString(DefineValue.PRODUCT_NAME,accountBBS.getProduct_name());
        data.putString(DefineValue.NO_BENEF,accountBBS.getAccount_no());
        data.putString(DefineValue.NAME_BENEF,accountBBS.getAccount_name());
        if(!accountBBS.getAccount_city().isEmpty())
            data.putString(DefineValue.BENEF_CITY,accountBBS.getAccount_city());

        actionListener.OnUpdateAccountListener(data);
    }


    private void refreshDataList(){
        retrieveAccountBBS();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.information, menu);
        inflater.inflate(R.menu.delete_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                showTutorial();
                return true;
            case R.id.menu_item_delete:
                listAccountBBSAdapter.toggleButtonDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeFragmentAdd(){
        Bundle data = new Bundle();
        data.putBoolean(DefineValue.IS_UPDATE,false);
        data.putString(DefineValue.COMMUNITY_NAME,dataComm.getComm_name());
        data.putString(DefineValue.COMMUNITY_CODE,dataComm.getComm_code());
        data.putString(DefineValue.COMMUNITY_ID,dataComm.getComm_id());
        data.putString(DefineValue.MEMBER_CODE,dataComm.getMember_code());
        actionListener.OnAddAccountListener(data);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnRefresh:
                refreshDataList();
                break;
        }
    }

    @Override
    public void onDestroy() {
        RetrofitService.dispose();
        RealmManager.closeRealm(realm);
        super.onDestroy();
    }

    void initializeData(){
        dataComm = realm.where(BBSCommModel.class).
                equalTo(BBSCommModel.SCHEME_CODE,DefineValue.ATC).findFirst();

        if(dataComm != null) {
            listDataAccount = realm.where(BBSAccountACTModel.class).findAll();
        }
    }

    void checkingData(){
        if(dataComm == null){
            Toast.makeText(getActivity(), R.string.joinagentbbs_toast_empty_comm, Toast.LENGTH_LONG).show();
            finishThis();
        }
        else {
            tvCommName.setText(dataComm.getComm_name());
            tvCommName.setVisibility(View.VISIBLE);
            spinCommunity.setVisibility(View.GONE);
            validasiTutorial();
        }
    }

    void finishThis(){
        if(getFragmentManager().getBackStackEntryCount()>0)
            getFragmentManager().popBackStack();
        else
            getActivity().finish();
    }

    private void retrieveAccountBBS(){
        listDataAccount = realm.where(BBSAccountACTModel.class).findAll();
        adapterDataComm.notifyDataSetChanged();
    }


    private void deleteBBSAccount(final int position, String tokenId){
        try{
            progressDialog.show();

            extraSignature = dataComm.getComm_code()+dataComm.getMember_code()+
                    listDataAccount.get(position).getProduct_type()+ listDataAccount.get(position).getProduct_code()
                    + listDataAccount.get(position).getAccount_no();
            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_BBS_BANK_ACCOUNT_DELETE, extraSignature);

            params.put(WebParams.COMM_CODE, dataComm.getComm_code());
            params.put(WebParams.MEMBER_CODE, dataComm.getMember_code());
            params.put(WebParams.PRODUCT_CODE, listDataAccount.get(position).getProduct_code());
            params.put(WebParams.PRODUCT_TYPE, listDataAccount.get(position).getProduct_type());
            params.put(WebParams.BENEF_ACCT_NO, listDataAccount.get(position).getAccount_no());
            params.put(WebParams.TOKEN_ID, RSA.opensslEncrypt(tokenId));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            params.put(WebParams.USER_ID, userPhoneID);
            Timber.d("isi params deleteAccountList:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_BBS_BANK_ACCOUNT_DELETE, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                Timber.d("Isi response deleteAccountList: "+response.toString());
                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    realm.beginTransaction();
                                    listAccountBBSAdapter.deleteItem(position);
                                    realm.commitTransaction();
                                } else {
                                    code = response.getString(WebParams.ERROR_MESSAGE);
                                    Toast.makeText(getActivity(),code, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            finally {

                                listAccountBBSAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progressDialog.dismiss();
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
                    listDataAccount.get(position).getAccount_name(),
                    listDataAccount.get(position).getAccount_no(),
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
