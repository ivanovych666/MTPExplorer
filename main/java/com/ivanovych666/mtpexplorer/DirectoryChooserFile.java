package com.ivanovych666.mtpexplorer;

import java.io.File;

import android.util.Log;

public class DirectoryChooserFile implements Comparable<DirectoryChooserFile>{

	public File file = null;
	public String alt = null;
	
	public DirectoryChooserFile(File filename){
		file = filename;
		alt = filename.getName();
	}
	
	public DirectoryChooserFile(File filename, String altname){
		Log.d("DirectoryChooserFile", "new DirectoryChooserFile("+filename+", "+altname+")");
		file = filename;
		alt = altname;
	}
	
	@Override
	public String toString(){
		return alt;
	}

	@Override
	public int compareTo(DirectoryChooserFile another) {
		return alt.compareTo(another.alt);
	}

}
