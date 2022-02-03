package com.eterno.joshspy.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {

  // string
  public static void writePreferenceValue(Context context, String prefsKey, String prefsValue) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    editor.putString(prefsKey, prefsValue);
    editor.commit();
  }

  private static SharedPreferences.Editor getPrefsEditor(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.edit();
  }

  public static String  getStringFromPref(Context context, String prefsKey){
    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(AppUtil.getApplication());
    return sharedPreferences.getString(prefsKey, "");
  }

}