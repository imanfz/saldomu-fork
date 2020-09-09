package com.sgo.saldomu.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
public class ProfileAgentFragment extends Fragment
{
    View rootView;
    private JSONObject agentInfoSingle = null;
    private ShopDetail shopDetail = new ShopDetail();
    String a;

    public void setShopDetail(ShopDetail shopDetail)
    {
        this.shopDetail = shopDetail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
        {
            rootView = inflater.inflate(R.layout.profile_agent_fragment, container, false);

            //get data agent from session
            getAgentLocationSharedPreferences();

            //display agent profile
            displayAgentInfo();
        }

        return rootView;
    }

    private void displayAgentInfo()
    {
        //pengambilan ID component dari layout
        TextView agentName        = (TextView)rootView.findViewById(R.id.agentName);
        TextView agentLastOnline  = (TextView)rootView.findViewById(R.id.agentLastOnline);
        TextView agentAddress     = (TextView)rootView.findViewById(R.id.agentAddress);
        ImageView agentProfilePic = (ImageView)rootView.findViewById(R.id.agentProfilePic);
        TextView agentDistance    = (TextView)rootView.findViewById(R.id.agentDistance);

        //set default value
        String agentNameSession        = "N/A";
        String agentLastOnlineSession  = getString(R.string.last_trx_agent_label) + ": N/A";
        String agentAddressSession     = "N/A";
        String agentProfilePicSession  = "N/A";
        String agentDistanceSession    = "N/A";

        agentNameSession                = this.shopDetail.getMemberName();
        agentAddressSession             = this.shopDetail.getShopAddress();
        agentDistanceSession            = this.shopDetail.getCalculatedDistance();

        if ( !this.shopDetail.getLastActivity().equals("") ) {
            try {

                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date shopLastActivity = dateFormat.parse(this.shopDetail.getLastActivity());
                agentLastOnlineSession      = DateTimeFormat.convertDatetoString(shopLastActivity, "dd MMM yyyy");
                agentLastOnlineSession      = getString(R.string.last_trx_agent_label) + agentLastOnlineSession;
            } catch (ParseException e ) {
                e.printStackTrace();
            }

        }
        /*try
        {
            //convert json array to json object
            JSONObject object = agentInfoSingle;

            //get value from session
            if(!object.isNull("name"))     agentNameSession = object.getString("name");
            if(!object.isNull("date"))     agentLastOnlineSession = object.getString("date");
            if(!object.isNull("address"))  agentAddressSession = object.getString("address");
            if(!object.isNull("image"))    agentProfilePicSession = object.getString("image");
            if(!object.isNull("distance")) agentDistanceSession = object.getString("distance");

            //modification data
            //agentProfilePicSession = "http://192.168.43.206/public/images/" + agentProfilePicSession;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }*/

        //set all data
        agentName.setText(agentNameSession);
        agentLastOnline.setText(agentLastOnlineSession);
        agentAddress.setText(agentAddressSession);
        agentDistance.setText(agentDistanceSession);

       /* ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).build();
        ImageLoader.getInstance().init(config);
        imageLoader.displayImage(agentProfilePicSession, agentProfilePic);*/

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

//        Picasso mPic;
//        if(MyApiClient.PROD_FLAG_ADDRESS)
//            mPic = MyPicasso.getUnsafeImageLoader(getActivity());
//        else
//            mPic= Picasso.with(getActivity());

        if ( this.shopDetail.getUrlSmallProfilePicture() != null && !this.shopDetail.getUrlSmallProfilePicture().isEmpty() ) {
            GlideManager.sharedInstance().initializeGlide(getActivity(), shopDetail.getUrlSmallProfilePicture(),  roundedImage, agentProfilePic );
        } else {
            GlideManager.sharedInstance().initializeGlide(getActivity(), R.drawable.user_unknown_menu, roundedImage, agentProfilePic);
//            mPic.load(R.drawable.user_unknown_menu)
//                    .error(roundedImage)
//                    .fit().centerInside()
//                    .placeholder(R.drawable.progress_animation)
//                    .transform(new RoundImageTransformation()).into(agentProfilePic);
        }
        //int profile = getActivity().getResources().getIdentifier(agentProfilePicSession, "drawable", getActivity().getPackageName());
        //agentProfilePic.setImageResource(profile);
    }

    private void getAgentLocationSharedPreferences()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String agentInfoSingleString  = preferences.getString(AgentConstant.AGENT_INFO_SINGLE_SHARED_PREFERENCES, "");

        if(!agentInfoSingleString.equals(""))
        {
            try
            {
                agentInfoSingle = new JSONObject(agentInfoSingleString);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
}
