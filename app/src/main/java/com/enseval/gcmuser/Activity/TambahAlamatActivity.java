package com.enseval.gcmuser.Activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.ListAlamat;
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

public class TambahAlamatActivity extends AppCompatActivity {
    private TextInputLayout alamat, kodepos, notelp;
    private TextInputEditText alamatt, kodeposs, no_telpp;
    private Button tambahBtn;
    private Spinner provinsi, kota, kecamatan, kelurahan;
    private String status, billtoActive, shiptoActive;
    private ImageView kembali;
    //private LoadingDialog loadingDialog;
    private long lastClickTime = 0;

    ArrayList<Integer> listIdSeller;

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
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), ListAlamat.class);
        i.putExtra("tipe", "edit");
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_alamat);

        status = getIntent().getStringExtra("status");
        billtoActive = getIntent().getStringExtra("billtoActive");
        shiptoActive = getIntent().getStringExtra("shiptoActive");

        provinsiArrayList = new ArrayList<>();
        kotaArrayList = new ArrayList<>();
        kecamatanArrayList = new ArrayList<>();
        kelurahanArrayList = new ArrayList<>();

        provinsiContain = new ArrayList<>();
        kotaContain = new ArrayList<>();
        kecamatanContain = new ArrayList<>();
        kelurahanContain = new ArrayList<>();

        provinsiContain.add( "----Pilih Provinsi----");
        kotaContain.add( "----Pilih Kota----");
        kecamatanContain.add( "----Pilih Kecamatan----");
        kelurahanContain.add( "----Pilih Kelurahan----");

        getProvinsi();
        getIdSeller();

        final ArrayAdapter<String> provinsiAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.spinner_item, provinsiContain);
        final ArrayAdapter<String> kotaAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.spinner_item, kotaContain);
        final ArrayAdapter<String> kecamatanAdapter = new ArrayAdapter<String>(getApplicationContext(),//ido
                R.layout.spinner_item, kecamatanContain);//ido
        final ArrayAdapter<String> kelurahanAdapter = new ArrayAdapter<String>(getApplicationContext(),//ido
                R.layout.spinner_item, kelurahanContain);//ido

        alamat = findViewById(R.id.alamatLayout);
        alamatt = findViewById(R.id.alamatt);
        kodeposs = findViewById(R.id.kodeposs);
        no_telpp = findViewById(R.id.no_telpp);
        kodepos = findViewById(R.id.kodeposLayout);
        notelp = findViewById(R.id.notelpLayout);
        tambahBtn = findViewById(R.id.tambahAlamatBtn);
        provinsi = findViewById(R.id.provinsiSpinner);
        provinsiAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        provinsi.setAdapter(provinsiAdapter);
        kota = findViewById(R.id.kotaSpinner);
        kotaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kecamatan = findViewById(R.id.kecamatanSpinner);
        kecamatanAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kelurahan = findViewById(R.id.kelurahanSpinner);
        kelurahanAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kembali = findViewById(R.id.btnKembali);



        kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ListAlamat.class);
                i.putExtra("tipe", "edit");
                startActivity(i);
                finish();
            }
        });

        if (status.equals("ubah")){
            alamatt.setText(getIntent().getStringExtra("alamat"));
            kodeposs.setText(getIntent().getStringExtra("kodepos"));
            no_telpp.setText(getIntent().getStringExtra("no_telp"));

            tambahBtn.setText("Ubah Alamat");
        }

        provinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kotaContain.clear();
                kotaContain.add("----Pilih Kota----");
                for (Provinsi prov : provinsiArrayList) {
                    if (prov.getProvinceName().equals(provinsi.getSelectedItem().toString())) {
                        idProvinsi = prov.getProvinceId();
                        Log.d(TAG, "onItemSelected: " + idProvinsi);
                    }
                }
                if (idProvinsi != "") {
                    getKota(idProvinsi);
                    kota.setAdapter(kotaAdapter);
                    checkAlamat();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkAlamat();
            }
        });

        //ido yang edit
        kota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kecamatanContain.clear();
                kecamatanContain.add("----Pilih Kecamatan----");
                for (Kota kotaa : kotaArrayList) {
                    if (kotaa.getNamaKota().equals(kota.getSelectedItem().toString())) {
                        idKota = kotaa.getIdKota();
                        Log.d("", "onItemSelected: " + idKota);
                    }
                }
                if (idKota != "") {
                    getKecamatan(idKota);
                    kecamatan.setAdapter(kecamatanAdapter);
                    checkAlamat();
                }
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
                for (Kecamatan kec : kecamatanArrayList) {
                    if (kec.getNamaKecamatan().equals(kecamatan.getSelectedItem().toString())) {
                        idKecamatan = kec.getIdKecamatan();
                        Log.d("", "onItemSelected: " + idKecamatan);
                    }
                }
                if (idKecamatan != "") {
                    getKelurahan(idKecamatan);
                    kelurahan.setAdapter(kelurahanAdapter);
                    checkAlamat();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkAlamat();
            }
        });

        kelurahan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for(Kelurahan k : kelurahanArrayList){
                    if(k.getNamaKelurahan().equals(kelurahan.getSelectedItem().toString())){
                        idKelurahan = k.getCityIdidKelurahan();
                        Log.d("", "onItemSelected: "+idKelurahan);
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
                    }else{
                        checkAlamat();
                        if (status.equals("ubah")){
                            ubahAlamat(getIntent().getIntExtra("id", 0));
                        }else {
                            tambahAlamat();
                        }
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private void getProvinsi(){
        try {
            String query = "SELECT * FROM gcm_location_province;";
            Call<JsonObject> provinsiCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
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

                                Provinsi provinsi = new Provinsi(provinceId, provinceName);
                                provinsiArrayList.add(provinsi);
                                provinsiContain.add(provinsi.getProvinceName());
                            }
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
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

    private void getKota(String idProvinsi){
        String query = "SELECT * FROM gcm_master_city where id_provinsi = '"+idProvinsi+"';";
        try {
            Call <JsonObject> kotaCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));

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
                                kotaContain.add(kota.getNamaKota());
                            }
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "gagal req kota");
                    Log.d("", "onFailure: "+t.getMessage());
                    try {
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getKecamatan(String idKota){
        try {
            Call <JsonObject> kecamatanCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kecamatan where id_city = '"+idKota+"'")));
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
                                kecamatanContain.add(kecamatan.getNamaKecamatan());
                            }
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "gagal req camat");
                    Log.d("", "onFailure: "+t.getMessage());
                    try {
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getKelurahan(String idKecamatan){
        try {
            Call <JsonObject> kelurahanCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kelurahan where id_kecamatan = '"+idKecamatan+"';")));
            kelurahanCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "sukses req lurah");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String kelurahanId = jsonObject.get("id").getAsString();
                                String kecamatanId = jsonObject.get("id_kecamatan").getAsString();
                                String kelurahanName = jsonObject.get("nama").getAsString();

                                Kelurahan kelurahan = new Kelurahan(kelurahanId, kecamatanId, kelurahanName);
                                kelurahanArrayList.add(kelurahan);
                                kelurahanContain.add(kelurahan.getNamaKelurahan());
                            }
                            Log.d(TAG, "onResponse: "+kelurahanArrayList.size());
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "Failur kelurahan: "+t.getMessage());
                    try {
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
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
            Log.d(TAG, "checkAlamat: "+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId());
        }
    }

    private void getIdSeller(){
        String query = "select seller_id from gcm_company_listing gcl where buyer_id  = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+";";
        try {
            Call<JsonObject> callIdSeller = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callIdSeller.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listIdSeller = new ArrayList<>();
                            for (int i=0; i<jsonArray.size(); i++){
                                int seller_id = jsonArray.get(i).getAsJsonObject().get("seller_id").getAsInt();
                                listIdSeller.add(seller_id);
                            }
                            Log.d(TAG, "idSeller: "+listIdSeller.size());
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

    private void tambahAlamat(){
        String queryAlamat = "with new_insert as (insert into gcm_master_alamat " +
                "(kelurahan, kecamatan, kota, provinsi, kodepos, no_telp, shipto_active, billto_active, company_id, alamat, flag_active) " +
                "values (" +
                "'"+idKelurahan+"', "+
                "'"+idKecamatan+"', "+
                "'"+idKota+"', "+
                "'"+idProvinsi+"', "+
                "'"+kodepos.getEditText().getText().toString()+"', " +
                "'"+notelp.getEditText().getText().toString()+"', " +
                "'"+'N'+"', " +
                "'"+'N'+"', " +
                "'"+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+"', " +
                "'"+alamat.getEditText().getText().toString()+"', "+
                "'A') returning id )";
        queryAlamat = queryAlamat + "insert into gcm_listing_alamat (id_master_alamat, id_buyer, id_seller, kode_shipto_customer, kode_billto_customer) values ";
        String loop = "";
        for (int i=0; i<listIdSeller.size(); i++){
            loop = loop + "((select id from new_insert), "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+listIdSeller.get(i)+", null, null) ";
            if (i < listIdSeller.size() - 1){
                loop = loop.concat(",");
            }
        }
        queryAlamat = queryAlamat + loop + " returning id";
        Log.d(TAG, "tambahAlamat: "+queryAlamat);
        try {
            Call<JsonObject> tambahAlamatCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(queryAlamat)));
            tambahAlamatCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Tambah Alamat berhasil!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), ListAlamat.class);
                        i.putExtra("tipe", "edit");
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Tambah Alamat gagal", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "tambahAlamat: query ga sukses");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Gagal menambahkan alamat, Coba periksa koneksi internet anda.", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void ubahAlamat(int id){
        String query;
        String shipto;
        String queryUpdateCart="";
        if (billtoActive.equals("Y")){
            if (shiptoActive.equals("Y")){
                shipto = "Y";
                queryUpdateCart = ", shipto_id = (select id from new_insert)";
            }else{
                shipto = "N";
            }
            Log.d(TAG, "ubahAlamat: "+queryUpdateCart);
            query = "with new_update as (update gcm_master_alamat set shipto_active = 'N', billto_active = 'N', flag_active = 'I' where id = "+id+" ), " +
                    "new_insert as (insert into gcm_master_alamat (kelurahan, kecamatan, kota, provinsi, kodepos, no_telp, shipto_active, billto_active, company_id, alamat, flag_active) values ( " +
                    "'"+idKelurahan+"', "+
                    "'"+idKecamatan+"', "+
                    "'"+idKota+"', "+
                    "'"+idProvinsi+"', "+
                    "'"+kodepos.getEditText().getText().toString()+"', " +
                    "'"+notelp.getEditText().getText().toString()+"', " +
                    "'"+shipto+"', " +
                    "'"+'Y'+"', " +
                    "'"+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+"', " +
                    "'"+alamat.getEditText().getText().toString()+"', "+
                    "'A') returning id ), " +
                    "new_update_1 as (update gcm_master_cart set billto_id = (select id from new_insert) "+queryUpdateCart+" where company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and status = 'A') ";
            query = query + "insert into gcm_listing_alamat (id_master_alamat, id_buyer, id_seller, kode_shipto_customer, kode_billto_customer) values ";
            String loop = "";
            for (int i=0; i<listIdSeller.size(); i++){
                loop = loop + "((select id from new_insert), "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+listIdSeller.get(i)+", null, null) ";
                if (i < listIdSeller.size() - 1){
                    loop = loop.concat(",");
                }
            }
            query = query + loop + " returning id";
        }else {
            query = "update gcm_master_alamat set " +
                    "alamat = '" + alamat.getEditText().getText().toString() + "', " +
                    "provinsi = '" + idProvinsi + "', " +
                    "kota = '" + idKota + "', " +
                    "kecamatan = '" + idKecamatan + "', " +
                    "kelurahan = '" + idKelurahan + "', " +
                    "kodepos = '" + kodepos.getEditText().getText().toString() + "', " +
                    "no_telp = '" + notelp.getEditText().getText().toString() + "' where id = " + id + ";";
        }
        Log.d(TAG, "ubahAlamat: "+query);
        try {
            Call<JsonObject> callUbahAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callUbahAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Alamat berhasil diubah", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), ListAlamat.class);
                        i.putExtra("tipe", "edit");
                        startActivity(i);
                        finish();
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

}
