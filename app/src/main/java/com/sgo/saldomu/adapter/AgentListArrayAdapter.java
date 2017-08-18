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
import android.widget.ListView;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsSearchAgentActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.AgentConstant;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.MyPicasso;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.models.ShopDetail;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Lenovo Thinkpad on 12/5/2016.
 */
public class AgentListArrayAdapter extends BaseAdapter implements View.OnClickListener
{
    int layoutResourceId;
    Context context;
    String[] menuList;
    private JSONArray agentLocation = null;
    private ImageView agentMapBtn;
    private ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    private BbsSearchAgentActivity bbsSearchAgentActivity;
    Picasso mPic;

    public AgentListArrayAdapter(Context context, int layoutResourceId, ArrayList<ShopDetail> shopDetails)
    {
        //super(context, layoutResourceId, shopDetails);
        this.layoutResourceId = layoutResourceId;
        this.context  = context;
        this.menuList = menuList;

        this.shopDetails = shopDetails;

        //get data agent from session
        getAgentLocationSharedPreferences();
        bbsSearchAgentActivity = (BbsSearchAgentActivity) context;
    }

    /*public void setAgentList(JSONArray agent)
    {
        agentLocation = agent;
    }*/

    static class ViewHolder
    {
        LinearLayout agentServiceList;
        TextView agentName;
        TextView agentLastOnline;
        TextView agentAddress;
        ImageView agentProfilePic;
        //ImageView agentRate;
        TextView agentDistance;
        //TextView agentAvailable;
        ImageView agentMapBtn;
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
        View rootView = convertView;
        ViewHolder viewHolder;

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
        viewHolder.agentServiceList       = (LinearLayout) rootView.findViewById(R.id.agent_service_list);
        viewHolder.agentName       = (TextView)rootView.findViewById(R.id.agentName);
        viewHolder.agentLastOnline = (TextView)rootView.findViewById(R.id.agentLastOnline);
        viewHolder.agentAddress    = (TextView)rootView.findViewById(R.id.agentAddress);
        viewHolder.agentProfilePic = (ImageView)rootView.findViewById(R.id.agentProfilePic);
        //viewHolder.agentRate = (ImageView)rootView.findViewById(R.id.agentRate);
        viewHolder.agentDistance   = (TextView)rootView.findViewById(R.id.agentDistance);
        //viewHolder.agentAvailable   = (TextView)rootView.findViewById(R.id.available);

        viewHolder.agentMapBtn = (ImageView)rootView.findViewById(R.id.agentMapBtn);
        viewHolder.agentMapBtn.setOnClickListener(this);

        //set default value
        String agentName        = "N/A";
        String agentLastOnline  = "N/A";
        String agentAddress     = "N/A";
        String agentProfilePic  = "N/A";
        String agentRate  = "N/A";
        String agentDistance    = "N/A";
        String agentAvailable    = "N/A";
        String agentServiceList    = "N/A";

        /*if ( shopDetails.size() > 0 )
        {

            agentName = shopDetails.get(position).getMemberName();
            agentAddress = shopDetails.get(position).getShopAddress();
            agentDistance = shopDetails.get(position).getCalculatedDistance();

        }*/
        if ( shopDetails.size() > 0 ) {
            ShopDetail shopDetail = (ShopDetail) getItem(position);
            agentName = shopDetail.getMemberName();
            agentAddress = shopDetail.getShopAddress();
            agentDistance = shopDetail.getCalculatedDistance();
        }

        /*try
        {
            //convert json array to json object
            JSONObject object = agentLocation.getJSONObject(position);

            //get value from session
            if(!object.isNull("name"))     agentName       = object.getString("name");
            if(!object.isNull("date"))     agentLastOnline = object.getString("date");
            if(!object.isNull("address"))  agentAddress    = object.getString("address");
            if(!object.isNull("image"))    agentProfilePic = object.getString("image");
            if(!object.isNull("rate"))    agentRate = object.getString("rate");
            if(!object.isNull("distance")) agentDistance   = object.getString("distance");
            if(!object.isNull("available_flag")) agentAvailable   = object.getString("available_flag");
            if(!object.isNull("services")) agentServiceList   = object.getString("services");

            //modification data
            //agentProfilePic = "http://192.168.43.206/public/images/" + agentProfilePic;
            //agentProfilePic = "http://192.168.0.100/public/images/" + agentProfilePic;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }*/

        //attach all data to view holder
        viewHolder.agentName.setText(agentName);
        viewHolder.agentLastOnline.setText(agentLastOnline);
        viewHolder.agentAddress.setText(agentAddress);
        viewHolder.agentDistance.setText(agentDistance);

        if(agentAvailable.equalsIgnoreCase("Y")) {

            /*viewHolder.agentAvailable.setText("Available");
            viewHolder.agentAvailable.setTextColor(Color.WHITE);
            viewHolder.agentAvailable.setBackgroundColor(Color.GREEN);*/
        }
        else
        {

            /*viewHolder.agentAvailable.setText("Not Available");
            viewHolder.agentAvailable.setTextColor(Color.WHITE);
            viewHolder.agentAvailable.setBackgroundColor(Color.RED);*/
        }

            /*ImageLoader imageLoader = ImageLoader.getInstance();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
            ImageLoader.getInstance().init(config);
            imageLoader.displayImage(agentProfilePic, viewHolder.agentProfilePic);*/

        //int profile = context.getResources().getIdentifier(agentProfilePic, "drawable", context.getPackageName());

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);


        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(context);
        else
            mPic= Picasso.with(context);

        if ( shopDetails.size() > 0 ) {
            ShopDetail shopDetail = (ShopDetail) getItem(position);
            if (shopDetail.getUrlSmallProfilePicture() != null && !shopDetail.getUrlSmallProfilePicture().isEmpty()) {
                mPic.load(shopDetail.getUrlSmallProfilePicture())
                        .error(roundedImage)
                        .fit().centerInside()
                        .placeholder(R.drawable.progress_animation)
                        .transform(new RoundImageTransformation()).into(viewHolder.agentProfilePic);
            } else {
                mPic.load(R.drawable.user_unknown_menu)
                        .error(roundedImage)
                        .fit().centerInside()
                        .placeholder(R.drawable.progress_animation)
                        .transform(new RoundImageTransformation()).into(viewHolder.agentProfilePic);
            }
        }

        //int profile = context.getResources().getIdentifier("R.drawable.user_unknown_menu", "drawable", context.getPackageName());
        //viewHolder.agentProfilePic.setImageResource(R.drawable.user_unknown_menu);
/*
        int rate = context.getResources().getIdentifier(agentRate, "drawable", context.getPackageName());
        viewHolder.agentRate.setImageResource(rate);
*/

        viewHolder.agentMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bbsSearchAgentActivity.onIconMapClick(position);
            }
        });

        /*List<String> items = Arrays.asList(agentServiceList.split("\\s*,\\s*"));

        for(int y = 0; y < items.size(); y++)
        {
//            List<AgentService> itemList = new Select().all().from(AgentService.class).where("agent_no = ?", y).execute();

            TextView services = new TextView(context);
            services.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
            services.setText(items.get(y) + " : YES");
            services.setTypeface(null, Typeface.BOLD);
            services.setPadding(4, 0, 4, 0);
            viewHolder.agentServiceList.addView(services);
        }*/


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

    //implements View.OnClickListener
    @Override
    public void onClick(View view)
    {
        if(view.getId() == agentMapBtn.getId())
        {
            //get object listview from view layput
            View parentRow = (View) view.getParent(); //up 1 level to parent relative layout
            View parentRow2 = (View) parentRow.getParent(); //up 1 level again to parent linear layout
            ListView listView = (ListView) parentRow2.getParent(); //up 1 level again to parent list view

            //get position on item list when button clicked


            int position = listView.getPositionForView(parentRow2);

            if ( this.context instanceof MainPage ) {
                //bbsSearchAgentActivity.onIconMapClick(position);
            } else if ( this.context instanceof BbsSearchAgentActivity ) {
                //bbsSearchAgentActivity.onIconMapClick(position);
                //((BbsSearchAgentActivity) this.context).dataUpdated();
                //AgentListFragment agentListFragment
            }

            //(BbsSearchAgentActivity getActivity()).
            //create fragment
            /*AgentMapFragment agentMapBbsFragment  = new AgentMapFragment();
            agentMapBbsFragment.setSingleAgent(position);
            //((Activity)context).getFragmentManager()
            ((AppCompatActivity)context).getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.listContent, agentMapBbsFragment).commit();
*/
            //update title in action bar
            /*try
            {
                JSONObject object = agentLocation.getJSONObject(position);
                //((AppCompatActivity)context).getSupportActionBar().setTitle(object.getString("name"));
                //get object activity
                MainAgentActivity mainBbsActivity = (MainAgentActivity)context;
                mainBbsActivity.initializeToolbar(object.getString("name"));
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }*/

        }

    }
}
