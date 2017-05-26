package com.sgo.hpku.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sgo.hpku.R;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentHistoryFragment extends Fragment implements AdapterView.OnItemClickListener
{
    View rootView;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
        {
            rootView = inflater.inflate(R.layout.agent_history_fragment, container, false);

            listView = (ListView) rootView.findViewById(R.id.listView);

            String[] listArray = {"0","1"};

            AgentHistoryArrayAdapter agentHistoryBbsArrayAdapter = new AgentHistoryArrayAdapter(getActivity(), R.layout.agent_history_listview, listArray);
            listView.setAdapter(agentHistoryBbsArrayAdapter);

            //set listener for list view
            listView.setOnItemClickListener(this);

        }

        return rootView;
    }

    private void displayAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are Sure Want To Cancel ?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //implements AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        displayAlertDialog();

    }
}
