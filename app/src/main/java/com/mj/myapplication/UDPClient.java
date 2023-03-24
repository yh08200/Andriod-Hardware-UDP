package com.mj.myapplication;

import android.os.HandlerThread;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Observable;

public class UDPClient extends Observable {
    //最大帧长
    private static final int MAX_DATA_PACKET_LENGTH = 1024;
    //服务器IP
    private static String SERVER_IP = "192.168.4.1";//"10.10.100.254";
    //服务器端口
    private static int SERVER_PORT_AUDIO = 8899;
    //本地端口
    private static int LOCAL_PORT_AUDIO = 8899;
    //接收数据缓存
    private byte[] Buffer_Receive = new byte[MAX_DATA_PACKET_LENGTH];
    //接收数据包
    public DatagramPacket Packet_Receive;
    //端口
    public DatagramSocket Udp_Socket;
    //接收数据
    public String ReceiverDate;

    public UDPClient() {
        try {
            //端口
            if(Udp_Socket==null) {
                Udp_Socket = new DatagramSocket(null);
                Udp_Socket.setReuseAddress(true);
                Udp_Socket.bind(new InetSocketAddress(LOCAL_PORT_AUDIO));
            }
            //接收包
            Packet_Receive = new DatagramPacket(Buffer_Receive, MAX_DATA_PACKET_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //发送指令
    public void send(byte[] data, int len) {
        Thread_Send thread_send = new UDPClient.Thread_Send(data, len);
        new Thread(thread_send).start();
    }
    //接收方法
    public void Receiver() {
        UDPClient.Thread_Receiver  thread_receiver = new UDPClient.Thread_Receiver();
        new Thread(thread_receiver).start();
    }
    //接收线程
    public class Thread_Receiver implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    if(Udp_Socket!=null){
                        //接收数据
                        Udp_Socket.receive(Packet_Receive);
                        Log.d("调试日志--------》接收记录", new String(Buffer_Receive, 0, Packet_Receive.getLength()));
                        ReceiverDate = new String(Buffer_Receive, 0, Packet_Receive.getLength());

                        //判断数据是否合法
                        InetSocketAddress address = (InetSocketAddress) Packet_Receive.getSocketAddress();
                        //判断是否是调度服务器的端口
                        if (address.getPort() != SERVER_PORT_AUDIO) {
                            continue;
                        }
                        setChanged();
                        notifyObservers(new String(Buffer_Receive,0,Packet_Receive.getLength()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //发送线程
    public class Thread_Send implements Runnable {
        //发送数据缓存
        private byte[] Buffer_Send = new byte[MAX_DATA_PACKET_LENGTH];
        //发送数据包
        private DatagramPacket Packet_Send;


        public Thread_Send(byte[] data, int len) {
            //发送包
            Packet_Send = new DatagramPacket(Buffer_Send, MAX_DATA_PACKET_LENGTH);
            Packet_Send.setData(data);
            Packet_Send.setLength(len);
        }
        @Override
        public void run() {
            try {
                Packet_Send.setPort(SERVER_PORT_AUDIO);
                Packet_Send.setAddress(InetAddress.getByName(SERVER_IP));
                Udp_Socket.send(Packet_Send);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
