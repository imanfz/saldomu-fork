package com.sgo.saldomu.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.GridMenuSCADM;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.sgo.saldomu.widgets.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

public class ActivitySCADM extends BaseActivity {

    private SecurePreferences sp;
    private InformationDialog dialogI;
    GridView GridSCADM;
    Intent intent;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_scadm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitializeToolbar();
        GridSCADM = (GridView)findViewById(R.id.grid_scadm);

        GridMenuSCADM adapter = new GridMenuSCADM(this, SetupListMenu(), SetupListMenuIcons());
        GridSCADM.setAdapter(adapter);

        GridSCADM.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Timber.d("masuk gridhomeonitemclicklistener");

                String menuItemName = ((TextView) view.findViewById(R.id.grid_text)).getText().toString();

                if (menuItemName.equalsIgnoreCase(getString(R.string.scadm_join)))
                {
                    intent = new Intent(ActivitySCADM.this, JoinCommunitySCADMActivity.class);
                    startActivity(intent);
                }else  if (menuItemName.equalsIgnoreCase(getString(R.string.scadm_topup)))
                {
                    intent = new Intent(ActivitySCADM.this, TopUpSCADMActivity.class);
                    startActivity(intent);
                }else
                {
                    intent = new Intent(ActivitySCADM.this, DenomSCADMActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private ArrayList<String> SetupListMenu(){
        String[] _data;
        ArrayList<String> data = new ArrayList<>() ;
        _data = getResources().getStringArray(R.array.list_menu_scadm);
        Collections.addAll(data,_data);
        return data;
    }

    private int[] SetupListMenuIcons(){

        int totalIdx            = 0;
        int overallIdx          = 0;
        TypedArray menu           = getResources().obtainTypedArray(R.array.list_menu_icon_frag_scadm);

        totalIdx                = menu.length();

        int[] data        = new int[totalIdx];

        for( int j = 0; j < menu.length(); j++) {
            data[overallIdx] = menu.getResourceId(j, -1);
            overallIdx++;
        }

        return data;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_information:
                if(!dialogI.isAdded())
                    dialogI.show(this.getSupportFragmentManager(), InformationDialog.TAG);
                return true;
            case android.R.id.home :
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void InitializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.menu_item_title_scadm));
    }
}
