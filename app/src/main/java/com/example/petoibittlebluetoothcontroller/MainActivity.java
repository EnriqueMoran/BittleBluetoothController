package com.example.petoibittlebluetoothcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener{

    Handler bluetoothIn;
    Button btDisconnect;
    final int handlerState=0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder dataStringIn = new StringBuilder();
    private ConnectedThread myConnectionBt;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        btDisconnect = findViewById(R.id.btDisconnect);

        bluetoothIn = new Handler() {
          public void handleMessage(android.os.Message msg) {
              if(msg.what == handlerState) {
                  Log.d("RECEIVED", (String) msg.obj);
              }
          }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        verifyBTStatus();

        btDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    btSocket.close();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Error disconnecting Bluetooth", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {
        switch (id)
        {
           /* case R.id.joystickRight:
                Log.d("Right Joystick", "X percent: " + xPercent + " Y percent: " + yPercent);
                break;*/
            case R.id.joystickLeft:
                Log.d("Left Joystick", "X percent: " + xPercent + " Y percent: " + yPercent);

              //  myConnectionBt.write("a");
                break;
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(PairedDevices.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = null;

        if(btAdapter != null) {
            device = btAdapter.getRemoteDevice(address);
        }

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Couldn't create socket.", Toast.LENGTH_LONG).show();
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
    }

    public void onPause() {
        super.onPause();

        try {
            btSocket.close();
        } catch (IOException e) {

        }
    }

    private void verifyBTStatus() {
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Bluetooth not supported by device", Toast.LENGTH_LONG).show();
        } else {
            if(btAdapter.isEnabled()) {

            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
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
                Toast.makeText(getBaseContext(), "Connection failed.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}

