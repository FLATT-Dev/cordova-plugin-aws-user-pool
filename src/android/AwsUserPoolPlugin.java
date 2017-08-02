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
import com.amazonaws.auth.AWSCognitoIdentityProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;

//[AM] How to get logcat to display android crash stack trace
//logcat AndroidRuntime:E *:S

public class AwsUserPoolPlugin extends CordovaPlugin {
	// actions

	private static final String ACT_INIT = "init";
	private static final String ACT_SIGNIN = "signIn";
	private static final String ACT_OFFLINE_SIGNIN = "offlineSignIn";
	private static final String ACT_SIGNOUT = "signOut";
	private static final String ACT_SIGNUP = "signUp";
	private static final String ACT_CONFIRM_SIGNUP = "confirmSignUp";
	private static final String ACT_FORGOT_PWD = "forgotPassword";
	private static final String ACT_UPDATE_PWD = "updatePassword";
	private static final String ACT_GET_DETAILS = "getDetails";
	private static final String ACT_RESEND_CONF_CODE = "resendConfirmationCode";
	private static final String ACT_CREATE_COGNITO_DATASET = "createAWSCognitoDataset";
	private static final String ACT_GET_USR_DATA_COGNITO_SYNC = "getUserDataCognitoSync";
	private static final String ACT_SET_USR_DATA_COGNITO_SYNC = "setUserDataCognitoSync";
	private static final String ACT_CALL_LAMBDA_FUNC = "callAWSLambdaFunction";

	public static final String TAG = "AwsUserPoolPlugin";

	private CallbackContext _curContext = null;
	private static String _username;
	private static String _password;

	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		LOG.d(TAG, "action = " + action);

		_curContext = callbackContext; // save context for this invocation

		boolean validAction = true;
		if (action.equals(ACT_INIT)) {

			handleInit(args);

		} else if (action.equals(ACT_SIGNIN)) {

			handleSignIn(args);

		} else if (action.equals(ACT_OFFLINE_SIGNIN)) {

			handleOfflineSignIn();

		} else if (action.equals(ACT_SIGNOUT)) {

			handleSignOut();

		} else if (action.equals(ACT_SIGNUP)) {

			handleSignUp(args);

		} else if (action.equals(ACT_CONFIRM_SIGNUP)) {

			handleConfirmSignUp();

		} else if (action.equals(ACT_FORGOT_PWD)) {

			handleForgotPassword();

		} else if (action.equals(ACT_UPDATE_PWD)) {

			handleUpdatePassword();

		} else if (action.equals(ACT_GET_DETAILS)) {

			handleGetDetails();

		} else if (action.equals(ACT_RESEND_CONF_CODE)) {

			handleResendConfCode();

		} else if (action.equals(ACT_CREATE_COGNITO_DATASET)) {

			handleCreateCognitoDataset();

		} else if (action.equals(ACT_GET_USR_DATA_COGNITO_SYNC)) {

			handleGetUserDataCognitoSync();

		} else if (action.equals(ACT_SET_USR_DATA_COGNITO_SYNC)) {

			handleSetUserDataCognitoSync();

		} else if (action.equals(ACT_CALL_LAMBDA_FUNC)) {

			handleCallLambdaFunc();

		} else {

			validAction = false;

		}
		return validAction;
	}

	// ------------------------------------
	private void handleInit(CordovaArgs args) {

		try {

			AppHelper.init(cordova.getActivity().getApplicationContext(), args);

			LOG.d(TAG, "Initialized App Helper object");
			_curContext.success();
		} catch (Exception e) {
			setActionFailResponse("Exception in handleInit: ", e);
		}
	}

	// ------------------------------------
	private void handleSignIn(CordovaArgs args) {

		try {

			JSONObject obj = new JSONObject(args.getString(0));
			_username = obj.getString("username");
			_password = obj.getString("password");

			AppHelper.setUser(_username);
			AppHelper.getPool().getUser(_username)
					.getSessionInBackground(authenticationHandler);
			// tell Javascript that action is in progress
			setActionInProgressResponse();
		} catch (Exception e) {
			setActionFailResponse("Exception in handleSignIn: ", e);

		}
	}

	// ------------------------------------
	private void handleSignUp(CordovaArgs args) {

		try {

			JSONObject obj = new JSONObject(args.getString(0));
			JSONArray attrsArray = obj.getJSONArray("attributes");

			CognitoUserAttributes userAttributes = null;
			if (attrsArray != null) {
				userAttributes = new CognitoUserAttributes();
				// Iterate an array of name-value json objects
				for (int i = 0; i < attrsArray.length(); ++i) {

					JSONObject curObj = attrsArray.getJSONObject(i);
					userAttributes.addAttribute(curObj.getString("name"),
							curObj.getString("value"));
					LOG.d(TAG, "Adding attribute: " + curObj.getString("name")
							+ " = " + curObj.getString("value"));
				}
			}
			// username is called id for whatever reason in the iOS version of
			// the plugin
			_username = obj.getString("id");
			_password = obj.getString("password");

			AppHelper.getPool().signUpInBackground(_username, _password,
					userAttributes, null, signUpHandler);

			// tell Javascript that action is in progress
			setActionInProgressResponse();
		} catch (Exception e) {
			setActionFailResponse("Exception in handleSignup: ", e);
		}
	}

	// ------------------------------------
	private void handleOfflineSignIn() {
		setActionFailResponse("Offline sign-in not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleSignOut() {
		setActionFailResponse("Sign out not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleConfirmSignUp() {
	}

	// ------------------------------------
	private void handleForgotPassword() {
		setActionFailResponse("Forgot password  not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleUpdatePassword() {
		setActionFailResponse("Update password  not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleGetDetails() {
		setActionFailResponse("Get Details not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleResendConfCode() {
		setActionFailResponse("Resent confirmation  not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleCreateCognitoDataset() {
		setActionFailResponse("Create Cognito Dataset not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleGetUserDataCognitoSync() {
		setActionFailResponse("Get User data  not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleSetUserDataCognitoSync() {
		setActionFailResponse("Set user data  not yet implemenetd on Android");
	}

	// ------------------------------------
	private void handleCallLambdaFunc() {
		setActionFailResponse("Call Lambda function  not yet implemenetd on Android");
	}

	// ------------------------------------
	// Callback that indicates that the plugin has an action in progress
	private void setActionInProgressResponse() {
		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		result.setKeepCallback(true);
		_curContext.sendPluginResult(result);
	}

	// -----------------------------------------------------
	// Notify Javascript that an action resulted in an error
	private void setActionFailResponse(String message, Exception e) {
		String messg = message
				+ (e == null ? "" : AppHelper.formatException(e));
		LOG.d(TAG, messg);
		_curContext.error(messg);
	}

	// -------------------------------------------
	private void setActionFailResponse(String message) {
		setActionFailResponse(message, null);
	}

	// ---------------------------------------
	private void getUserAuthentication(AuthenticationContinuation continuation,
			String username) {

		LOG.d(TAG, "Getting user authentication with username: " + _username
				+ ". password: " + _password);
		AuthenticationDetails authenticationDetails = new AuthenticationDetails(
				_username, _password, null);
		continuation.setAuthenticationDetails(authenticationDetails);
		continuation.continueTask();
	}

	// ============== Sign up and sign in auth handlers ========================
	AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
		@Override
		public void onSuccess(CognitoUserSession cognitoUserSession,
				CognitoDevice device) {
			LOG.d(TAG, "Auth Success");
			AppHelper.setCurrSession(cognitoUserSession);
			AppHelper.newDevice(device);
			_curContext.success();
		}

		@Override
		public void getAuthenticationDetails(
				AuthenticationContinuation authenticationContinuation,
				String username) {
			Locale.setDefault(Locale.US);
			getUserAuthentication(authenticationContinuation, username);
		}

		@Override
		public void getMFACode(
				MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
			// mfaAuth(multiFactorAuthenticationContinuation);
			setActionFailResponse("Multi Factor Authentication not yet implemenetd on Android");
		}

		@Override
		public void onFailure(Exception e) {

			setActionFailResponse("Sign in failed: ", e);
		}

		@Override
		public void authenticationChallenge(ChallengeContinuation continuation) {

			setActionFailResponse("Authentication challenge not yet implemented on Android ");
			/**
			 * For Custom authentication challenge, implement your logic to
			 * present challenge to the user and pass the user's responses to
			 * the continuation.
			 */
			/*
			 * if
			 * ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName()))
			 * { // This is the first sign-in attempt for an admin created user
			 * newPasswordContinuation = (NewPasswordContinuation) continuation;
			 * AppHelper
			 * .setUserAttributeForDisplayFirstLogIn(newPasswordContinuation
			 * .getCurrentUserAttributes(),
			 * newPasswordContinuation.getRequiredAttributes());
			 * closeWaitDialog(); firstTimeSignIn(); }
			 */
		}
	};
	// ================================
	SignUpHandler signUpHandler = new SignUpHandler() {
		@Override
		public void onSuccess(CognitoUser user,
				boolean signUpConfirmationState,
				CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
			_curContext.success();
		}

		// -----------------------------------
		@Override
		public void onFailure(Exception exception) {
			setActionFailResponse("Sign up failed :", exception);
		}
	};

} // END CLASS
