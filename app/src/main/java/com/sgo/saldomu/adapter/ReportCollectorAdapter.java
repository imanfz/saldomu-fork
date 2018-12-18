package com.sgo.saldomu.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.sgo.saldomu.Beans.ReportListEspayModel;
import com.sgo.saldomu.models.ReportlistCollectorModel;

public class ReportCollectorAdapter  extends ArrayAdapter<ReportlistCollectorModel> {
    public ReportCollectorAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
}
