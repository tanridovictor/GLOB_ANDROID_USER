package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.AlamatAdapter;
import com.enseval.gcmuser.Model.Alamat;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class ListAlamat extends AppCompatActivity {

    private RecyclerView rvAlamat;
    private TextView btnTambahAlamat;
    private ImageButton btnBack;
    private TextView edit;
    private SwipeRefreshLayout srlAlamat;
    private static ArrayList<Alamat> listAlamat;
    private ArrayList<Company> listCheckoutCompany;
    private ArrayList<Cart> listCart;
    private AlamatAdapter adapter;
    private long lastClickTime = 0;
    String TAG ="ido";
    private String flag;
    private long total;
    private float kursIdr;
    private String Status;
    private int maxAlamat;

    private ConstraintLayout noItem, failed;
    private Button refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alamat);

        maxAlamat();
        //kursIdr = getIntent().getFloatExtra("kurs",0);
        Status = getIntent().getStringExtra("tipe");

        rvAlamat = findViewById(R.id.rvAlamat);
        btnTambahAlamat = findViewById(R.id.tambahAlamat);
        srlAlamat = findViewById(R.id.srlAlamat);
        btnBack = findViewById(R.id.btnBack);

        noItem = findViewById(R.id.noItem);
        failed = findViewById(R.id.failed);
        refresh = findViewById(R.id.refresh);

        noItem.setVisibility(GONE);
        failed.setVisibility(GONE);

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

        if (Status.equals("pilih")){
            //edit.setVisibility(View.GONE);
            listCart = getIntent().getParcelableArrayListExtra("arridcart");
            flag = getIntent().getStringExtra("flag");
            listCheckoutCompany = getIntent().getParcelableArrayListExtra("listSeller");
            total = getIntent().getLongExtra("total",0);
            btnTambahAlamat.setVisibility(View.GONE);
        }else{
            //edit.setVisibility(View.VISIBLE);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //untuk memberikan warna pada loading
        srlAlamat.setColorSchemeResources(R.color.colorPrimary);

        //untuk loading swipe refresh
        srlAlamat.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        srlAlamat.setRefreshing(false);
                        alamatRequest();
                    }
                }, 2000);
            }
        });

        alamatRequest();

        //untuk menampilkan bottom sheet tambah alamat
        btnTambahAlamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TambahAlamatBottomSheetDialog bottomSheet = new TambahAlamatBottomSheetDialog(ListAlamat.this);
//                bottomSheet.setContentView(R.layout.tambah_alamat_bootom_sheet_dialog);
//                bottomSheet.dismiss();
//                bottomSheet.show();
                Log.d(TAG, "Max Alamat: " + maxAlamat);
                if (maxAlamat == 3) {
                    final Dialog dialog = new Dialog(ListAlamat.this);
                    dialog.setContentView(R.layout.konfirmasi_dialog);
                    Window window = dialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                    Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                    TextView title = dialog.findViewById(R.id.title);
                    TextView description = dialog.findViewById(R.id.description);
                    dialog.setCancelable(false);
                    btnBatal.setVisibility(View.GONE);

                    title.setText("Tambah Alamat");
                    description.setText("Tidak dapat menambah alamat. Jumlah alamat telah mencapai batas maksimal (" + maxAlamat + ")");

                    btnBatal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btnSetuju.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return;
                            } else {
                                dialog.dismiss();
                            }
                        }
                    });

                    dialog.show();
                } else {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                        return;
                    } else {
                        Intent i = new Intent(getApplicationContext(), TambahAlamatActivity.class);
                        i.putExtra("status", "tambahAlamat");
                        startActivity(i);
                        finish();
                    }
                    lastClickTime = SystemClock.elapsedRealtime();
                }
            }
        });


    }

    //method untuk request alamat
    private void alamatRequest(){
        try {
            Call<JsonObject> cartCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select a.id, alamat, b.name as provinsi, c.nama as kota, d.nama as kecamatan, e.nama as kelurahan, kodepos, shipto_active, billto_active from gcm_master_alamat a inner join gcm_location_province b on a.provinsi = b.id \n" +
                            "inner join gcm_master_city c on a.kota = c.id inner join gcm_master_kecamatan d on a.kecamatan = d.id inner join gcm_master_kelurahan e on a.kelurahan = e.id \n" +
                            "where a.company_id = "+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and a.flag_active ='A' order by a.id asc;")));
            Log.d(TAG, "alamatRequest: "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId());
            cartCall.enqueue(new Callback<JsonObject>(){
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "onResponse: sukses");
                        String status =  response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "onResponse: sukses lagi");
                            listAlamat = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            Log.d(TAG, "onResponse: "+jsonArray.size());
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listAlamat.add(new Alamat(
                                        jsonObject.get("kelurahan").getAsString(),
                                        jsonObject.get("kecamatan").getAsString(),
                                        jsonObject.get("kota").getAsString(),
                                        jsonObject.get("provinsi").getAsString(),
                                        jsonObject.get("kodepos").getAsString(),
                                        jsonObject.get("alamat").getAsString(),
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("shipto_active").getAsString(),
                                        jsonObject.get("billto_active").getAsString()
                                ));
                            }
                            Log.d(TAG, "onResponse: "+listAlamat.size());
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ListAlamat.this);
                            rvAlamat.setLayoutManager(layoutManager);
                            rvAlamat.setItemAnimator(new DefaultItemAnimator());
                            adapter = new AlamatAdapter(ListAlamat.this, listAlamat, listCart, flag, listCheckoutCompany, total, Status);
                            rvAlamat.setAdapter(adapter);
                        }
                    }else{
                        Log.d(TAG, "onResponse: gagal cuyyy");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    failed.setVisibility(View.VISIBLE);
                    rvAlamat.setVisibility(GONE);
                    btnTambahAlamat.setVisibility(GONE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void maxAlamat(){
        String query = "select count(id) from gcm_master_alamat where company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" " +
                "and flag_active = 'A'";
        try {
            Call<JsonObject> callMaxAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callMaxAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            maxAlamat = jsonArray.get(0).getAsJsonObject().get("count").getAsInt();
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
}
