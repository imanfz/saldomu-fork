package com.sgo.orimakardaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.sgo.orimakardaya.R;
import com.sgo.orimakardaya.activities.FriendsViewDetailActivity;
import com.sgo.orimakardaya.activities.MainPage;
import com.sgo.orimakardaya.coreclass.MyApiClient;
import com.sgo.orimakardaya.coreclass.MyPicasso;
import com.sgo.orimakardaya.coreclass.RoundedQuickContactBadge;
import com.squareup.picasso.Picasso;

/**
 * Created by thinkpad on 5/5/2015.
 */
public class FragFriendsViewDetail extends Fragment {

    View v;
    RoundedQuickContactBadge friendsPicContent;
    TextView tvName, tvID, tvPhone, tvEmail;
    Button btnAsk, btnPay;
    int RESULT;
    String imgUrl, name, id, phone, email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_friends_view_detail, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        friendsPicContent = (RoundedQuickContactBadge) getActivity().findViewById(R.id.friends_pic_content);
        tvName = (TextView) getActivity().findViewById(R.id.friends_value_name);
        tvID = (TextView) getActivity().findViewById(R.id.friends_value_id);
        tvPhone = (TextView) getActivity().findViewById(R.id.friends_value_phone);
        tvEmail = (TextView) getActivity().findViewById(R.id.friends_value_email);
        btnAsk = (Button) getActivity().findViewById(R.id.btn_ask_for_money);
        btnPay = (Button) getActivity().findViewById(R.id.btn_pay);

        Picasso mPic;
        if(MyApiClient.PROD_FLAG_ADDRESS)
            mPic = MyPicasso.getImageLoader(getActivity());
        else
            mPic= Picasso.with(getActivity());

        Intent i = getActivity().getIntent();
        if(i != null) {
            imgUrl = i.getStringExtra("image");
            name = i.getStringExtra("name");
            id = i.getStringExtra("id");
            phone = i.getStringExtra("phone");
            email = i.getStringExtra("email");

            if(imgUrl.equals(""))
                mPic.load(R.drawable.user_unknown_menu)
                    .fit()
                    .centerCrop()
                    .into(friendsPicContent);
            else
                mPic.load(imgUrl)
                    .error(R.drawable.user_unknown_menu)
                    .placeholder(R.anim.progress_animation)
                    .fit()
                    .centerCrop()
                    .into(friendsPicContent);

            tvName.setText(name);
            tvID.setText(id);
            tvPhone.setText(phone);
            tvEmail.setText(email);
        }
        else {
            mPic.load(R.drawable.user_unknown_menu)
                .fit()
                .centerCrop()
                .into(friendsPicContent);
        }

        btnAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new FragAskForMoney();
                Bundle args = new Bundle();
                args.putString("image", imgUrl);
                args.putString("name", name);
                args.putString("id", id);
                args.putString("phone", phone);
                args.putString("email", email);
                newFragment.setArguments(args);
                switchFragment(newFragment, getResources().getString(R.string.menu_item_title_ask_for_money), true);
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new FragPayFriends();
                Bundle args = new Bundle();
                args.putString("image", imgUrl);
                args.putString("name", name);
                args.putString("id", id);
                args.putString("phone", phone);
                args.putString("email", email);
                newFragment.setArguments(args);
                switchFragment(newFragment, getResources().getString(R.string.menu_item_title_pay_friends), true);
            }
        });
        RESULT = MainPage.RESULT_NORMAL;

    }

    private void switchFragment(Fragment i, String name, Boolean isBackstack){
        if (getActivity() == null)
            return;

        FriendsViewDetailActivity fca = (FriendsViewDetailActivity) getActivity();
        fca.switchContent(i,name,isBackstack);
    }
}
