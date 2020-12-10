package com.enseval.gcmuser.Fragment;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.CatalogActivity;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Adapter.BarangAdapter;
import com.enseval.gcmuser.Adapter.CategoryAdapter;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Kategori;
import com.enseval.gcmuser.Response.BarangResponse;
import com.enseval.gcmuser.Response.CategoryResponse;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**Fragment halaman utama (home/beranda)*/
public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private ArrayList<Kategori> listCategory;
    private ArrayList<Barang> listBarang;
    private RecyclerView rvCategory, rvBarang;
    private CategoryAdapter categoryAdapter;
    private BarangAdapter barangAdapter;
    private ShimmerFrameLayout shimmerFrameLayout, shimmerFrameLayout2;
    private EditText etSearch;
    private NestedScrollView scroll;
    private ConstraintLayout failed;
    private Button refresh;
    private long lastClickTime=0;
    //private static float kursIdr;
    private ArrayMap<Integer, String> listSellerStatus;
    private String strSellerStatusKevin = "";
    private Spinner spinner1, spinner2;
    private CardView nonlangganan;
    private TextView barangTerbaru;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.changeNavbar(0);

        checkReceived();
        statusRequestKevin();
        //checkCancel();

        listCategory = new ArrayList<>();
        listBarang = new ArrayList<>();

        //kondisi awal munculin skeleton + efek shimmernya (karena masih nunggu response dr backend)
        rvBarang = view.findViewById(R.id.rvBarang);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout2 = view.findViewById(R.id.shimmer_view_container2);
        rvCategory = view.findViewById(R.id.rvCategory);
        etSearch = view.findViewById(R.id.etSearch);
        scroll = view.findViewById(R.id.scroll);
        failed = view.findViewById(R.id.failed);
        refresh = view.findViewById(R.id.refresh);
        spinner1 = view.findViewById(R.id.spinfilter1);
        nonlangganan = view.findViewById(R.id.cvNonlangganan);
        barangTerbaru = view.findViewById(R.id.barangTerbaru);
        barangTerbaru.setVisibility(View.GONE);
        //spinner2 = view.findViewById(R.id.spinfilter2);

        scroll.setVisibility(View.VISIBLE);
        failed.setVisibility(View.INVISIBLE);
        nonlangganan.setVisibility(View.INVISIBLE);

        //ido
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.spinfilter1, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);

        if(SharedPrefManager.getInstance(getContext()).isLoggedin()){
            spinner1.setVisibility(view.VISIBLE);
            //spinner2.setVisibility(view.VISIBLE);
        }else{
            spinner1.setVisibility(View.GONE);
            //spinner1.setVisibility(view.INVISIBLE);
            //spinner2.setVisibility(view.INVISIBLE);
        }
        //tombol refresh jika loading halaman gagal
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(getActivity().getIntent());
                    getActivity().finish();
                    startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        //request kategori dan nilai kurs hari ini
        categoryRequest();
        //barangRequest();
//        requestBarang();


        Log.d("ido", "Token Lokal: "+SharedPrefManager.getInstance(getContext()).getToken());

        etSearch.addTextChangedListener(new TextWatcher() {
            final android.os.Handler handler = new android.os.Handler();
            Runnable runnable;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(s.length()>0){
                            Intent intent = new Intent(getActivity(), CatalogActivity.class);
                            intent.putExtra("keyword",etSearch.getText().toString());
                            intent.putExtra("tipe", "search");
                            startActivity(intent);
                        }
                    }
                };
                handler.postDelayed(runnable, 700);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout2.stopShimmerAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        //memastikan ketika resume posisi search bar kosong
        etSearch.setText("");
        etSearch.clearFocus();
        shimmerFrameLayout.startShimmerAnimation();
        shimmerFrameLayout2.startShimmerAnimation();
        //memastikan ketika resume posisi menu navbar di home
        MainActivity.changeNavbar(0);
    }

    /**Method untuk request kategori barang*/
    private void categoryRequest() {
        try {
            String query = "select * from gcm_master_category gmc order by id asc;";
            String encryptedQuery = QueryEncryption.Encrypt(query);

            JSONRequest jsonRequest = new JSONRequest(encryptedQuery);
            //response langsung dimasukkan ke dalam model CategoryResponse
            Call<CategoryResponse> categoryCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .requestCategory(jsonRequest);

            categoryCall.enqueue(new Callback<CategoryResponse>() {
                @Override
                public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                    if(response.isSuccessful()){
                        if(response.body().getData().size()>0){
                            listCategory = response.body().getData();
                            shimmerFrameLayout2.stopShimmerAnimation();
                            shimmerFrameLayout2.setVisibility(View.GONE);
                            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);
                            rvCategory.setLayoutManager(layoutManager);
                            rvCategory.setItemAnimator(new DefaultItemAnimator());
                            categoryAdapter = new CategoryAdapter(getActivity(), listCategory); //adapter utk category
                            rvCategory.setAdapter(categoryAdapter);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CategoryResponse> call, Throwable t) {
                    scroll.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestBarang(){
        String getBarang = "SELECT a.nama, b.id, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang, '.png')) as foto, category_id, b.company_id, c.nama_perusahaan, d.alias, e.nominal as kurs " +
                "FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
                "inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join " +
                "gcm_listing_kurs e on e.company_id=b.company_id where b.status='A' and now() " +
                "between e.tgl_start and e.tgl_end order by b.create_date desc, category_id asc, nama asc;";
        try {
            JSONRequest jsonRequest = new JSONRequest(QueryEncryption.Encrypt(getBarang));
            Call<BarangResponse> requestBarang = RetrofitClient
                    .getInstance()
                    .getApi()
                    .requestBarang(jsonRequest);
            requestBarang.enqueue(new Callback<BarangResponse>() {
                @Override
                public void onResponse(Call<BarangResponse> call, Response<BarangResponse> response) {
                    if(response.isSuccessful()){
                        if(response.body().getData().size()>0){
                            listBarang = response.body().getData();
                            statusRequest(listBarang);
                        }
                    }
                }

                @Override
                public void onFailure(Call<BarangResponse> call, Throwable t) {
                    scroll.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**Method untuk request barang*/
    private void barangRequest(String query) {
        try{
            Log.d("ido", "barangRequest: "+query);
            JSONRequest jsonRequest = new JSONRequest(QueryEncryption.Encrypt(query));
            Log.d("ido", "barangRequest: "+QueryEncryption.Encrypt(query));
            //response langsung dimasukkan ke dalam model BarangResponse
            Call <BarangResponse> barangCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .requestBarang(jsonRequest);
            barangCall.enqueue(new Callback<BarangResponse>() {
                @Override
                public void onResponse(Call<BarangResponse> call, Response<BarangResponse> response) {
                    if(response.isSuccessful()){
                        nonlangganan.setVisibility(View.INVISIBLE);
                        if(response.body().getData().size()>0){
                            listBarang = response.body().getData();
                            statusRequest(listBarang);
                        }else{
                            scroll.setEnabled(false);
                            nonlangganan.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<BarangResponse> call, Throwable t) {
                    scroll.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk request status pengguna*/
    private void statusRequest(final ArrayList<Barang> listBarang){
        if(SharedPrefManager.getInstance(getContext()).isLoggedin()){
            try {
                Call<JsonObject> statusCall = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt("SELECT seller_id, status FROM gcm_company_listing where buyer_id="+
                                SharedPrefManager.getInstance(getContext()).getUser().getCompanyId())));

                statusCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                listSellerStatus = new ArrayMap<>();

                                //masukkan respon berupa seller id dan statusnya ke dalam array map
                                for(int i=0; i<jsonArray.size(); i++){
                                    listSellerStatus.put(
                                            jsonArray.get(i).getAsJsonObject().get("seller_id").getAsInt(),
                                            jsonArray.get(i).getAsJsonObject().get("status").getAsString()
                                    );
                                }
                                shimmerFrameLayout.stopShimmerAnimation();
                                shimmerFrameLayout.setVisibility(View.GONE);
                                RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(getContext(),2);
                                rvBarang.setLayoutManager(layoutManager1);
                                rvBarang.setItemAnimator(new DefaultItemAnimator());
                                barangAdapter = new BarangAdapter(getActivity(), listBarang, listSellerStatus); //buat adapter utk barang dengan parameter status juga
                                rvBarang.setAdapter(barangAdapter);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            listSellerStatus = new ArrayMap<>();
            shimmerFrameLayout.stopShimmerAnimation();
            shimmerFrameLayout.setVisibility(View.GONE);
            RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(getContext(),2);
            rvBarang.setLayoutManager(layoutManager1);
            rvBarang.setItemAnimator(new DefaultItemAnimator());
            barangAdapter = new BarangAdapter(getActivity(), listBarang, listSellerStatus);
            rvBarang.setAdapter(barangAdapter);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String textspinner = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(),textspinner , Toast.LENGTH_SHORT).show();
        if(spinner1.getSelectedItemId() == 0) {
            String query="";
//            if(SharedPrefManager.getInstance(getContext()).isLoggedin()) {
//                if (SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis() == 1) {
//                    query = "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
//                            "(select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, " +
//                            "case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, " +
//                            "b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
//                            "FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
//                            "inner join gcm_master_company c on b.company_id=c.id " +
//                            "inner join  gcm_master_satuan d on a.satuan=d.id " +
//                            "inner join gcm_listing_kurs e on e.company_id = b.company_id " +
//                            "where a.status = 'A' and b.status='A' and b.company_id in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and now() between e.tgl_start and e.tgl_end " +
//                            "order by b.create_date desc, category_id asc, nama asc;";
//                }else{
//                    query = "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, "+
//                            "b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
//                            "inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id = b.company_id where a.status = 'A' and b.status='A' and b.company_id in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and a.category_id!= 1 and now() between e.tgl_start and e.tgl_end order by b.create_date desc, category_id asc, nama asc;";
//                }
//            }else{
//                query = "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, e.nominal as kurs "+
//                        "FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
//                        "inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status = 'A' and b.status='A' and now() between e.tgl_start and e.tgl_end order by b.create_date desc, category_id asc, nama asc;";
//            }

            if (SharedPrefManager.getInstance(getContext()).isLoggedin()){
                if (SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis() != 1){
                    query = "select * from (" +
                            "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                            "b.company_id in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and category_id not in (1,5)  and now() between e.tgl_start and e.tgl_end " +
                            "union all " +
                            "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                            "b.company_id in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and category_id = 5 and b.departmen_sales = "+SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis()+"" +
                            " and now() between e.tgl_start and e.tgl_end " +
                            ") as produk order by produk.nama asc";
                }else{
                    query = "select * from ( " +
                            "(SELECT a.nama, b.id, b.barang_id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                            "b.company_id in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and category_id != 5 and now() between e.tgl_start and e.tgl_end) " +
                            "union all " +
                            "(SELECT distinct on (b.barang_id) a.nama, b.id, b.barang_id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and b.company_id in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") " +
                            "and category_id = 5 and now() between e.tgl_start and e.tgl_end order by barang_id, price desc ) " +
                            ") as produk order by produk.nama asc";
                }
            }

            barangRequest(query);
        }
        else if(spinner1.getSelectedItemId()==1){
            String query="";
//            if(SharedPrefManager.getInstance(getContext()).isLoggedin()){
//                if (SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis() == 1) {
////                        query = "SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
////                            "where b.status='A' and b.company_id in ("+strSellerStatusKevin+") order by b.create_date desc, category_id asc, nama asc;";
//                    query = "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
//                            "inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id = b.company_id where a.status = 'A' and b.status='A' and b.company_id not in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and now() between e.tgl_start and e.tgl_end order by b.create_date desc, category_id asc, nama asc;";
//                } else {
////                        query = "SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
////                                "where b.status='A' and b.company_id in ("+strSellerStatusKevin+") and a.category_id="+SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis()+" order by b.create_date desc, category_id asc, nama asc;";
//                    query = "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id " +
//                            "inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id = b.company_id where a.status = 'A' and b.status='A' and b.company_id not in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and a.category_id!= 1 and now() between e.tgl_start and e.tgl_end order by b.create_date desc, category_id asc, nama asc;";
//                }
//            }

            if (SharedPrefManager.getInstance(getContext()).isLoggedin()){
                if (SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis() == 1){
                    query = "SELECT * FROM ( " +
                            "(SELECT a.nama, b.barang_id, b.id, b.kode_barang, price, price_terendah, foto, " +
                            "flag_foto, category_id, b.company_id, berat, b.deskripsi, b.jumlah_min_beli, " +
                            "b.jumlah_min_nego, c.kode_seller, c.nama_perusahaan, d.alias as satuan " +
                            "FROM gcm_master_satuan d ,gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' " +
                            "and b.status='A' and b.company_id = c.id and d.id = a.satuan " +
                            "and b.company_id not in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and a.category_id !=5) " +
                            "union all " +
                            "(SELECT distinct on (b.barang_id) a.nama, b.barang_id, b.id, b.kode_barang, price, price_terendah, foto, " +
                            "flag_foto, category_id, b.company_id, berat, b.deskripsi, b.jumlah_min_beli, " +
                            "b.jumlah_min_nego, c.kode_seller, c.nama_perusahaan, d.alias as satuan " +
                            "FROM gcm_master_satuan d ,gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' " +
                            "and b.status='A' and b.company_id = c.id and d.id = a.satuan " +
                            "and b.company_id not in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and a.category_id =5 order by b.barang_id, price desc )) as produk order by produk.nama";
                }else{
                    query = "SELECT * FROM ( " +
                            "(SELECT a.nama, b.barang_id, b.id, b.kode_barang, price, price_terendah, foto, " +
                            "flag_foto, category_id, b.company_id, berat, b.deskripsi, b.jumlah_min_beli, " +
                            "b.jumlah_min_nego, c.kode_seller, c.nama_perusahaan, d.alias as satuan " +
                            "FROM gcm_master_satuan d ,gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' " +
                            "and b.status='A' and b.company_id = c.id and d.id = a.satuan " +
                            "and b.company_id not in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and category_id not in (1,5)) " +
                            "union all " +
                            "(SELECT a.nama, b.barang_id, b.id, b.kode_barang, price, price_terendah, foto, " +
                            "flag_foto, category_id, b.company_id, berat, b.deskripsi, b.jumlah_min_beli," +
                            "b.jumlah_min_nego, c.kode_seller, c.nama_perusahaan, d.alias as satuan " +
                            "FROM gcm_master_satuan d ,gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' " +
                            "and b.status='A' and b.company_id = c.id and d.id = a.satuan " +
                            "and b.company_id not in ("+SharedPrefManager.getInstance(getContext()).getActiveSeller()+") and category_id = 5 and b.departmen_sales = "+SharedPrefManager.getInstance(getContext()).getUser().getTipeBisnis()+"" +
                            " )) as produk order by produk.nama";
                }
            }

            barangRequest(query);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //    /**Method untuk request kurs ke API*/
//    private void kursRequest(){
////        Call<JsonObject> callKurs = RetrofitClient
////                .getInstanceKurs()
////                .getApi()
////                .requestKurs("USD");
////
////        callKurs.enqueue(new Callback<JsonObject>() {
////            @Override
////            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
////                JsonObject jsonObject = response.body().getAsJsonObject().get("rates").getAsJsonObject();
////                kursIdr = jsonObject.get("IDR").getAsFloat(); //data kurs
////                barangRequest(); //request barang ketika kurs sudah didapat
////                Log.d("kursnyaaa", "onResponse: "+kursIdr);
////            }
////
////            @Override
////            public void onFailure(Call<JsonObject> call, Throwable t) {
////                Log.d("", "onFailure: "+t.getMessage());
////                scroll.setVisibility(View.INVISIBLE);
////                failed.setVisibility(View.VISIBLE);
////            }
////        });
//        String query = "SELECT * FROM gcm_master_kurs LIMIT 1;";
//        String encryptedQuery = null;
//        try {
//            encryptedQuery = QueryEncryption.Encrypt(query);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        JSONRequest jsonRequest = new JSONRequest(encryptedQuery);
//        Call<JsonObject> kursCall = RetrofitClient
//                .getInstance()
//                .getApi()
//                .request(jsonRequest);
//        kursCall.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                if (response.isSuccessful()) {
//                    String status = response.body().getAsJsonObject().get("status").getAsString();
//                    if (status.equals("success")) {
//                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
//                        kursIdr = jsonArray.get(0).getAsJsonObject().get("nominal").getAsFloat();
//                    }
//                }
//                barangRequest();
//                Log.d("kursnyaaa", "onResponse: "+kursIdr);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d("", "onFailure: "+t.getMessage());
//                scroll.setVisibility(View.INVISIBLE);
//                failed.setVisibility(View.VISIBLE);
//            }
//        });
//    }

    private void checkReceived(){
        String query = "select string_agg(distinct(''''||e.id_transaction||'''') , ',') as id_transaction " +
                "from  gcm_master_company gmc ,gcm_transaction_detail a inner join " +
                "gcm_list_barang b on a.barang_id=b.id " +
                "inner join gcm_master_transaction e on e.id_transaction = a.transaction_id " +
                "inner join gcm_limit_complain f on b.company_id = f.company_id " +
                "where gmc.id = b.company_id and e.status = 'RECEIVED' " +
                "and now() > e.date_received + ( f.limit_hari || ' days')::interval";
        try {
            Call<JsonObject> callReceived = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callReceived.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            if (jsonArray.get(0).getAsJsonObject().get("id_transaction").isJsonNull()){
                                String idTransaksi = "tidak ada barang";
                                Log.d("ido", "barang diterima: "+idTransaksi);
                            }else{
                                String idTransaksi = jsonArray.get(0).getAsJsonObject().get("id_transaction").getAsString();
                                updateFinished(idTransaksi);
                            }
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

    private void updateFinished(String idTransaksi){
        String query = "update gcm_master_transaction set status  = 'FINISHED', date_finished  = now() where id_transaction in ("+idTransaksi+") ";
        Log.d("ido", "onResponse: "+query);
        try {
            Call<JsonObject> callUpdate = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callUpdate.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d("ido", "UPDATE FINISHED: Berhasil!!!");
                    }else{
                        Log.d("ido", "UPDATE FINISHED: Gagal!!!");
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

    private void checkCancel(){
        String query = "select string_agg(''''||a.id_transaction||''''  , ',') as id_transaction, string_agg(''||a.id_transaction||''  , ', ') as id_transaction_edit, " +
                "count (a.id_transaction) as jumlah from gcm_master_transaction a " +
                "inner join gcm_payment_listing b on a.payment_id = b.id " +
                "inner join gcm_seller_payment_listing c on b.payment_id = c.id " +
                "inner join gcm_master_payment d on c.payment_id = d.id " +
                "where a.status = 'WAITING' and a.company_id = "+SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+" " +
                "and now() > a.create_date + interval '48 hours' and d.id = 2 and a.status_payment = 'UNPAID' and a.bukti_bayar is null and a.tanggal_bayar is null " +
                "and a.id_list_bank is null and a.pemilik_rekening is null  order by id_transaction";
        Log.d("ido", "checkCancel: "+query);
        try {
            Call<JsonObject> callCheckCancel = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCheckCancel.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            if (jsonArray.get(0).getAsJsonObject().get("id_transaction").isJsonNull()){
                                Log.d("ido", "cancel: Tidak ada barang");
                            }else{
                                Log.d("ido", "cancel: "+jsonArray.get(0).getAsJsonObject().get("id_transaction").getAsString());
                                String idTransaksi = jsonArray.get(0).getAsJsonObject().get("id_transaction").getAsString();
                                updateCancel(idTransaksi);
                            }
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

    private void updateCancel(String idTransaksi){
        String query = "update gcm_master_transaction set status ='CANCELED', id_cancel_reason = 2, " +
                "date_canceled = create_date + interval '2 days', cancel_reason = 'melewati batas waktu pembayaran' " +
                "where id_transaction in ("+idTransaksi+")";
        Log.d("ido", "updateCancel: "+query);
        try {
            Call<JsonObject> callCancelTransaction = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCancelTransaction.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d("ido", "updateCancel: Update Cancel Berhasil");
                    }else{
                        Log.d("ido", "updateCancel: Update Cancel Gagal");
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

    private void statusRequestKevin(){
        String query = "";
        if(SharedPrefManager.getInstance(getContext()).isLoggedin()){
            query = "select string_agg(distinct cast(gcl.seller_id as varchar), ',') as seller FROM gcm_master_company gmc, gcm_company_listing gcl " +
                    "where gcl.seller_id = gmc.id and gcl.buyer_id = "+SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+" and gmc.seller_status = 'A' and gcl.status = 'A'";
            try {
                Call<JsonObject> statusCall = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));

                statusCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                strSellerStatusKevin = jsonArray.get(0).getAsJsonObject().get("seller").getAsString();
                                SharedPrefManager
                                        .getInstance(getContext())
                                        .saveActiveSeller(strSellerStatusKevin);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            query = "select string_agg(distinct cast(gcl.seller_id as varchar), ',') as seller FROM gcm_master_company gmc ,gcm_company_listing gcl where gcl.seller_id = gmc.id and gmc.seller_status = 'A'";
            try {
                Call<JsonObject> statusCall = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));

                statusCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                strSellerStatusKevin = jsonArray.get(0).getAsJsonObject().get("seller").getAsString();
                                SharedPrefManager
                                        .getInstance(getContext())
                                        .saveActiveSeller(strSellerStatusKevin);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
