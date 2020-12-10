package com.enseval.gcmuser.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Fragment.NegoFragment;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private TextView badgeNotif;

    public TextView getBadgeNotif() {

        return badgeNotif;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("ido", "onMessageReceived: "+remoteMessage.getFrom());
        Log.d("ido", "onMessageReceived: "+remoteMessage.getData().size());

        if (remoteMessage.getData().size()>0){

            try {
                Map<String, String> map = remoteMessage.getData();
                handleDataMessage(map);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        notifNotif();
    }

    private void notifNotif(){
        String query = "select count(a.id) as jumlah from (select a.id, a.seller_id " +
                "from gcm_notification_nego a " +
                "inner join gcm_list_barang b on a.barang_id = b.id " +
                "inner join gcm_master_barang c on b.barang_id = c.id " +
                "inner join gcm_master_company d on a.buyer_id = d.id " +
                "where a.read_flag = 'N' and a.source = 'seller' and now() >= a.date and a.buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+") a " +
                "inner join gcm_master_company b on a.seller_id = b.id";
        try {
            Call<JsonObject> callCartNotif = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCartNotif.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int jumlah = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();
                            MainActivity.notifNotif(jumlah);
                            Log.d("ido", "jumlah notif: "+jumlah);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleDataMessage(Map<String, String> json){
        try {
            String key = json.get("key");
            Log.d("ido", "handleDataMessage: "+key);

            Notification.Builder builder = null;
            NotificationChannel mChannel = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String CHANNEL_ID = "Nego Notification";
                mChannel = new NotificationChannel(CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_HIGH);
                if (key!=null) {
                    if (key.equals("nego")) {
                        builder = new Notification.Builder(MyFirebaseMessagingService.this, CHANNEL_ID)
                                .setContentTitle("GLOB")
                                .setContentText("Balasan negosiasi dari penjual")
                                .setSmallIcon(R.mipmap.ic_launcher);
                    } else if (key.equals("nego_approved")) {
                        builder = new Notification.Builder(MyFirebaseMessagingService.this, CHANNEL_ID)
                                .setContentTitle("GLOB")
                                .setContentText("1 Nego berhasil disepakati")
                                .setSmallIcon(R.mipmap.ic_launcher);
                    }else{
                        builder = new Notification.Builder(MyFirebaseMessagingService.this, CHANNEL_ID)
                                .setContentTitle("GLOB")
                                .setContentText("Balasan negosiasi dari penjual")
                                .setSmallIcon(R.mipmap.ic_launcher);
                    }
                }else{
                    builder = new Notification.Builder(MyFirebaseMessagingService.this, CHANNEL_ID)
                                .setContentTitle("GLOB")
                                .setContentText("Balasan negosiasi dari penjual")
                                .setSmallIcon(R.mipmap.ic_launcher);
                }
                Intent intent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(mChannel);
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                manager.notify(0, notification);
            }else{
                Intent intent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);;
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                manager.notify(0, notification);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
