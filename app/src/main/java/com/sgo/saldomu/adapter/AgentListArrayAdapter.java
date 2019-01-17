package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.fragments.AgentListFragment;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentListArrayAdapter extends BaseAdapter
{
    int layoutResourceId;
    Context context;
    String[] menuList;
    private JSONArray agentLocation = null;
    private ImageView agentMapBtn;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private BbsSearchAgentActivity bbsSearchAgentActivity;
    int[] shopDetailsClicks;
    View rootView;
    private AgentListFragment.OnListAgentItemClick mOnListAgentItemClick;

    public AgentListArrayAdapter(Context context, int layoutResourceId,
                                 ArrayList<ShopDetail> shopDetails,
                                 AgentListFragment.OnListAgentItemClick mOnListAgentItemClick){
        //super(context, layoutResourceId, shopDetails);
        this.layoutResourceId = layoutResourceId;
        this.context  = context;
        this.menuList = menuList;

        this.shopDetails = shopDetails;

        //get data agent from session
        getAgentLocationSharedPreferences();
        bbsSearchAgentActivity = (BbsSearchAgentActivity) context;
        this.mOnListAgentItemClick = mOnListAgentItemClick;
    }

    /*public void setAgentList(JSONArray agent)
    {
        agentLocation = agent;
    }*/

    static class ViewHolder
    {
        LinearLayout agentServiceList;
        TextView agentName;
        //TextView agentLastOnline;
        //TextView agentAddress;
        ImageView agentProfilePic;
        //ImageView agentRate;
        TextView agentDistance;
        //TextView agentAvailable;
        ImageView agentMapBtn;
        TextView tvCountTrx;
        RatingBar ratingBar;
    }

    @Override
    public int getCount() {
        return shopDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return shopDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        rootView = convertView;
        final ViewHolder viewHolder;

        if(rootView == null)
        {
            viewHolder = new ViewHolder();
        }
        else
        {
            viewHolder = (ViewHolder) rootView.getTag();
        }

        //proses pengambilan layout
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        rootView = inflater.inflate(layoutResourceId, parent, false);

        //ViewHolder viewHolder = new ViewHolder();

        //pengambilan ID component dari layout
        viewHolder.agentServiceList       = rootView.findViewById(R.id.agent_service_list);
        viewHolder.agentName       = rootView.findViewById(R.id.agentName);
        //viewHolder.agentLastOnline = (TextView)rootView.findViewById(R.id.agentLastOnline);
        //viewHolder.agentAddress    = (TextView)rootView.findViewById(R.id.agentAddress);
        viewHolder.agentProfilePic = rootView.findViewById(R.id.agentProfilePic);
        //viewHolder.agentRate = (ImageView)rootView.findViewById(R.id.agentRate);
        viewHolder.agentDistance   = rootView.findViewById(R.id.agentDistance);
        //viewHolder.agentAvailable   = (TextView)rootView.findViewById(R.id.available);
        viewHolder.tvCountTrx       = rootView.findViewById(R.id.tvCountTrx);
        viewHolder.ratingBar        = rootView.findViewById(R.id.ratingBar);

        viewHolder.agentMapBtn = rootView.findViewById(R.id.agentMapBtn);
        //viewHolder.agentMapBtn.setOnClickListener(this);
        //viewHolder.agentMapBtn.setOnClickListener(imgClickListener);
        viewHolder.agentMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mOnListAgentItemClick.OnIconLocationClickListener(position, shopDetails);

            }
        });

        //set default value
        String agentName        = "N/A";
        String agentLastOnline  = "N/A";
        String agentAddress     = "N/A";
        String agentProfilePic  = "N/A";
        String agentRate  = "N/A";
        String agentDistance    = "N/A";
        String agentAvailable    = "N/A";
        String agentServiceList    = "N/A";
        String countTrx         = "";
        String score            = "0";
        int numStars            = 0;

        if ( shopDetails.size() > 0 ) {
            ShopDetail shopDetail = (ShopDetail) getItem(position);
            agentName       = shopDetail.getShopName();
            agentAddress    = shopDetail.getShopAddress();
            agentDistance   = shopDetail.getCalculatedDistance();
            countTrx        = shopDetail.getShopCount();
            score           = shopDetail.getShopScore();
            numStars        = shopDetail.getNumStars();

            if ( !shopDetail.getLastActivity().equals("") ) {
                try {

                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date shopLastActivity = dateFormat.parse(shopDetail.getLastActivity());
                    agentLastOnline      = DateTimeFormat.convertDatetoString(shopLastActivity, "dd MMM yyyy");

                } catch (ParseException e ) {
                    e.printStackTrace();
                }

            }
        }

        //attach all data to view holder
        viewHolder.agentName.setText(agentName);
        //viewHolder.agentLastOnline.setText(agentLastOnline);
        //viewHolder.agentAddress.setText(agentAddress);
        viewHolder.agentDistance.setText(agentDistance);
        viewHolder.tvCountTrx.setText(": " +countTrx);
        viewHolder.ratingBar.setRating(Float.valueOf(score));
        viewHolder.ratingBar.setNumStars(numStars);
        viewHolder.ratingBar.setIsIndicator(true);
        viewHolder.ratingBar.setFocusable(false);

        if(agentAvailable.equalsIgnoreCase("Y")) {


        }
        else
        {

        }

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        if ( shopDetails.size() > 0 ) {
            ShopDetail shopDetail = (ShopDetail) getItem(position);
            if (shopDetail.getUrlSmallProfilePicture() != null && !shopDetail.getUrlSmallProfilePicture().isEmpty()) {
                GlideManager.sharedInstance().initializeGlide(context, shopDetail.getUrlSmallProfilePicture(), roundedImage, viewHolder.agentProfilePic);
            } else {
                GlideManager.sharedInstance().initializeGlide(context, R.drawable.user_unknown_menu, roundedImage, viewHolder.agentProfilePic);
            }
        }

        /*viewHolder.agentMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewHolder.agentMapBtn.setOnClickListener(null);
                //ViewHolder vhViewHolder = (ViewHolder) rootView.getTag();
                //vhViewHolder.agentMapBtn.setOnClickListener(null);
                bbsSearchAgentActivity.onIconMapClick(position);



            }
        });*/

        //apply semua modifikasi ke layout
        rootView.setTag(viewHolder);



        return rootView;
    }

    private void getAgentLocationSharedPreferences()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
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


}
