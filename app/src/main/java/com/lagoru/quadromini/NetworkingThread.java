package com.lagoru.quadromini;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

class NetworkingThread extends Thread {
    private volatile boolean mRunning = true;
    private volatile boolean mQuadrocopterRunning = false;
    private Socket mTcpSocket;
    private byte [] mHeader;
    private byte [] mQuadrocopterOffString;
    private InputStream mInput;
    private OutputStream mOutput;
    private InetAddress mIpAddress;
    private int mPacketRate;
    private int mPort;
    private boolean mLeftActive = false, mRightActive = false;
    private int leftX = 0, leftY = 0, rightX = 0, rightY = 0;
    private Context mContext;

    public NetworkingThread(Context context){
        super();
        mContext = context;
    }

    public void setLeftX(int leftX) {
        this.leftX = leftX;
    }

    public void setLeftY(int leftY) {
        this.leftY = leftY;
    }

    public void setRightX(int rightX) {
        this.rightX = rightX;
    }

    public void setRightY(int rightY) {
        this.rightY = rightY;
    }

    public void setLeftActive(boolean value){
        mLeftActive = value;
    }

    public void setRightActive(boolean value){
        mRightActive = value;
    }

    public void updateSettings(InetAddress ipAddress, int packetRate, int port){
        mIpAddress = ipAddress;
        mPacketRate = packetRate;
        mPort = port;
    }

    public boolean connect(){
        // setup the networking
        try {
            if(mTcpSocket != null && mTcpSocket.isConnected()){
                try {
                    mTcpSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            mTcpSocket = new Socket(mIpAddress, mPort);
            mTcpSocket.setSoTimeout(500);
            mInput = mTcpSocket.getInputStream();
            mOutput = mTcpSocket.getOutputStream();
        } catch (Exception e) {
            // Networking exception
            return false;
        }

        return true;
    }
    public void run() {
        int l_x, l_y, r_x,r_y;
        byte[] write_buf;
        byte[] read_buf = new byte[200];
        String to_send;
        int read_size = 0;

        try {
            mHeader = mContext.getResources().getString(R.string.quadro_steer_header).getBytes("UTF-8");
            mQuadrocopterOffString = mContext.getResources().getString(R.string.quadrocopter_off_string).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        while(mRunning) {
            while(connect()==false){
                //probojemy sie polaczyc
            }
            while (mRunning) {
                if (mLeftActive) {
                    l_x = mapJoystick(leftX);
                    l_y = mapJoystick(leftY);
                } else {
                    l_x = 0;
                    l_y = 0;
                }
                if (mRightActive) {
                    r_x = mapJoystick(rightX);
                    r_y = mapJoystick(rightY);
                } else {
                    r_x = 0;
                    r_y = 0;
                }
                if(mQuadrocopterRunning){
                    to_send = new String(mHeader) + '_' +Integer.toString(l_x)
                            +'_'+ Integer.toString(l_y)+'_'+ Integer.toString(r_x)+'_'+ Integer.toString(r_y);
                }else{
                    to_send = new String(mQuadrocopterOffString);
                }

                try {

                    mOutput.write(to_send.getBytes());
                    read_size = mInput.read(read_buf);
                    Thread.sleep(mPacketRate);
                } catch (IOException e) {
                    Log.e("Connection Error", e.toString());
                    break; // jezeli cokolwiek zlego to ponowne polaczenie w wyzszej petli
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static double mapValue(double input, double inMin, double inMax, double outMin, double outMax) {
        return (input - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    private static byte mapJoystick(int input) {
        int result = (int) mapValue((double) input, -150, 150, 0, 255);

        if (result < 0)
            result = 0;
        else if (result > 255)
            result = 255;

        return (byte) result;
    }

    public synchronized void requestStop() {
        mRunning = false;
    }

    public void setQuadrocopterRunning(boolean mQuadrocopterRunning) {
        this.mQuadrocopterRunning = mQuadrocopterRunning;
    }

    public boolean isQuadrocopterRunning() {
        return mQuadrocopterRunning;
    }
}