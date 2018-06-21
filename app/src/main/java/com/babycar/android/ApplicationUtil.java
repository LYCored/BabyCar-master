package com.babycar.android;

import android.app.Application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ApplicationUtil extends Application {

    private Socket socket;
    private Socket videoSocket;
    private OutputStream out = null;
    private OutputStream videoOut = null;
    private InputStream in = null;
    private InputStream videoIn = null;

    public void init(String HOST,int PORT) throws IOException, Exception{
        this.socket = new Socket(HOST,PORT);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
    }

    public void initVSocket(String HOST,int PORT) throws IOException, Exception{
        this.videoSocket = new Socket(HOST,PORT);
        this.videoOut = videoSocket.getOutputStream();
        this.videoIn = videoSocket.getInputStream();
    }

    public Socket getSocket() {
        return socket;
    }

    public Socket getVSocket() {
        return videoSocket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStream getVOut() {
        return videoOut;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public InputStream getIn() {
        return in;
    }

    public InputStream getVIn() {
        return videoIn;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }
}
