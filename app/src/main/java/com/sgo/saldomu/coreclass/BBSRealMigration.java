package com.sgo.saldomu.coreclass;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by thinkpad on 6/12/2017.
 */

public class BBSRealMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            RealmObjectSchema bbs_bank_model = schema.get("BBSBankModel");
            bbs_bank_model.addField("product_display", String.class);
            oldVersion++;
        }
    }
}
