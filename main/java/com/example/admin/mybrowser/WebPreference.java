package com.example.admin.mybrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * Created by admin on 2017/3/2.
 */

public class WebPreference extends PreferenceActivity{
    Preference clear,skin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        clear=(Preference) findPreference("clear");
        skin=(Preference) findPreference("skin");
        clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(WebPreference.this)
                        .setTitle("清除缓存")
                        .setMessage("是否清除缓存")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.show.clearCache(true);
                                Toast.makeText(WebPreference.this,"清除成功",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });

        skin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent skin_intent=new Intent(WebPreference.this,ImageChose.class);
                WebPreference.this.startActivity(skin_intent);
                return true;
            }
        });
        setResult(MainActivity.CODE_SETTING,getIntent());
    }
}
