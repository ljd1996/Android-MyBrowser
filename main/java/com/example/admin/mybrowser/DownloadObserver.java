package com.example.admin.mybrowser;

import android.app.DownloadManager;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

/**
 * Created by admin on 2017/4/4.
 */

public class DownloadObserver extends ContentObserver {
    private Handler mHandler;
    private Context mContext;
    private DownloadManager mDownloadManager;
    private DownloadManager.Query query;
    private Cursor cursor;
    private Message message;
    private long currentSize;

    public DownloadObserver(Handler handler, Context context, long downId) {
        super(handler);
        this.mHandler = handler;
        this.mContext = context;
        mDownloadManager =  (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        query = new DownloadManager.Query().setFilterById(downId);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        cursor  = mDownloadManager.query(query);
        cursor.moveToFirst();
        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
            mHandler.sendEmptyMessage(0);
            cursor.close();
        }
        else {
            currentSize= cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            cursor.close();
            message = new Message();
            message.what = 0x123;
            message.obj=currentSize;
            mHandler.sendMessage(message);
        }
    }
}
