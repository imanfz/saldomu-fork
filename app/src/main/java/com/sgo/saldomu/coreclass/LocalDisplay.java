package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 7/9/2015.
 */
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

class LocalDisplay {

  private static int SCREEN_WIDTH_PIXELS;
  private static int SCREEN_HEIGHT_PIXELS;
  private static float SCREEN_DENSITY;
  private static int SCREEN_WIDTH_DP;
  private static int SCREEN_HEIGHT_DP;
  private static boolean sInitialed;

  public static void init(Context context) {
    if (sInitialed || context == null) {
      return;
    }
    sInitialed = true;
    DisplayMetrics dm = new DisplayMetrics();
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    wm.getDefaultDisplay().getMetrics(dm);
    SCREEN_WIDTH_PIXELS = dm.widthPixels;
    SCREEN_HEIGHT_PIXELS = dm.heightPixels;
    SCREEN_DENSITY = dm.density;
    SCREEN_WIDTH_DP = (int) (SCREEN_WIDTH_PIXELS / dm.density);
    SCREEN_HEIGHT_DP = (int) (SCREEN_HEIGHT_PIXELS / dm.density);
  }

  private static int dp2px(float dp) {
    final float scale = SCREEN_DENSITY;
    return (int) (dp * scale + 0.5f);
  }

  private static int designedDP2px(float designedDp) {
    if (SCREEN_WIDTH_DP != 320) {
      designedDp = designedDp * SCREEN_WIDTH_DP / 320f;
    }
    return dp2px(designedDp);
  }

  public static void setPadding(final View view, float left, float top, float right, float bottom) {
    view.setPadding(designedDP2px(left), dp2px(top), designedDP2px(right), dp2px(bottom));
  }
}
