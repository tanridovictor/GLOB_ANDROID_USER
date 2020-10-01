package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
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

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.CartBarangAdapter;
import com.enseval.gcmuser.Adapter.CartSellerAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Fragment.NotLoggedInFragment;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCartBarang;
    private CartBarangAdapter cartBarangAdapter;
    private ImageView close;
    private Barang barang;
    private int jumlah;
    private static long totalAll;
    private static TextView tvTotalAll;
    private CardView cvBottom;
    private ConstraintLayout noItem, content;
    private static Button btnCheckout;
//    private ArrayList<Negosiasi> listNego;
    private boolean noBack;
    private boolean fromNego;
    private LoadingDialog loadingDialog;
    private ConstraintLayout failed;
    private Button refresh;
    private long lastClickTime = 0;
    private static float kursIdr;
    private Cart cart;
    private static ArrayList<Company> listSeller;
    private CartSellerAdapter cartSellerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        content = findViewById(R.id.content);
        failed = findViewById(R.id.failed);
        content.setVisibility(View.VISIBLE);
        failed.setVisibility(View.INVISIBLE);

        loadingDialog = new LoadingDialog(this);

        totalAll=0;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Keranjang");
        getSupportActionBar().setHomeButtonEnabled(true);

        noBack = getIntent().getBooleanExtra("noBack", false);

        rvCartBarang = findViewById(R.id.rvCartBarang);
        close = findViewById(R.id.close);
        tvTotalAll = findViewById(R.id.tvTotal);
        cvBottom = findViewById(R.id.cvBottom);
        noItem = findViewById(R.id.noItem);
        btnCheckout = findViewById(R.id.btnCheckout);
        refresh = findViewById(R.id.refresh);

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

        cvBottom.setVisibility(View.INVISIBLE);
        noItem.setVisibility(View.INVISIBLE);

        if(SharedPrefManager.getInstance(this).isLoggedin()){
            //kursRequest(); //request kurs
            cartRequest();
        }
        else{
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new NotLoggedInFragment())
                    .commit();
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    if(noBack){
                        Intent intent = new Intent(CartActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    else{
                        finish();
                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        //jika checkout ditekan
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cek jumlah barang yang dalam negosiasi
                int jumlahNego=0;
                for(Company seller : listSeller){
                    for(Cart cart : seller.getListCart()){
                        if(cart.getNegoCount()>0 && cart.getHargaKonsumen()<cart.getHargaSales() && cart.isChecked()){
                            jumlahNego++;
                        }
                    }
                }
                //jika ada barang yg masih dalam negosiasi, tampilkan konfirmasi
                //apabila disetujui, maka harga yang digunakan adalah harga yang terakhir diajukan sales (bukan harga nego)
                if(jumlahNego>0){
                    final Dialog dialog = new Dialog(CartActivity.this);
                    dialog.setContentView(R.layout.konfirmasi_dialog);
                    Window window = dialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                    Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                    TextView title = dialog.findViewById(R.id.title);
                    TextView description = dialog.findViewById(R.id.description);

                    title.setText("Barang dalam proses negosiasi");
                    description.setText("Anda memiliki "+jumlahNego+" barang yang masih dalam proses negosiasi. " +
                            "Melanjutkan ke Checkout berarti menyetujui harga terakhir yang kami berikan.\nAnda yakin ingin melanjutkan ke Checkout?");

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
                            }
                            else {
                                dialog.dismiss();
                                toCheckout();
                            }
                            lastClickTime = SystemClock.elapsedRealtime();
                        }
                    });

                    dialog.show();
                }
                else{
                    toCheckout();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateCart(); //update cart ketika activity stop
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(noBack){
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**Method untuk mengambil barang-barang yang ada pada cart*/
    private void cartRequest() {
//        "SELECT a.id, c.nama, c.berat, a.barang_id, b.price, b.price_terendah, b.foto, c.category_id, b.company_id as seller_id, d.nama_perusahaan as nama_seller, qty, harga_konsumen, harga_sales, nego_count, e.alias, b.jumlah_min_nego, b.jumlah_min_beli, " +
//                "a.shipto_id, a.billto_id, a.payment_id, f.payment_name, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, i.nominal as kurs"+
//                " FROM gcm_master_payment f, gcm_seller_payment_listing g, gcm_payment_listing h, gcm_master_cart a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang c on b.barang_id=c.id inner join gcm_master_company d " +
//                " on b.company_id=d.id inner join gcm_master_satuan e on c.satuan=e.id inner join gcm_listing_kurs i on i.company_id = b.company_id where a.payment_id = h.id and h.payment_id = g.id and g.payment_id = f.id and a.company_id="+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and a.status='A' and now() between i.tgl_start and i.tgl_end order by a.create_date asc;"
        String query = "SELECT a.id, b.kode_barang, d.kode_seller, d.nama_perusahaan, c.nama, c.berat, a.barang_id, b.price, b.price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', d.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, c.category_id, b.company_id as seller_id, d.nama_perusahaan as nama_seller, qty, harga_konsumen, a.harga_sales, nego_count,  a.history_nego_id, e.harga_final, b.jumlah_min_beli, b.jumlah_min_nego, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, " +
                "f.alias as satuan, a.shipto_id, a.billto_id, a.payment_id, h.payment_name, j.nominal as kurs, case when e.harga_final is null then 0 else e.harga_final end, case when a.tgl_permintaan_kirim is null then now() else a.tgl_permintaan_kirim end, d.ppn_seller FROM gcm_listing_kurs j, gcm_seller_payment_listing i, gcm_master_payment h, gcm_payment_listing g, gcm_master_satuan f, gcm_master_cart a inner join gcm_list_barang b on a.barang_id = b.id inner join " +
                "gcm_master_barang c on b.barang_id = c.id inner join gcm_master_company d on b.company_id = d.id left join gcm_history_nego e on a.history_nego_id = e.id  where f.id = c.satuan and a.payment_id = g.id and g.payment_id = i.id and i.payment_id = h.id " +
                "and j.company_id = b.company_id and a.company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+"  and a.status = 'A' and now() between j.tgl_start and j.tgl_end  order by a.create_date asc";
        try {
            loadingDialog.showDialog();
            Call<JsonObject> cartCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));

            cartCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            listSeller = new ArrayList<>();
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                if(jsonArray.size()>0){
                                    for(int i=0;i<jsonArray.size();i++){
                                    long totalPerItem;
                                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                    int id = jsonObject.get("id").getAsInt();
                                    Log.d("", "id cart: "+id);
                                    String namaBarang = jsonObject.get("nama").getAsString();
                                    int idBarang = jsonObject.get("barang_id").getAsInt();
                                    String kode_barang = jsonObject.get("kode_barang").getAsString();
                                    double harga = jsonObject.get("price").getAsDouble();
                                    double harga_terendah = jsonObject.get("price_terendah").getAsDouble();
                                    String berat = jsonObject.get("berat").getAsString();
                                    String foto = jsonObject.get("foto").getAsString();
                                    int categoryId = jsonObject.get("category_id").getAsInt();
                                    int companyId = jsonObject.get("seller_id").getAsInt();
                                    String namaPerusahaan = jsonObject.get("nama_seller").getAsString();
                                    jumlah = jsonObject.get("qty").getAsInt();
                                    String alias = jsonObject.get("satuan").getAsString();
                                    float minNego = jsonObject.get("jumlah_min_nego").getAsFloat();
                                    float minBeli = jsonObject.get("jumlah_min_beli").getAsFloat();
                                    float persen_nego_1 = jsonObject.get("persen_nego_1").getAsFloat();
                                    float persen_nego_2 = jsonObject.get("persen_nego_2").getAsFloat();
                                    float persen_nego_3 = jsonObject.get("persen_nego_3").getAsFloat();
                                    kursIdr = jsonObject.get("kurs").getAsFloat();
                                    String flag_foto = jsonObject.get("flag_foto").getAsString();
                                    barang = new Barang(namaBarang, idBarang, harga, harga_terendah, berat, foto, categoryId, companyId, namaPerusahaan, alias, minNego, minBeli, persen_nego_1, persen_nego_2, persen_nego_3, kursIdr, kode_barang, flag_foto);
//                                    totalPerItem = (int) (barang.getHarga() * jumlah * kursIdr);
                                    double hargaBarang = (double) Math.ceil(barang.getHarga()*kursIdr);
                                    totalPerItem = (long) (hargaBarang * jumlah * Integer.parseInt(barang.getBerat()));
                                    int count = jsonObject.get("nego_count").getAsInt();
//april
                                    int id_shipto = jsonObject.get("shipto_id").getAsInt();
                                    int id_billto = jsonObject.get("billto_id").getAsInt();
                                    int id_payment = jsonObject.get("payment_id").getAsInt();
                                    String payment_name = jsonObject.get("payment_name").getAsString();
                                    Integer harga_final = jsonObject.get("harga_final").getAsInt();
                                    Integer history_nego_id = jsonObject.get("history_nego_id").getAsInt();
                                    String tgl_permintaan_kirim = jsonObject.get("tgl_permintaan_kirim").getAsString();
                                    float ppn_seller = jsonObject.get("ppn_seller").getAsFloat();
                                    //kondisi jika barang sudah negosiasi
                                    if(count>0){
                                        int hargaKonsumen = jsonObject.get("harga_konsumen").getAsInt();
                                        int hargaSales;
                                        if(jsonObject.get("harga_sales").isJsonNull()){
//                                            hargaSales  = (int) (barang.getHarga()*kursIdr);
                                            double hargabarang = (double) Math.ceil(barang.getHarga()*kursIdr);
                                            hargaSales  = (int) Math.ceil(hargabarang*Integer.parseInt(barang.getBerat())*jumlah);
                                            Log.d("ido", "onResponse: hargasalescart"+hargaSales);
                                        }
                                        else{
//                                            hargaSales = jsonObject.get("harga_sales").getAsInt();
                                            if (hargaKonsumen == jsonObject.get("harga_sales").getAsInt()) {
                                                hargaSales = jsonObject.get("harga_sales").getAsInt();
                                            } else {
                                                hargaSales  = (int) Math.ceil(barang.getHarga()*kursIdr*Integer.parseInt(barang.getBerat())*jumlah);
                                            }
                                        }
                                        totalPerItem = hargaSales;
//april                                 cart = new Cart(id, barang, jumlah, berat, hargaKonsumen, hargaSales, count, true);
//                                        listCart.add(cart);
                                        cart = new Cart(id, barang, jumlah, berat, hargaKonsumen, hargaSales, count, true, id_shipto, id_billto, id_payment, payment_name, harga_final, history_nego_id, tgl_permintaan_kirim, ppn_seller);
                                    }
                                    //kondisi jika barang belum pernah nego
                                    else{
//                                        cart = new Cart(id, barang, jumlah, berat, true);
//april
                                        cart = new Cart(id, barang, jumlah, berat, true, id_shipto, id_billto, id_payment, payment_name, harga_final, history_nego_id, tgl_permintaan_kirim, ppn_seller);
                                    }

                                    //mendata semua seller dari barang yang ada di dalam cart. jika ada beberapa barang yang sellernya sama, cukup dimasukkan sekali saja
                                    if(!checkListSeller(jsonObject.get("seller_id").getAsInt())){
                                        ArrayList<Cart> temp = new ArrayList<Cart>();
                                        temp.add(cart);
                                        listSeller.add(new Company(jsonObject.get("seller_id").getAsInt(), jsonObject.get("nama_seller").getAsString(), temp, true));
                                        Log.d("", "NAMA SELLER YANG DIMASUKKIN: "+jsonObject.get("nama_seller").getAsString());
                                    }
                                    else{
                                        for(Company company : listSeller){
                                            if(company.getId()==jsonObject.get("seller_id").getAsInt()){
                                                company.addCart(cart);
                                            }
                                        }
                                    }
                                    Log.d("apriltotalallsebelumif", String.valueOf(totalAll));
                                    if(cart.isChecked()){
                                        Log.d("apriltotalperitem", String.valueOf(totalPerItem));
                                        totalAll = totalAll + totalPerItem; //menghitung total harga semuanya
                                        Log.d("apriltotalallsebelumif", String.valueOf(totalAll));
                                    }
                                }
                                loadingDialog.hideDialog();

                                //buat adapter dari list seller (ini adapter terluar, nanti didalam masing-masing seller ada adapter lagi utk barang)
                                RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(CartActivity.this);
                                rvCartBarang.setLayoutManager(layoutManager1);
                                rvCartBarang.setHasFixedSize(true);
                                rvCartBarang.setItemAnimator(new DefaultItemAnimator());
                                cartSellerAdapter = new CartSellerAdapter(CartActivity.this, listSeller, kursIdr);
                                rvCartBarang.setAdapter(cartSellerAdapter);
                            }
                            cvBottom.setVisibility(View.VISIBLE);
                            tvTotalAll.setText(Currency.getCurrencyFormat().format(totalAll));
                        }
                        else if(status.equals("error")){
                            loadingDialog.hideDialog();
                            noItem.setVisibility(View.VISIBLE);
                            cvBottom.setVisibility(View.INVISIBLE);
                        }
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    loadingDialog.hideDialog();
                    content.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method static yang dapat dipanggil oleh adapter utk menambah harga total*/
    public static void addTotalAll(long hargaPerItem){
        totalAll = (long) (totalAll + (hargaPerItem));
        tvTotalAll.setText(Currency.getCurrencyFormat().format(totalAll));

    }

    /**Method static yang dapat dipanggil oleh adapter utk mengurangi harga total*/
    public static void subTotalAll(long hargaPerItem){
        totalAll = (long) (totalAll - (hargaPerItem));
        tvTotalAll.setText(Currency.getCurrencyFormat().format(totalAll));
    }

    public static void disableCheckout(String status){
        if (status.equals("error")){
            btnCheckout.setEnabled(false);
        }else{
            btnCheckout.setEnabled(true);
        }
    }

    /**Method static yang dapat dipanggil oleh adapter utk mengubahh kuantitas suatu barang pada arraylist*/
    public static void changeQty(int id, int jumlah){
        for(Company company : listSeller){
            for(Cart cart : company.getListCart()){
                if(cart.getId() == id){
                    cart.setQty(jumlah);
                }
            }
        }
    }

    /**Method untuk mengupdate cart pada database*/
    private void updateCart() {
        try{
            //buat string builder query untuk setiap barang pada cart
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE gcm_master_cart SET qty = CASE id ");
            StringBuilder in = new StringBuilder();
            for(int i=0;i<listSeller.size();i++){
                for(int j=0;j<listSeller.get(i).getListCart().size();j++){
                    sb.append(" WHEN "+listSeller.get(i).getListCart().get(j).getId()+" THEN "+listSeller.get(i).getListCart().get(j).getQty());
                    in.append(listSeller.get(i).getListCart().get(j).getId());
                    if(i==listSeller.size()-1 && j==listSeller.get(i).getListCart().size()-1){
                        in.append("");
                    }
                    else{
                        in.append(",");
                    }
                }
            }
            sb.append(" ELSE qty END WHERE id IN(");
            sb.append(in);
            sb.append(");");
            Log.d("", "updateCart: "+sb.toString());
            Call<JsonObject> cartCall = RetrofitClient
                    .getInstance2()
                    .getApi()
                    .requestInsert(new JSONRequest(QueryEncryption.Encrypt(sb.toString())));

            cartCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, Response<JsonObject> response) {
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**Method untuk melanjutkan pesanan ke CheckoutActivity*/
    private void toCheckout(){
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        ArrayList<Company> listSellerToCheckout = new ArrayList<>();

        //dilakukan pengecekan seller atau barang mana saja yang dipilih
        for(Company seller : listSeller){
            //jika checkbox seller checked maka masukkan seller ke array list seller
            if(seller.isChecked()){
                listSellerToCheckout.add(seller);
            }
            //jika tidak, maka ambil barang yang dipilih saja
            else{
                boolean ada = false;
                ArrayList<Cart> listBarangPerSeller = new ArrayList<>();
                for(Cart cart : seller.getListCart()){
                    if(cart.isChecked()){
                        ada = true;
                        listBarangPerSeller.add(cart); //jika barang dipilih maka masukkan ke array list barang
                    }
                }
                //buat object seller baru dengan barang-barang yang dipilih saja, lalu dimasukkan ke array list seller
                if(ada){
                    listSellerToCheckout.add(new Company(seller.getId(), seller.getNamaPerusahaan(), listBarangPerSeller, seller.isChecked()));
                }
            }
        }
        //intent ke CheckoutActivity dengan membawa parameter array list, total harga, dan kurs
        intent.putParcelableArrayListExtra("listSeller", listSellerToCheckout);
        intent.putExtra("total", totalAll);
        //intent.putExtra("kurs", kursIdr);
        startActivity(intent);
    }

    /**Cek apakah seller sudah dimasukkan ke dalam list atau belum*/
    private boolean checkListSeller(int id){
        boolean ada = false;
        for(Company c : listSeller){
            if(c.getId()==id){
                ada = true;
            }
        }
        return ada;
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
////                cartRequest(); //setelah kurs didapatkan maka request data-data cart
////            }
////
////            @Override
////            public void onFailure(Call<JsonObject> call, Throwable t) {
////                Log.d("", "onFailure: "+t.getMessage());
////                content.setVisibility(View.INVISIBLE);
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
//                        cartRequest(); //setelah kurs didapatkan maka request data-data cart
//                    }
//                }
//                Log.d("kursnyaaa", "onResponse: "+kursIdr);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d("", "onFailure: "+t.getMessage());
//                content.setVisibility(View.INVISIBLE);
//                failed.setVisibility(View.VISIBLE);
//            }
//        });
//    }
}
