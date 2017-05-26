package com.sgo.hpku.coreclass;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by yuddistirakiki on 4/5/16.
 */
class BillerRealMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            RealmObjectSchema account_collection_model = schema.get("Account_Collection_Model");
            account_collection_model.setNullable("comm_id",true);
            oldVersion++;
        }
    }
}
