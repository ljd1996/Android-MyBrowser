package com.example.admin.mybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 2017/3/17.
 */

public class ImageShow extends Activity{
    private String url;
    private ViewMatrix imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgsview);
        imageView=(ViewMatrix) findViewById(R.id.imgsview);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        url=bundle.getString("url");
        new imagecache(this).execute(url);
    }

    public class imagecache extends AsyncTask<String, String, Bitmap> {

        private Dialog dialog;
        private Context context;

        public imagecache(Context context){
            this.context = context;
        }

        /**
         * 在子线程执行前进行调用，比如显示一个进度条
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new AlertDialog.Builder(this.context)
                    .setMessage("正在加载...")
                    .create();
            this.dialog.show();
        }

        /**
         * 执行子线程，内容一般为比较耗时的操作，例如下载等
         * */
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap imgShow = null;
            try{
                URL url = new URL(params[0]);
                HttpURLConnection conn  = (HttpURLConnection)url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream inputStream=conn.getInputStream();
                imgShow = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            return imgShow;
        }

        /**
         * 执行完后台子线程后，运行完结操作
         * */
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            this.dialog.dismiss();
            imageView.setImageBitmap(result);
        }
    }
}
