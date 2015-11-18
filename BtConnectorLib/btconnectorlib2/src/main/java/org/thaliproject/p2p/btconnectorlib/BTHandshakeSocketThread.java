// Copyright (c) Microsoft. All Rights Reserved. Licensed under the MIT License. See license.txt in the project root for further information.
package org.thaliproject.p2p.btconnectorlib;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by juksilve on 11.3.2015.
 */

class BTHandshakeSocketThread extends Thread {

    public interface HandShakeCallback{
        void handshakeMessageRead(byte[] buffer, int size, BTHandshakeSocketThread who);
        void handshakeMessageWrite(byte[] buffer, int size, BTHandshakeSocketThread who);
        void handshakeDisconnected(String error, BTHandshakeSocketThread who);
    }

    private String peerIdentifier = "";
    private String peerName = "";
    private String peerAddress = "";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final HandShakeCallback mHandler;

    public BTHandshakeSocketThread(BluetoothSocket socket, HandShakeCallback handler)  throws IOException {
        Log.i("BTHandshakeSocketThread", "Creating BTHandshakeSocketThread");
        mHandler = handler;
        mmSocket = socket;
        mmInStream = mmSocket.getInputStream();
        mmOutStream = mmSocket.getOutputStream();
    }

    public BluetoothSocket getSocket(){
        return mmSocket;
    }

    public  String getPeerId(){ return peerIdentifier;}
    public  String getPeerName(){ return peerName;}
    public  String getPeerAddress(){ return peerAddress;}
    public  void setPeerId(String value){ peerIdentifier = value;}
    public  void setPeerName(String value ){ peerName = value;}
    public  void setPeerAddress(String value ){ peerAddress = value;}

    public void run() {
        Log.i("BTHandshakeSocketThread", "BTHandshakeSocketThread started");
        byte[] buffer = new byte[255];
        int bytes;

        try {
            bytes = mmInStream.read(buffer);
            //Log.d(TAG, "ConnectedThread read data: " + bytes + " bytes");
            mHandler.handshakeMessageRead(buffer, bytes,this);
        } catch (IOException e) {
            Log.i("BTHandshakeSocketThread", "BTHandshakeSocketThread disconnected: " + e.toString());
            mHandler.handshakeDisconnected(e.toString(),this);
        }
        Log.i("BTHandshakeSocketThread", "BTHandshakeSocketThread fully stopped");
    }
    /**
     * Write to the connected OutStream.
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {

        if (mmOutStream == null) {
            return;
        }

        try {
            mmOutStream.write(buffer);
            mHandler.handshakeMessageWrite(buffer, buffer.length,this);
        } catch (IOException e) {
            // when write fails, the timeout for handshake will clear things out eventually.
            Log.i("BTHandshakeSocketThread", "BTHandshakeSocketThread  write failed: " + e.toString());
        }
    }

    public void CloseSocket() {

        if (mmInStream != null) {
            try {mmInStream.close();} catch (IOException e) {e.printStackTrace();}
        }

        if (mmOutStream != null) {
            try {mmOutStream.close();} catch (IOException e) {e.printStackTrace();}
        }

        if (mmSocket != null) {
            try {mmSocket.close();} catch (IOException e) {e.printStackTrace();}
        }
    }
}