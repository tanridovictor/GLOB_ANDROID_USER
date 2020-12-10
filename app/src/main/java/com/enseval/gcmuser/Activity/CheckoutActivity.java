package com.enseval.gcmuser.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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
import com.enseval.gcmuser.API.JSONRequestTransaksi;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.CheckoutCompanyAdapter;
import com.enseval.gcmuser.Adapter.PagerAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Alamat;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.KalenderLibur;
import com.enseval.gcmuser.Model.Note;
import com.enseval.gcmuser.Model.Payment;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class CheckoutActivity extends AppCompatActivity {
    private ArrayList<String> listReturnIdTransaction;
    private ArrayList<Payment> listPayment;
    private ArrayList<Alamat> listAlamat;
    private ArrayList<Alamat> listAlamatBillto;
    private ArrayList<KalenderLibur> listLibur;
    private ArrayList<Note> listNote;
    private RecyclerView rvCheckoutCompany;
    private ImageView close;
    private ArrayList<Company> listCheckoutCompany;
    private ArrayList<Cart> listongkir;
    private CheckoutCompanyAdapter checkoutCompanyAdapter;
    private TextView tvTotal, tvOngkir, tvTotalAll, tvAlamatShipto, tvTotalTagihan;
    private long total;
    private Button btnOrder, refresh;
    private String idTransaction, createDate;
    private LoadingDialog loadingDialog;
    private long lastClickTime=0;
    String TAG = "ido";
    private int harga = 0;
    private int ppn = 0;
    private int ongkoskir = 0;
    private String statusPerubahan;
    private Barang barang;
    private Cart cart;
    private ConstraintLayout ringkasanHarga;
    private String TesCheckout;

    private ConstraintLayout failed;
    private CardView cvBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        mSocket.connect();

        failed = findViewById(R.id.failed);
        refresh = findViewById(R.id.refresh);
        cvBottom = findViewById(R.id.cvBottom);

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

        loadingDialog = new LoadingDialog(this);
        failed.setVisibility(GONE);
        cvBottom.setVisibility(View.VISIBLE);

        loadingDialog.showDialog();
        getShipto();
        //requestCheckout();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Checkout");
        getSupportActionBar().setHomeButtonEnabled(true);

        rvCheckoutCompany = findViewById(R.id.rvCheckoutCompany);
        ringkasanHarga = findViewById(R.id.consSummary);
        close = findViewById(R.id.close);
        tvTotal = findViewById(R.id.tvTotal);
        tvOngkir = findViewById(R.id.tvOngkir);
        tvTotalAll = findViewById(R.id.tvTotalAll);
        btnOrder = findViewById(R.id.btnOrder);
        tvAlamatShipto = findViewById(R.id.alamatShipto);
        tvTotalTagihan = findViewById(R.id.tvTotalTagihan);

        ringkasanHarga.setVisibility(GONE);

        listCheckoutCompany = getIntent().getParcelableArrayListExtra("listSeller");
        total = getIntent().getLongExtra("total",0);
        //kursIdr = getIntent().getFloatExtra("kurs",0);


        tvTotal.setText(Currency.getCurrencyFormat().format(total));
        long totalAll = total;
        tvTotalAll.setText(Currency.getCurrencyFormat().format(totalAll));

        //jika button ditekan maka tampilkan dialog konfirmasi
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(CheckoutActivity.this);
                dialog.setContentView(R.layout.konfirmasi_dialog);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                TextView title = dialog.findViewById(R.id.title);
                TextView description = dialog.findViewById(R.id.description);
                dialog.setCancelable(false);

                title.setText("Konfirmasi Pesanan");
                description.setText("Siap untuk membuat pesanan? Pastikan nama, jumlah, dan harga barang pada daftar pesanan anda telah sesuai.");

                btnBatal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //jika setuju lanjut ke request
                btnSetuju.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                            return;
                        } else {
                            dialog.dismiss();
                            loadingDialog.showDialog();
                            checkAlamat();
                        }
                        lastClickTime = SystemClock.elapsedRealtime();
                    }
                });

                dialog.show();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Function untuk insert order ke tabel master transaksi dan tabel detail transaksi
    private void orderRequest(){
        ArrayList<String> listIdTransaksi = new ArrayList<>();
        int shipto = 0, billto = 0, payment = 0;
        double ongkir = 0;
        double ongkirSatuan = 0;
        String tgl_pengiriman="";
        String query = "INSERT INTO gcm_master_transaction (id_transaction, company_id, status, create_by, create_date, update_by, "+
                       "update_date, kurs_rate, shipto_id, billto_id, payment_id, id_sales, ongkos_kirim, tgl_permintaan_kirim, ppn_seller, ongkos_kirim_satuan) VALUES ";
        String loop = "";
        for(int i=0;i<listCheckoutCompany.size();i++){
            int l = 0;
            while (l<listongkir.size()){
                if (listongkir.get(l).getNama_perusahaan().equals(listCheckoutCompany.get(i).getNamaPerusahaan())){
                    ongkirSatuan = listongkir.get(l).getOngkir();
                    break;
                }
                l++;
            }
            for (int j=0; j<listCheckoutCompany.size(); j++) {
                if (listAlamat.get(j).getNama_perusahaan().equals(listCheckoutCompany.get(i).getNamaPerusahaan())) {
                    shipto = listAlamat.get(j).getId();
                    tgl_pengiriman = listAlamat.get(j).getTgl_permintaan_kirim();
                }
                if (listAlamatBillto.get(j).getNama_perusahaan().equals(listCheckoutCompany.get(i).getNamaPerusahaan())) {
                    billto = listAlamatBillto.get(j).getId();
                }
                if (listPayment.get(j).getNama_perusahaan().equals(listCheckoutCompany.get(i).getNamaPerusahaan())) {
                    payment = listPayment.get(j).getId();
                }
                if (listongkir.get(j).getNama_perusahaan().equals(listCheckoutCompany.get(i).getNamaPerusahaan())) {
                    ongkir = listongkir.get(j).getOngkir() * getBerat(listCheckoutCompany.get(i).getListCart());
                }
            }
            ArrayList<Cart> listCheckout = listCheckoutCompany.get(i).getListCart();
            String getIdTransaksi = "(select concat('GLOB','/M/', TO_CHAR((select id from gcm_company_listing gcl " +
                    "where buyer_id = "+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() +" and status = 'A' and seller_id = "+listCheckoutCompany.get(i).getId()+"),'fm00000'), '/', TO_CHAR(NOW() :: DATE, 'yymm'), TO_CHAR(get_count+1,'fm0000'))" +
                    "from (select count(id_transaction) as get_count from gcm_master_transaction gmt where id_transaction like " +
                    "TO_CHAR((select id from gcm_company_listing gcl where buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and status = 'A' and seller_id = "+listCheckoutCompany.get(i).getId()+"),'%/fm00000/%')" +
                    "and create_date >=  date_trunc('year', CURRENT_TIMESTAMP ) and create_date >=  date_trunc('month', CURRENT_TIMESTAMP ))as id_trx)";
            String getSales = "(select id_sales from gcm_company_listing_sales " +
                    "where buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+"  and seller_id = "+listCheckoutCompany.get(i).getId()+"  and status = 'A' )";
            loop = loop + "(" +getIdTransaksi+", "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+
                    ", 'WAITING', "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+
                    ", now(), "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+
                    ", now(), "+getkurs(listCheckoutCompany.get(i).getListCart())+", "+shipto+
                    ", "+billto+
                    ", "+payment+
                    ", "+getSales+","+ongkir+", '"+tgl_pengiriman+"', "+getppn(listCheckoutCompany.get(i).getListCart())+"" +
                    ", "+ongkirSatuan+")";
            listIdTransaksi.add(getIdTransaksi);
            if(i < listCheckoutCompany.size()-1){
                loop = loop.concat(",");
            }
        }
        String queryTransaksi = query.concat(loop);
        Log.d("ido", "orderRequest: "+queryTransaksi);
        String queryDetilTransaksi = "insert into gcm_transaction_detail (transaction_id, barang_id, qty, harga, create_by, create_date ,update_by, buyer_id, harga_asli, harga_kesepakatan, note) values ";
        int count = 0;
        int count1 = 0;
        String loopQuery = "";
        for(int i=0; i<listCheckoutCompany.size(); i++){
            ArrayList<Cart> listCheckout = listCheckoutCompany.get(i).getListCart();
            for(int j=0; j<listCheckout.size(); j++){
                long harga = (long) Math.ceil(listCheckout.get(j).getBarang().getKursIdr()*listCheckout.get(j).getBarang().getHarga())*listCheckout.get(j).getQty()*Integer.parseInt(listCheckout.get(j).getBerat());
                if(listCheckout.get(j).getNegoCount() > 0 && listCheckout.get(j).getHarga_final() != 0 && listCheckout.get(j).getHistory_nego_id()!=0){
                    loopQuery = loopQuery + "(" +listIdTransaksi.get(i)+", "+listCheckout.get(j).getBarang().getId()+", "+listCheckout.get(j).getQty()+", "+
                            (listCheckout.get(j).getHarga_final()*listCheckout.get(j).getQty()*Integer.parseInt(listCheckout.get(j).getBerat()))+", "+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", now(), "+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", "+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+
                            Math.ceil(listCheckout.get(j).getBarang().getKursIdr()*listCheckout.get(j).getBarang().getHarga())+", "+
                            listCheckout.get(j).getHarga_final()+", '" +
                            listNote.get(j).getNote()+"' )";
                }else if(listCheckout.get(j).getNegoCount() == 0 || (listCheckout.get(j).getNegoCount()>0 && listCheckout.get(j).getHarga_final()==0)){
                    loopQuery = loopQuery + "(" +listIdTransaksi.get(i)+", "+listCheckout.get(j).getBarang().getId()+", "+listCheckout.get(j).getQty()+", "+
                            (Math.ceil(listCheckout.get(j).getBarang().getKursIdr()*listCheckout.get(j).getBarang().getHarga())*listCheckout.get(j).getQty()*Integer.parseInt(listCheckout.get(j).getBerat()))+", "+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", now(), "+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+", "+
                            SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+", "+
                            Math.ceil(listCheckout.get(j).getBarang().getKursIdr()*listCheckout.get(j).getBarang().getHarga())+", "+
                            Math.ceil(listCheckout.get(j).getBarang().getKursIdr()*listCheckout.get(j).getBarang().getHarga())+", '" +
                            listNote.get(j).getNote()+"' )";
                }
                if(count1 < listCheckout.size()-1){
                    loopQuery = loopQuery.concat(",");
                }
                count1++;
            }
            if (count < listCheckoutCompany.size()-1){
                loopQuery = loopQuery.concat(",");
            }
            count++;
        }
        String queryDetail = queryDetilTransaksi.concat(loopQuery);
        if(queryDetail.charAt(queryDetail.length()-1)== ','){
            queryDetail = queryDetail.substring(0, queryDetail.length()-1);
        }
        String finalQuery = "with new_insert1 as ("+queryTransaksi+"), new_insert2 as (" + queryDetail + " returning transaction_id) select distinct transaction_id from new_insert2";
        String hasil = finalQuery.replaceAll("[\\[\\]]", "");
        Log.d(TAG, "Final Query: "+hasil);
        try {
            Call<JsonObject> orderCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(hasil)));
            orderCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    final Dialog dialog = new Dialog(CheckoutActivity.this);
                    dialog.setContentView(R.layout.konfirmasi_dialog);
                    Window window = dialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                    Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                    btnBatal.setVisibility(GONE);
                    TextView title = dialog.findViewById(R.id.title);
                    TextView description = dialog.findViewById(R.id.description);
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            listReturnIdTransaction = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                String idTransaction = jsonArray.get(i).getAsJsonObject().get("transaction_id").getAsString();
                                listReturnIdTransaction.add(idTransaction);
                            }
                            loadingDialog.hideDialog();
                            title.setText("Transaksi Berhasil");
                            description.setText("Transaksi anda telah kami terima, Terima Kasih sudah berbelanja");

                            btnSetuju.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                        return;
                                    }else{
//                                        socket io
                                        attemptSend(listReturnIdTransaction);
                                        dialog.dismiss();
                                        Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }
                            });

                            dialog.show();
                        }else{
                            loadingDialog.hideDialog();
                            title.setText("Transaksi Gagal");
                            description.setText("Transaksi anda telah kami terima, status gagal");

                            btnSetuju.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                        return;
                                    }else{
                                        dialog.dismiss();
                                        Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }
                            });

                            dialog.show();
                        }
                    }else{
                        loadingDialog.hideDialog();
                        title.setText("Transaksi Gagal");
                        description.setText("Transaksi anda gagal, tanggal permintaan kirim masih belum ditentukan. Silahkan periksa kembali data pemesanan anda.");

                        btnBatal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        btnSetuju.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                    return;
                                }else{
                                    dialog.dismiss();
                                }
                            }
                        });

                        dialog.show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Function untuk mendapatkan data ongkos kirim
    private void requestOngkir(){
        String ongkir = "select distinct a.company_id, d.nama_perusahaan, a.shipto_id, b.kota, case when c.harga is null then 0 else c.harga end as ongkir " +
                "from (select distinct a.shipto_id, b.company_id from gcm_master_cart a inner join gcm_list_barang b on a.barang_id = b.id  where a.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and a.status = 'A') as a inner join gcm_master_alamat b on  a.shipto_id = b.id " +
                "left join gcm_ongkos_kirim c on c.tujuan_kota = b.kota and a.company_id = c.id_company inner join gcm_master_company d on d.id = a.company_id";
        Log.d(TAG, "requestOngkir: "+ongkir);
        try {
            Call<JsonObject> callOngkir = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(ongkir)));
            callOngkir.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listongkir = new ArrayList<>();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                //int buyer_id = jsonObject.get("company_id").getAsInt();
                                int company_id = jsonObject.get("company_id").getAsInt();
                                String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                int shipto_id = jsonObject.get("shipto_id").getAsInt();
                                String kota = jsonObject.get("kota").getAsString();
                                float ongkir = jsonObject.get("ongkir").getAsFloat();
                                Barang barang = new Barang(company_id, nama_perusahaan);
                                listongkir.add(new Cart(company_id, nama_perusahaan, shipto_id, kota, ongkir));
                            }

                            rvCheckoutCompany.setLayoutManager(new LinearLayoutManager(CheckoutActivity.this));
                            rvCheckoutCompany.setItemAnimator(new DefaultItemAnimator());
                            checkoutCompanyAdapter = new CheckoutCompanyAdapter(CheckoutActivity.this, listCheckoutCompany, total, listongkir, listAlamat, listPayment, listAlamatBillto, listLibur, listNote);
                            rvCheckoutCompany.setAdapter(checkoutCompanyAdapter);

                            long ongkir = 0;
                            long hargappn = 0;
                            for (int i=0; i<listCheckoutCompany.size(); i++){
                                int berat = 0;
                                long harga=0;
                                for (int j=0; j<listCheckoutCompany.get(i).getListCart().size(); j++){
                                    berat = berat + (Integer.parseInt(listCheckoutCompany.get(i).getListCart().get(j).getBerat())*listCheckoutCompany.get(i).getListCart().get(j).getQty());
                                    if (listCheckoutCompany.get(i).getListCart().get(j).getNegoCount()>0){
                                        harga = (long) (harga + listCheckoutCompany.get(i).getListCart().get(j).getHargaSales());
                                    }else {
                                        harga = (long) (harga + listCheckoutCompany.get(i).getListCart().get(j).getBarang().getKursIdr() * listCheckoutCompany.get(i).getListCart().get(j).getBarang().getHarga() * Integer.parseInt(listCheckoutCompany.get(i).getListCart().get(j).getBerat()) * listCheckoutCompany.get(i).getListCart().get(j).getQty());
                                    }
                                }
                                ongkir = (long) (ongkir + ((listongkir.get(i).getOngkir()*berat)));
                                float ppn = (getppn(listCheckoutCompany.get(i).getListCart())/100);
                                hargappn = (long) (hargappn + harga * ppn);
                            }
                            long TotalTagihan = (long) (total+hargappn+ongkir);
                            tvTotalTagihan.setText(Currency.getCurrencyFormat().format(TotalTagihan));
                            loadingDialog.hideDialog();
                        }
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    cvBottom.setVisibility(GONE);
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Function untuk mendapatkan list metode pembayaran
    private void getPayment(){
        String get_payment = "select distinct a.payment_id, d.nama_perusahaan, gmp.payment_name from "+
                "gcm_seller_payment_listing gspl, gcm_payment_listing gpl, gcm_master_payment gmp, gcm_master_cart a "+
                "inner join gcm_list_barang b on a.barang_id = b.id inner join gcm_master_barang c on b.barang_id = c.id "+
                "inner join gcm_master_company d on b.company_id = d.id where a.payment_id = gpl.id and gpl.payment_id = gspl.id "+
                "and gspl.payment_id = gmp.id and a.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" "+
                "and a.status = 'A'";
        try {
            Call<JsonObject> callGetPayment = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(get_payment)));
            callGetPayment.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listPayment = new ArrayList<>();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                int payment_id = jsonObject.get("payment_id").getAsInt();
                                String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                String payment_name = jsonObject.get("payment_name").getAsString();
                                listPayment.add(new Payment(nama_perusahaan, payment_id, payment_name));
                            }
                            getKalenderLibur();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    cvBottom.setVisibility(GONE);
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Function untuk mendapatkan alamat shipto
    private void getShipto(){
        final String get_shipto = "select distinct a.id, i.nama_perusahaan , alamat, b.name as provinsi, c.nama as kota, d.nama as kecamatan, e.nama as kelurahan, kodepos, to_char(f.tgl_permintaan_kirim, 'dd-MON-YYYY') tgl_permintaan_kirim from gcm_master_alamat a inner join gcm_location_province b on a.provinsi = b.id " +
                "inner join gcm_master_city c on a.kota = c.id inner join gcm_master_kecamatan d on a.kecamatan = d.id inner join gcm_master_kelurahan e on a.kelurahan = e.id inner join gcm_master_cart f on a.id = f.shipto_id inner join gcm_list_barang g on f.barang_id = g.id inner join " +
                "gcm_master_barang h on g.barang_id = h.id inner join gcm_master_company i on g.company_id = i.id " +
                "where f.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and f.status ='A';";
        try {
            Call<JsonObject> callGetShipto = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(get_shipto)));
            callGetShipto.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listAlamat = new ArrayList<>();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                if(jsonObject.get("tgl_permintaan_kirim").isJsonNull()){
                                    int id = jsonObject.get("id").getAsInt();
                                    String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                    String alamat = jsonObject.get("alamat").getAsString();
                                    String provinsi = jsonObject.get("provinsi").getAsString();
                                    String kota = jsonObject.get("kota").getAsString();
                                    String kecamatan = jsonObject.get("kecamatan").getAsString();
                                    String kelurahan = jsonObject.get("kelurahan").getAsString();
                                    String kodepos = jsonObject.get("kodepos").getAsString();
                                    String tgl_permintaan_kirim = "Belum ditentukan";
                                    listAlamat.add(new Alamat(id, nama_perusahaan, alamat, provinsi, kota, kecamatan, kelurahan, kodepos, tgl_permintaan_kirim));
                                }else{
                                    int id = jsonObject.get("id").getAsInt();
                                    String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                    String alamat = jsonObject.get("alamat").getAsString();
                                    String provinsi = jsonObject.get("provinsi").getAsString();
                                    String kota = jsonObject.get("kota").getAsString();
                                    String kecamatan = jsonObject.get("kecamatan").getAsString();
                                    String kelurahan = jsonObject.get("kelurahan").getAsString();
                                    String kodepos = jsonObject.get("kodepos").getAsString();
                                    String tgl_permintaan_kirim = jsonObject.get("tgl_permintaan_kirim").getAsString();
                                    listAlamat.add(new Alamat(id, nama_perusahaan, alamat, provinsi, kota, kecamatan, kelurahan, kodepos, tgl_permintaan_kirim));
                                }
                            }
                            getBillto();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    cvBottom.setVisibility(GONE);
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){

        }
    }

    //Function untuk mendapatkan alamat billto
    private void getBillto(){
        final String get_billto = "select distinct a.id, i.nama_perusahaan , alamat, b.name as provinsi, c.nama as kota, d.nama as kecamatan, e.nama as kelurahan, kodepos, to_char(f.tgl_permintaan_kirim, 'dd-MON-YYYY') tgl_permintaan_kirim from gcm_master_alamat a inner join gcm_location_province b on a.provinsi = b.id " +
                "inner join gcm_master_city c on a.kota = c.id inner join gcm_master_kecamatan d on a.kecamatan = d.id inner join gcm_master_kelurahan e on a.kelurahan = e.id inner join gcm_master_cart f on a.id = f.billto_id inner join gcm_list_barang g on f.barang_id = g.id inner join " +
                "gcm_master_barang h on g.barang_id = h.id inner join gcm_master_company i on g.company_id = i.id " +
                "where f.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and f.status ='A';";
        try {
            Call<JsonObject> callGetBillto = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(get_billto)));
            callGetBillto.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listAlamatBillto = new ArrayList<>();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                if(jsonObject.get("tgl_permintaan_kirim").isJsonNull()){
                                    int id = jsonObject.get("id").getAsInt();
                                    String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                    String alamat = jsonObject.get("alamat").getAsString();
                                    String provinsi = jsonObject.get("provinsi").getAsString();
                                    String kota = jsonObject.get("kota").getAsString();
                                    String kecamatan = jsonObject.get("kecamatan").getAsString();
                                    String kelurahan = jsonObject.get("kelurahan").getAsString();
                                    String kodepos = jsonObject.get("kodepos").getAsString();
                                    String tgl_permintaan_kirim = "Belum ditentukan";
                                    listAlamatBillto.add(new Alamat(id, nama_perusahaan, alamat, provinsi, kota, kecamatan, kelurahan, kodepos, tgl_permintaan_kirim));
                                }else{
                                    int id = jsonObject.get("id").getAsInt();
                                    String nama_perusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                    String alamat = jsonObject.get("alamat").getAsString();
                                    String provinsi = jsonObject.get("provinsi").getAsString();
                                    String kota = jsonObject.get("kota").getAsString();
                                    String kecamatan = jsonObject.get("kecamatan").getAsString();
                                    String kelurahan = jsonObject.get("kelurahan").getAsString();
                                    String kodepos = jsonObject.get("kodepos").getAsString();
                                    String tgl_permintaan_kirim = jsonObject.get("tgl_permintaan_kirim").getAsString();
                                    listAlamatBillto.add(new Alamat(id, nama_perusahaan, alamat, provinsi, kota, kecamatan, kelurahan, kodepos, tgl_permintaan_kirim));
                                }
                            }
                            getPayment();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    cvBottom.setVisibility(GONE);
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){

        }
    }

    //Function untuk mendapatkan data kalender libur
    private void getKalenderLibur(){
        String get_kalender = "SELECT * FROM gcm_kalender_libur where tanggal >= now() - interval '1day' order by tanggal asc";
        try{
            Call<JsonObject> callKalender = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(get_kalender)));
            callKalender.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listLibur = new ArrayList<>();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                int id = jsonObject.get("id").getAsInt();
                                String tanggal = jsonObject.get("tanggal").getAsString();
                                String keterangan = jsonObject.get("keterangan").getAsString();
                                listLibur.add(new KalenderLibur(id, tanggal, keterangan));
                            }
                            getNote();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    cvBottom.setVisibility(GONE);
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Function untuk mendapatkan data notes dari tabel master cart
    private void getNote(){
        String query = "select id, case when note is null then '' else note end from gcm_master_cart where company_id= "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and status = 'A' order by create_date asc;";
        try {
            Call<JsonObject> callGetNote = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetNote.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            listNote = new ArrayList<>();
                            for (int i=0; i<jsonArray.size(); i++) {
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                int id = jsonObject.get("id").getAsInt();
                                String note = jsonObject.get("note").getAsString();
                                listNote.add(new Note(id, note));
                            }
                            requestOngkir();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    cvBottom.setVisibility(GONE);
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Untuk mendapatkan ppn tiap barang
    private float getppn(ArrayList<Cart> listcart){
        float ppn = 0;
        for(Cart cart : listcart){
            ppn = cart.getPpn_seller();
        }
        return ppn;
    }

    //Untuk mendapatkan kurs tiap barang
    private float getkurs(ArrayList<Cart> listcart){
        float kurs = 0;
        for(Cart cart : listcart){
            kurs = cart.getBarang().getKursIdr();
        }
        return kurs;
    }

    //Untuk mendapatkan berat tiap barang
    private int getBerat(ArrayList<Cart> listCart){
        int berat = 0;
        long ongkir = 0;
        for(Cart cart : listCart){
            berat += cart.getQty()*Integer.parseInt(cart.getBerat());
        }
        return berat;
    }

    //Function untuk check apakah alamat sudah dilakukan mapping oleh seller. jika belum maka transaksi tidak bisa diselesaikan.
    private void checkAlamat(){
        String query = "select * from ( select string_agg(distinct ''||c.nama_perusahaan||''  , ', ') as nama_perusahaan_shipto from " +
                "(select distinct a.shipto_id, a.company_id, b.company_id as seller_id from gcm_master_cart a " +
                "inner join gcm_list_barang b on a.barang_id = b.id  where a.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and a.status = 'A')a " +
                "inner join gcm_listing_alamat b on a.shipto_id = b.id_master_alamat and a.company_id = b.id_buyer and a.seller_id = b.id_seller " +
                "inner join gcm_master_company c on b.id_seller = c.id and b.kode_shipto_customer is null ) shipto," +
                "( select string_agg(distinct ''||c.nama_perusahaan||''  , ', ') as nama_perusahaan_billto from " +
                "(select distinct a.billto_id, a.company_id, b.company_id as seller_id from gcm_master_cart a " +
                "inner join gcm_list_barang b on a.barang_id = b.id  where a.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and a.status = 'A')a " +
                "inner join gcm_listing_alamat b on a.billto_id = b.id_master_alamat and a.company_id = b.id_buyer and a.seller_id = b.id_seller " +
                "inner join gcm_master_company c on b.id_seller = c.id and b.kode_billto_customer is null ) billto";
        Log.d(TAG, "checkAlamat: "+query);
        try {
            Call<JsonObject> callCheckAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCheckAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();

                            final Dialog dialog = new Dialog(CheckoutActivity.this);
                            dialog.setContentView(R.layout.konfirmasi_dialog);
                            Window window = dialog.getWindow();
                            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                            Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                            TextView title = dialog.findViewById(R.id.title);
                            TextView description = dialog.findViewById(R.id.description);
                            if (jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_shipto").isJsonNull() && jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_billto").isJsonNull()) {
                                orderRequest();
                            }else if (!jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_shipto").isJsonNull() && jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_billto").isJsonNull()){
                                String distribuorShipto = jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_shipto").getAsString();
                                loadingDialog.hideDialog();
                                dialog.setCancelable(false);
                                btnBatal.setVisibility(GONE);

                                title.setText("Transaksi Gagal");
                                description.setText("Alamat Pengiriman yang dipilih tidak dapat digunakan untuk transaksi ke " + distribuorShipto + ". Silakan mengubah alamat pengiriman atau hubungi distributor terkait untuk mengaktifkan alamat.");

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
                            }else if (!jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_billto").isJsonNull()&&jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_shipto").isJsonNull()){
                                String distribuorBillto = jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_billto").getAsString();
                                loadingDialog.hideDialog();
                                dialog.setCancelable(false);
                                btnBatal.setVisibility(GONE);

                                title.setText("Transaksi Gagal");
                                description.setText("Alamat Penagihan yang dipilih tidak dapat digunakan untuk transaksi ke " + distribuorBillto + ". Silakan hubungi distributor terkait untuk mengaktifkan alamat.");

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
                            }else if(!jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_shipto").isJsonNull()&&!jsonArray.get(0).getAsJsonObject().get("nama_perusahaan_billto").isJsonNull()){
                                loadingDialog.hideDialog();
                                dialog.setCancelable(false);
                                btnBatal.setVisibility(GONE);

                                title.setText("Transaksi Gagal");
                                description.setText("Alamat penagihan dan pengiriman yang dipilih tidak dapat digunakan untuk transaksi. Silakan menghubungi distributor terkait.");

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
                            }
                        }
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    failed.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://transaction-socket.herokuapp.com");
        } catch (URISyntaxException e) {}
    }

    //Function untuk mendapatkan id sales untuk data kirim notifikasi transaksi.
    private void attemptSend(final ArrayList<String> idTransaksi) {
        String query = "select string_agg(''||id_sales||'' , ',') as id_sales from gcm_company_listing_sales " +
                "where buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" " +
                "and status = 'A' ";
        Log.d(TAG, "attemptSend: "+query);
        try {
            Call<JsonObject> callIdSales = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callIdSales.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String idSales = jsonArray.get(0).getAsJsonObject().get("id_sales").getAsString();

//                            JSONObject test = new JSONObject();
//                            int idCompanySeller = listCheckoutCompany.get(0).getId();

                            String idCompanySeller="";
                            String IdTrx = "";
                            int count = 0;
                            for(int i=0; i<listCheckoutCompany.size(); i++){
                                idCompanySeller = idCompanySeller + listCheckoutCompany.get(i).getId();
                                IdTrx = IdTrx+ listCheckoutCompany.get(i).getId()+"-"+idTransaksi.get(i).toString();
                                if (count < listCheckoutCompany.size()-1){
                                    idCompanySeller = idCompanySeller.concat(",");
                                    IdTrx = IdTrx.concat(",");
                                }
                                count++;
                            }
//                            try {
//                                test.put("receiver_id", ""+idCompanySeller+"-"+idSales+"");
//                                test.put("type", "buy");
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                            mSocket.emit("new_transaction", test);

                            sendNotifTrx(idSales, idCompanySeller, IdTrx);
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

    //Function untuk hit API kirim notifikasi transaksi ke seller.
    private void sendNotifTrx(final String id_sales, final String company_id_seller, final String IdTransaksi){
        final Call<JsonObject> sendNotifTrx = RetrofitClient
                .getInstanceGLOB()
                .getApi()
                .sendNotifTrx(new JSONRequestTransaksi(String.valueOf(id_sales), String.valueOf(company_id_seller), IdTransaksi));
        sendNotifTrx.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "Kirim Notifikasi Transaksi: Sukses");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
}