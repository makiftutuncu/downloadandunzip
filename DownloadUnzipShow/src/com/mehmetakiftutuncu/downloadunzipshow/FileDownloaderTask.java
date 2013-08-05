package com.mehmetakiftutuncu.downloadunzipshow;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * An AsyncTask class for handling a single file download with a ProgressDialog<br><br>
 * 
 * Needs use of {@link android.app.Activity.onRetainNonConfigurationInstance}
 * and {@link android.app.Activity.getLastNonConfigurationInstance} methods
 * to keep the progress dialog during any configuration changes (ex: screen orientation)
 * 
 * @author Mehmet Akif Tütüncü
 */
public class FileDownloaderTask extends AsyncTask<Void, Integer, Boolean>
{
	/** Tag for the logging */
	public static final String LOG_TAG = "FileDownloaderTask";
	
	/** Context of the downloader object */
	private Context myContext;
	
	/** Input stream of the downloader (obtained by a connection to the specified URL) */
	private InputStream myInputStream;
	
	/** Output stream of the downloader (to save the file) */
	private OutputStream myOutputStream;
	
	/** ProgressDialog to show the progress of the download */
	private ProgressDialog myProgressDialog;
	
	/** URL of the file to download */
	private String URL_TO_DOWNLOAD;
	
	/** Relative path in sd card to save the downloaded file<br><br>
	 *  If the sd card location is detected as "/sdcard" and this is given as "example"
	 *  then the file will be saved to "/sdcard/example" */
	private String PATH_IN_SD_TO_SAVE;
	
	/** Name of the file to save the downloaded data */
	private String FILE_NAME_TO_SAVE;
	
	/** Full absolute path of the file to be saved */
	private String FULL_PATH;
	
	/** Full absolute path of the file to be saved including the file name */
	private String FULL_PATH_WITH_FILE_NAME;
	
	/** Downloaded size in bytes */
	private double myDownloadedSize = 0;
	
	/** Total size of the file to download in bytes */
	private double myTotalSize = 0;
	
	/** Percentage of the download progress */
	private int myPercentage = 0;
	
	/** Flag indicating if the download was successful or not */
	private boolean isSuccessful = true;
	
	/** Flag indicating if the download was cancelled by clicking cancel button or not */
	private boolean isCancelled = false;
	
	/**
	 * Constructor of the downloader object
	 * 
	 * @param context			Context of the downloader object
	 * @param urlToDownload		URL of the file to download (Check {@link FileDownloaderTask#URL_TO_DOWNLOAD})
	 * @param pathInSdToSave	Where to save the file in sd card (Check {@link FileDownloaderTask#PATH_IN_SD_TO_SAVE})
	 * @param fileNameToSave	Name of the file to save (Check {@link FileDownloaderTask#FILE_NAME_TO_SAVE})
	 */
	public FileDownloaderTask(Context context, String urlToDownload, String pathInSdToSave, String fileNameToSave)
	{
		myContext = context;
		
		URL_TO_DOWNLOAD = urlToDownload;
		PATH_IN_SD_TO_SAVE = pathInSdToSave;
		FILE_NAME_TO_SAVE = fileNameToSave;
		
		FULL_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + PATH_IN_SD_TO_SAVE + "/";
		FULL_PATH_WITH_FILE_NAME = Environment.getExternalStorageDirectory().getPath() + "/" + PATH_IN_SD_TO_SAVE + "/" + FILE_NAME_TO_SAVE;
	}
	
	/**
	 * Gets a String representation of the download process with the following format:<br><br>
	 * 
	 * 123 kB / 532 kB
	 * 
	 * @return A String representation of the download process
	 */
	private String getDownloadedMessage()
	{
		if(myTotalSize < 1024)
		{
			return String.format("%d B / %d B", (int) myDownloadedSize, (int) myTotalSize);
		}
		else if(myTotalSize < 1048576)
		{
			return String.format("%.2f KB / %.2f KB", myDownloadedSize / 1024, myTotalSize / 1024);
		}
		else if(myTotalSize < 1073741824)
		{
			return String.format("%.2f MB / %.2f MB", myDownloadedSize / 1048576, myTotalSize / 1048576);
		}
		else
		{
			return String.format("%.2f GB / %.2f GB", myDownloadedSize / 1073741824, myTotalSize / 1073741824);
		}
	}
	
	/**
	 * Updates the value and message of the ProgressDialog
	 * 
	 * @param progress Value of the ProgressBar
	 * @param message Message in the ProgressDialog
	 */
	private void updateProgressDialog(int progress, String message)
	{
		myProgressDialog.setProgress(progress);
		myProgressDialog.setMessage(message);
	}
	
	/**
	 * Prepares and shows a ProgressDialog
	 * 
	 * @param progress Value that the ProgressBar will have at the beginning
	 */
	private void prepareProgressDialog(int progress)
	{
		myProgressDialog = new ProgressDialog(myContext);
		myProgressDialog.setTitle("Downloading...");
		myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		myProgressDialog.setMax(100);
		myProgressDialog.setCancelable(false);
		myProgressDialog.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int whichButton)
			{
				isCancelled = true;
				myProgressDialog.cancel();
				cancel(true);
			}
		});
		updateProgressDialog(progress, getDownloadedMessage());
		myProgressDialog.show();
	}
	
	/**
	 * Sets the context of the downloader object
	 * 
	 * @param context New context
	 */
	public void setContext(Context context)
	{
		this.myContext = context;
		
		if(context == null)
		{
			cancel(true);
		}
		else
		{
			prepareProgressDialog(myPercentage);
		}
	}
	
	/**
	 * Gets the ProgressDialog of the downloader object
	 * 
	 * @return ProgressDialog of the downloader object
	 */
	public ProgressDialog getProgressDialog()
	{
		return myProgressDialog;
	}
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		prepareProgressDialog(0);
		
		Log.d(LOG_TAG, "Going to download " + URL_TO_DOWNLOAD + " to: " + FULL_PATH_WITH_FILE_NAME);
	}
	
	@Override
	protected Boolean doInBackground(Void... params)
	{
		int countOfBytesRead;
		
		try
		{
			URL url = new URL(URL_TO_DOWNLOAD);
			URLConnection connection = url.openConnection();
			
			connection.connect();
			
			myTotalSize = connection.getContentLength();
			
			File path = new File(FULL_PATH);
			if(!path.exists())
			{
				path.mkdir();
			}
			
			myInputStream = new BufferedInputStream(url.openStream());
			myOutputStream = new FileOutputStream(FULL_PATH_WITH_FILE_NAME);
			
			byte[] data = new byte[1024];
			
			myDownloadedSize = 0;
			
			while(!isCancelled() && (countOfBytesRead = myInputStream.read(data)) != -1)
			{
				myDownloadedSize += countOfBytesRead;
				
				myPercentage = (int) ((myDownloadedSize / myTotalSize) * 100);
				
				publishProgress(myPercentage);
				
				myOutputStream.write(data, 0, countOfBytesRead);
			}
			
			myOutputStream.flush();
			myOutputStream.close();
			myInputStream.close();
		}
		catch(Exception e)
		{
			isSuccessful = false;
			Log.e(LOG_TAG, "An error occured while downloading. Details: " + e.getMessage());
		}
		
		return isSuccessful;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		
		updateProgressDialog(values[0], getDownloadedMessage());
	}
	
	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		
		if(isCancelled)
		{
			Log.d(LOG_TAG, "Download is cancelled.");
			
			try
			{
				myOutputStream.close();
				myInputStream.close();
				
				File file = new File(FULL_PATH_WITH_FILE_NAME);
				if(file.exists())
				{
					file.delete();
				}
			}
			catch(Exception e)
			{
				Log.e(LOG_TAG, "An error occured while cancelling the download. Details: " + e.getMessage());
			}
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		
		if(result)
		{
			Log.d(LOG_TAG, "File is successfully downloaded to: " + FULL_PATH_WITH_FILE_NAME);
			
			DecompressorTask myDecompressor = new DecompressorTask(myContext, PATH_IN_SD_TO_SAVE, FILE_NAME_TO_SAVE, PATH_IN_SD_TO_SAVE);
			myDecompressor.execute();
		}
		
		myProgressDialog.dismiss();
	}
}