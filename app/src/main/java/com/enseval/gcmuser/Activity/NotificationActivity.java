package com.enseval.gcmuser.Activity;

import android.app.Notification;
import android.content.Intent;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.AlamatAdapter;
import com.enseval.gcmuser.Adapter.NotifikasiAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Notifikasi;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import static android.view.View.GONE;

public class NotificationActivity extends AppCompatActivity {

    private int id;
    private String barang_nama, seller_nama, read_flag, date, source;
    private ArrayList<Notifikasi> listNotif;
    private RecyclerView rvNotifikasi;
    private ConstraintLayout noItem, failed;
    private Button refresh;
    private ImageView back;
    private long lastClickTime = 0;
    private NotifikasiAdapter NotifikasiAdalper;
    private String TAG = "ido";
    private LoadingDialog loadingDialog;

//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: jalan");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifikasi = findViewById(R.id.rvNotif);
        noItem = findViewById(R.id.noItem);
        failed = findViewById(R.id.failed);
        refresh = findViewById(R.id.refresh);
        back = findViewById(R.id.btnBack);
        loadingDialog = new LoadingDialog(this);

        //refresh jika gagal memuat halaman
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(getIntent());
                    finish();
                    startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        noItem.setVisibility(GONE);
        failed.setVisibility(GONE);

        notifNotif();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void getNotifikasi(){
        String query = "select a.barang_id, a.nama_barang, a.buyer_id, a.buyer_nama, a.seller_id, to_char(a.date, 'dd/MM/yyyy HH24:MI') as date, " +
                "a.status, b.nama_perusahaan as seller_nama,case when a.timestamp_kirim is null then '0000000000000' else a.timestamp_kirim end from (select a.barang_id, c.nama as nama_barang, a.buyer_id, " +
                "d.nama_perusahaan as buyer_nama, a.seller_id, a.date, a.status, case when a.timestamp_kirim is null then '0000000000000' else a.timestamp_kirim end " +
                "from gcm_notification_nego a " +
                "inner join gcm_list_barang b on a.barang_id = b.id " +
                "inner join gcm_master_barang c on b.barang_id = c.id " +
                "inner join gcm_master_company d on a.buyer_id = d.id " +
                "where a.read_flag = 'N' and a.source = 'seller' and now() >= a.date and a.buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" ) a " +
                "inner join gcm_master_company b on a.seller_id = b.id order by a.date desc";
        Log.d(TAG, "getNotifikasi: "+query);
        try {
            loadingDialog.showDialog();
            Call<JsonObject> callGetNotif = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetNotif.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            listNotif = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listNotif.add(new Notifikasi(
                                        jsonObject.get("nama_barang").getAsString(),
                                        jsonObject.get("seller_nama").getAsString(),
                                        jsonObject.get("date").getAsString(),
                                        jsonObject.get("status").getAsString(),
                                        jsonObject.get("buyer_nama").getAsString(),
                                        jsonObject.get("barang_id").getAsInt(),
                                        jsonObject.get("timestamp_kirim").getAsString()
                                ));
                            }
                            Log.d(TAG, "isi list notif: "+listNotif.size());
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NotificationActivity.this);
                            rvNotifikasi.setLayoutManager(layoutManager);
                            rvNotifikasi.setItemAnimator(new DefaultItemAnimator());
                            NotifikasiAdalper = new NotifikasiAdapter(getApplicationContext(), listNotif);
                            rvNotifikasi.setAdapter(NotifikasiAdalper);
                            updateFlag();
                        }else{
                            loadingDialog.hideDialog();
                        }
                    }else{
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    rvNotifikasi.setVisibility(GONE);
                    noItem.setVisibility(GONE);
                    failed.setVisibility(View.VISIBLE);
                    loadingDialog.hideDialog();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateFlag(){
        String query = "update gcm_notification_nego set read_flag = 'Y' where buyer_id = "+
                SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and date <= now()";
        Log.d(TAG, "updateFlag: "+query);
        try {
            Call<JsonObject> callUpdateFlag = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callUpdateFlag.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "update flag: sukses");
                        loadingDialog.hideDialog();
                    }else{
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
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
                            if (jumlah == 0){
                                rvNotifikasi.setVisibility(GONE);
                                noItem.setVisibility(View.VISIBLE);
                            }else{
                                noItem.setVisibility(GONE);
                                rvNotifikasi.setVisibility(View.VISIBLE);
                            }
                            getNotifikasi();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    rvNotifikasi.setVisibility(GONE);
                    noItem.setVisibility(GONE);
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
