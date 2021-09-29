package com.sgo.saldomu.coreclass;

import android.content.Context;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.sgo.saldomu.services.UpdateLocationService;
import com.sgo.saldomu.services.jobs.JobRegisterFCM;
import com.sgo.saldomu.services.jobs.JobUpdateBBSData;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/28/17.
 */

public class JobScheduleManager {

    private FirebaseJobDispatcher dispatcher;

    public JobScheduleManager(Context context){
        this.dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
    }

    public static JobScheduleManager getInstance(Context context){
        return new JobScheduleManager(context);
    }

    public void scheduleUpdateDataBBS(){
        Timber.d("Masuk schedule Update Data BBS");
        scheduleJob(JobUpdateBBSData.class,JobUpdateBBSData.TAG);
    }

    public void scheduleUploadLocationService() {
        Timber.d("Masuk schedule Update Location Service");
        scheduleRecurringJob(UpdateLocationService.class,UpdateLocationService.TAG);
    }

    private void scheduleJob(Class <? extends JobService> servicesClass, String tag) {
        Job myJob = dispatcher.newJobBuilder()
                .setService(servicesClass)
                .setTag(tag)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        dispatcher.schedule(myJob);
    }

    private void scheduleRecurringJob(Class <? extends JobService> servicesClass, String tag) {
        Job myLocationJob = dispatcher.newJobBuilder()
                .setService(servicesClass)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(120, 130)) //120-130detik
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTag(tag)
                .setLifetime(Lifetime.FOREVER)
                .build();
        dispatcher.mustSchedule(myLocationJob);

    }

    public void cancelAll() {
        dispatcher.cancelAll();
    }
}
