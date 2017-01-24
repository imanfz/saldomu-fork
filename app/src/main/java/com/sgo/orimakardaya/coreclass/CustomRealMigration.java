package com.sgo.orimakardaya.coreclass;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

/**
 * Created by yuddistirakiki on 4/5/16.
 */
class CustomRealMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

//        RealmSchema schema = realm.getSchema();
//
//        if (oldVersion == 0) {
//            RealmObjectSchema BillerSchema = schema.get("Biller_Data_Model");
//
//            BillerSchema.removePrimaryKey();
//        }
    }
}
