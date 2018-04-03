package com.example.admin.mybrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;

public class ChildAdapter extends BaseAdapter {
	private Point mPoint = new Point(0, 0);

	private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private GridView mGridView;
	private List<String> list;
	protected LayoutInflater mInflater;

	public ChildAdapter(Context context, List<String> list, GridView mGridView) {
		this.list = list;
		this.mGridView = mGridView;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		String path = list.get(position);
		
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.grid_child_item, null);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.child_image);

			viewHolder.mImageView.setOnMeasureListener(new MyImageView.OnMeasureListener() {
				@Override
				public void onMeasureSize(int width, int height) {
					mPoint.set(width, height);
				}
			});
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.mImageView.setImageResource(R.drawable.pictures_no);
		}
		viewHolder.mImageView.setTag(path);

		Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint, new NativeImageLoader.NativeImageCallBack() {
			
			@Override
			public void onImageLoader(Bitmap bitmap, String path) {
				ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
				if(bitmap != null && mImageView != null){
					mImageView.setImageBitmap(bitmap);
				}
			}
		});
		
		if(bitmap != null){
			viewHolder.mImageView.setImageBitmap(bitmap);
		}else{
			viewHolder.mImageView.setImageResource(R.drawable.pictures_no);
		}
		
		return convertView;
	}
	
	public static class ViewHolder{
		public MyImageView mImageView;
	}
}
