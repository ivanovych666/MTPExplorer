package com.ivanovych666.mtpexplorer;

import android.app.Fragment;
import android.hardware.usb.UsbDevice;
import android.mtp.MtpDevice;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ivanovych666 on 17.07.2016.
 */
public class RetainedFragment extends Fragment {

    private static final String TAG = "RetainedFragment";
    public MtpFile mtpFile;
    public UsbDevice usbDevice = null;
    public MtpDevice mtpDevice = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

}
