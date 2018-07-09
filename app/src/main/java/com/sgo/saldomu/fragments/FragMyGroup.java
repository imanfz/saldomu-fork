package com.sgo.saldomu.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.view.*;
import android.widget.AbsListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.Beans.MyGroupObject;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.CreateGroupActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.adapter.MyGroupAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogLogout;
import com.sgo.saldomu.dialogs.DefinedDialog;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import timber.log.Timber;

/**
 * Created by thinkpad on 4/9/2015.
 */
public class FragMyGroup extends ListFragment {

    private SecurePreferences sp;

    private ProgressDialog progdialog;
    private PtrFrameLayout ptrFrameLayout;
    private ArrayList<MyGroupObject> groups = null;
    private int sectionPosition = 0;
    private int listPosition = 0;

    private String _ownerID,accessKey;
    private String page = "0";

    private MyGroupAdapter myGroupAdapter;

    @Override
    public void onStart() {
        super.onStart();

        sectionPosition = 0;
        listPosition = 0;
        page = "0";
        groups = new ArrayList<>();
        groups.clear();
        getGroupList();
        initializeAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_my_group, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        ptrFrameLayout = (PtrFrameLayout) getActivity().findViewById(R.id.mygroup_ptr_frame);

        StoreHouseHeader header = new StoreHouseHeader(getActivity().getApplicationContext());
        header.setPadding(0, 20, 0, 20);
        header.setTextColor(Color.BLACK);
        header.initWithString(getString(R.string.updating));

        ptrFrameLayout.setDurationToCloseHeader(1500);
        ptrFrameLayout.setHeaderView(header);
        ptrFrameLayout.addPtrUIHandler(header);
        ptrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page = Integer.toString(Integer.parseInt(page) + 1);
                        getGroupList();
                        ptrFrameLayout.refreshComplete();
                    }
                }, 1800);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return !canScrollUp(content);
            }

            public boolean canScrollUp(View view) {
                if (android.os.Build.VERSION.SDK_INT < 14) {
                    if (view instanceof AbsListView) {
                        final AbsListView absListView = (AbsListView) view;
                        return absListView.getChildCount() > 0
                                && (absListView.getFirstVisiblePosition() > 0 || absListView
                                .getChildAt(0).getTop() < absListView.getPaddingTop());
                    } else {
                        return view.getScrollY() > 0;
                    }
                } else {
                    return ViewCompat.canScrollVertically(view, -1);
                }
            }

        });

    }

    private void getGroupList() {
        try {
            progdialog = DefinedDialog.CreateProgressDialog(getActivity(), "");
            progdialog.show();

            RequestParams params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_GROUP_LIST,
                    _ownerID,accessKey);
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.PAGE, page);
            params.put(WebParams.COUNT, DefineValue.COUNT);
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);

            Timber.d("isi params get group list:" + params.toString());

            MyApiClient.getGroupList(getActivity(),params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog.dismiss();

                    try {
                        String code = response.getString(WebParams.ERROR_CODE);
                        String count = response.getString(WebParams.COUNT);

                        if (code.equals(WebParams.SUCCESS_CODE) && !count.equals("0")) {
                            Timber.d("isi params group list:"+response.toString());
                            JSONArray mArrayGroup = new JSONArray(response.getString(WebParams.DATA_GROUP));
                            for(int i = 0 ; i < mArrayGroup.length() ; i++) {
                                String groupid = mArrayGroup.getJSONObject(i).getString(WebParams.GROUP_ID);
                                String groupName = mArrayGroup.getJSONObject(i).getString(WebParams.GROUP_NAME);
//                                String groupDesc = mArrayGroup.getJSONObject(i).getString(WebParams.GROUP_DESC);

                                boolean flagSame = false;

                                // cek apakah ada group id yang sama.. kalau ada tidak dimasukan ke array
                                if(groups.size() > 0) {
                                    for (int index = 0; index < groups.size(); index++) {
                                        if (!groups.get(index).getGroupID().equals(groupid)) {
                                            flagSame = false;
                                        } else {
                                            flagSame = true;
                                            break;
                                        }
                                    }
                                }

                                if(!flagSame) {
                                    MyGroupObject myGroupObject = new MyGroupObject();
                                    myGroupObject.setType(0);
                                    myGroupObject.setGroupID(groupid);
                                    myGroupObject.setGroupName(groupName);
                                    myGroupObject.setSectionPosition(sectionPosition);
                                    myGroupObject.setListPosition(listPosition++);
                                    groups.add(myGroupObject);

                                    if(!mArrayGroup.getJSONObject(i).getString(WebParams.MEMBERS).equals("")) {
                                        JSONArray mArrayMember = new JSONArray(mArrayGroup.getJSONObject(i).getString(WebParams.MEMBERS));

                                        for (int j = 0; j < mArrayMember.length(); j++) {
//                                            String memberid = mArrayMember.getJSONObject(j).getString(WebParams.MEMBER_ID);
                                            String memberName = mArrayMember.getJSONObject(j).getString(WebParams.MEMBER_NAME);
                                            String memberProfilePicture = mArrayMember.getJSONObject(j).getString(WebParams.MEMBER_PROFILE_PICTURE);

                                            MyGroupObject myMemberObject = new MyGroupObject();
                                            myMemberObject.setType(1);
                                            myMemberObject.setGroupID(groupid);
                                            myMemberObject.setGroupName(groupName);
                                            myMemberObject.setMemberName(memberName);
                                            myMemberObject.setMemberProfilePicture(memberProfilePicture);
                                            myMemberObject.setSectionPosition(sectionPosition);
                                            myMemberObject.setListPosition(listPosition++);
                                            groups.add(myMemberObject);
                                        }

                                    }
                                    sectionPosition++;
                                }
                            }

                            myGroupAdapter.notifyDataSetChanged();
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            Timber.d("isi error group list:"+response.toString());
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getActivity(), code, Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (JSONException e) {
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

                private void failure(Throwable throwable){
                    if(MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getActivity(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    if(progdialog.isShowing())
                        progdialog.dismiss();
                    Timber.w("Error Koneksi get group list Mygroup:"+throwable.toString());
                }
            });

        }catch (Exception e){
            Timber.d("httpclient:"+e.getMessage());
        }

    }

    @SuppressLint("NewApi")
    private void initializeAdapter() {
        myGroupAdapter = new MyGroupAdapter(getActivity().getApplicationContext(), groups);
        setListAdapter(myGroupAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("options menu add contact");
        inflater.inflate(R.menu.create_group, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_create_group:
                Intent i = new Intent(getActivity(), CreateGroupActivity.class);
                switchActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }
}
