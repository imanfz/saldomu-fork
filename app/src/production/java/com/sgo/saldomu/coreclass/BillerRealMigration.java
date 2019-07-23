package com.sgo.saldomu.coreclass;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by yuddistirakiki on 2/1/18.
 */

public class BillerRealMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        RealmObjectSchema billerDataModel;

        if (oldVersion == 0) {
            schema.get("Account_Collection_Model")
                    .setRequired("comm_id",true);

            schema.get("bank_biller_model")
                    .setRequired("product_code",true);

            billerDataModel = schema.get("Biller_Data_Model");
            billerDataModel.addField("manual_advice",String.class, FieldAttribute.REQUIRED);
            oldVersion++;

        }
        else if (oldVersion==1)
        {
            billerDataModel = schema.get("Biller_Data_Model");
            billerDataModel.addField("biller_info",String.class);
            oldVersion++;
        }
    }
}
