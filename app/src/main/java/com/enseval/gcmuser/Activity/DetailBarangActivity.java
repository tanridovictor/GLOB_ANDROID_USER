package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.Response.BarangResponse;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.math.BigInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailBarangActivity extends AppCompatActivity {

    private ImageView appbarimg, imgBtnCart, backBtn;
    //    private ImageButton backBtn;
    private Button beliBtn;
    private CardView addToCartBtn, chatBtn;
    private TextView tvNamaBarang, tvHarga, notEligible, login, tvDeskripsi, tvDetailInformation;
    private int idBarang, tipe_bisnis, idListBr;
    private ShimmerFrameLayout shimmerFrameLayout, shimmerFrameLayout2, shimmerFrameLayout3, shimmerFrameLayoutNego, shimmerFrameLayoutBottom;
    private CardView nego, prosesNego;
    private Barang barang;
    private String company_status;
    private String user_status;
    private int hargaNego;
    private long lastClickTime=0;
    private static float kursIdr;
    private float persenNego1, persenNego2, persenNego3, harga, hargaRendah;
    private int maxNego;
    private String checkNego;

    private String valueBerat;
    private float valueMinBeli;
    private float valueMinNego;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_barang);

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout2 = findViewById(R.id.shimmer_view_container2);
        shimmerFrameLayout3 = findViewById(R.id.shimmer_view_container3);
        shimmerFrameLayoutNego = findViewById(R.id.shimmer_view_container_nego);
        shimmerFrameLayoutBottom = findViewById(R.id.shimmer_view_container_bottom);

        appbarimg = findViewById(R.id.app_bar_image);
        tvNamaBarang = findViewById(R.id.tvNamaBarang);
        backBtn = findViewById(R.id.backBtn);
        addToCartBtn = findViewById(R.id.btnAddCart);
        chatBtn = findViewById(R.id.btnChat);
        beliBtn = findViewById(R.id.btnBuy);
        imgBtnCart = findViewById(R.id.imgBtnCart);
        tvHarga = findViewById(R.id.tvHarga);
        notEligible = findViewById(R.id.notEligible);
        nego = findViewById(R.id.cardNego);
        prosesNego = findViewById(R.id.cardProsesNego);
        login = findViewById(R.id.login);
        tvDeskripsi = findViewById(R.id.tvDeskripsi);
        tvDetailInformation = findViewById(R.id.tvDetailInformation);

        appbarimg.setVisibility(View.INVISIBLE);
        beliBtn.setVisibility(View.INVISIBLE);
        addToCartBtn.setVisibility(View.INVISIBLE);
        chatBtn.setVisibility(View.INVISIBLE);
        nego.setVisibility(View.INVISIBLE);
        prosesNego.setVisibility(View.INVISIBLE);
        notEligible.setVisibility(View.INVISIBLE);
        login.setVisibility(View.GONE);

        tipe_bisnis = SharedPrefManager.getInstance(this).getUser().getTipeBisnis(); //tipe bisnis user

        idBarang = getIntent().getIntExtra("id",-1); //ambil parameter intent berupa id barang
        //kursIdr =  getIntent().getFloatExtra("kursIdr",0);

        checkNego = getIntent().getStringExtra("checkNego");
        Log.d("ido", "cek nego: "+checkNego);
        if (checkNego.equals("tidakNego")){
            nego.setVisibility(View.INVISIBLE);
            shimmerFrameLayoutNego.setVisibility(View.GONE);

        }else{
            nego.setVisibility(View.VISIBLE);
            shimmerFrameLayoutNego.setVisibility(View.VISIBLE);
        }

        //kursRequest(); //request kurs saat ini
        barangRequest();
        cekMaxNego();
        checkNegoAuto();

        Log.d("ido", "onCreate: nilai kurs "+kursIdr);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //jika add to cart ditekan maka intent ke DetailPemesananActivity
        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(DetailBarangActivity.this, DetailPemesananActivity.class);
                    intent.putExtra("tipe", "cart"); //tipe intentnya adalah cart (karena add to cart)
                    intent.putExtra("barang", (Serializable) barang);
                    intent.putExtra("kurs", kursIdr);
                    intent.putExtra("berat", valueBerat);
                    intent.putExtra("jumlah_min_beli", valueMinBeli);
                    startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
        //jika add to cart ditekan maka intent ke DetailPemesananActivity
        beliBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(DetailBarangActivity.this, DetailPemesananActivity.class);
                    intent.putExtra("tipe", "buy"); //tipe intentnya adalah buy (karena beli)
                    intent.putExtra("barang", (Serializable) barang);
                    intent.putExtra("kurs", kursIdr);
                    intent.putExtra("berat", valueBerat);
                    intent.putExtra("jumlah_min_beli", valueMinBeli);
                    startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
        //note: perbedaan add to cart dan beli adalah:
        //jika add to cart, setelah barang dimasukkan ke cart maka akan langsung dikembalikan ke home untuk berbelanja lagi
        //jika beli, setelah barang dimasukkan ke cart maka akan langsung diarahkan ke cart.

        //jika button cart ditekan maka akan dipindahkan ke CartActivity
        imgBtnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(DetailBarangActivity.this, CartActivity.class);
                    startActivity(intent);
                    finish();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        //jika button nego ditekan maka akan dimunculkan dialog untuk nego
        nego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    if(maxNego>=2){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.maxNego), Toast.LENGTH_LONG).show();
                    }else {
                        negoDialog();
                        checkNegoAuto();
                        checkStatusCart();
                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        //jika button nego ditekan maka akan dipindahkan ke ChatActivity dengan membawa beberapa parameter
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(DetailBarangActivity.this, ChatActivity.class);
                    intent.putExtra("from", "barang"); //sebagai penanda untuk ChatActivity bahwa chat dilakukan karena ada pertanyaan seputar barang
                    intent.putExtra("companyId", barang.getCompanyId());
                    intent.putExtra("id", barang.getId());
                    intent.putExtra("img", barang.getFoto());
                    intent.putExtra("nama", barang.getNama());
                    startActivity(intent);
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });

        //jika belum login maka akan ada tombol login yang akan mengarahkan user ke halaman login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(DetailBarangActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout2.stopShimmerAnimation();
        shimmerFrameLayout3.stopShimmerAnimation();
        shimmerFrameLayoutNego.stopShimmerAnimation();
        shimmerFrameLayoutBottom.stopShimmerAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmerAnimation();
        shimmerFrameLayout2.startShimmerAnimation();
        shimmerFrameLayout3.startShimmerAnimation();
        shimmerFrameLayoutNego.startShimmerAnimation();
        shimmerFrameLayoutBottom.startShimmerAnimation();
//        kursRequest();
    }

    /**Merupakan method untuk menampilkan dialog negosiasi*/
    private void negoDialog(){
        final Dialog dialog = new Dialog(DetailBarangActivity.this);
        dialog.setContentView(R.layout.nego_dialog_awal);
        TextView namaBarang = dialog.findViewById(R.id.tvNama);
        TextView hargaBarang = dialog.findViewById(R.id.tvHarga);
        ImageView jatahTerpakai1 = dialog.findViewById(R.id.jatahTerpakai1);
        ImageView jatahTerpakai2 = dialog.findViewById(R.id.jatahTerpakai2);
        ImageView jatahTerpakai3 = dialog.findViewById(R.id.jatahTerpakai3);
        jatahTerpakai1.setVisibility(View.GONE);
        jatahTerpakai2.setVisibility(View.GONE);
        jatahTerpakai3.setVisibility(View.GONE);

        final TextInputLayout inputHargaNego = dialog.findViewById(R.id.hargaNego);
        final Button btnNego = dialog.findViewById(R.id.btnNego);

        btnNego.setEnabled(false); //pada kondisi awal button nego masih disabled (harga blm dimasukkan)

        int harga = (int) (barang.getHarga()*kursIdr);
        Log.d("ido", "negoDialog: harga "+harga);
//        long harga_sales_satuan = (long) harga / Long.parseLong(barang.getBerat());

        namaBarang.setText(tvNamaBarang.getText().toString());
//        hargaBarang.setText("Harga negosiasi satuan " + Currency.getCurrencyFormat().format(harga_sales_satuan));
        hargaBarang.setText("Harga Barang : " + Currency.getCurrencyFormat().format(harga) + "/" +barang.getAlias());

        //text watcher untuk input harga negosiasi
        inputHargaNego.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //handle input harga negosiasi yang dimasukkan user
                if(!TextUtils.isEmpty(inputHargaNego.getEditText().getText())){
                    BigInteger bigInteger = new BigInteger(inputHargaNego.getEditText().getText().toString());
                    int harga = (int) (barang.getHarga()*kursIdr);
                    Log.d("ido", "onTextChanged: "+harga);
                    //jika harga nego lebih besar dari harga awal akan diberikan peringatan
                    if(bigInteger.compareTo(BigInteger.valueOf(harga))==1){
                        inputHargaNego.setErrorEnabled(true);
                        inputHargaNego.setError("Harga nego melebihi harga barang");
                        btnNego.setEnabled(false);
                    }
                    else{
                        inputHargaNego.setErrorEnabled(false);
                        btnNego.setEnabled(true); //button bisa ditekan jika isi harga nego sudah tidak bermasalah
                    }
                }
                else{
                    btnNego.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //jika button nego ditekan maka akan dipindahkan ke DetailPemesananActivity dengan membawa parameter harga nego juga
        btnNego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(DetailBarangActivity.this, DetailPemesananActivity.class);
                    intent.putExtra("tipe", "nego");
                    intent.putExtra("barang", (Serializable) barang);
                    intent.putExtra("hargaNego", Integer.parseInt(inputHargaNego.getEditText().getText().toString()));
                    intent.putExtra("kurs", kursIdr);
                    intent.putExtra("berat", valueBerat);
                    intent.putExtra("jumlah_min_nego", valueMinNego);
                    Log.d("nego", "onClick: "+valueMinNego);
                    startActivity(intent);
                    checkNegoAuto();
                    checkStatusCart();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        dialog.show();
    }



    /**Method untuk request barang*/
    private void barangRequest() {
        String query = "SELECT a.nama, b.id, b.kode_barang, c.kode_seller, price, price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', c.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, category_id, berat, volume, ex, deskripsi, b.company_id, c.nama_perusahaan, d.alias, b.jumlah_min_nego, b.jumlah_min_beli, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, e.nominal as kurs " +
                "FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id inner join gcm_master_company c on b.company_id=c.id inner join gcm_master_satuan d on a.satuan=d.id inner join gcm_listing_kurs e on e.company_id=b.company_id where b.id="+idBarang+" and now() between e.tgl_start and e.tgl_end;";
        Log.d("ido", "barangRequest: "+query);
        try {
            final Call<BarangResponse> barangCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .requestBarang(new JSONRequest(QueryEncryption.Encrypt(query)));

            barangCall.enqueue(new Callback<BarangResponse>() {
                @Override
                public void onResponse(Call<BarangResponse> call, Response<BarangResponse> response) {
                    if(response.isSuccessful()){
                        barang = response.body().getData().get(0);
                        valueBerat = barang.getBerat();
                        valueMinNego = barang.getMinNego();
                        valueMinBeli = barang.getMinBeli();
                        kursIdr = barang.getKursIdr();
                        Log.d("companyId", "onResponse: "+barang.getCompanyId());
                        Log.d("berat", "onResponse: "+barang.getBerat());
                        Log.d("Nego", "onResponse: "+barang.getMinNego());
                        Log.d("ido", "min Beli: "+barang.getMinBeli());
                        Log.d("ido", "onResponse: "+barang.getKursIdr());
//                        Glide.with(getApplicationContext())
//                                .load("https://www.glob.co.id/admin/assets/images/product/"+barang.getKode_seller()+"/"+barang.getKode_barang()+".png")
//                                .fallback(R.id.shimmer_view_container)
//                                .error(R.id.shimmer_view_container)
//                                .into(appbarimg);
                        Log.d("ido", "onResponse: "+barang.getFoto());
                        if (barang.getFlag_foto().equals("Y")) {
                            Glide.with(getApplicationContext())
                                    .load(barang.getFoto())
                                    .into(appbarimg);
                        }else{
                            Glide.with(getApplicationContext())
                                    .load("https://www.glob.co.id/admin/assets/images/no_image.png")
                                    .into(appbarimg);
                        }
                        tvNamaBarang.setText(barang.getNama());
                        tvDeskripsi.setText(
                                "Dijual oleh: "+barang.getNamaPerusahaan()+"\nGratis ongkir untuk wilayah Jabodetabek"
                        );

                        tvDetailInformation.setText("Deskripsi barang : " + barang.getDeskripsi());
                        appbarimg.setVisibility(View.VISIBLE);
                        if (SharedPrefManager.getInstance(DetailBarangActivity.this).isLoggedin()) {
                            login.setVisibility(View.GONE);
                            notEligible.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            checkStatus(barang.getCompanyId());
                        }
                        else{
                            shimmerFrameLayoutBottom.stopShimmerAnimation();
                            shimmerFrameLayoutBottom.setVisibility(View.GONE);
                            shimmerFrameLayoutNego.stopShimmerAnimation();
                            shimmerFrameLayoutNego.setVisibility(View.GONE);
                            notEligible.setVisibility(View.VISIBLE);
                            notEligible.setText("Anda harus login sebelum membeli.");
                            beliBtn.setVisibility(View.INVISIBLE);
                            addToCartBtn.setVisibility(View.INVISIBLE);
                            chatBtn.setVisibility(View.INVISIBLE);
                            nego.setVisibility(View.INVISIBLE);
                            login.setVisibility(View.VISIBLE);
                        }
                        shimmerFrameLayout.stopShimmerAnimation();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout2.stopShimmerAnimation();
                        shimmerFrameLayout2.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onFailure(Call<BarangResponse> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    barangRequest();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk cek status user pada seller penjual barang yang bersangkutan*/
    private void checkStatus(final int sellerId) {
        Log.d("", "sellerId checkStatus: "+sellerId);
        try {
            Call<JsonObject> checkStatusCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select c.status as company_status, b.status as user_status from gcm_master_company a inner join gcm_master_user b " +
                            "on a.id=b.company_id inner join gcm_company_listing c on a.id=c.buyer_id where a.id="+ SharedPrefManager.getInstance(this).getUser().getCompanyId()+" and " +
                            "b.id="+SharedPrefManager.getInstance(this).getUser().getUserId()+" and c.seller_id="+sellerId+";")));

            checkStatusCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d("", "onResponse: checkstatus");
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonObject jsonObject = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
                            company_status = jsonObject.get("company_status").getAsString();
                            user_status = jsonObject.get("user_status").getAsString();
                            //jika status A maka harga ditampilkan dan dilanjutkan dengan cek listing
                            if(company_status.equals("A") && user_status.equals("A")){
                                try {
                                    double harga = (double) Math.ceil(barang.getHarga()*kursIdr);
                                    Log.d("ido", "onResponse: harga"+barang.getHarga()+" "+barang.getKursIdr()+" "+Math.ceil(barang.getHarga()*kursIdr));
//                                    long harga_sales_satuan = (long) harga / Long.parseLong(barang.getBerat());
//                                    tvHarga.setText(Currency.getCurrencyFormat().format(harga_sales_satuan));
                                    tvHarga.setText(Currency.getCurrencyFormat().format(harga) +"/"+ barang.getAlias());
                                    Log.d("ido","tes"+barang.getAlias());
                                    tvHarga.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                                    checkListing(); //cek listing barang user
                                    checkCart();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            //jika status I maka harga tidak ditampilkan dan tidak bisa melakukan transaksi barang ini
                            else{
                                tvHarga.setText("Tidak dapat melihat harga");
                                tvHarga.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                shimmerFrameLayoutBottom.stopShimmerAnimation();
                                shimmerFrameLayoutBottom.setVisibility(View.GONE);
                                shimmerFrameLayoutNego.stopShimmerAnimation();
                                shimmerFrameLayoutNego.setVisibility(View.GONE);
                                notEligible.setVisibility(View.VISIBLE);
                                notEligible.setText("Perusahaan anda belum terverifikasi");
                                beliBtn.setVisibility(View.INVISIBLE);
                                addToCartBtn.setVisibility(View.INVISIBLE);
                                chatBtn.setVisibility(View.INVISIBLE);
                                nego.setVisibility(View.INVISIBLE);
                            }
                            shimmerFrameLayout3.stopShimmerAnimation();
                            shimmerFrameLayout3.setVisibility(View.GONE);
                        }
                        //kondisi jika belum terhubung dengan seller ini (tidak dipilih saat registrasi)
                        else{
                            shimmerFrameLayoutBottom.stopShimmerAnimation();
                            shimmerFrameLayoutBottom.setVisibility(View.GONE);
                            notEligible.setVisibility(View.VISIBLE);
                            notEligible.setText("Anda tidak terhubung dengan distributor ini");
                            beliBtn.setVisibility(View.INVISIBLE);
                            addToCartBtn.setVisibility(View.INVISIBLE);
                            chatBtn.setVisibility(View.INVISIBLE);
                            nego.setVisibility(View.INVISIBLE);
                            shimmerFrameLayout3.stopShimmerAnimation();
                            shimmerFrameLayout3.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    checkStatus(sellerId);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk cek listing barang dari user
     * cek listing dilakukan untuk mengecek barang apa saja yang dapat dibeli oleh user*/
    private void checkListing() {
        try {
            Call<JsonObject> checkStatusCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select b.id from gcm_master_company a inner join gcm_listing_detail b using (listing_id) where a.id="+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+";")));

            checkStatusCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d("", "onResponse: checklisting");
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();

                            //mekanisme pengecekan apakah barang terdapat di listing user
                            boolean isListed=false;
                            for(int i=0; i<jsonArray.size(); i++){
                                int barangId = jsonArray.get(i).getAsJsonObject().get("barang_id").getAsInt();
                                if(barangId==barang.getId()){
                                    isListed=true;
                                }
                            }

                            //jika ada, dilanjutkan dengan pengecekan cart
                            if(isListed){
                                checkCart(); //pengecekan isi cart
                            }
                            //jika tidak ada maka barang tidak bisa dibeli
                            else{
                                shimmerFrameLayoutBottom.stopShimmerAnimation();
                                shimmerFrameLayoutBottom.setVisibility(View.GONE);
                                shimmerFrameLayoutNego.stopShimmerAnimation();
                                shimmerFrameLayoutNego.setVisibility(View.GONE);
                                notEligible.setVisibility(View.VISIBLE);
                                notEligible.setText("Tidak memiliki izin untuk barang ini");
                                beliBtn.setVisibility(View.INVISIBLE);
                                addToCartBtn.setVisibility(View.INVISIBLE);
                                chatBtn.setVisibility(View.INVISIBLE);
                                nego.setVisibility(View.INVISIBLE);
                            }
                        }
                        else if(status.equals("error")){
                            shimmerFrameLayoutBottom.stopShimmerAnimation();
                            shimmerFrameLayoutBottom.setVisibility(View.GONE);
                            shimmerFrameLayoutNego.stopShimmerAnimation();
                            shimmerFrameLayoutNego.setVisibility(View.GONE);
                            notEligible.setVisibility(View.VISIBLE);
                            notEligible.setText("Tidak memiliki izin untuk barang ini");
                            beliBtn.setVisibility(View.INVISIBLE);
                            addToCartBtn.setVisibility(View.INVISIBLE);
                            chatBtn.setVisibility(View.INVISIBLE);
                            nego.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    checkListing();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk mengecek isi cart untuk mengetahui apakah barang telah dimasukkan ke dalam cart atau belum*/
    private void checkCart() {
        try {
            Call<JsonObject> checkNegoStatusCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select nego_count from gcm_master_cart where company_id="+
                            SharedPrefManager.getInstance(this).getUser().getCompanyId()+" and barang_id="+
                            idBarang+" and status='A';")));

            checkNegoStatusCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d("", "onResponse: checkcart");
                    if(response.isSuccessful()){
                        shimmerFrameLayoutNego.stopShimmerAnimation();
                        shimmerFrameLayoutNego.setVisibility(View.GONE);
                        shimmerFrameLayoutBottom.stopShimmerAnimation();
                        shimmerFrameLayoutBottom.setVisibility(View.GONE);
                        String status = response.body().getAsJsonObject().get("status").getAsString();

                        //kondisi jika barang sudah ada di dalam cart
                        if(status.equals("success")){
                            Log.d("", "onResponse: "+response.body().toString());
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int negoCount = jsonArray.get(0).getAsJsonObject().get("nego_count").getAsInt();
                            beliBtn.setVisibility(View.INVISIBLE);
                            addToCartBtn.setVisibility(View.INVISIBLE);
                            chatBtn.setVisibility(View.INVISIBLE);
                            nego.setVisibility(View.INVISIBLE);
                            notEligible.setVisibility(View.VISIBLE);
                            notEligible.setText("Barang telah dimasukkan ke dalam cart");
                            if(negoCount>0){
                                prosesNego.setVisibility(View.VISIBLE);
                            }
                            else{
                                prosesNego.setVisibility(View.INVISIBLE);
                            }
                        }
                        //kondisi jika barang belum ada di dalam cart, maka barang bisa dibeli/dimasukkan ke cart
                        else if(status.equals("error")){
                            notEligible.setVisibility(View.INVISIBLE);
                            beliBtn.setVisibility(View.VISIBLE);
                            addToCartBtn.setVisibility(View.VISIBLE);
                            chatBtn.setVisibility(View.VISIBLE);
//                            if (barang.getHarga_terendah()==barang.getHarga()){
//                                nego.setVisibility(View.INVISIBLE);
//                            }else{
//                                nego.setVisibility(View.VISIBLE);
//                            }
                            prosesNego.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    checkCart();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkNegoAuto(){
        String query="SELECT price, price_terendah, persen_nego_1, persen_nego_2, persen_nego_3 FROM gcm_list_barang WHERE id="+idBarang+";";
        Log.d("ido", "checkNegoAuto: "+query);
        try{
            Call<JsonObject> checkNego = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            checkNego.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().get("data").getAsJsonArray();
                            persenNego1 = jsonArray.get(0).getAsJsonObject().get("persen_nego_1").getAsFloat();
                            persenNego2 = jsonArray.get(0).getAsJsonObject().get("persen_nego_2").getAsFloat();
                            persenNego3 = jsonArray.get(0).getAsJsonObject().get("persen_nego_3").getAsFloat();
                            harga = jsonArray.get(0).getAsJsonObject().get("price").getAsFloat();
                            hargaRendah = jsonArray.get(0).getAsJsonObject().get("price_terendah").getAsFloat();
                        }
                    }
                    Log.d("ido", "onResponse: "+persenNego1+" "+persenNego2+" "+persenNego3);
                    if(persenNego1!=0.00||persenNego2!=0.00||persenNego3!=0.00){
                        //Toast.makeText(getApplicationContext(),"Jalankan Nego auto",Toast.LENGTH_SHORT).show();
                    }else{
                        //Toast.makeText(getApplicationContext(),"Jalankan Nego auto dengan harga rendah",Toast.LENGTH_SHORT).show();
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

    private void checkStatusCart(){
        String query = "select a.id, a.history_nego_id, a.status, a.nego_count, a.qty, c.berat, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3 from gcm_master_cart a, gcm_list_barang b, gcm_master_barang c where a.barang_id = b.id and c.id = b.barang_id and a.status = 'A' and a.company_id ="+SharedPrefManager.getInstance(this).getUser().getCompanyId()+" and a.barang_id ="+idBarang+";";
        Log.d("ido", "checkStatusCart: "+query);
        try {
            Call<JsonObject> checkStatusCart = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            checkStatusCart.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().get("data").getAsJsonArray();
                            Log.d("ido", "onResponse: "+jsonArray);
                            if(jsonArray == null){
                                Toast.makeText(getApplicationContext(), "berhasil", Toast.LENGTH_SHORT).show();
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

    private void cekMaxNego(){
        String query = "select count(*) from gcm_master_cart gmc where company_id = "+SharedPrefManager.getInstance(this).getUser().getCompanyId()+" and status = 'I' " +
                "and history_nego_id != 0 and barang_id = "+idBarang+" and create_date >= date_trunc('day', CURRENT_TIMESTAMP)";
        Log.d("ido", "cekMaxNego: "+query);
        try {
            Call<JsonObject> cekMaxNego = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            cekMaxNego.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().get("data").getAsJsonArray();
                            maxNego = jsonArray.get(0).getAsJsonObject().get("count").getAsInt();
                            Log.d("ido", "onResponse: "+maxNego);
                        }else{
                            Toast.makeText(getApplicationContext(), "Tidak mendapatkan data count nego", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Tidak Terkoneksi", Toast.LENGTH_SHORT).show();
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

    /**Method untuk request kurs*/
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
////                barangRequest();
////                Log.d("kursnyaaa", "onResponse: "+kursIdr);
////            }
////
////            @Override
////            public void onFailure(Call<JsonObject> call, Throwable t) {
////                Log.d("", "onFailure: "+t.getMessage());
////                kursRequest();
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
//                        barangRequest();
//                    }
//                }
//                Log.d("kursnyaaa", "onResponse: "+kursIdr);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d("", "onFailure: "+t.getMessage());
//                kursRequest();
//            }
//        });
//    }
}