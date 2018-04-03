package com.example.admin.mybrowser;

import android.app.Application;
import android.os.Handler;

/**
 * Created by admin on 2017/3/16.
 */

public class MyApplication extends Application {
    private Handler handler = null;

    // set方法
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    // get方法
    public Handler getHandler() {
        return handler;
    }
}
