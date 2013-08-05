package com.mehmetakiftutuncu.downloadunzipshow;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener
{
	public static final String LOG_TAG = "DownloadUnzipShow";
	
	public static final String URL_TO_DOWNLOAD = "https://dl.dropbox.com/u/37485576/androidDownloadSample.zip";
	public static final String PATH_TO_SD = Environment.getExternalStorageDirectory().getPath();
	public static final String PATH_IN_SD_TO_SAVE = "mehmetakiftutuncu";
	public static final String FILE_NAME_TO_SAVE = "images.zip";
	
	Button buttonDownload, buttonUnzip, buttonShow;
	
	FileDownloaderTask myDownloader;
	DecompressorTask myDecompressor;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonDownload = (Button) findViewById(R.id.button_download);
		buttonUnzip = (Button) findViewById(R.id.button_unzip);
		buttonShow = (Button) findViewById(R.id.button_show);
		
		buttonDownload.setOnClickListener(this);
		buttonUnzip.setOnClickListener(this);
		buttonShow.setOnClickListener(this);
		
		Object[] lastConfigurations = (Object[]) getLastNonConfigurationInstance();
		
		if(lastConfigurations != null)
		{
			myDownloader = (FileDownloaderTask) lastConfigurations[0];
			myDecompressor = (DecompressorTask) lastConfigurations[1];
			
			if(myDownloader != null && myDownloader.getStatus() != AsyncTask.Status.FINISHED)
			{
				myDownloader.setContext(this); 
			}
			
			if(myDecompressor != null && myDecompressor.getStatus() != AsyncTask.Status.FINISHED)
			{
				myDecompressor.setContext(this); 
			}
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return new Object[] {myDownloader, myDecompressor};
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if(myDownloader != null && myDownloader.getProgressDialog() != null)
		{
			myDownloader.getProgressDialog().dismiss();
		}
		
		if(myDecompressor != null && myDecompressor.getProgressDialog() != null)
		{
			myDecompressor.getProgressDialog().dismiss();
		}
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.button_download:
				myDownloader = new FileDownloaderTask(this, URL_TO_DOWNLOAD, PATH_IN_SD_TO_SAVE, FILE_NAME_TO_SAVE);
				myDownloader.execute();
				break;
			
			case R.id.button_unzip:
				myDecompressor = new DecompressorTask(this, PATH_IN_SD_TO_SAVE, FILE_NAME_TO_SAVE, PATH_IN_SD_TO_SAVE);
				myDecompressor.execute();
				break;
			
			case R.id.button_show:
				break;
		}
	}
}