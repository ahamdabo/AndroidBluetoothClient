package co.bt.client.btclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    final int BT_EN_REQUEST = 1;
    /*service/app uuid, 1101 SPP UUID*/
    final String app_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    /*remote device mac address*/
    /*You shall modify this to match your remote device mac address*/
    final String MACAddress = "70:F3:95:57:BF:FE";
    Button btnLeft, btnRight;
    TextView tv_msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLeft = (Button) findViewById(R.id.left);
        btnRight = (Button) findViewById(R.id.right);
        tv_msg = (TextView) findViewById(R.id.tv_msg);

    }

    @Override
    protected void onResume() {
        super.onResume();

        btnLeft.setEnabled(false);
        btnRight.setEnabled(false);
        /*My phone bluetooth .. */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, BT_EN_REQUEST);
            } else {
                btnLeft.setEnabled(true);
                btnRight.setEnabled(true);
            }
        } else {
            finish();
        }

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

        rmDevice = btAdapter.getRemoteDevice(MACAddress);
        try {
            btSocket = rmDevice.createRfcommSocketToServiceRecord(UUID.fromString(app_UUID));
        } catch (Exception e) {
            Log.d("error", "UUID is NULL || UUID is not formatted correctly");
            finish();
        }

        try {
            btAdapter.cancelDiscovery();/*not preferable to connect to a socket while discovery is on*/
            btSocket.connect();
            new Thread(this).start();
            Log.d("BT", "successfully connected");
        } catch (Exception e) {
            Log.d("error", "Connection failure");
        }
        try {
            outStream = btSocket.getOutputStream();
        } catch (Exception e) {
            Log.d("error", "couldn't create output stream");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /*Check enable bluetooth request result*/
        if (requestCode == BT_EN_REQUEST) {
            if (resultCode == RESULT_OK) {
                btnLeft.setEnabled(true);
                btnRight.setEnabled(true);
            } else {
                finish();
            }
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        //Buffer to save the received data
        byte Buffer[] = new byte[1024];
        int n_bytes;
        String msg;
        while (true) {
            try {

                inStream = btSocket.getInputStream();
                n_bytes = inStream.read(Buffer);
                if (n_bytes >= 1) {
                    msg = new String(Buffer, 0, n_bytes);
                    Log.d("msg", "***" + msg + "***");
                        final String msg_ui = msg;
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
