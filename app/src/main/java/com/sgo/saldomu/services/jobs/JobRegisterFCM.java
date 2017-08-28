package com.sgo.saldomu.services.jobs;

import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.sgo.saldomu.coreclass.BBSDataManager;
import com.sgo.saldomu.fcm.FCMManager;
import com.sgo.saldomu.fcm.FCMWebServiceLoader;
import com.sgo.saldomu.receivers.LocalResultReceiver;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/25/17.
 */

public class JobRegisterFCM extends JobService{

    static public final String TAG = "JobRegisterFCM";

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.d("masuk onstartJob");
        FCMWebServiceLoader.getInstance(this, new FCMWebServiceLoader.LoaderListener() {
            @Override
            public void onSuccessLoader() {
                jobFinished(job,false);
            }

            @Override
            public void onFailedLoader() {
                jobFinished(job,false);
            }
        }).sentTokenToServer(false);
        //return true karena job yang kita jalaninnya harus jalanin intent service
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
