package com.yi.ming.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.yi.ming.bleclass.BluetoothLEScanner;
import com.yi.ming.bleclass.BluetoothLEUtils;
import com.yi.ming.bluetoothlowenergy.R;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothLEUtils mBluetoothLEUtils ;
    private BluetoothLEScanner mBluetoothLEScanner;

    private Set<String> mDeviceAddressSet;

    private Button scan;
    private ListView deviceList;
    private ArrayAdapter listAdapter;
    private String TAG = "LogDemo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //存放搜尋到的Bluetooth Device Address
        mDeviceAddressSet = new HashSet<>();

        findViewById();
        //設定ListView  Adapter
        SetListAdapter();
        //Bluetooth 初始化
        BluetoothLEinit();
        //設定按鈕 Listener
        ButtonClickListener();

    }

    private void ButtonClickListener() {
        scan.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果不在Bluetooth 搜尋狀態，就clear ListView的數值
                if(!mBluetoothLEScanner.isScanning()){
                    listAdapter.clear();
                    mDeviceAddressSet.clear();
                }
                //搜尋開始(10秒)
                mBluetoothLEScanner.scanLeDevice(10000,true);
            }
        });
    }

    private void BluetoothLEinit() {
        mBluetoothLEUtils = new BluetoothLEUtils(this);
        //檢查是否支援BLE
        mBluetoothLEUtils.CheckSupportBLE();
        //檢查Bluetooth 是否開啟
        mBluetoothLEUtils.CheckEnableBT();
        mBluetoothLEScanner = new BluetoothLEScanner( mBluetoothLEUtils , mLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mDeviceAddressSet.contains(device.getAddress())) {
                                Log.d(TAG,"Name: "+device.getName() +", address: "+ device.getAddress());
                                mDeviceAddressSet.add(device.getAddress());
                                listAdapter.add(device.getName()+"\n"+device.getAddress());
                            }
                        }
                    });
                }
            };

    private void findViewById() {
        scan = (Button)findViewById(R.id.button_Scan);
        deviceList = (ListView)findViewById(R.id.listView_Devices);
    }

    private void SetListAdapter() {
        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        deviceList.setAdapter(listAdapter);
        deviceList.setOnItemClickListener(adapterOnItemClickListener);
    }

    private AdapterView.OnItemClickListener adapterOnItemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            Log.i(TAG,info);

            //將info 資料傳到 BluetoothActivity
            Intent intent = new Intent(MainActivity.this,BluetoothActivity.class);
            intent.putExtra("Info",info);
            startActivity(intent);
        }
    };
}
