package com.example.admin.mybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/3/2.
 */
public class CHShow extends Activity{

    private ListView collect_show,history_show;
    private ArrayList<Map<String, String>> list_history=new ArrayList<Map<String, String>>();
    private ArrayList<Map<String, String>> list_collect=new ArrayList<Map<String, String>>();
    private MySQLiteopenHelper mySQLiteopenHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor collect_cursor;
    private Cursor history_cursor;
    private Button clear,edit;
    CHBaseAdapter collect_adapter,history_adapter;
    private List<Integer> selectItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_h_show);
        collect_show = (ListView) findViewById(R.id.collect_show);
        history_show = (ListView) findViewById(R.id.history_show);
        clear=(Button)findViewById(R.id.clear_c_h);
        edit=(Button) findViewById(R.id.edit_c_h);
        mySQLiteopenHelper= new MySQLiteopenHelper(CHShow.this, "history.db3", 1);
        sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
        collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
        history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});
        if (!collect_cursor.moveToNext()){
            clear.setEnabled(false);
            edit.setEnabled(false);
            findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
        }
        else {
            clear.setEnabled(true);
            edit.setEnabled(true);
            collect_cursor.moveToPrevious();
            findViewById(R.id.no_tip).setVisibility(View.GONE);
        }
        collect_adapter = new CHBaseAdapter(this,list_collect=CursortoList(collect_cursor),0);
        collect_show.setAdapter(collect_adapter);
        history_adapter = new CHBaseAdapter(this,list_history=CursortoList(history_cursor),0);
        history_show.setAdapter(history_adapter);

        collect_show.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s=((TextView) view.findViewById(R.id.item_url)).getText().toString();
                Intent intent=getIntent();
                intent.putExtra("url",s);
                CHShow.this.setResult(MainActivity.CODE_CH,intent);
                finish();
            }
        });

        collect_show.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popupMenu = new PopupMenu(CHShow.this, view);
                MenuInflater menuInflater = new MenuInflater(CHShow.this);
                menuInflater.inflate(R.menu.popup_menu_collect, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.copy_collect:
                                ClipboardManager cm = (ClipboardManager) CHShow.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(list_collect.get(position).get("url"));
                                Toast.makeText(CHShow.this,"链接复制成功",Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.change_collect:
                                final LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.change, null);
                                final EditText name_change = (EditText) linearLayout.findViewById(R.id.name_change);
                                final EditText url_change = (EditText) linearLayout.findViewById(R.id.url_change);
                                name_change.setText(list_collect.get(position).get("name"));
                                url_change.setText(list_collect.get(position).get("url"));
                                new AlertDialog.Builder(CHShow.this)
                                        .setTitle("修改书签")
                                        .setView(linearLayout)
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String name = name_change.getText().toString().trim();
                                                String url = url_change.getText().toString().trim();
                                                if (name.trim().isEmpty() || url.trim().isEmpty()) {
                                                    Toast.makeText(CHShow.this, "请输入内容!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    sqLiteDatabase.execSQL("update collect_table set name=?,url=? where url=?",
                                                            new String[]{name, url,list_collect.get(position).get("url") });
                                                    Toast.makeText(CHShow.this,"修改成功",Toast.LENGTH_SHORT).show();
                                                    collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
                                                    collect_adapter = new CHBaseAdapter(CHShow.this,list_collect=CursortoList(collect_cursor),0);
                                                    collect_show.setAdapter(collect_adapter);
                                                    dialog.dismiss();
                                                }
                                            }
                                        })
                                        .setNegativeButton("取消", null)
                                        .create()
                                        .show();
                                break;

                            case R.id.delete_collect:
                                new AlertDialog.Builder(CHShow.this)
                                        .setTitle("删除书签")
                                        .setMessage("是否确认删除？")
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                sqLiteDatabase.execSQL("delete from collect_table where url=?",
                                                        new String[]{list_collect.get(position).get("url")});
                                                Toast.makeText(CHShow.this,"删除成功",Toast.LENGTH_SHORT).show();
                                                collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
                                                dialog.dismiss();
                                                if (!collect_cursor.moveToNext()){
                                                    clear.setEnabled(false);
                                                    edit.setEnabled(false);
                                                    findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
                                                    collect_show.setVisibility(View.GONE);
                                                }
                                                else {
                                                    clear.setEnabled(true);
                                                    edit.setEnabled(true);
                                                    collect_cursor.moveToPrevious();
                                                    findViewById(R.id.no_tip).setVisibility(View.GONE);
                                                    collect_adapter = new CHBaseAdapter(CHShow.this,list_collect=CursortoList(collect_cursor),0);
                                                    collect_show.setAdapter(collect_adapter);
                                                    collect_show.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        })
                                        .setNegativeButton("取消", null)
                                        .create()
                                        .show();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });

        history_show.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s=((TextView) view.findViewById(R.id.item_url)).getText().toString();
                Intent intent=getIntent();
                intent.putExtra("url",s);
                CHShow.this.setResult(MainActivity.CODE_CH,intent);
                finish();
            }
        });

        history_show.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                PopupMenu popupMenu = new PopupMenu(CHShow.this, view);
                MenuInflater menuInflater = new MenuInflater(CHShow.this);
                menuInflater.inflate(R.menu.popup_menu_history, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.copy_history:
                                ClipboardManager cm = (ClipboardManager) CHShow.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(list_history.get(position).get("url"));
                                Toast.makeText(CHShow.this,"链接复制成功",Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.delete_history:
                                new AlertDialog.Builder(CHShow.this)
                                        .setTitle("删除历史")
                                        .setMessage("是否确认删除？")
                                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                sqLiteDatabase.execSQL("delete from history_table where url=?",
                                                        new String[]{((TextView)view.findViewById(R.id.item_url)).getText().toString()});
                                                Toast.makeText(CHShow.this,"删除成功",Toast.LENGTH_SHORT).show();
                                                history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});
                                                dialog.dismiss();
                                                if (!history_cursor.moveToNext()){
                                                    clear.setEnabled(false);
                                                    edit.setEnabled(false);
                                                    findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
                                                    history_show.setVisibility(View.GONE);
                                                }
                                                else {
                                                    clear.setEnabled(true);
                                                    edit.setEnabled(true);
                                                    history_cursor.moveToPrevious();
                                                    findViewById(R.id.no_tip).setVisibility(View.GONE);
                                                    history_adapter = new CHBaseAdapter(CHShow.this,list_history=CursortoList(history_cursor),0);
                                                    history_show.setAdapter(history_adapter);
                                                    history_show.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        })
                                        .setNegativeButton("取消", null)
                                        .create()
                                        .show();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });

        Intent intent=getIntent();
        CHShow.this.setResult(MainActivity.CODE_NO,intent);
    }

    private ArrayList<Map<String, String>> CursortoList(Cursor cursor) {
        ArrayList<Map<String, String>> result=new ArrayList<Map<String, String>>();
        if (cursor.moveToLast()){
            Map<String,String> map1=new HashMap<>();
            map1.put("name",cursor.getString(1));
            map1.put("url",cursor.getString(2));
            result.add(map1);
        }
        while (cursor.moveToPrevious()){
            Map<String,String> map=new HashMap<>();
            map.put("name",cursor.getString(1));
            map.put("url",cursor.getString(2));
            result.add(map);
        }
        return result;
    }

    public void collect_button(View view){
        findViewById(R.id.edit_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.clear_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
        findViewById(R.id.delete_c_h).setVisibility(View.GONE);
        history_show.setVisibility(View.GONE);
        collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
        if (!collect_cursor.moveToNext()){
            clear.setEnabled(false);
            edit.setEnabled(false);
            findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
            collect_show.setVisibility(View.GONE);
        }
        else {
            clear.setEnabled(true);
            edit.setEnabled(true);
            collect_cursor.moveToPrevious();
            findViewById(R.id.no_tip).setVisibility(View.GONE);
            collect_adapter = new CHBaseAdapter(CHShow.this,list_collect=CursortoList(collect_cursor),0);
            collect_show.setAdapter(collect_adapter);
            collect_show.setVisibility(View.VISIBLE);
        }
    }

    public void history_button(View view){
        findViewById(R.id.edit_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.clear_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
        findViewById(R.id.delete_c_h).setVisibility(View.GONE);
        collect_show.setVisibility(View.GONE);
        history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});

        if (!history_cursor.moveToNext()){
            clear.setEnabled(false);
            edit.setEnabled(false);
            findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
            history_show.setVisibility(View.GONE);
        }
        else {
            clear.setEnabled(true);
            edit.setEnabled(true);
            history_cursor.moveToPrevious();
            findViewById(R.id.no_tip).setVisibility(View.GONE);
            history_adapter = new CHBaseAdapter(CHShow.this,list_history=CursortoList(history_cursor),0);
            history_show.setAdapter(history_adapter);
            history_show.setVisibility(View.VISIBLE);
        }
    }

    public void clear_ch(View view){
        if (collect_show.isShown()) {
            new AlertDialog.Builder(CHShow.this)
                    .setTitle("清空收藏")
                    .setMessage("是否确认清空？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sqLiteDatabase.execSQL("delete from collect_table");
                            Toast.makeText(CHShow.this, "清空成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            collect_show.setVisibility(View.GONE);
                            findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
                            findViewById(R.id.edit_c_h).setVisibility(View.GONE);
                            findViewById(R.id.clear_c_h).setVisibility(View.GONE);
                            findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
                            findViewById(R.id.delete_c_h).setVisibility(View.GONE);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }
        if (history_show.isShown()){
            new AlertDialog.Builder(CHShow.this)
                    .setTitle("清空历史记录")
                    .setMessage("是否确认清空？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sqLiteDatabase.execSQL("delete from history_table");
                            Toast.makeText(CHShow.this, "清空成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            history_show.setVisibility(View.GONE);
                            findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
                            findViewById(R.id.edit_c_h).setVisibility(View.GONE);
                            findViewById(R.id.clear_c_h).setVisibility(View.GONE);
                            findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
                            findViewById(R.id.delete_c_h).setVisibility(View.GONE);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }
    }

    public void select_ch(View view){
        findViewById(R.id.edit_c_h).setVisibility(View.GONE);
        findViewById(R.id.clear_c_h).setVisibility(View.GONE);
        findViewById(R.id.cancel_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.delete_c_h).setVisibility(View.VISIBLE);

        if (collect_show.isShown()){
            mySQLiteopenHelper= new MySQLiteopenHelper(CHShow.this, "history.db3", 1);
            sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
            collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
            collect_adapter = new CHBaseAdapter(this,list_collect=CursortoList(collect_cursor),1);
            collect_show.setAdapter(collect_adapter);
        }
        if (history_show.isShown()){
            mySQLiteopenHelper= new MySQLiteopenHelper(CHShow.this, "history.db3", 1);
            sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
            history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});
            history_adapter = new CHBaseAdapter(this,list_history=CursortoList(history_cursor),1);
            history_show.setAdapter(history_adapter);
        }
    }

    public void cancel_ch(View view){
        findViewById(R.id.edit_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.clear_c_h).setVisibility(View.VISIBLE);
        findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
        findViewById(R.id.delete_c_h).setVisibility(View.GONE);
        if (collect_show.isShown()){
            mySQLiteopenHelper= new MySQLiteopenHelper(CHShow.this, "history.db3", 1);
            sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
            collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
            collect_adapter = new CHBaseAdapter(this,list_collect=CursortoList(collect_cursor),0);
            collect_show.setAdapter(collect_adapter);
        }
        if (history_show.isShown()){
            mySQLiteopenHelper= new MySQLiteopenHelper(CHShow.this, "history.db3", 1);
            sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
            history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});
            history_adapter = new CHBaseAdapter(this,list_history=CursortoList(history_cursor),0);
            history_show.setAdapter(history_adapter);
        }
    }

    public void delete_ch(View view){
        if (collect_show.isShown()){
            selectItems=collect_adapter.getSelectItems();
            if (selectItems.size()==0){
                Toast.makeText(CHShow.this,"请选择删除项目",Toast.LENGTH_SHORT).show();
            }
            else {
                new AlertDialog.Builder(CHShow.this)
                        .setTitle("删除记录")
                        .setMessage("是否确认删除？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!selectItems.isEmpty()){
                                    sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                    collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
                                    list_collect=CursortoList(collect_cursor);
                                    Iterator iterator=selectItems.iterator();
                                    while (iterator.hasNext()) {
                                        int id=(Integer)iterator.next();
                                        sqLiteDatabase.execSQL("delete from collect_table where url=?",
                                                new String[]{list_collect.get(id).get("url")});
                                    }

                                    findViewById(R.id.edit_c_h).setVisibility(View.VISIBLE);
                                    findViewById(R.id.clear_c_h).setVisibility(View.VISIBLE);
                                    findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
                                    findViewById(R.id.delete_c_h).setVisibility(View.GONE);
                                    collect_cursor = sqLiteDatabase.rawQuery("select * from collect_table where url like ?", new String[]{"%" + ":" + "%"});
                                    if (!collect_cursor.moveToNext()){
                                        clear.setEnabled(false);
                                        edit.setEnabled(false);
                                        findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
                                        collect_show.setVisibility(View.GONE);
                                    }
                                    else {
                                        clear.setEnabled(true);
                                        edit.setEnabled(true);
                                        collect_cursor.moveToPrevious();
                                        findViewById(R.id.no_tip).setVisibility(View.GONE);
                                        collect_adapter = new CHBaseAdapter(CHShow.this,list_collect=CursortoList(collect_cursor),0);
                                        collect_show.setAdapter(collect_adapter);
                                        collect_show.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }
        }
        if (history_show.isShown()){
            selectItems=history_adapter.getSelectItems();
            if (selectItems.size()==0){
                Toast.makeText(CHShow.this,"请选择删除项目",Toast.LENGTH_SHORT).show();
            }
            else{
                new AlertDialog.Builder(CHShow.this)
                        .setTitle("删除记录")
                        .setMessage("是否确认删除？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!selectItems.isEmpty()){
                                    sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                    history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});
                                    list_history=CursortoList(history_cursor);
                                    Iterator iterator=selectItems.iterator();
                                    while (iterator.hasNext()) {
                                        int id=(Integer)iterator.next();
                                        sqLiteDatabase.execSQL("delete from history_table where url=?",
                                                new String[]{list_history.get(id).get("url")});
                                    }

                                    findViewById(R.id.edit_c_h).setVisibility(View.VISIBLE);
                                    findViewById(R.id.clear_c_h).setVisibility(View.VISIBLE);
                                    findViewById(R.id.cancel_c_h).setVisibility(View.GONE);
                                    findViewById(R.id.delete_c_h).setVisibility(View.GONE);
                                    history_cursor = sqLiteDatabase.rawQuery("select * from history_table where url like ?", new String[]{"%" + ":" + "%"});
                                    if (!history_cursor.moveToNext()){
                                        clear.setEnabled(false);
                                        edit.setEnabled(false);
                                        findViewById(R.id.no_tip).setVisibility(View.VISIBLE);
                                        history_show.setVisibility(View.GONE);
                                    }
                                    else {
                                        clear.setEnabled(true);
                                        edit.setEnabled(true);
                                        history_cursor.moveToPrevious();
                                        findViewById(R.id.no_tip).setVisibility(View.GONE);
                                        history_adapter= new CHBaseAdapter(CHShow.this,list_history=CursortoList(history_cursor),0);
                                        history_show.setAdapter(history_adapter);
                                        history_show.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }
        }
    }
}
