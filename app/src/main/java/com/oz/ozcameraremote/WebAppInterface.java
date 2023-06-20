package com.oz.ozcameraremote;

import android.webkit.JavascriptInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WebAppInterface {
    private WebAppContext mContext;
    private DatagramSocket udpsocket;
    private InetAddress addr;

    /** Instantiate the interface and set the context */
    WebAppInterface(WebAppContext c) {
        try {
            udpsocket = new DatagramSocket();

        } catch (IOException e) {
            e.printStackTrace();
        }
        mContext = c;
    }

    @JavascriptInterface public boolean setcameraname(String cam) {
        mContext.cameraid = cam;
        return true;
    }

    @JavascriptInterface public boolean pause() {
        mContext.paused = true;
        return true;
    }

    @JavascriptInterface public boolean resume() {
        mContext.paused = false;
        return true;
    }

    @JavascriptInterface public boolean ispaused() {
        return mContext.paused;
    }

    @JavascriptInterface
    public boolean connect(String address, String port) {
        mContext.proxyaddress = address;
        mContext.proxyport = Integer.parseInt(port);
        return true;
    }

    @JavascriptInterface
    public String transmitdata(String address, String port, String json) {

        System.out.println(mContext.zoomval);
        try {
            addr = InetAddress.getByName(address);
            json = json.substring(0, json.length() - 1);
            json = json + ",\"zoom\": " + mContext.zoomval + "}";
            byte[] message = json.getBytes();
            DatagramPacket packet = new DatagramPacket(message, message.length, addr, Integer.parseInt(port));
            udpsocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Oh yes";
    }
}