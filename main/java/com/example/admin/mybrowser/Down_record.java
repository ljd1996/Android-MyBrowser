package com.example.admin.mybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin on 2017/3/3.
 */

public class Down_record extends Activity {

    private ListView down_listView;
    private Button edit_button,delete_button,clear_button,cancel_button;
    private MySQLiteopenHelper mySQLiteopenHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor download_cursor;
    private DownRecordBaseAdapter adapter;
    private ArrayList<Long> list;

    private DownloadManager downloadManager;
    private DownloadManager.Query query;
    private Cursor cursor=null;

    private List<Integer> selectItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.down_show);

        down_listView=(ListView)findViewById(R.id.down_list);
        edit_button=(Button) findViewById(R.id.id_edit_down);
        delete_button=(Button)findViewById(R.id.id_delete_down);
        clear_button=(Button)findViewById(R.id.id_clear_down);
        cancel_button=(Button)findViewById(R.id.id_cancel_down);

        downloadManager= (DownloadManager) Down_record.this.getSystemService(Context.DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();

        mySQLiteopenHelper= new MySQLiteopenHelper(this, "history.db3", 1);
        sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
        download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});

        if (!download_cursor.moveToNext()){
            clear_button.setEnabled(false);
            edit_button.setEnabled(false);
        }
        else {
            clear_button.setEnabled(true);
            edit_button.setEnabled(true);
            download_cursor.moveToPrevious();
        }
        adapter=new DownRecordBaseAdapter(this,list=CursortoList(download_cursor),0);
        down_listView.setAdapter(adapter);
        down_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                edit_button.setVisibility(View.GONE);
                clear_button.setVisibility(View.GONE);
                delete_button.setVisibility(View.VISIBLE);
                cancel_button.setVisibility(View.VISIBLE);

                sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});

                adapter=new DownRecordBaseAdapter(Down_record.this,list=CursortoList(download_cursor),1);
                down_listView.setAdapter(adapter);
                return true;
            }
        });
    }

    private ArrayList<Long> CursortoList(Cursor cursor) {
        ArrayList<Long> result=new ArrayList<Long>();
        while (cursor.moveToNext()){
            result.add(cursor.getLong(1));
        }
        return result;
    }

    public void file_open(View view){
        Intent intent=new Intent(Down_record.this,FileBrowser.class);
        startActivity(intent);
    }

    public void clear_down(View view){
        new AlertDialog.Builder(Down_record.this)
                .setTitle("清空下载记录")
                .setMessage("是否清除?")
                .setPositiveButton("删除任务及文件", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                        download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});
                        list=CursortoList(download_cursor);
                        Iterator iterator=list.iterator();
                        while (iterator.hasNext()){
                            long id=(long)iterator.next();
                            query.setFilterById(id);
                            cursor=downloadManager.query(query);
                            if (!cursor.moveToFirst()) {
                                cursor.close();
                            }
                            else {
                                downloadManager.remove(id);
                                cursor.close();
                            }
                        }
                        sqLiteDatabase.execSQL("delete from download_table");
                        Toast.makeText(Down_record.this, "清空成功", Toast.LENGTH_SHORT).show();
                        down_listView.setVisibility(View.GONE);
                        edit_button.setVisibility(View.GONE);
                        clear_button.setVisibility(View.GONE);
                        delete_button.setVisibility(View.GONE);
                        cancel_button.setVisibility(View.GONE);
                    }
                })
                .setNeutralButton("删除任务", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                        download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});
                        list=CursortoList(download_cursor);
                        Iterator iterator=list.iterator();
                        while (iterator.hasNext()){
                            long id=(long)iterator.next();
                            query.setFilterById(id);
                            cursor=downloadManager.query(query);
                            if (!cursor.moveToFirst()) {
                                cursor.close();
                            }
                            else {
                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))!=DownloadManager.STATUS_SUCCESSFUL) {
                                    downloadManager.remove(id);
                                }
                                cursor.close();
                            }
                        }
                        sqLiteDatabase.execSQL("delete from download_table");
                        Toast.makeText(Down_record.this, "清空成功", Toast.LENGTH_SHORT).show();
                        down_listView.setVisibility(View.GONE);
                        edit_button.setVisibility(View.GONE);
                        clear_button.setVisibility(View.GONE);
                        delete_button.setVisibility(View.GONE);
                        cancel_button.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("取消",null)
                .create()
                .show();
    }

    public void edit_down(View view){
        edit_button.setVisibility(View.GONE);
        clear_button.setVisibility(View.GONE);
        delete_button.setVisibility(View.VISIBLE);
        cancel_button.setVisibility(View.VISIBLE);

        sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
        download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});

        adapter=new DownRecordBaseAdapter(this,list=CursortoList(download_cursor),1);
        down_listView.setAdapter(adapter);
    }

    public void delete_down(View view){
        selectItems=adapter.getSelectItems();
        if (selectItems.size()==0){
            Toast.makeText(Down_record.this,"请选择删除项目",Toast.LENGTH_SHORT).show();
        }
        else {
            new AlertDialog.Builder(Down_record.this)
                    .setTitle("删除下载记录")
                    .setMessage("是否确认删除？")
                    .setPositiveButton("删除任务及文件", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!selectItems.isEmpty()){
                                sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});
                                list=CursortoList(download_cursor);
                                Iterator iterator=selectItems.iterator();
                                while (iterator.hasNext()) {
                                    int position=(int)iterator.next();
                                    long id=list.get(position);
                                    sqLiteDatabase.execSQL("delete from download_table where downloadID="+String.valueOf(id));
                                    query.setFilterById(id);
                                    cursor=downloadManager.query(query);
                                    if (!cursor.moveToFirst()) {
                                        cursor.close();
                                    }
                                    else {
                                        downloadManager.remove(id);
                                        cursor.close();
                                    }
                                    Toast.makeText(Down_record.this, "删除成功", Toast.LENGTH_SHORT).show();
                                }

                                edit_button.setVisibility(View.VISIBLE);
                                clear_button.setVisibility(View.VISIBLE);
                                cancel_button.setVisibility(View.GONE);
                                delete_button.setVisibility(View.GONE);

                                download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});
                                if (!download_cursor.moveToNext()){
                                    clear_button.setEnabled(false);
                                    edit_button.setEnabled(false);
                                    down_listView.setVisibility(View.GONE);
                                }
                                else {
                                    clear_button.setEnabled(true);
                                    edit_button.setEnabled(true);
                                    download_cursor.moveToPrevious();
                                    adapter=new DownRecordBaseAdapter(Down_record.this,list=CursortoList(download_cursor),0);
                                    down_listView.setAdapter(adapter);
                                }
                            }
                        }
                    })
                    .setNeutralButton("删除任务", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!selectItems.isEmpty()){
                                sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});
                                list=CursortoList(download_cursor);
                                Iterator iterator=selectItems.iterator();
                                while (iterator.hasNext()) {
                                    int position=(int)iterator.next();
                                    long id=list.get(position);
                                    sqLiteDatabase.execSQL("delete from download_table where downloadID="+String.valueOf(id));
                                    query.setFilterById(id);
                                    cursor=downloadManager.query(query);
                                    if (!cursor.moveToFirst()) {
                                        cursor.close();
                                    }
                                    else {
                                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))!=DownloadManager.STATUS_SUCCESSFUL) {
                                            downloadManager.remove(id);
                                        }
                                        cursor.close();
                                    }
                                    Toast.makeText(Down_record.this, "删除成功", Toast.LENGTH_SHORT).show();
                                }

                                edit_button.setVisibility(View.VISIBLE);
                                clear_button.setVisibility(View.VISIBLE);
                                cancel_button.setVisibility(View.GONE);
                                delete_button.setVisibility(View.GONE);

                                sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});
                                if (!download_cursor.moveToNext()){
                                    clear_button.setEnabled(false);
                                    edit_button.setEnabled(false);
                                    down_listView.setVisibility(View.GONE);
                                }
                                else {
                                    clear_button.setEnabled(true);
                                    edit_button.setEnabled(true);
                                    download_cursor.moveToPrevious();
                                    adapter=new DownRecordBaseAdapter(Down_record.this,list=CursortoList(download_cursor),0);
                                    down_listView.setAdapter(adapter);
                                }
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }
    }

    public void cancel_down(View view){
        edit_button.setVisibility(View.VISIBLE);
        clear_button.setVisibility(View.VISIBLE);
        delete_button.setVisibility(View.GONE);
        cancel_button.setVisibility(View.GONE);

        sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
        download_cursor = sqLiteDatabase.rawQuery("select * from download_table where downloadID like ?", new String[]{"%"});

        adapter=new DownRecordBaseAdapter(this,list=CursortoList(download_cursor),0);
        down_listView.setAdapter(adapter);
    }
}
