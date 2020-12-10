package com.enseval.gcmuser.Activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.OrderDetailAdapter;
import com.enseval.gcmuser.Adapter.RekeningAdapter;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.Model.Rekening;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.enseval.gcmuser.Utilities.Currency;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.VISIBLE;

public class DetailOrderActivity extends AppCompatActivity {

    private String transactionId, Status;
    private RecyclerView recyclerView;
    private OrderDetailAdapter orderDetailAdapter;
    private ArrayList<OrderDetail> orderDetailList;
    private TextView idTransaksi, status, penjual, tglPermintaanKirim, shipTo, billTo, payment, tvTotalharga, tvOngkosKirim, tvHargaTotal, tvKetDibatalkan, tvPPN, tvTotalPPN, statusPayment, POPembeli;
    private long totalHarga, hargaTotal, hargaPPN;
    private double ongkosKirim;
    private float ppn;
    private Button btnPembayaran;
    public String statusPembayaran;
    private SwipeRefreshLayout Reload;

    //untuk dialog info pembayaran
    private RecyclerView rvListRekening;
    private Button btnInformasiPembayaran;
    private ImageButton imgBtnClose;
    private TextView tvRekening, tvNoRek, tvNamaRek;
    private ArrayList<Rekening> listRekening;
    private RekeningAdapter rekeningAdapter;

    //untuk dialog PO Pembeli
    private TextInputEditText nomorPO;
    private ImageView fileUpload;
    private TextView txtFilePath;
    private Button btnUploadFile, btnKonfirmasi;
    private ImageButton imgBtnclose;
    private ImageView imagePO;

    private final int PICK_IMAGE_REQUEST = 22;
    // Uri indicates, where the image will be picked from
    private Uri filePath;
    private String filename = null;
    private String fileUrl, extension;
    FirebaseStorage storage;
    StorageReference storageReference;

    private String statusPO, linkImage;

    private long lastClickTime=0;

    //Beli lagi
    private Button btnBelilg;
    private CardView cvBtnBelilg;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(
                                        intent,
                                        "Select Image from here..."),
                                PICK_IMAGE_REQUEST);
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        transactionId = getIntent().getExtras().getString("transactionId");
        hargaTotal = getIntent().getExtras().getLong("total");
        ongkosKirim = getIntent().getExtras().getDouble("ongkir");
        Status = getIntent().getExtras().getString("status");
        ppn = getIntent().getExtras().getFloat("ppn");

        Log.d("ido", "onCreate: "+transactionId+" "+hargaTotal+" "+ongkosKirim+" "+Status+" "+ppn);


        recyclerView = findViewById(R.id.rvOrderDetails);
        idTransaksi = findViewById(R.id.idTransaksi);
        status = findViewById(R.id.status);
        penjual = findViewById(R.id.penjual);
        tglPermintaanKirim = findViewById(R.id.permintaanKirim);
        shipTo = findViewById(R.id.shipTo);
        billTo = findViewById(R.id.billTo);
        payment = findViewById(R.id.payment);
        tvHargaTotal = findViewById(R.id.totalHarga);
        tvOngkosKirim = findViewById(R.id.ongkosKirim);
        tvTotalharga = findViewById(R.id.total);
        tvKetDibatalkan = findViewById(R.id.ketDibatalkan);
        tvPPN = findViewById(R.id.tvPpntotalHarga);
        tvTotalPPN = findViewById(R.id.ppntotalHarga);
        statusPayment = findViewById(R.id.statusPayment);
        btnPembayaran = findViewById(R.id.informasiPembayaran);
        Reload = findViewById(R.id.reload);
        POPembeli = findViewById(R.id.POPembeli);
        btnBelilg = findViewById(R.id.btnBelilg);
        cvBtnBelilg = findViewById(R.id.cvBtnBelilg);
        imagePO = findViewById(R.id.imagePO);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(Status.equals("complain")){
            getComplain();
        }else{
            orderDetailRequest();
        }
        shiptoReq();
        billtoReq();
        paymentReq();
//        getHargaTotal();
//        getOngkosKirirm()

        idTransaksi.setText(transactionId);
        if(Status.equals("menunggu")){
            status.setText("Menunggu");
            tvKetDibatalkan.setVisibility(View.GONE);
            btnBelilg.setVisibility(View.GONE);
        }else if(Status.equals("diproses")){
            status.setText("Diproses");
            btnBelilg.setVisibility(View.GONE);
            tvKetDibatalkan.setVisibility(View.GONE);
        }else if(Status.equals("dikirim")){
            status.setText("Dikirim");
            tvKetDibatalkan.setVisibility(View.GONE);
            btnBelilg.setVisibility(View.GONE);
        }else if(Status.equals("diterima")){
            status.setText("Diterima");
            tvKetDibatalkan.setVisibility(View.GONE);
            btnBelilg.setVisibility(View.GONE);
        }else if(Status.equals("dibatalkan")){
            status.setText("Dibatalkan");
            btnPembayaran.setVisibility(View.GONE);
            getKetDibatalkan();
        }else if(Status.equals("complain")){
            status.setText("Dikomplain");
            tvKetDibatalkan.setVisibility(View.GONE);
            btnBelilg.setVisibility(View.GONE);
        }else{
            status.setText("Selesai");
            tvKetDibatalkan.setVisibility(View.GONE);
        }

        tvHargaTotal.setText(Currency.getCurrencyFormat().format(hargaTotal));
        tvOngkosKirim.setText(Currency.getCurrencyFormat().format(ongkosKirim));
        tvPPN.setText("PPN "+Math.round(ppn)+"%");
        Log.d("ido", "PPN: "+hargaTotal+"*"+ppn+"/100");
        hargaPPN = hargaTotal*Math.round(ppn)/100;
        tvTotalPPN.setText(Currency.getCurrencyFormat().format(hargaPPN));
        totalHarga  = (long) (hargaTotal+ongkosKirim+hargaPPN);
        tvTotalharga.setText(Currency.getCurrencyFormat().format(totalHarga));
        tglPermintaanKirim.setText("-");

        btnPembayaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DetailOrderActivity.this);
                dialog.setContentView(R.layout.dialog_payment_information);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                rvListRekening = dialog.findViewById(R.id.rekBank);
                btnInformasiPembayaran = dialog.findViewById(R.id.btnInformasiPembayaran);
                imgBtnClose = dialog.findViewById(R.id.btnClose);

                dialog.setCancelable(false);

                imgBtnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                getRekBank();

                if (statusPembayaran.equals("sudah")){
                    btnInformasiPembayaran.setVisibility(View.GONE);
                }else{
                    btnInformasiPembayaran.setVisibility(VISIBLE);
                    btnInformasiPembayaran.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(), KonfirmasiPembayaranActivity.class);
                            i.putExtra("status", "menunggu");
                            i.putExtra("idTransaksi", transactionId);
                            i.putExtra("total", hargaTotal);
                            i.putExtra("ongkir", ongkosKirim);
                            i.putExtra("ppn", ppn);
                            startActivity(i);
                            finish();
                            dialog.dismiss();
                        }
                    });
                }

                dialog.show();

//                Intent i = new Intent(getApplicationContext(), TestMainActivity.class);
//                startActivity(i);
            }
        });

        //untuk memberikan warna pada loading
        Reload.setColorSchemeResources(R.color.colorPrimary);

        //untuk loading swipe refresh
        Reload.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Reload.setRefreshing(false);
                        if(Status.equals("complain")){
                            getComplain();
                        }else{
                            orderDetailRequest();
                        }
                        shiptoReq();
                        billtoReq();
                        paymentReq();
                    }
                }, 2000);
            }
        });

        btnBelilg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    funcBeliLagi();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            txtFilePath.setVisibility(VISIBLE);

            // Get the Uri of data
            filePath = data.getData();
            File file = new File(filePath.toString()); //file yang dipilih

            if (filePath.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getApplicationContext().getContentResolver().query(filePath, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (filePath.toString().startsWith("file://")) {
                filename = file.getName();
            }

            txtFilePath.setText(filename); //nama file
            extension = filename.substring(filename.lastIndexOf(".")); //ekstension file

            try {
                fileUpload.setVisibility(VISIBLE);
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                fileUpload.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private boolean doesUserHavePermission()
    {
        int result = checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**Method untuk request detail pesanan*/
    private void orderDetailRequest(){
        String query = "select a.id, a.barang_id, a.transaction_id, c.nama, f.nama_perusahaan, (select concat('https://www.glob.co.id/admin/assets/images/product/', f.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, a.qty, a.harga, c.berat, d.alias, to_char(e.tgl_permintaan_kirim, 'dd-Mon-YYYY') tgl_permintaan_kirim, a.batch_number, a.exp_date, a.qty_dipenuhi, a.harga_final , e.shipto_id, e.billto_id, e.payment_id, case when note is null then '' else note end, h.payment_id as master_payment_id, e.id_transaction_ref, e.foto_transaction_ref "+
                "from gcm_transaction_detail a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang c on c.id = b.barang_id "+
                "inner join gcm_master_satuan d on d.id = c.satuan inner join gcm_master_transaction e on e.id_transaction = a.transaction_id inner join gcm_master_company f on b.company_id = f.id " +
                "inner join gcm_payment_listing g on e.payment_id = g.id inner join gcm_seller_payment_listing h on g.payment_id = h.id "+
                "where a.transaction_id= '"+transactionId+"' order by c.category_id asc, c.nama asc";
        Log.d("ido", "orderDetailRequest: "+query);
        try {
            Log.d("ido", "orderDetailRequest: "+QueryEncryption.Encrypt(query));
            Call<JsonObject> orderDetailCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));

            orderDetailCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        Log.d("ido", "onResponse: sukses nembak");
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d("ido", "onResponse: sukses get data");
                            orderDetailList = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++) {
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                if(jsonObject.get("batch_number").isJsonNull() && jsonObject.get("exp_date").isJsonNull() && jsonObject.get("qty_dipenuhi").isJsonNull() && jsonObject.get("harga_final").isJsonNull()){
                                    Log.d("ido", "onResponse: pertama");
                                    orderDetailList.add(new OrderDetail(
                                            jsonObject.get("id").getAsInt(),
                                            jsonObject.get("transaction_id").getAsString(),
                                            jsonObject.get("nama").getAsString(),
                                            jsonObject.get("foto").getAsString(),
                                            jsonObject.get("qty").getAsInt(),
                                            jsonObject.get("harga").getAsLong(),
                                            jsonObject.get("berat").getAsString(),
                                            jsonObject.get("alias").getAsString(),
                                            jsonObject.get("shipto_id").getAsInt(),
                                            jsonObject.get("billto_id").getAsInt(),
                                            jsonObject.get("payment_id").getAsInt(),
                                            "",
                                            "",
                                            0,
                                            0,
                                            jsonObject.get("note").getAsString(),
                                            jsonObject.get("flag_foto").getAsString(),
                                            jsonObject.get("barang_id").getAsString()
                                    ));
                                }else{
                                    Log.d("ido", "onResponse: kedua");
                                    orderDetailList.add(new OrderDetail(
                                            jsonObject.get("id").getAsInt(),
                                            jsonObject.get("transaction_id").getAsString(),
                                            jsonObject.get("nama").getAsString(),
                                            jsonObject.get("foto").getAsString(),
                                            jsonObject.get("qty").getAsInt(),
                                            jsonObject.get("harga").getAsLong(),
                                            jsonObject.get("berat").getAsString(),
                                            jsonObject.get("alias").getAsString(),
                                            jsonObject.get("shipto_id").getAsInt(),
                                            jsonObject.get("billto_id").getAsInt(),
                                            jsonObject.get("payment_id").getAsInt(),
                                            jsonObject.get("batch_number").getAsString(),
                                            jsonObject.get("exp_date").getAsString(),
                                            jsonObject.get("qty_dipenuhi").getAsInt(),
                                            jsonObject.get("harga_final").getAsInt(),
                                            jsonObject.get("note").getAsString(),
                                            jsonObject.get("flag_foto").getAsString(),
                                            jsonObject.get("barang_id").getAsString()
                                    ));
                                }
                                penjual.setText(jsonArray.get(0).getAsJsonObject().get("nama_perusahaan").getAsString());
                                if(jsonArray.get(0).getAsJsonObject().get("tgl_permintaan_kirim").isJsonNull()){
                                    tglPermintaanKirim.setText("-");
                                }else{
                                    tglPermintaanKirim.setText(jsonArray.get(0).getAsJsonObject().get("tgl_permintaan_kirim").getAsString());
                                }
                            }
                            if (jsonArray.get(0).getAsJsonObject().get("master_payment_id").getAsInt()!=2){
                                statusPembayaran = "sudah";
                                statusPayment.setText("Belum bayar");
//                                btnPembayaran.setVisibility(View.GONE);
                            }else{
                                payment();
                            }
                            if (Status.equals("menunggu")){
                                if (jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").isJsonNull() || jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").isJsonNull()){
                                    POPembeli.setVisibility(VISIBLE);
                                    POPembeli.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        showPoPembeli();
                                        }
                                    });
                                }else{
                                    POPembeli.setText(jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").getAsString());
                                    linkImage = jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").getAsString();
                                    POPembeli.setTextColor(getResources().getColor(R.color.colorPrimary));
                                    imagePO.setVisibility(VISIBLE);
                                    POPembeli.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showImagePoPembeli();
                                        }
                                    });
                                }
                            }else{
                                if (jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").isJsonNull() || jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").isJsonNull()){
                                    POPembeli.setText("-");
                                    POPembeli.setTextColor(getResources().getColor(R.color.textColor));
                                    POPembeli.setTypeface(POPembeli.getTypeface(), Typeface.BOLD);
                                }else{
                                    POPembeli.setText(jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").getAsString());
                                    statusPO = "ada";
                                    linkImage = jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").getAsString();
                                    POPembeli.setTextColor(getResources().getColor(R.color.colorPrimary));
                                    imagePO.setVisibility(VISIBLE);
                                    POPembeli.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showImagePoPembeli();
                                        }
                                    });
                                }
                            }

                        }
                        //buat adapter untuk masing-masing barang pesanan
                        recyclerView.setLayoutManager(new LinearLayoutManager(DetailOrderActivity.this));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        orderDetailAdapter = new OrderDetailAdapter(DetailOrderActivity.this, orderDetailList, Status);
                        recyclerView.setAdapter(orderDetailAdapter);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    Log.d("ido", "onFailure: gagal");
                    orderDetailRequest();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shiptoReq(){
        String query = "select a.id, alamat, b.name as provinsi, c.nama as kota, d.nama as kecamatan, e.nama as kelurahan, kodepos from gcm_master_alamat a inner join gcm_location_province b on a.provinsi = b.id " +
                "inner join gcm_master_city c on a.kota = c.id inner join gcm_master_kecamatan d on a.kecamatan = d.id inner join gcm_master_kelurahan e on a.kelurahan = e.id inner join gcm_master_transaction f on f.shipto_id = a.id " +
                "where f.id_transaction ='"+transactionId+"';";
        try {
            Call<JsonObject> shiptoReq = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            shiptoReq.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String Alamat = jsonArray.get(0).getAsJsonObject().get("alamat").getAsString();
                            String Provinsi = jsonArray.get(0).getAsJsonObject().get("provinsi").getAsString();
                            String Kota = jsonArray.get(0).getAsJsonObject().get("kota").getAsString();
                            String Kecamatan = jsonArray.get(0).getAsJsonObject().get("kecamatan").getAsString();
                            String Kelurahan = jsonArray.get(0).getAsJsonObject().get("kelurahan").getAsString();
                            String Kodepos = jsonArray.get(0).getAsJsonObject().get("kodepos").getAsString();
                            shipTo.setText(Alamat +", Kelurahan "+ Kelurahan.toLowerCase() +", Kecamatan "+ Kecamatan.toLowerCase() +", Kota "+ Kota.toLowerCase() +"\n"+ Provinsi +", "+Kodepos);
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

    private void billtoReq(){
        String query = "select a.id, alamat, b.name as provinsi, c.nama as kota, d.nama as kecamatan, e.nama as kelurahan, kodepos from gcm_master_alamat a inner join gcm_location_province b on a.provinsi = b.id " +
                "inner join gcm_master_city c on a.kota = c.id inner join gcm_master_kecamatan d on a.kecamatan = d.id inner join gcm_master_kelurahan e on a.kelurahan = e.id inner join gcm_master_transaction f on f.billto_id = a.id " +
                "where f.id_transaction ='"+transactionId+"';";
        try {
            Call<JsonObject> shiptoReq = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            shiptoReq.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String Alamat = jsonArray.get(0).getAsJsonObject().get("alamat").getAsString();
                            String Provinsi = jsonArray.get(0).getAsJsonObject().get("provinsi").getAsString();
                            String Kota = jsonArray.get(0).getAsJsonObject().get("kota").getAsString();
                            String Kecamatan = jsonArray.get(0).getAsJsonObject().get("kecamatan").getAsString();
                            String Kelurahan = jsonArray.get(0).getAsJsonObject().get("kelurahan").getAsString();
                            String Kodepos = jsonArray.get(0).getAsJsonObject().get("kodepos").getAsString();
                            billTo.setText(Alamat +", Kelurahan "+ Kelurahan.toLowerCase() +", Kecamatan "+ Kecamatan.toLowerCase() +", Kota "+ Kota.toLowerCase() +"\n"+ Provinsi +", "+Kodepos);
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

    private void paymentReq(){
        String query = "select gmt.payment_id, gmp.payment_name from gcm_master_transaction gmt inner join gcm_payment_listing gpl on gmt.payment_id = gpl.id " +
                "inner join gcm_seller_payment_listing gspl on gpl.payment_id = gspl.id inner join gcm_master_payment gmp on gspl.payment_id = gmp.id where gmt.id_transaction = '"+transactionId+"';";
        try {
            Call<JsonObject> shiptoReq = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            shiptoReq.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String Payment = jsonArray.get(0).getAsJsonObject().get("payment_name").getAsString();
                            payment.setText(Payment);
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

    private void getKetDibatalkan(){
        String query = "select cancel_reason from gcm_master_transaction where id_transaction = '"+transactionId+"';";
        Log.d("ido", "getHargaTotal: "+query);
        try {
            Call<JsonObject> getHarga = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            getHarga.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            tvKetDibatalkan.setText("*"+jsonArray.get(0).getAsJsonObject().get("cancel_reason").getAsString());
                            Log.d("ido", "hargatotal: "+hargaTotal);
                        }
                        Log.d("ido", "hargatotal: suksess");
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

    private void getComplain(){
        String query = "select a.id, a.transaction_id, c.nama, f.nama_perusahaan, (select concat('https://www.glob.co.id/admin/assets/images/product/', f.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, a.qty, a.harga, c.berat, d.alias, to_char(e.tgl_permintaan_kirim, 'dd-Mon-YYYY') tgl_permintaan_kirim, a.batch_number, a.exp_date, a.qty_dipenuhi, a.harga_final, g.notes_complain, case when note is null then '' else note end, h.payment_id as master_payment_id, e.id_transaction_ref, e.foto_transaction_ref " +
                "from gcm_transaction_detail a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang c on c.id = b.barang_id " +
                "inner join gcm_master_satuan d on d.id = c.satuan inner join gcm_master_transaction e on e.id_transaction = a.transaction_id " +
                "inner join gcm_master_company f on b.company_id = f.id inner join gcm_transaction_complain g on g.detail_transaction_id = a.id " +
                "inner join gcm_payment_listing i on e.payment_id = i.id inner join gcm_seller_payment_listing h on i.payment_id = h.id " +
                "where a.transaction_id= '"+transactionId+"' order by c.category_id asc, c.nama asc";
        try {
            Call<JsonObject> getComplain = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            getComplain.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        Log.d("ido", "onResponse: sukses nembak");
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d("ido", "onResponse: sukses get data");
                            orderDetailList = new ArrayList<>();
                            final JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++) {
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                if(jsonObject.get("batch_number").isJsonNull() && jsonObject.get("exp_date").isJsonNull() && jsonObject.get("qty_dipenuhi").isJsonNull() && jsonObject.get("harga_final").isJsonNull()){
                                    Log.d("ido", "onResponse: pertama");
                                    orderDetailList.add(new OrderDetail(
                                            jsonObject.get("id").getAsInt(),
                                            jsonObject.get("transaction_id").getAsString(),
                                            jsonObject.get("nama").getAsString(),
                                            jsonObject.get("foto").getAsString(),
                                            jsonObject.get("qty").getAsInt(),
                                            jsonObject.get("harga").getAsLong(),
                                            jsonObject.get("berat").getAsString(),
                                            jsonObject.get("alias").getAsString(),
                                            jsonObject.get("notes_complain").getAsString(),
                                            "",
                                            "",
                                            0,
                                            0,
                                            jsonObject.get("note").getAsString(),
                                            jsonObject.get("flag_foto").getAsString()
                                    ));
                                }else{
                                    Log.d("ido", "onResponse: kedua");
                                    orderDetailList.add(new OrderDetail(
                                            jsonObject.get("id").getAsInt(),
                                            jsonObject.get("transaction_id").getAsString(),
                                            jsonObject.get("nama").getAsString(),
                                            jsonObject.get("foto").getAsString(),
                                            jsonObject.get("qty").getAsInt(),
                                            jsonObject.get("harga").getAsLong(),
                                            jsonObject.get("berat").getAsString(),
                                            jsonObject.get("alias").getAsString(),
                                            jsonObject.get("notes_complain").getAsString(),
                                            jsonObject.get("batch_number").getAsString(),
                                            jsonObject.get("exp_date").getAsString(),
                                            jsonObject.get("qty_dipenuhi").getAsInt(),
                                            jsonObject.get("harga_final").getAsInt(),
                                            jsonObject.get("note").getAsString(),
                                            jsonObject.get("flag_foto").getAsString()
                                    ));
                                }
                                penjual.setText(jsonArray.get(0).getAsJsonObject().get("nama_perusahaan").getAsString());
                                if(jsonArray.get(0).getAsJsonObject().get("tgl_permintaan_kirim").isJsonNull()){
                                    tglPermintaanKirim.setText("-");
                                }else{
                                    tglPermintaanKirim.setText(jsonArray.get(0).getAsJsonObject().get("tgl_permintaan_kirim").getAsString().substring(0,10));
                                }
                                if (jsonArray.get(0).getAsJsonObject().get("master_payment_id").getAsInt()!=2){
                                    statusPembayaran = "sudah";
                                    statusPayment.setText("Belum bayar");
//                                    btnPembayaran.setVisibility(View.GONE);
                                }else{
                                    payment();
                                }
                                if (Status.equals("menunggu")){
                                    if (jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").isJsonNull() || jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").isJsonNull()){
                                        POPembeli.setVisibility(VISIBLE);
                                        POPembeli.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showPoPembeli();
                                            }
                                        });
                                    }else{
                                        POPembeli.setText(jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").getAsString());
                                        linkImage = jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").getAsString();
                                        POPembeli.setTextColor(getResources().getColor(R.color.colorPrimary));
                                        imagePO.setVisibility(VISIBLE);
                                        POPembeli.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showImagePoPembeli();
                                            }
                                        });
                                    }
                                }else {
                                    if (jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").isJsonNull() || jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").isJsonNull()) {
                                        POPembeli.setText("-");
                                        POPembeli.setTextColor(getResources().getColor(R.color.textColor));
                                        POPembeli.setTypeface(POPembeli.getTypeface(), Typeface.BOLD);
                                    } else {
                                        POPembeli.setText(jsonArray.get(0).getAsJsonObject().get("id_transaction_ref").getAsString());
                                        statusPO = "ada";
                                        linkImage = jsonArray.get(0).getAsJsonObject().get("foto_transaction_ref").getAsString();
                                        POPembeli.setTextColor(getResources().getColor(R.color.colorPrimary));
                                        imagePO.setVisibility(VISIBLE);
                                        POPembeli.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showImagePoPembeli();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        //buat adapter untuk masing-masing barang pesanan
                        recyclerView.setLayoutManager(new LinearLayoutManager(DetailOrderActivity.this));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        orderDetailAdapter = new OrderDetailAdapter(DetailOrderActivity.this, orderDetailList, Status);
                        recyclerView.setAdapter(orderDetailAdapter);
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

    private void payment(){
        String query = "select id, id_transaction, status_payment, bukti_bayar, tanggal_bayar, id_list_bank, pemilik_rekening from gcm_master_transaction gmt where id_transaction = '"+transactionId+"'";
        Log.d("ido", "payment: "+query);
        try{
            Call<JsonObject> callPayment = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callPayment.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String status_payment = jsonArray.get(0).getAsJsonObject().get("status_payment").getAsString();
                            if (jsonArray.get(0).getAsJsonObject().get("bukti_bayar").isJsonNull() &&
                                    jsonArray.get(0).getAsJsonObject().get("tanggal_bayar").isJsonNull() &&
                                    jsonArray.get(0).getAsJsonObject().get("id_list_bank").isJsonNull() &&
                                    jsonArray.get(0).getAsJsonObject().get("pemilik_rekening").isJsonNull()){
                                if (status_payment.equals("UNPAID")){
                                    statusPayment.setText(getResources().getString(R.string.menungguPembayaran));
                                    statusPayment.setTextColor(getResources().getColor(R.color.color_warning));
                                }
                                statusPembayaran = "belum";
                            }else if (jsonArray.get(0).getAsJsonObject().get("bukti_bayar").isJsonNull() ||
                                    jsonArray.get(0).getAsJsonObject().get("tanggal_bayar").isJsonNull() ||
                                    jsonArray.get(0).getAsJsonObject().get("id_list_bank").isJsonNull() ||
                                    jsonArray.get(0).getAsJsonObject().get("pemilik_rekening").isJsonNull()){
                                if (status_payment.equals("UNPAID")){
                                    statusPayment.setText(getResources().getString(R.string.menungguKonfirmasi));
                                    statusPayment.setTextColor(getResources().getColor(R.color.yellow));
                                }else{
                                    statusPayment.setText(getResources().getString(R.string.lunas));
                                    statusPayment.setTextColor(getResources().getColor(R.color.colorPrimary));
                                }
                                statusPembayaran = "sudah";
                            }else{
                                if (status_payment.equals("UNPAID")){
                                    statusPayment.setText(getResources().getString(R.string.menungguKonfirmasi));
                                    statusPayment.setTextColor(getResources().getColor(R.color.yellow));
                                }else{
                                    statusPayment.setText(getResources().getString(R.string.lunas));
                                    statusPayment.setTextColor(getResources().getColor(R.color.colorPrimary));
                                }
                                statusPembayaran = "sudah";
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

    private void getRekBank(){
        String query = "SELECT a.id, a.no_rekening, a.pemilik_rekening, b.nama " +
                "FROM gcm_listing_bank a " +
                "left join gcm_master_bank b on a.id_bank = b.id " +
                "WHERE a.status = 'A' and b.status = 'A' and a.company_id = (select distinct company_id from gcm_transaction_detail a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_company f on b.company_id = f.id where a.transaction_id= '"+transactionId+"') " +
                "ORDER BY b.nama";
        try {
            Call<JsonObject> callGetRekening = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetRekening.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            listRekening = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++) {
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listRekening.add(new Rekening(
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("no_rekening").getAsString(),
                                        jsonObject.get("pemilik_rekening").getAsString(),
                                        jsonObject.get("nama").getAsString()
                                ));
                            }
                            rvListRekening.setLayoutManager(new LinearLayoutManager(DetailOrderActivity.this));
                            rvListRekening.setItemAnimator(new DefaultItemAnimator());
                            rekeningAdapter = new RekeningAdapter(DetailOrderActivity.this, listRekening);
                            rvListRekening.setAdapter(rekeningAdapter);
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

    private void checkData(){
        if (nomorPO.getText().toString().equals("") ||  txtFilePath.getText().equals("")){
            Toast.makeText(getApplicationContext(), "Data masih ada yang kososng", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "Sukses", Toast.LENGTH_LONG).show();
            uploadFile();
        }
    }

    private void uploadFile()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            final StorageReference ref = storageReference.child("po_reference/"+ Calendar.getInstance().getTimeInMillis()+"-Purchase-Order"+extension);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Image uploaded successfully
                            // Dismiss dialog
                            progressDialog.dismiss();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    fileUrl = String.valueOf(uri);
                                    saveDataPO();
                                    Toast.makeText(DetailOrderActivity.this, "Upload sukses", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(DetailOrderActivity.this, "Upload gagal", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                }
                            });
        }
    }

    private void saveDataPO(){
        String query = "update gcm_master_transaction set id_transaction_ref = '"+nomorPO.getText().toString()+"', foto_transaction_ref = '"+fileUrl+"' where id_transaction = '"+transactionId+"'";
        Log.d("ido", "saveDataPO: "+query);
        try {
            Call<JsonObject> saveDataPO = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            saveDataPO.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Status pesanan telah diperbarui", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), DetailOrderActivity.class);
                        i.putExtra("status", "menunggu");
                        i.putExtra("transactionId", transactionId);
                        i.putExtra("total", hargaTotal);
                        i.putExtra("ongkir", ongkosKirim);
                        i.putExtra("ppn", ppn);
                        startActivity(i);
                        finish();
                    }else{

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

    private void showPoPembeli(){
        final Dialog dialog = new Dialog(DetailOrderActivity.this);
        dialog.setContentView(R.layout.dialog_po_pembeli);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        imgBtnclose = dialog.findViewById(R.id.btnClose);
        nomorPO = dialog.findViewById(R.id.nomorPO);
        fileUpload = dialog.findViewById(R.id.gambarUploadPO);
        txtFilePath = dialog.findViewById(R.id.txtFilePath);
        btnUploadFile = dialog.findViewById(R.id.btnUploadPO);
        btnKonfirmasi = dialog.findViewById(R.id.btnKonfirmasi);

        imgBtnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        fileUpload.setVisibility(View.GONE);
        txtFilePath.setVisibility(View.GONE);
        dialog.setCancelable(false);

        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    if(!doesUserHavePermission()){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }
                    else{
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(
                                        intent,
                                        "Select Image from here..."),
                                PICK_IMAGE_REQUEST);
                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        btnKonfirmasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });

        dialog.show();
    }

    private void showImagePoPembeli(){
        final Dialog dialog = new Dialog(DetailOrderActivity.this);
        dialog.setContentView(R.layout.dialog_image_po);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imagePO = dialog.findViewById(R.id.imagePO);
        Glide.with(getApplicationContext()).load(linkImage).into(imagePO);

        dialog.show();
    }

    private void funcBeliLagi(){
        String loopBarang = "";
        String query_add_to_cart = "new_insert as (insert into gcm_master_cart (company_id, barang_id, qty, create_by, update_by, shipto_id, billto_id, payment_id) " +
                "select d.company_id, d.barang_id, d.qty, d.create_by, d.update_by, d.shipto_id, d.billto_id, d.payment_id " +
                "from data d " +
                "where not exists (select 1 from gcm_master_cart c where c.barang_id = d.barang_id and status = 'A' and company_id = " + SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ") returning barang_id) select count(barang_id) from new_insert";

        String query_get_shipto = "(select id from gcm_master_alamat where company_id = '" + SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + "' and flag_active = 'A' and shipto_active = 'Y' )";
        String query_get_billto = "(select id from gcm_master_alamat where company_id = '" + SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + "' and flag_active = 'A' and billto_active = 'Y' )";
        String query_get_payment = "(select a.id " +
                "from gcm_payment_listing a " +
                "inner join gcm_seller_payment_listing b on a.payment_id = b.id " +
                "inner join gcm_master_payment c on b.payment_id = c.id " +
                "where buyer_id = '"+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() +"' and b.status = 'A' and a.status = 'A' order by c.payment_name asc limit 1)";
        String query_data_insert = "with data(company_id, barang_id, qty, create_by, update_by, shipto_id, billto_id, payment_id) as ( values ";

        for (int i=0; i<orderDetailList.size(); i++){
            loopBarang = loopBarang + "(" + SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ", " +orderDetailList.get(i).getBarang_id()+", " +
                    orderDetailList.get(i).getQty() + ", " + SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
                    SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " + query_get_shipto + ", " + query_get_billto + ", " + query_get_payment + ")";
            if (i < orderDetailList.size() - 1){
                loopBarang = loopBarang.concat(",");
            }
            if (i == orderDetailList.size() - 1){
                loopBarang = loopBarang.concat("),");
            }
        }

        String query_final = query_data_insert.concat(loopBarang).concat(query_add_to_cart);
        Log.d("ido", "funcBeliLagi: "+query_final);

        try {
            Call<JsonObject> pesanUlang = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query_final)));
            pesanUlang.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            final int jmlhInsert = jsonArray.get(0).getAsJsonObject().get("count").getAsInt();

                            final Dialog dialog = new Dialog(DetailOrderActivity.this);
                            dialog.setContentView(R.layout.dialog_handle);
                            Window window = dialog.getWindow();
                            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            ImageView image = dialog.findViewById(R.id.iconImage);
                            TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                            Button btnSetuju = dialog.findViewById(R.id.btnYa);
                            TextView title = dialog.findViewById(R.id.judul);
                            TextView description = dialog.findViewById(R.id.isi);
                            dialog.setCancelable(false);
                            image.setVisibility(View.GONE);

                            title.setText("Pesan Ulang");
                            description.setText("Apakah anda yakin ingin pesan ulang ?");

                            btnBatal.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                        return;
                                    }
                                    else {
                                        dialog.dismiss();
                                    }
                                    lastClickTime=SystemClock.elapsedRealtime();
                                }
                            });

//                          jika setuju lanjut ke request
                            btnSetuju.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                        return;
                                    }
                                    else {
                                        if (jmlhInsert != orderDetailList.size() && jmlhInsert > 0){
                                            int jmlhTdkInsert = orderDetailList.size()-jmlhInsert;
                                            String text = jmlhTdkInsert+" barang tidak ditambahkan, karena barang sudah ada di keranjang";
                                            dialogNotif(text);
                                            dialog.dismiss();
                                            Log.d("ido", "funcBeliLagi: "+jmlhTdkInsert+" barang tidak ditambahkan karena sudah ada di keranjang");
                                        }else if (jmlhInsert == 0){
                                            String text = "Tidak ada barang yang ditambahkan ke keranjang karena barang sudah tersedia di keranjang";
                                            dialogNotif(text);
                                            Log.d("ido", "funcBeliLagi: Tidak ada barang yang ditambahkan ke keranjang karena barang sudah tersedia di keranjang");
                                            dialog.dismiss();
                                        }else{
                                            Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                                            intent.putExtra("noBack", true);
                                            startActivity(intent);
                                            finish();
                                            dialog.dismiss();
                                        }
                                    }
                                    lastClickTime=SystemClock.elapsedRealtime();
                                }
                            });
                            dialog.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e) {

        }
    }

    private void dialogNotif(String text){
        final Dialog dialog = new Dialog(DetailOrderActivity.this);
        dialog.setContentView(R.layout.dialog_handle);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView image = dialog.findViewById(R.id.iconImage);
        TextView btnBatal = dialog.findViewById(R.id.btnBatal);
        Button btnSetuju = dialog.findViewById(R.id.btnYa);
        TextView title = dialog.findViewById(R.id.judul);
        TextView description = dialog.findViewById(R.id.isi);
        dialog.setCancelable(false);
        btnBatal.setVisibility(View.GONE);
        image.setVisibility(View.GONE);

        title.setText("Pesan Ulang");
        description.setText(text);

        //jika setuju lanjut ke request
        btnSetuju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                intent.putExtra("noBack", true);
                startActivity(intent);
                finish();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
