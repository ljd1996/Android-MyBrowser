package com.example.admin.mybrowser;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by admin on 2016/12/3.
 */

public class Popupmenu extends PopupWindow {
    public GridView gv_body;
    private Context context;
    private LinearLayout linearLayout;

    public Popupmenu(final Context context, final List<String> item_names, final List<Integer> item_images) {
        super(context);
        this.context=context;
        linearLayout= new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        BaseAdapter baseAdapter=new BaseAdapter() {
            @Override
            public int getCount() {
                return item_images.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout layout = new LinearLayout(Popupmenu.this.context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.CENTER);

                TextView tv_item = new TextView(Popupmenu.this.context);
                tv_item.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv_item.setGravity(Gravity.CENTER);
                tv_item.setTextColor(Color.WHITE);
                tv_item.setPadding(10, 10, 10, 10);
                tv_item.setText((CharSequence) item_names.get(position));

                ImageView img_item = new ImageView(Popupmenu.this.context);
                img_item.setLayoutParams(new ViewGroup.LayoutParams(50, 50));
                img_item.setImageResource(item_images.get(position));

                layout.addView(img_item);
                layout.addView(tv_item);
                return layout;
            }
        };
        gv_body = new GridView(context);
        gv_body.setNumColumns(4);
        gv_body.setBackgroundColor(Color.TRANSPARENT);
        gv_body.setPadding(0, 10, 0, 10);
        gv_body.setAdapter(baseAdapter);
        linearLayout.addView(gv_body);
        setContentView(linearLayout);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
    }
}
