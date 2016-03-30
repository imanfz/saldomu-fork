package com.sgo.mdevcash.coreclass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.securepreferences.SecurePreferences;
import com.sgo.mdevcash.BuildConfig;
import com.sgo.mdevcash.R;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 3/28/16.
 */
public class FabInstance {
    MaterialSheetFab materialSheetFab;
    public static final int ITEM_FAB_PAYFRIENDS = 0;
    public static final int ITEM_FAB_ASK4MONEY = 1;
    public static final int ITEM_FAB_SHARE = 2;
    private SecurePreferences sp;


    public interface OnBtnListener {
        void OnClickItemFAB(int idx);
    }

    public static MaterialSheetFab newInstance(final Activity mContext, final OnBtnListener mListener){
            FabInstance mObj = new FabInstance(mContext,mListener);
            return mObj.materialSheetFab;
    }


    public FabInstance() {

    }


    public FabInstance(final Activity mContext,final OnBtnListener mListener){

        Fab fab = (Fab) mContext.findViewById(R.id.fab);
        View sheetView = mContext.findViewById(R.id.fab_sheet);
        View overlay = mContext.findViewById(R.id.overlay);
        int sheetColor = getColor(mContext, R.color.white);
        int fabColor = getColor(mContext, R.color.colorPrimaryDarked);
        final int[] statusBarColor = new int[1];

        sp = CustomSecurePref.getInstance().getmSecurePrefs();
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);

        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                // Save current status bar color
                statusBarColor[0] = getStatusBarColor(mContext);
                // Set darker status bar color to match the dim overlay
//                setStatusBarColor(getResources().getColor(R.color.fab_color));
                setStatusBarColor(mContext, statusBarColor[0]);
            }

            @Override
            public void onHideSheet() {
                // Restore status bar color
                setStatusBarColor(mContext, statusBarColor[0]);
            }
        });

        // Set material sheet item click listeners
        mContext.findViewById(R.id.fab_sheet_item_PayFriend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnClickItemFAB(ITEM_FAB_PAYFRIENDS);
                materialSheetFab.hideSheet();
            }
        });
        mContext.findViewById(R.id.fab_sheet_item_AskForMoney).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnClickItemFAB(ITEM_FAB_ASK4MONEY);
                materialSheetFab.hideSheet();
            }
        });

        mContext.findViewById(R.id.fab_sheet_item_Share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntentApp(mContext);
                materialSheetFab.hideSheet();
            }
        });
    }



    private void shareIntentApp(Context mContext){
        try
        { Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.appname));
            String sAux =   sp.getString(DefineValue.LINK_APP,"");
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            mContext.startActivity(Intent.createChooser(i, mContext.getString(R.string.share_title)));
        }
        catch(Exception e)
        {
            Timber.d(e.toString());
        }
    }

    private int getStatusBarColor(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(mContext instanceof Activity)
                return ((Activity)mContext).getWindow().getStatusBarColor();
        }
        return 0;
    }

    private void setStatusBarColor(Context mContext,int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(mContext instanceof Activity)
                ((Activity)mContext).getWindow().setStatusBarColor(color);
        }
    }

    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getResources().getColor(id, context.getTheme());
            }
            else
                return context.getResources().getColor(id);
        }
    }
}
