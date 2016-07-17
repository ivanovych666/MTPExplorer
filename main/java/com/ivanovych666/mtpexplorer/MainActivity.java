package com.ivanovych666.mtpexplorer;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public MtpDevice connectedDevice = null;
	public String connectedDeviceName = "";
	public ArrayAdapter<UsbDeviceListItem> adapter = null;
	private static final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
	private static final String USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
	private static final String ACTION_USB_PERMISSION = "android.permission.USB_PERMISSION";
	public List<UsbDevice> usbDevices;
	
	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		setContentView(R.layout.activity_main);
		
		adapter = new ArrayAdapter<UsbDeviceListItem>(this, android.R.layout.simple_list_item_1);
		
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener(){
		 
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				
				UsbDeviceListItem item = (UsbDeviceListItem) parent.getAdapter().getItem(position);
				connectDevice(item.getUsbDevice());
				
			}
			
		});
		
		registerReceiver(usbBroadcastReceiver, new IntentFilter(ACTION_USB_PERMISSION));
		registerReceiver(usbBroadcastReceiver, new IntentFilter(USB_DEVICE_DETACHED));
		usbBroadcastReceiver.onReceive(this, getIntent());
		
		refreshList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		unregisterReceiver(usbBroadcastReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
	    switch (item.getItemId()) {
	        case R.id.action_refresh:
	        	refreshList();
	            break;
	    }
	    return true;
	}
	
	private final BroadcastReceiver usbBroadcastReceiver = new BroadcastReceiver() {

	    public void onReceive(Context context, Intent intent){
	    	
	    	UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	    	String action = intent.getAction();
	    		
    		if(action.equals(USB_DEVICE_ATTACHED)){
    			connectDevice(device);
    		}else if(action.equals(USB_DEVICE_DETACHED)){
    			refreshList();
    			showStatus("Device disconnected.");
    		}else if(action.equals(ACTION_USB_PERMISSION)){
                if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                	connectDevice(device);
                }else{
                	showStatus("Permission denied for chosen device.");
                }
    		}
	        
	    }
	    
	};
	
	public void refreshList(){
		Log.d(TAG, "actionScan");
		
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		
		adapter.clear();
		
		if(!deviceList.isEmpty()){
			for(UsbDevice device : deviceList.values()){
				adapter.add(new UsbDeviceListItem(this, device));
			}
		}
		
	}
	
	public void showStatus(String status){
		Toast.makeText(this, status, Toast.LENGTH_LONG).show();
	}
	
	public void connectDevice(UsbDevice device){
		Log.d(TAG, "connectDevice(" + device + ")");
		
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		if(!manager.hasPermission(device)){
			manager.requestPermission(
				device,
				PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0)
			);
			return;
		}
		
		/*
		connectedDevice = new MtpDevice(device);
		
		
		MtpDeviceInfo info = connectedDevice.getDeviceInfo();
		if(info.equals(null)){
			showStatus("Failed to get MTP Device Info.");
			return;
		}
		
		connectedDeviceName = info.getManufacturer();
		connectedDeviceName += " " + info.getModel();
		connectedDeviceName += " " + info.getSerialNumber();
		connectedDeviceName += " " + info.getVersion();
		*/
		
		Intent intent = new Intent(this, BrowserActivity.class);
		intent.putExtra("deviceName", device.getDeviceName());
	    startActivity(intent);
	}

}
