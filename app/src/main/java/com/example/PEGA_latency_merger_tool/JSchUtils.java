package com.example.PEGA_latency_merger_tool;

import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Vector;


public class JSchUtils {
    private static JSch jsch;
    private static Session session = null;
    private static Channel channel = null;

    public static ChannelSftp connect(sshInfo node) throws Exception {
        jsch = new JSch();// 建立JSch物件
        session = jsch.getSession(node.sshUsr, node.sshIp, node.sshPort);// 根據使用者名稱、主機ip、埠號獲取一個Session物件
        session.setPassword(node.sshPW);// 設定密碼

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);// 為Session物件設定properties
        session.setTimeout(1000*30);// 設定超時
        session.connect();// 通過Session建立連線
        Log.i("JSCH", "Session connected.");
        channel = session.openChannel("sftp"); // 開啟SFTP通道
        channel.connect(); // 建立SFTP通道的連線
        Log.i("JSCH", "Connected successfully to host = " + node.sshIp + ",as user = " + node.sshUsr);
        return (ChannelSftp) channel;
    }

    public static void close() {
        if (channel != null) {
            channel.disconnect();
            Log.i("JSCH", "關閉channel成功");
        }
        if (session != null) {
            session.disconnect();
            Log.i("JSCH", "關閉session成功");
        }
    }

    public static String execCmd(String command) {
        BufferedReader reader = null;
        String callback = "";
        try {
            if (command != null) {
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect();

                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String buf = null;
                while ((buf = reader.readLine()) != null) {
                    callback += buf;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel.disconnect();
            return callback;
        }
    }

    public static void upload(String src, String dst) throws Exception {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        FileInputStream file = new FileInputStream(new File(src));
        channelSftp.put(file,dst);
        Log.i("JSCH", "上傳: " + src + "成功!");
    }

    public static void download(String src, String dst) throws Exception {
        // src linux伺服器檔案地址，dst 本地存放地址
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        channelSftp.get(src, dst);
        Log.i("JSCH", "下載檔案："+src+"成功");
        channelSftp.quit();
    }

    public void delete(String directory, String deleteFile) throws Exception {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.cd(directory);
        channelSftp.rm(deleteFile);
        Log.i("JSCH", "刪除成功");
    }

    @SuppressWarnings("rawtypes")
    public Vector listFiles(String directory) throws Exception {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        return channelSftp.ls(directory);
    }
}
