package com.enseval.gcmuser;

import android.app.Application;
import android.content.ServiceConnection;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.enseval.gcmuser.Service.NotificationWorker;

public class App extends Application {

    boolean mBound = false;
    private ServiceConnection sCartConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
//        sCartConnection = new ServiceConnection() {
//
//            @Override
//            public void onServiceConnected(ComponentName className,
//                                           IBinder service) {
//                // We've bound to LocalService, cast the IBinder and get LocalService instance
//                ChatNotifService.LocalBinder binder = (ChatNotifService.LocalBinder) service;
////             mService = binder.getService();
//                mBound = true;
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName arg0) {
//                mBound = false;
//            }
//        };
//
//        Intent intent = new Intent(new Intent(this, ChatNotifService.class));
//        startService(intent);
//        bindService(intent, sCartConnection, Context.BIND_AUTO_CREATE);
//    }
        WorkManager.getInstance().enqueue(workRequest);
    }
}
