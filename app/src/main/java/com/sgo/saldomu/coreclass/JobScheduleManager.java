package com.sgo.saldomu.coreclass;

import android.content.Context;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobService;
import com.sgo.saldomu.services.jobs.JobRegisterFCM;
import com.sgo.saldomu.services.jobs.JobUpdateBBSData;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 8/28/17.
 */

public class JobScheduleManager {

    private Context mContext;
    private FirebaseJobDispatcher dispatcher;

    public JobScheduleManager(Context context){
        this.mContext = context;
        this.dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
    }

    public static JobScheduleManager getInstance(Context context){
        return new JobScheduleManager(context);
    }

    public void scheduleRegisterFCM(){
        Timber.d("Masuk schedule Register FCM");
        scheduleJob(JobRegisterFCM.class,JobRegisterFCM.TAG);
    }

    public void scheduleUpdateDataBBS(){
        Timber.d("Masuk schedule Update Data BBS");
        scheduleJob(JobUpdateBBSData.class,JobUpdateBBSData.TAG);
    }

    private void scheduleJob(Class <? extends JobService> servicesClass, String tag) {
        Job myJob = dispatcher.newJobBuilder()
                .setService(servicesClass)
                .setTag(tag)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        dispatcher.schedule(myJob);
    }
}
