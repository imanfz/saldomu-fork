package com.sgo.saldomu.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.common.StringUtil;
import com.sgo.saldomu.Beans.HelpModel;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
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
    private Activity activity;


    public HelpAdapter(Context context, ArrayList<HelpModel> _data, Activity activity) {
        mInflater = LayoutInflater.from(context);
        this.data = _data;
        this.context = context;
        this.activity = activity;
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
            holder.name = (TextView) view.findViewById(R.id.help_name_value);
            holder.phone = (TextView) view.findViewById(R.id.help_phone_value);
            holder.mail = (TextView) view.findViewById(R.id.help_mail_value);
            holder.whatsapp = view.findViewById(R.id.help_phone_whatsapp_value);

            holder.trPhone = (TableRow) view.findViewById(R.id.tr_phone);
            holder.trMail = (TableRow) view.findViewById(R.id.tr_mail);
            holder.trWhatsapp = (TableRow) view.findViewById(R.id.tr_whatsapp);

            view.setTag(holder);

            holder.phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+ holder.phone.getText().toString()));
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions((Activity)activity, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    }
                    else {
                        context.startActivity(callIntent);
                       }

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
        String mail = data.get(position).getMail();
        String whatsappNo = data.get(position).getWhatsappPhone();

        if(phone.equals("")) {
            holder.trPhone.setVisibility(View.GONE);
        }
        else {
            holder.trPhone.setVisibility(View.VISIBLE);
            holder.phone.setText(phone);
        }

        if(mail.equals("")) {
            holder.trMail.setVisibility(View.GONE);
        }
        else {
            holder.trMail.setVisibility(View.VISIBLE);
            holder.mail.setText(mail);
        }

        if(whatsappNo.equals("")){
            holder.trWhatsapp.setVisibility(View.GONE);
        }else{
            holder.trWhatsapp.setVisibility(View.VISIBLE);
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

    private class ViewHolder {
        public TextView name, phone, mail, whatsapp;
        public TableRow trPhone, trMail, trWhatsapp;
    }
}
