package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.ComplainAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.ComplainedOrder;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplainActivity extends AppCompatActivity {
    private ArrayList<OrderDetail> complainList;
    private static ArrayList<ComplainedOrder> complainedOrderList;
    private RecyclerView recyclerView;
    private static ComplainAdapter complainAdapter;
    private String transactionId;
    private TextView title;
    private static Button btnKirim;
    private long lastClickTime = 0;
    private ImageView close;

    private ArrayList<Integer> posisi = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Komplain");

        if (getIntent().getStringExtra("from").equals("upload")){
            complainList = getIntent().getParcelableArrayListExtra("complainList");
            transactionId = getIntent().getStringExtra("transactionId");
            posisi = getIntent().getIntegerArrayListExtra("posisi");
        }else{
            complainList = getIntent().getParcelableArrayListExtra("complainList");
            transactionId = getIntent().getStringExtra("transactionId");
        }
        complainedOrderList = new ArrayList<>();

        //masukkin list yg diterima ke list model baru
        for(OrderDetail od : complainList){
            complainedOrderList.add(new ComplainedOrder(od, "", ""));
        }

        title = findViewById(R.id.textView34);
        recyclerView = findViewById(R.id.rvComplain);
        btnKirim = findViewById(R.id.btnKirim);
        close = findViewById(R.id.close);

        title.setText("Komplain Pesanan \n"+transactionId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        complainAdapter = new ComplainAdapter(this, complainList, transactionId, posisi);
        recyclerView.setAdapter(complainAdapter);

        checkButtonEnabled(); //cek apakah semua form sudah terisi

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //jika button kirim ditekan
        btnKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tampilkan dialog konfirmasi
                final Dialog dialog = new Dialog(ComplainActivity.this);
                dialog.setContentView(R.layout.konfirmasi_dialog);

                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                TextView title = dialog.findViewById(R.id.title);
                TextView description = dialog.findViewById(R.id.description);

                title.setText("Kirim komplain");
                description.setText("Pastikan jenis komplain dan penjelasan anda sudah benar dan lengkap");

                btnBatal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //jika setuju, build query untuk komplain lalu panggil method untuk mengirimkan komplain tsb
                btnSetuju.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                            return;
                        }
                        else {
                            dialog.dismiss();
                            String insertComplain = "with new_insert as (INSERT INTO gcm_transaction_complain (detail_transaction_id, jenis_complain, notes_complain, create_by, update_by) VALUES ";

                            StringBuilder query = new StringBuilder();
                            query.append("with new_insert as (INSERT INTO gcm_transaction_complain (detail_transaction_id, jenis_complain, notes_complain, create_by, update_by) VALUES ");
                            for(int i = 0; i< complainedOrderList.size(); i++){
                                query.append("("+ complainedOrderList.get(i).getOrderDetail().getId()+", " +
                                        "'"+ complainedOrderList.get(i).getJenisKomplain()+"', " +
                                        "'"+ complainedOrderList.get(i).getCatatan()+"', " +
                                        +SharedPrefManager.getInstance(ComplainActivity.this).getUser().getUserId()+", " +
                                        SharedPrefManager.getInstance(ComplainActivity.this).getUser().getUserId()+")");
                                if(i< complainedOrderList.size()-1){
                                    query.append(",");
                                }
                                else{
                                    query.append(")");
                                }
                            }
                            query.append("update gcm_master_transaction set status = 'COMPLAINED', update_by = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+
                                    ", update_date=now(), date_complained=now() where id_transaction = '"+transactionId+"' returning id;");
                            komplainRequest(query.toString());
                            Log.d("ido", "onClick: "+query.toString());
                        }
                        lastClickTime = SystemClock.elapsedRealtime();
                    }
                });

                dialog.show();
            }
        });
    }

    /**Method untuk set jenis komplain*/
    public static void setJenisKomplain(int position, String value){
        complainedOrderList.get(position).setJenisKomplain(value);
        checkButtonEnabled();
    }

    /**Method untuk set catatan*/
    public static void setCatatan(int position, String value){
        complainedOrderList.get(position).setCatatan(value);
        checkButtonEnabled();
    }

    /**Method pengecekan apakah form sudah terisi semua*/
    public static void checkButtonEnabled(){
        boolean allFilled = true; //flag
        if(complainedOrderList.size()>0){
            for(int i = 0; i< complainedOrderList.size(); i++){
                if(complainedOrderList.get(i).getCatatan().trim().isEmpty() || complainedOrderList.get(i).getJenisKomplain().isEmpty()){
                    allFilled = false;
                }
            }
            btnKirim.setEnabled(allFilled);
        }
        else{
            btnKirim.setEnabled(false);
        }
    }

    /**Method untuk mengirimkan komplain*/
    private void komplainRequest(String query){
        try {
            final LoadingDialog loadingDialog = new LoadingDialog(ComplainActivity.this);
            loadingDialog.showDialog();
            Call<JsonObject> komplainCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));

            komplainCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            loadingDialog.hideDialog();
                            Toast.makeText(ComplainActivity.this, "Komplain berhasil dikirim", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ComplainActivity.this, MainActivity.class);
                            intent.putExtra("fragment", "orderFragment");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        else if(status.equals("error")){
                            loadingDialog.hideDialog();
                            Toast.makeText(ComplainActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    loadingDialog.hideDialog();
                    Toast.makeText(ComplainActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
