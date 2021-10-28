package com.sgo.saldomu.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.viewpager.widget.ViewPager;

import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.BbsMyOrdersTabAdapter;
import com.sgo.saldomu.dialogs.InformationDialog;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 * Use the {@link FragBbsMyOrders#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragBbsMyOrders extends Fragment {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Bbs_My_Orders";

    private BbsMyOrdersTabAdapter currentAdapternya;
    private View currentView;
    private InformationDialog dialogI;

    public FragBbsMyOrders() {
        // Required empty public constructor
    }

    public static FragBbsMyOrders newInstance() {
        return new FragBbsMyOrders();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_bbs_my_orders, container, false);
        setCurrentView(v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                    .getDisplayMetrics());
            BbsMyOrdersTabAdapter adapternya;
            TabPageIndicator tabs;
            ViewPager pager;
            String[] titles = getActivity().getResources().getStringArray(R.array.bbs_order_status);

            dialogI = InformationDialog.newInstance(10);
            dialogI.setTargetFragment(this,0);
            List<ListFragment> mList = new ArrayList<>();
            mList.add(FragReport.newInstance(FragReport.REPORT_ESPAY));
            mList.add(FragReport.newInstance(FragReport.REPORT_SCASH));
            mList.add(FragReport.newInstance(FragReport.REPORT_ASK));

            tabs = (TabPageIndicator) getCurrentView().findViewById(R.id.report_tabs);
            pager = (ViewPager) getCurrentView().findViewById(R.id.report_pager);
            adapternya = new BbsMyOrdersTabAdapter(getChildFragmentManager(), getActivity(), mList, titles);
            setTargetFragment(this, 0);
            pager.setAdapter(adapternya);
            pager.setPageMargin(pageMargin);
            tabs.setViewPager(pager);
            pager.setCurrentItem(0);

            setCurrentAdapternya(adapternya);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private View getCurrentView() {
        return currentView;
    }

    private void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public BbsMyOrdersTabAdapter getCurrentAdapternya() {
        return currentAdapternya;
    }

    private void setCurrentAdapternya(BbsMyOrdersTabAdapter currentAdapternya) {
        this.currentAdapternya = currentAdapternya;
    }
}
