package com.sgo.saldomu.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BbsMemberListAdapter;
import com.sgo.saldomu.adapter.KelolaMerchantAdapter;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;
import com.sgo.saldomu.models.ShopDetail;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static io.realm.Realm.getDefaultInstance;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragKelolaMerchant extends Fragment {


    GridView gridview;
    KelolaMerchantAdapter customAdapter;
    ArrayList<String> menuItems     = new ArrayList<>();
    ArrayList<Integer> menuIcons    = new ArrayList<>();
    private Realm myRealm;
    SharedPreferences sp;
    RealmResults<MerchantCommunityList> dataResult;
    ArrayList<ShopDetail> shopDetails = new ArrayList<>();
    ArrayList<ShopDetail> tempDetails = new ArrayList<>();
    private BbsMemberListAdapter bbsMemberListAdapter;
    ListView lvListMember;

    public FragKelolaMerchant(ArrayList<ShopDetail> shopDetails) {
        // Required empty public constructor
        this.shopDetails = shopDetails;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp          = CustomSecurePref.getInstance().getmSecurePrefs();
        myRealm     = getDefaultInstance();

        /*dataResult = myRealm.where(MerchantCommunityList.class)
                .equalTo("memberCust", sp.getString(DefineValue.USERID_PHONE, ""))
                .equalTo("memberType", DefineValue.SHOP_MERCHANT)
                .findAll();
*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_kelola_merchant, container, false);

        if ( shopDetails.size() > 0 ) {
            for (int i = 0; i < shopDetails.size(); i++) {
                if ( shopDetails.get(i).getMemberType().equals(DefineValue.SHOP_MERCHANT) ) {
                    tempDetails.add(shopDetails.get(i));
                }
            }

        }

        bbsMemberListAdapter = new BbsMemberListAdapter(getActivity(), tempDetails);

        lvListMember    = (ListView) v.findViewById(R.id.list);
        lvListMember.setAdapter(bbsMemberListAdapter);

        /*gridview            = (GridView) v.findViewById(R.id.simpleGridView2);
        customAdapter       = new KelolaMerchantAdapter(v.getContext(), menuItems, menuIcons);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    Intent intent=new Intent(getActivity(),BbsKelolaAgentActivity.class);
                    intent.putExtra("shopId", dataResult.get(position).getShopId() );
                    intent.putExtra("memberId", dataResult.get(position).getMemberId()  );
                    intent.putExtra("memberType", dataResult.get(position).getMemberType()  );
                    startActivity(intent);

                /*
                TimePickerFragment timePickerFragment = new TimePickerFragment(BbsMerchantSetupHourActivity.this);

                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("NamaHari", setupOpenHour.getSetupOpenHours().get(position).getNamaHari());
                bundle.putString("startHour", setupOpenHour.getSetupOpenHours().get(position).getStartHour());
                bundle.putString("endHour", setupOpenHour.getSetupOpenHours().get(position).getEndHour());
                bundle.putInt("iStartHour", setupOpenHour.getSetupOpenHours().get(position).getiStartHour() );
                bundle.putInt("iStartMinute", setupOpenHour.getSetupOpenHours().get(position).getiStartMinute() );
                bundle.putInt("iEndHour", setupOpenHour.getSetupOpenHours().get(position).getiEndHour() );
                bundle.putInt("iEndMinute", setupOpenHour.getSetupOpenHours().get(position).getiEndMinute() );

                timePickerFragment.setArguments(bundle);
                timePickerFragment.show(getFragmentManager(),TimePickerFragment.TAG  );


            }
        });*/

        // Inflate the layout for this fragment
        return v;
    }

}
