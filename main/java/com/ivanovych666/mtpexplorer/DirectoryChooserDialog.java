package com.ivanovych666.mtpexplorer;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

public class DirectoryChooserDialog extends DialogFragment {
	
	private File currentFile = null;
	private DirectoryFileFilter directoryFileFilter = new DirectoryFileFilter();
	private ArrayAdapter<DirectoryChooserFile> adapter = null;
	private OnDirectoryChoseListener onDirectoryChoseListener = null;
	private static final String TAG = "DirectoryChooserDialog";
	
	public interface OnDirectoryChoseListener {

		public void onDirectoryChose(String dir);
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		
		dialogBuilder
			.setTitle("Choose Destination")
			.setPositiveButton("OK", new OnClickListener() {
				
		        @Override
		        public void onClick(DialogInterface dialog, int which) 
		        {
		            if(onDirectoryChoseListener != null){
		            	onDirectoryChoseListener.onDirectoryChose(currentFile.getAbsolutePath());
		            }
		            dialog.cancel();
		        }
		        
		    })
		    .setNeutralButton("New Folder", null)
		    .setNegativeButton("Cancel", new OnClickListener() {
				
		        @Override
		        public void onClick(DialogInterface dialog, int which) 
		        {
		            dialog.cancel();
		        }
		        
		    });
		
		adapter = new ArrayAdapter<DirectoryChooserFile>(
			this.getActivity(),
			android.R.layout.select_dialog_item,
			android.R.id.text1
		);
		
		dialogBuilder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setDestination(adapter.getItem(which).file);
			}
			
		});
		
		setDestination();
		
		return dialogBuilder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		AlertDialog dialog = (AlertDialog) getDialog();
		
		View btn = (View) dialog.getButton(Dialog.BUTTON_NEUTRAL);
		btn.setOnClickListener(new View.OnClickListener() {
			
	        @Override
	        public void onClick(View view) 
	        {
	        	Bundle args = new Bundle();
	        	args.putString("dir", currentFile.getAbsolutePath());
	        	
	        	NewDirectoryDialog newDirectoryDialog = new NewDirectoryDialog();
	        	newDirectoryDialog.setArguments(args);
	        	newDirectoryDialog.setOnDirectoryCreatedListener(new NewDirectoryDialog.OnDirectoryCreatedListener() {
					
					@Override
					public void onDirectoryCreate(String dir) {
						setDestination(dir);
					}
					
				});
	        	newDirectoryDialog.show(getFragmentManager(), "new directory");
	        }
	        
	    });
	}
	
	private void setDestination(){
		this.setDestination(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
		);
	}
	
	private void setDestination(String path){
		setDestination(
			new File(path)
		);
	}
	
	private void setDestination(File filename){
		currentFile = filename;
		refreshList();
	}
	
	private void refreshList(){
		adapter.clear();
		
		Log.d(TAG, "setDestination("+currentFile+")");
		
		File parent = currentFile.getParentFile();
		Log.d(TAG, "parent => " + parent);
		if(parent != null){
			adapter.add(new DirectoryChooserFile(parent, ".."));
		}
		
		File[] files = currentFile.listFiles(directoryFileFilter);
		Log.d(TAG, "files => " + files);
		
		if(files.length > 0){
			DirectoryChooserFile[] dcFiles = new DirectoryChooserFile[files.length];
			for(int i = 0; i < files.length; i++){
				dcFiles[i] = new DirectoryChooserFile(files[i]);
			}
			Arrays.sort(dcFiles);
			adapter.addAll(dcFiles);
		}
	}
	
	public void setOnDirectoryChoseListener(OnDirectoryChoseListener listener){
		onDirectoryChoseListener = listener;
	}
	
}
