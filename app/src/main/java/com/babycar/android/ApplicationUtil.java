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
    private OutputStream out = null;
    private InputStream in = null;

    public void init(String HOST,int PORT) throws IOException, Exception{
        this.socket = new Socket(HOST,PORT);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }
}
