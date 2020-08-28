package com.sgo.saldomu.dialogs;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sgo.saldomu.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

public class NextVisitDateDialog extends DialogFragment {
    ActionListener listener;
    private DateFormat fromFormat;
    private DateFormat toFormat;
    private String date_visit;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;
    TextView tv_nextVisit;
    Button submitButton;

    public interface ActionListener {
        void onClick(NextVisitDateDialog dialog, String date);
    }

    public static NextVisitDateDialog newDialog(ActionListener listener) {
        NextVisitDateDialog dialog = new NextVisitDateDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_next_visit_date, container);
        tv_nextVisit = v.findViewById(R.id.tv_nextVisit);
        submitButton = v.findViewById(R.id.submitButton);

        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        toFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID", "INDONESIA"));
        toFormat = new SimpleDateFormat("dd-M-yyyy", new Locale("ID", "INDONESIA"));

        Calendar c = Calendar.getInstance();
        String dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:%s", dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        tv_nextVisit.setOnClickListener(view -> {
            if (getFragmentManager() != null) {
                dpd.show(getFragmentManager(), "asd");
            }
        });
        submitButton.setOnClickListener(view -> {
            if (tv_nextVisit.getText().toString().equals(Objects.requireNonNull(getActivity()).getString(R.string.choose_date))){
                Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.please_choose_date), Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onClick(this, date_visit);
        });

        return v;
    }

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            String dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker dob");
            try {
                date_visit = fromFormat.format(Objects.requireNonNull(toFormat.parse(dedate)));
                Timber.d("masuk date picker dob masuk tanggal : %s", date_visit);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_nextVisit.setText(dedate);
        }
    };
}
