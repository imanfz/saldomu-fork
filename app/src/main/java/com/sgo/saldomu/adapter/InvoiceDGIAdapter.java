package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.fragments.FragListInvoiceTagih;
import com.sgo.saldomu.models.InvoiceDGI;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class InvoiceDGIAdapter extends BaseAdapter implements Filterable {
    private Activity activity;
    private ArrayList<InvoiceDGI> listInvoice = FragListInvoiceTagih.listInovice;
    private ArrayList<InvoiceDGI> listInvoiceDisplay = FragListInvoiceTagih.listInovice;
    public InvoiceDGIAdapter(Activity act) {
        this.activity = act;
    }
//    public int getCount() {
//        // TODO Auto-generated method stub
//        return listInvoiceDisplay.size();
//    }


    @Override
    public int getCount() {
        return listInvoiceDisplay.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_invdgi_list_item, null);
            holder = new ViewHolder();

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        long remain = Long.parseLong(listInvoiceDisplay.get(position).getRemainAmount());
        long hold   = Long.parseLong(listInvoiceDisplay.get(position).getHoldAmount());
        long remain_amount = remain - hold;
        String remain_amount_string  = Long.toString(remain_amount);
        StringTokenizer tokens = new StringTokenizer(remain_amount_string, ".");
        String first = tokens.nextToken();

        holder.txtText    = (TextView) convertView.findViewById(R.id.txtText);
        holder.txtSubText = (TextView) convertView.findViewById(R.id.txtSubText);
        holder.txtSubText2 = (TextView) convertView.findViewById(R.id.txtSubText2);

        holder.txtText.setText("Invoice "+listInvoiceDisplay.get(position).getDocNo());
        holder.txtSubText.setText( "Sisa : " +  CurrencyFormat.format(first));

        String InputAmount = listInvoiceDisplay.get(position).getInputAmount();
        if (InputAmount != null && !InputAmount.equals("") && !InputAmount.equals("null") && Long.parseLong(InputAmount) > 0){
            holder.txtSubText2.setVisibility(View.VISIBLE);
            holder.txtSubText2.setText( "Bayar : " +  CurrencyFormat.format(InputAmount));
        }else{
            holder.txtSubText2.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                Log.d("adapter","dgiadapter result publish =="+results.values+" constraint=="+constraint);
                listInvoiceDisplay = (ArrayList<InvoiceDGI>) results.values; // has the filtered values
                FragListInvoiceTagih.listInovice = listInvoiceDisplay;
                notifyDataSetChanged();  // notifies the data with new filtered values
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<InvoiceDGI> FilteredArrList = new ArrayList<InvoiceDGI>();

                if (listInvoice == null) {
                    Log.d("adapter","dgiadapter list item null...");
                    listInvoice = new ArrayList<>(); // saves the original data in mOriginalValues

                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {
                    Log.d("adapter","dgiadapter constraint null...");
                    // set the Original result to return
                    results.count = listInvoice.size();
                    results.values = listInvoice;
                } else if(constraint != null && constraint.length() !=0){
                    Log.d("adapter","dgiadapter constraint ...");
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < listInvoice.size(); i++) {
                        String data = "Invoice "+listInvoice.get(i).getDocNo();
                        if (data.toLowerCase().contains(constraint)) {
                            Log.d("adapter","dgiadapter constraint ..."+constraint.toString()+" data="+data);
                            FilteredArrList.add(new InvoiceDGI(
                                    listInvoice.get(i).getDocNo(),
                                    listInvoice.get(i).getDocId(),
                                    listInvoice.get(i).getDocDesc(),
                                    listInvoice.get(i).getAmount(),
                                    listInvoice.get(i).getRemainAmount(),
                                    listInvoice.get(i).getHoldAmount(),
                                    listInvoice.get(i).getCcy(),
                                    listInvoice.get(i).getDueDate(),
                                    listInvoice.get(i).getInputAmount(),
                                    listInvoice.get(i).getSessionId()
                                    ));
//                            int sisa = Integer.valueOf(listInvoice.get(i).getRemainAmount()) - Integer.valueOf(listInvoice.get(i).getHoldAmount());
//                            Log.d("adapter","dgiadapter memberid:"+listInvoice.get(i).getDocNo()+" sisa:"+sisa);
                        }
                    }
                    // set the Filtered result to return

                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;

                    Log.d("adapter","dgiadapter results size..."+results.count);
                }
                return results;
            }
        };
        return filter;
    }

    static class ViewHolder {
        TextView txtText, txtSubText, txtSubText2;
    }
}
