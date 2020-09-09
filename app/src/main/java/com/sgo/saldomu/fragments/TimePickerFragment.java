package com.sgo.saldomu.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.sgo.saldomu.R;

public class TimePickerFragment extends DialogFragment {

    private TimePickerListener tpl;

    public interface TimePickerListener {
        public void onOkTimePickerClick(int position, String startTime, String endTime, int iStartHour, int iStartMinute, int iEndHour, int iEndMinute);
        public void onCancelTimePickerClick(int position);
    }

    //add a custom constructor so that you have an initialised NoticeDialogListener
    public TimePickerFragment(){
        super();
    }
    //TimePickerListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //remove the check that verfis if your activity has the DialogListener Attached because you want to attach it into your list view onClick()
    }

    public static final String TAG = "TimePicker";
    private int position = 0;
    private TimePicker tpStartHour, tpEndHour;
    TextView etStartHour, etEndHour;
    Button btnYes, btnNo;
    String startTime = "", endTime = "", NamaHari = "";
    int iStartHour = 0, iStartMinute = 0, iEndHour = 0, iEndMinute = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            tpl = (TimePickerFragment.TimePickerListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement TimePickerListener interface");
        }
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //tpStartHour     = (TimePicker) getActivity().findViewById(R.id.tpStartHour);
        //tpEndHour       = (TimePicker) getActivity().findViewById(R.id.tpEndHour);

        //tpStartHour.setIs24HourView(true);
        //tpEndHour.setIs24HourView(true);

        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.custom_range_time_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etStartHour     = (TextView) view.findViewById(R.id.etStartHour);
        etEndHour       = (TextView) view.findViewById(R.id.etEndHour);
        btnYes          = (Button) view.findViewById(R.id.btnYes);
        btnNo           = (Button) view.findViewById(R.id.btnNo);

        if ( !startTime.isEmpty() ) {
            etStartHour.setText(startTime);
        }

        if ( !endTime.isEmpty() ) {
            etEndHour.setText(endTime);
        }

        etStartHour.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {

                    int defStartHour    = 0;
                    int defStartMinute  = 0;
                    if ( !startTime.isEmpty() ) {
                        defStartHour        = iStartHour;
                        defStartMinute      = iStartMinute;
                    }

                    // Perform action on click
                    TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                            iStartHour      = hourOfDay;
                            iStartMinute    = minute;

                            String hourData = String.valueOf(hourOfDay);
                            if ( hourData.length() == 1 ) {
                                hourData = "0" + hourData;
                            }

                            String minuteData = String.valueOf(minute);
                            if ( minuteData.length() == 1 ) {
                                minuteData = "0" + minuteData;
                            }

                            startTime   = hourData + ":" + minuteData;
                            etStartHour.setText(startTime);
                        }
                    }, defStartHour, defStartMinute, true);
                    timePickerDialog.show();
                }
            }
        );

        etEndHour.setOnClickListener(
                new View.OnClickListener() {



                    public void onClick(View v) {

                        int defEndHour    = 0;
                        int defEndMinute  = 0;
                        if ( !endTime.isEmpty() ) {
                            defEndHour        = iEndHour;
                            defEndMinute      = iEndMinute;
                        }

                        // Perform action on click
                        TimePickerDialog timePickerDialog2 = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


                                iEndHour      = hourOfDay;
                                iEndMinute    = minute;

                                String hourData = String.valueOf(hourOfDay);
                                if ( hourData.length() == 1 ) {
                                    hourData = "0" + hourData;
                                }

                                String minuteData = String.valueOf(minute);
                                if ( minuteData.length() == 1 ) {
                                    minuteData = "0" + minuteData;
                                }

                                endTime = hourData + ":" + minuteData;
                                etEndHour.setText(endTime);
                            }
                        }, defEndHour, defEndMinute, true);
                        timePickerDialog2.show();
                    }
                }
        );

        btnYes.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        int iStartTime  = iStartHour + iStartMinute;
                        int iEndTime    = iEndHour + iEndMinute;

                        if ( iStartTime >= iEndTime ) {
                            androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setTitle(getString(R.string.alertbox_title_information));
                            alertDialog.setCancelable(false);

                            alertDialog.setMessage(getString(R.string.err_start_time_must_lower_than_end_time));



                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();
                                        }
                                    });

                            alertDialog.show();
                        } else {
                            try {
                                tpl.onOkTimePickerClick(position, startTime, endTime, iStartHour, iStartMinute, iEndHour, iEndMinute);
                                getDialog().dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        btnNo.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        tpl.onCancelTimePickerClick(position);
                        getDialog().dismiss();
                    }
                }
        );
    }


    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        position        = getArguments().getInt("position");
        NamaHari        = getArguments().getString("NamaHari");
        iStartHour      = getArguments().getInt("iStartHour");
        iStartMinute    = getArguments().getInt("iStartMinute");
        iEndHour        = getArguments().getInt("iEndHour");
        iEndMinute      = getArguments().getInt("iEndMinute");
        startTime       = getArguments().getString("startHour");
        endTime         = getArguments().getString("endHour");

        Dialog dialog   = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.title_setup_shop_hour) + " - " + NamaHari);



        // Use the Builder class for convenient dialog construction


        return dialog;
    }
}