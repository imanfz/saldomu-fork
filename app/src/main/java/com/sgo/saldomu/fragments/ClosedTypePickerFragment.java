package com.sgo.saldomu.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.sgo.saldomu.models.SetupOpenHour;

import java.util.ArrayList;


public class ClosedTypePickerFragment extends DialogFragment {

    public static final String TAG = "ClosedTypePicker";
    private int position = 0, countChecked = 0;
    private String closedType = "";
    Button btnYes, btnNo;
    ArrayList<String> selectedDays;
    ArrayList<String> selectedDate;
    SetupOpenHour setupOpenHour;
    LinearLayout llPicker;

    private ClosedTypePickerFragment.ClosedTypePickerListener cpl;

    public interface ClosedTypePickerListener {
        public void onOkClosedTypePickerClick(int position, ArrayList<String> selectedDays, ArrayList<String> selectedDate);
        public void onCancelClosedTypePickerClick(int position);
    }

    //add a custom constructor so that you have an initialised NoticeDialogListener
    public ClosedTypePickerFragment(ClosedTypePickerFragment.ClosedTypePickerListener cpl){
        super();
        this.cpl = cpl;
    }
    ClosedTypePickerFragment.ClosedTypePickerListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //remove the check that verfis if your activity has the DialogListener Attached because you want to attach it into your list view onClick()
    }

    public ClosedTypePickerFragment() {
        super();
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
        return inflater.inflate(R.layout.frag_closed_type_picker, container, false);
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.

        position        = getArguments().getInt("position");
        closedType      = getArguments().getString("closedType");
        setupOpenHour   = new SetupOpenHour();
        selectedDate    = getArguments().getStringArrayList("selectedDate");
        selectedDays    = getArguments().getStringArrayList("selectedDays");

        Dialog dialog   = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.closed_shop_per) + " - " + closedType);



        // Use the Builder class for convenient dialog construction


        return dialog;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnYes          = (Button) view.findViewById(R.id.btnYes);
        btnNo           = (Button) view.findViewById(R.id.btnNo);
        llPicker        = (LinearLayout) view.findViewById(R.id.llPicker);

        if ( position == 1 )
        {
            ArrayList<String> dataWeekDays = setupOpenHour.getWeekDays();
            for(int i =0; i < dataWeekDays.size(); i++) {
                CheckBox cb = new CheckBox(getActivity());
                cb.setText(dataWeekDays.get(i));
                cb.setId(i);

                if (selectedDays.size() > 0) {
                    if ( selectedDays.contains(String.valueOf(i)) ) {
                        cb.setChecked(true);
                    }
                }

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        if ( isChecked ) {
                            selectedDays.add(String.valueOf(buttonView.getId()));
                        } else {
                            selectedDays.remove(String.valueOf(buttonView.getId()));
                        }


                    }
                });

                llPicker.addView(cb);
            }
        }
        else if ( position == 2 ) {
            ArrayList<Integer> dataWeekDates = setupOpenHour.getDays();
            /*for (int i = 0; i < dataWeekDates.size(); i++) {
                CheckBox cb = new CheckBox(getActivity());

                int idxHari = dataWeekDates.get(i) + 1;
                cb.setText(String.valueOf(idxHari));
                cb.setId(i);
                llPicker.addView(cb);

            }*/


            TableLayout tl = new TableLayout(view.getContext());
            int offset_in_column=0;/*, table_size=*the size of your answer from the server*/;
            TableRow tr=null;
            for (int offset_in_table=0; offset_in_table < dataWeekDates.size(); offset_in_table++) {
                /* maybe you want to do something special with the data from the server here ? */

                if (offset_in_column == 0) {
                    tr = new TableRow(getActivity());
                    tr.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                final CheckBox check = new CheckBox(getActivity());

                int idxHari = dataWeekDates.get(offset_in_table) + 1;
                check.setText(String.valueOf(idxHari));
                check.setId(offset_in_table);

                if (selectedDate.size() > 0) {
                    /*int data = Integer.valueOf(selectedDate.get(offset_in_table));

                    if ( data == offset_in_table ) {
                        check.setChecked(true);
                    }*/

                    if ( selectedDate.contains(String.valueOf(offset_in_table)) ) {
                        check.setChecked(true);
                    }
                }

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        if ( isChecked ) {

                            selectedDate.add(String.valueOf(buttonView.getId()));

                        } else {
                            selectedDate.remove(String.valueOf(buttonView.getId()));
                        }


                    }
                });
                check.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                tr.addView(check);

                offset_in_column++;
                if (offset_in_column == 4) {

                    tl.addView(tr);
                    offset_in_column = 0;
                }
            }

            if ( offset_in_column != 0 ) {


                CheckBox check = new CheckBox(getActivity());

                int idxHari2 = 100;
                check.setText(String.valueOf(idxHari2));
                check.setVisibility(View.INVISIBLE);
                check.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                tr.addView(check);

                tl.addView(tr);
            }

            llPicker.addView(tl);
        }

        btnYes.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {


                        if ( selectedDays.size() == 0 && selectedDate.size() == 0 ){
                            Toast.makeText(getActivity(), R.string.err_empty_pick, Toast.LENGTH_LONG).show();
                        } else {
                            cpl.onOkClosedTypePickerClick(position, selectedDays, selectedDate);
                            getDialog().dismiss();
                        }



                    }
                }
        );

        btnNo.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        cpl.onCancelClosedTypePickerClick(position);
                        getDialog().dismiss();
                    }
                }
        );
    }

}
