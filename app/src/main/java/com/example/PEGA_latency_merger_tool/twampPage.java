package com.example.PEGA_latency_merger_tool;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

public class twampPage extends AppCompatActivity {
    private int TST_PKT_SIZE = 1472;
    private long interv_msg = 0;
    private String destIpText;
    private String destTcpPortText;
    private int PORTBASE_SEND = 30000;
    private int PORTBASE_RECV = 20000;
    private InetAddress destIp;
    private int destTcpPort;
    private int destUdpPort;
    private Socket TcpSocket;
    private DatagramSocket UdpSocket;
    private InputStream in;
    private DataOutputStream out ;
    private String hostIp;
    private int LOSTTIME = 2;
    private int recvPort_int;
    private int sendPort_int;
    private int Modes;
    private int workmode = 1;
    private int payload_len = 1400;
    private int snd_tos = 0;
    private int serveroct = 0;
    private int test_sessions_msg = 0;

    private LinearLayout resContainer;
    private Handler handler = new Handler();
    private LineChartData lineChartData;
    private LineChart lineChart;
    private ArrayList<String> xData = new ArrayList<>();
    private ArrayList<Entry> yData = new ArrayList<>();
    private float min_NwRTD = Float.POSITIVE_INFINITY;
    private float max_NwRTD = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twamp_page);

        lineChart = findViewById(R.id.lineChart);
        lineChartData = new LineChartData(lineChart,this);

        resContainer = (LinearLayout) findViewById(R.id.linearLayoutRes);
        SeekBar pktSeek = (SeekBar)findViewById(R.id.seekBarNumPkt);
        EditText pktVal = (EditText)findViewById(R.id.editTextNumberNumPkt);
        pktSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        SeekBar interSeek = (SeekBar)findViewById(R.id.seekBarInter);
        EditText interVal = (EditText)findViewById(R.id.editTextNumberInter);
        interSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                    if (numPkt >= interSeek.getMin() && numPkt <= interSeek.getMax()) {
                        interSeek.setProgress(numPkt);
                    }
                    else{
                        interSeek.setProgress(interSeek.getMin());
                    }
                }
                catch (Exception e){
                    interSeek.setProgress(interSeek.getMin());
                }
            }
        });

        SeekBar paySeek = (SeekBar)findViewById(R.id.seekBarPayload);
        EditText payVal = (EditText)findViewById(R.id.editTextNumberPayload);
        paySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    public void connecting(View view) throws InterruptedException {
        clsMsg();
        destIpText = String.valueOf(((EditText)findViewById(R.id.editTextPostalAddressDst)).getText());
        destTcpPortText = String.valueOf(((EditText)findViewById(R.id.editTextNumberPort)).getText());
        payload_len = Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberPayload)).getText()));
        test_sessions_msg = Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberNumPkt)).getText()));
        interv_msg =  Integer.parseInt(String.valueOf(((EditText)findViewById(R.id.editTextNumberInter)).getText()));
        Thread threadTcp = new Thread(TWAMP_test);
        threadTcp.start();
    }

    private Runnable TWAMP_test = new Runnable() {
        @Override
        public void run() {
            try{
                CtrCnt();
                testStart();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void closeAllcnt(){
        try {
            in.close();
            out.close();
            TcpSocket.close();
            UdpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CtrCnt() throws IOException {
        destIp = InetAddress.getByName(destIpText.replace(" ","")); //
        destTcpPort = Integer.parseInt(destTcpPortText);
        TcpSocket = new Socket(destIp, destTcpPort);
        in = TcpSocket.getInputStream();
        out = new DataOutputStream(TcpSocket.getOutputStream());
        hostIp = TcpSocket.getInetAddress().getHostName();
        if(!TcpSocket.isConnected()){
            Log.e("cntErr","Error connecting.");
            closeAllcnt();
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("Connecting to Server %s", destIpText));
                content.setBackgroundColor(Color.parseColor("#888888"));
                resContainer.addView(content);
            }
        });

        recvGreet();
    }

    private void recvGreet() throws IOException {
        int[] greet = new int[64];
        for (int i = 0; i < 64; i++) greet[i] = in.read();

        Modes = resealizeAtt(12, 4, greet);
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format(String.format("Received ServerGreeting message with mode %d", Modes)));
                content.setBackgroundColor(Color.parseColor("#dda774"));
                resContainer.addView(content);
            }
        });

        if ((Modes & 0x000F) == 0) {
            Log.e("cntErr","");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    TextView content  = new TextView(twampPage.this);
                    content.setText("The server does not support any usable Mode.");
                    content.setBackgroundColor(Color.parseColor("#444444"));
                    content.setTextColor(Color.parseColor("#FF0000"));
                    resContainer.addView(content);
                }
            });
            closeAllcnt();
            return;
        }
        computeSetUpResponse();
    }

    private void computeSetUpResponse() throws IOException {
        byte[] resp = new byte[164];
        Arrays.fill(resp, (byte) 0);
        workmode = Modes & 1;
        resp[3] = (byte)workmode;
        if (workmode == 0) {
            Log.e("cntErr","The client and server do not support any usable Mode.");
            closeAllcnt();
            return;
        }
        try {
            out.write(resp);
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
            closeAllcnt();
            return;
        }
        recvServerStart();
    }

    private void recvServerStart() throws IOException {
        int[] start = new int[48];
        for (int i = 0; i < 48; i++) start[i] = in.read();
        if (start[16] != 0){ //if server does not accept
            Log.e("cntErr","Request failed.");
            closeAllcnt();
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format(String.format("Received ServerStart at Client %s", hostIp)));
                content.setBackgroundColor(Color.parseColor("#dda774"));
                resContainer.addView(content);
            }
        });
        sndReq();
    }

    private void sndReq() throws IOException {
        int bindTime = 0;

        for(bindTime = 0; bindTime < 100; bindTime++){
            try {
                sendPort_int = (int) (PORTBASE_SEND + Math.random()*1000);
                UdpSocket = new DatagramSocket(sendPort_int);
            }
            catch (SocketException e){
                String cause = String.valueOf(e.getCause());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView content  = new TextView(twampPage.this);
                        content.setText(cause);
                        content.setBackgroundColor(Color.parseColor("#444444"));
                        content.setTextColor(Color.parseColor("FF0000"));
                        resContainer.addView(content);
                    }
                });
                continue;
            }
            break;
        }

        if(bindTime >= 100) {
            Log.e("cntErr","Couldn't find a port to bind for session.");
            TcpSocket.close();
            return;
        }
        recvPort_int = (int) (PORTBASE_RECV + Math.random()*1000);

        byte[] req = new byte[112];
        Arrays.fill(req, (byte) 0);
        byte[] sendPort = ByteBuffer.allocate(4).putInt(sendPort_int).array();
        byte[] recvPort = ByteBuffer.allocate(4).putInt(recvPort_int).array();
        InetAddress ip = InetAddress.getByName(hostIp);
        byte[] hostAddress = ip.getAddress();
        int mbz_offset = 0;
        if ((workmode & 64) == 64) {
            mbz_offset = 27;
            if ((workmode & 256) == 256) {
                mbz_offset = 28;
            }
        }
        int PeddingLength_int = payload_len - 14 - mbz_offset;   // As defined in RFC 6038#4.2
        byte[] PaddingLength = ByteBuffer.allocate(4).putInt(PeddingLength_int).array();
        long[] timestamp = getTimestamp();
        int Timeout_integer_int = 10;
        int Timeout_fractional_int = 0;
        byte[] timestamp_integer = ByteBuffer.allocate(8).putLong(timestamp[0]).array();
        byte[] timestamp_fractional = ByteBuffer.allocate(8).putLong(timestamp[1]).array();
        byte[] Timeout_integer = ByteBuffer.allocate(4).putInt(Timeout_integer_int).array();
        byte[] Timeout_fractional = ByteBuffer.allocate(4).putInt(Timeout_fractional_int).array();

        BigInteger TypePDescriptor_int = new BigInteger(String.valueOf(snd_tos & 0xFC)).shiftLeft(22);
        byte[] TypePDescriptor = ByteBuffer.allocate(4).put(TypePDescriptor_int.toByteArray()).array();

        req[0] = (byte)5; //set Type
        req[1] = (byte)4; //set IPVN
        req[12] = sendPort[2];
        req[13] = sendPort[3];
        req[14] = recvPort[2];
        req[15] = recvPort[3];
        req[16] = req[32] = hostAddress[0];
        req[17] = req[33] = hostAddress[1];
        req[18] = req[34] = hostAddress[2];
        req[19] = req[35] = hostAddress[3];
        req[64] = PaddingLength[0];
        req[65] = PaddingLength[1];
        req[66] = PaddingLength[2];
        req[67] = PaddingLength[3];
        req[68] = timestamp_integer[4];
        req[69] = timestamp_integer[5];
        req[70] = timestamp_integer[6];
        req[71] = timestamp_integer[7];
        req[72] = timestamp_fractional[4];
        req[73] = timestamp_fractional[5];
        req[74] = timestamp_fractional[6];
        req[75] = timestamp_fractional[7];
        req[76] = Timeout_integer[0];
        req[77] = Timeout_integer[1];
        req[78] = Timeout_integer[2];
        req[79] = Timeout_integer[3];
        req[80] = Timeout_fractional[0];
        req[81] = Timeout_fractional[1];
        req[82] = Timeout_fractional[2];
        req[83] = Timeout_fractional[3];
        req[84] = TypePDescriptor[0];
        req[85] = TypePDescriptor[1];
        req[86] = TypePDescriptor[2];
        req[87] = TypePDescriptor[3];
        if ((workmode & 32) == 32)
            req[91] = (byte)2; //PadLenghtToReflect
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("Sending RequestTWSession for Receiver port %d ...", recvPort_int));
                content.setBackgroundColor(Color.parseColor("#888888"));
                resContainer.addView(content);
            }
        });
        out.write(req);
        recvAccept();
    }

    private void recvAccept() throws IOException {
        int[] acc = new int[48];
        for (int i = 0; i < 48; i++) acc[i] = in.read();
        if (acc[0] != 0){
            Log.e("cntErr","Request be rejected.");
            closeAllcnt();
        }
        destUdpPort = resealizeAtt(2,2, acc);

        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("Received Accept-Session for Receiver port %d...", destUdpPort));
                content.setBackgroundColor(Color.parseColor("#dda774"));
                resContainer.addView(content);
            }
        });


        int sid_addr = resealizeAtt(4,4, acc);
        int sid_time_integer = resealizeAtt(8, 4, acc);
        int sid_time_fractional = resealizeAtt(12, 4, acc);
        int sid_rand = resealizeAtt(16, 4, acc);

        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("SID: 0x%04X.%04X.%04X.%04X", sid_addr, sid_time_integer, sid_time_fractional, sid_rand));
                content.setBackgroundColor(Color.parseColor("#888888"));
                resContainer.addView(content);

            }
        });

        serveroct = resealizeAtt(22,2, acc);
        int reflectedOctets =  resealizeAtt(20,2,acc);

        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("#Session \t%d, Sender  \t%s:%d, Receiver \t%s:%d, Mode: %d",
                        0, hostIp, sendPort_int,
                        destIpText, destUdpPort, workmode));
                content.setBackgroundColor(Color.parseColor("#888888"));
                resContainer.addView(content);
            }
        });


        if ((workmode & 32) == 32) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    TextView content  = new TextView(twampPage.this);
                    content.setText(String.format("Octets to be reflected: %d, Reflected octets: %d,"+
                            " Server Octets: %d", 0, reflectedOctets, serveroct));
                    content.setBackgroundColor(Color.parseColor("#888888"));
                    resContainer.addView(content);
                }
            });

        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("Nb of Packets \t%d, Packet length \t%d, DSCP \t%d, TOS \t%d\n"+
                                "Sending Start-Sessions for all active Sender ports...",
                        test_sessions_msg, payload_len, (snd_tos & 0xFC), snd_tos));
                content.setBackgroundColor(Color.parseColor("#888888"));
                resContainer.addView(content);
            }
        });


        sndStartSession();
    }

    private void sndStartSession() throws IOException {
        byte[] start = new byte[32];
        Arrays.fill(start, (byte) 0);
        start[0] = (byte)2;
        out.write(start);

        return;
    }

    private void testStart() throws IOException, InterruptedException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(String.format("\tTime\t, Snd#\t, Rcv#\t, NwRTD\t, IntD\t, FWD\t, SWD  [ms]"));
                content.setBackgroundColor(Color.parseColor("#000000"));
                resContainer.addView(content);
            }
        });


        int lost_msg = 0;
        int fw_lost_msg = 0;
        int sw_lost_msg = 0;
        int rt_msg = 0;
        int rcv_sn = 0;
        int snd_sn = 0;
        int index = 0;
        long rtd = LOSTTIME * 1000000;

        for (index = 0; index < test_sessions_msg; index++) {
            byte[] pack = new byte[TST_PKT_SIZE];
            Arrays.fill(pack, (byte) 0);
            byte[] seq_number = ByteBuffer.allocate(4).putInt(index).array();
            pack[0] = seq_number[0];
            pack[1] = seq_number[1];
            pack[2] = seq_number[2];
            pack[3] = seq_number[3];
            long[] time_long = getTimestamp();
            byte[] time_integer = ByteBuffer.allocate(8).putLong(time_long[0]).array();
            byte[] time_fractional = ByteBuffer.allocate(8).putLong(time_long[1]).array();
            pack[4] = time_integer[4];
            pack[5] = time_integer[5];
            pack[6] = time_integer[6];
            pack[7] = time_integer[7];
            pack[8] = time_fractional[4];
            pack[9] = time_fractional[5];
            pack[10] = time_fractional[6];
            pack[11] = time_fractional[7];
            byte[] error_estimate = ByteBuffer.allocate(4).putInt(32769).array();
            pack[12] = error_estimate[2];
            pack[13] = error_estimate[3];
            byte[] padding = ByteBuffer.allocate(4).putInt(serveroct).array();
            pack[14] = padding[2];
            pack[15] = padding[3];

            DatagramPacket packet = new DatagramPacket(pack, pack.length, destIp, destUdpPort);
            UdpSocket.send(packet);

            /* recving Message */
            byte[] pack_reflect = new byte[42];
            long[] rcv_resp_time = getTimestamp();
            DatagramPacket message = new DatagramPacket(pack_reflect, 42);
            UdpSocket.setSoTimeout(3000);
            try {
                UdpSocket.receive(message);
            }
            catch (SocketException e) {
                String res = String.format("%.0f\t, %3d\t, %3c\t, %d\t, %d\t, %3c\t, %3c\t, %3c\t, %3c\t, " +
                                " %3c\t, %3c\t, %3c\t, %3c\t, %3c\t, %3c",
                        (double) time_long[1] * 1e-3, index, '-', sendPort_int, destUdpPort,
                        '-', '-', '-', '-', '-', '-', '-', '-', '-', '-');
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView content = new TextView(twampPage.this);
                        content.setText(res);
                        content.setBackgroundColor(Color.parseColor("#000000"));
                        content.setTextColor(Color.parseColor("#ffa73a"));
                        resContainer.addView(content);
                    }
                });

                lost_msg++;
                /* print loss results */
                if (((index + 1) % 10) == 0 && lost_msg != 0) {
                    String res1 = String.format("RT Lost packets: %d/%d,  RT Loss Ratio: %3.2f%%",
                            lost_msg, index + 1, (float)100 * lost_msg / (index + 1));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView content = new TextView(twampPage.this);
                            content.setText(res1);
                            content.setBackgroundColor(Color.parseColor("#888888"));
                            content.setTextColor(Color.parseColor("#ffa73a"));
                            resContainer.addView(content);
                        }
                    });

                    if (rt_msg != 0) {
                        String res2 = String.format("FW Lost packets: %d/%d,  FW Loss Ratio: %3.2f%%\n " +
                                        "SW Lost packets: %d/%d,  SW Loss Ratio: %3.2f%%",
                                fw_lost_msg, rt_msg, (float)100 * fw_lost_msg / rt_msg,
                                sw_lost_msg, rt_msg - fw_lost_msg,
                                (float)100 * sw_lost_msg / (rt_msg - fw_lost_msg));

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView content = new TextView(twampPage.this);
                                content.setText(res2);
                                content.setBackgroundColor(Color.parseColor("#888888"));
                                content.setTextColor(Color.parseColor("#ffa73a"));
                                resContainer.addView(content);
                            }
                        });

                    }
                }
                continue;
            }

            int sw_ttl = 0;
            int sw_tos = 0;

            if (rt_msg == 0) {
                rt_msg = 1;
            } else {
                fw_lost_msg = fw_lost_msg + index - snd_sn - resealizeAtt(0,4, pack_reflect) + rcv_sn;
                sw_lost_msg = sw_lost_msg + resealizeAtt(0,4, pack_reflect) - rcv_sn - 1;
                rt_msg = rt_msg + index - snd_sn;
            }
            /* Print the latency metrics */
            rtd = printMetrics(sendPort_int, destUdpPort,
                            snd_tos, sw_ttl, sw_tos, rcv_resp_time, pack_reflect, workmode);

            /* Indicators for one-way losses */
            snd_sn = index;
            rcv_sn = resealizeAtt(0,4, pack_reflect);

            /* Print loss results */
            if ((((index + 1) % 10) == 0) && ((index + 1) < test_sessions_msg)) {
                String res = String.format("RT Lost packets: %d/%d,  RT Loss Ratio: %3.2f%%",
                        lost_msg, (int)(index + 1), (float)100 * lost_msg / (index + 1));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView content = new TextView(twampPage.this);
                        content.setText(res);
                        content.setBackgroundColor(Color.parseColor("#888888"));
                        content.setTextColor(Color.parseColor("#ffa73a"));
                        resContainer.addView(content);
                    }
                });


                if (rt_msg != 0 && lost_msg != 0) {
                    String res1 = String.format("FW Lost packets: %d/%d,  FW Loss Ratio: %3.2f%%\n " +
                                    "SW Lost packets: %d/%d,  SW Loss Ratio: %3.2f%%",
                            fw_lost_msg, rt_msg, (float)100 * fw_lost_msg / rt_msg,
                            sw_lost_msg, rt_msg - fw_lost_msg,
                            (float)100 * sw_lost_msg / (rt_msg - fw_lost_msg));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView content = new TextView(twampPage.this);
                            content.setText(res1);
                            content.setBackgroundColor(Color.parseColor("#888888"));
                            content.setTextColor(Color.parseColor("#ffa73a"));
                            resContainer.addView(content);
                        }
                    });

                }
            }

            /* Interval sleep between packets */
            if (rtd < interv_msg * 1000) {
                int sleepTime = (int) (interv_msg * 1000 - rtd);
                Thread.sleep((int)sleepTime/1000, sleepTime%1000);
            }
        }
        /* Print final loss results */
        String res = String.format("RT Lost packets: %d/%d,  RT Loss Ratio: %3.2f%%",
                lost_msg, test_sessions_msg, (float)100 * lost_msg / test_sessions_msg);
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content = new TextView(twampPage.this);
                content.setText(res);
                content.setBackgroundColor(Color.parseColor("#888888"));
                content.setTextColor(Color.parseColor("#ffa73a"));
                resContainer.addView(content);
            }
        });

        if (rt_msg != 0 && lost_msg != 0) {
            String res1 = String.format("FW Lost packets: %d/%d,  FW Loss Ratio: %3.2f%%\n " +
                            "SW Lost packets: %d/%d,  SW Loss Ratio: %3.2f%%",
                    fw_lost_msg & 0x0FF, rt_msg & 0x0FF, (float)100 * fw_lost_msg / rt_msg,
                    sw_lost_msg & 0x0FF, (rt_msg - fw_lost_msg) & 0x0FF,
                    (float)100 * sw_lost_msg / (rt_msg - fw_lost_msg));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    TextView content = new TextView(twampPage.this);
                    content.setText(res1);
                    content.setBackgroundColor(Color.parseColor("#888888"));
                    content.setTextColor(Color.parseColor("#ffa73a"));
                    resContainer.addView(content);
                }
            });

        }

        sndStopSessions();
    }

    private void sndStopSessions(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText("Sending Stop-Sessions for all active ports...");
                content.setBackgroundColor(Color.parseColor("#888888"));
                resContainer.addView(content);
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                lineChartData.initX(xData);
                lineChartData.initY(min_NwRTD-100F,max_NwRTD+100F);
                lineChartData.initDataSet(yData);
            }
        });


        byte[] stop = new byte[32];
        Arrays.fill(stop, (byte) 0);
        stop[0] = (byte)3;
        stop[1] = (byte)0;

        closeAllcnt();
    }

    public int resealizeAtt (int beg, int len, int[] arr) throws IOException {
        int att = 0;
        int end = beg+len;
        for(int i = beg; i < end; i++){
            att = att << 8;
            att += arr[i];
        }
        return att;
    }

    public int resealizeAtt (int beg, int len, byte[] arr) throws IOException {
        int att = 0;
        int end = beg+len;
        for(int i = beg; i < end; i++){
            att = att << 8;
            att += (arr[i] & 0xFF);
        }
        return att;
    }

    public long[] getTimestamp(){
        long[] sysCurrTimeList = new long[2];
        Instant sysCurrTime = Instant.now();
        sysCurrTimeList[0] = sysCurrTime.getEpochSecond()+2208988800L;
        sysCurrTimeList[1] = (long) ((sysCurrTime.getNano()/1000) * ((long)1 << 32) / 1e6);
        return sysCurrTimeList;
    }

    public long getUsec(long[] timestamp){
        timestamp[0] -= 2208988800L;
        timestamp[1] = (long) ((timestamp[1]*1e6) / ((long)1 << 32));
        return timestamp[0]*1000000 + timestamp[1];
    }

    public long printMetrics(int snd_port, int rcv_port, int snd_tos, int sw_ttl, int sw_tos,
                              long[] recv_resp_time, byte[] pack_reflect, int mode) throws IOException {
        /* Compute timestamps in usec */
        long t_sender[] = {(long)resealizeAtt(28, 4, pack_reflect), (long)resealizeAtt(32, 4, pack_reflect)};
        long t_sender_usec = getUsec(t_sender);
        long t_receive[] = {(long)resealizeAtt(16, 4, pack_reflect), (long)resealizeAtt(20, 4, pack_reflect)};
        long t_receive_usec = getUsec(t_receive);
        long t_reflsender[] = {(long)resealizeAtt(4, 4, pack_reflect), (long)resealizeAtt(8, 4, pack_reflect)};
        long t_reflsender_usec = getUsec(t_reflsender);
        long t_recvresp_usec = getUsec(recv_resp_time);

        /* Compute delays */
        long fwd = (t_receive_usec - t_sender_usec)& 0xFFFFFFFFL;
        long swd = (t_recvresp_usec - t_reflsender_usec)& 0xFFFFFFFFL;
        long intd = (t_reflsender_usec - t_receive_usec) & 0xFFFFFFFFL;

        char sync = 'Y';
        if ((fwd < 0) || (swd < 0)) {
            sync = 'N';
        }

        /*Sequence number */
        int rcv_sn = resealizeAtt(0,4, pack_reflect);
        int snd_sn = resealizeAtt(24,4, pack_reflect);

        /* Sender TOS received at Reflector */
        float NwRTD = (float)((fwd + swd) * 1e-3);
        min_NwRTD = NwRTD < min_NwRTD? NwRTD: min_NwRTD;
        max_NwRTD = NwRTD > max_NwRTD? NwRTD: max_NwRTD;
        xData.add(String.valueOf(snd_sn));
        yData.add(new Entry(snd_sn, NwRTD));
        String res = String.format("%.0f\t, %3d\t, %3d\t, %.3f\t, %.3f\t, %.3f\t, %.3f",
                (double)(t_sender_usec& 0xFFFFFFFFL) * 1e-3, snd_sn, rcv_sn, NwRTD,
                (double)(intd) * 1e-3, (double)fwd * 1e-3, (double)swd * 1e-3);
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView content  = new TextView(twampPage.this);
                content.setText(res);
                content.setBackgroundColor(Color.parseColor("#000000"));
                content.setTextColor(Color.parseColor("#ffa73a"));
                resContainer.addView(content);
            }
        });

        return t_recvresp_usec& 0xFFFFFFFFL - t_sender_usec& 0xFFFFFFFFL;
    }

    public void clsMsg(){
        LinearLayout resContainer = (LinearLayout) findViewById(R.id.linearLayoutRes);
        resContainer.removeAllViews();
        xData = new ArrayList<>();
        yData = new ArrayList<>();
        min_NwRTD = Float.POSITIVE_INFINITY;
        max_NwRTD = 0;
        handler.post(new Runnable() {
            @Override
            public void run() {
                lineChart = findViewById(R.id.lineChart);
                lineChart.clear();
            }
        });
    }
}
