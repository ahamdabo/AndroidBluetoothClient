package co.bt.client.btclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class BluetoothList extends ActionBarActivity {

    /*ListView that shall display the bluetooth devices list*/
    ListView listView;
    /*Array adapter to set the list view with bluetooth devices*/
    ArrayAdapter<String> arrAdapter;
    /*Mobile bluetooth device*/
    BluetoothAdapter btAdapter;
    /*A set of Bluetooth Devices structure that shall hold the the bonded bluetooth devices*/
    Set<BluetoothDevice> btDevices;
    /*A set structure that shall hold all devices(paired/found initialized as a hashSet of
    * BluetoothDevices to enable adding/removing devices from the list*/
    Set<BluetoothDevice> all_dev = new HashSet<BluetoothDevice>();
    /*request code for the Enabling Bluetooth message*/
    final int BT_EN_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);
        /*Initializing at the "OnResume" to ensure removing any redundant device from the list*/
        arrAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        /*Get the local device and ensure enabling the bluetooth*/
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent bt_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bt_intent, BT_EN_REQUEST);
        }
        /*starting the remote device discovery*/
        btAdapter.startDiscovery();
        /*Register for a broadcast receiver, every time the bluetooth finds a remote device the
        * BluetoothFound() will be called by the system */
        registerReceiver(BluetoothFound,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        btDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice d : btDevices) {
            arrAdapter.add(d.getName());
            all_dev.add(d);
        }

        /*Assign adapter to ListView*/
        listView.setAdapter(arrAdapter);
        /*handling list items click and passing the MACAddress to the main Activity */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item index
                for (BluetoothDevice d : all_dev) {
                    if (arrAdapter.getItem(position).equals(d.getName())) {
                        Toast.makeText(getBaseContext(), d.getAddress(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BluetoothList.this, MainActivity.class);
                        intent.putExtra("MAC", d.getAddress());
                        startActivity(intent);
                    }

                }

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(BluetoothFound);
        btAdapter.cancelDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BT_EN_REQUEST) {
            if (!(resultCode == RESULT_OK)) {
                finish();
            }
        }
    }


    private final BroadcastReceiver BluetoothFound = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                /*adding the new device to the list view*/
                arrAdapter.add(device.getName());
                /*adding the device to the global Bluetooth Devices Set*/
                all_dev.add(device);
            }
        }
    };
}
