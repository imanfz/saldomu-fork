package com.sgo.saldomu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.sgo.saldomu.Beans.TopupATMObject;
import com.sgo.saldomu.R;

import java.util.List;

/**
 * Created by thinkpad on 6/4/2015.
 */
public class TopupATMAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<TopupATMObject> listAtm;
    private Context context;
    private String max_topup_value;

    public TopupATMAdapter(Context _context, List<TopupATMObject> _listAtm, String _max_topup_value) {
        context = _context;
        mInflater = LayoutInflater.from(context);
        listAtm = _listAtm;
        max_topup_value = _max_topup_value;
    }

    @Override
    public int getCount() {
        return listAtm.size();
    }

    @Override
    public Object getItem(int position) {
        return listAtm.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.list_topup_atm_item, parent, false);
            holder = new ViewHolder();
            holder.titleAtm = (TextView)view.findViewById(R.id.title_atm);
            holder.atmBank = (TextView)view.findViewById(R.id.text_list_atm_item_bank);
            holder.pinAccount = (TextView)view.findViewById(R.id.pin_account);
            holder.otherInputCode = (TextView)view.findViewById(R.id.other_input_code);
            holder.imageAtm = (ImageView)view.findViewById(R.id.img_atm);
            holder.maxTopup = (TextView)view.findViewById(R.id.max_topup);
            holder.maxTopupOther = (TextView)view.findViewById(R.id.max_topup_other);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        holder.titleAtm.setText(listAtm.get(position).getBank_name());
        holder.atmBank.setText(context.getResources().getString(R.string.listatm_topup_ins_2) + " " + listAtm.get(position).getBank_name());
        holder.pinAccount.setText(listAtm.get(position).getNo_va());
        holder.otherInputCode.setText(listAtm.get(position).getBank_code() + " + " + listAtm.get(position).getNo_va());
        holder.imageAtm.setImageResource(R.drawable.amt_other);
        holder.maxTopup.setText("*" + context.getResources().getString(R.string.max_topup_allowed) + " : " + max_topup_value);
        holder.maxTopupOther.setText("*" + context.getResources().getString(R.string.max_topup_allowed) + " : " + max_topup_value);

        return view;
    }

    private class ViewHolder {
        public TextView titleAtm, pinAccount, otherInputCode, atmBank, maxTopup, maxTopupOther;
        public ImageView imageAtm;
    }
}
