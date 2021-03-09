package com.example.petoibittlebluetoothcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.security.auth.callback.Callback;

public class BluetoothConnection extends Thread implements Runnable, Callback {

    Handler bluetoothIn;
    final int handlerState=0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder dataStringIn = new StringBuilder();
    private ConnectedThread myConnectionBt;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;
    private BluetoothConnectionListener bluetoothCallback;

    private boolean isBittleReady = false;
    private String bittleConnectionStatus;  // Store Bittle connection initialization messages
    private Activity activity;

    BluetoothConnection(Activity activity) {
        this.activity = activity;
    }

    public void run () {
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if(msg.what == handlerState) {
                    String received =  msg.obj.toString();
                    Log.d("RECEIVED", received);
                    if (!isBittleReady) {
                        bittleConnectionStatus = bittleConnectionStatus + received;
                        boolean isFound = bittleConnectionStatus.indexOf("Finished!") !=-1? true: false; //true
                        if (bittleConnectionStatus.length() > 10 && isFound) {
                            isBittleReady = true;
                            bittleConnectionStatus = "";
                        }
                    } else {
                        bluetoothCallback.onMessageReceived(received);
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        verifyBTStatus();
    }

    private void verifyBTStatus() {
        if (btAdapter == null) {
            Toast.makeText(activity.getBaseContext(), "Bluetooth not supported by device", Toast.LENGTH_LONG).show();
        } else {
            if(btAdapter.isEnabled()) {

            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public boolean isBittleReady() {
        return isBittleReady;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public boolean connect(Intent intent) {
        boolean res = false;
        address = intent.getStringExtra(PairedDevices.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = null;

        if(btAdapter != null) {
            // address = "5C:BA:37:FA:08:4E";
            device = btAdapter.getRemoteDevice(address);
        }

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
             Toast.makeText(activity.getBaseContext(), "Couldn't create socket.", Toast.LENGTH_LONG).show();
        }

        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {

            }
        }
        myConnectionBt = new ConnectedThread(btSocket);
        myConnectionBt.start();
        return res;
    }

    public void closeConnection() {
        try {
            btSocket.close();
            isBittleReady = false;
            bittleConnectionStatus = "";
        } catch (IOException e) {

        }
    }

    public interface BluetoothConnectionListener {
        void onMessageReceived(String msg);
    }

    private class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            resetConnection();
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] byte_in = new byte[1];
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                Toast.makeText(activity.getBaseContext(), "Connection failed: " + e.toString(), Toast.LENGTH_LONG).show();
                //finish();
            }
        }

        public void resetConnection() {
            if(mmInStream != null) {
                try {mmInStream.close();} catch (Exception e) {}
                mmInStream = null;
            }
            if(mmOutStream != null) {
                try {mmOutStream.close();} catch (Exception e) {}
                mmOutStream = null;
            }
        }
    }

}
