package com.example.PEGA_latency_merger_tool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.jcraft.jsch.ChannelSftp;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class e2eLatencyPage extends AppCompatActivity {
    private final Handler handler = new Handler();
    sshInfo N6;
    String dest = "/home/pegauser/N6";
//    String dest = "/home/workspace/William_Li";
    String[] files = new String[2];
    static boolean twampFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twamp_page);

        SeekBar pktSeek = findViewById(R.id.seekBarNumPkt);
        EditText pktVal = findViewById(R.id.editTextNumberNumPkt);
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

        SeekBar batSeek = findViewById(R.id.seekBarNumBat);
        EditText batVal = findViewById(R.id.editTextNumberNumBat);
        batSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    batVal.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        batVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int numBat = Integer.parseInt(s.toString());
                    if (numBat >= batSeek.getMin() && numBat <= batSeek.getMax()) {
                        batSeek.setProgress(numBat);
                    }
                    else{
                        batSeek.setProgress(batSeek.getMin());
                    }
                }
                catch (Exception e){
                    batSeek.setProgress(batSeek.getMin());
                }
            }
        });

        SeekBar paySeek = findViewById(R.id.seekBarPayload);
        EditText payVal = findViewById(R.id.editTextNumberPayload);
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

        SeekBar pktIntSeek = findViewById(R.id.seekBarPktInter);
        EditText pkyIntVal = findViewById(R.id.editTextNumberPktInter);
        pktIntSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    pkyIntVal.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        pkyIntVal.addTextChangedListener(new TextWatcher() {
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

        SeekBar batIntSeek = findViewById(R.id.seekBarBatInter);
        EditText batIntVal = findViewById(R.id.editTextNumberBatInter);
        batIntSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    batIntVal.setText(Integer.toString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        batIntVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int numBat = Integer.parseInt(s.toString());
                    if (numBat >= batIntSeek.getMin() && numBat <= batIntSeek.getMax()) {
                        batIntSeek.setProgress(numBat);
                    }
                    else{
                        batIntSeek.setProgress(batIntSeek.getMin());
                    }
                }
                catch (Exception e){
                    batIntSeek.setProgress(batIntSeek.getMin());
                }
            }
        });
    }

    public void displayResult(JSONObject result){
        TableLayout resContainer = findViewById(R.id.tableLayoutRes);
        String[] keys = {"session", "UE_F1", "F1_N6", "N6_F1", "F1_UE", "amount"};
        handler.post(() -> {
            try {
                for (String key : keys) {
                    JSONArray valArray = (JSONArray) result.get(key);
                    TableLayout.LayoutParams lp =
                            new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT);
                    TableRow rowContainer = new TableRow(e2eLatencyPage.this);
                    lp.setMargins(5, 10, 5, 10);
                    rowContainer.setLayoutParams(lp);
                    TextView title = new TextView(e2eLatencyPage.this);
                    title.setBackgroundResource(R.color.black);
                    title.setTextColor(Color.parseColor("#EFEFEF"));
                    title.setText(String.format(key));
                    title.setGravity(Gravity.CENTER);
                    title.setWidth(4);
                    title.setPadding(0, 5, 0, 8);
                    rowContainer.addView(title);
                    for (int i = 0; i < valArray.length(); i++) {
                        if (key.equals("session")) {
                            int val = (int) valArray.get(i);
                            TextView delay = new TextView(e2eLatencyPage.this);
                            delay.setText(String.format(String.valueOf(val)));
                            delay.setGravity(Gravity.CENTER);
                            delay.setWidth(5);
                            delay.setPadding(0, 5, 0, 8);
                            delay.setBackgroundColor(Color.parseColor("#666666"));
                            rowContainer.addView(delay);
                        } else {
                            Double val = (Double) valArray.get(i);
                            TextView delay = new TextView(e2eLatencyPage.this);
                            delay.setText(String.format(String.valueOf(val)));
                            delay.setGravity(Gravity.CENTER);
                            delay.setWidth(5);
                            delay.setPadding(0, 8, 0, 8);
                            rowContainer.addView(delay);
                        }
                    }
                    resContainer.addView(rowContainer, lp);
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public void displayException(String except){
        Log.d("error", except);
        TextView exceptContainer = findViewById(R.id.textViewExcept);
        handler.post(() -> {
            exceptContainer.setText(String.format(except));
            exceptContainer.setVisibility(View.VISIBLE);
        });
    }

    public void cleanDisplay(){
        TableLayout resContainer = findViewById(R.id.tableLayoutRes);
        TextView exceptContainer = findViewById(R.id.textViewExcept);
        handler.post(() -> {
            resContainer.removeAllViews();
            exceptContainer.setVisibility(View.INVISIBLE);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void e2eLatency(View view){
//        Thread thrdTWAMP = new Thread(ntpTimeSync);
//        thrdTWAMP.start();
        cleanDisplay();

        String destIp = "172.16.41.219"; //String.valueOf(((EditText)findViewById(R.id.editTextPostalAddressDst)).getText());
        String destTcpPort = "8877"; //String.valueOf(((EditText)findViewById(R.id.editTextNumberPort)).getText());
        int payload = 900;//Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberPayload)).getText()));
        int numberPkts = 50; //Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberNumPkt)).getText()));
        int numberBats = 5; //Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberNumBat)).getText()));
        int pktInter = 10000; //Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberPktInter)).getText()));
        int batInter = 1000; //Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberBatInter)).getText()));
        N6 = new sshInfo("pegauser", "pega", destIp, 17722);

        try{
            String result = initialEnv();
            if(result != "") throw new Exception(result);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            Thread.UncaughtExceptionHandler h = (th, ex) -> {
                cleanEnv();
                twampFlag = true;
                displayException(ex.getMessage());
            };

            twampClient twamp = new twampClient(getApplicationContext(), destIp, destTcpPort,
                    payload, numberPkts, numberBats, pktInter, batInter);
            Thread thrdTWAMP = new Thread(twamp.TWAMP_test);
            thrdTWAMP.setUncaughtExceptionHandler(h);
            thrdTWAMP.start();
            thrdTWAMP.join();
            if(twampFlag) return;

            String src = getFilesDir().getAbsolutePath();
            SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
            String timeStamp = date.format(new Date());
            files[0] = timeStamp+"_UE_tx.log";
            files[1] = timeStamp+"_UE_rx.log";

            Thread thrdUpload = new Thread(pushLogs(N6, dest, src, files));

            thrdUpload.start();
            thrdUpload.join();

            Thread.sleep(3000);

            result = calLatency();
            if(result != ""){
                result = result.replace("\'", "\"");
                Log.d("result", result);
                JSONObject jsonResult = new JSONObject(result);
                displayResult(jsonResult);
            }
            else
                throw new Exception("Failed to execution.");
        }
        catch (Exception e){
            displayException(e.getMessage());

        }
    }

    private void cleanEnv(){
        Callable<String> callable = sshExec(N6, new String[]{"python3 "+dest+"/proxy.py E2E_Latency_Termiate"});
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.submit(callable);
    }

    private String initialEnv() throws ExecutionException, InterruptedException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Callable<String> callable = sshExec(N6, new String[]{"python3 "+dest+"/proxy.py E2E_Latency_Initial"});
        ExecutorService service = Executors.newFixedThreadPool(1);
        Future<String> result = service.submit(callable);
        String callback = result.get();
        return callback;
    }

    private String calLatency() throws ExecutionException, InterruptedException {
        Thread.sleep(3000);
        Callable<String> callable = sshExec(N6, new String[]{"python3 "+dest+"/proxy.py E2E_Latency_Start"});
        ExecutorService service = Executors.newFixedThreadPool(1);
        Future<String> result = service.submit(callable);
        String callback = result.get();
        return callback;
    }

    private final Runnable pushLogs(sshInfo host, String dest, String src, String[] files){
        Runnable runnable = () -> {
            try {
                ChannelSftp sftp = JSchUtils.connect(host);
                for(int i = 0; i < files.length; i++) {
                    FileInputStream in = new FileInputStream(src + "/" +files[i]);
                    sftp.put(in, dest + "/" + files[i]);
                    Log.i("JSCH", files[i]+" is uploaded.");
                }
                Callable<String> callable = sshExec(N6, new String[]{"python3 "+dest+"/N6logPush.py"});
                ExecutorService service = Executors.newFixedThreadPool(1);
                Future<String> result = service.submit(callable);
                String callback = result.get();
                Log.d("push_Log", callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        return runnable;
    }

    private final Callable<String> sshExec(sshInfo node, String[] cmds){
        return () -> {
            JSchUtils client = new JSchUtils();
            String callback = "";
            try {
                client.connect(node);
                for(int i = 0; i < cmds.length; i++){
                    callback += client.execCmd(cmds[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return callback;
        };
    }

    private final Runnable shellExec(String[] cmds){
        Runnable aRunnable = () -> {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());
                InputStream in = process.getInputStream();
                BufferedReader reader = null;
                String buf = null;
                reader = new BufferedReader(new InputStreamReader(in));

                for(int i = 0; i < cmds.length; i++) {
                    out.writeBytes(cmds[i] + "\n");
                }
                out.writeBytes("exit\n");
                out.flush();

                while ((buf = reader.readLine()) != null) {
                    Log.i("JSCH", buf);
                }
                process.waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
        return aRunnable;
    }

    private final Runnable ntpTimeSync = () -> {
        NTPUDPClient timeClient = new NTPUDPClient();
        TimeInfo info = null;
        String TIME_SERVER = "172.16.41.219";
        InetAddress inetAddress;
        while(true) {
            try {
                inetAddress = InetAddress.getByName(TIME_SERVER);
                info = timeClient.getTime(inetAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }

            NtpV3Packet message = info.getMessage();
            long destTime = info.getReturnTime();
            TimeStamp origNtpTime = message.getOriginateTimeStamp();    //t1
            TimeStamp rcvNtpTime = message.getReceiveTimeStamp();       //t2
            TimeStamp xmitNtpTime = message.getTransmitTimeStamp();     //t3
            TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);     //t4

            long offset = ((rcvNtpTime.getTime() - origNtpTime.getTime()) + (xmitNtpTime.getTime() - destNtpTime.getTime())) / 2;
            Log.d("offset", String.valueOf(offset));

//            handler.post(() -> {
//                try {
//                    Process process = Runtime.getRuntime().exec("su");
//                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
//                    String command = "date " + syncTime.toInstant() + "\n";
//                    os.writeBytes(command);
//                    os.writeBytes("exit\n");
//                    os.flush();
//                    process.waitFor();
//                } catch (InterruptedException | IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            try {
//                Thread.sleep(1000,0);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    };
}
