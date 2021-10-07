package com.sgo.saldomu.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OpenCloseDatePickerFragment extends DialogFragment {
    public static final String TAG = "OpenCloseDatePicker";

    private OpenCloseDatePickerListener dpl;
    ArrayList<String> selectedDates = new ArrayList<>();
    ArrayList<Date> listDates = new ArrayList<>();
    Button btnYes, btnNo;
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

    public interface OpenCloseDatePickerListener {
        void onOkDatePickerClick(ArrayList<String> selectedDates, ArrayList<Date> listDates);

        void onCancelDatePickerClick();
    }

    public OpenCloseDatePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.selectedDates = (ArrayList<String>) args.getSerializable("selectDates");
            this.listDates = (ArrayList<Date>) args.getSerializable("listDates");
        }

        try {
            dpl = (OpenCloseDatePickerFragment.OpenCloseDatePickerListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement OpenCloseDatePickerListener interface");
        }
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.choose_date));

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_open_close_date_picker, container, false);

        final CalendarPickerView calendar_view = (CalendarPickerView) v.findViewById(R.id.calendar_view);

        btnYes = (Button) v.findViewById(R.id.btnYes);
        btnNo = (Button) v.findViewById(R.id.btnNo);


        //getting current
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.MONTH, 2);
        Date today = new Date();

        //add one year to calendar from todays date

        if (selectedDates.size() > 0) {
            ArrayList<Date> tempDates = new ArrayList<>();
            for (int j = 0; j < selectedDates.size(); j++) {
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
                selectedDates.add(fDate);

                try {
                    listDates.add(sf.parse(fDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDateUnselected(Date date) {
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
                String tempDate;

                listDates = new ArrayList<Date>();

                for (int i = 0; i < selectedDates.size(); i++) {
                    tempDate = selectedDates.get(i);

                    if (fDate.equals(tempDate)) {
                        selectedDates.remove(selectedDates.get(i));
                    } else {
                        try {
                            listDates.add(sf.parse(tempDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        btnYes.setOnClickListener(
                v12 -> {
                    dpl.onOkDatePickerClick(selectedDates, listDates);
                    getDialog().dismiss();
                }
        );

        btnNo.setOnClickListener(
                v1 -> {
                    dpl.onCancelDatePickerClick();
                    getDialog().dismiss();
                }
        );

        return v;
    }
}
