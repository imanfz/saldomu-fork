package com.sgo.saldomu.coreclass;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by thinkpad on 6/12/2017.
 */

public class TagihDataMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 1) {
            schema.create("TagihModel")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("anchor_name", String.class)
                    .addField("anchor_cust", String.class);
            schema.create("TagihCommunityModel")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("comm_code", String.class)
                    .addField("comm_name", String.class);
        }
    }
}
