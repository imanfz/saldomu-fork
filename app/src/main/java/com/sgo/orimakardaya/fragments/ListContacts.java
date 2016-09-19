package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.securepreferences.SecurePreferences;
import com.sgo.orimakardaya.Beans.friendModel;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.adapter.FriendAdapter;
import com.sgo.orimakardaya.coreclass.CustomSecurePref;
import com.sgo.orimakardaya.coreclass.DefineValue;
import com.u1aryz.android.lib.newpopupmenu.MenuItem;
import com.u1aryz.android.lib.newpopupmenu.PopupMenu;

import java.util.ArrayList;

/**
 * Created by thinkpad on 3/25/2015.
 */
public class ListContacts extends ListFragment implements PopupMenu.OnItemSelectedListener {

    View v;
    View layout_list_contact;
    private FriendAdapter mAdapter;
    private ArrayList<friendModel> mMFM;

    SecurePreferences sp;
    private String _ownerID,isContactNew;

    EditText etSearchContact;

    private final static int CONTACT = 0;
    private final static int SMS = 1;
    private final static int EMAIL = 2;

    int positionSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_list_my_contacts, container, false);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        positionSelected = position;

        PopupMenu menu = new PopupMenu(getActivity().getApplicationContext());
        menu.setWidth(300);
        menu.setOnItemSelectedListener(this);
        menu.add(CONTACT, R.string.menu_item_add_by_contact).setIcon(
                getResources().getDrawable(R.drawable.ic_add_by_contact));
        menu.add(SMS, R.string.menu_item_add_by_sms).setIcon(
                getResources().getDrawable(R.drawable.ic_add_by_sms));
        menu.add(EMAIL, R.string.menu_item_add_by_email).setIcon(
                getResources().getDrawable(R.drawable.ic_add_by_email));
        menu.show(v);

    }

    @Override
    public void onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTACT:
                Toast.makeText(getActivity().getApplicationContext(),"Contact " + mMFM.get(positionSelected).getFull_name().toString(), Toast.LENGTH_LONG).show();
                break;

            case SMS:
                Toast.makeText(getActivity().getApplicationContext(),"SMS", Toast.LENGTH_LONG).show();
                break;

            case EMAIL:
                Toast.makeText(getActivity().getApplicationContext(),"Email", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void switchActivity(Intent mIntent){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchActivity(mIntent,MainPage.ACTIVITY_RESULT);
    }
    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        MainPage fca = (MainPage) getActivity();
        fca.switchContent(i,name,isBackstack);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        _ownerID = sp.getString(DefineValue.USERID_PHONE,"");
        isContactNew = sp.getString(DefineValue.CONTACT_FIRST_TIME,"");

        mMFM = new ArrayList<friendModel>();
        mMFM.addAll(friendModel.getAll());

        layout_list_contact = v.findViewById(R.id.layout_list_contact);
        layout_list_contact.setVisibility(View.VISIBLE);

        etSearchContact = (EditText) v.findViewById(R.id.etSearchContact);

        mAdapter = new FriendAdapter(getActivity(),R.layout.list_myfriends_item,mMFM);
        setListAdapter(mAdapter);
        getListView().setTextFilterEnabled(true);

        etSearchContact.addTextChangedListener(new TextWatcher() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void crossfadingView(final View vFrom, final View vTo){
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

    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new android.util.DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
