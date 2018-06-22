package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.BankHeaderTopUp;
import com.sgo.saldomu.Beans.ListBankDataTopup;
import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.widgets.AnimatedExpandableListView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by thinkpad on 12/6/2016.
 */

//public class SelectBankProductAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
    public class SelectBankProductAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<listBankModel> mListDataHeader;
    private HashMap<String, listBankModel> mListDataChild;
    boolean is_expanded;
    private static final int NORMAL_VIEW = 0;
    private static final int ATM_VIEW = 1;
    private static final int OTHER_ATM_VIEW = 2;

    public SelectBankProductAdapter(Context context, List<listBankModel> listDataHeader,
                                    HashMap<String, listBankModel> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
        this.is_expanded = false;
    }

    static class ViewHolder {
        private HashMap<Integer, View> storedViews = new HashMap<>();
        ViewHolder addView(View view)
        {
            int id = view.getId();
            storedViews.put(id, view);
            return this;
        }

        public View getView(int id)
        {
            return storedViews.get(id);
        }
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        listBankModel headerObject = (listBankModel) getGroup(groupPosition);

        View v = convertView;
        LinearLayout itemClick;
        TextView itemTitle;
        ImageView indicator;

        if (v == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = infalInflater.inflate(R.layout.list_topup_child_item, parent,false);
            itemClick = v.findViewById(R.id.adapter_child_item_layout);
            indicator = v.findViewById(R.id.child_indicator);
            itemTitle = v.findViewById(R.id.adapter_child_title_child);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.addView(itemClick);
            viewHolder.addView(itemTitle);
            viewHolder.addView(indicator);
            v.setTag(viewHolder);
        }

        ViewHolder cholder = (ViewHolder) v.getTag();
        itemClick = (LinearLayout) cholder.getView(R.id.adapter_child_item_layout);
        itemTitle = (TextView) cholder.getView(R.id.adapter_child_title_child);
        indicator = (ImageView) cholder.getView(R.id.child_indicator);

        itemTitle.setText(headerObject.getProduct_name());

        if (isExpanded){
            indicator.setImageResource(R.drawable.child_indicator_small_expand);
        }else

            indicator.setImageResource(R.drawable.child_indicator_small_collapse);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        listBankModel childDataWrapper = (listBankModel) getChild(groupPosition, childPosition);
        View layout_view_child = convertView;

        if(layout_view_child == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout_view_child = inflater.inflate(getATMLayout(childDataWrapper), parent,false);

            TextView tvTitleATM = layout_view_child.findViewById(R.id.title_atm);
            TextView tvPinAccount = layout_view_child.findViewById(R.id.pin_account);
            tvTitleATM.setText(childDataWrapper.getBank_name());
            tvPinAccount.setText(childDataWrapper.getNoVA());

            TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
            if (tv_fee!=null) {
                if(childDataWrapper.getFee() == null)
                    tv_fee.setText("");
                else
                    tv_fee.setText(mContext.getString(R.string.listatm_topup_deskripsi_fee,childDataWrapper.getFee()));
            }
        }

        return layout_view_child;
    }

//    @Override
//    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//
//        listBankModel childDataWrapper = (listBankModel) getChild(groupPosition, childPosition);
//        View layout_view_child = convertView;
//
//        if(layout_view_child == null) {
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            layout_view_child = inflater.inflate(getATMLayout(childDataWrapper), parent,false);
//
//            TextView tvTitleATM = layout_view_child.findViewById(R.id.title_atm);
//            TextView tvPinAccount = layout_view_child.findViewById(R.id.pin_account);
//            tvTitleATM.setText(childDataWrapper.getBank_name());
//            tvPinAccount.setText(childDataWrapper.getNoVA());
//
//            TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
//            if (tv_fee!=null) {
//                if(childDataWrapper.getFee() == null)
//                    tv_fee.setText("");
//                else
//                    tv_fee.setText(mContext.getString(R.string.listatm_topup_deskripsi_fee,childDataWrapper.getFee()));
//            }
//        }
//
//        return layout_view_child;
//    }

//    @Override
//        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        listBankModel childDataWrapper = (listBankModel) getChild(groupPosition, childPosition);
////        ListBankDataTopup childDataBank = childDataWrapper.bank;
////        is_expanded = ((BankHeaderTopUp)getGroup(groupPosition)).getExpanded();
//        int itemType = getChildType(groupPosition,childPosition);
//        View layout_view_child = convertView;
//        View v_divider,childItemLayout, layoutAtm;
//        ImageView indicator;
//        TextView title_child;
//        childItemLayout = convertView;
//        if(childItemLayout == null) {
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            switch (itemType){
//                case NORMAL_VIEW:
//                    layout_view_child = inflater.inflate(R.layout.list_topup_child_item, parent,false);
//                    break;
//                case ATM_VIEW:
//                    layout_view_child = inflater.inflate(R.layout.list_topup_child_atm_item, parent,false);
//                    break;
//                case OTHER_ATM_VIEW:
//                    layout_view_child = inflater.inflate(R.layout.list_topup_atm_others_item, parent,false);
//                    break;
//            }
//
//            holder = new ViewHolder();
//            v_divider = layout_view_child.findViewById(R.id.divider);
//            holder.addView(v_divider);
//
//            if(itemType == OTHER_ATM_VIEW){
//                TextView tvPinAccount = layout_view_child.findViewById(R.id.pin_account);
//                TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
//                holder.addView(tvPinAccount);
//                holder.addView(tv_fee);
//            }
//            else{
//                childItemLayout =layout_view_child.findViewById(R.id.child_item_layout);
//                title_child = layout_view_child.findViewById(R.id.title_child);
//                indicator = layout_view_child.findViewById(R.id.child_indicator);
//
//                if(itemType == ATM_VIEW){
//                    TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
//                    holder.addView(tv_fee);
//                }
//
//                holder.addView(title_child);
//                holder.addView(childItemLayout);
//                holder.addView(indicator);
//            }
//            layout_view_child.setTag(holder);
//        }
//
//        holder = (ViewHolder) layout_view_child.getTag();
//
//        if(itemType == OTHER_ATM_VIEW){
////            ((TextView)holder.getView(R.id.pin_account)).setText(childDataWrapper.getNoVa());
////            if(childDataWrapper.getFee() == null)
////                holder.getView(R.id.fee_deskripsi).setVisibility(View.GONE);
////            else
////                ((TextView)holder.getView(R.id.fee_deskripsi)).setText(mContext.getString(R.string.listatm_topup_deskripsi_fee,childDataWrapper.getFee()));
//        }
//        else {
//            title_child = (TextView) holder.getView(R.id.title_child);
//            childItemLayout = holder.getView(R.id.child_item_layout);
//            indicator = (ImageView) holder.getView(R.id.child_indicator);
//            if(itemType == ATM_VIEW){
//                TextView tv_fee = (TextView)holder.getView(R.id.fee_deskripsi);
//                layout_view_child.findViewById(R.id.layout_atm_mandiri).setVisibility(View.GONE);
//                layout_view_child.findViewById(R.id.layout_atm_bri).setVisibility(View.GONE);
//                layout_view_child.findViewById(R.id.layout_atm_bca_stk).setVisibility(View.GONE);
//                layout_view_child.findViewById(R.id.layout_atm_bca_klikbcava).setVisibility(View.GONE);
//                layout_view_child.findViewById(R.id.layout_atm_bca_mbca).setVisibility(View.GONE);
//                layout_view_child.findViewById(R.id.layout_atm).setVisibility(View.GONE);
//                layoutAtm = layout_view_child.findViewById(getATMLayoutID(childDataWrapper));
////                if (childDataBank.getVisible()) {
//                layoutAtm.setVisibility(View.VISIBLE);
////                    tv_fee.setVisibility(View.VISIBLE);
////                    indicator.setImageResource(R.drawable.child_indicator_small_expand);
////                    childItemLayout.setBackgroundResource(R.color.colorPrimary);
////                    title_child.setTextColor(mContext.getResources().getColor(R.color.white));
////                }else {
////                    layoutAtm.setVisibility(View.GONE);
////                    tv_fee.setVisibility(View.GONE);
////                    indicator.setImageResource(R.drawable.child_indicator_small_collapse);
////                    childItemLayout.setBackgroundResource(R.color.grey_200);
////                    title_child.setTextColor(mContext.getResources().getColor(R.color.black));
////                }
//
//                TextView tvTitleATM = layoutAtm.findViewById(R.id.title_atm);
//                TextView tvPinAccount = layoutAtm.findViewById(R.id.pin_account);
//                tvTitleATM.setText(childDataWrapper.getBank_name());
//                tvPinAccount.setText(childDataWrapper.getNoVA());
//                if(childDataWrapper.getFee() == null)
//                    tv_fee.setText("");
//                else
//                    tv_fee.setText(mContext.getString(R.string.listatm_topup_deskripsi_fee,childDataWrapper.getFee()));
//            }
//
//            title_child.setText(childDataWrapper.getProduct_name());
//        }
//
//        v_divider = holder.getView(R.id.divider);
//
//        if (is_expanded && isLastChild){
//            v_divider.setVisibility(View.VISIBLE);
//        }else
//            v_divider.setVisibility(View.GONE);
//        return layout_view_child;
//    }

    private int getATMLayoutID(listBankModel bankData){
        String bankCode = bankData.getBank_code();
        if(bankCode.equals(DefineValue.BankMandiri))
            return R.id.layout_atm_mandiri;
        else if (bankCode.equals(DefineValue.BankBRI))
            return R.id.layout_atm_bri;
        else if (bankCode.equals(DefineValue.BankBCA)) {
            if (bankData.getProduct_code().equals(DefineValue.PRODUCT_BCA_SIMTOOLKIT))
                return R.id.layout_atm_bca_stk;
            else if (bankData.getProduct_code().equals(DefineValue.PRODUCT_BCA_KLIKBCAVA))
                return R.id.layout_atm_bca_klikbcava;
            else if (bankData.getProduct_code().equals(DefineValue.PRODUCT_BCA_MOBILEBANK))
                return R.id.layout_atm_bca_mbca;
        }
        else if (bankCode.equals(DefineValue.BankDanamon))
            return R.id.layout_atm_danamon;
        else if (bankCode.equals(DefineValue.BankBII))
            return R.id.layout_atm_bii;
        else if (bankCode.equals(DefineValue.BankCIMB))
            return R.id.layout_atm_cimb;
        return R.id.layout_atm;
    }

    private int getATMLayout(listBankModel bankData){
        String bankCode = bankData.getBank_code();
        if(bankCode.equals(DefineValue.BankMandiri))
            return R.layout.list_topup_atm_mandiri_item;
        else if (bankCode.equals(DefineValue.BankBRI))
            return R.layout.list_topup_atm_bri_item;
        else if (bankCode.equals(DefineValue.BankBCA)) {
            if (bankData.getProduct_code().equals(DefineValue.PRODUCT_BCA_SIMTOOLKIT))
                return R.layout.list_topup_atm_bca_simtoolkit;
            else if (bankData.getProduct_code().equals(DefineValue.PRODUCT_BCA_KLIKBCAVA))
                return R.layout.list_topup_atm_bca_klikva;
            else if (bankData.getProduct_code().equals(DefineValue.PRODUCT_BCA_MOBILEBANK))
                return R.layout.list_topup_atm_bca_mbca;
        }
        else if (bankCode.equals(DefineValue.BankDanamon))
            return R.layout.list_topup_atm_danamon_item;
        else if (bankCode.equals(DefineValue.BankBII))
            return R.layout.list_topup_maybank_item;
        else if (bankCode.equals(DefineValue.BankCIMB))
            return R.layout.list_topup_atm_cimb_item;
        return R.layout.list_topup_atm_item;
    }

//    public void toggleVisible(View v, int group_pos, int child_pos){
//        ListBankDataTopup bankDataTopUp = this.mListDataChild.get(this.mListDataHeader.get(group_pos).getHeader()).getBankData().get(child_pos);
//        if(bankDataTopUp.getVisible())
//            bankDataTopUp.setVisible(false);
//        else
//            bankDataTopUp.setVisible(true);
//        notifyDataSetChanged();
//    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mListDataHeader.get(groupPosition).getProduct_type().equals("ATM"))
            return 1;
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

//    @Override
//    public int getRealChildrenCount(int groupPosition) {
//        if (mListDataHeader.get(groupPosition).getProduct_type().equals("ATM"))
//            return 1;
//        return 0;
//    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition).getProduct_code());
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

