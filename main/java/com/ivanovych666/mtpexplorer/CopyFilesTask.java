package com.ivanovych666.mtpexplorer;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class CopyFilesTask extends AsyncTask<MtpFile, Integer, Long> {

	public ProgressDialog progressDialog = null;
	public String destination = null;
	public long total = 0;
	private static final String TAG = "CopyFilesTask";
	
	public CopyFilesTask(ProgressDialog progressDialog, String destination){
		this.progressDialog = progressDialog;
		this.destination = destination;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.setTitle("Copying files");
		progressDialog.setMessage("Please wait.");
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.show();
		progressDialog.setMax(100);
	}

	@Override
	protected Long doInBackground(MtpFile... files) {

		for(int i = 0; i < files.length; i++){
			total += files[i].getSize();
		}

		long progress = 0;
		for(int i = 0; i < files.length; i++){
			if(isCancelled()) break;

			try {
				TimeUnit.SECONDS.sleep(4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			String destPath = this.destination + File.separator + files[i];
			
			Log.d(TAG, "Copy File " + files[i] + " => " + destPath);

			files[i].open(progressDialog.getContext());
			if(files[i].copyTo(destPath)){
				Log.d(TAG, "	success");
			}else{
				Log.d(TAG, "	failed");
			}
			progress += files[i].getSize();
			files[i].close();
			
			publishProgress((int) (progress * 100 / total));
		}
		
		return total;
	}

	protected void onProgressUpdate(Integer... progress) {
		progressDialog.setProgress(progress[0]);
    }
	
	@Override
	protected void onPostExecute(Long result) {
		progressDialog.dismiss();
	}

}
