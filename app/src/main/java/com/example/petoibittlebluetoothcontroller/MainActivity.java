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
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

enum Command {
    REST,
    FORWARD,
    GYRO,
    LEFT,
    BALANCE,
    RIGHT,
    SHUTDOWN,
    BACKWARD,
    CALIBRATION,
    STEP,
    CRAWL,
    WALK,
    TROT,
    LOOKUP,
    BUTTUP,
    RUN,
    BOUND,
    GREETING,
    PUSHUP,
    PEE,
    STRETCH,
    SIT,
    ZERO
}

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
    private ProgressBar progressBar;

    private Button restButton;
    private Button gyroButton;
    private Button stepButton;
    private Button crawlButton;
    private Button walkButton;
    private Button trotButton;
    private Button standButton;

    // Instruction map, values must be the same as in your OpenCat.h file!
    private HashMap<Command, String> instructionMap = new HashMap<Command, String>();

    private Command lastDirection;  // Used to send new direction movement command

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        // Initialize instruction map
        instructionMap.put(Command.REST, "kd");
        instructionMap.put(Command.FORWARD, "F");
        instructionMap.put(Command.GYRO, "g");
        instructionMap.put(Command.LEFT, "L");
        instructionMap.put(Command.BALANCE, "kbalance");
        instructionMap.put(Command.RIGHT, "R");
        instructionMap.put(Command.SHUTDOWN, "z");
        instructionMap.put(Command.BACKWARD, "B");
        instructionMap.put(Command.CALIBRATION, "c");
        instructionMap.put(Command.STEP, "vt");
        instructionMap.put(Command.CRAWL, "cr");
        instructionMap.put(Command.WALK, "wk");
        instructionMap.put(Command.TROT, "tr");
        instructionMap.put(Command.LOOKUP, "lu");
        instructionMap.put(Command.BUTTUP, "buttUp");
        instructionMap.put(Command.RUN, "rn");
        instructionMap.put(Command.BOUND, "bd");
        instructionMap.put(Command.GREETING, "hi");
        instructionMap.put(Command.PUSHUP, "pu");
        instructionMap.put(Command.PEE, "pee");
        instructionMap.put(Command.STRETCH, "str");
        instructionMap.put(Command.SIT, "sit");
        instructionMap.put(Command.ZERO, "zero");

        lastDirection = Command.BALANCE;

        progressBar = findViewById(R.id.idProgressBar);
        btDisconnect = findViewById(R.id.btDisconnect);
        restButton = findViewById(R.id.btRest);
        gyroButton = findViewById(R.id.btGyro);
        stepButton = findViewById(R.id.btStep);
        crawlButton = findViewById(R.id.btCrawl);
        walkButton = findViewById(R.id.btWalk);
        trotButton = findViewById(R.id.btTrot);
        standButton = findViewById(R.id.btStand);

        // Set Buttons functionality
        btDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    String text = instructionMap.get(Command.SHUTDOWN);
                    myConnectionBt.write(text);
                    btSocket.close();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Error disconnecting Bluetooth", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        restButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.REST);
                myConnectionBt.write("text");
            }
        });

        gyroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.GYRO);
                myConnectionBt.write(text);
            }
        });

        stepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.STEP);
                myConnectionBt.write(text);
            }
        });

        crawlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.CRAWL);
                myConnectionBt.write(text);
            }
        });

        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.WALK);
                myConnectionBt.write(text);
            }
        });

        trotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.TROT);
                myConnectionBt.write(text);
            }
        });

        standButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = instructionMap.get(Command.BALANCE);
                myConnectionBt.write(text);
            }
        });

        // Set Bluetooth message receiver
        bluetoothIn = new Handler() {
          public void handleMessage(android.os.Message msg) {
              if(msg.what == handlerState) {
                  Log.d("RECEIVED", (String) msg.obj.toString());
              }
          }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        verifyBTStatus();
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {
        switch (id)
        {
           /* case R.id.joystickRight:
                Log.d("Right Joystick", "X percent: " + xPercent + " Y percent: " + yPercent);
                break;*/
            case R.id.joystickLeft:
                double angle = getAngle(xPercent, yPercent);
                Command newDirection = getNewDirection(angle);
                if (lastDirection != newDirection) {
                    String text = instructionMap.get(newDirection);
                    myConnectionBt.write(text);
                    lastDirection = newDirection;
                }
                Log.d("Left Joystick", "X percent: " + xPercent + " Y percent: " +
                        yPercent + " Angle: " + angle + " Direction: " + newDirection.toString());
                break;
        }
    }

    private double getAngle(float xPercent, float yPercent) {
        double angle_deg = 0;
        if (xPercent > 0 && yPercent > 0) {  // First quadrant
             double angle_rad = Math.atan2(xPercent, yPercent);
             angle_deg = angle_rad * 180 / Math.PI;
        } else if (xPercent > 0 && yPercent < 0) {  // Second quadrant
            double angle_rad = Math.atan2(xPercent, yPercent);
            angle_deg = angle_rad * 180 / Math.PI;
        } else if (xPercent < 0 && yPercent < 0) {  // Third quadrant
            double angle_rad = Math.atan2(xPercent, yPercent);
            angle_deg = angle_rad * 180 / Math.PI;
            angle_deg = 360 + angle_deg;
        } else if (xPercent < 0 && yPercent > 0) {  // Fourth quadrant
            double angle_rad = Math.atan2(xPercent, yPercent);
            angle_deg = angle_rad * 180 / Math.PI;
            angle_deg = 360 + angle_deg;
        }
        return angle_deg;
    }

    private Command getNewDirection(double angle) {
        Command res = Command.BALANCE;  // Neutral position
        if (angle >= 315 || angle <= 45) {
            if (angle < 0.01) {
                res = Command.BALANCE;
            } else {
                res = Command.FORWARD;
            }
        } else if (angle > 45 && angle <= 135) {
            res = Command.RIGHT;
        } else if (angle > 135 && angle <= 225) {
            res = Command.BACKWARD;
        } else if (angle > 225 && angle <= 315) {
            res = Command.LEFT;
        }
        return res;
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
            // address = "5C:BA:37:FA:08:4E";
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
                Toast.makeText(getBaseContext(), "Connection failed: " + e.toString(), Toast.LENGTH_LONG).show();
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

