package com.enseval.gcmuser.Activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.API.API;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.NotifAI.ModelNotif;
import com.enseval.gcmuser.Model.NotifFirebase.Data;
import com.enseval.gcmuser.Model.NotifFirebase.NotificationBody;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DetailPemesananActivity extends AppCompatActivity {

    private static final String TAG="ido";
    private CardView cvCatatan;
    private EditText etJumlah;
    private ImageView plus, minus, cancel;
    private TextView tvNama, tvHarga, tvHargaNego, tvTotal, tvHargaSatuan, tvSatuan;
    private int jumlah = 1;
    private String tipe;
    private Button btnBeli;
    private Barang barang;
    private ImageView imgBarang;
    private int hargaNego;
    private long lastClickTime=0;
    private LoadingDialog loadingDialog;
    private float kursIdr, persenNego1, persenNego2, persenNego3;
    private int id_barang;

    private API mApi;

    private int hargaSales, hargaFinal;
    private int id_shipto, id_billto, id_company_seller, id_payment;

    private String valueNegoCount, valueNegoID;
    private EditText inputNotes;
    private String valueSatuan;
    private float valueMinNego;
    private float valueMinBeli;

    private static int idHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pemesanan);

        loadingDialog = new LoadingDialog(this);
        mApi = RetrofitClient.getNotifService();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Detail Pesanan");
        getSupportActionBar().setHomeButtonEnabled(true);

        //untuk get id alamat shipto dan billto
        getAlamatShiptoCompany();
        getAlamatBilltoCompany();

        etJumlah = (EditText) findViewById(R.id.etJumlah);
        plus = (ImageView) findViewById(R.id.plus);
        minus = (ImageView) findViewById(R.id.minus);
        tvNama = (TextView) findViewById(R.id.tvNama);
        tvSatuan = (TextView) findViewById(R.id.textSatuan);
        tvHarga = (TextView) findViewById(R.id.tvHarga);
        cancel = (ImageView) findViewById(R.id.close);
        btnBeli = (Button) findViewById(R.id.btnBuy);
        imgBarang = findViewById(R.id.imgBarang);
        tvTotal = findViewById(R.id.tvTotal);
        tvHargaNego = findViewById(R.id.tvHargaNego);
        inputNotes = findViewById(R.id.inputNotes);
        cvCatatan = findViewById(R.id.cvCatatan);
        cvCatatan.setVisibility(View.GONE);
        tvHargaSatuan = findViewById(R.id.tvHargaSatuan);
        tvHargaSatuan.setVisibility(View.GONE);

        //ambil data intent
        tipe = getIntent().getStringExtra("tipe");
        barang = (Barang) getIntent().getSerializableExtra("barang");
        kursIdr = getIntent().getFloatExtra("kurs",0);
        valueSatuan = getIntent().getStringExtra("berat");
        valueMinNego = getIntent().getFloatExtra("jumlah_min_nego", 0);
        valueMinBeli = getIntent().getFloatExtra("jumlah_min_beli", 0);

        Log.d("nego", "onCreate: " + valueMinNego);
        id_barang = barang.getId();

        id_company_seller = barang.getCompanyId();
        getPaymentDefault();
//        int harganormal = (int) (barang.getHarga()*kursIdr);
//        int hargarendah = (int) (barang.getHarga_terendah()*kursIdr);
//        int negoawal = (hargarendah+(harganormal-hargarendah)*90/100);
//        Log.d(TAG, "onCreate: "+negoawal);

        if(tipe.equals("nego")){
            int total = (int) (barang.getHarga()*kursIdr*jumlah*Math.round(valueMinNego)); //harga total
            Log.d("nego", "total: "+total);
        }else{
            int total = (int) (barang.getHarga()*kursIdr*jumlah*Math.round(valueMinBeli)); //harga total
            Log.d("nego", "total: "+total);
        }

//        tvTotal.setText(Currency.getCurrencyFormat().format(total));

//        int hargaBarangGanteng  = (int) (barang.getHarga()* kursIdr / barang.getbe );
//        long harga_satuan       = (long) hargaBarangGanteng / Long.parseLong(barang.getBerat());
//        int total_bayar         = (int) Integer.parseInt(String.valueOf(harga_satuan)) * Integer.valueOf(etJumlah.getText().toString());
//        tvTotal.setText(Currency.getCurrencyFormat().format(total_bayar));

        if(tipe.equals("cart")){
            btnBeli.setText("Tambahkan");
        }
        else if(tipe.equals("buy")){
            btnBeli.setText("Beli");
        }
        else if(tipe.equals("nego")){
            hargaNego = getIntent().getIntExtra("hargaNego",0);
            tvHargaSatuan.setVisibility(View.VISIBLE);
            tvHarga.setVisibility(View.GONE);
            btnBeli.setText("Ajukan harga nego");
            btnBeli.setBackgroundResource(R.drawable.button_secondary_enabled);
        }

        tvNama.setText(barang.getNama());
        double harga = (double) Math.ceil(barang.getHarga()*kursIdr);
        tvHarga.setText(Currency.getCurrencyFormat().format(harga)+"/"+barang.getAlias());
        tvSatuan.setText("Kuantitas ("+barang.getAlias()+") :");

        Log.d("cekit",String.valueOf(barang.getId()));
        Log.d("cekit",String.valueOf(barang.getHarga()));
        Log.d("cekit",String.valueOf(valueSatuan));

//        int value = (int) ((barang.getHarga() * kursIdr) / Integer.valueOf(valueSatuan));
//        tvHargaSatuan.setText(Currency.getCurrencyFormat().format(value) + "  / kilogram");
        int value = (int) ((barang.getHarga() * kursIdr));
        tvHargaSatuan.setText(Currency.getCurrencyFormat().format(value) + " /"+barang.getAlias());

        //jika nego maka tampilkan harga negonya
        if(tipe.equals("nego")){
            tvHargaNego.setVisibility(View.VISIBLE);
//            int totalnego = getIntent().getIntExtra("hargaNego",0) * jumlah;
            int totalnego = getIntent().getIntExtra("hargaNego",0) * Math.round(valueMinNego);
            Log.d("nego", "onCreate: "+totalnego);
            if(tipe.equals("nego"))tvTotal.setText(Currency.getCurrencyFormat().format(totalnego));
            tvHargaNego.setText("Harga Nego:\n"+Currency.getCurrencyFormat().format(hargaNego));
        }
        else{
            tvHargaNego.setVisibility(View.GONE);
            //Gantisini
            long hargaBarang = (long) Math.ceil(barang.getHarga()*kursIdr);
            long total = (long) (hargaBarang*jumlah*Math.round(valueMinBeli));
            Log.d("harga", "Total: "+total+" "+barang.getHarga()+"*"+kursIdr+"*"+Integer.parseInt(valueSatuan));
            tvTotal.setText(Currency.getCurrencyFormat().format(total));
        }

//        Glide.with(getApplicationContext())
//                .load(barang.getFoto()).fallback(R.id.shimmer_view_container)
//                .error(R.id.shimmer_view_container)
//                .into(imgBarang);

        Log.d(TAG, "cek foto: "+barang.getFoto());
        if (barang.getFlag_foto().equals("Y")) {
            Glide.with(getApplicationContext())
                    .load(barang.getFoto())
                    .into(imgBarang);
        }else{
            Glide.with(getApplicationContext())
                    .load("https://www.glob.co.id/admin/assets/images/no_image.png")
                    .into(imgBarang);
        }

        minus.setVisibility(View.INVISIBLE);
        plus.setVisibility(View.VISIBLE);
        if(tipe.equals("nego")){
            jumlah = Math.round(valueMinNego);
        }else{
            jumlah = Math.round(valueMinBeli);
        }

        etJumlah.setText(String.format("%d", jumlah));

        //tambahkan atau kurangi kuantitas barang
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                jumlah += 1;
                jumlah += Integer.parseInt(valueSatuan);
                minus.setVisibility(View.VISIBLE);
                etJumlah.clearFocus();
                etJumlah.setText(String.format("%d", jumlah));
                etJumlah.setError(null);
                long harga = (long) (barang.getHarga()*kursIdr);
                //Gantisini
                long total = (long) (harga*jumlah);
                Log.d("harga", "total: " +total+" "+barang.getHarga()+"*"+kursIdr+"*"+jumlah);
                    tvTotal.setText(Currency.getCurrencyFormat().format(total));
                int totalnego = getIntent().getIntExtra("hargaNego",0) * jumlah;
                if(tipe.equals("nego"))tvTotal.setText(Currency.getCurrencyFormat().format(totalnego));

            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etJumlah.clearFocus();
//                if(jumlah>1){
//                    jumlah -= 1;
//                    etJumlah.setText(String.format("%d", jumlah));
//                    int total = (int) (barang.getHarga()*kursIdr*jumlah);
//                    tvTotal.setText(Currency.getCurrencyFormat().format(total));
//
//                    int totalnego = getIntent().getIntExtra("hargaNego",0) * jumlah;
//                    if(tipe.equals("nego"))tvTotal.setText(Currency.getCurrencyFormat().format(totalnego));
//                    if(jumlah==1){
//                        minus.setVisibility(View.INVISIBLE);
//                    }
//                }
                if(jumlah>valueMinNego){
//                    jumlah -= 1;
                    jumlah -= Integer.parseInt(valueSatuan);
                    etJumlah.setError(null);
                    etJumlah.setText(String.format("%d", jumlah));
                    //Gantisini
                    long hargaBarang = (long) (barang.getHarga()*kursIdr);
                    long total = (long) (hargaBarang*jumlah);
                    Log.d("harga", "onClick: "+total);
                    tvTotal.setText(Currency.getCurrencyFormat().format(total));

                    int totalnego = getIntent().getIntExtra("hargaNego",0) * jumlah;
                    if(tipe.equals("nego"))tvTotal.setText(Currency.getCurrencyFormat().format(totalnego));
//                    if(jumlah==1){
//                        minus.setVisibility(View.INVISIBLE);
//                    }
                    if(jumlah==valueMinNego){
                        minus.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        //ubah jumlah barang
        etJumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(tipe.equals("nego")){
                    if(etJumlah.getText().toString().equals("")){
                        etJumlah.setError("Minimum pembelian "+valueMinNego+" "+barang.getAlias());
                        btnBeli.setEnabled(false);
                    }
                    else if(Integer.parseInt(etJumlah.getText().toString())<valueMinNego){
                        etJumlah.setError("Minimum pembelian "+valueMinNego+" "+barang.getAlias());
                        jumlah=0;
                        btnBeli.setEnabled(false);
                    }
                    else if(Integer.parseInt(etJumlah.getText().toString())==valueMinNego){
                        minus.setVisibility(View.INVISIBLE);
                        jumlah = Integer.parseInt(etJumlah.getText().toString());
                        btnBeli.setEnabled(true);
                    }
                    else{
                        jumlah = Integer.parseInt(etJumlah.getText().toString());
                        minus.setVisibility(View.VISIBLE);
                        btnBeli.setEnabled(true);
                    }
                }
                else{
                    if(etJumlah.getText().toString().equals("")){
                        etJumlah.setError("Minimum pembelian "+valueMinBeli+" "+barang.getAlias());
                        btnBeli.setEnabled(false);
                    }
                    else if(Integer.parseInt(etJumlah.getText().toString())<Math.round(valueMinBeli)){
                        etJumlah.setError("Minimum pembelian "+valueMinBeli+" "+barang.getAlias());
                        jumlah=0;
                        btnBeli.setEnabled(false);
                    }
                    else if(Integer.parseInt(etJumlah.getText().toString())==Math.round(valueMinBeli)){
                        minus.setVisibility(View.INVISIBLE);
                        jumlah = Integer.parseInt(etJumlah.getText().toString());
                        btnBeli.setEnabled(true);
                    }
                    else{
                        jumlah = Integer.parseInt(etJumlah.getText().toString());
                        minus.setVisibility(View.VISIBLE);
                        btnBeli.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                long hargaBarang = (long) (barang.getHarga()*kursIdr);
                long total = (long) (hargaBarang*(jumlah/Integer.parseInt(valueSatuan))*Integer.parseInt(valueSatuan));
                Log.d("harga", "afterTextChanged: "+total+" "+barang.getHarga()+"*"+jumlah+"/"+valueSatuan+"*"+kursIdr);
                tvTotal.setText(Currency.getCurrencyFormat().format(total));
                Log.d("harga", "afterTextChanged: "+Currency.getCurrencyFormat());
                int totalnego = getIntent().getIntExtra("hargaNego",0) * jumlah;
                if(tipe.equals("nego"))tvTotal.setText(Currency.getCurrencyFormat().format(totalnego));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //jika button dipencet maka akan dimasukkan ke dalam cart
        btnBeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else{
                    if (tipe.equals("nego")) {
                        Log.d(TAG, "onClick: kepencet "+barang.getPersen_nego_1()+" "+barang.getPersen_nego_2()+" "+barang.getPersen_nego_3());
                        cartRequest();
                    }else {
                        cartRequest();
                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

    }

    private void cartRequest(){
        loadingDialog.showDialog();
        String query = "";

        //Mengecek apakah nego atau tidak
        if (tipe.equals("nego")){
            int jmlinsert = jumlah / Integer.parseInt(valueSatuan);
            double hargaNormal = (double) Math.ceil(barang.getHarga()*kursIdr);
            double hargaRendah = (double) Math.ceil(barang.getHarga_terendah()*kursIdr);
            double persenNego = (double) barang.getPersen_nego_1()/100;
            long hargaNego1 = (long) Math.ceil(hargaRendah+(hargaNormal-hargaRendah)*persenNego);
            long hargaSales = (long) Math.ceil(hargaNego1*jumlah);

            Log.d(TAG, "harga normal: "+hargaNormal);
            Log.d(TAG, "harga rendah: "+hargaRendah);
            Log.d(TAG, "harga nego 1: "+hargaNego1);
            Log.d(TAG, "harga nego: "+hargaNego);

            //Nego dengan SALES
            if (barang.getPersen_nego_1()==0 && barang.getPersen_nego_2()==0 && barang.getPersen_nego_3()==0){
//                if (hargaNego >= hargaRendah){
//                    query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, harga_konsumen, harga_sales, nego_count, shipto_id, billto_id, payment_id) " +
//                            "VALUES (" +
//                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ", " +
//                            barang.getId() + ", " +
//                            jmlinsert + ", " +
//                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
//                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
//                            String.valueOf(hargaNego * jumlah) + ", " +
//                            String.valueOf(hargaNego * jumlah) + ", " +
//                            "1, " +
//                            id_shipto + ", " +
//                            id_billto + ", " +
//                            id_payment + ") RETURNING id, nego_count;";
//                    Log.d(TAG, "QUERY Nego Sales: " + query);
//                }else {
                    query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, harga_konsumen, nego_count, shipto_id, billto_id, payment_id) " +
                            "VALUES (" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ", " +
                            barang.getId() + ", " +
                            jmlinsert + ", " +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
                            String.valueOf(hargaNego * jumlah) + ", " +
                            "1, " +
                            id_shipto + ", " +
                            id_billto + ", " +
                            id_payment + ") RETURNING id, nego_count;";
                    Log.d(TAG, "QUERY Nego Sales: " + query);
//                }
            }else{
                //Jika kondisi nego dengan AI
                if (barang.getPersen_nego_1()==0 && hargaNego<Math.round(hargaRendah)){
                    //Jika nego persen pertama 0 dan harga nego di bawah harga terendah
                    query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, harga_konsumen, harga_sales, nego_count, shipto_id, billto_id, payment_id) " +
                            "VALUES (" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+
                            barang.getId()+", " +
                            jmlinsert+", " +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                            String.valueOf(hargaNego*jumlah)+", " +
                            String.valueOf(Math.round(hargaRendah)*jumlah)+", " +
                            "1," +
                            id_shipto+", " +
                            id_billto+", " +
                            id_payment+") RETURNING id, nego_count;";
                    Log.d(TAG, "Nego AI(persen nego 0 dan hara nego < harga rendah): "+query);
                }else if (barang.getPersen_nego_1()==0 && hargaNego>Math.round(hargaRendah)){
                    //Jika nego persen pertama 0 dan harga nego di atas harga terendah
                    query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, harga_konsumen, harga_sales, nego_count, shipto_id, billto_id, payment_id) " +
                            "VALUES (" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+
                            barang.getId()+", " +
                            jmlinsert+", " +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                            String.valueOf(hargaNego*jumlah)+", " +
                            String.valueOf(hargaNego*jumlah)+", " +
                            "1," +
                            id_shipto+", " +
                            id_billto+", " +
                            id_payment+") RETURNING id, nego_count;";
                    Log.d(TAG, "Nego AI(persen nego 0 dan hara nego > harga rendah): "+query);
                }else {
                    //Jika kedua kondisi diatas tidak terpenuhi
                    if (hargaNego < hargaNego1){
                        //Jika kondisi harga nego user lebih kecil dari harga nego persen yang pertama
                        query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, harga_konsumen, harga_sales, nego_count, shipto_id, billto_id, payment_id) " +
                                "VALUES (" +
                                SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+
                                barang.getId()+", " +
                                jmlinsert+", " +
                                SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                                SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                                String.valueOf(hargaNego*jumlah)+", " +
                                String.valueOf(hargaSales)+", " +
                                "1," +
                                id_shipto+", " +
                                id_billto+", " +
                                id_payment+") RETURNING id, nego_count;";
                        Log.d(TAG, "Nego AI(harga nego user < harga nego persen 1): "+query);
                    }else {
                        //Jika kondisi harga nego user lebih besar dari harga nego persen yang pertama
                        query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, harga_konsumen, harga_sales, nego_count, shipto_id, billto_id, payment_id) " +
                                "VALUES (" +
                                SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+
                                barang.getId()+", " +
                                jmlinsert+", " +
                                SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                                SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                                String.valueOf(hargaNego*jumlah)+", " +
                                String.valueOf(hargaNego*jumlah)+", " +
                                "1," +
                                id_shipto+", " +
                                id_billto+", " +
                                id_payment+") RETURNING id, nego_count;";
                        Log.d(TAG, "Nego AI(harga nego user > harga nego persen 1): "+query);
                    }
                }
            }
        }else{
            //Jika kondisi tidak nego
            int jmlinsert = jumlah / Integer.parseInt(valueSatuan);
            query = "INSERT INTO gcm_master_cart (company_id, barang_id, qty, create_by, update_by, shipto_id, billto_id, payment_id) VALUES (" +
                    SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", " +
                    barang.getId()+", " +
                    jmlinsert+", " +
                    SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                    SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", " +
                    id_shipto+", " +
                    id_billto+", " +
                    id_payment+") RETURNING id;";
            Log.d(TAG, "QUERY Tidak Nego: "+query);
        }

        try {
            Call<JsonObject> callAddCart = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callAddCart.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            if (tipe.equals("cart")){
                                loadingDialog.hideDialog();
                                Intent intent = new Intent(DetailPemesananActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(DetailPemesananActivity.this, "Barang telah ditambahkan", Toast.LENGTH_SHORT).show();
                            }else if (tipe.equals("buy")){
                                loadingDialog.hideDialog();
                                Intent intent = new Intent(DetailPemesananActivity.this, CartActivity.class);
                                intent.putExtra("noBack", true);
                                startActivity(intent);
                            }else if (tipe.equals("nego")){
                                int returning_value = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
                                valueNegoID = String.valueOf(returning_value);
                                fillDataHistory();
                                Intent intent = new Intent(DetailPemesananActivity.this, CartActivity.class);
                                intent.putExtra("noBack", true);
                                startActivity(intent);
                            }
                        }else{
                            loadingDialog.hideDialog();
                            Toast.makeText(getApplicationContext(), "Koneksi gagal, Status error", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        loadingDialog.hideDialog();
                        Toast.makeText(getApplicationContext(), "Koneksi gagal tidak sukses", Toast.LENGTH_SHORT).show();
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

    private void fillDataHistory(){
        loadingDialog.showDialog();
        //Membuat waktu sekatang menjadi + 1 jam
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        Date DateTime = calendar.getTime();
        long TimeStamp = calendar.getTimeInMillis();
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String Date = datetime.format(DateTime);

        long timestamp_now = Calendar.getInstance().getTimeInMillis();

        String query = "";

        double harganormal = (double) Math.ceil(barang.getHarga()*kursIdr);
        double hargarendah = (double) Math.ceil(barang.getHarga_terendah()*kursIdr);
        double persenNego = (double) barang.getPersen_nego_1()/100;
        long hargaNego1 = (long) Math.ceil(hargarendah+(harganormal-hargarendah)*persenNego);
        long tmp_harga_sales = (long) Math.ceil(hargarendah+(harganormal-hargarendah)*persenNego);

        //Nego dengan SALES
        if (barang.getPersen_nego_1() == 0 && barang.getPersen_nego_2() == 0 && barang.getPersen_nego_3() == 0){
//            if (hargaNego>=hargarendah){
//                query = "INSERT INTO gcm_history_nego (harga_nego, harga_sales, harga_final, created_by, updated_by, notes) VALUES(" +
//                        hargaNego + ", " +
//                        hargaNego + ", " +
//                        hargaNego + ", " +
//                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
//                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
//                        "'') RETURNING id;";
//                Log.d(TAG, "Nego Sales: " + query);
//            }else {
                query = "INSERT INTO gcm_history_nego (harga_nego, harga_sales, harga_final, created_by, updated_by, notes, timestamp_created_date, timestamp_updated_date) VALUES(" +
                        hargaNego + ", " +
                        Math.round(harganormal) + ", '0', " +
                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + ", " +
                        "'', "+timestamp_now+", "+timestamp_now+") RETURNING id;";
                Log.d(TAG, "Nego Sales: " + query);
//            }
        }else{
            if (barang.getPersen_nego_1() == 0 && hargaNego < Math.round(hargarendah)){
                Log.d(TAG, "fillDataHistory: persenn nego 0");
                query = "INSERT INTO gcm_history_nego (harga_nego, harga_sales, harga_final, created_by, updated_by, notes, time_respon, timestamp_respon, timestamp_created_date, timestamp_updated_date) values" +
                        "('" + hargaNego + "','" +
                        Math.round(hargarendah) + "','0','" +
                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+"','"+
                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+"','"+
                        inputNotes.getText().toString() + "','"+Date+"', "+TimeStamp+", "+timestamp_now+", "+timestamp_now+") RETURNING id;";
                Log.d(TAG, "fillDataHistory: "+query);
            }else if(barang.getPersen_nego_1() == 0 && hargaNego > Math.round(hargarendah)){
                Log.d(TAG, "fillDataHistory: persen nego 0, and diatas harga rendah");
                query = "INSERT INTO gcm_history_nego (harga_nego, harga_sales, harga_final, created_by, updated_by, notes, time_respon, timestamp_respon, timestamp_created_date, timestamp_updated_date) values" +
                        "('" + hargaNego + "','" +
                        hargaNego + "',"+hargaNego+",'" +
                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+"','"+
                        SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+"','"+
                        inputNotes.getText().toString() + "','"+Date+"', "+TimeStamp+", "+timestamp_now+", "+timestamp_now+") RETURNING id;";
                Log.d(TAG, "fillDataHistory: "+query);
            }
            else {
                if (hargaNego < (hargaNego1)) {
                    Log.d(TAG, "fillDataHistory: dibawah persen nego 1");
                    query = "INSERT INTO gcm_history_nego (harga_nego, harga_sales, harga_final, created_by, updated_by, notes, time_respon, timestamp_respon, timestamp_created_date, timestamp_updated_date) values" +
                            "('" + hargaNego + "','" +
                            tmp_harga_sales + "','0','" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + "','" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + "','" +
                            inputNotes.getText().toString() + "','" + Date + "', "+TimeStamp+", "+timestamp_now+", "+timestamp_now+") RETURNING id;";
                    Log.d(TAG, "fillDataHistory: "+query);
                } else {
                    Log.d(TAG, "fillDataHistory: diatas persen nego 1");
                    query = "INSERT INTO gcm_history_nego (harga_nego, harga_sales, harga_final, created_by, updated_by, notes, time_respon, timestamp_respon, timestamp_created_date, timestamp_updated_date) values" +
                            "('" + hargaNego + "','" +
                            hargaNego + "','" + hargaNego + "','" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + "','" +
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + "','" +
                            inputNotes.getText().toString() + "','" + Date + "', "+TimeStamp+", "+timestamp_now+", "+timestamp_now+") RETURNING id;";
                    Log.d(TAG, "fillDataHistory: "+query);
                }
            }
        }

        try {
            Call<JsonObject> callHistoryNego = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callHistoryNego.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success") && response.body() != null){
                            loadingDialog.hideDialog();
                            idHistory = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
                            updateToCartDatabase(idHistory);
                        }else{
                            loadingDialog.hideDialog();
                            Toast.makeText(getApplicationContext(), "Proses mengisi database History gagal  ", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        loadingDialog.hideDialog();
                        Toast.makeText(getApplicationContext(), "response not isSuccessful", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    Toast.makeText(getApplicationContext(), "Maintenance Server", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateToCartDatabase(int idHistory) {
        String query = "update gcm_master_cart " +
                "set history_nego_id = "+ idHistory +", " +
//                "harga_sales = " + barang.getHarga() * kursIdr + ", " +
                "update_date=now(),update_by= " +SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + " "+
                "where id = " + valueNegoID + " returning id , history_nego_id ;";
        try {
            Call<JsonObject> updateData = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            updateData.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            loadingDialog.hideDialog();
                            int valueback = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
                            getTokenSeles();
                            //Toast.makeText(getApplicationContext(), "Update berhasil untuk id = " + String.valueOf(valueback), Toast.LENGTH_SHORT).show();
                        }
                        else if(status.equals("error")){
                            loadingDialog.hideDialog();
                            Toast.makeText(getApplicationContext(), "Koneksi gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    t.printStackTrace();
                    Log.wtf("CATCH_ONFAILURE === updateToCartDatabase",t);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            Log.wtf("CATCH_ERROR === updateToCartDatabase",e);
        }
    }

    //Method untuk get id alamat Shipto
    private void getAlamatShiptoCompany() {
        String querygetalamatshipto = "select id from gcm_master_alamat where company_id="+
                SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+
                " and shipto_active='Y'";
        try {
            Call<JsonObject> getidshipto = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(querygetalamatshipto)));
            getidshipto.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            id_shipto = jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
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

    //Method untuk get id alamat Billto
    private void getAlamatBilltoCompany() {
        String querygetalamatbillto = "select id from gcm_master_alamat where company_id="+
                SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+
                " and billto_active='Y'";
        try {
            Call<JsonObject> getidbillto = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(querygetalamatbillto)));
            getidbillto.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            id_billto = jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
                            Log.d("aprilbillto", String.valueOf(id_billto));
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

    //Method get id pembayaran
    private void getPaymentDefault() {
        String querygetpaymentdefault = "select id from gcm_payment_listing where buyer_id="+
                SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+
                " and seller_id="+id_company_seller+" and status='A' limit 1";
        try {
            Call<JsonObject> getpaymentdefault = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(querygetpaymentdefault)));
            getpaymentdefault.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            id_payment = jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
                            Log.d("aprilpaymentid", String.valueOf(id_payment));
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    id_payment = 0;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkNegoAuto(){
        String query="SELECT price, price_terendah, persen_nego_1, persen_nego_2, persen_nego_3 FROM gcm_list_barang WHERE id="+id_barang+";";
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
                        }
                    }
                    Log.d("ido", "onResponse: "+persenNego1+" "+persenNego2+" "+persenNego3);
                    if(persenNego1!=0.00||persenNego2!=0.00||persenNego3!=0.00){
                        Toast.makeText(getApplicationContext(),"Jalankan Nego auto",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onResponse: "+barang.getHarga());
                        negoAuto();
                    }else{
                        Toast.makeText(getApplicationContext(),"Jalankan Nego auto dengan harga rendah",Toast.LENGTH_SHORT).show();

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

    private void negoAuto(){
        double harganormal = (double) (barang.getHarga()*kursIdr);
        double hargarendah = (double) (barang.getHarga_terendah()*kursIdr);
        double negoawal = (hargarendah+(harganormal-hargarendah)*persenNego1/100);
        Log.d(TAG, "onCreate: "+negoawal);
    }

    private void sendNotif(ArrayList<String> listToken){
        String title = "GLOB";
        String content = "Notifikasi Negosiasi dari Penjual";

        Data data = new Data(title, content);
        NotificationBody body = new NotificationBody(listToken, data);

        API api = RetrofitClient.getClient().create(API.class) ;

        Call<ResponseBody> call = api.sendNotification(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: sukses kirim pesan");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "Error: ", t);
            }
        });
    }

    private void getTokenSeles(){
        final ArrayList<String> listToken = new ArrayList<>();
//        String query = "select distinct token from gcm_notification_token where user_id in " +
////                "(select id_sales from gcm_company_listing_sales where buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" " +
////                "and seller_id = "+id_company_seller+" and status = 'A')";
        String query = "select a.*, b.status from(select * from gcm_notification_token " +
                "where user_id in (select id_sales from gcm_company_listing_sales " +
                "where buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and seller_id = "+id_company_seller+" " +
                "and status = 'A') or company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" ) as a, " +
                "(select status from gcm_master_cart where id = "+valueNegoID+") as b order by id desc";
        Log.d(TAG, "getTokenSeles: "+query);
        try {
            Call<JsonObject> callGetToken = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetToken.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            Log.d(TAG, "onResponse: sukses masuk sini");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                listToken.add(i, jsonArray.get(i).getAsJsonObject().get("token").getAsString());
                            }
                            String flag = "";
                            if (barang.getPersen_nego_1()==0 && barang.getPersen_nego_2()==0 && barang.getPersen_nego_3()==0){
                                flag = "sales";
                                insertNotif(flag);
//                                sendNotif(listToken);
                                sendNotifNegoPersen("nego_sales", "0");
                                Log.d(TAG, "notif: nego sales");
                            }else{
                                flag = "AI";
                                insertNotif(flag);
                                sendNotifNegoPersen("nego_persen", "3600000");
                                Log.d(TAG, "notif: nego AI");
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

    private void insertNotif(String flag){
        long timestamp_kirim_sales = Calendar.getInstance().getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        long timestamp_kirim_AI = calendar.getTimeInMillis();
        String query = "";
        if (flag.equals("sales")) {
            query = "insert into gcm_notification_nego (barang_id, buyer_id, seller_id, source, status, timestamp_kirim) values (" +
                    barang.getId() + ", " +
                    SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ", " +
                    barang.getCompanyId() + ", " +
                    "'buyer', 'nego', "+timestamp_kirim_sales+") returning id;";
        }else if (flag.equals("AI")) {
            query = "insert into gcm_notification_nego (barang_id, buyer_id, seller_id, date, source, status, timestamp_kirim) values (" +
                    barang.getId() + ", " +
                    SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ", " +
                    barang.getCompanyId() + ", " +
                    "now() + interval '1 hour', " +
                    "'seller', 'nego', "+timestamp_kirim_AI+") returning id;";
        }
        Log.d(TAG, "insertNotif: "+query);
        try {
            Call<JsonObject> callInsertNotif = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callInsertNotif.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "sukses insert notif: YES");
                    }else{
                        Log.d(TAG, "sukses insert notif: NO");
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

    private void sendNotifNegoPersen(String nego_type, String timeout){
        Log.d(TAG, "sendNotifNegoPersen: "+valueNegoID);
        String id_cart = String.valueOf(valueNegoID);
        String company_id_buyer = String.valueOf(SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId());
        String company_id_seller = String.valueOf(barang.getCompanyId());

        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("nego_type", nego_type);
            requestBody.put("timeout", timeout);
            requestBody.put("id_cart", id_cart);
            requestBody.put("company_id_buyer", company_id_buyer);
            requestBody.put("company_id_seller", company_id_seller);

            Log.d(TAG, "sendNotifNegoPersen: "+String.valueOf(requestBody));

            Call<ModelNotif> call = mApi.sendNotifNegoPersen(requestBody);
            call.enqueue(new Callback<ModelNotif>() {
                @Override
                public void onResponse(Call<ModelNotif> call, Response<ModelNotif> response) {
                    Log.d(TAG, "tesNotif: berhasilll");
                }

                @Override
                public void onFailure(Call<ModelNotif> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
