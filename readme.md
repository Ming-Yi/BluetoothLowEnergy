# Bluetooth Low Energy #
## 權限(Permission) ##
- 在Androidmanifest.xml
	```
	<uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
	```
- 及Androidmanifest.xml 的 `<application>` 標籤中加上
	```
    <service android:name="com.yi.ming.bleclass.BluetoothLEService"
             android:enabled="true" />
	```
## 自訂類別(Class) ##
1. **BluetoothLEUtils** : 取得裝置的Bluetooth服務。
	- BluetoothLEUtils(final Activity activity) : 建構子。
	- CheckSupportBLE() : 檢查裝置是否支援BLE。
	- CheckEnableBT() : 檢查Bluetooth是否啟動，沒啟動則會有Dialog給使用者啟動。
	- EX:
		```java
	    mBluetoothLEUtils = new BluetoothLEUtils(this);
	    mBluetoothLEUtils.CheckSupportBLE();
	    mBluetoothLEUtils.CheckEnableBT();
		``` 
2. **BluetoothLEScanner** : 取得附近的Bluetooth裝置。
	- BluetoothLEScanner(final BluetoothLEUtils bluetoothUtils , final BluetoothAdapter.LeScanCallback leScanCallback) : 建構子。
		> **BluetoothAdapter.LeScanCallback** : 搜尋到裝置後的Callback function
	- scanLeDevice(final int duration, final boolean enable) : 搜索附近的藍芽設備。
		> **duration** : 掃秒時間  ，**enable** : 是否啟動搜索
	- isScanning() : 判斷目前是不是在搜索藍芽設備。
	    > **return** : true -> 裝置搜索中  ，false -> 未啟動搜索
	- EX:
		```java
		//BluetoothLEScanner 宣告
		BluetoothLEScanner mBluetoothLEScanner = new BluetoothLEScanner( mBluetoothLEUtils , mLeScanCallback);
		
		//Scanner Callback
	    private BluetoothAdapter.LeScanCallback mLeScanCallback =
	            new BluetoothAdapter.LeScanCallback(){
	                @Override
	                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                            if (!mDeviceAddressSet.contains(device.getAddress())) {
	                                Log.d(TAG,"Name: "+device.getName() +", address: "+ device.getAddress());
	                            }
	                        }
	                    });
	                }
	            };

		//啟動搜索(搜索時間10秒)
		mBluetoothLEScanner.scanLeDevice(10000,true);
			
		```
3. **BluetoothLEService**
	- 設定廣播 Receiver。
		```
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	    ```
		```
		private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            final String action = intent.getAction();
	            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
					//連線到GATT服務時
	            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
					//與GATT服務斷開時
	            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
					//探索GATT的服務(可以在這撰寫 fucntion 去取得該設備 characteristic 和 descriptor)
	            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
					//取得資料時

					//該設備 characteristic 的 uuid
	                final String uuid = intent.getStringExtra(BluetoothLEService.EXTRA_UUID_CHAR);

					//資料陣列
	                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLEService.EXTRA_DATA_RAW);
	            }
	        }
    	};

	    private static IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
	        return intentFilter;
	    }
		```
	- 啟動BLE服務 並且連線到 mDeviceAddress 裝置。
		```
		Intent gattServiceIntent = new Intent(BluetoothActivity.this, BluetoothLEService.class);
        bindService(gattServiceIntent, mServiceConnection , BIND_AUTO_CREATE);
		```
		```
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
		```
	- 連線特定的 characteristic (Read) : 只會讀一次 Bluetooth Device 的資料。
		```
        BluetoothGattCharacteristic characteristic = mBluetoothLEService.getUUIDService({GATT Servece UUID}).getCharacteristic(UUID.fromString({特定 characteristic UUID}));
        mBluetoothLEService.readCharacteristic(characteristic);			
		```
	- 連線特定的 characteristic (Notification) : Bluetooth Device 有回覆資料就會接收。
		```
        BluetoothGattCharacteristic  characteristic = mBluetoothLEService.getUUIDService({GATT Servece UUID}).getCharacteristic(UUID.fromString({特定 characteristic UUID}));
        mBluetoothLEService.setCharacteristicNotification(characteristic,true);			
		```
	> Read 和 Notification 都會在Receiver 的 BluetoothLEService.ACTION_DATA_AVAILABLE 去處理資料。