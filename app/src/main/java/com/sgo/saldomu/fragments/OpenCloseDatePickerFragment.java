package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sgo.saldomu.R;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OpenCloseDatePickerFragment.OpenCloseDatePickerListener} interface
 * to handle interaction events.
 * Use the {@link OpenCloseDatePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OpenCloseDatePickerFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "OpenCloseDatePicker";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OpenCloseDatePickerListener dpl;
    ArrayList<String> selectedDates = new ArrayList<>();
    ArrayList<Date> listDates = new ArrayList<>();
    Button btnYes, btnNo;
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

    public interface OpenCloseDatePickerListener {
        public void onOkDatePickerClick(ArrayList<String> selectedDates, ArrayList<Date> listDates);
        public void onCancelDatePickerClick();
    }

    public OpenCloseDatePickerFragment() {
        // Required empty public constructor
    }

    public OpenCloseDatePickerFragment(OpenCloseDatePickerListener dpl, ArrayList<String> selectedDates, ArrayList<Date> listDates){
        super();
        this.dpl = dpl;
        this.selectedDates = selectedDates;
        this.listDates = listDates;
    }
    OpenCloseDatePickerListener mListener;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OpenCloseDatePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OpenCloseDatePickerFragment newInstance(String param1, String param2) {
        OpenCloseDatePickerFragment fragment = new OpenCloseDatePickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog   = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.choose_date));

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_open_close_date_picker, container, false);

        final CalendarPickerView calendar_view = (CalendarPickerView) v.findViewById(R.id.calendar_view);

        btnYes          = (Button) v.findViewById(R.id.btnYes);
        btnNo           = (Button) v.findViewById(R.id.btnNo);


        //getting current
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.MONTH, 2);
        Date today = new Date();

        //add one year to calendar from todays date

        if ( selectedDates.size() > 0 ) {
            ArrayList<Date> tempDates = new ArrayList<>();
            for(int j = 0; j < selectedDates.size(); j++) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date d = dateFormat.parse(selectedDates.get(j));
                    tempDates.add(d);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            calendar_view.init(today, nextYear.getTime()).inMode(CalendarPickerView.SelectionMode.MULTIPLE).withSelectedDates(tempDates);
        } else {
            calendar_view.init(today, nextYear.getTime()).inMode(CalendarPickerView.SelectionMode.MULTIPLE);
        }

        //action while clicking on a date
        calendar_view.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
                selectedDates.add(fDate.toString());

                try {
                    listDates.add(sf.parse(fDate.toString()));
                } catch(ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDateUnselected(Date date) {
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
                String tempDate;

                listDates   = new ArrayList<Date>();

                for(int i = 0; i < selectedDates.size(); i++) {
                    tempDate = selectedDates.get(i);

                    if ( fDate.equals(tempDate) ) {
                        selectedDates.remove(selectedDates.get(i));
                    } else {
                        try {
                            listDates.add(sf.parse(tempDate));
                        } catch(ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        btnYes.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        dpl.onOkDatePickerClick(selectedDates, listDates);
                        getDialog().dismiss();
                    }

                }
        );

        btnNo.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        dpl.onCancelDatePickerClick();
                        getDialog().dismiss();
                    }

                }
        );

        return v;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
