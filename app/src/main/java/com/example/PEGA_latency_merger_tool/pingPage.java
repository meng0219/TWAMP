package com.example.PEGA_latency_merger_tool;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class pingPage extends AppCompatActivity {
    Thread thread = null;
    private LinearLayout resContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_page);

        resContainer = (LinearLayout) findViewById(R.id.linearLayoutRes);
        SeekBar pktSeek = (SeekBar)findViewById(R.id.seekBarNumPkt);
        EditText pktVal = (EditText)findViewById(R.id.editTextNumberNumPkt);
        pktSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    pktVal.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        pktVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int numPkt = Integer.parseInt(s.toString());
                    if (numPkt >= pktSeek.getMin() && numPkt <= pktSeek.getMax()) {
                        pktSeek.setProgress(numPkt);
                    }
                    else{
                        pktSeek.setProgress(pktSeek.getMin());
                    }
                }
                catch (Exception e){
                    pktSeek.setProgress(pktSeek.getMin());
                }
            }
        });

        SeekBar pktIntSeek = (SeekBar)findViewById(R.id.seekBarInter);
        EditText interVal = (EditText)findViewById(R.id.editTextNumberInter);
        pktIntSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    interVal.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        interVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int numPkt = Integer.parseInt(s.toString());
                    if (numPkt >= pktIntSeek.getMin() && numPkt <= pktIntSeek.getMax()) {
                        pktIntSeek.setProgress(numPkt);
                    }
                    else{
                        pktIntSeek.setProgress(pktIntSeek.getMin());
                    }
                }
                catch (Exception e){
                    pktIntSeek.setProgress(pktIntSeek.getMin());
                }
            }
        });

        SeekBar paySeek = (SeekBar)findViewById(R.id.seekBarPayload);
        EditText payVal = (EditText)findViewById(R.id.editTextNumberPayload);
        paySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    payVal.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        payVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int numPkt = Integer.parseInt(s.toString());
                    if (numPkt >= paySeek.getMin() && numPkt <= paySeek.getMax()) {
                        paySeek.setProgress(numPkt);
                    }
                    else{
                        paySeek.setProgress(paySeek.getMin());
                    }
                }
                catch (Exception e){
                    paySeek.setProgress(paySeek.getMin());
                }
            }
        });
    }
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void connecting(View view) {
        clsMsg();
        String dest = String.valueOf(((EditText) findViewById(R.id.editTextPostalAddressDst)).getText()).replace(" ","");
        float interval = Float.parseFloat(String.valueOf(((EditText)findViewById(R.id.editTextNumberInter)).getText()))/1000;
        String pktNum = String.valueOf(((EditText)findViewById(R.id.editTextNumberNumPkt)).getText());
        String payload = String.valueOf(((EditText)findViewById(R.id.editTextNumberPayload)).getText());
        Handler handler = new Handler();

        thread = new Thread(() -> {
            handler.post(this::changeBtnState);
            Process p = null;
            try {
                p = Runtime.getRuntime().exec(String.format("ping -i %f -c %s -s %s -W 2 %s", interval, pktNum, payload, dest));
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert p != null;
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            while (true) {
                String update = null;
                try {
                    update = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (update == null) break;

                String finalUpdate = update;
                handler.post(() -> {
                    TextView content  = new TextView(pingPage.this);
                    content.setText(finalUpdate);
                    resContainer.addView(content);
                });
            }

            int status = 0;
            try {
                status = p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (status != 0) {
                handler.post(() -> {
                    TextView content  = new TextView(pingPage.this);
                    content.setText("The dest is not reachable.");
                    resContainer.addView(content);
                });
            }
            handler.post(this::changeBtnState);
        });
        thread.start();
    }

    public void clsMsg(){
        LinearLayout resContainer = (LinearLayout) findViewById(R.id.linearLayoutRes);
        resContainer.removeAllViews();
    }

    public void changeBtnState(){
        Button btn = (Button) findViewById(R.id.buttonCnt);
        btn.setEnabled(!btn.isEnabled());
    }
}