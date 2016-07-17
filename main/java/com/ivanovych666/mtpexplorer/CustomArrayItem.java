package com.ivanovych666.mtpexplorer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CustomArrayItem {
	
	public MtpFile file = null;
	public String name = null;
	public Bitmap thumbnail = null;
	public boolean checked = false;
	
	public CustomArrayItem(MtpFile mtpFile){
		file = mtpFile;
		name = mtpFile.getName();
		
		byte[] thumbnail = mtpFile.getThumbnail();
		if(thumbnail != null){
			this.thumbnail = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
		}
	}
	
	@Override
	public String toString(){
		return name;
	}
	
}
