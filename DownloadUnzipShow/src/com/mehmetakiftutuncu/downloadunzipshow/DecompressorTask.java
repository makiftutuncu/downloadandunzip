package com.mehmetakiftutuncu.downloadunzipshow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * An AsyncTask class for decompressing a zip file with a ProgressDialog<br><br>
 * 
 * @author Mehmet Akif Tütüncü
 */
public class DecompressorTask extends AsyncTask<Void, Void, Void>
{
	/** Tag for the logging */
	public static final String LOG_TAG = "DecompressorTask";
	
	/** Context of the decompressor object */
	private Context myContext;
	
	/** ProgressDialog to show the progress of the decompression */
	private ProgressDialog myProgressDialog;
	
	/** Relative path in sd card that contains the zip file<br><br>
	 *  If the sd card location is detected as "/sdcard" and this is given as "example"
	 *  then the zip file shoudl be in "/sdcard/example" */
	private String PATH_TO_ZIP_FILE;
	
	/** Name of the zip file to decompress */
	private String ZIP_FILE_NAME;
	
	/** Name of the zip file to decompress without the extension (This will be the name of the folder to decompress) */
	private String ZIP_FILE_NAME_WITHOUT_EXTENSION;
	
	/** Relative path in sd card to decompress the zip file<br><br>
	 *  If the sd card location is detected as "/sdcard" and this is given as "example"
	 *  then the file will be decompressed to "/sdcard/example" */
	private String PATH_TO_DECOMPRESS;
	
	/** Full absolute path of the zip file to decompress */
	private String FULL_PATH_OF_ZIP_FILE;
	
	/** Full absolute path of the decompression */
	private String FULL_PATH_OF_DECOMPRESSION;
	
	/** Flag indicating if the decompression was successful or not */
	private boolean isSuccessful = true;
	
	/**
	 * Constructor of the decompressor object
	 * 
	 * @param context			Context of the decompressor object
	 * @param pathToZipFile		Path of the zip file to decompress (Check {@link DecompressorTask#PATH_TO_ZIP_FILE})
	 * @param zipFileName		Name of the zip file to decompress (Check {@link DecompressorTask#ZIP_FILE_NAME})
	 * @param pathToDecompress	Where to decompress the file in sd card (Check {@link DecompressorTask#PATH_TO_DECOMPRESS})
	 */
	public DecompressorTask(Context context, String pathToZipFile, String zipFileName, String pathToDecompress)
	{
		myContext = context;
		
		PATH_TO_ZIP_FILE = pathToZipFile;
		ZIP_FILE_NAME = zipFileName;
		ZIP_FILE_NAME_WITHOUT_EXTENSION = ZIP_FILE_NAME.endsWith(".zip") ? ZIP_FILE_NAME.substring(0, ZIP_FILE_NAME.length() - 4) : ZIP_FILE_NAME;
		PATH_TO_DECOMPRESS = pathToDecompress;
		
		FULL_PATH_OF_ZIP_FILE = Environment.getExternalStorageDirectory().getPath() + "/" + PATH_TO_ZIP_FILE + "/" + ZIP_FILE_NAME;
		FULL_PATH_OF_DECOMPRESSION = Environment.getExternalStorageDirectory().getPath() + "/" + PATH_TO_DECOMPRESS + "/" + ZIP_FILE_NAME_WITHOUT_EXTENSION + "/";
	}
	
	/**
	 * Prepares and shows a ProgressDialog
	 */
	private void prepareProgressDialog()
	{
		myProgressDialog = new ProgressDialog(myContext);
		myProgressDialog.setTitle("Please Wait");
		myProgressDialog.setMessage("Decompressing the compressed file...");
		myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		myProgressDialog.setCancelable(false);
		myProgressDialog.setIndeterminate(true);
		myProgressDialog.show();
	}
	
	/**
	 * Handles the directories that is if given is a directory and doesn't exist, creates it
	 * 
	 * @param directory Directory to check
	 */
	private void directoryHandler(String directory)
	{
		File f = new File(FULL_PATH_OF_DECOMPRESSION + directory);
		
		if(!f.exists())
		{
			f.mkdirs();
		}
	}
	
	/**
	 * Sets the context of the decompressor object
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
			prepareProgressDialog();
		}
	}
	
	/**
	 * Gets the ProgressDialog of the decompressor object
	 * 
	 * @return ProgressDialog of the decompressor object
	 */
	public ProgressDialog getProgressDialog()
	{
		return myProgressDialog;
	}
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		prepareProgressDialog();
		
		directoryHandler("");
		
		Log.d(LOG_TAG, "Going to decompress " + FULL_PATH_OF_ZIP_FILE + " to: " + FULL_PATH_OF_DECOMPRESSION);
	}
	
	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			FileInputStream fileInputStream = new FileInputStream(FULL_PATH_OF_ZIP_FILE);
			ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
			ZipEntry zipEntry = null;
			
			while((zipEntry = zipInputStream.getNextEntry()) != null)
			{
				Log.d(LOG_TAG, "Decompressing: " + zipEntry.getName());
				
				if(zipEntry.isDirectory())
				{
					directoryHandler(zipEntry.getName());
				}
				else
				{
					FileOutputStream fileOutputStream = new FileOutputStream(FULL_PATH_OF_DECOMPRESSION + zipEntry.getName());
					
					byte[] buffer = new byte[1024];
					int length;
					
					while((length = zipInputStream.read(buffer)) > 0)
					{
						fileOutputStream.write(buffer, 0, length);
					}
					
					zipInputStream.closeEntry();
					fileOutputStream.close();
				}
			}
			zipInputStream.close();
		}
		catch(Exception e)
		{
			isSuccessful = false;
			
			Log.e(LOG_TAG, "Unable to decompress:", e);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		super.onPostExecute(result);
		
		if(isSuccessful)
		{
			Log.d(LOG_TAG, FULL_PATH_OF_ZIP_FILE + " is successfully decompressed to: " + FULL_PATH_OF_DECOMPRESSION);
		}
		
		myProgressDialog.dismiss();
	}
}