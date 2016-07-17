package com.ivanovych666.mtpexplorer;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class NewDirectoryDialog extends DialogFragment {

	private EditText input = null;
	private String dir = null;
	private OnDirectoryCreatedListener onDirectoryCreatedListener = null;
	
	public interface OnDirectoryCreatedListener{
		
		public void onDirectoryCreate(String dir);
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Activity activity = getActivity();
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		
		input = new EditText(activity);
		dir = getArguments().getString("dir");
		
		dialogBuilder
			.setTitle("New Folder")
			.setView(input)
			.setPositiveButton("Create", new OnClickListener() {
				
		        @Override
		        public void onClick(DialogInterface dialog, int which) 
		        {
		        	String text = input.getText().toString();
		        	if(text.isEmpty()){
		        		Toast.makeText(getActivity(), "Filenam is missing.", Toast.LENGTH_LONG).show();
		        		return;
		        	}else{
		        		
		        		File file = new File(dir, text);
		        		if(file.mkdir() || file.isDirectory()){
		        			
		        			if(onDirectoryCreatedListener != null){
		        				onDirectoryCreatedListener.onDirectoryCreate(file.getAbsolutePath());
		        			}
		        			dialog.cancel();
		        			
		        		}else{
		        			Toast.makeText(getActivity(), "Failed to create directory.", Toast.LENGTH_LONG).show();
		        		}
		        		
		        	}
		        }
		        
		    })
			.setNegativeButton("Cancel", null);
		
		return dialogBuilder.create();
	}

	
	public void setOnDirectoryCreatedListener(OnDirectoryCreatedListener listener){
		onDirectoryCreatedListener = listener;
	}

}
