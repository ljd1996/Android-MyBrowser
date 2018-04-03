package com.example.admin.mybrowser;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CHBaseAdapter extends BaseAdapter {

	private HashMap<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private ArrayList<Map<String, String>> list;
	private int flag;
	protected LayoutInflater mInflater;
	private ViewHolder viewHolder=null;

	public CHBaseAdapter(Context context, ArrayList<Map<String, String>> list, int flag) {
		this.list = list;
		mInflater = LayoutInflater.from(context);
		this.flag=flag;
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
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.ch_listitem, null);
			viewHolder.item_name = (TextView) convertView.findViewById(R.id.item_name);
			viewHolder.item_url = (TextView) convertView.findViewById(R.id.item_url);
			viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder=(ViewHolder)convertView.getTag();
		}

        if (flag==1){
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!mSelectMap.containsKey(position) || !mSelectMap.get(position)){
                        addAnimation((CheckBox)buttonView);
                    }
                    mSelectMap.put(position, isChecked);
                }
            });
            viewHolder.mCheckBox.setChecked(mSelectMap.containsKey(position) ? mSelectMap.get(position) : false);
        }
        if (flag==0){
            viewHolder.mCheckBox.setVisibility(View.GONE);
        }
        viewHolder.item_name.setText(list.get(position).get("name"));
        viewHolder.item_url.setText(list.get(position).get("url"));

		return convertView;
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
		public TextView item_name,item_url;
		public CheckBox mCheckBox;
	}
}
