package com.example.tanis.four;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {

    EditText msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        final String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        msg=(EditText)findViewById(R.id.message);
        Button sender= (Button) findViewById(R.id.sender);
        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String _msg = msg.getText().toString();
                MyClientTask myClientTask = new MyClientTask(_msg);
                myClientTask.execute();




//


//
////                msg.setText(ip);
            }
        });
    }


    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String servAddress="192.168.43.213";
        int servPort=5678;
        String text;
        public String response = "";

        MyClientTask(String msg_){
            text=msg_;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(servAddress, servPort);

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

    /*
     * notice:
     * inputStream.read() will block if no data return
     */     try {
                    output.flush();
                    output.writeObject(text);
                    while (true) {
                        try {
                            String s = (String) input.readObject();
                            if (s.length()>0){response = s;break;}

                        } catch (Exception e) {
                        }
                    }

                } catch (IOException e) {
                }


            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            msg.setText(response);
            String[] com = text.split(" ");
            Integer minutes=null;
            Integer hours=null;
            Boolean t=false;
            //проверяем корректность введённых данных и, если всё хорошо, получаем реакцию из таблицы
//            if(com.length==2)
//            {
//                if(com[0].equals("alarm")) {
//                    if(com[2].equals("on")) {
//                        t=true;
//                        String [] time = com[1].split(":");
//                        if(time.length==2) {
//                            try {
//                                hours = new Double(time[0]).intValue();
//                                minutes = new Double(time[1]).intValue();
//
//                            }catch (Exception e){}}}}}
//
            String [] time = com[1].split(":");
            hours = new Double(time[0]).intValue();
            minutes = new Double(time[1]).intValue();

            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);

                i.putExtra(AlarmClock.EXTRA_MESSAGE, "New Alarm");
                i.putExtra(AlarmClock.EXTRA_HOUR, hours);
                i.putExtra(AlarmClock.EXTRA_MINUTES, minutes );

//            try {
//                TimeUnit.SECONDS.sleep(5);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            startActivity(i);


        }

    }

}
