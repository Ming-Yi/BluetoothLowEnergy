package com.yi.ming.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.yi.ming.bleclass.BluetoothLEService;
import com.yi.ming.bluetoothlowenergy.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    private String TAG = "LogDemo";
    private BluetoothLEService mBluetoothLEService;

    TextView tv_DeviceName,tv_ConnentStatus;

    Button btn_Read;
    String mDeviceAddress,mDeviceName;

    String DR,G,NB;
    int Nbit,Nnumb;

    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        findViewById();

        GetIntent();
        tv_DeviceName.setText(mDeviceName);
        btn_Read.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic characteristic1 = mBluetoothLEService.getUUIDService("197a1820-13ce-11e5-a56d-0002a5d5c51b").getCharacteristic(UUID.fromString("197a2aa3-13ce-11e5-a56d-0002a5d5c51b"));
                mBluetoothLEService.readCharacteristic(characteristic1);
            }
        });
        EnableService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLEService != null) {
            final boolean result = mBluetoothLEService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLEService = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    private void EnableService() {
        Intent gattServiceIntent = new Intent(BluetoothActivity.this, BluetoothLEService.class);
        bindService(gattServiceIntent, mServiceConnection , BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLEService = ((BluetoothLEService.LocalBinder) service).getService();
            if (!mBluetoothLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.i(TAG, "initialize Bluetooth");
            mBluetoothLEService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLEService = null;
            Log.e(TAG, "Bluetooth No initialize");
        }
    };


    private void findViewById() {
        tv_DeviceName = (TextView) findViewById(R.id.TextView_BName);
        tv_ConnentStatus = (TextView) findViewById(R.id.TextView_ConnectStatus);
        btn_Read = (Button) findViewById(R.id.btn_Read);
    }

    private void GetIntent() {
        Intent intent = BluetoothActivity.this.getIntent();
        String Info = intent.getStringExtra("Info");
        mDeviceName = Info.substring(0 ,Info.length() - 17);
        mDeviceAddress = Info.substring(Info.length() - 17);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                tv_ConnentStatus.setText("connecnt");
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                tv_ConnentStatus.setText("disconnecnt");
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "ACTION_DATA_AVAILABLE");
                final String uuid = intent.getStringExtra(BluetoothLEService.EXTRA_UUID_CHAR);
                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLEService.EXTRA_DATA_RAW);

                final StringBuilder stringBuilder = new StringBuilder(dataArr.length);
                for(byte byteChar : dataArr)
                    stringBuilder.append(String.format("%8s ", Integer.toBinaryString(byteChar & 0xFF)).replace(' ', '0'));

                if(uuid.equals("197a2aa3-13ce-11e5-a56d-0002a5d5c51b")){
                    DR = stringBuilder.toString().substring(0,4);
                    G = stringBuilder.toString().substring(4,8);
                    NB = stringBuilder.toString().substring(8,12);

                    BluetoothGattCharacteristic  characteristic2 = mBluetoothLEService.getUUIDService("197a1820-13ce-11e5-a56d-0002a5d5c51b").getCharacteristic(UUID.fromString("197a2aa1-13ce-11e5-a56d-0002a5d5c51b"));
                    mBluetoothLEService.setCharacteristicNotification(characteristic2,true);
                }
                else if(uuid.equals("197a2aa1-13ce-11e5-a56d-0002a5d5c51b")){
                    Byte PN_byte = Byte.parseByte(stringBuilder.toString().substring(8,12),2);
                    String PN = Byte.toString(PN_byte);
                    Byte PL_byte = Byte.parseByte(stringBuilder.toString().substring(12,16),2);
                    String PL = Byte.toString(PL_byte);

                    Log.i(TAG,"PN:"+ PN);
                    Log.i(TAG,"PL:"+ PL);

                    switch(NB){
                        case "0001":
                            Nbit = 8 ;
                            Nnumb = 18;
                            break;
                        case "0010":
                            Nbit=10;
                            Nnumb = 14;
                            break;
                        case "0100":
                            Nbit=12;
                            Nnumb = 12;
                            break;
                        case "1000":
                            Nbit=14;
                            Nnumb = 10;
                            break;
                    }
                    String DN = stringBuilder.toString().substring(15,stringBuilder.toString().length());
                    ArrayList data = new ArrayList();
                    for(int i=0 ; i < Nnumb ; i++){
                        data.add( binaryToHex(DN.substring(i*Nbit,(i+1)*Nbit)));
                    }
                    Log.i(TAG,"DN:"+data);
                }


            }
        }
    };

    public static int binaryToHex(String bin) {
        return Integer.parseInt(bin, 2);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
