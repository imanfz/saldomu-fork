package com.sgo.saldomu.services.jobs;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.receivers.LocalResultReceiver;

/**
 * Created by yuddistirakiki on 8/25/17.
 */

public class JobUpdateBBSData extends JobService {

    static public final String TAG = "JobUpdateBBSData";

    @Override
    public boolean onStartJob(final JobParameters job) {
        BBSDataManager bbsDataManager = new BBSDataManager();
        bbsDataManager.runServiceUpdateData(this,
                LocalResultReceiver.getSimpleInstance(() -> jobFinished(job, false)));
        //return true karena job yang kita jalaninnya harus jalanin intent service
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
