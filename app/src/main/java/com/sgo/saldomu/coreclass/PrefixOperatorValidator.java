package com.sgo.saldomu.coreclass;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sgo.saldomu.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by yuddistirakiki on 6/13/17.
 */

public class PrefixOperatorValidator {

    public class OperatorModel{
        public String prefix;
        public String prefix_name;

    }

    Context context;

    private PrefixOperatorValidator(Context mContext){
        context = mContext;
    }


    public static OperatorModel validation(Context mContext,String number){
        number = NoHPFormat.formatTo08(number);
        if(!number.isEmpty()){
            number = number.substring(0,4);
            PrefixOperatorValidator prefValidator = new PrefixOperatorValidator(mContext);
                ArrayList<OperatorModel> operatorModels = prefValidator.getListData();
            if(operatorModels.size() > 0){
                int start = 0;
                int end = operatorModels.size() - 1;
                while (start <= end) {
                    int mid = (start + end) / 2;
                    int cIdx = number.compareTo(operatorModels.get(mid).prefix);
                    if (cIdx == 0) {
                        return operatorModels.get(mid);
                    }
                    if (cIdx < 0) {
                        end = mid - 1;
                    } else {
                        start = mid + 1;
                    }
                }
            }
        }
        return null;
    }

    private ArrayList<OperatorModel> getListData(){
        InputStream in = context.getResources().openRawResource(R.raw.prefixoperator);
        Reader rd = new BufferedReader(new InputStreamReader(in));
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<OperatorModel>>(){}.getType();
        ArrayList<OperatorModel> operatorModels  = gson.fromJson(rd,listType);
        if(operatorModels != null && operatorModels.size() > 0)
            return operatorModels;
        else
            return new ArrayList<>();
    }
}
