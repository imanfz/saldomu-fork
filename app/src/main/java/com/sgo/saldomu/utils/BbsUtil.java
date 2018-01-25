package com.sgo.saldomu.utils;

import com.sgo.saldomu.R;
import com.sgo.saldomu.entityRealm.BBSBankModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lenovo on 08/01/2018.
 */

public class BbsUtil {
    public static List<HashMap<String,String>> mappingProductCodeIcons(List<BBSBankModel> bankMember) {
        List<HashMap<String, String>> mapIcons = new ArrayList<>();

        for(int i=0;i<bankMember.size();i++){
            HashMap<String, String> hm = new HashMap<>();
            hm.put("txt", bankMember.get(i).getProduct_name());

            if(bankMember.get(i).getProduct_name().toLowerCase().contains("mandiri"))
                hm.put("flag", Integer.toString(R.drawable.logo_mandiri_bank_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bri"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bri_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("permata"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_permata_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("uob"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_uob_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("maspion"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_maspion_rev1_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bii"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_bii_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("jatim"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_jatim_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("bca"))
                hm.put("flag", Integer.toString(R.drawable.logo_bca_bank_small));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("nobu"))
                hm.put("flag", Integer.toString(R.drawable.logo_bank_nobu));
            else if(bankMember.get(i).getProduct_name().toLowerCase().contains("saldomu"))
                hm.put("flag", Integer.toString(R.drawable.logo_small));
            else
                hm.put("flag", Integer.toString(R.drawable.ic_square_gate_one));
            mapIcons.add(hm);
        }

        return mapIcons;
    }
}
