package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.AdapterSearchContact;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ContactList;
import com.sgo.saldomu.utils.CustomStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class FragmentSearch extends Fragment {

    EditText et_search;
    RecyclerView recycler_view;
    ProgressBar prgLoading;
    //ArrayList<ContactList> contactLists = new ArrayList<>();
    ContactList selectedContact;
    private AdapterSearchContact adapter;

    boolean isSearchPhone = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_contact, container, false);
        et_search = v.findViewById(R.id.et_search);
        recycler_view = v.findViewById(R.id.recycler_view);
        prgLoading = v.findViewById(R.id.prgLoading);

        //disableCopyPaste();
        initAdapter();

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.equals("")) {

                } else {
                    adapter.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return v;
    }


//    private void disableCopyPaste(){
//        List<EditTextObj> list = new ArrayList<>();
//
//        list.add(new EditTextObj (et_search,true));
//
//        EditTextUtil.disableCopy(list);
//    }

    private List<ContactList> getContactList() {
        List<ContactList> contactLists = new ArrayList<>();

        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Timber.i("Name: " + name);
                        String finalPhoneNo = CustomStringUtil.filterPhoneNo(phoneNo);
                        Timber.i("Phone Number: " + finalPhoneNo);
                        contactLists.add(new ContactList(name, finalPhoneNo));
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

        return contactLists;
    }


    private void initAdapter() {
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(lm);

        Completable.complete()
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() ->
                        Observable.fromIterable(getContactList())
                        .distinct()
                        .sorted((t0, t1) -> t0.getName().compareToIgnoreCase(t1.getName()))
                        .toList()
                        .subscribe(contactLists -> {
                            prgLoading.setVisibility(View.GONE);

                            adapter = new AdapterSearchContact(contactLists, obj -> {
                                selectedContact = obj;
                                updateView();
                            });
                            recycler_view.setAdapter(adapter);
                        }, e -> {
                            prgLoading.setVisibility(View.GONE);
                            Toast.makeText(getContext(), R.string.no_data, Toast.LENGTH_SHORT).show();
                        }).dispose())
                .subscribe();
    }

    private void updateView() {
        Intent intent = new Intent();
        intent.putExtra(DefineValue.ITEM_SELECTED, selectedContact);
        Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            isSearchPhone = true;
            //contactLists = bundle.getParcelableArrayList(DefineValue.BUNDLE_LIST);
        }
    }
}
