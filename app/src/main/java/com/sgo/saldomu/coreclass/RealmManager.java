package com.sgo.saldomu.coreclass;

import android.content.Context;

import com.sgo.saldomu.Beans.Account_Collection_Model;
import com.sgo.saldomu.Beans.Biller_Data_Model;
import com.sgo.saldomu.Beans.Biller_Type_Data_Model;
import com.sgo.saldomu.Beans.Denom_Data_Model;
import com.sgo.saldomu.Beans.bank_biller_model;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.entityRealm.AgentDetail;
import com.sgo.saldomu.entityRealm.AgentServiceDetail;
import com.sgo.saldomu.entityRealm.BBSAccountACTModel;
import com.sgo.saldomu.entityRealm.BBSBankModel;
import com.sgo.saldomu.entityRealm.BBSCommModel;
import com.sgo.saldomu.entityRealm.List_BBS_City;
import com.sgo.saldomu.entityRealm.MerchantCommunityList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.annotations.RealmModule;
import timber.log.Timber;

/**
 * Created by yuddistirakiki on 4/24/17.
 */

public class RealmManager {

    public static RealmConfiguration BillerConfiguration;
    public static RealmConfiguration BBSConfiguration;
    public static RealmConfiguration BBSMemberBankConfiguration;

    @RealmModule(classes = { Account_Collection_Model.class, bank_biller_model.class,
            Biller_Data_Model.class, Biller_Type_Data_Model.class, Denom_Data_Model.class})
    private static class BillerModule {
    }

    @RealmModule(classes = { List_BBS_City.class, AgentDetail.class, AgentServiceDetail.class, MerchantCommunityList.class})
    private static class AppModule {
    }

    @RealmModule(classes = { BBSBankModel.class, BBSCommModel.class, BBSAccountACTModel.class})
    private static class BBSModule {
    }

    @RealmModule(classes = { BBSBankModel.class})
    private static class BBSMemberBankModule {
    }

    public static void init(Context mContext, int rawBiller){
        File file = new File(mContext.getFilesDir(), BuildConfig.REALM_BILLER_NAME);
        copyBundledRealmFile(mContext.getResources().openRawResource(rawBiller),file);

        file = new File(mContext.getFilesDir(),BuildConfig.REALM_BBS_MEMBER_BANK_NAME);
        copyBundledRealmFile(mContext.getResources().openRawResource(R.raw.bbsmemberbank),file);

        Realm.init(mContext);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(BuildConfig.REALM_APP_NAME)
                .schemaVersion(BuildConfig.REALM_SCHEME_APP_VERSION)
                .modules(new AppModule())
                .migration(new AppRealMigration())
                .build();

        Realm.setDefaultConfiguration(config);

        BillerConfiguration = new RealmConfiguration.Builder()
                .name(BuildConfig.REALM_BILLER_NAME)
                .schemaVersion(BuildConfig.REALM_SCHEME_BILLER_VERSION)
                .modules(new BillerModule())
                .migration(new BillerRealMigration())
                .build();

        BBSConfiguration = new RealmConfiguration.Builder()
                .name(BuildConfig.REALM_BBS_NAME)
                .schemaVersion(BuildConfig.REALM_SCHEME_BBS_VERSION)
                .modules(new BBSModule())
                .migration(new BBSRealMigration())
                .build();

        BBSMemberBankConfiguration = new RealmConfiguration.Builder()
                .name(BuildConfig.REALM_BBS_MEMBER_BANK_NAME)
                .schemaVersion(BuildConfig.REALM_SCHEME_BBS_MEMBER_BANK_VERSION)
                .modules(new BBSMemberBankModule())
                .migration(new BBSMemberBankMigration())
                .build();
    }

    private static DynamicRealm getDynamicRealm(RealmConfiguration realmConfig){
        return DynamicRealm.getInstance(realmConfig);
    }

    public static long getCurrentVersionRealm(RealmConfiguration realmConfiguration){
        return getDynamicRealm(realmConfiguration).getVersion();
    }

    public static Realm getRealmBBSMemberBank(){
        return Realm.getInstance(BBSMemberBankConfiguration);
    }

    public static Realm getRealmBBS(){
        return Realm.getInstance(BBSConfiguration);
    }

    public static Realm getRealmBiller(){
        return Realm.getInstance(BillerConfiguration);
    }

    public static void closeRealm(Realm realm){
        if(realm != null) {
            realm.removeAllChangeListeners();
            realm.close();
        }
    }


    private static String copyBundledRealmFile(InputStream inputStream, File fileRealm) {
        try {
            long sizeraw = inputStream.available();
            long sizefile = 0;
            if(fileRealm.exists()) {
                sizefile = fileRealm.length();
                Timber.d("sizeRaw / sizeFile = "+ String.valueOf(sizeraw)+" / "+ String.valueOf(sizefile));
            }

            if(sizeraw != sizefile) {
                FileOutputStream outputStream = new FileOutputStream(fileRealm);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
                outputStream.close();
                Timber.d("file baru dicopy");
                return fileRealm.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timber.d("file tidak dicopy");
        return null;
    }

}
