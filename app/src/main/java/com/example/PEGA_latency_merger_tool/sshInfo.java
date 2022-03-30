package com.example.PEGA_latency_merger_tool;

public class sshInfo{
    String sshIp;
    int sshPort;
    String sshUsr;
    String sshPW;

    public sshInfo(String usr, String pw, String ip, int port){
        sshIp = ip;
        sshPort = port;
        sshUsr = usr;
        sshPW = pw;
    }
}
