package com.sinchcalling;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import android.content.Context;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import android.util.Log;

/**
 * Created by parita.detroja on 5/12/17.
 * Contains all required method for audio call handling.
 */

public class SinchAudioCall {

    private String TAG = "SinchAudioCall";

    private CallClient callClient;

    private Call call = null;

    private CallbackContext audioCallCallbackContext = null;

    private PluginResult result = null;

    private Uri mUri;

    private  Ringtone mRingtone;

    private int callType = -1;

    public SinchAudioCall(Context context)
    {
        callClient = ConfigureSinch.retrieveSinchClient().getCallClient();
        mUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(context, mUri);
    }

    public void initializeCallbackContex(CallbackContext callbackContext)
    {
        audioCallCallbackContext = callbackContext;
    }

    public void createAudioCall(String remoteUsedId)
    {
        Log.d(TAG, "createAudioCall");
        if (call == null) 
        {
            call = callClient.callUser(remoteUsedId);
            //this.audioCallCallbackContext = callbackContext;
            call.addCallListener(new SinchCallListener());
            callType = Constant.OUTGOING_CALL;
        }
    }

    public void hangUpAudioCall()
    {
        Log.e(TAG, "hangUpAudioCall");
        if(mRingtone != null && mRingtone.isPlaying())
        {
            Log.e(TAG,"Stop ring tone");
            mRingtone.stop();
        }
        if(call != null)
        {
            call.hangup();
        }
    }

    public void addCallClientListener()
    {
        callClient.addCallClientListener(new SinchCallClientListener());
    }

    private class SinchCallListener implements CallListener
    {
        @Override
        public void onCallEnded(Call endedCall)
        {
            call = null;
            Log.e(TAG, "onCallEnded");
            sendPluginResult("onCallEnded");
            if(mRingtone != null && mRingtone.isPlaying())
            {
                Log.e(TAG,"Stop ring tone");
                mRingtone.stop();
            }
        }

        @Override
        public void onCallEstablished(Call establishedCall)
        {
            Log.e(TAG, "onCallEstablished");
            sendPluginResult("onCallEstablished");
            if(mRingtone != null && mRingtone.isPlaying())
            {
                Log.e(TAG,"Stop ring tone");
                mRingtone.stop();
            }
        }

        @Override
        public void onCallProgressing(Call progressingCall)
        {
            Log.e(TAG, "onCallProgressing");
            sendPluginResult("onCallProgressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) 
        {
    
        }
    }

    private class SinchCallClientListener implements CallClientListener 
    {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) 
        {
            call = incomingCall;
            Log.e(TAG, "onIncomingCall");
            sendPluginResult("onIncomingCall");
            //call.answer();
            //call.addCallListener(new SinchCallListener());
            callType = Constant.INCOMING_CALL;
            if(mRingtone != null && callType == Constant.INCOMING_CALL)
            {
                Log.e(TAG,"Start ring tone");
                mRingtone.play();
            }
        }
    }

    public void answerIncomingCall(boolean answer)
    {
        Log.e(TAG, "answerIncomingCall");
        if(answer)
        {
            call.answer();
            call.addCallListener(new SinchCallListener());
        }else
        {
            hangUpAudioCall();
        }
        
    }

    private void sendPluginResult(String callStatus)
    {
        try
        {
            JSONObject status = new JSONObject();
            status.put("call_status", callStatus);
            Log.e(TAG, "Plugin result send for " + callStatus);
            result = new PluginResult(PluginResult.Status.OK, status);
            result.setKeepCallback(true);
            //audioCallCallbackContext.success("Success");
            Log.e(TAG, audioCallCallbackContext.getCallbackId());
            audioCallCallbackContext.sendPluginResult(result);
        } catch(Exception e)
        {
            e.printStackTrace();
            audioCallCallbackContext.error("error");
        }
    }
}