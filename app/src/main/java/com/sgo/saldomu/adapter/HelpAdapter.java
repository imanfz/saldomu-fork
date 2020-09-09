package com.sgo.saldomu.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.sgo.saldomu.Beans.HelpModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.NoHPFormat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by thinkpad on 6/9/2015.
 */
public class HelpAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<HelpModel> data;
    private Context context;


    public HelpAdapter(Context context, ArrayList<HelpModel> _data, Activity activity) {
        mInflater = LayoutInflater.from(context);
        this.data = _data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        final ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_help_center_item, parent, false);
            holder = new ViewHolder();
            holder.name = view.findViewById(R.id.help_name_value);
            holder.phone = view.findViewById(R.id.help_phone_value);
            holder.whatsapp = view.findViewById(R.id.help_phone_whatsapp_value);

            holder.phone_card_view = view.findViewById(R.id.phone_card_view);
            holder.whatsup_card_view = view.findViewById(R.id.whatsup_card_view);
            holder.tvCopy = view.findViewById(R.id.tv_copy);

            view.setTag(holder);

            holder.phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+ holder.phone.getText().toString()));
                    context.startActivity(callIntent);
                }
            });

            holder.tvCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    copyRefNo(data.get(position).getPhone());
                }
            });

            holder.whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirectToWhatsapp(data.get(position).getWhatsappPhone());
                }
            });
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }
        holder.name.setText(data.get(position).getName());

        String phone = data.get(position).getPhone();
        String whatsappNo = data.get(position).getWhatsappPhone();

        if(phone.equals("")) {
            holder.phone_card_view.setVisibility(View.GONE);
        }
        else {
            holder.phone_card_view.setVisibility(View.VISIBLE);
            holder.phone.setText(phone);
        }

        if(whatsappNo.equals("")){
            holder.whatsup_card_view.setVisibility(View.GONE);
        }else{
            holder.whatsup_card_view.setVisibility(View.VISIBLE);
            holder.whatsapp.setText(whatsappNo);
        }
        return view;
    }


    private void redirectToWhatsapp(String phoneNo){
        PackageManager pm= context.getPackageManager();
        try {

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);

            String helpMsg = context.getString(R.string.lbl_need_help);

            String encodedMsg = URLEncoder.encode(helpMsg, "utf-8");


            String redirect
                    = "https://api.whatsapp.com/send?phone="+ NoHPFormat.formatTo62(phoneNo)
                    +"&text="+ encodedMsg;

            Timber.wtf("Isi redirect msg whatsapp..."+redirect);

            Uri uri = Uri.parse(redirect);
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            i.setPackage("com.whatsapp");
            context.startActivity(Intent.createChooser(i, ""));


        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Timber.d("Failedd to encode whatsapp message....");
        }

    }

    private void copyRefNo(String text){
        Toast.makeText(context,"Copy to clipboard",Toast.LENGTH_SHORT).show();
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("asd", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    private class ViewHolder {
        public TextView name, phone, mail, whatsapp, tvCopy;
        public CardView phone_card_view, whatsup_card_view;
    }
}
