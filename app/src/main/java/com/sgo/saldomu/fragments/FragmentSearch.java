package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.ActivitySearch;
import com.sgo.saldomu.adapter.AdapterSearchContact;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.models.ContactList;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentSearch extends Fragment {

    EditText et_search;
    RecyclerView recycler_view;
    ArrayList<ContactList> contactLists = new ArrayList<>();
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

    private void initAdapter() {
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        adapter = new AdapterSearchContact(ActivitySearch.TYPE_SEARCH_CONTACT, contactLists, obj -> {
            selectedContact = obj;
            updateView();
        });

        recycler_view.setLayoutManager(lm);
        recycler_view.setAdapter(adapter);
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
            contactLists = bundle.getParcelableArrayList(DefineValue.BUNDLE_LIST);
        }
    }
}
