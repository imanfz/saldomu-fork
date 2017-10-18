package com.sgo.saldomu.adapter;

import android.app.FragmentManager;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sgo.saldomu.Beans.ListMyProfile_model;
import com.sgo.saldomu.R;
import com.sgo.saldomu.interfaces.OnDateChooseListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Lenovo Thinkpad on 9/27/2017.
 */

public class ExpandableListProfile extends BaseExpandableListAdapter {

    private FragmentManager fragmentManager;
    private Context mContext;
    private List<String> mListDataHeader;
    private HashMap<String, List<ListMyProfile_model>> mListDataChild;
    private final String DOB = "tagDOB";
    private Calendar date ;
    viewHolder Holder;

    onClick listener;
    OnDateChooseListener onDateChooseListener;

    public interface onClick{
        void onTextChange(String message, int choice);
        void DateChooseListener(String date);
        void NextListener ();
        void setImageCameraKTP();
        void setImageSelfieKTP();
        void setImageCameraTTD();
    }

    public ExpandableListProfile(Context mContext, List<String> mListDataHeader,
                                 HashMap<String,List<ListMyProfile_model>> mListDataChild, onClick listener,
                                 FragmentManager fragmentManager, Calendar date)
    {
        this.mContext = mContext;
        this.mListDataHeader = mListDataHeader;
        this.mListDataChild = mListDataChild;
        this.listener = listener;
        this.fragmentManager = fragmentManager;
        this.date = date;
    }

    @Override
    public int getGroupCount() {
        return mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mListDataChild.get(mListDataHeader.get(groupPosition)).size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mListDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mListDataChild.get(mListDataHeader.get(groupPosition));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = convertView;
        groupHolder holder;
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (v == null){
            holder = new groupHolder();
            v = infalInflater.inflate(R.layout.list_profile_group, parent, false);
            holder.title = (TextView) v.findViewById(R.id.group_title);
            v.setTag(holder);
        }else {
            holder = (groupHolder) v.getTag();
        }

        holder.title.setText(mListDataHeader.get(groupPosition));
        return v;
    }



    @Override
    public int getChildType(int groupPosition, int childPosition) {
        List<ListMyProfile_model> lists = (List<ListMyProfile_model>)  getChild(groupPosition, childPosition);
        ListMyProfile_model obj= lists.get(0);
        if (obj.getMemberBasic())
        {
            return 1;
        }
        else return 2;
    }

    @Override
    public int getChildTypeCount() {
        return 3;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final viewHolder Holder;
        if (v==null) {
            Holder = new viewHolder();
            switch (getChildType(groupPosition, childPosition)) {
                case 1:
                    v = infalInflater.inflate(R.layout.list_profile_child_item, parent, false);
                    Holder.noHP = (EditText) v.findViewById(R.id.myprofile_value_hp);
                    Holder.noHP.setEnabled(false);
                    Holder.nama = (EditText) v.findViewById(R.id.myprofile_value_name);
                    Holder.email = (EditText) v.findViewById(R.id.myprofile_value_email);
                    Holder.email.setEnabled(false);
                    Holder.dob = (TextView) v.findViewById(R.id.myprofile_value_dob);
                    Holder.button1 = (Button) v.findViewById(R.id.button1);
                    Holder.dob.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                                    dobPickerSetListener,
                                    date.get(Calendar.YEAR),
                                    date.get(Calendar.MONTH),
                                    date.get(Calendar.DAY_OF_MONTH)
                            );

                            dpd.show(fragmentManager, DOB);
                        }
                    });

                    Holder.button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(Validation())
                                listener.NextListener();
                        }
                    });
                    break;
                case 2:
                    v = infalInflater.inflate(R.layout.list_verified_member_item, parent, false);
                    Holder.cameraKTP = (ImageButton) v.findViewById(R.id.camera_ktp_paspor);
                    Holder.cameraSelfieKTP = (ImageButton) v.findViewById(R.id.camera_selfie_ktp_paspor);
                    Holder.cameraTTD = (ImageButton) v.findViewById(R.id.camera_ttd);
                    Holder.button2 = (Button) v.findViewById(R.id.button2);

                    Holder.cameraKTP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Timber.d("Masuk ke onclick kamera ktp");
                            listener.setImageCameraKTP();
                        }
                    });

                    Holder.cameraSelfieKTP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Timber.d("Masuk ke onclick selfie ktp");
                            listener.setImageSelfieKTP();
                        }
                    });

                    Holder.cameraTTD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Timber.d("Masuk ke onclick camera ttd");
                            listener.setImageCameraTTD();
                        }
                    });
            }
            assert v != null;
            v.setTag(Holder);
        }else
            Holder = (viewHolder) v.getTag();

        switch (getChildType(groupPosition, childPosition)) {
            case 1:
                Holder.noHP.setText(mListDataChild.get(mListDataHeader.get(groupPosition)).get(childPosition).getNoHP());
                Holder.nama.setText(mListDataChild.get(mListDataHeader.get(groupPosition)).get(childPosition).getNama());
                Holder.email.setText(mListDataChild.get(mListDataHeader.get(groupPosition)).get(childPosition).getEmail());
                Holder.dob.setText(mListDataChild.get(mListDataHeader.get(groupPosition)).get(childPosition).getDob());

                Holder.noHP.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        listener.onTextChange(s.toString(), 1);
                    }
                });

                Holder.nama.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        listener.onTextChange(s.toString(), 2);
                    }
                });

                Holder.email.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        listener.onTextChange(s.toString(), 3);
                    }
                });

                Holder.dob.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        listener.onTextChange(s.toString(), 4);
                    }
                });
                break;

            case 2:
                break;
        }
        return v;
    }

    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener dobPickerSetListener = new com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
//            String dedate;

//            if(view.getTag().equals(DOB)){
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",new Locale("id","INDONESIA"));
                Holder.dob.setText(format.format(cal.getTime()));
            listener.DateChooseListener(format.format(cal.getTime()));
//            }
        }
    };

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class groupHolder {
        TextView title;
    }
    class viewHolder {
        EditText noHP;
        EditText nama;
        EditText email;
        TextView dob;
        Button button1;
        ImageButton cameraKTP;
        ImageButton cameraSelfieKTP;
        ImageButton cameraTTD;
        Button button2;

    }

    public Boolean Validation()
    {
//        int compare = 100;
//        if(Holder.dob != null) {
//            Date dob = null;
//            Date now = null;
//            try {
//                dob = fromFormat.parse(date_dob);
//                now = fromFormat.parse(dateNow);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//
//            if (dob != null) {
//                if (now != null) {
//                    compare = dob.compareTo(now);
//                }
//            }
//            Timber.d("compare date:"+ Integer.toString(compare));
//        }
        if (Holder.noHP.getText().toString().equals(""))
        {
            Holder.noHP.setError("Nomor HP tidak boleh kosong!");
            Holder.noHP.requestFocus();
            return false;
        }
        if (Holder.nama.getText().toString().equals(""))
        {
            Holder.nama.setError("Nama tidak boleh kosong!");
            Holder.nama.requestFocus();
            return false;
        }
        if (Holder.email.getText().toString().equals(""))
        {
            Holder.email.setError("Email tidak boleh kosong!");
            Holder.email.requestFocus();
            return false;
        }
        if (Holder.dob.getText().toString().equals(""))
        {
            Holder.dob.setError("Pilih tanggal lahir!");
            Holder.dob.requestFocus();
            return false;
        }

        return true;
    }

    public void setImageCameraKTP(File file)
    {
        if(Holder.cameraKTP!=null && file!=null)
        {
            Timber.d("Filepath"+file.getPath());
            Picasso.with(mContext).load(file).centerCrop().fit().into(Holder.cameraKTP);
        }
    }
    public void setImageSelfieKTP(File file)
    {
        if(Holder.cameraSelfieKTP!=null && file!=null)
        {
            Timber.d("Filepath"+file.getPath());
            Picasso.with(mContext).load(file).centerCrop().fit().into(Holder.cameraSelfieKTP);
        }
    }
    public void setImageCameraTTD(File file)
    {
        if(Holder.cameraTTD!=null && file!=null)
        {
            Timber.d("Filepath"+file.getPath());
            Picasso.with(mContext).load(file).centerCrop().fit().into(Holder.cameraTTD);
        }
    }
}
