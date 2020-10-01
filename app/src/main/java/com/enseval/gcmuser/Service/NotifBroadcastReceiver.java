package com.enseval.gcmuser.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotifBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, ChatNotifService.class));
    }
}
