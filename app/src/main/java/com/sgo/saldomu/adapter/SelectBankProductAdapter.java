package com.sgo.saldomu.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.Beans.listBankModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;

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

        ViewHolder addView(View view) {
            int id = view.getId();
            storedViews.put(id, view);
            return this;
        }

        public View getView(int id) {
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
            v = infalInflater.inflate(R.layout.list_topup_child_item, parent, false);
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

        if (isExpanded) {
            indicator.setImageResource(R.drawable.child_indicator_small_expand);
        } else

            indicator.setImageResource(R.drawable.child_indicator_small_collapse);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        listBankModel childDataWrapper = (listBankModel) getChild(groupPosition, childPosition);
        View layout_view_child = convertView;

//        if (layout_view_child == null && !childDataWrapper.getBank_code().equalsIgnoreCase(DefineValue.BankMandiri)) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout_view_child = inflater.inflate(getATMLayout(childDataWrapper), parent, false);

        TextView tvTitleATM = layout_view_child.findViewById(R.id.title_atm);
        TextView tvPinAccount = layout_view_child.findViewById(R.id.pin_account);
        ImageView ivCopyButton = layout_view_child.findViewById(R.id.iv_copyButton);
        tvTitleATM.setText(childDataWrapper.getBank_name());
        tvPinAccount.setText(childDataWrapper.getNoVA());

        ivCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copy to Clip Board", tvPinAccount.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "Coppied to ClipBoard", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tv_fee = layout_view_child.findViewById(R.id.fee_deskripsi);
        if (tv_fee != null) {
            if (childDataWrapper.getFee() == null)
                tv_fee.setText("");
            else
                tv_fee.setText(mContext.getString(R.string.listatm_topup_deskripsi_fee, childDataWrapper.getFee()));
        }
//        }

        return layout_view_child;
    }

    private int getATMLayout(listBankModel bankData) {
        String bankCode = bankData.getBank_code();
        if (bankCode.equals(DefineValue.BankMandiri)) {
            String bankName = bankData.getProduct_type();
            if (bankName.equalsIgnoreCase("MANDIRI_ATM")) {
                return R.layout.list_topup_mandiri_online;
            } else
                return R.layout.list_topup_atm_mandiri_item;
        }
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
        else if (bankCode.equals(DefineValue.BankCIMB))
            return R.layout.list_topup_atm_cimb_item;
        else if (bankCode.equals(DefineValue.BankBNI))
            return R.layout.list_topup_atm_bni_item;
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
        if (mListDataHeader.get(groupPosition).getProduct_type().equals("ATM") ||
                mListDataHeader.get(groupPosition).getProduct_type().equals("MANDIRI_ATM"))
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
        String product_code = this.mListDataHeader.get(groupPosition).getProduct_code();
        return this.mListDataChild.get(product_code);
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

