package com.ivanovych666.mtpexplorer;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ivanovych666 on 17.07.2016.
 */
public class RetainedFragment extends Fragment {

    private static final String TAG = "RetainedFragment";
    private MtpFile data;

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

    public void setData(MtpFile value) {
        Log.d(TAG, "setData");
        String dataStr = "NULL";
        data = value;

        if (data != null) {
            dataStr = data.toString();
        }
        Log.d(TAG, "data: " + dataStr);
    }

    public MtpFile getData() {
        Log.d(TAG, "getData");
        String dataStr = "NULL";

        if (data != null) {
            dataStr = data.toString();
        }
        Log.d(TAG, "data: " + dataStr);

        return data;
    }
}
