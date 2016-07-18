package com.ivanovych666.mtpexplorer;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDevice;
import android.mtp.MtpDeviceInfo;
import android.net.Uri;
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
	String actionBarTitle = null;

	UsbDevice usbDevice = null;
	MtpDevice mtpDevice = null;
	boolean mtpDeviceOpened = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		setContentView(R.layout.activity_browser);

		adapter = new CustomArrayAdapter(this);

		gridView = (GridView) findViewById(R.id.browser_grid);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (selectionMode) {
					setItemSelected(position, !adapter.getItem(position).checked);
					adapter.notifyDataSetChanged();
				} else {
					MtpFile file = ((CustomArrayAdapter) parent.getAdapter()).getItem(position).file;
					if (file.isDirectory()) {
						showFiles(file);
					}
				}

			}

		});

		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				setSelectionMode(!selectionMode);
				adapter.notifyDataSetChanged();

				return false;
			}

		});

		retainedFragment = (RetainedFragment) getFragmentManager().findFragmentByTag("data");

		if (retainedFragment == null) {

			retainedFragment = new RetainedFragment();
			getFragmentManager()
					.beginTransaction()
					.add(retainedFragment, "data")
					.commit();
		}

		if (retainedFragment.mtpFile == null) {
			String deviceName = getIntent().getStringExtra("deviceName");
			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			usbDevice = usbManager.getDeviceList().get(deviceName);
			mtpDevice = new MtpDevice(usbDevice);
			currentFile = new MtpFile(mtpDevice);
		} else {
			usbDevice = retainedFragment.usbDevice;
			mtpDevice = retainedFragment.mtpDevice;
			currentFile = retainedFragment.mtpFile;
		}

		try {

			mtpDeviceOpen();

		} catch (Exception e) {

			Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			toast.show();
			finish();
			return;

		}

		String title = null;

		if (savedInstanceState != null) {
			title = savedInstanceState.getString("actionBarTitle");
		}

		if (title == null) {
			setActionBarTitle(currentFile.getDevice());
		} else {
			setActionBarTitle(title);
		}

		showFiles(currentFile);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG, "onRestoreInstanceState");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("actionBarTitle", actionBarTitle);
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");

		retainedFragment.usbDevice = usbDevice;
		retainedFragment.mtpDevice = mtpDevice;
		retainedFragment.mtpFile = currentFile;

		mtpDeviceClose();

		super.onDestroy();
	}

	public void setItemSelected(int position, boolean checked) {
		Log.d(TAG, "setItemSelected(" + position + "/" + adapter.getCount() + ", " + checked + ")");

		CustomArrayItem item = adapter.getItem(position);

		if (item.checked != checked) {
			item.checked = checked;
			selectedTotal += checked ? 1 : -1;
		}
	}

	public void showFiles(MtpFile file) {
		Log.d(TAG, "showFiles(" + file + ")");

		currentFile = file;
		adapter.clear();

		MtpFile[] mtpFiles = currentFile.listFiles();
		if (mtpFiles.equals(null)) {
			Log.d(TAG, "folder is empty");
			Toast toast = Toast.makeText(this, "The folder is empty.", Toast.LENGTH_LONG);
			toast.show();
		} else {
			Log.d(TAG, "folder total childs: " + mtpFiles.length);
			for (int i = 0, l = mtpFiles.length; i < l; i++) {
				Log.d(TAG, "file => " + mtpFiles[i]);
				adapter.add(new CustomArrayItem(mtpFiles[i]));
			}
		}
		Log.d(TAG, "close folder");


	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (selectionMode) {
				setSelectionMode(false);
				adapter.notifyDataSetChanged();
				return false;
			} else if (!currentFile.equals(null) && !currentFile.isRoot()) {
				showFiles(currentFile.getParent());
				return false;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public void setSelectionMode(boolean status) {
		Log.d(TAG, "setSelectionMode");

		selectionMode = status;
		selectedTotal = 0;

		int count = adapter.getCount();
		CustomArrayItem item = null;
		for (int index = 0; index < count; index++) {
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
				dialog.setOnDirectoryChoseListener(new DirectoryChooserDialog.OnDirectoryChoseListener() {

					@Override
					public void onDirectoryChose(String filename) {
						Log.d(TAG, "Directory Choosed => " + filename);

						ArrayList<MtpFile> filesList = new ArrayList<MtpFile>();
						int length = 0;
						CustomArrayItem item = null;

						for (int index = 0; index < count; index++) {
							item = adapter.getItem(index);

							if (item.checked && item.file.isFile()) {
								length++;
								filesList.add(item.file);
							}
						}

						final MtpFile[] files = filesList.toArray(new MtpFile[0]);

						new CopyFilesTask(context, filename)
								.setOnResultListener(new CopyFilesTask.OnResultListener() {

									@Override
									public void onResult(Integer result) {

										String msg = "Copied " + result + " file" + (result != 1 ? "s" : "") + " of " + files.length + ".";
										Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
										toast.show();

									}

								})
								.execute(files);

					}

				});
				dialog.show(getFragmentManager(), "directories");
				break;
			case R.id.selectAll:

				setSelectionMode(true);
				for (int position = 0; position < count; position++) {
					setItemSelected(position, true);
				}
				adapter.notifyDataSetChanged();


				break;
			case R.id.selectNone:

				for (int position = 0; position < count; position++) {
					setItemSelected(position, false);
				}
				adapter.notifyDataSetChanged();

				break;
		}
		return true;
	}

	public void setActionBarTitle(MtpDevice device) {
		Log.d(TAG, "setActionBarTitle(" + device + ")");
		String vendorName = null;
		String productName = null;

		MtpDeviceInfo info = device.getDeviceInfo();
		if (info != null) {
			vendorName = info.getManufacturer();
			productName = info.getModel();
		}

		if (vendorName == null) {
			vendorName = "Unknown vendor";
		}

		if (productName == null) {
			productName = "Unknown product";
		}

		setActionBarTitle(vendorName + " " + productName);
	}

	public void setActionBarTitle(String title) {
		Log.d(TAG, "setActionBarTitle(" + title + ")");
		actionBarTitle = title;
		getActionBar().setTitle(actionBarTitle);
	}



	private void mtpDeviceOpen() throws Exception {
		Log.d(TAG, "mtpDeviceOpen");

		UsbManager manager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

		if(!manager.hasPermission(usbDevice)){
			throw new Exception("Permissions denied for USB Device.");
		}

		UsbDeviceConnection connection = manager.openDevice(usbDevice);
		if(connection == null){
			throw new Exception("Failed to connect to USB Device.");
		}

		if(!mtpDevice.open(connection)){
			throw new Exception("Failed to open USB Device as MTP.");
		}

		mtpDeviceOpened = true;

	}

	private void mtpDeviceClose(){
		Log.d(TAG, "mtpDeviceClose");
		if (mtpDeviceOpened) {
			mtpDevice.close();
			mtpDeviceOpened = false;
		}
	}

}
