package com.sgo.hpku.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsMapNagivationActivity;
import com.sgo.hpku.activities.BbsSearchAgentActivity;
import com.sgo.hpku.activities.BookBbsActivity;
import com.sgo.hpku.activities.MainAgentActivity;
import com.sgo.hpku.activities.MainPage;
import com.sgo.hpku.activities.SearchAgentActivity;
import com.sgo.hpku.coreclass.AgentConstant;
import com.sgo.hpku.fragments.AgentMapFragment;
import com.sgo.hpku.fragments.ProfileAgentFragment;
import com.sgo.hpku.entityRealm.AgentDetail;
import com.sgo.hpku.models.ShopDetail;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;

//import static com.sgo.indonesiakoe.activities.SearchAgentActivity.service_name_arr;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class AgentDetailFragmentDialog extends DialogFragment implements View.OnClickListener
{
    private BbsSearchAgentActivity mainBbsActivity;
    private ImageView closeBtn;
    private ImageView agentMapBtn;
    private ImageView callBtn;
    private ImageView smsBtn;
    private ImageView emailBtn, navigationBtn;
    private Button bookBtn;
    private JSONObject agentInfoSingle = null;
    private int agentPosition;
    private int businessId;
    private ShopDetail shopDetail = new ShopDetail();
    private Double currentLatitude;
    private Double currentLongitude;

    View rootView;
    Realm realm;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //Dialog dialog = super.onCreateDialog(savedInstanceState);
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setContentView(R.layout.agent_detail_bbs_fragment_dialog);
        //dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);


        return dialog;
    }

    public void setAgentInfoSingle(ShopDetail shopDetail, int position)
    {
        this.shopDetail = shopDetail;
        //String name = agentInfoSingle.getJSONObject("businessId");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.agent_detail_fragment_dialog, container, false);

        // if(rootView == null)
        // {
        //set session to angent info single
        //setAgentInfoSingleSharedPreferences();

        //set agent profile
        displayAgentProfile();

        //get object activity
        mainBbsActivity = (BbsSearchAgentActivity) getActivity();

        closeBtn    = (ImageView) rootView.findViewById(R.id.closeBtn);
        agentMapBtn = (ImageView) rootView.findViewById(R.id.agentMapBtn);
        callBtn     = (ImageView) rootView.findViewById(R.id.callBtn);
        smsBtn      = (ImageView) rootView.findViewById(R.id.smsBtn);
        emailBtn    = (ImageView) rootView.findViewById(R.id.emailBtn);
        bookBtn     = (Button) rootView.findViewById(R.id.bookBtn);
        navigationBtn   = (ImageView) rootView.findViewById(R.id.navigationBtn);

        closeBtn.setOnClickListener(this);
        agentMapBtn.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        smsBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
        bookBtn.setOnClickListener(this);
        navigationBtn.setOnClickListener(this);
        //}

        return rootView;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    private void displayAgentProfile()
    {
        //create fragment
        ProfileAgentFragment profileBbsFragment = new ProfileAgentFragment();
        profileBbsFragment.setShopDetail(this.shopDetail);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.detailProfile, profileBbsFragment).commit();
    }

    private void setAgentInfoSingleSharedPreferences()
    {
        //save data to session
        SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AgentConstant.AGENT_INFO_SINGLE_SHARED_PREFERENCES,agentInfoSingle.toString());
        editor.apply();
    }

    //implements View.OnClickListener
    @Override
    public void onClick(View view)
    {
        if(view.getId() == callBtn.getId())
        {
            setPhoneCall();
        }
        else if(view.getId() == smsBtn.getId())
        {
            showSms();
        }
        else if(view.getId() == emailBtn.getId())
        {
            showEmail();
        }
        else if(view.getId() == bookBtn.getId())
        {
            //Intent intent = new Intent(getActivity(), BookBbsActivity.class);
            //SearchAgentActivity.business_name_arr.get(agentPosition).
            //intent.putExtra("businessId", businessId);
            //startActivity(intent);
        }
        else if(view.getId() == agentMapBtn.getId())
        {
            showSingleAgentMap();
        }
        else if(view.getId() == closeBtn.getId())
        {
            closeAgentDetailFragmentDialog();
        }
        else if (view.getId() == navigationBtn.getId() )
        {
            //call new activity
            this.shopDetail.getShopLongitude();
            this.shopDetail.getShopLatitude();

            Intent intent=new Intent(getActivity(),BbsMapNagivationActivity.class);
            intent.putExtra("targetLatitude", this.shopDetail.getShopLatitude());
            intent.putExtra("targetLongitude", this.shopDetail.getShopLongitude());
            intent.putExtra("currentLatitude", this.currentLatitude);
            intent.putExtra("currentLongitude", this.currentLongitude);
            startActivity(intent);
        }
    }


    private void showSingleAgentMap()
    {
        int backStackCount = getFragmentManager().getBackStackEntryCount();

        if(backStackCount == 0)
        {
            //mengarahkan viewpager ke tab list agent
            mainBbsActivity.viewPager.setCurrentItem(0);

            closeAgentDetailFragmentDialog();
            createAgentMapFragment();
            updateActionBarTittle();
        }
        else
        {
            //mengarahkan viewpager ke tab list agent
            mainBbsActivity.viewPager.setCurrentItem(0);

            closeAgentDetailFragmentDialog();
        }
    }

    private void closeAgentDetailFragmentDialog()
    {
        //close fragment dialog of detail agent
        Fragment fragment = getFragmentManager().findFragmentByTag(AgentConstant.AGENT_DETAIL_FRAGMENT_DIALOG_TAG);

        if(fragment != null)
        {
            //DialogFragment dialogFragment = (DialogFragment)fragment;
            AgentDetailFragmentDialog dialogFragment = (AgentDetailFragmentDialog)fragment;
            dialogFragment.dismiss();
        }
    }

    private void createAgentMapFragment()
    {
        //create fragment
        AgentMapFragment agentMapBbsFragment = new AgentMapFragment();
        agentMapBbsFragment.setSingleAgent(agentPosition);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.listContent, agentMapBbsFragment).commit();
    }

    private void updateActionBarTittle()
    {
        //update title in action bar
        try
        {
            //((ActionBarActivity)getActivity())
            //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(agentInfoSingle.getString("name"));
            //get object activity
            MainAgentActivity mainBbsActivity = (MainAgentActivity) getActivity();
            mainBbsActivity.initializeToolbar(agentInfoSingle.getString("name"));
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    private void showSms()
    {
        try
        {
            /*Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", "081807128119");
            smsIntent.putExtra("sms_body", "Body of Message");
            startActivity(smsIntent);*/

            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.setData(Uri.parse("sms:" + "081807128119"));
            startActivity(smsIntent);
        }
        catch(ActivityNotFoundException e)
        {
            Toast.makeText(getActivity(), "Sorry sms application can not be openned for now", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmail()
    {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Text");

        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private void showPhoneCall()
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:081807128119"));
        startActivity(callIntent);
    }

    private void setPhoneCall()
    {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, AgentConstant.TRUE);
        }
        else
        {
            showPhoneCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode)
        {
            case AgentConstant.TRUE:
            {
                //If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted. Do the contacts-related task you need to do.
                    showPhoneCall();
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }
}
