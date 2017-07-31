

package com.amarder.cordova.aws.userpool;

import android.Manifest;
import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Build;

import android.provider.Settings;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
//import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.*;

// AWS stuff

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;


//[AM] How to get logcat to display android crash stack trace
//logcat AndroidRuntime:E *:S

public class AwsUserPoolPlugin extends CordovaPlugin  {
    // actions
  
    private static final String ACT_INIT 						= "init";   
    private static final String ACT_SIGNIN 					= "signIn";
    private static final String ACT_OFFLINE_SIGNIN 			= "offlineSignIn";
    private static final String ACT_SIGNOUT 					= "signOut";
    private static final String ACT_SIGNUP 					= "signUp";
    private static final String ACT_CONFIRM_SIGNUP 			= "confirmSignUp";
    private static final String ACT_FORGOT_PWD 				= "forgotPassword";
    private static final String ACT_UPDATE_PWD 				= "updatePassword";
    private static final String ACT_GET_DETAILS 				= "getDetails";
    private static final String ACT_RESEND_CONF_CODE 			= "resendConfirmationCode";
    private static final String ACT_CREATE_COGNITO_DATASET 	= "createAWSCognitoDataset";
    private static final String ACT_GET_USR_DATA_COGNITO_SYNC 	= "getUserDataCognitoSync";
    private static final String ACT_SET_USR_DATA_COGNITO_SYNC 	= "setUserDataCognitoSync";
    private static final String ACT_CALL_LAMBDA_FUNC 			= "callAWSLambdaFunction";
    
    
    public static final String TAG = "AwsUserPoolPlugin";
   
    private CallbackContext _curContext = null;
    private static String _username;
    private static String _password;
   // private static CognitoConfig _cognitoConfig = new CognitoConfig();
    
    
    /*callbacks
    CallbackContext discoverCallback;
    private CallbackContext enableBluetoothCallback;
	
    // key is the MAC Address
    Map<String, Peripheral> peripherals = new LinkedHashMap<String, Peripheral>();

    // scan options
    boolean reportDuplicates = false;

    // Android 23 requires new permissions for BluetoothLeScanner.startScan()
    private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int PERMISSION_DENIED_ERROR = 20;
    private CallbackContext permissionCallback;
    private UUID[] serviceUUIDs;
    private int scanSeconds;

    // Bluetooth state notification
    CallbackContext stateCallback;
    BroadcastReceiver stateReceiver;
    Map<Integer, String> bluetoothStates = new Hashtable<Integer, String>() {{
        put(BluetoothAdapter.STATE_OFF, "off");
        put(BluetoothAdapter.STATE_TURNING_OFF, "turningOff");
        put(BluetoothAdapter.STATE_ON, "on");
        put(BluetoothAdapter.STATE_TURNING_ON, "turningOn");
    }};
	
    public void onDestroy() {
        //removeStateListener();
    }

    public void onReset() {
        //removeStateListener();
    }*/

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "action = " + action);
        
        _curContext = callbackContext; // save context for this invocation
/*
        if (bluetoothAdapter == null) {
            Activity activity = cordova.getActivity();
            boolean hardwareSupportsBLE = activity.getApplicationContext()
                                            .getPackageManager()
                                            .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
                                            Build.VERSION.SDK_INT >= 18;
            if (!hardwareSupportsBLE) {
              LOG.w(TAG, "This hardware does not support Bluetooth Low Energy.");
              callbackContext.error("This hardware does not support Bluetooth Low Energy.");
              return false;
            }
            BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
		*/
        boolean validAction = true;
        if (action.equals(ACT_INIT)) {
        	
            /*UUID[] serviceUUIDs = parseServiceUUIDList(args.getJSONArray(0));
            int scanSeconds = args.getInt(1);
            resetScanOptions();
            findLowEnergyDevices(callbackContext, serviceUUIDs, scanSeconds);*/
        	handleInit(args);

        } else if (action.equals(ACT_SIGNIN )) {

           /* UUID[] serviceUUIDs = parseServiceUUIDList(args.getJSONArray(0));
            resetScanOptions();
            findLowEnergyDevices(callbackContext, serviceUUIDs, -1);*/
        	handleSignIn(args);

        } else if (action.equals(ACT_OFFLINE_SIGNIN)) {

           /* bluetoothAdapter.stopLeScan(this);
            callbackContext.success();*/
        	handleOfflineSignIn();

        } else if (action.equals(ACT_SIGNOUT )) {

            //listKnownDevices(callbackContext);
        	handleSignOut();

        } else if (action.equals( ACT_SIGNUP)) {

            //String macAddress = args.getString(0);
           // connect(callbackContext, macAddress);
        	handleSignUp();

        } else if (action.equals(ACT_CONFIRM_SIGNUP )) {

            /*String macAddress = args.getString(0);
            disconnect(callbackContext, macAddress);*/
        	handleConfirmSignUp();

        } else if (action.equals(ACT_FORGOT_PWD )) {

            /*String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            read(callbackContext, macAddress, serviceUUID, characteristicUUID);*/
        	handleForgotPassword();

        } else if (action.equals(ACT_UPDATE_PWD )) {

            /*String macAddress = args.getString(0);
            readRSSI(callbackContext, macAddress);*/
        	handleUpdatePassword();

        } else if (action.equals(ACT_GET_DETAILS)) {

           /* String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            byte[] data = args.getArrayBuffer(3);
            int type = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
            write(callbackContext, macAddress, serviceUUID, characteristicUUID, data, type);*/
        	handleGetDetails();

        } else if (action.equals(ACT_RESEND_CONF_CODE)) {

           /* String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            byte[] data = args.getArrayBuffer(3);
            int type = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
            write(callbackContext, macAddress, serviceUUID, characteristicUUID, data, type);*/
        	handleResendConfCode();

        } else if (action.equals(ACT_CREATE_COGNITO_DATASET)) {

            /*String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            registerNotifyCallback(callbackContext, macAddress, serviceUUID, characteristicUUID);*/
        	handleCreateCognitoDataset();

        } else if (action.equals(ACT_GET_USR_DATA_COGNITO_SYNC)) {

           /* String macAddress = args.getString(0);
            UUID serviceUUID = uuidFromString(args.getString(1));
            UUID characteristicUUID = uuidFromString(args.getString(2));
            removeNotifyCallback(callbackContext, macAddress, serviceUUID, characteristicUUID);*/
        	handleGetUserDataCognitoSync();

        } else if (action.equals(ACT_SET_USR_DATA_COGNITO_SYNC)) {

            /*if (bluetoothAdapter.isEnabled()) {
                callbackContext.success();
            } else {
                callbackContext.error("Bluetooth is disabled.");
            }*/
        	handleSetUserDataCognitoSync();

        } else if (action.equals(ACT_CALL_LAMBDA_FUNC)) {

            /*String macAddress = args.getString(0);

            if (peripherals.containsKey(macAddress) && peripherals.get(macAddress).isConnected()) {
                callbackContext.success();
            } else {
                callbackContext.error("Not connected.");
            }*/
        	handleCallLambdaFunc();

        } else {

            validAction = false;

        }
        return validAction;
    }
    
    //------------------------------------
    private void handleInit(CordovaArgs args){
    	
    	try{
    		/*if(!_cognitoConfig.init(args)){
    			throw (new Error("Error initializing cognito config"));   							
    		}*/   	
    		AppHelper.init(cordova.getActivity().getApplicationContext(),args);
    		LOG.d(TAG,"Initialized App Helper object");   
    		_curContext.success();
    	}	
    	catch(Error e){
    		_curContext.error("Exception : "+e.getMessage());
    	} 
    	catch(JSONException jsonExp){
    		_curContext.error("JSON Exception : "+jsonExp.getMessage());
    	}    	  	 
    }
    //------------------------------------
    private void handleSignIn(CordovaArgs args){
    	
    	try{
    		
    		JSONObject obj = new JSONObject(args.getString(0));
    		_username = obj.getString("username");
    		_password = obj.getString("password");
    		AppHelper.setUser(_username);
    		AppHelper.getPool().getUser(_username).getSessionInBackground(authenticationHandler);
    		// tell cordova that action is in progress
    		setActionInProgressResponse();   		
    	}	
    	catch(Error e){
    		_curContext.error("Exception : "+e.getMessage());
    	} 
    	catch(JSONException jsonExp){
    		_curContext.error("JSON Exception : "+jsonExp.getMessage());
    	}    	  	    	
    }
    //------------------------------------
    private void handleOfflineSignIn(){
    	_curContext.error("Offline sign-in not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleSignOut(){
    	_curContext.error("Sign out not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleSignUp(){
    	_curContext.error("Sign up not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleConfirmSignUp(){
    	_curContext.error("Confirm sign up not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleForgotPassword(){
    	_curContext.error("Forgot password  not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleUpdatePassword(){
    	_curContext.error("Update password  not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleGetDetails(){
    	_curContext.error("Get Details not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleResendConfCode(){
    	_curContext.error("Resent confirmation  not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleCreateCognitoDataset(){
    	_curContext.error("Create Cognito Dataset not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleGetUserDataCognitoSync(){
    	_curContext.error("Get User data  not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleSetUserDataCognitoSync(){
    	_curContext.error("Set user data  not yet implemenetd on Android");
    }
    //------------------------------------
    private void handleCallLambdaFunc(){
    	_curContext.error("Call Lambda function  not yet implemenetd on Android");
    }
    //------------------------------------
    // Callback that indicates that we're in progress
    private  void setActionInProgressResponse() {
 		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
	    result.setKeepCallback(true);
	    _curContext.sendPluginResult(result);
    }
    
	//---------------------------------------
	private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
	    
	    LOG.d(TAG,"Getting user authentication with username: "+ _username + ". password: " + _password);
	    AuthenticationDetails authenticationDetails = new AuthenticationDetails(_username, _password, null);
	    continuation.setAuthenticationDetails(authenticationDetails);
	    continuation.continueTask();
	}
	//======================================


	//
	AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
	    @Override
	    public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
	        LOG.d(TAG, "Auth Success");
	        AppHelper.setCurrSession(cognitoUserSession);
	        AppHelper.newDevice(device);     
	        _curContext.success();
	    }
	
	    @Override
	    public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
	        Locale.setDefault(Locale.US);
	        getUserAuthentication(authenticationContinuation, username);
	    }
	
	    @Override
	    public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
	       // mfaAuth(multiFactorAuthenticationContinuation);
	    	_curContext.error("MFA not yet implemenetd on Android");
	    }
	
	    @Override
	    public void onFailure(Exception e) {
	    	
	    	String formattedError = AppHelper.formatException(e);    	
	    	LOG.d(TAG,"Sign-in failed: "+ formattedError);
	    	_curContext.error("Sign-in failed: " + formattedError);
	    	
	    	/*
	        closeWaitDialog();
	        TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
	        label.setText("Sign-in failed");
	        inPassword.setBackground(getDrawable(R.drawable.text_border_error));
	
	        label = (TextView) findViewById(R.id.textViewUserIdMessage);
	        label.setText("Sign-in failed");
	        inUsername.setBackground(getDrawable(R.drawable.text_border_error));
	
	        showDialogMessage("Sign-in failed", AppHelper.formatException(e));*/
	    }
	
	    @Override
	    public void authenticationChallenge(ChallengeContinuation continuation) {
	        
	    	_curContext.error("Authentication challenge not yet implemented on Android ");
	    	/**
	         * For Custom authentication challenge, implement your logic to present challenge to the
	         * user and pass the user's responses to the continuation.
	         */
	    	/*
	        if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
	            // This is the first sign-in attempt for an admin created user
	            newPasswordContinuation = (NewPasswordContinuation) continuation;
	            AppHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
	                    newPasswordContinuation.getRequiredAttributes());
	            closeWaitDialog();
	            firstTimeSignIn();
	        }*/
	    }
	};













/*
    private UUID[] parseServiceUUIDList(JSONArray jsonArray) throws JSONException {
        List<UUID> serviceUUIDs = new ArrayList<UUID>();
        
        LOG.d(TAG,"parseServiceUUIDList");
        for(int i = 0; i < jsonArray.length(); i++){
            String uuidString = jsonArray.getString(i);
            LOG.d(TAG,"curString = "+uuidString);
            serviceUUIDs.add(uuidFromString(uuidString));
        }

        return serviceUUIDs.toArray(new UUID[jsonArray.length()]);
    }

    private void onBluetoothStateChange(Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            sendBluetoothStateChange(state);
        }
    }

    private void sendBluetoothStateChange(int state) {
        if (this.stateCallback != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, this.bluetoothStates.get(state));
            result.setKeepCallback(true);
            this.stateCallback.sendPluginResult(result);
        }
    }

    private void addStateListener() {
        if (this.stateReceiver == null) {
            this.stateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onBluetoothStateChange(intent);
                }
            };
        }

        try {
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            webView.getContext().registerReceiver(this.stateReceiver, intentFilter);
        } catch (Exception e) {
            LOG.e(TAG, "Error registering state receiver: " + e.getMessage(), e);
        }
    }

    private void removeStateListener() {
        if (this.stateReceiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.stateReceiver);
            } catch (Exception e) {
                LOG.e(TAG, "Error unregistering state receiver: " + e.getMessage(), e);
            }
        }
        this.stateCallback = null;
        this.stateReceiver = null;
    }

    private void connect(CallbackContext callbackContext, String macAddress) {
        Peripheral peripheral = peripherals.get(macAddress);
        if (peripheral != null) {
            peripheral.connect(callbackContext, cordova.getActivity());
        } else {
            callbackContext.error("Peripheral " + macAddress + " not found.");
        }

    }

    private void disconnect(CallbackContext callbackContext, String macAddress) {

        Peripheral peripheral = peripherals.get(macAddress);
        if (peripheral != null) {
            peripheral.disconnect();
        }
        callbackContext.success();

    }

    private void read(CallbackContext callbackContext, String macAddress, UUID serviceUUID, UUID characteristicUUID) {

        Peripheral peripheral = peripherals.get(macAddress);

        if (peripheral == null) {
            callbackContext.error("Peripheral " + macAddress + " not found.");
            return;
        }

        if (!peripheral.isConnected()) {
            callbackContext.error("Peripheral " + macAddress + " is not connected.");
            return;
        }

        //peripheral.readCharacteristic(callbackContext, serviceUUID, characteristicUUID);
        peripheral.queueRead(callbackContext, serviceUUID, characteristicUUID);

    }

    private void readRSSI(CallbackContext callbackContext, String macAddress) {

        Peripheral peripheral = peripherals.get(macAddress);

        if (peripheral == null) {
            callbackContext.error("Peripheral " + macAddress + " not found.");
            return;
        }

        if (!peripheral.isConnected()) {
            callbackContext.error("Peripheral " + macAddress + " is not connected.");
            return;
        }
        peripheral.queueReadRSSI(callbackContext);
    }

    private void write(CallbackContext callbackContext, String macAddress, UUID serviceUUID, UUID characteristicUUID,
                       byte[] data, int writeType) {

        Peripheral peripheral = peripherals.get(macAddress);

        if (peripheral == null) {
            callbackContext.error("Peripheral " + macAddress + " not found.");
            return;
        }

        if (!peripheral.isConnected()) {
            callbackContext.error("Peripheral " + macAddress + " is not connected.");
            return;
        }

        //peripheral.writeCharacteristic(callbackContext, serviceUUID, characteristicUUID, data, writeType);
        peripheral.queueWrite(callbackContext, serviceUUID, characteristicUUID, data, writeType);

    }

    private void registerNotifyCallback(CallbackContext callbackContext, String macAddress, UUID serviceUUID, UUID characteristicUUID) {

        Peripheral peripheral = peripherals.get(macAddress);
        if (peripheral != null) {

            if (!peripheral.isConnected()) {
                callbackContext.error("Peripheral " + macAddress + " is not connected.");
                return;
            }

            //peripheral.setOnDataCallback(serviceUUID, characteristicUUID, callbackContext);
            peripheral.queueRegisterNotifyCallback(callbackContext, serviceUUID, characteristicUUID);

        } else {

            callbackContext.error("Peripheral " + macAddress + " not found");

        }

    }

    private void removeNotifyCallback(CallbackContext callbackContext, String macAddress, UUID serviceUUID, UUID characteristicUUID) {

        Peripheral peripheral = peripherals.get(macAddress);
        if (peripheral != null) {

            if (!peripheral.isConnected()) {
                callbackContext.error("Peripheral " + macAddress + " is not connected.");
                return;
            }

            peripheral.queueRemoveNotifyCallback(callbackContext, serviceUUID, characteristicUUID);

        } else {

            callbackContext.error("Peripheral " + macAddress + " not found");

        }

    }

    private void findLowEnergyDevices(final CallbackContext callbackContext, 
    									UUID[] serviceUUIDs, 
    									int scanSeconds)  {

        if(!PermissionHelper.hasPermission(this, ACCESS_COARSE_LOCATION)) {
            // save info so we can call this method again after permissions are granted
            permissionCallback = callbackContext;
            this.serviceUUIDs = serviceUUIDs;
            this.scanSeconds = scanSeconds;
            PermissionHelper.requestPermission(this, REQUEST_ACCESS_COARSE_LOCATION, ACCESS_COARSE_LOCATION);
            return;
        }

        // ignore if currently scanning, alternately could return an error
        if (bluetoothAdapter.isDiscovering()) {
            return;
        }

        // clear non-connected cached peripherals
        for(Iterator<Map.Entry<String, Peripheral>> iterator = peripherals.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Peripheral> entry = iterator.next();
            Peripheral device = entry.getValue();
            boolean connecting = device.isConnecting();
            if (connecting){
                LOG.d(TAG, "Not removing connecting device: " + device.getDevice().getAddress());
            }
            if(!entry.getValue().isConnected() && !connecting) {
                iterator.remove();
            }
        }

        discoverCallback = callbackContext;

        if (serviceUUIDs.length > 0) {
            bluetoothAdapter.startLeScan(serviceUUIDs, this);
        } else {
            bluetoothAdapter.startLeScan(this);
        }

        if (scanSeconds > 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LOG.d(TAG, "Stopping Scan and sending notification to javascript");
                    BLECentralPlugin.this.bluetoothAdapter.stopLeScan(BLECentralPlugin.this);
                    // [AM]
                    // Notifty javascript that the scan interval is complete
                    // Create an empty device JSON object required by cordova plugin system
                    // On the javascript side the users will have to check that the device name and ID is not empty
                    try {
                    	JSONObject obj = new JSONObject("{ \"name\": \"\", \"id\": \"\", \"rssi\": 0, \"advertising\": \"\"}");                  
                    	//obj.put("plugin_message","SCAN_COMPLETE");
                    	PluginResult result = new PluginResult(PluginResult.Status.OK,obj);
                    	result.setKeepCallback(true);
                    	callbackContext.sendPluginResult(result);    
                    } catch (JSONException je) {
                    	LOG.d(TAG, "Failed to create json object: "+je.getMessage());
                    }
                }
            }, scanSeconds * 1000);
        }

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    private void listKnownDevices(CallbackContext callbackContext) {

        JSONArray json = new JSONArray();

        // do we care about consistent order? will peripherals.values() be in order?
        for (Map.Entry<String, Peripheral> entry : peripherals.entrySet()) {
            Peripheral peripheral = entry.getValue();
            json.put(peripheral.asJSONObject());
        }

        PluginResult result = new PluginResult(PluginResult.Status.OK, json);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        String address = device.getAddress();
        boolean alreadyReported = peripherals.containsKey(address);
       // LOG.d(TAG,"onLEScan,device address = " + address);

        if (!alreadyReported) {

            Peripheral peripheral = new Peripheral(device, rssi, scanRecord);
            peripherals.put(device.getAddress(), peripheral);

            if (discoverCallback != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, peripheral.asJSONObject());
                result.setKeepCallback(true);
                discoverCallback.sendPluginResult(result);
            }

        } else {
            Peripheral peripheral = peripherals.get(address);
            if(peripheral != null) {
	            peripheral.update(rssi, scanRecord);
	            if (reportDuplicates && discoverCallback != null) {
	                PluginResult result = new PluginResult(PluginResult.Status.OK, peripheral.asJSONObject());
	                result.setKeepCallback(true);
	                discoverCallback.sendPluginResult(result);
	            }
            }
            else {
            	//[AM]
            	 LOG.d(TAG,"#####----onLEScan,cannot find peripheral for address:" + address);
            	 for(Iterator<Map.Entry<String, Peripheral>> iterator = peripherals.entrySet().iterator(); iterator.hasNext(); ) {
                     Map.Entry<String, Peripheral> entry = iterator.next();
                     Peripheral cur = entry.getValue();
                     LOG.d(TAG, "Cur peripheral name=" + cur.getDevice().getName());
                     LOG.d(TAG, "Address=" + cur.getDevice().getAddress());
                }
            	 // TODO: report to javascript
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

            if (resultCode == Activity.RESULT_OK) {
                LOG.d(TAG, "User enabled Bluetooth");
                if (enableBluetoothCallback != null) {
                    enableBluetoothCallback.success();
                }
            } else {
                LOG.d(TAG, "User did *NOT* enable Bluetooth");
                if (enableBluetoothCallback != null) {
                    enableBluetoothCallback.error("User did not enable Bluetooth");
                }
            }

            enableBluetoothCallback = null;
        }
    }

    /// @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults)  throws JSONException  {
        for(int result:grantResults) {
            if(result == PackageManager.PERMISSION_DENIED)
            {
                LOG.d(TAG, "User *rejected* Coarse Location Access");
                this.permissionCallback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }

        switch(requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                LOG.d(TAG, "User granted Coarse Location Access");
                findLowEnergyDevices(permissionCallback, serviceUUIDs, scanSeconds);
                this.permissionCallback = null;
                this.serviceUUIDs = null;
                this.scanSeconds = -1;
                break;
        }
    }

    private UUID uuidFromString(String uuid) {
        return UUIDHelper.uuidFromString(uuid);
    }

    
    // Reset the BLE scanning options
   
    private void resetScanOptions() {
        this.reportDuplicates = false;
    }
*/
}
