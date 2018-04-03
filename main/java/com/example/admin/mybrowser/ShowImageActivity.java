package com.example.admin.mybrowser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.List;

public class ShowImageActivity extends Activity {
	private GridView mGridView;
	private List<String> list;
	private ChildAdapter adapter;
	private MyApplication myApplication;
	private Handler handler;
	private PopupWindow popupWindow;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_image_activity);
		View view=getLayoutInflater().inflate(R.layout.cache_pop,null);
		popupWindow=new PopupWindow(view,320,250);
		popupWindow.setAnimationStyle(R.style.PopupAnimation);
		mGridView = (GridView) findViewById(R.id.child_grid);
		list = getIntent().getStringArrayListExtra("data");
		myApplication=(MyApplication) getApplication();
		handler=myApplication.getHandler();

		adapter = new ChildAdapter(this, list, mGridView);
		mGridView.setAdapter(adapter);

		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (popupWindow.isShowing()){
					popupWindow.dismiss();
				}
				else {
					popupWindow.showAtLocation(view, Gravity.CENTER,0,0);
					path=list.get(position);
				}
			}
		});
	}

	public void sure(View view){
		Message message = new Message();
		message.obj = path;
		message.what = 0x123;
		handler.sendMessage(message);
		Toast.makeText(ShowImageActivity.this, "皮肤更换成功", Toast.LENGTH_SHORT).show();
		popupWindow.dismiss();
	}

	public void no(View view){
		popupWindow.dismiss();
	}

	@Override
	public void onBackPressed() {
		if (popupWindow.isShowing()){
			popupWindow.dismiss();
		}
		else {
			super.onBackPressed();
		}
	}
}
