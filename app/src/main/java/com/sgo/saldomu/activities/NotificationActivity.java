package com.sgo.saldomu.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.PageTabAdapter;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.fragments.FragMessage;
import com.sgo.saldomu.fragments.FragNotification;
import com.sgo.saldomu.widgets.BaseActivity;

/*
  Created by Administrator on 5/6/2015.
 */
public class NotificationActivity extends BaseActivity {

    public final static int TYPE_LIKE = 2;
    public final static int TYPE_COMMENT = 3;
    public final static int TYPE_TRANSFER = 6;
    public final static int TYPE_PAID = 7;
    public final static int TYPE_DECLINE = 8;
    public final static int TYPE_NON_MEMBER= 10;
    public final static int CLAIM_NON_MEMBER= 11;
    public final static int REJECTED_KTP= 13;
    public final static int REJECTED_SIUP_NPWP= 14;
    public final static int BLAST_INFO= 15;
    public final static int SOURCE_OF_FUND= 16;

    public final static int P2PSTAT_PENDING = 1;
    public final static int P2PSTAT_PAID = 2;
    public final static int P2PSTAT_FAILED = 3;
    public final static int P2PSTAT_SUSPECT = 4;
    public final static int P2PSTAT_CANCELLED = 5;

    // Info Toko untuk bayar invoice
    public final static int TOKO_PAY_INVOICE = 18;
    // Info Kode Otp Create PO
    public final static int OTP_CREATE_PO = 19;
    //Info Kode Otp Create GR
    public final static int OTP_CREATE_GR = 20;
    //Info GR baru created  , klik notif panggil ws no. 8. InquiryDocDetail
    public final static int CREATE_GR = 21;
    //Info Invoice Paid ,  klik notif panggil ws no. 8. InquiryDocDetail
    public final static int INVOICE_PAID = 22;
    //Info New Invoice for Toko ,  klik notif panggil ws no. 8. InquiryDocDetail
    public final static int NEW_INVOICE_TOKO = 23;
    //Info Canvasser ada New Invoice for Toko,  klik notif panggil ws no. 8. InquiryDocDetail
    public final static int NEW_INVOICE_CANVASSER= 24;

    public final static int UNREAD = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeToolbar();

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        PageTabAdapter adapter = new PageTabAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragNotification(), getString(R.string.notifications));
        adapter.addFragment(new FragMessage(), getString(R.string.payfriends_text_message));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initializeToolbar(){
        setActionBarIcon(R.drawable.ic_arrow_left);
        setActionBarTitle(getString(R.string.notifications_ab_title));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        RetrofitService.dispose();
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_tab;
    }
}