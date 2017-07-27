package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.adapter.AgentListArrayAdapter;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.MainResultReceiver;
import com.sgo.saldomu.dialogs.AgentDetailFragmentDialog;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentListFragment extends Fragment implements AdapterView.OnItemClickListener,
        MainResultReceiver.Receiver
{
    View rootView;
    ListView listView;
    JSONArray agentLocation = null;
    private Activity activity;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private AgentListArrayAdapter agentListArrayAdapter;
    private String mobility;

    public AgentListFragment() {

    }

    public AgentListFragment(String mobility) {
        this.mobility = mobility;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
        {
            rootView = inflater.inflate(R.layout.agent_list_fragment, container, false);

            //set activity to global variabel.
            //agar tidak perlu selalu memanggil getActivity(), karena bisa null
            activity = getActivity();

            //get object activity
            BbsSearchAgentActivity mainBbsActivity = (BbsSearchAgentActivity) getActivity();

            //set realtime listener for receiver. Will call function : onReceiveResult
            //mainBbsActivity.agentListResultReceiver.setReceiver(this);

            listView = (ListView) rootView.findViewById(R.id.listView);

            //create adapter and set to list view
            setAdapterToListView();
        }

        return rootView;
    }

    //Implements MainBbsResultReceiver.Receiver
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        if(resultCode == 0)
        {
            //Toast.makeText(getActivity(), "--------- update agent list -----------", Toast.LENGTH_SHORT).show();

            //String errorMsg = resultData.getString("resultValue");
            setAdapterToListView();
        }
    }

    private void setAdapterToListView()
    {
        //get data agent from session
        getAgentLocationSharedPreferences();

        //if ( shopDetails.size() > 0 )
        //{
            agentListArrayAdapter = new AgentListArrayAdapter(activity, R.layout.agent_list_listview, this.shopDetails);
            //agentListArrayAdapter.setAgentList(agentLocation);
            listView.setAdapter(agentListArrayAdapter);

            //set listener for list view
            listView.setOnItemClickListener(this);
        //}

        //jika data agent is available, maka proses
        /*if(agentLocation != null && agentLocation.length() != 0)
        {
            String[] listArray = getListArray();

            agentListArrayAdapter = new AgentListArrayAdapter(activity, R.layout.agent_list_listview, shopDetails);
            //agentListArrayAdapter.setAgentList(agentLocation);
            listView.setAdapter(agentListArrayAdapter);

            //set listener for list view
            listView.setOnItemClickListener(this);
        }*/
    }

    //implements AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

        if ( mobility.equals(DefineValue.STRING_NO) ) {

            //show fragment dialog for agent detail
            FragmentManager fragmentManager = getFragmentManager();
            AgentDetailFragmentDialog agentDetailBbsFragmentDialog = new AgentDetailFragmentDialog();
            agentDetailBbsFragmentDialog.setAgentInfoSingle(this.shopDetails.get(position), position);
            agentDetailBbsFragmentDialog.show(fragmentManager, AgentConstant.AGENT_DETAIL_FRAGMENT_DIALOG_TAG);

        }
    }

    private void getAgentLocationSharedPreferences()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String dataJson = preferences.getString(AgentConstant.AGENT_INFO_SHARED_PREFERENCES, "");

        if(!dataJson.equals(""))
        {
            try
            {
                agentLocation = new JSONArray(dataJson);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String[] getListArray()
    {
        int length = agentLocation.length();
        String[] listArray = new String[length];
        for(int i=0; i < length; i++)
        {
            listArray[i] = "value";
        }

        return listArray;
    }

    public void updateView(ArrayList<ShopDetail> shopDetails) {

        this.shopDetails.clear();
        if ( shopDetails.size() > 0 ) {
            this.shopDetails.addAll(shopDetails);
        }
        //agentListArrayAdapter = new AgentListArrayAdapter(activity, R.layout.agent_list_listview, shopDetails);
        //agentListArrayAdapter.setAgentList(agentLocation);
        //listView.setAdapter(agentListArrayAdapter);

        //set listener for list view
        //listView.setOnItemClickListener(this);
        agentListArrayAdapter.notifyDataSetChanged();
        //setAdapterToListView();
    }



}
