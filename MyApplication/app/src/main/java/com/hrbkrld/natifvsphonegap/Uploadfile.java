package com.hrbkrld.natifvsphonegap;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by Arnaud on 04/12/2015.
 */
public class UploadFile extends AppCompatActivity {

    ProgressDialog progressDialog;

    long timeStart;
    long timeStop;
    long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(UploadFile.this);
        progressDialog.setMessage("Uploading ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        timeStart = new Date().getTime();
        final UploadTask uploadTask = new UploadTask(UploadFile.this);
        uploadTask.execute("blabla");

    }

    private class UploadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock wakeLock;

        public UploadTask (Context context) { this.context = context; }

        @Override
        protected String doInBackground(String... params) {
            int status = this.logRemote("abrun");
            if (status == -1){
                return "Failed to create file";
            }
            if (status == -2){
                return "Failed to establish remote connection";
            }
            if (status == -3){
                return "IO eXception somewhere";
            }
            if (status == -4){
                return "File already exists on remote host";
            }


            return null;
        }

        protected int logRemote(String username)
        {

            String pathTransfert = "http://etunix.uqac.ca/"+username+"/transfert.php";

            //Créer le fichier a uploader :
            String fileName = getFilesDir() + "/testimage.jpeg";
            if (this.createFile(fileName) != 0 ){
                return -1;
            }


            URL url = null;
            try {
                url = new URL(pathTransfert);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return -2;
            }

            try {
                //Variables utiles :
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";


                //Récupération du fichier
                FileInputStream fileInputStream = new FileInputStream(new File(fileName));
                Log.d("TEST FILENAME", fileName);
                Log.d("TEST FILE", ""+fileInputStream.available());

                //configuration de la connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection == null){
                    return -2;
                }

                urlConnection.setDoInput(true); // Allow Inputs
                urlConnection.setDoOutput(true); // Allow Outputs
                urlConnection.setUseCaches(false); // Don't use a Cached Copy
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                for (Map.Entry<String, List<String>> entry : urlConnection.getRequestProperties().entrySet())
                {
                    Log.d("PROPERTIES", entry.getKey() + "/" + entry.getValue());
                }



                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + fileName +"\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // Creation de buffer pour ecrire le fichier
                int maxBufferSize = 1024 * 1024;
                int bytesAvailable = fileInputStream.available();
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                // Lire le fichier dans un buffer
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    Log.d("WRITING", "write "+bytesRead);
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Reponse du server (code and message)
                int serverResponseCode = urlConnection.getResponseCode();
                String serverResponseMessage = urlConnection.getResponseMessage();

                int code200 = 0;
                if(serverResponseCode == 200){
                    Log.d("RESPONSE", "file uploaded"+serverResponseCode+" - "+serverResponseMessage);
                    code200 = 1;
                }else{
                    Log.d("RESPONSE", "error : "+serverResponseCode+" - "+serverResponseMessage);
                }
                fileInputStream.close();
                dos.flush();
                dos.close();

                urlConnection.connect();

                // Contenu renvoyé par le serveur (echo ou html brut)
                InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
                String response = getResponse(isr);
                isr.close();

                //Verifier le contenu renvoyé
                if (code200 == 1 && response.contains("Sorry"))
                    return -4;


            } catch (IOException e) {
                e.printStackTrace();
                return -3;
            }
            return 0;
        }

        public int createFile(String fileName){

            int status = 0;
            InputStream in = null;
            OutputStream out = null;

            try {
                //Ouvrir le fichier de raw
                in = getResources().openRawResource(R.raw.testimage);

                //Creer le fichier de destination
                out = new FileOutputStream(fileName);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                Log.d("CREATION FILE", "done");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                status = -1;
            } catch (IOException e) {
                e.printStackTrace();
                status = -1;
            }finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return status;
            }

        }


        //Read response
        public String getResponse(InputStreamReader isr){
            String line = "";
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Response from server after login process will be stored in response variable.
            String response = sb.toString();
            Log.d("REMOTE", response);
            return response;
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
                Toast.makeText(context, "Upload error: " + s, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Upload success !", Toast.LENGTH_SHORT).show();

                timeStop = new Date().getTime();
                duration = timeStop - timeStart;

                TextView tv = (TextView) findViewById(R.id.dl_txt);
                tv.setText("Download time : " + duration + "ms");
            }
            Log.d("POST", "pute");
        }


    }




}
