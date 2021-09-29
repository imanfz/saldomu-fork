package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.AgentListArrayAdapter;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

//import com.sgo.saldomu.dialogs.AgentDetailFragmentDialog;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentListFragment extends Fragment implements AdapterView.OnItemClickListener,
        MainResultReceiver.Receiver {
    View rootView;
    ListView listView;
    JSONArray agentLocation = null;
    private Activity activity;
    private final ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private AgentListArrayAdapter agentListArrayAdapter;
    private OnListAgentItemClick mOnListAgentItemClick;

    public interface OnListAgentItemClick {
        void OnIconLocationClickListener(int position);
    }

    public AgentListFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnListAgentItemClick = (OnListAgentItemClick) context;
        } catch (ClassCastException e) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.agent_list_fragment, container, false);

            //set activity to global variabel.
            //agar tidak perlu selalu memanggil getActivity(), karena bisa null
            activity = getActivity();

            listView = (ListView) rootView.findViewById(R.id.listView);

            //create adapter and set to list view
            setAdapterToListView();
        }

        return rootView;
    }

    //Implements MainBbsResultReceiver.Receiver
    @Override
    public void onReceiveResult(int resultCode) {
        if (resultCode == 0) {
            setAdapterToListView();
        }
    }

    private void setAdapterToListView() {
        //get data agent from session
        getAgentLocationSharedPreferences();

        //if ( shopDetails.size() > 0 )
        //{
        agentListArrayAdapter = new AgentListArrayAdapter(activity, R.layout.agent_list_listview
                , this.shopDetails, mOnListAgentItemClick);
        //agentListArrayAdapter.setAgentList(agentLocation);
        listView.setAdapter(agentListArrayAdapter);

        //set listener for list view
        listView.setOnItemClickListener(this);
        //}

    }

    //implements AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//        if (mobility.equals(DefineValue.STRING_NO)) {
//
//            //show fragment dialog for agent detail
//            FragmentManager fragmentManager = getFragmentManager();
//            AgentDetailFragmentDialog agentDetailBbsFragmentDialog = new AgentDetailFragmentDialog();
//            agentDetailBbsFragmentDialog.setAgentInfoSingle(this.shopDetails.get(position), position);
//            agentDetailBbsFragmentDialog.setCancelable(false);
//            agentDetailBbsFragmentDialog.show(fragmentManager, AgentConstant.AGENT_DETAIL_FRAGMENT_DIALOG_TAG);
//
//        }

    }

    private void getAgentLocationSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String dataJson = preferences.getString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, "");

        if (!dataJson.equals("")) {
            try {
                agentLocation = new JSONArray(dataJson);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void updateView(ArrayList<ShopDetail> shopDetails) {

        this.shopDetails.clear();
        if (shopDetails.size() > 0) {
            this.shopDetails.addAll(shopDetails);
        }

        agentListArrayAdapter.notifyDataSetChanged();

    }


}
