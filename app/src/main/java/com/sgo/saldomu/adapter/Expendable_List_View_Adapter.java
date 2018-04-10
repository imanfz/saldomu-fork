package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.Beans.BankDataTopUp;
import com.sgo.saldomu.Beans.BankHeaderTopUp;
import com.sgo.saldomu.Beans.ListBankDataTopup;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;

import java.util.HashMap;
import java.util.List;

/**
 * Created by thinkpad on 12/6/2016.
 */

public class Expendable_List_View_Adapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<BankHeaderTopUp> mListDataHeader;
    private HashMap<String, BankDataTopUp> mListDataChild;
    boolean is_expanded;
    private static final int NORMAL_VIEW = 0;
    private static final int ATM_VIEW = 1;
    private static final int OTHER_ATM_VIEW = 2;

    public Expendable_List_View_Adapter(Context context, List<BankHeaderTopUp> listDataHeader,
                                        HashMap<String,BankDataTopUp> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
        this.is_expanded = false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
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
        BankHeaderTopUp headerObject = (BankHeaderTopUp) getGroup(groupPosition);

        View v = convertView;
        ImageView tri_img;
        ImageView logo_img;
        TextView group_title;
        View v_divider;
        if (v == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = infalInflater.inflate(R.layout.list_topup_group_item, parent,false);
            tri_img = v.findViewById(R.id.triangle);
            logo_img = v.findViewById(R.id.logo);
            group_title = v.findViewById(R.id.group_title);
            v_divider = v.findViewById(R.id.divider);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.addView(tri_img);
            viewHolder.addView(logo_img);
            viewHolder.addView(group_title);
            viewHolder.addView(v_divider);
            v.setTag(viewHolder);
        }

        ViewHolder cholder = (ViewHolder) v.getTag();
        tri_img = (ImageView) cholder.getView(R.id.triangle);
        logo_img = (ImageView) cholder.getView(R.id.logo);
        group_title = (TextView) cholder.getView(R.id.group_title);
        v_divider = cholder.getView(R.id.divider);

        group_title.setText(headerObject.getHeader());
        BankDataTopUp bankData = mListDataChild.get(headerObject.getHeader());

        int logoId = 0;
        switch (bankData.getBankCode()) {
            case DefineValue.BankJatim:
                logoId = R.drawable.logo_bank_jatim_small;
                break;
            case DefineValue.BankBCA:
                logoId = R.drawable.logo_bca_bank_small;
                break;
            case DefineValue.BankMandiri:
                logoId = R.drawable.logo_mandiri_bank_small;
                break;
            case DefineValue.BankMaspion:
                logoId = R.drawable.logo_bank_maspion_rev1_small;
                break;
            case DefineValue.BankPermata:
                logoId = R.drawable.logo_bank_permata;
                break;
            case DefineValue.BankBII:
                logoId = R.drawable.logo_bank_bii;
                break;
            case DefineValue.BankUOB:
                logoId = R.drawable.logo_bank_uob_small;
                break;
            case DefineValue.BankBRI:
                logoId = R.drawable.logo_bank_bri_small;
                break;
            case DefineValue.BankBNI:
                logoId = R.drawable.logo_bank_bni;
                break;
        }

        if (headerObject.getHeader().equals(mContext.getString(R.string.other_bank)))
            logo_img.setVisibility(View.GONE);
        else {
            logo_img.setImageResource(logoId);
            logo_img.setVisibility(View.VISIBLE);
        }


        if(isExpanded){
            tri_img.setImageResource(R.drawable.triangle_open);
            v_divider.setVisibility(View.VISIBLE);
            headerObject.setExpanded(true);
            group_title.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }else {
            tri_img.setImageResource(R.drawable.triangle_close);
            v_divider.setVisibility(View.GONE);
            headerObject.setExpanded(false);
            group_title.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        BankDataTopUp childDataWrapper = (BankDataTopUp) getChild(groupPosition, childPosition);
        ListBankDataTopup childDataBank = childDataWrapper.getBankData().get(childPosition);
        is_expanded = ((BankHeaderTopUp)getGroup(groupPosition)).getExpanded();
        int itemType = getChildType(groupPosition,childPosition);
        View layout_view_child = convertView;
        View v_divider,childItemLayout, layoutAtm;
        ImageView indicator;
        TextView title_child;
        childItemLayout = convertView;
        if(childItemLayout == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (itemType){
                case NORMAL_VIEW:
                    layout_view_child = inflater.inflate(R.layout.list_topup_child_item, parent,false);
                    break;
                case ATM_VIEW:
                    layout_view_child = inflater.inflate(R.layout.list_topup_child_atm_item, parent,false);
                    break;
                case OTHER_ATM_VIEW:
                    layout_view_child = inflater.inflate(R.layout.list_topup_atm_others_item, parent,false);
                    break;
            }

            holder = new ViewHolder();
            v_divider = layout_view_child.findViewById(R.id.divider);
            holder.addView(v_divider);

            if(itemType == OTHER_ATM_VIEW){
                TextView tvPinAccount = layout_view_child.findViewById(R.id.pin_account);
                TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
                holder.addView(tvPinAccount);
                holder.addView(tv_fee);
            }
            else{
                childItemLayout =layout_view_child.findViewById(R.id.child_item_layout);
                title_child = layout_view_child.findViewById(R.id.title_child);
                indicator = layout_view_child.findViewById(R.id.child_indicator);

                if(itemType == ATM_VIEW){
                    TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
                    holder.addView(tv_fee);
                }

                holder.addView(title_child);
                holder.addView(childItemLayout);
                holder.addView(indicator);
            }
            layout_view_child.setTag(holder);
        }

        holder = (ViewHolder) layout_view_child.getTag();

        if(itemType == OTHER_ATM_VIEW){
            ((TextView)holder.getView(R.id.pin_account)).setText(childDataWrapper.getNoVa());
            if(childDataWrapper.getFee() == null)
                holder.getView(R.id.fee_deskripsi).setVisibility(View.GONE);
            else
                ((TextView)holder.getView(R.id.fee_deskripsi)).setText(mContext.getString(R.string.listatm_topup_deskripsi_fee,childDataWrapper.getFee()));
        }
        else {
            title_child = (TextView) holder.getView(R.id.title_child);
            childItemLayout = holder.getView(R.id.child_item_layout);
            indicator = (ImageView) holder.getView(R.id.child_indicator);
            if(itemType == ATM_VIEW){
                TextView tv_fee = (TextView)holder.getView(R.id.fee_deskripsi);
                layout_view_child.findViewById(R.id.layout_atm_mandiri).setVisibility(View.GONE);
                layout_view_child.findViewById(R.id.layout_atm_bri).setVisibility(View.GONE);
                layout_view_child.findViewById(R.id.layout_atm_bca_stk).setVisibility(View.GONE);
                layout_view_child.findViewById(R.id.layout_atm_bca_klikbcava).setVisibility(View.GONE);
                layout_view_child.findViewById(R.id.layout_atm_bca_mbca).setVisibility(View.GONE);
                layout_view_child.findViewById(R.id.layout_atm).setVisibility(View.GONE);
                layoutAtm = layout_view_child.findViewById(getATMLayoutID(childDataBank));
                if (childDataBank.getVisible()) {
                    layoutAtm.setVisibility(View.VISIBLE);
                    tv_fee.setVisibility(View.VISIBLE);
                    indicator.setImageResource(R.drawable.child_indicator_small_expand);
                    childItemLayout.setBackgroundResource(R.color.colorPrimary);
                    title_child.setTextColor(mContext.getResources().getColor(R.color.white));
                }else {
                    layoutAtm.setVisibility(View.GONE);
                    tv_fee.setVisibility(View.GONE);
                    indicator.setImageResource(R.drawable.child_indicator_small_collapse);
                    childItemLayout.setBackgroundResource(R.color.grey_200);
                    title_child.setTextColor(mContext.getResources().getColor(R.color.black));
                }

                TextView tvTitleATM = layoutAtm.findViewById(R.id.title_atm);
                TextView tvPinAccount = layoutAtm.findViewById(R.id.pin_account);
                tvTitleATM.setText(childDataBank.getBankName());
                tvPinAccount.setText(childDataWrapper.getNoVa());
                if(childDataWrapper.getFee() == null)
                    tv_fee.setText("");
                else
                    tv_fee.setText(mContext.getString(R.string.listatm_topup_deskripsi_fee,childDataWrapper.getFee()));
            }

            title_child.setText(childDataBank.getProductName());
        }

        v_divider = holder.getView(R.id.divider);

        if (is_expanded && isLastChild){
            v_divider.setVisibility(View.VISIBLE);
        }else
            v_divider.setVisibility(View.GONE);
        return layout_view_child;
    }

    private int getATMLayoutID(ListBankDataTopup childDataBank){
        String bankCode = childDataBank.getBankCode();
        if(bankCode.equals(DefineValue.BankMandiri))
            return R.id.layout_atm_mandiri;
        else if (bankCode.equals(DefineValue.BankBRI))
            return R.id.layout_atm_bri;
        else if (bankCode.equals(DefineValue.BankBCA)) {
            if (childDataBank.getProductCode().equals(DefineValue.PRODUCT_BCA_SIMTOOLKIT))
                return R.id.layout_atm_bca_stk;
            else if (childDataBank.getProductCode().equals(DefineValue.PRODUCT_BCA_KLIKBCAVA))
                return R.id.layout_atm_bca_klikbcava;
            else if (childDataBank.getProductCode().equals(DefineValue.PRODUCT_BCA_MOBILEBANK))
                return R.id.layout_atm_bca_mbca;
        }
        return R.id.layout_atm;
    }


    public void toggleVisible(View v, int group_pos, int child_pos){
        ListBankDataTopup bankDataTopUp = this.mListDataChild.get(this.mListDataHeader.get(group_pos).getHeader()).getBankData().get(child_pos);
        if(bankDataTopUp.getVisible())
            bankDataTopUp.setVisible(false);
        else
            bankDataTopUp.setVisible(true);
        notifyDataSetChanged();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public int getChildTypeCount() {
        return 3;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        if(this.mListDataChild.get(this.mListDataHeader.get(groupPosition).getHeader()).getBankData().
                get(childPosition).getListBankModel().getProduct_type().equalsIgnoreCase(DefineValue.BANKLIST_TYPE_ATM)) {
            if(this.mListDataHeader.get(groupPosition).getHeader().equals(mContext.getString(R.string.other_bank)))
                return OTHER_ATM_VIEW;
            else
                return ATM_VIEW;
        }else
            return NORMAL_VIEW;
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition).getHeader()).getBankData().size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition).getHeader());
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
        return true;
    }
}

