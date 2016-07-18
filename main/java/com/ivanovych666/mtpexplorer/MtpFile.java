package com.ivanovych666.mtpexplorer;

import java.util.Arrays;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.mtp.MtpConstants;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.mtp.MtpStorageInfo;
import android.util.Log;
import android.widget.Toast;

public class MtpFile {
	
	private final static int CLASS_DEVICE = 0;
	private final static int CLASS_STORAGE = 1;
	private final static int CLASS_FOLDER = 2;
	private final static int CLASS_FILE = 3;

	private int entryClass = -1;
	private Object entry = null;
	private MtpDevice device = null;
	private MtpFile parentFile = null;

	private static final String TAG = "MtpFile";
	
	public MtpFile(MtpDevice mtpDevice){
		entryClass = CLASS_DEVICE;
		device = mtpDevice;
		entry = mtpDevice;
	}
	
	public MtpFile(MtpStorageInfo storage, MtpFile parent){
		entryClass = CLASS_STORAGE;
		entry = storage;
		parentFile = parent;
		device = parent.device;
	}
	
	public MtpFile(MtpObjectInfo object, MtpFile parent){
		entryClass = object.getFormat() == MtpConstants.FORMAT_ASSOCIATION
				? CLASS_FOLDER : CLASS_FILE;
		entry = object;
		parentFile = parent;
		device = parent.device;
	}

	public MtpDevice getDevice(){
		return device;
	}

	public MtpFile getParent(){
		return parentFile;
	}
	
	public boolean isRoot(){
		return entryClass == CLASS_DEVICE;
	}
	
	public boolean isDirectory(){
		return entryClass != CLASS_FILE;
	}
	
	public boolean isFile(){
		return entryClass == CLASS_FILE;
	}
	
	public String getName(){
		String result = "";
		
		switch(entryClass){
		case CLASS_STORAGE:
			
			result = ((MtpStorageInfo) entry).getVolumeIdentifier();
			if(result.isEmpty()) result = "Removable storage";
			
			break;
		case CLASS_FOLDER:
		case CLASS_FILE:
			
			result = ((MtpObjectInfo) entry).getName();
			
			break;
		}
		
		return result;
	}
	
	public byte[] getThumbnail(){
		if(isFile()){
			return device.getThumbnail(((MtpObjectInfo) entry).getObjectHandle());
		}
		return null;
	}
	
	@Override
	public String toString(){
		return getName();
	}

	public MtpFile[] listFiles(){
		Log.d(TAG, "listFiles");
		MtpFile[] list = null;
		
		switch(entryClass){
		case CLASS_DEVICE:
			MtpDevice mtpDevice = (MtpDevice) entry;
			Log.d(TAG, "mtpDevice => " + mtpDevice);
			int[] storagesIds = mtpDevice.getStorageIds();
			Log.d(TAG, "storagesIds => " + storagesIds);
			list = new MtpFile[storagesIds.length];
			Log.d(TAG, "list => " + list);
			for(int i = 0; i < storagesIds.length; i++){
				list[i] = new MtpFile(mtpDevice.getStorageInfo(storagesIds[i]), this);
			}
			break;
		case CLASS_STORAGE:
			list = listFiles(((MtpStorageInfo) entry).getStorageId(), 0xFFFFFFFF);
			break;
		case CLASS_FOLDER:
			MtpObjectInfo info = (MtpObjectInfo) entry;
			list = listFiles(info.getStorageId(), info.getObjectHandle());
			break;
		}
		
		return list;
	}
	
	private MtpFile[] listFiles(int storageId, int parentHandle){
		Log.d(TAG, "listFiles");
		int l = 0;
		int[] handles = device.getObjectHandles(
			storageId,
			0,
			parentHandle
		);
		MtpFile[] list = new MtpFile[handles.length];
		MtpObjectInfo info;
		
		for(int i = 0; i < handles.length; i++){
			info = device.getObjectInfo(handles[i]);
			list[l++] = new MtpFile(info, this);
		}
		
		if(l < list.length){
			list = Arrays.copyOfRange(list, 0, l);
		}
		
		return list;
	}
	
	int getSize(){
		int result = 0;
		
		if(entryClass == CLASS_FILE){
			result = ((MtpObjectInfo) entry).getCompressedSize();
		}
		
		return result;
	}
	
	boolean copyTo(String destPath){
		return device.importFile(
			((MtpObjectInfo) entry).getObjectHandle(),
			destPath
		);
	}
	
}
