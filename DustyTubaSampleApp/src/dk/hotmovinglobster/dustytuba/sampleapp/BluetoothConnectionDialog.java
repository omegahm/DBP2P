/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current connection session.
 * TODO: Currently a debug connection dialog, we want a nicer one that looks more like a progress dialog that can be canceled!
 */
public class BluetoothConnectionDialog extends Activity {
    // Debugging
    private static final String TAG = "BluetoothConnectionDialog";
    private static final boolean D = false;
    
	/* This Intent: Extras ( use with .name() )*/
    public enum BT_CONN_DATA {
		SERVER,  /** Act as server (listen w/ BluetoothServerSocket)? */
		MAC, 	 /** MAC to establish connection to */
		UUID,    /** Application specific UUID */
		SDP_NAME /** Application specific name (for SDP)*/
	}

    // Message types sent from the BluetoothConnectionService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothConnectionService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private ListView mConversationView;
	private Button mSendButton;
    private Button mCancelButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the connection services
    private BluetoothConnectionService mConnService = null;
	private boolean isServer;
	private String mac;
	private String uuid;
	private String sdp_name;
	private BluetoothDevice device;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        Bundle extras = getIntent().getExtras();
        isServer = extras.getBoolean(BT_CONN_DATA.SERVER.name());
        mac = extras.getString(BT_CONN_DATA.MAC.name());
        uuid = extras.getString(BT_CONN_DATA.UUID.name());
        sdp_name = extras.getString(BT_CONN_DATA.SDP_NAME.name());
               
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bt_conn_dialog);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
       
        // If BT is not on, request that it be enabled.
        // setupUI() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the connection session
        } else {
            if (mConnService == null) setupUI();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        if (D) mConversationArrayAdapter.add("onResume()");
        
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mConnService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mConnService.getState() == BluetoothConnectionService.STATE_NONE) {
              // Start the Bluetooth connection services
              mConnService.start();
              if (D) mConversationArrayAdapter.add("Listening for connections");
            }
        }
        
        // HACK: Always connect on start
        if (!isServer){
	        device = mBluetoothAdapter.getRemoteDevice(mac);
	        // HACK: Ugly hack for 2 sec delay, since client might establish connection before server listens
	        // TODO: Proper Delay w/ Retry and Error code is the way to go
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
        	if (D) mConversationArrayAdapter.add("Connecting to server");
	        mConnService.connect(device);  
        }        
    }

    private void setupUI() {
        Log.d(TAG, "setupUI()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.bt_conn_dialog_message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
             
		// HACK: Present full screen
		for (int i = 0; i < 18; i++) {
			mConversationArrayAdapter.add("");
		}

        // Initialize the cancel button with a listener that for click events
        mCancelButton = (Button) findViewById(R.id.btCon_cancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	setResult(RESULT_CANCELED);
            	finish();
            }
        });
        
        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.btCon_inject);
        mSendButton.setOnClickListener(new OnClickListener() {
            private int garbage_data = 0;

			public void onClick(View v) {
                // Send a 'message'
				garbage_data += 1;
                sendMessage(Integer.toString(garbage_data).getBytes());
            }
        });

        // Initialize the BluetoothConnectionService to perform bluetooth connections
        mConnService = new BluetoothConnectionService(this, mHandler, uuid, sdp_name);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        
        // DEBUG OUTPUT
        if (D){
        mConversationArrayAdapter.add("Received on Dialog Launch");
        mConversationArrayAdapter.add("isServer: " + isServer);
        mConversationArrayAdapter.add("MAC: " + mac);
        mConversationArrayAdapter.add("UUID: " + uuid);
        mConversationArrayAdapter.add("SDP: " + sdp_name);
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth connection services
        if (mConnService != null) mConnService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mConnService.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            Toast.makeText(this, "Cannot send, BT Not connected", Toast.LENGTH_SHORT).show();
            if (D) mConversationArrayAdapter.add("ERR: " + "Cannot Send. Not connected");
            //mConnService.connect(device); // FIXME: Reestablishing connection, but loosing messages. Should probably give error instead.
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothConnectionService to write
            byte[] send = message;
            mConnService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    // The Handler that gets information back from the BluetoothConnectionService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnectionService.STATE_CONNECTED:
                    mConversationArrayAdapter.add("STATUS: " + "CONNECTED");
                    break;
                case BluetoothConnectionService.STATE_CONNECTING:
                	mConversationArrayAdapter.add("STATUS: " + "CONNECTING");
                    break;
                case BluetoothConnectionService.STATE_LISTEN:
                case BluetoothConnectionService.STATE_NONE:
                	mConversationArrayAdapter.add("STATUS: " + "NOT CONNECTED");
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("OUT:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add("IN :  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns (initated onStart)
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a connection session
                setupUI();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                // TODO: String
                Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        }
    }

}