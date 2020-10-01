package com.enseval.gcmuser.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Model.Chatroom;
import com.enseval.gcmuser.Model.Chats;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationWorker extends Worker {
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

//    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//    }

    @NonNull
    @Override
    public Result doWork() {
        notif();
        return null;
    }

    private void notif(){
        Log.d("", "onBind: hai");
        root.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot chatroom : dataSnapshot.getChildren()){
                    ArrayList<Chatroom> savedChatroomList = SharedPrefManager.getInstance(getApplicationContext()).getChatroom();
                    for(Chatroom chatroom1 : savedChatroomList){
                        if(chatroom.getKey().toString().equals(chatroom1.getRoomId())){
                            int childCount = (int) chatroom.child("message").getChildrenCount();
                            ArrayList<Chats> chatMessages = SharedPrefManager.getInstance(getApplicationContext()).getChatMessages(chatroom1.getRoomId());
                            if(childCount>chatMessages.size()){
                                int count=0;
                                for(DataSnapshot chat : chatroom.child("message").getChildren()){
                                    if(count>=childCount-(childCount-chatMessages.size())){
                                        Log.d("", "Notif pesan baru: "+chat.child("contain").getValue().toString());

                                        Notification.Builder builder = null;
                                        NotificationChannel mChannel = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            String CHANNEL_ID = "Chat Notification";
                                            mChannel = new NotificationChannel(CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_HIGH);
                                            builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                                    .setContentTitle("Pesan Baru")
                                                    .setContentText(chat.child("contain").getValue().toString())
                                                    .setSmallIcon(R.drawable.outline_textsms_black_18);
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                                            builder.setContentIntent(pendingIntent);
                                            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                            manager.createNotificationChannel(mChannel);
                                            Notification notification = builder.build();
                                            notification.flags |= Notification.FLAG_AUTO_CANCEL;
                                            manager.notify(0, notification);
                                        }
                                        else{
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                                            builder.setContentIntent(pendingIntent);
                                            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);;
                                            Notification notification = builder.build();
                                            notification.flags |= Notification.FLAG_AUTO_CANCEL;
                                            manager.notify(0, notification);
                                        }
                                    }
                                    count++;
                                }
                            }
                        }
                        else {
                            for(DataSnapshot chat : dataSnapshot.child("message").getChildren()){
                                Log.d("", "Notif chatroom baru: "+chat.child("contain").getValue().toString());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
