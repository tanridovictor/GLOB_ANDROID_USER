package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyachi.stepview.HorizontalStepView;
import com.baoyachi.stepview.bean.StepBean;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.Utilities.Helper;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailHistoryNego extends AppCompatActivity {
    private TextView tvNamaBarang, tvStatusNego;
    private String valueCompanyId,valueBarangID,valueImg,valueNamaBarang,valueHargaTerakhir, valueNego;
    private ShimmerFrameLayout shimmerFrameLayout, shimmerFrameLayout2, shimmerFrameLayout3, shimmerFrameNego;
    private ImageView appbarimg, backBtn;

    SimpleDateFormat time = new SimpleDateFormat("d MMM yyyy");

    private LoadingDialog loadingDialog;

    private List<StepBean> listNego = new ArrayList<>();
    private HorizontalStepView lineNego;
    private long lastClickTime=0;

    private int valueIdNegoClasify, valueCountNegoClasify;
    private  LinearLayout layoutNego1,layoutNego2,layoutNego3, layouthargafinal1, layouthargafinal2, layouthargafinal3;
    TextView tvDateNego1, tvDateNego2, tvDateNego3,
                        tvUpdateByNego1, tvUpdateByNego2, tvUpdateByNego3,
                            tvHargaKonsumen1, tvHargaKonsumen2, tvHargaKonsumen3,
                                tvHargaSales1, tvHargaSales2, tvHargaSales3,
                                    tvHargaFinal1, tvHargaFinal2, tvHargaFinal3;
    int passingvalue_count;
    private CardView nego;
    private String value_notes = " - ";
    private String value_nego_id , value_nego_idhistory;
    private Boolean isOpen;
    Date TimeRespon, dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_nego);

        loadingDialog = new LoadingDialog(this);

        Calendar calendar = Calendar.getInstance();
        Date DateTime = calendar.getTime();
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeS = datetime.format(DateTime);

        try {
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateTime = time.parse(getIntent().getStringExtra("timeRespon"));
            TimeRespon = datetime.parse(DateTimeS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("ido", "onCreate: Time "+TimeRespon+" "+dateTime);
        initialTools();
        mappingValue();
        countingNegotiate();

        passingvalue_count = Integer.parseInt(getIntent().getStringExtra("count"));
        tvStatusNego.setText("Nego ke " + getIntent().getStringExtra("count"));

        callDataHistoryNego(Integer.parseInt(getIntent().getStringExtra("id_nego_history")));

        Glide.with(getApplicationContext())
                .load(valueImg)
                .fallback(R.id.shimmer_view_container)
                .error(R.id.shimmer_view_container)
                .into(appbarimg);

        tvStatusNego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(DetailHistoryNego.this);
                dialog.setContentView(R.layout.dialog_notes);

                isOpen = false;

                TextView title = dialog.findViewById(R.id.tvTitle);
                final TextView nama = dialog.findViewById(R.id.tvNama);
                final TextView notes = dialog.findViewById(R.id.value);
                final EditText input = dialog.findViewById(R.id.input);
                final ImageView btnEdit = dialog.findViewById(R.id.edit);
                final Button btnSave = dialog.findViewById(R.id.btnSave);
                nama.setText(valueNamaBarang);
                notes.setText(value_notes);
                input.setText(value_notes);
                notes.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.GONE);
                input.setVisibility(View.GONE);
                title.setText("Notes untuk nego nomor " + value_nego_id);


                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String temp = input.getText().toString();
                        updateNotes(value_nego_idhistory, temp);
//                        dialog.dismiss();
                    }

                    private void updateNotes(String passing_id, String passing_note) {
                        dialog.dismiss();
                        loadingDialog.showDialog();
                        String query = "update gcm_history_nego set notes = '"+ passing_note +"' where id =" + passing_id + " returning id";
                        Log.d("updateNotes" , query);
                        try {
                            Call<JsonObject> negoCall = RetrofitClient
                                    .getInstance()
                                    .getApi()
                                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                            negoCall.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    if(response.isSuccessful()){
                                        String status = response.body().getAsJsonObject().get("status").getAsString();
                                        if(status.equals("success")){
                                            loadingDialog.hideDialog();
                                            JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
                                            Toast.makeText(DetailHistoryNego.this, "Updated Notes number "+ jsonObject.get("id").toString(), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }else {
                                            loadingDialog.hideDialog();
                                            Log.wtf("", "onResponse: NOT success ");
                                        }
                                    }else {
                                        loadingDialog.hideDialog();
                                        Log.d("cekit","onResponse: NOT isSuccesful");
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    loadingDialog.hideDialog();
                                    Log.wtf("onFailure updateNotes function" , t);
                                    Toast.makeText(DetailHistoryNego.this, "onFailure: func countingNegotiate " + t, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            loadingDialog.hideDialog();
                            Log.wtf("Exception on updateNotes function", e);
                            e.printStackTrace();
                        }
                    }
                });

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isOpen){
                            notes.setVisibility(View.GONE);
                            btnSave.setVisibility(View.VISIBLE);
                            input.setVisibility(View.VISIBLE);
                        }else {
                            notes.setVisibility(View.VISIBLE);
                            input.setVisibility(View.GONE);
                        }
                    }
                });

                dialog.show();

            }
        });
    }

    private void countingNegotiate() {
        String query = "select id, nego_count, history_nego_id from gcm_master_cart where id =" + getIntent().getStringExtra("id_nego");
        Log.d("cekit query" , query);
        try {
                Call<JsonObject> negoCall = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                negoCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if(response.isSuccessful()){
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
                                value_nego_id       = String.valueOf(jsonObject.get("id").getAsInt());
                                int countNego       = jsonObject.get("nego_count").getAsInt();
                                int idHistoryNego   = jsonObject.get("history_nego_id").getAsInt();

                                shimmerFrameLayout.stopShimmerAnimation();
                                shimmerFrameLayout.setVisibility(View.GONE);
                                shimmerFrameLayout2.stopShimmerAnimation();
                                shimmerFrameLayout2.setVisibility(View.GONE);
                                shimmerFrameNego.stopShimmerAnimation();
                                shimmerFrameNego.setVisibility(View.GONE);

                                valueNego = String.valueOf(countNego);

                                if (idHistoryNego != 0) {
                                    value_nego_idhistory = String.valueOf(idHistoryNego);
                                    clasifyByNegoCount(countNego);
//                                    callDataHistoryNego(countNego);
                                }else {
                                    Toast.makeText(DetailHistoryNego.this, "Your Nego History Unrecorded", Toast.LENGTH_LONG).show();
                                }
                            }else {
                                Log.d("", "onResponse: NOT success ");
                            }
                        }else {
                            Log.d("cekit","onResponse: NOT isSuccesful");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(DetailHistoryNego.this, "onFailure: func countingNegotiate " + t, Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialTools() {
        tvNamaBarang        =   findViewById(R.id.tvNamaBarang);
        nego                =   findViewById(R.id.cardNego);
//        tvHarga             =   findViewById(R.id.tvHarga);
        tvStatusNego        =   findViewById(R.id.negoStatus);
        appbarimg           =   findViewById(R.id.app_bar_image);

        shimmerFrameLayout  =   findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout2 =   findViewById(R.id.shimmer_view_container2);
//        shimmerFrameLayout3 =   findViewById(R.id.shimmer_view_container3);
        shimmerFrameNego    =   findViewById(R.id.shimmer_view_container_nego);

        lineNego            =   findViewById(R.id.timeline);

        backBtn             =   findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvDateNego1         =   findViewById(R.id.tvDateNego1);
        tvDateNego2         =   findViewById(R.id.tvDateNego2);
        tvDateNego3         =   findViewById(R.id.tvDateNego3);
        tvUpdateByNego1     =   findViewById(R.id.tvUpdateByNego1);
        tvUpdateByNego2     =   findViewById(R.id.tvUpdateByNego2);
        tvUpdateByNego3     =   findViewById(R.id.tvUpdateByNego3);
        tvHargaKonsumen1    =   findViewById(R.id.tvHargaKonsumen1);
        tvHargaKonsumen2    =   findViewById(R.id.tvHargaKonsumen2);
        tvHargaKonsumen3    =   findViewById(R.id.tvHargaKonsumen3);
        tvHargaSales1       =   findViewById(R.id.tvHargaSales1);
        tvHargaSales2       =   findViewById(R.id.tvHargaSales2);
        tvHargaSales3       =   findViewById(R.id.tvHargaSales3);
        tvHargaFinal1       =   findViewById(R.id.tvHargaFinal1);
        tvHargaFinal2       =   findViewById(R.id.tvHargaFinal2);
        tvHargaFinal3       =   findViewById(R.id.tvHargaFinal3);

        layoutNego1         =   findViewById(R.id.layoutNego1);
        layoutNego2         =   findViewById(R.id.layoutNego2);
        layoutNego3         =   findViewById(R.id.layoutNego3);
        layoutNego1.setVisibility(View.GONE);
        layoutNego2.setVisibility(View.GONE);
        layoutNego3.setVisibility(View.GONE);
        layouthargafinal1   =   findViewById(R.id.layouthargafinal1);
        layouthargafinal2   =   findViewById(R.id.layouthargafinal2);
        layouthargafinal3   =   findViewById(R.id.layouthargafinal3);
        layouthargafinal1.setVisibility(View.GONE);
        layouthargafinal2.setVisibility(View.GONE);
        layouthargafinal3.setVisibility(View.GONE);
    }

    private void mappingValue() {
        valueBarangID       =   getIntent().getStringExtra("id");
        valueCompanyId      =   getIntent().getStringExtra("companyId");
        valueImg            =   getIntent().getStringExtra("img");
        valueNamaBarang     =   getIntent().getStringExtra("nama");
        valueHargaTerakhir  =   getIntent().getStringExtra("harga_terakhir");
        valueNego           =   getIntent().getStringExtra("id_nego");


        tvNamaBarang.setText(valueNamaBarang);
//        tvStatusNego.setText("Status = " + passingvalue_count);

        Glide.with(getApplicationContext())
                .load(valueImg)
                .fallback(R.id.shimmer_view_container)
                .error(R.id.shimmer_view_container)
                .into(appbarimg);
    }

    private void callDataHistoryNego(int idHIstory) {

        String query = "";

        if (Integer.parseInt(getIntent().getStringExtra("count")) == 1){
//            Toast.makeText(this, "masuk 1", Toast.LENGTH_SHORT).show();
            query = "select * from gcm_history_nego where id = " + idHIstory;
            try {
                Call<JsonObject> callData = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                callData.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if(response.isSuccessful()){
                            Log.d("onResponse === ",response.body().toString());
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
                                Log.d("cekit_json", String.valueOf(jsonObject));
                                String notes = "-";
                                    if (jsonObject.get("notes").getAsString() != null) {
                                        notes  =  jsonObject.get("notes").getAsString();
                                        value_notes = notes;
                                    }else {value_notes = notes;}
                                int id = jsonObject.get("id").getAsInt();
                                int harganego  = jsonObject.get("harga_nego").getAsInt();
                                int hargasales = jsonObject.get("harga_sales").getAsInt();
                                int createdby = jsonObject.get("created_by").getAsInt();
                                int updatedby = jsonObject.get("updated_by").getAsInt();

                                int deal = jsonObject.get("harga_final").getAsInt();
                                String tanggal = jsonObject.get("created_date").getAsString();

//                                tvDateNego1.setText(String.valueOf(Helper.convertStringToDateSimple(jsonObject.get("created_date").getAsString())));
                                tvUpdateByNego1.setText(String.valueOf(createdby));
                                tvHargaKonsumen1.setText(String.valueOf(Helper.getCurrencyFormat().format(harganego)));
                                if (TimeRespon.after(dateTime)) {
                                    tvHargaSales1.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales)));
                                } else {
                                    tvHargaSales1.setText("Menunggu respon");
                                }

                                Log.d("passingvalue",id+" === "+harganego+" === "+hargasales+" === "+notes+createdby+" === "+updatedby +" === "+tanggal);

                                if (deal != 0 && TimeRespon.after(dateTime)) {
                                    tvStatusNego.setText("DEAL");
                                    layouthargafinal1.setVisibility(View.VISIBLE);
                                    tvHargaSales1.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales)));
                                    tvHargaFinal1.setText(String.valueOf(Helper.getCurrencyFormat().format(deal)));
                                }

                                printCompanyName("nego_pertama",createdby);

                                //timestamp update date
                                long timestamp_update_1 = jsonObject.get("timestamp_updated_date").getAsLong();
                                String timestamp_update1 = time.format(timestamp_update_1);

                                tvDateNego1.setText(String.valueOf(timestamp_update1));
                            }else {
                                Log.d("", "onResponse: NOT success ");
                            }
                        }else {
                            Log.d("cekit","onResponse: NOT isSuccesful");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(DetailHistoryNego.this, "onFailure: func countingNegotiate " + t, Toast.LENGTH_SHORT).show();
                        Log.wtf("onFailure: func countingNegotiate", t);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "error function for callDataHistoryNego", Toast.LENGTH_SHORT).show();
                Log.wtf("error function for callDataHistoryNego", e);
            }
        }else if (Integer.parseInt(getIntent().getStringExtra("count")) == 2){
//            Toast.makeText(this, "masuk 2", Toast.LENGTH_SHORT).show();
            query = "select * from gcm_history_nego where id = " + idHIstory;
            try {
                Call<JsonObject> callData = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                callData.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if(response.isSuccessful()){
                            Log.d("onResponse === ",response.body().toString());
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
                                Log.d("cekit_json", String.valueOf(jsonObject));
                                int id = jsonObject.get("id").getAsInt();

                                String notes = "-";
                                if (jsonObject.get("notes").getAsString() != null) {
                                    notes  =  jsonObject.get("notes").getAsString();
                                    value_notes = notes;
                                }else {value_notes = notes;}

                                int harganego  = jsonObject.get("harga_nego").getAsInt();
                                int hargasales = jsonObject.get("harga_sales").getAsInt();
                                value_notes  =  jsonObject.get("notes").getAsString();
                                int createdby = jsonObject.get("created_by").getAsInt();
                                int updatedby = jsonObject.get("updated_by").getAsInt();

                                int deal = jsonObject.get("harga_final").getAsInt();
                                String tanggal = jsonObject.get("created_date").getAsString();
                                String tanggal2 = jsonObject.get("updated_date_2").getAsString();
                                int updatedby2 = jsonObject.get("updated_by_2").getAsInt();
                                int harganego2 = jsonObject.get("harga_nego_2").getAsInt();
                                int hargasales2;
                                if (jsonObject.get("harga_sales_2").toString().equals("null")) { hargasales2 = 0; }
                                    else { hargasales2 = jsonObject.get("harga_sales_2").getAsInt(); }

//                                tvDateNego1.setText(String.valueOf(Helper.convertStringToDateSimple(tanggal)));
                                long timestamp_update_1 = jsonObject.get("timestamp_updated_date").getAsLong();
                                String timestamp_update1 = time.format(timestamp_update_1);
                                tvDateNego1.setText(timestamp_update1);
//                                tvDateNego2.setText(String.valueOf(Helper.convertStringToDateSimple(tanggal2)));
                                long timestamp_update_2 = jsonObject.get("timestamp_updated_date_2").getAsLong();
                                String timestamp_update2 = time.format(timestamp_update_2);
                                tvDateNego2.setText(timestamp_update2);
                                tvUpdateByNego1.setText(String.valueOf(createdby));
                                tvHargaKonsumen1.setText(String.valueOf(Helper.getCurrencyFormat().format(harganego)));
                                tvHargaSales1.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales)));

                                tvUpdateByNego2.setText(String.valueOf(updatedby2));
                                tvHargaKonsumen2.setText(String.valueOf(Helper.getCurrencyFormat().format(harganego2)));
                                if (TimeRespon.before(dateTime)) { tvHargaSales2.setText("Menunggu respon"); }
                                    else {tvHargaSales2.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales2)));}

                                if (deal != 0 && TimeRespon.after(dateTime)) {
                                    tvStatusNego.setText("DEAL");
                                    nego.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
                                    layouthargafinal2.setVisibility(View.VISIBLE);
                                    tvHargaSales2.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales2)));
                                    tvHargaFinal2.setText(String.valueOf(Helper.getCurrencyFormat().format(deal)));
                                }

                                printCompanyName("nego_pertama",createdby);
                                printCompanyName("nego_kedua",updatedby2);
                            }else {
                                Log.d("", "onResponse: NOT success ");
                            }
                        }else {
                            Log.d("cekit","onResponse: NOT isSuccesful");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(DetailHistoryNego.this, "onFailure: func countingNegotiate " + t, Toast.LENGTH_SHORT).show();
                        Log.wtf("onFailure: func countingNegotiate", t);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "error function for callDataHistoryNego", Toast.LENGTH_SHORT).show();
                Log.wtf("error function for callDataHistoryNego", e);
            }
        }else if (Integer.parseInt(getIntent().getStringExtra("count")) == 3){
//            Toast.makeText(this, "masuk 3", Toast.LENGTH_SHORT).show();
            query = "select * from gcm_history_nego where id = " + idHIstory;
            try {
                Call<JsonObject> callData = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                callData.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if(response.isSuccessful()){
                            Log.d("onResponse === ",response.body().toString());
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
//                                String  notes  =  jsonObject.get("notes").getAsString();

                                String notes = "-";
                                if (jsonObject.get("notes").getAsString() != null) {
                                    notes  =  jsonObject.get("notes").getAsString();
                                    value_notes = notes;
                                }else {value_notes = notes;}

                                int deal = jsonObject.get("harga_final").getAsInt();
                                int id = jsonObject.get("id").getAsInt();

                                int harganego  = jsonObject.get("harga_nego").getAsInt();
                                int hargasales = jsonObject.get("harga_sales").getAsInt();
                                int createdby = jsonObject.get("created_by").getAsInt();
                                int updatedby = jsonObject.get("updated_by").getAsInt();

                                int updatedby2 = jsonObject.get("updated_by_2").getAsInt();
                                int harganego2 = jsonObject.get("harga_nego_2").getAsInt();
                                int hargasales2 = jsonObject.get("harga_sales_2").getAsInt();

                                int updatedby3 = jsonObject.get("updated_by_3").getAsInt();
                                int harganego3 = jsonObject.get("harga_nego_3").getAsInt();
                                int hargasales3;
                                if (jsonObject.get("harga_sales_3").toString().equals("null")) { hargasales3 = 0; }
                                else { hargasales3 = jsonObject.get("harga_sales_3").getAsInt(); }

                                String tanggal = jsonObject.get("created_date").getAsString();
                                String tanggal2 = jsonObject.get("updated_date_2").getAsString();
                                String tanggal3 = jsonObject.get("updated_date_3").getAsString();

//                                tvDateNego1.setText(String.valueOf(Helper.convertStringToDateSimple(tanggal)));
                                long timestamp_update_1 = jsonObject.get("timestamp_updated_date").getAsLong();
                                String timestamp_update1 = time.format(timestamp_update_1);
                                tvDateNego1.setText(timestamp_update1);
//                                tvDateNego2.setText(String.valueOf(Helper.convertStringToDateSimple(tanggal2)));
                                long timestamp_update_2 = jsonObject.get("timestamp_updated_date_2").getAsLong();
                                String timestamp_update2 = time.format(timestamp_update_2);
                                tvDateNego2.setText(timestamp_update2);
//                                tvDateNego3.setText(String.valueOf(Helper.convertStringToDateSimple(tanggal3)));
                                long timestamp_update_3 = jsonObject.get("timestamp_updated_date_3").getAsLong();
                                String timestamp_update3 = time.format(timestamp_update_3);
                                tvDateNego3.setText(timestamp_update3);
                                tvUpdateByNego1.setText(String.valueOf(createdby));
                                tvHargaKonsumen1.setText(String.valueOf(Helper.getCurrencyFormat().format(harganego)));
                                tvHargaSales1.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales)));
                                tvUpdateByNego2.setText(String.valueOf(updatedby2));
                                tvHargaKonsumen2.setText(String.valueOf(Helper.getCurrencyFormat().format(harganego2)));
                                tvHargaSales2.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales2)));
                                tvUpdateByNego3.setText(String.valueOf(updatedby3));
                                tvHargaKonsumen3.setText(String.valueOf(Helper.getCurrencyFormat().format(harganego3)));

                                if (TimeRespon.before(dateTime)) { tvHargaSales3.setText("Menunggu respon"); }
                                else {tvHargaSales3.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales3)));}

                                if (deal != 0 && TimeRespon.after(dateTime)) {
                                    tvStatusNego.setText("DEAL");
                                    layouthargafinal3.setVisibility(View.VISIBLE);
                                    tvHargaSales3.setText(String.valueOf(Helper.getCurrencyFormat().format(hargasales3)));
                                    tvHargaFinal3.setText(String.valueOf(Helper.getCurrencyFormat().format(deal)));
                                }
                                printCompanyName("nego_pertama",createdby);
                                printCompanyName("nego_kedua",updatedby2);
                                printCompanyName("nego_ketiga",updatedby3);
                            }else {
                                Log.d("", "onResponse: NOT success ");
                            }
                        }else {
                            Log.d("cekit","onResponse: NOT isSuccesful");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(DetailHistoryNego.this, "onFailure: func countingNegotiate " + t, Toast.LENGTH_SHORT).show();
                        Log.wtf("onFailure: func countingNegotiate", t);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "error function for callDataHistoryNego", Toast.LENGTH_SHORT).show();
                Log.wtf("error function for callDataHistoryNego", e);
            }
        }

        Log.d("cekit query" , query);
        Log.d("cekit idcart",String.valueOf(idHIstory));
    }

    private void printCompanyName(final String from , int value_passing) {
        String query = "select a.id , a.nama,b.nama_perusahaan,a.username\n" +
                "from gcm_master_user a inner join gcm_master_company b on a.company_id = b.id\n" +
                "where a.id =" + value_passing;
        Log.d("print_query" , query);
        try {
            Call<JsonObject> negoCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            negoCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
                            int id = jsonObject.get("id").getAsInt();
                            String nama = jsonObject.get("nama").getAsString();
                            String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                            String username = jsonObject.get("username").getAsString();

                            if (from.equals("nego_pertama")) {
                                tvUpdateByNego1.setText(nama);
                                Log.d("CEK 1",nama);
                            }else if (from.equals("nego_kedua")) {
                                tvUpdateByNego1.setText(nama);
                                tvUpdateByNego2.setText(nama);
                                Log.d("CEK 2",nama);
                            }else if (from.equals("nego_ketiga")) {
                                tvUpdateByNego1.setText(nama);
                                tvUpdateByNego2.setText(nama);
                                tvUpdateByNego3.setText(nama);
                                Log.d("CEK 3",nama);
                            }

                            Log.d("print_query", id + " === " + nama + " === " + nama_perusahaan + " === " + username);
                        }else {
                            Log.d("", "onResponse: NOT success ");
                        }
                    }else {
                        Log.d("cekit","onResponse: NOT isSuccesful");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(DetailHistoryNego.this, "onFailure: func countingNegotiate " + t, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clasifyByNegoCount(int countNego) {
        Animation animation= AnimationUtils.loadAnimation(this, R.anim.fui_slide_in_right);

        if (countNego == 1){
            StepBean stepBean1 = new StepBean("Nego 1",0);
            StepBean stepBean2 = new StepBean("Nego 2",-1);
            StepBean stepBean3 = new StepBean("Nego 3",-1);
            listNego.add(stepBean1);
            listNego.add(stepBean2);
            listNego.add(stepBean3);

            layoutNego1.setVisibility(View.VISIBLE);
            layoutNego1.setAnimation(animation);


        }else if(countNego == 2){
            StepBean stepBean1 = new StepBean("Nego 1",1);
            StepBean stepBean2 = new StepBean("Nego 2",0);
            StepBean stepBean3 = new StepBean("Nego 3",-1);
            listNego.add(stepBean1);
            listNego.add(stepBean2);
            listNego.add(stepBean3);

            layoutNego1.setVisibility(View.VISIBLE);
            layoutNego2.setVisibility(View.VISIBLE);

            layoutNego1.setAnimation(animation);
            layoutNego2.setAnimation(animation);

        }else if (countNego == 3){
            StepBean stepBean1 = new StepBean("Nego 1",1);
            StepBean stepBean2 = new StepBean("Nego 2",1);
            StepBean stepBean3 = new StepBean("Nego 3",0);
            listNego.add(stepBean1);
            listNego.add(stepBean2);
            listNego.add(stepBean3);

            layoutNego1.setVisibility(View.VISIBLE);
            layoutNego2.setVisibility(View.VISIBLE);
            layoutNego3.setVisibility(View.VISIBLE);
            layoutNego1.setAnimation(animation);
            layoutNego2.setAnimation(animation);
            layoutNego3.setAnimation(animation);
        }

        lineNego.setStepViewTexts(listNego)
                .setTextSize(14)//set textSize
                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(getApplicationContext(), R.color.btnDisabled))
                .setStepViewComplectedTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary))
                .setStepViewUnComplectedTextColor(ContextCompat.getColor(getApplicationContext(), R.color.btnSecondaryDisabled))
                .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.verified_user))
                .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.default_icon))
                .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_verified_account))
                .ondrawIndicator();
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout2.stopShimmerAnimation();
//        shimmerFrameLayout3.stopShimmerAnimation();
        shimmerFrameNego.stopShimmerAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmerAnimation();
        shimmerFrameLayout2.startShimmerAnimation();
//        shimmerFrameLayout3.startShimmerAnimation();
        shimmerFrameNego.stopShimmerAnimation();
    }
}
