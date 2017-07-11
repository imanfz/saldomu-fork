package com.sgo.hpku.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sgo.hpku.R;
import com.sgo.hpku.activities.BbsMemberLocationActivity;
import com.sgo.hpku.activities.BbsMerchantSetupHourActivity;
import com.sgo.hpku.adapter.BbsMemberListAdapter;
import com.sgo.hpku.adapter.KelolaAgentAdapter;
import com.sgo.hpku.coreclass.CustomSecurePref;
import com.sgo.hpku.coreclass.DefineValue;
import com.sgo.hpku.entityRealm.MerchantCommunityList;
import com.sgo.hpku.models.ShopDetail;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static io.realm.Realm.getDefaultInstance;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragKelolaAgent extends Fragment {

    GridView gridview;
    KelolaAgentAdapter customAdapter;
    ArrayList<String> menuItems     = new ArrayList<>();
    ArrayList<Integer> menuIcons    = new ArrayList<>();
    private Realm myRealm;
    SharedPreferences sp;
    RealmResults<MerchantCommunityList> dataResult;
    ImageView ivLocation, ivOpenHour, ivTutupManual;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    ArrayList<ShopDetail> tempDetails = new ArrayList<>();
    private BbsMemberListAdapter bbsMemberListAdapter;
    ListView lvListMember;

    public FragKelolaAgent(ArrayList<ShopDetail> shopDetails) {
        // Required empty public constructor
        this.shopDetails = shopDetails;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp          = CustomSecurePref.getInstance().getmSecurePrefs();
        myRealm     = getDefaultInstance();

/*        dataResult = myRealm.where(MerchantCommunityList.class)
                .equalTo("memberCust", sp.getString(DefineValue.USERID_PHONE, ""))
                .equalTo("memberType", DefineValue.SHOP_AGENT)
                .findAll();
*/


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_kelola_agent, container, false);

        if ( shopDetails.size() > 0 ) {
            for (int i = 0; i < shopDetails.size(); i++) {
                if ( shopDetails.get(i).getMemberType().equals(DefineValue.SHOP_AGENT) ) {
                    tempDetails.add(shopDetails.get(i));
                }
            }

        }

        bbsMemberListAdapter = new BbsMemberListAdapter(getActivity(), tempDetails);

        lvListMember    = (ListView) v.findViewById(R.id.list);
        lvListMember.setAdapter(bbsMemberListAdapter);

        /*ivLocation      = (ImageView) v.findViewById(R.id.ivLocation);
        ivTutupManual   = (ImageView) v.findViewById(R.id.ivTutupManual);
        ivOpenHour      = (ImageView) v.findViewById(R.id.ivOpenHour);

        TextView tvAgentName    = (TextView) v.findViewById(R.id.agentName);
        TextView tvCategory     = (TextView) v.findViewById(R.id.agentKategori);
        TextView tvNoTelp       = (TextView) v.findViewById(R.id.agentNoTelp);

        if ( shopDetails.size() > 0 ) {
            tvAgentName.setText(shopDetails.get(0).getMemberName());
            tvCategory.setText("");
            tvNoTelp.setText("");
        }*/

        /*ivLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( shopDetails.size() > 0 ) {
                    Intent intent = new Intent(getActivity(), BbsMemberLocationActivity.class);
                    intent.putExtra("memberId", dataResult.get(0).getMemberId());
                    intent.putExtra("shopId", dataResult.get(0).getShopId());
                    startActivity(intent);
                }
            }
        });

        ivTutupManual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( dataResult.size() > 0 ) {
                    Intent intent = new Intent(getActivity(), BbsRegisterOpenClosedShopActivity.class);
                    intent.putExtra("memberId", dataResult.get(0).getMemberId());
                    intent.putExtra("shopId", dataResult.get(0).getShopId());
                    startActivity(intent);
                }
            }
        });

        ivOpenHour.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( dataResult.size() > 0 ) {
                    Intent intent = new Intent(getActivity(), BbsMerchantSetupHourActivity.class);
                    intent.putExtra("memberId", dataResult.get(0).getMemberId());
                    intent.putExtra("shopId", dataResult.get(0).getShopId());
                    startActivity(intent);
                }
            }
        });*/

        //menuItems.add(getString(R.string.menu_settings));
        //menuIcons.add(R.drawable.ic_settings_kelola);
        /*
        gridview            = (GridView) v.findViewById(R.id.simpleGridView);
        customAdapter       = new KelolaAgentAdapter(v.getContext(), menuItems, menuIcons);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(getActivity(),BbsKelolaActivity.class);
                intent.putExtra("shopId", dataResult.get(position).getShopId() );
                intent.putExtra("memberId", dataResult.get(position).getMemberId()  );
                intent.putExtra("memberType", dataResult.get(position).getMemberType()  );
                startActivity(intent);
            }
        });
        */

        // Inflate the layout for this fragment
        return v;
    }

}
