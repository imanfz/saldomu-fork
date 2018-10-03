package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.sgo.saldomu.Beans.HelpModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.HelpAdapter;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/*
 Created by thinkpad on 6/9/2015.
 */
public class HelpFragment extends BaseFragment {

    private View v;
    private Activity act;
    private Activity act1;

    private ArrayList<HelpModel> listHelp;
    private HelpAdapter mAdapter;
    private ProgressDialog progdialog;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_help_center, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        act = getActivity();
        act1 = getActivity();

        listHelp = new ArrayList<>();
        ListView mListView = v.findViewById(R.id.lvHelpCenter);

        getHelpList();

        mAdapter = new HelpAdapter(act, listHelp, act1);
        mListView.setAdapter(mAdapter);
    }

    private void getHelpList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(act, "");
            progdialog.show();

            HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_HELP_LIST);
            params.put(WebParams.USER_ID, userPhoneID);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);
            Timber.d("isi params help list:" + params.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_HELP_LIST, params,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
                            try {
                                String code = response.getString(WebParams.ERROR_CODE);
                                String message = response.getString(WebParams.ERROR_MESSAGE);

                                if (code.equals(WebParams.SUCCESS_CODE)) {
                                    Timber.d("isi params help list:"+response.toString());
                                    String count = response.getString(WebParams.COUNT);
                                    if(count.equals("0")) {
                                        Timber.d("isi help list kosong");
                                    }
                                    else {
                                        JSONArray mArrayContact = new JSONArray(response.getString(WebParams.CONTACT_DATA));

                                        for (int i = 0; i < mArrayContact.length(); i++) {
                                            HelpModel helpModel = new HelpModel();
                                            helpModel.setId(mArrayContact.getJSONObject(i).getString(WebParams.ID));
                                            helpModel.setName(mArrayContact.getJSONObject(i).getString(WebParams.NAME));
                                            helpModel.setDesc(mArrayContact.getJSONObject(i).getString(WebParams.DESCRIPTION));
                                            helpModel.setPhone(mArrayContact.getJSONObject(i).getString(WebParams.CONTACT_PHONE));
                                            helpModel.setMail(mArrayContact.getJSONObject(i).getString(WebParams.CONTACT_EMAIL));
                                            listHelp.add(helpModel);
                                        }
                                        mAdapter.notifyDataSetChanged();
                                    }

                                }
                                else if(code.equals(WebParams.LOGOUT_CODE)){
                                    Timber.d("isi response autologout:"+response.toString());
                                    AlertDialogLogout test = AlertDialogLogout.getInstance();
                                    test.showDialoginActivity(act,message);
                                }
                                else {
                                    Toast.makeText(act, message, Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            if(progdialog.isShowing())
                                progdialog.dismiss();
                        }
                    });
        }
        catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }
    }

}
