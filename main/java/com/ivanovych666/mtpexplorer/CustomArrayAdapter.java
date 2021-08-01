package com.ivanovych666.mtpexplorer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

public class CustomArrayAdapter extends ArrayAdapter<CustomArrayItem> {
	
	public CustomArrayAdapter(Context context){
		super(context, R.layout.browser_item, R.id.headline);
    }
	
	@Override
	public View getView(int pos, View convertView, ViewGroup parent){
		View view = super.getView(pos, convertView, parent);
		
        final BrowserActivity activity = (BrowserActivity) this.getContext();
		
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
		ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
		
        CustomArrayItem item = getItem(pos);
        checkBox.setTag(pos);
		
		if(convertView == null){
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int position = (Integer) buttonView.getTag();
					activity.setItemSelected(position, isChecked);
					notifyDataSetChanged();
				}
				
			});
			
		}
		
		checkBox.setVisibility(activity.selectionMode ? View.VISIBLE : View.GONE);
		checkBox.setChecked(item.checked);
		
		thumbnail.setImageBitmap(item.thumbnail);
		
	    return view;
	}
	
}
