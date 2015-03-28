package co.bt.client.btclient;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements Runnable {

    BluetoothAdapter btAdapter;
    BluetoothDevice rmDevice;
    BluetoothSocket btSocket;
    OutputStream outStream;
    InputStream inStream;
    /**/
    /*service/app uuid, 1101 SPP UUID
    * The UUID has different uses in Android as it may be customized
     * to define your application*/
    final String app_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    /*remote device mac address*/
    /*This variable shall hold the macAddress of the remote dvice we shall connect to*/
    String MACAddress = "70:F3:95:57:BF:FE";
    /*Buttons and TextView from the layout*/
    Button btnLeft, btnRight;
    TextView tv_msg;
    /*Data structure to hold the bonded devices*/
    Set<BluetoothDevice> devices;
    /*Adapter to adapt the data structure elements to the listView*/
    ArrayAdapter<String> btArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLeft = (Button) findViewById(R.id.left);
        btnRight = (Button) findViewById(R.id.right);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        Intent intent_get = getIntent();
        MACAddress = intent_get.getExtras().getString("MAC");
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("left");
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("right");
            }
        });

        ConnectToRemoteDevice();

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            /*close the bluetooth socket in case of exiting the application*/
            btSocket.close();
        } catch (Exception e) {
            Log.d("error", "an error occurred while quiting the application");
        }
    }

    /**
     * @param msg This function shall be used to send messages to the remote bluetooth device
     */
    private void sendMessage(String msg) {

        try {
            outStream.write(msg.getBytes());
            Log.d("BT", "the message " + msg + " sent successfully");
        } catch (Exception e) {
            Log.d("error", "IOException error occurred while writing output stream");
        }
    }

    void ConnectToRemoteDevice() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        rmDevice = btAdapter.getRemoteDevice(MACAddress);

        try {
            btSocket = rmDevice.createRfcommSocketToServiceRecord(UUID.fromString(app_UUID));
        } catch (Exception e) {
            Log.d("error", "UUID is NULL || UUID is not formatted correctly");
        }
        try {
            /* as spotted in SDK it's not preferable to connect to a socket while discovery is on*/
            btAdapter.cancelDiscovery();
            btSocket.connect();
            new Thread(this).start();
            Log.d("BT", "successfully connected");
        } catch (Exception e) {
            Log.d("error", "Connection failure");
            finishAffinity();
        }
        try {
            outStream = btSocket.getOutputStream();
        } catch (Exception e) {
            Log.d("error", "couldn't create output stream");
        }
    }


    @Override
    public void run() {
        //Buffer to save the received data
        byte Buffer[] = new byte[1024];
        //Number of bytes
        int n_bytes;
        //Variable to hold the incoming messages
        String msg;

        while (true) {

            if (btSocket.isConnected() == false) {
                break;
            }

            try {
                inStream = btSocket.getInputStream();
                n_bytes = inStream.read(Buffer);
                if (n_bytes >= 1) {
                    msg = new String(Buffer, 0, n_bytes);
                    Log.d("msg", "***" + msg + "***");
                    final String msg_ui = msg;
                    /*This shall be used to send actions to the UI thread*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_msg.setText("BT:" + msg_ui);
                        }
                    });
                }

            } catch (Exception e) {
                Log.d("error", "the socket is not connected__Thread");
            }

        }
    }

}