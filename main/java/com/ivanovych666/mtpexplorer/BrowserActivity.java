package com.ivanovych666.mtpexplorer;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDevice;
import android.mtp.MtpDeviceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class BrowserActivity extends Activity {
	
	private static final String TAG = "BrowserActivity";
	GridView gridView = null;
	CustomArrayAdapter adapter = null;
	MtpFile currentFile = null;
	boolean selectionMode = false;
	int selectedTotal = 0;
	private RetainedFragment retainedFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getFragmentManager();
		retainedFragment = (RetainedFragment) fm.findFragmentByTag("data");

		if (retainedFragment == null) {
			Log.d(TAG, "new RetainedFragment()");
			retainedFragment = new RetainedFragment();
			fm.beginTransaction().add(retainedFragment, "data").commit();
		} else {
			currentFile = retainedFragment.getData();
		}

		setContentView(R.layout.activity_browser);
		
		adapter = new CustomArrayAdapter(this);
		
		gridView = (GridView) findViewById(R.id.browser_grid);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener(){
			 
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				
				if(selectionMode){
					setItemSelected(position, !adapter.getItem(position).checked);
					adapter.notifyDataSetChanged();
				}else{
					MtpFile file = ((CustomArrayAdapter) parent.getAdapter()).getItem(position).file;
					if(file.isDirectory()){
						showFiles(file);
					}
				}
				
			}
			
		});
		
		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				setSelectionMode(!selectionMode);
				adapter.notifyDataSetChanged();
				
				return false;
			}
			
		});
		
		
		Log.d(TAG, "onCreate");

		if (currentFile != null) {
			showFiles(currentFile);
			return;
		}

		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		String deviceName = getIntent().getStringExtra("deviceName");
		UsbDevice usbDevice = manager.getDeviceList().get(deviceName);
		MtpDevice mtpDevice = new MtpDevice(usbDevice);
		MtpFile mtpFile = new MtpFile(mtpDevice);

		if(mtpFile.open(this)){
			MtpDeviceInfo info = mtpDevice.getDeviceInfo();
			if(info != null){
				ActionBar actionBar = getActionBar();
				actionBar.setTitle(info.getModel() + " (" + info.getManufacturer() + ")");
			}
			mtpFile.close();
		}

		showFiles(mtpFile);
	}
	
	@Override
	protected void onRestart(){
		super.onRestart();
		Log.d(TAG, "onRestart");
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Log.d(TAG, "onStart");
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG, "onRestoreInstanceState");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.d(TAG, "onResume"); 
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		Log.d(TAG, "onPause"); 
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		Log.d(TAG, "onStop"); 
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		retainedFragment.setData(currentFile);
		Log.d(TAG, "onDestroy");
	}
	
	public void setItemSelected(int position, boolean checked){
		Log.d(TAG, "setItemSelected("+position+"/"+adapter.getCount()+", "+checked+")");
		
		CustomArrayItem item = adapter.getItem(position);
		
		if(item.checked != checked){
			item.checked = checked;
			selectedTotal += checked ? 1 : -1;
		}
	}
	
	public void showFiles(MtpFile file){
		Log.d(TAG, "showFiles");
		currentFile = file;
		adapter.clear();
		if(currentFile.open(this)){
			Log.d(TAG, "folder opened");
			MtpFile[] mtpFiles = currentFile.listFiles();
			if(mtpFiles.equals(null)){
				Log.d(TAG, "folder is empty");
				Toast toast = Toast.makeText(this, "The folder is empty.", Toast.LENGTH_LONG);
				toast.show();
			}else{
				Log.d(TAG, "folder total childs: " + mtpFiles.length);
				for(int i = 0, l = mtpFiles.length; i < l; i++){
					Log.d(TAG, "file => " + mtpFiles[i]);
					adapter.add(new CustomArrayItem(mtpFiles[i]));
				}
			}
			Log.d(TAG, "close folder");
			currentFile.close();
		}else{
			Log.d(TAG, "folder not opened");
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	if(selectionMode){
	    		setSelectionMode(false);
	    		adapter.notifyDataSetChanged();
	    		return false;
	    	}else if(!currentFile.equals(null) && !currentFile.isRoot()){
	    		showFiles(currentFile.getParent());
		        return false;	
	    	}
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	public void setSelectionMode(boolean status){
		Log.d(TAG, "setSelectionMode");
		
		selectionMode = status;
		selectedTotal = 0;
		
		int count = adapter.getCount();
		CustomArrayItem item = null;
		for(int index = 0; index < count; index++){
			item = adapter.getItem(index);
			item.checked = false;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.browser, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG, "onPrepareOptionsMenu");
		Log.d(TAG, "selectionMode => " + selectionMode);
		Log.d(TAG, "selectedTotal => " + selectedTotal);
		Log.d(TAG, "childCount => " + gridView.getCount());
		
		boolean notAllSelected = selectedTotal < gridView.getCount();
		boolean hasSelected = selectedTotal > 0;
		
		menu.setGroupVisible(R.id.notAllSelected, notAllSelected);
		menu.setGroupVisible(R.id.hasSelected, hasSelected);
		return notAllSelected || hasSelected;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		final Context context = this;
		final int count = adapter.getCount();
		
		switch (item.getItemId()) {
			case R.id.copySelection:
				DirectoryChooserDialog dialog = new DirectoryChooserDialog();
				dialog.setOnDirectoryChoseListener(new DirectoryChooserDialog.OnDirectoryChoseListener(){

					@Override
					public void onDirectoryChose(String filename) {
						Log.d(TAG, "Directory Choosed => " + filename);
						
						ArrayList<MtpFile> filesList = new ArrayList<MtpFile>();
						CustomArrayItem item = null;
						
						for(int index = 0; index < count; index++){
							item = adapter.getItem(index);
							
							if(item.checked && item.file.isFile()){
								filesList.add(item.file);
							}
						}
						
						MtpFile[] files = new MtpFile[filesList.size()];
						files = filesList.toArray(files);
						
						ProgressDialog progressDialog = new ProgressDialog(context);
						CopyFilesTask task = new CopyFilesTask(progressDialog, filename);
						task.execute(files);
						
					}
				
				});
				dialog.show(getFragmentManager(), "directories");
			break;
			case R.id.selectAll:
				
				setSelectionMode(true);
				for(int position = 0; position < count; position++){
					setItemSelected(position, true);
				}
				adapter.notifyDataSetChanged();
				
				
			break;
			case R.id.selectNone:
				
				for(int position = 0; position < count; position++){
					setItemSelected(position, false);
				}
				adapter.notifyDataSetChanged();
				
			break;
		}
		return true;
	}
	
}
