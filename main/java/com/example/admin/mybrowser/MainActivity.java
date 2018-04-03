package com.example.admin.mybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    public  static WebView show;
    public static SharedPreferences sharedPreferences;
    public static int CODE_CH=0,CODE_SETTING=1,CODE_NO=100;

    private Bitmap bitmap=null;
    private SharedPreferences.Editor editor;
    private RelativeLayout relativeLayout;
    private String skin_path;
    private EditText url;
    private String urlStr;
    private Button openNetAddress;
    private ProgressBar progressBar;
    private ImageButton backB,forwardB;
    private LinearLayout buttonGroup;
    private DownloadManager downloadManager;
    private View dialogSave;
    private EditText DownloadName;
    private TextView houzhui_view;
    private String name;
    private String houzhui;
    private PopWindowMenu popWindowMenu;
    private Dialog saveImageToChoosePath;
    private String path;
    private com.example.admin.mybrowser.ItemLongClickedPopWindow itemLongClickedPopWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayout=(RelativeLayout)findViewById(R.id.activity_main);
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        editor=sharedPreferences.edit();
        skin_path=sharedPreferences.getString("skin_path","");
        if (skin_path.equals("")) {
            relativeLayout.setBackground(getResources().getDrawable(R.drawable.background1));
        }
        else {
            bitmap=handle_bitmap(skin_path,320,420);
            relativeLayout.setBackground(new BitmapDrawable(bitmap));
        }
        Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==0x123){
                    skin_path=(String)msg.obj;
                    bitmap=handle_bitmap(skin_path,320,420);
                    relativeLayout.setBackground(new BitmapDrawable(bitmap));
                    editor.putString("skin_path",skin_path);
                    editor.commit();
                }
            }
        };
        MyApplication myApplication=(MyApplication)getApplication();
        myApplication.setHandler(handler);
        url=(EditText) findViewById(R.id.netAddress);
        openNetAddress=(Button) findViewById(R.id.openNetAddress);
        show=(WebView) findViewById(R.id.webView);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        backB= (ImageButton) findViewById(R.id.back);
        forwardB=(ImageButton) findViewById(R.id.forward);
        buttonGroup=(LinearLayout)findViewById(R.id.buttonGroup);
        WebSettings webSettings=show.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        show.requestFocusFromTouch();
        show.setBackgroundColor(0);

        show.loadUrl("file:///android_asset/test.html");

        downloadManager = (DownloadManager) MainActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        show.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                File root=new File("file:///mnt/sdcard/");
                if (root.exists()) {
                    path = "file:///mnt/sdcard/浏览器/下载";
                    root.mkdirs();
                }
                dialogSave = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_saveimg, null);
                DownloadName = (EditText) dialogSave.findViewById(R.id.dialog_fileName_input);
                houzhui_view=(TextView)dialogSave.findViewById(R.id.houzhui);
                if (MimeTypeMap.getSingleton().hasMimeType(mimetype)){
                    houzhui="."+MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype);
                }
                else {
                    houzhui="."+MimeTypeMap.getFileExtensionFromUrl(url);
                }
                name = URLUtil.guessFileName(url,null,null);
                if (name.endsWith(houzhui)){
                    name = name.substring(0, name.lastIndexOf("."));
                }
                DownloadName.setText(name);
                houzhui_view.setText(houzhui);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("文件下载")
                        .setView(dialogSave)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this,"正在下载...",Toast.LENGTH_SHORT).show();
                                DownloadManager.Request request = null;
                                request = new DownloadManager.Request(Uri.parse(url));
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                name=(String)DownloadName.getText().toString();
                                if (name.trim().equals("")){
                                    name="sky";
                                }
                                try {
                                    name = URLEncoder.encode(name,"UTF-8")
                                            .replace("+","%20");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                if (name.length()>20){
                                    name = name.substring(0,20);
                                }
                                // 设置通知的标题和描述
                                request.setTitle(name+houzhui);
                                request.setDescription("正在下载");
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                                request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/浏览器/下载",name+houzhui)));
                                long downloadId = downloadManager.enqueue(request);

                                MySQLiteopenHelper mySQLiteopenHelper = new MySQLiteopenHelper(MainActivity.this, "history.db3", 1);
                                SQLiteDatabase sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                sqLiteDatabase.execSQL("insert into download_table(downloadID) values(?)",
                                        new Long[]{downloadId});

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(dialogSave.getWindowToken(), 0);
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(dialogSave.getWindowToken(), 0);
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });

        show.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult result =((WebView)v).getHitTestResult();
                if (null == result)
                    return false;

                int type = result.getType();
                if (type == WebView.HitTestResult.UNKNOWN_TYPE)
                    return false;

                if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
                    return true;
                }

                switch (type) {
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                    case WebView.HitTestResult.IMAGE_TYPE:
                        // 处理长按图片的菜单项
                        itemLongClickedPopWindow = new com.example.admin.mybrowser.ItemLongClickedPopWindow(MainActivity.this,
                                com.example.admin.mybrowser.ItemLongClickedPopWindow.IMAGE_VIEW_POPUPWINDOW, 200, 250);
                        itemLongClickedPopWindow.showAtLocation(v, Gravity.CENTER,(int)v.getX(), (int)v.getY());
                        TextView viewImage = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImage);
                        TextView saveImage = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_saveImage);
                        TextView viewImageAttributes = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImageAttributes);
                        popWindowMenu = new PopWindowMenu(result.getType(), result.getExtra());
                        viewImage.setOnClickListener(popWindowMenu);
                        saveImage.setOnClickListener(popWindowMenu);
                        viewImageAttributes.setOnClickListener(popWindowMenu);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        show.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                urlStr=url;
                MainActivity.this.url.setText(urlStr);
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, "打开网页失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (show.canGoBack()){
                    backB.setBackground(getResources().getDrawable(R.drawable.back));
                }
                else {
                    backB.setBackground(getResources().getDrawable(R.drawable.back1));
                }
                if (show.canGoForward()){
                    forwardB.setBackground(getResources().getDrawable(R.drawable.forward));
                }
                else {
                    forwardB.setBackground(getResources().getDrawable(R.drawable.forward1));
                }
                if(url.equals("file:///android_asset/test.html")){
                    MainActivity.this.url.setText("");
                }
                else {
                    MainActivity.this.url.setText(view.getTitle());
                }
                if (!url.equals("file:///android_asset/test.html")) {
                    if (!sharedPreferences.getBoolean("no_trace",false)) {
                        MySQLiteopenHelper mySQLiteopenHelper = new MySQLiteopenHelper(MainActivity.this, "history.db3", 1);
                        SQLiteDatabase sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                        Cursor cursor = sqLiteDatabase.rawQuery("select * from history_table where url =?", new String[]{url});
                        if (!cursor.moveToNext()) {
                            sqLiteDatabase.execSQL("insert into history_table(name,url) values(?,?)", new String[]{view.getTitle(), url});
                        } else {
                            sqLiteDatabase.execSQL("delete from history_table where url=?", new String[]{url});
                            sqLiteDatabase.execSQL("insert into history_table(name,url) values(?,?)", new String[]{view.getTitle(), url});
                        }
                    }
                }
            }
        });
        show.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    public static Bitmap handle_bitmap(String path,int width,int height){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW /width, photoH /height);//通过比较获取较小的缩放比列

        bmOptions.inJustDecodeBounds = false;// 将inJustDecodeBounds置为false，设置bitmap的缩放比列
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(path,bmOptions);//再次decode获取bitmap
    }

    private class PopWindowMenu implements View.OnClickListener {

        private int type;
        private String value;

        public PopWindowMenu(int type, String value){
            this.type = type;
            this.value = value;
        }

        @Override
        public void onClick(View v) {
            itemLongClickedPopWindow.dismiss();
            if(v.getId()==R.id.item_longclicked_viewImage){
                //图片菜单-查看图片
                Intent image_show=new Intent(MainActivity.this,ImageShow.class);
                Bundle bundle=new Bundle();
                bundle.putString("url",value);
                image_show.putExtras(bundle);
                startActivity(image_show);
            }else if(v.getId()==R.id.item_longclicked_saveImage){
                //图片菜单-保存图片
                File root=new File("/mnt/sdcard/");
                if (root.exists()) {
                    path = "/mnt/sdcard/白夜浏览器/image";
                    root.mkdirs();
                }
                dialogSave = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_saveimg, null);
                DownloadName = (EditText) dialogSave.findViewById(R.id.dialog_fileName_input);
                houzhui_view=(TextView) dialogSave.findViewById(R.id.houzhui);
                if (value.lastIndexOf(".")!=-1) {
                    name = value.substring(value.lastIndexOf("/") + 1, value.lastIndexOf("."));
                    houzhui = value.substring(value.lastIndexOf("."));
                }
                else {
                    name = value.substring(value.lastIndexOf("/") + 1);
                    houzhui="";
                }
                DownloadName.setText(name);
                if (!houzhui.equals(".jpg")&&!houzhui.equals(".png")) {
                    houzhui=".jpg";
                }
                houzhui_view.setText(houzhui);
                saveImageToChoosePath = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("图片下载")
                        .setView(dialogSave)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this,"正在下载...",Toast.LENGTH_SHORT).show();
                                DownloadManager.Request request = null;
                                request = new DownloadManager.Request(Uri.parse(value));
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                name=(String)DownloadName.getText().toString();
                                if (name.trim().equals("")){
                                    name="sky";
                                }
                                try {
                                    name = URLEncoder.encode(name,"UTF-8")
                                            .replace("+","%20");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                if (name.length()>20){
                                    name = name.substring(0,20);
                                }
                                // 设置通知的标题和描述
                                request.setTitle(name+houzhui);
                                request.setDescription("正在下载");
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                                request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/浏览器/图片保存",name+houzhui)));
                                long downloadId = downloadManager.enqueue(request);

                                MySQLiteopenHelper mySQLiteopenHelper = new MySQLiteopenHelper(MainActivity.this, "history.db3", 1);
                                SQLiteDatabase sqLiteDatabase = mySQLiteopenHelper.getReadableDatabase();
                                sqLiteDatabase.execSQL("insert into download_table(downloadID) values(?)",
                                        new Long[]{downloadId});

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(dialogSave.getWindowToken(), 0);
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(dialogSave.getWindowToken(), 0);
                                }
                            }
                        })
                        .create();
                saveImageToChoosePath.show();
            }else if(v.getId()==R.id.item_longclicked_viewImageAttributes){
                //取消
            }
        }
    }

    public void go(View view){
        String urlStr=url.getText().toString();
        if (urlStr.trim().length()!=0) {
            //使软键盘消失
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (!URLUtil.isNetworkUrl(urlStr)) {
                urlStr = "http://www.baidu.com/s?word=" + urlStr;
            }
            show.loadUrl(urlStr);
        }
    }

    long firstTime=0;

    @Override
    public void onBackPressed() {
        if (!(findViewById(R.id.input)).isShown()){
            findViewById(R.id.input).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonGroup).setVisibility(View.VISIBLE);
        }
        else {
            if (show.canGoBack()) {
                show.goBack();
            } else {
                long secondTime=System.currentTimeMillis();
                if(secondTime-firstTime>2000){
                    Toast.makeText(MainActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                    firstTime=secondTime;
                }
                else {
                    finish();
                }
            }
        }
    }

    public void back(View view){
        if (show.canGoBack()) {
            show.goBack();
        }
    }

    public void forward(View view) {
        if (show.canGoForward()) {
            show.goForward();
        }
    }

    com.example.admin.mybrowser.Popupmenu myPopupMenu;

    public void menu(View view) {
        List<String> item_names; // 选项名称
        List<Integer> item_images; // 选项图标
        // 选项图标
        item_images = new ArrayList<>();
        item_images.add(R.drawable.add);
        item_images.add(R.drawable.mark_history);
        item_images.add(R.drawable.full_screen);
        item_images.add(R.drawable.refresh);
        item_images.add(R.drawable.setting);
        item_images.add(R.drawable.download);
        item_images.add(R.drawable.share);
        item_images.add(R.drawable.finish);
        //选项名称
        item_names = new ArrayList<String>();
        item_names.add("添加网址");
        item_names.add("收藏/历史");
        item_names.add("全屏");
        item_names.add("刷新");
        item_names.add("设置");
        item_names.add("下载");
        item_names.add("分享");
        item_names.add("退出");

        myPopupMenu = new Popupmenu(this,item_names,item_images);;
        myPopupMenu.setAnimationStyle(R.style.PopupAnimation);
        myPopupMenu.showAtLocation(findViewById(R.id.menu), Gravity.BOTTOM, 0, 0);
        myPopupMenu.gv_body.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent collect_intent=new Intent(MainActivity.this,Collect.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("name",((WebView) MainActivity.this.findViewById(R.id.webView)).getTitle());
                        bundle.putString("url",((WebView) MainActivity.this.findViewById(R.id.webView)).getUrl());
                        collect_intent.putExtras(bundle);
                        MainActivity.this.startActivity(collect_intent);
                        break;
                    case 1:
                        Intent c_h_intent=new Intent(MainActivity.this,CHShow.class);
                        MainActivity.this.startActivityForResult(c_h_intent,CODE_CH);
                        break;
                    case 2:
                        MainActivity.this.findViewById(R.id.input).setVisibility(View.GONE);
                        MainActivity.this.findViewById(R.id.buttonGroup).setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,"按返回键退出全屏",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        MainActivity.show.loadUrl(MainActivity.show.getUrl());
                        break;
                    case 4:
                        Intent intent=new Intent(MainActivity.this,WebPreference.class);
                        MainActivity.this.startActivityForResult(intent,CODE_SETTING);
                        break;
                    case 5:
                        Intent down_intent=new Intent(MainActivity.this,Down_record.class);
                        MainActivity.this.startActivity(down_intent);
                        break;
                    case 6:
                        WebView url=(WebView) MainActivity.this.findViewById(R.id.webView);
                        ClipboardManager cm = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(url.getUrl().toString());
                        Toast.makeText(MainActivity.this,"复制网址成功",Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        MainActivity.this.finish();
                        break;
                }
                myPopupMenu.dismiss();
            }
        });
    }

    public void mainwin(View view){
        show.loadUrl("file:///android_asset/test.html");
    }

    public void newwin(View view){
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==CODE_CH&&resultCode==CODE_CH){
            Bundle bundle=data.getExtras();
            String url=bundle.getString("url");
            show.loadUrl(url);
        }
        if (requestCode==CODE_SETTING&&resultCode==CODE_SETTING){
            switch (sharedPreferences.getString("picture_dis","3")){
                case "1":
                    show.getSettings().setBlockNetworkImage(true);
                    break;
                case "2":
                    ConnectivityManager mConnectivity= (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo info= mConnectivity.getActiveNetworkInfo();
                    int netType = info.getType();
                    if (netType == ConnectivityManager.TYPE_WIFI)
                        show.getSettings().setBlockNetworkImage(false);
                    else {
                        show.getSettings().setBlockNetworkImage(true);
                    }
                    break;
                case "3":
                    show.getSettings().setBlockNetworkImage(false);
                    break;
            }
            switch (sharedPreferences.getString("text_size","3")){
                case "1":
                    show.getSettings().setTextSize(WebSettings.TextSize.SMALLEST);
                    break;
                case "2":
                    show.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
                    break;
                case "3":
                    show.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
                    break;
                case "4":
                    show.getSettings().setTextSize(WebSettings.TextSize.LARGER);
                    break;
                case "5":
                    show.getSettings().setTextSize(WebSettings.TextSize.LARGEST);
                    break;
            }
            if(sharedPreferences.getBoolean("javascript",true)){
                show.getSettings().setJavaScriptEnabled(true);
            }
            else {
                show.getSettings().setJavaScriptEnabled(false);
            }
            if(sharedPreferences.getBoolean("cache",true)){
                show.getSettings().setAppCachePath(getFilesDir().getAbsolutePath());
                show.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //设置 缓存模式
                show.getSettings().setAppCacheEnabled(true);
            }
            else {
                show.getSettings().setAppCacheEnabled(false);
            }
        }
    }
}
