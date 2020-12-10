package com.enseval.gcmuser.Activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.BarangAdapter;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Kategori;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogActivity extends AppCompatActivity {

    private ArrayList<Barang> listBarang;
    private RecyclerView rvBarang;
    private BarangAdapter barangAdapter;
    private ImageView cart, backBtn;
    private Kategori kategori;
    private ShimmerFrameLayout shimmerFrameLayout;
    private TextView tvKategori, tvJml;
    private ConstraintLayout noItem;
    private String tipe, strSellerStatusKevin;
    private EditText etSearch;
    private long lastClickTime=0;
    //private static float kursIdr;
    private NestedScrollView scroll;
    private ConstraintLayout failed;
    private Button refresh;
    ArrayMap<Integer, String> listSellerStatus = new ArrayMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        listBarang = new ArrayList<>();
        tipe = getIntent().getStringExtra("tipe"); //mendapatkan tipe intent dari Activity lain

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        rvBarang = findViewById(R.id.rvBarang);
        backBtn = findViewById(R.id.backBtn);
        cart = findViewById(R.id.cart);
        tvKategori = findViewById(R.id.tvKategori);
        tvJml = findViewById(R.id.result);
        noItem = findViewById(R.id.noItem);
        etSearch = findViewById(R.id.etSearch);
        scroll = findViewById(R.id.scroll);
        failed = findViewById(R.id.failed);
        refresh = findViewById(R.id.refresh);

        scroll.setVisibility(View.VISIBLE);
        failed.setVisibility(View.INVISIBLE);

        tvJml.setVisibility(View.GONE);
        noItem.setVisibility(View.INVISIBLE);
        tvKategori.setVisibility(View.INVISIBLE);

        //kursRequest(); //ambil kurs saat ini

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //jika cart ditekan, pindah ke CartActivity
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(CatalogActivity.this, CartActivity.class);
                    startActivity(intent);
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });

        //tombol utk refresh jika loading halaman gagal
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

        if(tipe.equals("search")){
            tvKategori.setVisibility(View.GONE);
            String keyword = getIntent().getStringExtra("keyword");
            if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()) {
                if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis() == 1) {
//                    barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' " +
//                            "and lower(a.nama) like '%"+keyword.toLowerCase()+"%' " +
//                            "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and now() between e.tgl_start and e.tgl_end order by nama asc;");
                    barangRequest("select * from ( " +
                            "(SELECT a.nama, b.barang_id, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                            "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and lower(a.nama) like '%"+keyword.toLowerCase()+"%' and category_id != 5 and now() between e.tgl_start and e.tgl_end) " +
                            "union all " +
                            "(SELECT distinct on (b.barang_id) a.nama, b.barang_id, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and lower(a.nama) like '%"+keyword.toLowerCase()+"%' and d.id = a.satuan and e.company_id = b.company_id and b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") " +
                            "and category_id = 5 and now() between e.tgl_start and e.tgl_end order by b.barang_id, price desc ) " +
                            ") as produk order by produk.nama asc");
                }else {
//                    barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' " +
//                            "and lower(a.nama) like '%"+keyword.toLowerCase()+"%' " +
//                            "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id!= 1 and now() between e.tgl_start and e.tgl_end order by nama asc;");
                    barangRequest("select * from (" +
                            "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                            "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and lower(a.nama) like '%"+keyword.toLowerCase()+"%' and category_id not in (1,5)  and now() between e.tgl_start and e.tgl_end " +
                            "union all " +
                            "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                            "and b.company_id = c.id and and lower(a.nama) like '%"+keyword.toLowerCase()+"%' d.id = a.satuan and e.company_id = b.company_id and " +
                            "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id = 5 and b.departmen_sales = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+"" +
                            " and now() between e.tgl_start and e.tgl_end " +
                            ") as produk order by produk.nama asc");
                }
            }
//            else {
//                barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and lower(nama) like '%"+keyword.toLowerCase()+"%' " +
//                        "and now() between e.tgl_start and e.tgl_end order by nama asc;");
//            }
            etSearch.setText(keyword);
            etSearch.setSelection(0);
            etSearch.addTextChangedListener(handleSearch);
        }
        //jika tipenya kategori, maka dilakukan request barang berdasarkan kategorinya saja tanpa keyword khusus
        else if(tipe.equals("kategori")){
            kategori = (Kategori) getIntent().getSerializableExtra("kategori");
            etSearch.setHint(kategori.getNama());
            tvKategori.setText(kategori.getNama());
            tvKategori.setVisibility(View.VISIBLE);
            if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()) {
                if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis() == 1) {
                    if (kategori.getId() == 5){
                        barangRequest("SELECT distinct on (b.barang_id) a.nama, b.barang_id, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") " +
                                "and category_id = 5 and now() between e.tgl_start and e.tgl_end order by b.barang_id, price desc ");
                    }else{
                        barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
                                "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id " +
                                "inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and category_id="+kategori.getId()+" and b.company_id in(" +
                                SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and now() between e.tgl_start and e.tgl_end order by nama asc;");
                    }
                } else {
                    if (kategori.getId() == 5){
                        barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                                "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id = 5 and b.departmen_sales = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+"" +
                                " and now() between e.tgl_start and e.tgl_end");
                    }else{
                        barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
                                "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id " +
                                "inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and category_id="+kategori.getId()+" and b.company_id in(" +
                                SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id!= 1 and now() between e.tgl_start and e.tgl_end order by nama asc;");
                    }
                }
            }else {
                barangRequest("SELECT x.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and category_id="+kategori.getId()+
                        "and now() between e.tgl_start and e.tgl_end order by nama asc;");
            }
            etSearch.addTextChangedListener(handleSearch);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmerAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmerAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();
        shimmerFrameLayout.startShimmerAnimation();
    }

    //TextWatcher untuk menghandle tiap ada perubahan isi pada searchbar
    TextWatcher handleSearch = new TextWatcher() {
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
            //kondisi pada 0,7 detik setelah user selesai mengetik (delay 700 ms), dilanjutkan dengan request barang dengan parameter search terbaru
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(tipe.equals("search")){
                        if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()) {
                            if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis() == 1) {
//                                barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' and " +
//                                        "lower(nama) like '%"+s.toString()+"%'" +
//                                        "and b.company_id in("+strSellerStatusKevin+") "+
//                                        "order by nama asc;");
//                                barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and " +
//                                        "lower(a.nama) like '%"+s.toString()+"%'" +
//                                        "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") "+
//                                        "and now() between e.tgl_start and e.tgl_end order by nama asc;");
                                barangRequest("select * from ( " +
                                        "(SELECT a.nama, b.barang_id, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                        "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                        "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                        "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                        "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                        "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                                        "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and lower(a.nama) like '%"+s.toString().toLowerCase()+"%' and category_id != 5 and now() between e.tgl_start and e.tgl_end) " +
                                        "union all " +
                                        "(SELECT distinct on (b.barang_id) a.nama, b.barang_id, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                        "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                        "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                        "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                        "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                        "and b.company_id = c.id and lower(a.nama) like '%"+s.toString().toLowerCase()+"%' and d.id = a.satuan and e.company_id = b.company_id and b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") " +
                                        "and category_id = 5 and now() between e.tgl_start and e.tgl_end order by b.barang_id, price desc ) " +
                                        ") as produk order by produk.nama asc");
                            } else {
//                                barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
//                                    "inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' and " +
//                                    "lower(nama) like '%"+s.toString()+"%'" +
//                                    "and b.company_id in("+strSellerStatusKevin+")"+
//                                    "and category_id="+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+" order by nama asc;");
//                                barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and " +
//                                        "lower(a.nama) like '%"+s.toString()+"%'" +
//                                        "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+")"+
//                                        "and category_id!= 1 and now() between e.tgl_start and e.tgl_end order by nama asc;");
                                barangRequest("select * from (" +
                                        "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                        "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                        "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                        "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                        "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                        "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                                        "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and lower(a.nama) like '%"+s.toString().toLowerCase()+"%' and category_id not in (1,5)  and now() between e.tgl_start and e.tgl_end " +
                                        "union all " +
                                        "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                        "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                        "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                        "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                        "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                        "and b.company_id = c.id and lower(a.nama) like '%"+s.toString().toLowerCase()+"%' and d.id = a.satuan and e.company_id = b.company_id and " +
                                        "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id = 5 and b.departmen_sales = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+"" +
                                        " and now() between e.tgl_start and e.tgl_end " +
                                        ") as produk order by produk.nama asc");
                            }
                        }
//                        else {
//                            barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs"+
//                                    " FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id"+
//                                    " where a.status='A' and b.status='A' and lower(a.nama) like '%"+s.toString()+"%' and now() between e.tgl_start and e.tgl_end order by nama asc;");
//                        }
                    }
                    else if(tipe.equals("kategori")){
                        if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()) {
                            if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis() == 1) {
//                                barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' " +
//                                        "and lower(nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" " +
//                                        "and b.company_id in("+strSellerStatusKevin+") "+
//                                        "order by nama asc;");
//                                barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id = b.company_id where a.status='A' and b.status='A' " +
//                                        "and lower(a.nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" " +
//                                        "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and now() between e.tgl_start and e.tgl_end "+
//                                        "order by nama asc;");
                                if (kategori.getId() == 5){
                                    barangRequest("SELECT distinct on (b.barang_id) a.nama, b.barang_id, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") " +
                                            "and lower(a.nama) like '%"+s.toString()+"%'and category_id = 5 and now() between e.tgl_start and e.tgl_end order by b.barang_id, price desc ");
                                }else{
                                    barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
                                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id " +
                                            "inner join  gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where a.status='A' and b.status='A' and lower(a.nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" and b.company_id in(" +
                                            SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and now() between e.tgl_start and e.tgl_end order by nama asc;");
                                }
                            } else {
//                                barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
//                                    "inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' " +
//                                    "and lower(nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" " +
//                                    "and b.company_id in("+strSellerStatusKevin+")"+
//                                    "and category_id="+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+" "+
//                                    "order by nama asc;");
//                                barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id = b.company_id where a.status='A' and b.status='A' " +
//                                        "and lower(a.nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" " +
//                                        "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+")"+
//                                        "and category_id!= 1 "+
//                                        "and now() between e.tgl_start and e.tgl_end order by nama asc;");
                                if (kategori.getId() == 5){
                                    barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, " +
                                            "case when price = price_terendah then 'no' else 'yes' end as negotiable, " +
                                            "foto, flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                                            "FROM gcm_listing_kurs e, gcm_master_satuan d, gcm_master_company c, gcm_master_barang a " +
                                            "inner join gcm_list_barang b on a.id=b.barang_id where a.status='A' and b.status='A' " +
                                            "and b.company_id = c.id and d.id = a.satuan and e.company_id = b.company_id and " +
                                            "b.company_id in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id = 5 and b.departmen_sales = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+"" +
                                            " and lower(a.nama) like '%"+s.toString()+"%' and now() between e.tgl_start and e.tgl_end");
                                }else{
                                    barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs FROM gcm_master_barang a " +
                                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id = b.company_id where a.status='A' and b.status='A' " +
                                            "and lower(a.nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" " +
                                            "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+")"+
                                            "and category_id!= 1 "+
                                            "and now() between e.tgl_start and e.tgl_end order by nama asc;");
                                }
                            }
                        }
//                        else {
//                            barangRequest("SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs"+
//                                    " FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id" +
//                                    " where a.status='A' and b.status='A' and lower(a.nama) like '%"+s.toString()+"%' and category_id="+kategori.getId()+" and now() between e.tgl_start and e.tgl_end order by nama asc;");
//                        }
                    }
                }
            };
            handler.postDelayed(runnable, 700);
        }
    };

    /**Method untuk request barang*/
    private void barangRequest(final String query) {
        try {
            JSONRequest jsonRequest = new JSONRequest(QueryEncryption.Encrypt(query));
            Log.d("april", query);
            listBarang.clear();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmerAnimation();
            tvJml.setVisibility(View.GONE);

            Call<JsonObject> barangCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(jsonRequest);

            barangCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d("ido", "onResponse: "+query);
                            noItem.setVisibility(View.INVISIBLE);
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0;i<jsonArray.size();i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                Log.d("", "nama barang: "+jsonObject.get("nama").getAsString());
                                listBarang.add(new Barang(
                                        jsonObject.get("nama").getAsString(),
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("price").getAsDouble(),
                                        jsonObject.get("price_terendah").getAsDouble(),
                                        jsonObject.get("foto").getAsString(),
                                        jsonObject.get("category_id").getAsInt(),
                                        jsonObject.get("company_id").getAsInt(),
                                        jsonObject.get("nama_perusahaan").getAsString(),
                                        jsonObject.get("alias").getAsString(),
                                        jsonObject.get("persen_nego_1").getAsFloat(),
                                        jsonObject.get("persen_nego_2").getAsFloat(),
                                        jsonObject.get("persen_nego_3").getAsFloat(),
                                        jsonObject.get("kurs").getAsFloat(),
                                        jsonObject.get("kode_barang").getAsString(),
                                        jsonObject.get("flag_foto").getAsString()
                                ));
                            }
                            statusRequest(listBarang); //check status user dulu
                            Log.d("ido", "onResponse: ga ada data");
                        }
                        else if(status.equals("error")){
                            shimmerFrameLayout.stopShimmerAnimation();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            noItem.setVisibility(View.VISIBLE);
                            listBarang.clear();
                            Log.d("ido", "onResponse: gagal total");
//                            barangAdapter.notifyDataSetChanged();
                        }
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    scroll.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk request status user pada tiap seller*/
    private void statusRequest(final ArrayList<Barang> listBarang){
        try {
            if(SharedPrefManager.getInstance(CatalogActivity.this).isLoggedin()){
                Call<JsonObject> statusCall = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt("SELECT seller_id, status FROM gcm_company_listing where buyer_id="+
                                SharedPrefManager.getInstance(CatalogActivity.this).getUser().getCompanyId())));

                statusCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                //masukkan respon berupa seller id dan statusnya ke dalam array map
                                for(int i=0; i<jsonArray.size(); i++){
                                    listSellerStatus.put(
                                            jsonArray.get(i).getAsJsonObject().get("seller_id").getAsInt(),
                                            jsonArray.get(i).getAsJsonObject().get("status").getAsString()
                                    );
                                }
                                shimmerFrameLayout.stopShimmerAnimation();
                                shimmerFrameLayout.setVisibility(View.GONE);
                                RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(CatalogActivity.this,2);
                                rvBarang.setLayoutManager(layoutManager1);
                                rvBarang.setItemAnimator(new DefaultItemAnimator());
                                barangAdapter = new BarangAdapter(getApplicationContext(), listBarang, listSellerStatus); //buat adapter utk barang dengan parameter status juga
                                rvBarang.setAdapter(barangAdapter);
                            tvJml.setText(listBarang.size()+" barang ditemukan"); //tampilkan jumlah barang yang didapatkan
                            tvJml.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            }
            else{
                listSellerStatus = new ArrayMap<>();
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(CatalogActivity.this,2);
                rvBarang.setLayoutManager(layoutManager1);
                rvBarang.setItemAnimator(new DefaultItemAnimator());
                barangAdapter = new BarangAdapter(CatalogActivity.this, listBarang, listSellerStatus);
                rvBarang.setAdapter(barangAdapter);
                tvJml.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    /**Method untuk request kurs hari ini*/
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
////                kursIdr = jsonObject.get("IDR").getAsFloat();
//                //setelah didapatkan kurs, dicek tipe intent apakah search atau kategori.
//                //jika tipenya search, maka dilakukan request barang berdasarkan keyword saja tanpa parameter kategori
////                if(tipe.equals("search")){
////                    tvKategori.setVisibility(View.GONE);
////                    String keyword = getIntent().getStringExtra("keyword");
////                    barangRequest("SELECT nama, b.id, price, foto, category_id, b.company_id FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' and lower(nama) like '%"+keyword.toLowerCase()+"%' order by nama asc;");
////                    etSearch.setText(keyword);
////                    etSearch.setSelection(0);
////                    etSearch.addTextChangedListener(handleSearch);
////                }
////                //jika tipenya kategori, maka dilakukan request barang berdasarkan kategorinya saja tanpa keyword khusus
////                else if(tipe.equals("kategori")){
////                    kategori = (Kategori) getIntent().getSerializableExtra("kategori");
////                    etSearch.setHint(kategori.getNama());
////                    tvKategori.setText(kategori.getNama());
////                    tvKategori.setVisibility(View.VISIBLE);
////                    barangRequest("SELECT nama, b.id, price, foto, category_id, b.company_id FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' and category_id="+kategori.getId()+" order by nama asc;");
////                    etSearch.addTextChangedListener(handleSearch);
////                }
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
//                        //setelah didapatkan kurs, dicek tipe intent apakah search atau kategori.
//                        //jika tipenya search, maka dilakukan request barang berdasarkan keyword saja tanpa parameter kategori
//                        if(tipe.equals("search")){
//                            tvKategori.setVisibility(View.GONE);
//                            String keyword = getIntent().getStringExtra("keyword");
//                            if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()) {
//                                if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis() == 1) {
////                                    barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
////                                            "inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' " +
////                                            "and lower(nama) like '%"+keyword.toLowerCase()+"%' " +
////                                            "and b.company_id in("+strSellerStatusKevin+") order by nama asc;");
//                                    barangRequest("SELECT a.nama, b.id, price, price_terendah, foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 FROM gcm_master_barang a " +
//                                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id where b.status='A' " +
//                                            "and lower(a.nama) like '%"+keyword.toLowerCase()+"%' " +
//                                            "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") order by nama asc;");
//                                } else {
////                                    barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
////                                        "inner join gcm_list_barang b on a.id=b.barang_id where b.status='A' " +
////                                        "and lower(nama) like '%"+keyword.toLowerCase()+"%' " +
////                                        "and b.company_id in("+strSellerStatusKevin+") and category_id="+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+" order by nama asc;");
//                                    barangRequest("SELECT a.nama, b.id, price, price_terendah, foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 FROM gcm_master_barang a " +
//                                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id where b.status='A' " +
//                                            "and lower(a.nama) like '%"+keyword.toLowerCase()+"%' " +
//                                            "and b.company_id in("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id="+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+" order by nama asc;");
//
//                                }
//                            } else {
//                                barangRequest("SELECT a.nama, b.id, price, price_terendah, foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id where b.status='A' and lower(nama) like '%"+keyword.toLowerCase()+"%' " +
//                                        " order by nama asc;");
//                            }
//                            etSearch.setText(keyword);
//                            etSearch.setSelection(0);
//                            etSearch.addTextChangedListener(handleSearch);
//                        }
//                        //jika tipenya kategori, maka dilakukan request barang berdasarkan kategorinya saja tanpa keyword khusus
//                        else if(tipe.equals("kategori")){
//                            kategori = (Kategori) getIntent().getSerializableExtra("kategori");
//                            etSearch.setHint(kategori.getNama());
//                            tvKategori.setText(kategori.getNama());
//                            tvKategori.setVisibility(View.VISIBLE);
//                            if (SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()) {
//                                if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis() == 1) {
////                                    barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
////                                            "inner join gcm_list_barang b on a.id=b.barang_id " +
////                                            "where b.status='A' and category_id="+kategori.getId()+" and b.company_id in(" +
////                                            strSellerStatusKevin+") order by nama asc;");
//                                    barangRequest("SELECT a.nama, b.id, price, price_terendah, foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 FROM gcm_master_barang a " +
//                                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id " +
//                                            "inner join  gcm_master_satuan d on a.satuan=d.id where b.status='A' and category_id="+kategori.getId()+" and b.company_id in(" +
//                                            SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") order by nama asc;");
//                                } else {
////                                    barangRequest("SELECT nama, b.id, price, price_terendah, foto, category_id, b.company_id FROM gcm_master_barang a " +
////                                        "inner join gcm_list_barang b on a.id=b.barang_id " +
////                                        "where b.status='A' and category_id="+kategori.getId()+" and b.company_id in(" +
////                                        strSellerStatusKevin+") and category_id="+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+" order by nama asc;");
//                                    barangRequest("SELECT a.nama, b.id, price, price_terendah, foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 FROM gcm_master_barang a " +
//                                            "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id " +
//                                            "inner join  gcm_master_satuan d on a.satuan=d.id where b.status='A' and category_id="+kategori.getId()+" and b.company_id in(" +
//                                            SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and category_id="+SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()+" order by nama asc;");
//                                }
//                            } else {
//                                barangRequest("SELECT a.nama, b.id, price, price_terendah, foto, category_id, b.company_id, c.nama_perusahaan, d.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 FROM gcm_master_barang a " +
//                                        "inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join  gcm_master_satuan d on a.satuan=d.id where b.status='A' and category_id="+kategori.getId()+
//                                        " order by nama asc;");
//                            }
//                            etSearch.addTextChangedListener(handleSearch);
//                        }
//                    }
//                }
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

}
