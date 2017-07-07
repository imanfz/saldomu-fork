package com.sgo.hpku.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.activeandroid.ActiveAndroid;
import com.google.gson.*;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.securepreferences.SecurePreferences;
import com.sgo.hpku.Beans.friendModel;
import com.sgo.hpku.Beans.myFriendModel;
import com.sgo.hpku.R;
import com.sgo.hpku.activities.AddByQRCodeActivity;
import com.sgo.hpku.activities.FriendsViewDetailActivity;
import com.sgo.hpku.activities.ListContactActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.adapter.MyFriendAdapter;
import com.sgo.hpku.coreclass.*;
import com.sgo.hpku.dialogs.AlertDialogLogout;
import com.sgo.hpku.dialogs.InformationDialog;
import com.u1aryz.android.lib.newpopupmenu.MenuItem;
import com.u1aryz.android.lib.newpopupmenu.PopupMenu;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/*
  Created by Administrator on 1/27/2015.
 */
public class ListMyFriends extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PopupMenu.OnItemSelectedListener {

    private static final int CONTACTS_LOADER = 10;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private View v;
    private View layout_check_contact;
    private View layout_list_contact;
    private View layout_loading_contact;
    private MyFriendAdapter mAdapter;
    private ArrayList<myFriendModel> mMFM;
    private ContentResolver mCR;
    private Button btn_check_contact;
    private Cursor mCursor;
    private CircleProgressBar loadingCircle;
    private SecurePreferences sp;
    private String _ownerID,isContactNew,accessKey;
    private PopupMenu mPopMenu;
    private EditText etSearchFriend;

    private InformationDialog dialogI;

    private final static int ASK_FOR_MONEY = 0;
    private final static int PAY = 1;
    private final static int VIEW_DETAIL = 2;

    private int positionSelected;
    private int mState;
    private int HIDE_MENU = 100;
    private int SHOW_MENU = 200;

    public ListMyFriends() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mState = HIDE_MENU;
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.frag_list_my_friends, container, false);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);

        positionSelected = position;
        mPopMenu.show(v);
    }

    @Override
    public void onItemSelected(MenuItem item) {
//        Fragment newFragment;
        Bundle args;
        switch (item.getItemId()) {
            case ASK_FOR_MONEY:
//                newFragment = new FragAskForMoney();
                args = new Bundle();
                args.putString("image", mMFM.get(positionSelected).getImg_url());
                args.putString("name", mMFM.get(positionSelected).getFull_name());
                args.putString("id", mMFM.get(positionSelected).getUser_id());
                args.putString("phone", mMFM.get(positionSelected).getFriend_number());
                args.putString("email", mMFM.get(positionSelected).getEmail());
//                newFragment.setArguments(args);
//                switchFragment(newFragment, getResources().getString(R.string.menu_item_title_ask_for_money), false);
                switchMenu(NavigationDrawMenu.MASK4MONEY,args);

                break;

            case PAY:
//                newFragment = new FragPayFriends();
                args = new Bundle();
                args.putString("image", mMFM.get(positionSelected).getImg_url());
                args.putString("name", mMFM.get(positionSelected).getFull_name());
                args.putString("id", mMFM.get(positionSelected).getUser_id());
                args.putString("phone", mMFM.get(positionSelected).getFriend_number());
                args.putString("email", mMFM.get(positionSelected).getEmail());
//                newFragment.setArguments(args);
//                switchFragment(newFragment, getResources().getString(R.string.menu_item_title_pay_friends), false);
                switchMenu(NavigationDrawMenu.MPAYFRIENDS,args);

                break;

            case VIEW_DETAIL:
                Intent i = new Intent(getActivity(), FriendsViewDetailActivity.class);
                i.putExtra("image", mMFM.get(positionSelected).getImg_url());
                i.putExtra("name", mMFM.get(positionSelected).getFull_name());
                i.putExtra("id", mMFM.get(positionSelected).getUser_id());
                i.putExtra("phone", mMFM.get(positionSelected).getFriend_number());
                i.putExtra("email", mMFM.get(positionSelected).getEmail());
                switchActivity(i, MainPage.ACTIVITY_RESULT);
                break;

        }
    }

    private void switchMenu(int IdxItemMenu, Bundle data){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchMenu(IdxItemMenu, data);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActiveAndroid.initialize(getActivity());
        mPopMenu = new PopupMenu(getActivity().getApplicationContext());
        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        accessKey = sp.getString(DefineValue.ACCESS_KEY,"");
        isContactNew = sp.getString(DefineValue.CONTACT_FIRST_TIME,"");
        mCR = getActivity().getContentResolver();

        layout_check_contact = v.findViewById(R.id.layout_check_contact);
        layout_list_contact = v.findViewById(R.id.layout_list_contact);
        layout_loading_contact = v.findViewById(R.id.layout_loading_contact);
        btn_check_contact = (Button) v.findViewById(R.id.btn_check_contact);
        loadingCircle = (CircleProgressBar) v.findViewById(R.id.listfriend_loading_circle);
        etSearchFriend = (EditText) v.findViewById(R.id.etSearchFriend);

        btn_check_contact.setOnClickListener(checkContactListener);

        dialogI = InformationDialog.newInstance(9);
        dialogI.setTargetFragment(this,0);

        mMFM = new ArrayList<>();
        mAdapter = new MyFriendAdapter(getActivity(),R.layout.list_myfriends_item,mMFM);
        setListAdapter(mAdapter);
        getListView().setTextFilterEnabled(true);

        etSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPopMenu.setOnItemSelectedListener(this);
        mPopMenu.add(ASK_FOR_MONEY, R.string.menu_item_title_ask_for_money).setIcon(
                getDraw(R.drawable.ic_ask_icon_color));
        mPopMenu.add(PAY, R.string.menu_item_title_pay_friends).setIcon(
                getDraw(R.drawable.ic_payfriends_icon_color));
        mPopMenu.add(VIEW_DETAIL, R.string.view_detail).setIcon(
                getDraw(R.drawable.ic_view_detail));

        Timber.d("is Contact new:"+isContactNew);
        if(isContactNew.equals(DefineValue.NO)){
            layout_check_contact.setVisibility(View.GONE);
            layout_list_contact.setVisibility(View.VISIBLE);
            initializeDataFriend();
            mState = SHOW_MENU;
        }
    }

    private Drawable getDraw(int idDrawable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(idDrawable, getActivity().getTheme());
        } else {
            return getResources().getDrawable(idDrawable);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Timber.d("options menu add contact");
        inflater.inflate(R.menu.list_contacts, menu);
//        inflater.inflate(R.menu.information, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_item_add_by_qrcode:
                if (mState == HIDE_MENU)
                {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.check_contact_first), Toast.LENGTH_LONG).show();
                }
                else if (mState == SHOW_MENU){
                    Intent i = new Intent(getActivity(), AddByQRCodeActivity.class);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
                return true;
            case R.id.menu_item_add_by_nfc:
                if (mState == HIDE_MENU)
                {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.check_contact_first), Toast.LENGTH_LONG).show();
                }
//                else if (mState == SHOW_MENU){
//                }
                return true;
            case R.id.menu_item_contact_list:
                if (mState == HIDE_MENU)
                {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.check_contact_first), Toast.LENGTH_LONG).show();
                }
                else if (mState == SHOW_MENU){
//                    Fragment newFragment = new ListContacts();
//                    switchFragment(newFragment, "Contact List", true);
                    Intent i = new Intent(getActivity(), ListContactActivity.class);
                    switchActivity(i, MainPage.ACTIVITY_RESULT);
                }
                return true;
//            case R.id.action_information:
//                dialogI.show(getActivity().getSupportFragmentManager(), InformationDialog.TAG);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Button.OnClickListener checkContactListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            }
            else {
                runLoader();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                runLoader();
            } else {
                Toast.makeText(getActivity(), getString(R.string.cancel_permission_read_contacts), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void runLoader(){
        crossfadingView(layout_check_contact, layout_loading_contact);
        loadingCircle.setMax(100);
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);
    }

    private void crossfadingView(final View vFrom, final View vTo){
        final Animation out = AnimationUtils.makeOutAnimation(getActivity(), true);
        final Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vFrom.startAnimation(out);
                vFrom.setVisibility(View.GONE);
                vTo.startAnimation(in);
                vTo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initializeDataFriend(){
        Timber.d("initializeDataFriend");
        mMFM.addAll(myFriendModel.getAll());
        mAdapter.notifyDataSetChanged();
        runLoader();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
//        String[] projectionFields =  new String[] { ContactsContract.Contacts._ID,
//                ContactsContract.Contacts.DISPLAY_NAME };
        // Construct the loader

        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";


        if(loaderID == CONTACTS_LOADER) {
            return new CursorLoader(getActivity(),
                    //ContactsContract.Contacts.CONTENT_URI, // URI
                    Contacts.CONTENT_URI,
                    null,  // projection fields
                    select, // the selection criteria
                    null, // the selection args
                    Contacts.DISPLAY_NAME // the sort order
            );
        }
        // Return the loader for use
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {

        setmCursor(cursor);
        if(loader.getId() == CONTACTS_LOADER){
            if (cursor.getCount() > 0) {
                Timber.wtf("isi size cursor:"+String.valueOf(cursor.getCount()));
                //mAdapter.swapCursor(cursor);
                Thread getContactBackground = new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        ActiveAndroid.initialize(getActivity());
                        List<friendModel> mListFriendModel = new ArrayList<>();
                        String _name;
                        String _id;

                        int finalI = 0;
                        while (!cursor.isClosed() && cursor.moveToNext()) {
                            _id = cursor.getString(cursor.getColumnIndex(Contacts._ID));
                            _name = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
                            if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER))) > 0) {
                                Cursor pCur = mCR.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{_id}, null);

                                String _phone1 = null, _phone2 = null, _phone3 = null, _phoneTemp;
                                int idx = 0;

                                if(pCur != null) {
                                    while (pCur.moveToNext()) {
                                        _phoneTemp = NoHPFormat.formatTo62(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                        switch (idx) {
                                            case 0:
                                                _phone1 = _phoneTemp;
                                                break;
                                            case 1:
                                                _phone2 = _phoneTemp;
                                                break;
                                            case 2:
                                                _phone3 = _phoneTemp;
                                                break;
                                        }
                                        idx++;
                                    }
                                }
                                if (pCur != null) {
                                    pCur.close();
                                }

                                pCur = mCR.query(
                                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Email.CONTACT_ID +" = ?",
                                        new String[]{_id}, null);

                                String _email=null;

                                if (pCur != null) {
                                    while (pCur.moveToNext()) {
                                        _email = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                    }
                                }
                                if (pCur != null) {
                                    pCur.close();
                                }

                                Timber.wtf("isi contact yg disimpen:"+_name+" / "+_phone1+" / "+_phone2+" / "+_phone3+" / "+_email+" / "+_ownerID);

                                mListFriendModel.add(new friendModel(_name, _phone1, _phone2, _phone3, _email, _ownerID));
                                if(!cursor.isClosed())
                                    loadingCircle.setProgress((int) ((finalI+1)* (50.0/(double)cursor.getCount())));
                                finalI++;
                            }

                        }
                        insertContact(mListFriendModel);
                        Looper.loop();
                        //getLoaderManager().destroyLoader(CONTACTS_LOADER);
                    }
                };
                getContactBackground.start();
            }else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingCircle.setProgress(0);
                        if(layout_loading_contact.getVisibility() == View.VISIBLE)
                            crossfadingView(layout_loading_contact,layout_check_contact);
                    }
                });
                Toast.makeText(getActivity(),getString(R.string.error_message),Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void insertContact(List<friendModel> mfriendModel){
        try{

            RequestParams params;
            if(isContactNew.equals(DefineValue.NO)){
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_UPDATE,
                        _ownerID,accessKey);
            }
            else
                params = MyApiClient.getSignatureWithParams(MyApiClient.COMM_ID,MyApiClient.LINK_USER_CONTACT_INSERT,
                        _ownerID,accessKey);

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.registerTypeAdapter(friendModel.class, new friendAdapter()).create();
            params.put(WebParams.USER_ID, _ownerID);
            params.put(WebParams.DATE_TIME, DateTimeFormat.getCurrentDateTime());
            params.put(WebParams.CONTACTS, gson.toJson(mfriendModel));
            params.put(WebParams.COMM_ID, MyApiClient.COMM_ID);


            JsonHttpResponseHandler mHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String code = response.getString(WebParams.ERROR_CODE);

                        if(isContactNew.equals(DefineValue.NO))
                            Timber.d("Isi response UpdateContact:" + response.toString());
                        else Timber.d("Isi response InsertContact:"+response.toString());

                        if (code.equals(WebParams.SUCCESS_CODE)) {
                            String arrayFriend = response.getString(WebParams.DATA_CONTACT);
                            String arrayMyFriend = response.getString(WebParams.DATA_FRIEND);
                            insertFriendToDB(new JSONArray(arrayFriend), new JSONArray(arrayMyFriend));
                        }
                        else if(code.equals(WebParams.LOGOUT_CODE)){
                            Timber.d("isi response autologout:"+response.toString());
                            String message = response.getString(WebParams.ERROR_MESSAGE);
                            AlertDialogLogout test = AlertDialogLogout.getInstance();
                            test.showDialoginMain(getActivity(),message);
                        }
                        else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            if(isContactNew.equals(DefineValue.YES)){
                                layout_check_contact.setVisibility(View.VISIBLE);
                                layout_list_contact.setVisibility(View.GONE);
                            }
                            Toast.makeText(getActivity(),code,Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    Timber.d("onProgress insert contact:"+bytesWritten + " / " + totalSize);
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

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingCircle.setProgress(0);
                            if(layout_loading_contact.getVisibility() == View.VISIBLE)
                                crossfadingView(layout_loading_contact,layout_check_contact);
                        }
                    });

                    Timber.w("Error Koneksi insert contact myfriend:"+throwable.toString());
                }
            };

            if(isContactNew.equals(DefineValue.NO)){
                Timber.d("isi params update Contact:"+params.toString());
                MyApiClient.sentUpdateContact(getActivity(),params, mHandler);
            }
            else {
                Timber.d("isi params insert Contact:"+params.toString());
                MyApiClient.sentInsertContact(getActivity(),params, mHandler);
            }
        }catch (Exception e){
            Timber.d("httpclient:"+ e.getMessage());
        }
    }

    private void insertFriendToDB(JSONArray arrayFriend, JSONArray arrayMyfriend){
        try {
            ActiveAndroid.beginTransaction();
            friendModel mFm;
            myFriendModel mMfm;
            String bucket;

            friendModel.deleteAll();
            myFriendModel.deleteAll();

            Timber.d("arrayfriend lenght:"+String.valueOf(arrayFriend.length()));
            if(arrayFriend.length()>0){
                for (int i = 0; i < arrayFriend.length(); i++) {
                    mFm = new friendModel();
                    mFm.setContact_id(arrayFriend.getJSONObject(i).getInt(friendModel.CONTACT_ID));
                    mFm.setFull_name(arrayFriend.getJSONObject(i).getString(friendModel.FULL_NAME));
                    mFm.setMobile_number(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER));
                    mFm.setMobile_number2(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER2));
                    mFm.setMobile_number3(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER3));
                    mFm.setEmail(arrayFriend.getJSONObject(i).getString(friendModel.EMAIL));
                    mFm.setOwner_id(arrayFriend.getJSONObject(i).getString(friendModel.OWNER_ID));

                    bucket = arrayFriend.getJSONObject(i).getString(friendModel.IS_FRIEND);
                    if(!bucket.equals(""))mFm.setIs_friend(Integer.parseInt(bucket));

                    mFm.setCreated_date(DateTimeFormat.convertStringtoCustomDate(arrayFriend.getJSONObject(i).getString(friendModel.CREATED_DATE)));
                    if(isContactNew.equals(DefineValue.NO) && !arrayFriend.getJSONObject(i).getString(friendModel.UPDATED_DATE).isEmpty()){
                        mFm.setUpdate_date(DateTimeFormat.convertStringtoCustomDate(arrayFriend.getJSONObject(i).getString(friendModel.UPDATED_DATE)));
                    }
                    mFm.save();
                    Timber.d("idx array friend:"+String.valueOf(i));
                    if(layout_loading_contact.getVisibility() == View.VISIBLE)
                        loadingCircle.setProgress((int) ((i+1)* (25.0/(double)arrayFriend.length()))+50);
                }
            }
            else {
                if(layout_loading_contact.getVisibility() == View.VISIBLE)
                    loadingCircle.setProgress(75);
            }

            Timber.d("arrayMyfriend lenght:"+String.valueOf(arrayMyfriend.length()));
            if(arrayMyfriend.length()>0){
                for (int i = 0; i < arrayMyfriend.length(); i++) {
                    mMfm = new myFriendModel();
                    mMfm.setContact_id(arrayMyfriend.getJSONObject(i).getInt(myFriendModel.CONTACT_ID));
                    mMfm.setFull_name(arrayMyfriend.getJSONObject(i).getString(myFriendModel.FULL_NAME));
                    mMfm.setFriend_number(arrayMyfriend.getJSONObject(i).getString(myFriendModel.FRIEND_NUMBER));
                    mMfm.setEmail(arrayMyfriend.getJSONObject(i).getString(myFriendModel.EMAIL));
                    mMfm.setUser_id(arrayMyfriend.getJSONObject(i).getString(myFriendModel.USER_ID));
                    mMfm.setImg_url(arrayMyfriend.getJSONObject(i).optString(myFriendModel.IMG_URL,""));
                    mMfm.save();
                    Timber.d("idx array my friend:"+String.valueOf((int) ((i + 1) * (25.0 / (double) arrayMyfriend.length())) + 75));
                    if(layout_loading_contact.getVisibility() == View.VISIBLE)
                        loadingCircle.setProgress((int) ((i+1)* (25.0/(double)arrayMyfriend.length()))+75);
                }
            }
            else {
                if(layout_loading_contact.getVisibility() == View.VISIBLE)
                    loadingCircle.setProgress(100);
            }

            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
            sp.edit().putString(DefineValue.CONTACT_FIRST_TIME, DefineValue.NO).apply();

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            if (isContactNew.equals(DefineValue.NO)) {
                                mMFM.clear();
                                mMFM.addAll(myFriendModel.getAll());
                                mAdapter.notifyDataSetChanged();
                                crossfadingView(layout_loading_contact, layout_list_contact);
                            } else {
                                mMFM.clear();
                                mMFM.addAll(myFriendModel.getAll());
                                mAdapter.notifyDataSetChanged();
                                crossfadingView(layout_loading_contact, layout_list_contact);
                            }
                            Timber.d("finish initialize my friend");
                            mState = SHOW_MENU;
                        }
                    }
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            if(ActiveAndroid.inTransaction())
                ActiveAndroid.endTransaction();
        }
    }

    /*Untuk update friend & my friend dbd
    public void UpdateFriendToDB(JSONArray arrayFriend, JSONArray arrayMyfriend){
        try {
            ActiveAndroid.beginTransaction();
            friendModel mFm;
            myFriendModel mMfm;
            String bucket;

            Log.d("arrayfriend lenght", String.valueOf(arrayFriend.length()));
            if(arrayFriend.length()>0){
                for (int i = 0; i < arrayFriend.length(); i++) {
                    mFm = new friendModel();
                    mFm.setContact_id(arrayFriend.getJSONObject(i).getInt(friendModel.CONTACT_ID));
                    mFm.setFull_name(arrayFriend.getJSONObject(i).getString(friendModel.FULL_NAME));
                    mFm.setMobile_number(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER));
                    mFm.setMobile_number2(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER2));
                    mFm.setMobile_number3(arrayFriend.getJSONObject(i).getString(friendModel.MOBILE_NUMBER3));
                    mFm.setEmail(arrayFriend.getJSONObject(i).getString(friendModel.EMAIL));
                    mFm.setOwner_id(arrayFriend.getJSONObject(i).getString(friendModel.OWNER_ID));

                    bucket = arrayFriend.getJSONObject(i).getString(friendModel.IS_FRIEND);
                    if(!bucket.equals(""))mFm.setIs_friend(Integer.parseInt(bucket));

                    mFm.setCreated_date(DateTimeFormat.convertStringtoCustomDate(arrayFriend.getJSONObject(i).getString(friendModel.CREATED_DATE)));
                    if(isContactNew.equals(CoreApp.NO) && !arrayFriend.getJSONObject(i).getString(friendModel.UPDATED_DATE).isEmpty()){
                        mFm.setUpdate_date(DateTimeFormat.convertStringtoCustomDate(arrayFriend.getJSONObject(i).getString(friendModel.UPDATED_DATE)));
                    }
                    mFm.save();
                    Log.d("idx array friend", String.valueOf(i));
                    if(layout_loading_contact.getVisibility() == View.VISIBLE)
                        loadingCircle.setProgress((int) ((i+1)* (25.0/(double)arrayFriend.length()))+50);
                }
            }
            else {
                if(layout_loading_contact.getVisibility() == View.VISIBLE)
                    loadingCircle.setProgress(75);
            }

            Log.d("arrayMyfriend lenght", String.valueOf(arrayMyfriend.length()));
            if(arrayMyfriend.length()>0){
                for (int i = 0; i < arrayMyfriend.length(); i++) {
                    mMfm = new myFriendModel();
                    mMfm.setContact_id(arrayMyfriend.getJSONObject(i).getInt(myFriendModel.CONTACT_ID));
                    mMfm.setFull_name(arrayMyfriend.getJSONObject(i).getString(myFriendModel.FULL_NAME));
                    mMfm.setFriend_number(arrayMyfriend.getJSONObject(i).getString(myFriendModel.FRIEND_NUMBER));
                    mMfm.setEmail(arrayMyfriend.getJSONObject(i).getString(myFriendModel.EMAIL));
                    mMfm.setUser_id(arrayMyfriend.getJSONObject(i).getString(myFriendModel.USER_ID));
                    mMfm.setImg_url(arrayMyfriend.getJSONObject(i).optString(myFriendModel.IMG_URL,""));
                    mMfm.save();
                    Log.d("idx array my friend", String.valueOf((int) ((i + 1) * (25.0 / (double) arrayMyfriend.length())) + 75));
                    if(layout_loading_contact.getVisibility() == View.VISIBLE)
                        loadingCircle.setProgress((int) ((i+1)* (25.0/(double)arrayMyfriend.length()))+75);
                }
            }
            else {
                if(layout_loading_contact.getVisibility() == View.VISIBLE)
                    loadingCircle.setProgress(100);
            }

            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
            sp.edit().putString(CoreApp.CONTACT_FIRST_TIME,CoreApp.NO).apply();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isAdded()){
                        if(isContactNew.equals(CoreApp.NO)){
                            mMFM.clear();
                            mMFM.addAll(myFriendModel.getAll());
                            mAdapter.notifyDataSetChanged();
                            crossfadingView(layout_loading_contact, layout_list_contact);
                        }
                        else {
                            mMFM.clear();
                            mMFM.addAll(myFriendModel.getAll());
                            mAdapter.notifyDataSetChanged();
                            crossfadingView(layout_loading_contact, layout_list_contact);
                        }
                        Log.d("finish initialize", "oke");
                        mState = SHOW_MENU;
                    }
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            if(ActiveAndroid.inTransaction())
                ActiveAndroid.endTransaction();
        }
    }*/

    private Cursor getmCursor() {
        return mCursor;
    }

    private void setmCursor(Cursor mCursor) {
        this.mCursor = mCursor;
    }

    private class friendAdapter implements JsonSerializer<friendModel> {

        @Override
        public JsonElement serialize(friendModel _friendModel, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(friendModel.FULL_NAME, _friendModel.getFull_name());
            jsonObject.addProperty(friendModel.MOBILE_NUMBER, _friendModel.getMobile_number());
            jsonObject.addProperty(friendModel.MOBILE_NUMBER2, _friendModel.getMobile_number2());
            jsonObject.addProperty(friendModel.MOBILE_NUMBER3, _friendModel.getMobile_number3());
            jsonObject.addProperty(friendModel.EMAIL, _friendModel.getEmail());
            jsonObject.addProperty(friendModel.OWNER_ID, _friendModel.getOwner_id());
            return jsonObject;
        }
    }

    @Override
    public void onPause() {
        //if(ActiveAndroid.inTransaction()){
        //    ActiveAndroid.endTransaction();
        //}

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getmCursor() != null) {
            getmCursor().close();
        }
        if(ActiveAndroid.inTransaction()){
            ActiveAndroid.endTransaction();
        }
        super.onDestroy();
    }

    private void switchActivity(Intent mIntent, int j){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent, j);
    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i, name, isBackstack);
    }
}