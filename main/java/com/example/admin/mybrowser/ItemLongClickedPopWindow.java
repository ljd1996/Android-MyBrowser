package com.example.admin.mybrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

public class ItemLongClickedPopWindow extends PopupWindow{

	public static final int IMAGE_VIEW_POPUPWINDOW = 1;
	
    private LayoutInflater itemLongClickedPopWindowInflater;
    private View itemLongClickedPopWindowView;
    private Context context;
    private int type;

    public ItemLongClickedPopWindow(Context context, int type, int width, int height){
    	super(context);
    	this.context = context;
    	this.type = type;
    	
    	//创建
    	this.initTab();
    	
    	//设置默认选项
    	setWidth(width);
    	setHeight(height);
    	setContentView(this.itemLongClickedPopWindowView);
    	setOutsideTouchable(true);
    	setFocusable(true);
    }

    private void initTab(){
    	this.itemLongClickedPopWindowInflater = LayoutInflater.from(this.context);
    	switch(type){
    	case IMAGE_VIEW_POPUPWINDOW:
    		//图片
    		this.itemLongClickedPopWindowView = this.itemLongClickedPopWindowInflater.inflate(R.layout.list_item_longclicked_img, null);
    		break;
    	default:
			break;
    	}
    }
    
    public View getView(int id){
    	return this.itemLongClickedPopWindowView.findViewById(id);
    }
}
