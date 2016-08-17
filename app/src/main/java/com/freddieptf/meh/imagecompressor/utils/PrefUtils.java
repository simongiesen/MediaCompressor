package com.freddieptf.meh.imagecompressor.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.freddieptf.meh.imagecompressor.PrefsActivity;
import com.freddieptf.meh.imagecompressor.R;

/**
 * Created by freddieptf on 15/08/16.
 */
public class PrefUtils {

    public static boolean canGetPictureBroadcast(Context context){
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PrefsActivity.APP_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(context.getString(R.string.quick_image_compress_title), false);
    }

}
