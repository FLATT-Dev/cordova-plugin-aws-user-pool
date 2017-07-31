package com.amarder.cordova.aws.userpool;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.LOG;
import java.util.*;
import org.json.*;

public class CognitoConfig {
	
	private String _userPoolId 		= "";
	private String _appClientId 	= "";
	private String _appClientSecret = "";
	private String _identityPoolId	= "";
	private int	   _cognitoRegion = 0;
	
	//----------------------------------------------------
	public boolean init(CordovaArgs args) throws JSONException{
		
		
		JSONObject obj = new JSONObject(args.getString(0));
		LOG.d(AwsUserPoolPlugin.TAG,"CognitoConfig: got init object");
		
		_userPoolId 		= obj.getString("CognitoIdentityUserPoolId");
		_appClientId 		= obj.getString("CognitoIdentityUserPoolAppClientId");
		_appClientSecret 	= obj.getString("CognitoIdentityUserPoolAppClientSecret");
		_identityPoolId 	= obj.getString("arnIdentityPoolId");
		_cognitoRegion 		= obj.getInt("CognitoRegion");
		 
		 dumpValues();
		 
		return ((!_userPoolId.isEmpty()) &&
				(!_appClientId.isEmpty()) &&
				(!_identityPoolId.isEmpty()) &&
				(_cognitoRegion >=0 )				
				);		
	}
	//-------------------------------------
	public  void dumpValues() {		
		// debug
		LOG.d(AwsUserPoolPlugin.TAG,"_userPoolId:"		+_userPoolId);
		LOG.d(AwsUserPoolPlugin.TAG,"_appClientId:"		+_appClientId);
		LOG.d(AwsUserPoolPlugin.TAG,"_appClientSecret:"	+_appClientSecret);
		LOG.d(AwsUserPoolPlugin.TAG,"_identityPoolId:"	+_identityPoolId);
		LOG.d(AwsUserPoolPlugin.TAG,"_cognitoRegion:" 	+Integer.toString(_cognitoRegion));			
	}
	//----------------------------------------------------
	public String get_userPoolId() {
		return _userPoolId;
	}
	//----------------------------------------------------
	public String get_appClientId() {
		return _appClientId;
	}

	//----------------------------------------------------
	public String get_appClientSecret() {
		return _appClientSecret;
	}

	//----------------------------------------------------
	public String get_identityPoolId() {
		return _identityPoolId;
	}
	//----------------------------------------------------
	public int get_cognitoRegion() {
		return _cognitoRegion;
	}	
}