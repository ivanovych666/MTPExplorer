package com.ivanovych666.mtpexplorer;

import android.content.Context;
import android.hardware.usb.UsbDevice;

public class UsbDeviceListItem {
	
	private UsbDevice usbDevice = null;
	private Context context = null;
	
	public UsbDeviceListItem(Context context, UsbDevice usbDevice){
		this.context = context;
		this.usbDevice = usbDevice;
	}
	
	public UsbDevice getUsbDevice(){
		return usbDevice;
	}
	
	public String toString(){
		return UsbDeviceInfo.getUsbDeviceLabel(context, usbDevice);
	}
	
}
