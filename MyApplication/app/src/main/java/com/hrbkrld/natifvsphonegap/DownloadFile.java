package com.hrbkrld.natifvsphonegap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class DownloadFile extends AppCompatActivity {

    ProgressDialog progressDialog;

    long timeStart;
    long timeStop;
    long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        progressDialog = new ProgressDialog(DownloadFile.this);
        progressDialog.setMessage("Downloading ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        timeStart = new Date().getTime();
        final DownloadTask downloadTask = new DownloadTask(DownloadFile.this);
        downloadTask.execute("http://www.zastavki.com/pictures/originals/2013/Photoshop_Image_of_the_horse_053857_.jpg");

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock wakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream is = null;
            OutputStream os = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                is = connection.getInputStream();
                os = new FileOutputStream(getFilesDir() + "/ile.jpg");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = is.read(data)) != -1) {
                    if (isCancelled()) {
                        is.close();
                        return  null;
                    }
                    total += count;

                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    os.write(data, 0, count);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (os != null)
                        os.close();
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (connection != null)
                    connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire();

            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            wakeLock.release();
            progressDialog.dismiss();

            if (s != null) {
                Toast.makeText(context, "Download error: " + s, Toast.LENGTH_SHORT).show();
            } else {
                ImageView imageView = (ImageView) findViewById(R.id.dl_img);
                File imageFile = new File(getFilesDir(), "ile.jpg");
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);

                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                timeStop = new Date().getTime();
                duration = timeStop - timeStart;

                TextView tv = (TextView) findViewById(R.id.dl_txt);
                tv.setText("Download time : "+duration);
                imageFile.delete();
            }
        }
    }

}
