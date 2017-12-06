package com.example.tanis.four;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.SpeechKit;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;
import ru.yandex.speechkit.gui.RecognizerActivity;



public class MainActivity extends AppCompatActivity implements VocalizerListener {

    public final static int REQUEST_COMMAND = 1;
    private Vocalizer vocalizer;
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

                // RecognizerActivity myVerySmartRecognizer = new RecognizerActivity();
                SpeechKit.getInstance().configure(getApplicationContext(), "59a9449d-303a-4561-a413-845f11304c4f");

                RecognizerActivity bestRecognizerEver = new RecognizerActivity();
                Intent intent = new Intent(MainActivity.this, RecognizerActivity.class);
                intent.putExtra("EXTRA_LANGUAGE", Recognizer.Language.ENGLISH);
                intent.putExtra("EXTRA_MODEL", Recognizer.Model.NOTES);

                startActivityForResult(intent, REQUEST_COMMAND);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_COMMAND: {
                String command = data.getStringExtra(RecognizerActivity.EXTRA_RESULT);
                // msg.setText(command);
                MyClientTask myClientTask = new MyClientTask(command);
                myClientTask.execute();
                break;
            }

        }
    }

    private void resetVocalizer() {
        if (vocalizer != null) {
            vocalizer.cancel();
            vocalizer = null;
        }
    }

    @Override
    public void onSynthesisBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis) {

    }

    @Override
    public void onPlayingBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onPlayingDone(Vocalizer vocalizer) {

    }

    @Override
    public void onVocalizerError(Vocalizer vocalizer, Error error) {
        resetVocalizer();
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
            try {
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
            if (TextUtils.isEmpty(response)) {
                //Toast.makeText(getContext(), "Write smth to be vocalized!", Toast.LENGTH_SHORT).show();
            } else {
                // Reset the current vocalizer.
                resetVocalizer();
                vocalizer = Vocalizer.createVocalizer(Vocalizer.Language.RUSSIAN, response, true, Vocalizer.Voice.ZAHAR);
                vocalizer.setListener(MainActivity.this);
                vocalizer.start();
            }

            //msg.setText(response);
            String[] com = text.split(" ");
            Integer minutes=null;
            Integer hours=null;

            if (com.length==3)
            {
                hours = new Double(com[1]).intValue();
                minutes = new Double(com[2]).intValue();
            }

            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);

                i.putExtra(AlarmClock.EXTRA_MESSAGE, "New Alarm");
                i.putExtra(AlarmClock.EXTRA_HOUR, hours);
                i.putExtra(AlarmClock.EXTRA_MINUTES, minutes );

            startActivity(i);


        }

    }

}
