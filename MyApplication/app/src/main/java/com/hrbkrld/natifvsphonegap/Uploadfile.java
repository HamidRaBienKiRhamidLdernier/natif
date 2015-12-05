package com.hrbkrld.natifvsphonegap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;


/**
 * Created by Arnaud on 04/12/2015.
 */
public class Uploadfile extends AppCompatActivity {

    private TextView mElapsedTime;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mElapsedTime = (TextView) findViewById(R.id.textView);

    }

    /**
     * Se connecter au FTP via socket
     */
    public void connectFTP(View v) {

        //S'assurer qu'on a une connectivité réseau
        if (checkNetwork() == 0) {
            //Argument bidon
            new joinFTP().execute("connection");
        }
    }

    private class joinFTP extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            int port = 23;
            String url = "etunix.uqac.ca";
            String user = "abrun";
            String mdp = "O0r8q1U8";

            try {
                String res = executeRemoteCommand(user, mdp, url, port);
                Log.d("JSCH", res);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public String executeRemoteCommand(
                String username,
                String password,
                String hostname,
                int port) throws Exception {

            JSch jsch = new JSch();
            Session session = jsch.getSession(username, hostname, port);
            session.setPassword(password);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            Log.d("JSCH", "before connection");
            session.connect();
            Log.d("JSCH", "after connection");


            // SSH Channel
            ChannelExec channelssh = (ChannelExec) session.openChannel("exec");

            //Inputs
            InputStream inputStream = channelssh.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            // Execute command
            channelssh.setCommand("ls -la");
            Log.d("JSCH", "execute command");
            channelssh.connect();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }

            channelssh.disconnect();

            Log.d("JSCH", "result : " + stringBuilder.toString());
            return stringBuilder.toString();
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("JoinFTP", "end of execution");
        }
    }

    /*public void joinFTP()
        try {
            Log.d("RESPONSE", "avant socket");
            socket = new Socket(url, 23);
            Log.d("RESPONSE", "apres socket");
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Log in
            out.write(user.getBytes());
            out.write(mdp.getBytes());

            //Interact with remote ftp server
            out.write("ls".getBytes());
            Log.d("RESPONSE", in.readLine());


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public int checkNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d("NETWORK", "Device connected to network");
            return 0;
        } else {
            Log.d("NETWORK", "Device NOT CONNECTED to network");
            return -1;
        }
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
