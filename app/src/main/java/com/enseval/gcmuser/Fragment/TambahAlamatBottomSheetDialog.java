package com.enseval.gcmuser.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Model.Kecamatan;
import com.enseval.gcmuser.Model.Kelurahan;
import com.enseval.gcmuser.Model.Kota;
import com.enseval.gcmuser.Model.Provinsi;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahAlamatBottomSheetDialog extends BottomSheetDialog {

    public TambahAlamatBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    //spinner belom disentuh sama sekali

    private TextInputLayout alamat, kodepos, notelp;
    private Button tambahBtn;
    private Spinner provinsi, kota, kecamatan, kelurahan;
    //private LoadingDialog loadingDialog;
    private long lastClickTime = 0;

    private ArrayList<Provinsi> provinsiArrayList;
    private ArrayList<Kota> kotaArrayList;
    private ArrayList<Kecamatan> kecamatanArrayList;
    private ArrayList<Kelurahan> kelurahanArrayList;

    private ArrayList<String> provinsiContain;
    private ArrayList<String> kotaContain;
    private ArrayList<String> kecamatanContain;
    private ArrayList<String> kelurahanContain;

    private static String idProvinsi, idKota, idKecamatan, idKelurahan;

    String TAG = "ido";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tambah_alamat_bootom_sheet_dialog);

        //loadingDialog = new LoadingDialog(getContext());

        provinsiArrayList = new ArrayList<>();
        kotaArrayList = new ArrayList<>();
        kecamatanArrayList = new ArrayList<>();
        kelurahanArrayList = new ArrayList<>();

        provinsiContain = new ArrayList<>();
        kotaContain = new ArrayList<>();
        kecamatanContain = new ArrayList<>();
        kelurahanContain = new ArrayList<>();

        provinsiContain.add("---Pilih Provinsi---");
        kotaContain.add("---Pilih Kota---");
        kecamatanContain.add("---Pilih Kecamatan");
        kelurahanContain.add("---Pilih Kelurahan---");

        final ArrayAdapter<String> provinsiAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.spinner_item, provinsiContain);
        final ArrayAdapter<String> kotaAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.spinner_item, kotaContain);
        final ArrayAdapter<String> kecamatanAdapter = new ArrayAdapter<String>(getContext(),//ido
                R.layout.spinner_item, kecamatanContain);//ido
        final ArrayAdapter<String> kelurahanAdapter = new ArrayAdapter<String>(getContext(),//ido
                R.layout.spinner_item, kelurahanContain);//ido

        alamat = findViewById(R.id.alamatLayout);
        kodepos = findViewById(R.id.kodeposLayout);
        notelp = findViewById(R.id.notelpLayout);
        provinsi = findViewById(R.id.provinsiSpinner);
        provinsiAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        provinsi.setAdapter(provinsiAdapter);
        kota = findViewById(R.id.kotaSpinner);
        kotaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kota.setAdapter(kotaAdapter);
        kecamatan = findViewById(R.id.kecamatanSpinner);
        kecamatanAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kecamatan.setAdapter(kecamatanAdapter);
        kelurahan = findViewById(R.id.kelurahanSpinner);
        kecamatanAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        tambahBtn = findViewById(R.id.tambahAlamatBtn);

        try {
            requestLokasi();
            Log.d(TAG, "reqlokasi berhasil");
        } catch (Exception e) {
            Log.d(TAG, "reqlokasi gagal");
            e.printStackTrace();
        }

        provinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kotaContain.clear();
                kotaContain.add("----Pilih Kota----");
                kota.setAdapter(kotaAdapter);
                if(kotaArrayList.size()>0){
                    for(Provinsi p : provinsiArrayList){
                        if(p.getProvinceName().equals(provinsi.getSelectedItem().toString())){
                            idProvinsi = p.getProvinceId();
                            Log.d("", "onItemSelected: "+idProvinsi);
                        }
                    }
                    for(Kota k : kotaArrayList){
                        if(k.getIdProvinsi().equals(idProvinsi)){
                            kotaContain.add(k.getNamaKota());
                        }
                    }
                }
                checkAlamat();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkAlamat();
            }
        });

        kota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kecamatanContain.clear();
                kecamatanContain.add("----Pilih Kecamatan----");
                kecamatan.setAdapter(kecamatanAdapter);
                if(kecamatanArrayList.size()>0){
                    for(Kota k : kotaArrayList){
                        if(k.getNamaKota().equals(kota.getSelectedItem().toString())){
                            idKota = k.getIdKota();
                            Log.d("", "onItemSelected: "+idKota);
                        }
                    }
                    for(Kecamatan k : kecamatanArrayList){
                        if(k.getIdKota().equals(idKota)){
                            kecamatanContain.add(k.getNamaKecamatan());
                        }
                    }
                }
                checkAlamat();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkAlamat();
            }
        });

        kecamatan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kelurahanContain.clear();
                kelurahanContain.add("----Pilih Kelurahan----");
                kelurahan.setAdapter(kelurahanAdapter);
                //Log.d(TAG, "Total Id Kelurahan: "+ kelurahanArrayList.size());
                if(kelurahanArrayList.size()>0){
                    for(Kecamatan k : kecamatanArrayList){
                        if(k.getNamaKecamatan().equals(kecamatan.getSelectedItem().toString())){
                            idKecamatan = k.getIdKecamatan();
                            Log.d(TAG, "onItemSelected: "+idKecamatan);
                        }
                    }
                    for(Kelurahan k : kelurahanArrayList){
                        if(k.getIdKecamatan().equals(idKecamatan)){
                            kelurahanContain.add(k.getNamaKelurahan());
                        }
                    }
                }
                checkAlamat();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkAlamat();
            }
        });
        kelurahan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(kelurahanArrayList.size()>0){
                    for(Kelurahan k : kelurahanArrayList){
                        if(k.getNamaKelurahan().equals(kelurahan.getSelectedItem().toString())){
                            idKelurahan = k.getCityIdidKelurahan();
                            Log.d("", "onItemSelected: "+idKelurahan);
                        }
                    }
                }
                checkAlamat();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkAlamat();
            }
        });

        checkAlamat();

        alamat.getEditText().addTextChangedListener(mWatcher);
        kodepos.getEditText().addTextChangedListener(mWatcher);
        notelp.getEditText().addTextChangedListener(mWatcher);

        tambahBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                        return;
                    }
                    else{
                        checkAlamat();
                        tambahAlamat();
                        //Toast.makeText(getContext(), "Alamat baru: "+alamat.getEditText().getText(), Toast.LENGTH_SHORT).show();
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void requestLokasi() throws Exception {
        int i = 0;
        while(i < 90000 ){
            Log.d(TAG, "SELECT * FROM gcm_master_kelurahan limit "+(25000)+" offset "+(i));
            Call<JsonObject> kelurahanCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kelurahan limit "+25000+" offset "+(i)+";")));
            kelurahanCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "sukses get data response");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String kelurahanId = jsonObject.get("id").getAsString();
                                String kecamatanId = jsonObject.get("id_kecamatan").getAsString();
                                String kelurahanName = jsonObject.get("nama").getAsString();

                                Kelurahan kelurahan = new Kelurahan(kelurahanId, kecamatanId, kelurahanName);
                                kelurahanArrayList.add(kelurahan);
                            }
                            Log.d(TAG, "onResponse: "+kelurahanArrayList.size());
//                            loadingDialog.hideDialog();
                        }else{
                            Log.d(TAG, "Status Error in kelurahan");
                        }
                    }else{
                        Log.d(TAG, "Status Error in kelurahan");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "Failur kelurahan: "+t.getMessage());
                    try {
//                        loadingDialog.hideDialog();
//                        content.setVisibility(View.INVISIBLE);
//                        failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            i+=25000;
        }
        Call <JsonObject> provinsiCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_location_province;")));
        provinsiCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        Log.d(TAG, "berhasil req propinsi");
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String provinceId = jsonObject.get("id").getAsString();
                            String provinceName = jsonObject.get("name").getAsString();

                            Provinsi provinsi = new Provinsi(provinceId,provinceName);
                            provinsiArrayList.add(provinsi);
                            provinsiContain.add(provinsi.getProvinceName());
                        }
//                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "gagal req propinsi");
                Log.d("", "onFailure: "+t.getMessage());
                try {
//                    loadingDialog.hideDialog();
//                    content.setVisibility(View.INVISIBLE);
//                    failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //REQUEST KOTA
        Call <JsonObject> kotaCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_city;")));

        kotaCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        Log.d(TAG, "berhasil req kota");
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String cityId = jsonObject.get("id").getAsString();
                            String provinceId = jsonObject.get("id_provinsi").getAsString();
                            String cityName = jsonObject.get("nama").getAsString();

                            Kota kota = new Kota(cityId,provinceId,cityName);
                            kotaArrayList.add(kota);
                        }
//                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "gagal req kota");
                Log.d("", "onFailure: "+t.getMessage());
                try {
//                    loadingDialog.hideDialog();
//                    content.setVisibility(View.INVISIBLE);
//                    failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //ido yang edit
        Call <JsonObject> kecamatanCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kecamatan;")));
        kecamatanCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        Log.d(TAG, "berhasil req camat");
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String kecamatanId = jsonObject.get("id").getAsString();
                            String kotaId = jsonObject.get("id_city").getAsString();
                            String kecamatanName = jsonObject.get("nama").getAsString();

                            Kecamatan kecamatan = new Kecamatan(kecamatanId, kotaId, kecamatanName);
                            kecamatanArrayList.add(kecamatan);
                        }
//                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "gagal req camat");
                Log.d("", "onFailure: "+t.getMessage());
                try {
//                    loadingDialog.hideDialog();
//                    content.setVisibility(View.INVISIBLE);
//                    failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //ido yang edit
    }

    TextWatcher mWatcher = new TextWatcher() {
        int len = 0;
        final android.os.Handler handler = new android.os.Handler();
        Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(final Editable s) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(kodepos.getEditText().getText().hashCode()==s.hashCode()){
                        if(kodepos.getEditText().getText().length()!=5){
                            kodepos.setErrorEnabled(true);
                            kodepos.setError("Tidak valid");
                            Log.d(TAG, "run: masuk ke if");
                        }
                        else{
                            kodepos.setErrorEnabled(false);
                            Log.d(TAG, "run: masuk ke else");
                        }
                    }
                    if(notelp.getEditText().getText().hashCode()==s.hashCode()){
                        if(notelp.getEditText().getText().length()<=7){
                            notelp.setErrorEnabled(true);
                            notelp.setError("No Telp Tidak valid");
                        }
                        else{
                            notelp.setErrorEnabled(false);
                        }
                    }
                    checkAlamat();
                }
            };
            handler.postDelayed(runnable, 700);
            checkAlamat();
        }
    };

    private void checkAlamat(){
        if((TextUtils.isEmpty(alamat.getEditText().getText()))
                || (TextUtils.isEmpty(kodepos.getEditText().getText()))
                || (TextUtils.isEmpty(notelp.getEditText().getText()))
                || provinsi.getSelectedItemPosition()==0
                || kota.getSelectedItemPosition()==0
                || kecamatan.getSelectedItemPosition()==0
                || kelurahan.getSelectedItemPosition()==0
                || notelp.isErrorEnabled()
                || kodepos.isErrorEnabled()){
            tambahBtn.setEnabled(false);
            Log.d(TAG, "masih kosong");
        }else{
            tambahBtn.setEnabled(true);
            Log.d(TAG, "Berhasil!!!");
            Log.d(TAG, "checkAlamat: "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId());
        }
    }

    private void tambahAlamat(){
        try {
            Call<JsonObject> tambahAlamatCall = RetrofitClient
                    .getInstance2()
                    .getApi()
                    .requestInsert(new JSONRequest(QueryEncryption.Encrypt("INSERT INTO gcm_master_alamat "+
                            "(kelurahan, kecamatan, kota, provinsi, kodepos, no_telp, shipto_active, billto_active, company_id, alamat) "+
                            "VALUES ('"+idKelurahan+"', "+
                            "'"+idKecamatan+"',"+
                            "'"+idKota+"',"+
                            "'"+idProvinsi+"',"+
                            "'"+kodepos.getEditText().getText().toString()+"'," +
                            "'"+notelp.getEditText().getText().toString()+"'," +
                            "'"+'N'+"'," +
                            "'"+'N'+"'," +
                            "'"+SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+"'," +
                            "'"+alamat.getEditText().getText().toString()+"');")));
            tambahAlamatCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            Toast.makeText(getContext(), "Tambah Alamat berhasil!", Toast.LENGTH_LONG).show();
                            dismiss();
                        } else if (status.equals("error")) {
                            Toast.makeText(getContext(), "Tambah Alamat gagal", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getContext(), "Gagal menambahkan alamat, Coba periksa koneksi internet anda.", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
