package com.example.PEGA_latency_merger_tool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import cn.yan.android.tracepath.AndroidTracePath;

public class tracePage extends AppCompatActivity {
    Button btn;
    Thread thread = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_page);
        btn = (Button)findViewById(R.id.buttonCnt);
    }
    public void connecting(View view) throws IOException, InterruptedException {
        clsMsg();
        EditText dest = (EditText) findViewById(R.id.editTextPostalAddressDst);
        TextView resContainer = (TextView) findViewById(R.id.textViewRes);

        Handler handler = new Handler();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AndroidTracePath androidTracePath = new AndroidTracePath(new AndroidTracePath.StateListener() {
                    @Override
                    public void onStart() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                changeBtnState();
                            }});
                    }

                    @Override
                    public void onUpdate(String update) {
                        Log.i("TRACE",update);
                        String update_temp = update.replace(" ", "");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(update_temp.indexOf(':') > -1){
                                    resContainer.append("\n");
                                }
                                resContainer.append(update_temp+"\t");
                            }});
                    }

                    @Override
                    public void onEnd() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                changeBtnState();
                            }});
                    }
                });
                androidTracePath.startTrace(String.valueOf(dest.getText()).replace(" ",""));
            }
        });
        thread.start();
    }

    public void clsMsg(){
        TextView resContainer = (TextView) findViewById(R.id.textViewRes);
        resContainer.setText("");
    }

    public void changeBtnState(){
        Button btn = (Button) findViewById(R.id.buttonCnt);
        btn.setEnabled(!btn.isEnabled());
    }
}