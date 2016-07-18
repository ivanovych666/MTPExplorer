package com.ivanovych666.mtpexplorer;

import java.io.File;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class CopyFilesTask extends AsyncTask<MtpFile, Integer, Integer> {

	public interface OnResultListener {

		void onResult(Integer result);

	}

	private ProgressDialog progressDialog = null;
	private String destination = null;
	private Integer result = 0;
	private String task = null;
	private Integer length = 0;
	private Integer index = 0;
	private OnResultListener onResultListener = null;
	private static final String TAG = "CopyFilesTask";
	
	public CopyFilesTask(Context context, String destination){
		this.progressDialog = new ProgressDialog(context);
		this.destination = destination;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.setTitle("Copying files");
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.show();
		progressDialog.setMax(100);
	}

	@Override
	protected Integer doInBackground(MtpFile... files) {
		long sizeTotal = 0;
		length = files.length;

		task = "Calculating size";
		index = 0;
		publishProgress(0);

		for(; index < length; index++){

			if(isCancelled()) {
				return null;
			}
			MtpFile file = files[index];

			sizeTotal += file.getSize();
			publishProgress((int) ((index + 1) * 100 / length));
		}

		long progress = 0;
		task = "Copying files";
		index = 0;
		publishProgress(0);

		for(; index < length; index++){
			if(isCancelled()) {
				return null;
			}
			MtpFile file = files[index];

			String destPath = this.destination + File.separator + file;
			
			Log.d(TAG, "Copy File " + file + " => " + destPath);

			if(file.copyTo(destPath)){
				result++;
				Log.d(TAG, "	success");
			}else{
				Log.d(TAG, "	failed");
			}
			progress += file.getSize();

			publishProgress((int) (progress * 100 / sizeTotal));
		}
		
		return result;

	}

	protected void onProgressUpdate(Integer... progress) {
		progressDialog.setMessage(task + " (" + (index + 1) + " of " + length + ").");
		progressDialog.setProgress(progress[0]);
    }
	
	@Override
	protected void onPostExecute(Integer result) {
		progressDialog.dismiss();
		if (onResultListener != null) {
			onResultListener.onResult(result);
		}
	}

	public CopyFilesTask setOnResultListener (OnResultListener listener) {
		onResultListener = listener;
		return this;
	}

}
