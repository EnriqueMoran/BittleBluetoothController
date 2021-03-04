package com.example.petoibittlebluetoothcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;

/*************************************************************************************************
 *  CODE TAKEN FROM https://www.youtube.com/watch?v=YqNIN4t7IuM&t=1689s&ab_channel=INNOVADOMOTICS
 ************************************************************************************************/

public class PairedDevices extends AppCompatActivity {

    private static final String TAG = "PairedDevices";
    ListView idList;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter m_bt_adapter;
    private ArrayAdapter m_paired_devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_paired);
    }

    public void onResume() {
        super.onResume();

        verifyBTStatus();
        m_paired_devices = new ArrayAdapter(this, R.layout.found_devices);
        idList = (ListView) findViewById(R.id.idList);
        idList.setAdapter(m_paired_devices);
        idList.setOnItemClickListener(m_device_click_listener);
        m_bt_adapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> paired_devices = Collections.emptySet();
        if(m_bt_adapter != null) {
            paired_devices = m_bt_adapter.getBondedDevices();
        }
        if(paired_devices.size() > 0) {
            for(BluetoothDevice device : paired_devices) {
                m_paired_devices.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private AdapterView.OnItemClickListener m_device_click_listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            finishAffinity();
            Intent intent = new Intent(PairedDevices.this, MainActivity.class);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intent);
        }
    };

    private void verifyBTStatus() {
        m_bt_adapter = BluetoothAdapter.getDefaultAdapter();

        if(m_bt_adapter == null) {
            Toast.makeText(getBaseContext(), "Bluetooth not supported by device.", Toast.LENGTH_SHORT).show();
        } else {
            if(m_bt_adapter.isEnabled()) {
                Log.d(TAG, "BLuetooth already enabled");
            } else {
                Intent enable_BT_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enable_BT_intent, 1);
            }
        }
    }
}