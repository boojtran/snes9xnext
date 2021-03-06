/**
 * HALSAFAR DownloaderHelpers
 * 
 * SEE LICENSE FILE
 * 	Free to use, non-commercial license.
 * 
 * Copyright 2011 - Stephen Damm (shinhalsafar@gmail.com)
 */

package ca.halsafar.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class Decompress extends AsyncTask<Void, String, Integer>
{
     private static final String LOG_TAG = "Decompress";
     
     private String _zipFile;
     private String _location;
     private ProgressDialog _dialog;
     private DecompressListener _listener;

     public Decompress(String zipFile, String location, ProgressDialog progressDailog, DecompressListener listener)
     {
          _listener = listener;
          _zipFile = zipFile;
          _location = location;
          _dialog = progressDailog;
          _dialog.setMessage("Decompressing shaders...");
          
          _dirChecker("");
     }


     @Override
     protected Integer doInBackground(Void... Nothing)
     {
          try
          {         
               Log.v("Decompress", "Unzipping " + _zipFile);
               FileInputStream fin = new FileInputStream(_zipFile);
               ZipInputStream zin = new ZipInputStream(fin);                   
               ZipEntry ze = null;
               
               int fileCount = 0;
               
               while ((ze = zin.getNextEntry()) != null)
               {
                    String outFile = _location + "/" + ze.getName();
                    Log.v("Decompress", "Unzipping to: " + outFile);

                    if (ze.isDirectory())
                    {
                         _dirChecker(ze.getName());
                    }
                    else
                    {
                         publishProgress(ze.getName());
                         FileOutputStream fout = new FileOutputStream(outFile);
                         for (int c = zin.read(); c != -1; c = zin.read())
                         {
                              fout.write(c);
                         }

                         zin.closeEntry();
                         fout.close();
                         
                         fileCount++;
                    }

               }
               zin.close();
               
               return fileCount;
          }
          catch (Exception e)
          {
               Log.e("Decompress", "unzip", e);
          }                            

          return -1;
     }


     private void _dirChecker(String dir)
     {
          File f = new File(_location + dir);

          if (!f.isDirectory())
          {
               f.mkdirs();
          }
     }
     
     @Override
     public void onProgressUpdate(String... progress)
     {
          _dialog.setMessage("Decompressing: " + progress[0]);
          //_dialog.setProgress(progress[0]);               
     }


     @Override
     protected void onPostExecute(Integer result)
     {
          Log.d(LOG_TAG, "Unzipped " + result + " files");
          
          if (result < 0)
          {
              _listener.onDecompressFail(_zipFile); 
          }
          else if (result >= 0)
          {
               _listener.onDecompressSuccess(_zipFile, result);
          }

          _dialog.dismiss();
     }           
}