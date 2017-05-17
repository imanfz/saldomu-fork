package com.sgo.orimakardaya.coreclass;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by yuddistirakiki on 4/5/16.
 */
public class AppRealMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            schema.create("List_BBS_City")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("city_id", String.class)
                    .addField("city_name", String.class);
            oldVersion++;
        }
    }
}
