package com.freddieptf.meh.imagecompressor;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


/**
 * Created by freddieptf on 17/08/16.
 */
public class PrefsActivity extends AppCompatActivity {

    public static String APP_PREFS = "app_prefs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.prefs_container, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        SharedPreferences sharedPreferences;
        Preference imageCompressorPref;
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            sharedPreferences = getActivity().getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);

            imageCompressorPref = findPreference(getString(R.string.quick_image_compress_title));
            imageCompressorPref.setOnPreferenceChangeListener(this);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode == 90 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                onPreferenceChange(imageCompressorPref, true);
            }
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if(preference instanceof SwitchPreference) {

                if(preference.getKey().equals(getString(R.string.quick_image_compress_title))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if ((boolean) o && getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 90);
                            return false;
                        }
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.quick_image_compress_title), (boolean) o);
                    editor.apply();
                }
            }
            return true;
        }
    }
}
