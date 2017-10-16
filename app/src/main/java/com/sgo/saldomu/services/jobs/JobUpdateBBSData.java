package com.sgo.saldomu.services.jobs;

import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.receivers.LocalResultReceiver;

/**
 * Created by yuddistirakiki on 8/25/17.
 */

public class JobUpdateBBSData extends JobService{

    static public final String TAG = "JobUpdateBBSData";

    @Override
    public boolean onStartJob(final JobParameters job) {
        BBSDataManager bbsDataManager = new BBSDataManager();
        bbsDataManager.runServiceUpdateData(this,
                LocalResultReceiver.getSimpleInstance(new LocalResultReceiver.LocalResultInterface() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        jobFinished(job,false);
                    }
                }));
        //return true karena job yang kita jalaninnya harus jalanin intent service
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
