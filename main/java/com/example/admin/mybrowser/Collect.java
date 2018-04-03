package com.example.admin.mybrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by admin on 2017/3/2.
 */

public class Collect extends Activity {
    EditText name,url;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect);
        name=(EditText) findViewById(R.id.name);
        url=(EditText) findViewById(R.id.url);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        name.setText(bundle.getString("name"));
        url.setText(bundle.getString("url"));
    }

    public void cancel(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        finish();
    }

    public void confirm(View view){
        MySQLiteopenHelper mySQLiteopenHelper=new MySQLiteopenHelper(Collect.this,"history.db3",1);
        SQLiteDatabase sqLiteDatabase=mySQLiteopenHelper.getReadableDatabase();
        Cursor cursor=sqLiteDatabase.rawQuery("select * from collect_table where url =?",new String[]{url.getText().toString().trim()});
        if (!cursor.moveToNext()){
            sqLiteDatabase.execSQL("insert into collect_table(name,url) values(?,?)",new String[]{name.getText().toString(),url.getText().toString().trim()});
            Toast.makeText(Collect.this,"添加书签成功",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,"该书签已经存在！",Toast.LENGTH_SHORT).show();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        finish();
    }
}
