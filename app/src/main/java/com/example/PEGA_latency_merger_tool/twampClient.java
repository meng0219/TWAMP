package com.example.PEGA_latency_merger_tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurementsEvent;
import android.location.LocationManager;
import android.util.Log;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.TimeUnit;

public class twampClient {
    private final int snd_tos = 0;
    private String destIpText;
    private String destTcpPortText;
    private InetAddress destIp;
    private Socket TcpSocket;
    private DatagramSocket UdpSocket;
    private InputStream in;
    private DataOutputStream out ;
    private String hostIp;
    private int destUdpPort;
    private int recvPort_int;
    private int sendPort_int;
    private int Modes;
    private int workmode = 1;
    private int payload_len;
    private int serveroct = 0;
    private int test_sessions_msg;
    private int numBat;
    private int interv_msg;
    private int interv_bat;
    private int rt_msg = 0;
    private long timeoffset = 0;
    private Context context;
    private static LocationManager locationManager;
    private static GnssMeasurementsEvent.Callback gnssMeasurementsEvent;
    public static long startNanoSec;
    public static long GPST = -1;
    private static volatile String txReport = "";
    private static volatile String rxReport = "";

    public twampClient(Context local, String ip, String port,
                       int payload, int numberPkts, int numberBats, int pktInter, int batInter){
        this.destIpText = ip;
        this.destTcpPortText = port;
        this.payload_len = payload;
        this.test_sessions_msg = numberPkts;
        this.numBat = numberBats;
        this.interv_msg = pktInter;
        this.interv_bat = batInter;
        this.context = local;
        rxReport = "";
        txReport = "";
    }

    private void closeAllcnt() throws Exception {
        try {
            in.close();
            out.close();
            TcpSocket.close();
            UdpSocket.close();
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    private void controlConnect() throws Exception {
        destIp = InetAddress.getByName(destIpText.replace(" ",""));
        int destTcpPort = Integer.parseInt(destTcpPortText);
        TcpSocket = new Socket(destIp, destTcpPort);
        in = TcpSocket.getInputStream();
        out = new DataOutputStream(TcpSocket.getOutputStream());
        hostIp = TcpSocket.getInetAddress().getHostName();
        if(!TcpSocket.isConnected()){
            Log.e("cntErr","Error connecting.");
            closeAllcnt();
            return;
        }
        recvGreet();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void recvGreet() throws Exception {
        int[] greet = new int[64];
        for (int i = 0; i < 64; i++) greet[i] = in.read();

        Modes = resealizeAtt(12, 4, greet);

        if ((Modes & 0x000F) == 0) {
            Log.e("cntErr","");
            closeAllcnt();
            return;
        }
        computeSetUpResponse();
    }

    private void computeSetUpResponse() throws Exception {
        byte[] resp = new byte[164];
        Arrays.fill(resp, (byte) 0);
        workmode = Modes & 1;
        resp[3] = (byte)workmode;
        if (workmode == 0) {
            closeAllcnt();
            throw new Exception("The client and server do not support any usable Mode.");
        }
        try {
            out.write(resp);
        } catch (IOException e) {
            closeAllcnt();
            throw new Exception(e.getMessage());
        }
        recvServerStart();
    }

    private void recvServerStart() throws Exception {
        int[] start = new int[48];
        for (int i = 0; i < 48; i++) start[i] = in.read();
        if (start[16] != 0){ //if server does not accept
            closeAllcnt();
            throw new Exception("Request failed.");
        }
        sndReq();
    }

    @SuppressLint("DefaultLocale")
    private void sndReq() throws Exception {
        int bindTime;

        for(bindTime = 0; bindTime < 100; bindTime++){
            try {
                int PORTBASE_SEND = 30000;
                sendPort_int = (int) (PORTBASE_SEND + Math.random()*1000);
                UdpSocket = new DatagramSocket(sendPort_int);
            }
            catch (SocketException e){
                String cause = String.valueOf(e.getCause());
                Log.e("twamp", String.valueOf(e.getCause()));
                continue;
            }
            break;
        }

        if(bindTime >= 100) {
            TcpSocket.close();
            throw new Exception("Couldn't find a port to bind for session.");
        }
        int PORTBASE_RECV = 20000;
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
        out.write(req);
        recvAccept();
    }

    @SuppressLint("DefaultLocale")
    private void recvAccept() throws Exception {
        int[] acc = new int[48];
        for (int i = 0; i < 48; i++) acc[i] = in.read();
        if (acc[0] != 0){
            Log.e("cntErr","Request be rejected.");
            closeAllcnt();
        }
        destUdpPort = resealizeAtt(2,2, acc);

        int sid_addr = resealizeAtt(4,4, acc);
        int sid_time_integer = resealizeAtt(8, 4, acc);
        int sid_time_fractional = resealizeAtt(12, 4, acc);
        int sid_rand = resealizeAtt(16, 4, acc);

        serveroct = resealizeAtt(22,2, acc);
        int reflectedOctets =  resealizeAtt(20,2,acc);
        sndStartSession();
    }

    private void sndStartSession() throws IOException {
        byte[] start = new byte[32];
        Arrays.fill(start, (byte) 0);
        start[0] = (byte)2;
        out.write(start);
    }

    @SuppressLint("SetTextI18n")
    private void testStart() throws Exception {
        for(int i = 0; i < this.numBat; i++) {
            Thread sndPktThd = new Thread(sndTestPkt);
            sndPktThd.start();

            Thread rcvPktThd = new Thread(rcvTestPkt);
            rcvPktThd.start();

            sndPktThd.join();
            rcvPktThd.join();
            Thread.sleep(this.interv_bat);
        }
        sndStopSessions();
    }

    @SuppressLint("SetTextI18n")
    private void sndStopSessions() throws Exception {
        byte[] stop = new byte[32];
        Arrays.fill(stop, (byte) 0);
        stop[0] = (byte)3;
        stop[1] = (byte)0;
        out.write(stop);
        closeAllcnt();
    }

    public int resealizeAtt (int beg, int len, int[] arr) {
        int att = 0;
        int end = beg+len;
        for(int i = beg; i < end; i++){
            att = att << 8;
            att += arr[i];
        }
        return att;
    }

    public long[] getTimestamp(){
        long[] sysCurrTimeList = new long[3];
        /* for GNSS time
        long nowNanoSec = GPST+(System.nanoTime()-startNanoSec);
         */
        /* for NTP time */
        long nowNanoSec =  ((System.currentTimeMillis()+timeoffset)*1000000+System.nanoTime()%100000);
        Log.d("nowTime", String.valueOf(nowNanoSec));
        sysCurrTimeList[0] = nowNanoSec/1000000000 + 2208988800L;
        sysCurrTimeList[1] = (long) ((nowNanoSec/1000) * ((long)1 << 32) / 1e6);
        sysCurrTimeList[2] = nowNanoSec;
        return sysCurrTimeList;
    }

    public void writeToFile(String data, String file, Context context) throws Exception {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            throw new Exception("Failed to write file: " + e.toString());
        }
    }

    public final Runnable TWAMP_test = () -> {
        String[] files = {"UE_tx.log", "UE_rx.log"};
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        String timeStamp = date.format(new Date());
        for(int i = 0; i < files.length; i++)
            files[i] = timeStamp+"_"+files[i];

        try {
            initTimestamp();
            controlConnect();
            testStart();

            writeToFile(txReport, files[0], context);
            writeToFile(rxReport, files[1], context);
        } catch (Exception e) {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
//        } finally {
//            locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEvent);
        }
    };

    public void initTimestamp() throws InterruptedException {
//        initGPST();
        Thread thrNtp = new Thread(getTimeOffsetByNtp);
        thrNtp.start();
        thrNtp.join();
    }

    private Runnable sndTestPkt = () -> {
        for (int index = 0; index < test_sessions_msg; index++) {
            int finalIndex = index;
            try {
                Thread.sleep(interv_msg/1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] pack = new byte[payload_len];
                    Arrays.fill(pack, (byte) 0);
                    byte[] seq_number = ByteBuffer.allocate(4).putInt(finalIndex).array();
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
                    try {
                        String txTime = new SimpleDateFormat("HH:mm:ss.SSS").format(time_long[2]/1000000);
                        txTime += String.valueOf(time_long[2]%100000);
                        txReport += (txTime + " IP TWAMP-client > TWAMP-server: UDP, length" + payload_len +"\n");
                        UdpSocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    };

    private Runnable rcvTestPkt = () -> {
        for (int index = 0; index < test_sessions_msg; index++) {
            byte[] pack_reflect = new byte[42];
            DatagramPacket message = new DatagramPacket(pack_reflect, 42);

            try {
                UdpSocket.setSoTimeout(10000);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                UdpSocket.receive(message);
                long[] rcv_resp_time = getTimestamp();
                String rxTime = new SimpleDateFormat("HH:mm:ss.SSS").format(rcv_resp_time[2]/1000000);
                rxTime += String.valueOf(rcv_resp_time[2]%100000);
                rxReport += (rxTime + " IP TWAMP-server > TWAMP-client: UDP, length " + payload_len +"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (rt_msg == 0) {
                rt_msg = 1;
            }
        }
    };

    public void initGPST() throws Exception {
        startNanoSec = -1;
        GPST = -1;
        gnssMeasurementsEvent = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived(eventArgs);
                GnssClock clock = eventArgs.getClock();
                GPST = (long) (clock.getTimeNanos() -
                        (clock.hasFullBiasNanos()? clock.getFullBiasNanos(): 0) -
                        (clock.hasBiasNanos()? clock.getBiasNanos(): 0) -
                        (clock.hasLeapSecond()? clock.getLeapSecond()*Math.pow(10,9): 0)
                );
                startNanoSec = System.nanoTime();
            }
        };
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEvent);
            Thread.sleep(30000);
            if (GPST < 0) throw new InterruptedException();
        } catch (SecurityException | InterruptedException e) {
            throw new Exception("Failed to register GNSS.");
        }
    }

    private Runnable getTimeOffsetByNtp = () -> {
        NTPUDPClient timeClient = new NTPUDPClient();
        TimeInfo info = null;
        InetAddress inetAddress = null;
        List<Long> values = new ArrayList<>();
        try {
            inetAddress = InetAddress.getByName(this.destIpText);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 20; i++) {
            try {
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
            long offset = (((rcvNtpTime.getTime() - origNtpTime.getTime()) + (xmitNtpTime.getTime() - destNtpTime.getTime()))/2);
            long delay = ((destNtpTime.getTime() - origNtpTime.getTime()) - (xmitNtpTime.getTime() - rcvNtpTime.getTime()))/4;
            values.add(offset-delay);
//            values.add(offset);
        }
        LongSummaryStatistics summaryStats = eliminateOutliers(values,1.5f).stream()
                .mapToLong((a) -> a)
                .summaryStatistics();
        timeoffset = (long) summaryStats.getAverage();
        Log.d("timeoffset", String.valueOf(timeoffset));
    };

    protected static double getMean(List<Long> values) {
        int sum = 0;
        for (Long value : values) {
            sum += value;
        }

        return (sum / values.size());
    }

    public static double getVariance(List<Long> values) {
        double mean = getMean(values);
        int temp = 0;

        for (Long a : values) {
            temp += (a - mean) * (a - mean);
        }

        return temp / (values.size() - 1);
    }

    public static double getStdDev(List<Long> values) {
        return Math.sqrt(getVariance(values));
    }

    public static List<Long> eliminateOutliers(List<Long> values, float scaleOfElimination) {
        double mean = getMean(values);
        double stdDev = getStdDev(values);

        final List<Long> newList = new ArrayList<>();

        for (long value : values) {
            boolean isLessThanLowerBound = value < mean - stdDev * scaleOfElimination;
            boolean isGreaterThanUpperBound = value > mean + stdDev * scaleOfElimination;
            boolean isOutOfBounds = isLessThanLowerBound || isGreaterThanUpperBound;

            if (!isOutOfBounds) {
                newList.add(value);
            }
        }

        int countOfOutliers = values.size() - newList.size();
        if (countOfOutliers == 0) {
            return values;
        }
        return eliminateOutliers(newList,scaleOfElimination);
    }
}
