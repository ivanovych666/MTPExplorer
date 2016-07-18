package com.ivanovych666.mtpexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;

public class UsbDeviceInfo {

	public static String getUsbDeviceClassName(UsbDevice device){
		String name;
		Integer deviceClass = device.getDeviceClass();
		Integer iCount = device.getInterfaceCount();
		
		if(deviceClass != UsbConstants.USB_CLASS_PER_INTERFACE || iCount == 0){
			return getUsbDeviceClassName(deviceClass);
		}else if(iCount == 1){
			return getUsbDeviceClassName(device.getInterface(0).getInterfaceClass());
		}else{
			name = "USB composite device (";
			for(Integer index = 0; index < iCount; index++){
				name = getUsbDeviceClassName(device.getInterface(index).getInterfaceClass());
				if(index < iCount) name += ", ";
			}
			name += ")";
		}
		
		return name;
	}
	
	public static String getUsbDeviceClassName(Integer deviceClass){
		String name;
		
		switch(deviceClass){
			case UsbConstants.USB_CLASS_APP_SPEC:
				name = "Application specific USB Device";
				break;
			case UsbConstants.USB_CLASS_AUDIO:
				name = "Audio device";
			break;
			case UsbConstants.USB_CLASS_CDC_DATA:
				name = "CDC (Communications Device Class) device";
			break;
			case UsbConstants.USB_CLASS_COMM:
				name = "Communication device";
			break;
			case UsbConstants.USB_CLASS_CONTENT_SEC:
				name = "Content security device";
			break;
			case UsbConstants.USB_CLASS_CSCID:
				name = "Content smart card device";
			break;
			case UsbConstants.USB_CLASS_HID:
				name = "Human interface device";
			break;
			case UsbConstants.USB_CLASS_HUB:
				name = "USB hub";
			break;
			case UsbConstants.USB_CLASS_MASS_STORAGE:
				name = "Mass storage device";
			break;
			case UsbConstants.USB_CLASS_MISC:
				name = "Wireless miscellaneous device";
			break;
			case UsbConstants.USB_CLASS_PER_INTERFACE:
				name = "USB_CLASS_PER_INTERFACE device";
			break;
			case UsbConstants.USB_CLASS_PHYSICA:
				name = "Physical device";
			break;
			case UsbConstants.USB_CLASS_PRINTER:
				name = "Printer";
			break;
			case UsbConstants.USB_CLASS_STILL_IMAGE:
				name = "Digital camera";
			break;
			case UsbConstants.USB_CLASS_VENDOR_SPEC:
				name = "Vendor specific device";
			break;
			case UsbConstants.USB_CLASS_VIDEO:
				name = "Video device";
			break;
			case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
				name = "Wireless controller device";
			break;
			default:
				name = "Unknown class USB device";
		}
		
		return name;
	}
	
	public static String getUsbDeviceLabel(Context context, UsbDevice device){
		InputStream is = context.getResources().openRawResource(R.raw.usb);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		String vendorName = null;
		String productName = null;
		String vendorId = String.format("%04x", device.getVendorId()) + "  ";
		String productId = "\t" + String.format("%04x", device.getProductId()) + "  ";
		
		try{
			while((line = br.readLine()) != null){
            	if(line.length() == 0) continue;
            	if(line.charAt(0) == '#') continue;
            	
            	if(vendorName == null){
            		if(line.startsWith(vendorId)){
                		vendorName = line.substring(6);
                	}
            	}else{
            		if(line.startsWith(productId)){
            			productName = line.substring(7);
            			break;
            		}else if(!line.startsWith("\t")) break;
            	}
            }
        }catch(IOException e){
        	
        }

		if (vendorName == null) {
			vendorName = "Unknown vendor";
		}

		if(productName == null){
			productName = "Unknown product";
		}

		return vendorName + " " + productName;
	}
	
}
