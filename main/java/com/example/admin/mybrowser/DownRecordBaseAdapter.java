package com.example.admin.mybrowser;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by admin on 2017/4/2.
 */

public class DownRecordBaseAdapter extends BaseAdapter {

    private final int SUCCESSFUL=1;
    private final int FAILED=2;
    private final int PAUSED=3;
    private final int PENDING=4;
    private final int RUNNING=5;

    private Context context;
    private ListView listView;
    private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
    private ArrayList<Long> id_list;
    private int flag;
    private LayoutInflater mInflater;
    private DownRecordBaseAdapter.ViewHolder viewHolder=null;

    public DownRecordBaseAdapter(Context context, ArrayList<Long> id_list,int flag){
        this.context=context;
        this.id_list=id_list;
        this.flag=flag;
        this.mInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return id_list.size();
    }

    @Override
    public Object getItem(int position) {
        return id_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        listView=(ListView)parent;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.down_listitem, null);
            viewHolder = new DownRecordBaseAdapter.ViewHolder();
            viewHolder.file_name = (TextView) convertView.findViewById(R.id.file_name);
            viewHolder.down_description = (TextView) convertView.findViewById(R.id.down_description);
            viewHolder.down_checkBox = (CheckBox) convertView.findViewById(R.id.down_checkBox);
            viewHolder.down_progress = (RoundProgressBar) convertView.findViewById(R.id.down_progress);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        viewHolder.down_progress.setTag(id_list.get(position));
        viewHolder.down_description.setTag(String.valueOf(id_list.get(position)));
        viewHolder.down_progress.setVisibility(View.GONE);

        if (flag==1){
            viewHolder.down_checkBox.setVisibility(View.VISIBLE);
            viewHolder.down_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!mSelectMap.containsKey(position) || !mSelectMap.get(position)){
                        addAnimation((CheckBox)buttonView);
                    }
                    mSelectMap.put(position, isChecked);
                }
            });
            viewHolder.down_checkBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);
        }
        if (flag==0){
            viewHolder.down_checkBox.setVisibility(View.GONE);
        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id_list.get(position));
        Cursor cursor = downloadManager.query(query);

        if (!cursor.moveToFirst()) {
            cursor.close();
            viewHolder.file_name.setText("文件不存在");
            viewHolder.down_description.setText("该文件可能已被删除");
        }
        else {
            switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    String filePath=cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    viewHolder.file_name.setText(filePath.substring(filePath.lastIndexOf("/")+1));
                    viewHolder.down_description.setText(byteTran(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))) + "  下载成功");
                    cursor.close();
                    break;
                case DownloadManager.STATUS_FAILED:
                    String filePath1=cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    viewHolder.file_name.setText(filePath1.substring(filePath1.lastIndexOf("/")+1));
                    viewHolder.down_description.setText("下载失败");
                    cursor.close();
                    break;
                case DownloadManager.STATUS_PENDING:
                    String filePath2=cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    viewHolder.file_name.setText(filePath2.substring(filePath2.lastIndexOf("/")+1));
                    viewHolder.down_description.setText("等待中...");
                    cursor.close();
                    break;
                case DownloadManager.STATUS_PAUSED:
                    String filePath3=cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    viewHolder.file_name.setText(filePath3.substring(filePath3.lastIndexOf("/")+1));
                    viewHolder.down_description.setText("暂停中...");
                    cursor.close();
                    break;
                case DownloadManager.STATUS_RUNNING:
                    String filePath4=cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    if (filePath4.isEmpty()){
                        viewHolder.file_name.setText("文件名解析出错");
                    }
                    else {
                        viewHolder.file_name.setText(filePath4.substring(filePath4.lastIndexOf("/") + 1));
                    }
                    Timer timer = new Timer();
                    MyHandle handler = new MyHandle(timer,listView, cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            , id_list.get(position));
                    timer.schedule(new MyTimerTask(context, id_list.get(position), handler), 0, 100);
                    //MyRunnable myRunnable=new MyRunnable(context,id_list.get(position),handler);
                    //ScheduledFuture<?> scheduledFuture=Down_record.sScheduledExecutorService.scheduleWithFixedDelay(myRunnable,0,100, TimeUnit.MILLISECONDS);
                    break;
            }
        }
        return convertView;
    }

    class MyHandle extends Handler{
        private Timer timer;
       // private ScheduledFuture<?> scheduledFuture;
        private ListView parent;
        private long totalSize;
        private long id;

        public MyHandle(Timer timer,ListView listView,long totalSize,long id){
            this.timer=timer;
            //this.scheduledFuture=scheduledFuture;
            this.parent=listView;
            this.totalSize=totalSize;
            this.id=id;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUCCESSFUL:
                    updateView(id,parent,true, byteTran(totalSize) + "  下载成功", 0);
                    //scheduledFuture.cancel(true);
                    timer.cancel();
                    break;
                case FAILED:
                    updateView(id,parent,true, "下载失败", 0);
                    //scheduledFuture.cancel(true);
                    timer.cancel();
                    break;
                case PAUSED:
                    updateView(id,parent,true, "暂停中...", 0);
                    //scheduledFuture.cancel(true);
                    timer.cancel();
                    break;
                case PENDING:
                    updateView(id,parent,true, "等待中...", 0);
                   // scheduledFuture.cancel(true);
                    timer.cancel();
                    break;
                case RUNNING:
                    String description = DownRecordBaseAdapter.byteTran((long) (msg.obj)) + "/" + DownRecordBaseAdapter.byteTran(totalSize);
                    int progress = (int) (((long) msg.obj * 100) / totalSize);
                    if (progress<0){
                        progress=0;
                    }
                    updateView(id,parent,false, description, progress);
                    break;
            }
        }
    }

    class MyTimerTask extends TimerTask{
        private Cursor cursor;
        private DownloadManager manager;
        private DownloadManager.Query query;
        public  long id;
        private Handler handler;
        public MyTimerTask(Context context,long id,Handler handler){
            this.manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            this.query = new DownloadManager.Query();
            this.id=id;
            this.handler=handler;
        }
        @Override
        public void run() {
            query.setFilterById(id);
            cursor = manager.query(query);
            if (!cursor.moveToFirst()){
                cursor.close();
            }
            else {
                switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        handler.sendEmptyMessage(SUCCESSFUL);
                        cursor.close();
                        break;
                    case DownloadManager.STATUS_FAILED:
                        handler.sendEmptyMessage(FAILED);
                        cursor.close();
                        break;
                    case DownloadManager.STATUS_PENDING:
                        handler.sendEmptyMessage(PENDING);
                        cursor.close();
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        handler.sendEmptyMessage(PAUSED);
                        cursor.close();
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        long currentSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        Message message = new Message();
                        message.what = RUNNING;
                        message.obj = currentSize;
                        handler.sendMessage(message);
                        cursor.close();
                        break;
                }
            }
        }
    }

    class MyRunnable implements Runnable {
        private Cursor cursor;
        private DownloadManager manager;
        private DownloadManager.Query query;
        private   long id;
        private Handler handler;

        public MyRunnable(Context context,long id,Handler handler){
            this.manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            this.query = new DownloadManager.Query();
            this.id=id;
            this.handler=handler;
        }
        @Override
        public void run() {
            query.setFilterById(id);
            cursor = manager.query(query);
            cursor.moveToFirst();
            switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    handler.sendEmptyMessage(SUCCESSFUL);
                    cursor.close();
                    break;
                case DownloadManager.STATUS_FAILED:
                    handler.sendEmptyMessage(FAILED);
                    cursor.close();
                    break;
                case DownloadManager.STATUS_PENDING:
                    handler.sendEmptyMessage(PENDING);
                    cursor.close();
                    break;
                case DownloadManager.STATUS_PAUSED:
                    handler.sendEmptyMessage(PAUSED);
                    cursor.close();
                    break;
                case DownloadManager.STATUS_RUNNING:
                    long currentSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    Message message = new Message();
                    message.what = RUNNING;
                    message.obj = currentSize;
                    handler.sendMessage(message);
                    cursor.close();
                    break;
            }
        }
    }

    public static String byteTran(long bytes){
        DecimalFormat decimalFormat =new DecimalFormat("0.00");
        if (bytes<1024){
            return String.valueOf(bytes)+"b";
        }
        else if (bytes>1024&&bytes<1024*1024){
            return decimalFormat.format(bytes/1024f)+"Kb";
        }
        else {
            return decimalFormat.format(bytes/(1024f*1024f))+"Mb";
        }
    }

    public void updateView(long id,ListView listView,boolean isOK,String description,int progress){
        RoundProgressBar roundProgressBar=(RoundProgressBar)listView.findViewWithTag(id);
        TextView textView=(TextView)listView.findViewWithTag(String.valueOf(id));
        if (roundProgressBar!=null&&textView!=null){
            if (isOK){
                textView.setText(description);
                roundProgressBar.setVisibility(View.GONE);
            }
            else {
               textView.setText(description);
                roundProgressBar.setVisibility(View.VISIBLE);
                roundProgressBar.setProgress(progress);
            }
        }
    }

    private void addAnimation(CheckBox view){
        if (view.isChecked()) {
            float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
            AnimatorSet set = new AnimatorSet();
            set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                    ObjectAnimator.ofFloat(view, "scaleY", vaules));
            set.setDuration(150);
            set.start();
        }
    }

    public List<Integer> getSelectItems(){
        List<Integer> list = new ArrayList<Integer>();
        for(Iterator<Map.Entry<Integer, Boolean>> it = mSelectMap.entrySet().iterator(); it.hasNext();){
            Map.Entry<Integer, Boolean> entry = it.next();
            if(entry.getValue()){
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public static class ViewHolder{
        public TextView file_name,down_description;
        public CheckBox down_checkBox;
        public RoundProgressBar down_progress;
    }
}
