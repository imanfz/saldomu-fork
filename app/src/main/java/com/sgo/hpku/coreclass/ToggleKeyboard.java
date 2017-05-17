package com.sgo.hpku.coreclass;/*
  Created by Administrator on 5/12/2015.
 */

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class ToggleKeyboard {

  public static void hide_keyboard(Activity activity) {
    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    //Find the currently focused view, so we can grab the correct window token from it.
    View view = activity.getCurrentFocus();
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if(view == null) {
      view = new View(activity);
    }
    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  public static void show_keyboard(Activity activity) {
    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    //Find the currently focused view, so we can grab the correct window token from it.
    View view = activity.getCurrentFocus();
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if(view == null) {
      view = new View(activity);
    }
    inputMethodManager.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
  }

}
