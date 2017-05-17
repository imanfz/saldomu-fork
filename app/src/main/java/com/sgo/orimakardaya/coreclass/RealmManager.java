package com.sgo.orimakardaya.coreclass;

import android.content.Context;

import com.sgo.orimakardaya.R;
//import com.sgo.orimakardaya.entityRealm.Account_Collection_Model;
import com.sgo.orimakardaya.entityRealm.BBSBankModel;
import com.sgo.orimakardaya.entityRealm.BBSCommModel;
//import com.sgo.orimakardaya.entityRealm.Biller_Data_Model;
//import com.sgo.orimakardaya.entityRealm.Biller_Type_Data_Model;
//import com.sgo.orimakardaya.entityRealm.Denom_Data_Model;
//import com.sgo.orimakardaya.entityRealm.List_Account_Nabung;
import com.sgo.orimakardaya.entityRealm.List_BBS_City;
//import com.sgo.orimakardaya.entityRealm.List_Bank_Nabung;
//import com.sgo.orimakardaya.entityRealm.Target_Saving_Model;
//import com.sgo.orimakardaya.entityRealm.bank_biller_model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.annotations.RealmModule;
import timber.log.Timber;

/**
 * Created by yuddistirakiki on 4/24/17.
 */

public class RealmManager {

//    public static RealmConfiguration BillerConfiguration;
    public static RealmConfiguration BBSConfiguration;

//    @RealmModule(classes = { Account_Collection_Model.class, bank_biller_model.class, Biller_Data_Model.class, Biller_Type_Data_Model.class,
//            Denom_Data_Model.class})
//    private static class BillerModule {
//    }

    @RealmModule(classes = { List_BBS_City.class})
    private static class AppModule {
    }

    @RealmModule(classes = { BBSBankModel.class, BBSCommModel.class})
    private static class BBSModule {
    }

    public static void init(Context mContext){
//        File file = new File(mContext.getFilesDir(),mContext.getString(R.string.realmBillerName));
//        copyBundledRealmFile(mContext.getResources().openRawResource(R.raw.akardayadev),file);

        File file = new File(mContext.getFilesDir(),mContext.getString(R.string.realmBBSName));
        copyBundledRealmFile(mContext.getResources().openRawResource(R.raw.hpkubbsdev),file);

        Realm.init(mContext);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(mContext.getString(R.string.realmname))
                .schemaVersion(mContext.getResources().getInteger(R.integer.realscheme))
                .modules(new AppModule())
                .migration(new AppRealMigration())
                .build();

        Realm.setDefaultConfiguration(config);

//        BillerConfiguration = new RealmConfiguration.Builder()
//                .name(mContext.getString(R.string.realmBillerName))
//                .schemaVersion(mContext.getResources().getInteger(R.integer.realBillerscheme))
//                .modules(new BillerModule())
//                .migration(new BillerRealMigration())
//                .build();

        BBSConfiguration = new RealmConfiguration.Builder()
                .name(mContext.getString(R.string.realmBBSName))
                .schemaVersion(mContext.getResources().getInteger(R.integer.realBBScheme))
                .modules(new BBSModule())
                .build();
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
