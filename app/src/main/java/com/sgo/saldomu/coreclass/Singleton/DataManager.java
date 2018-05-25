package com.sgo.saldomu.coreclass.Singleton;

import com.sgo.saldomu.Beans.SCADMCommunityModel;

public class DataManager {

    private static DataManager singleton;

    private SCADMCommunityModel item;

    public static DataManager getInstance(){
        if (singleton == null){
            singleton = new DataManager();
        }
        return singleton;
    }

    public void setSACDMCommMod(SCADMCommunityModel item){
        this.item = item;
    }

    public SCADMCommunityModel getSACDMCommMod(){
        return item;
    }
}
