package com.enseval.gcmuser.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.NegoAdapter;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Negosiasi;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NegoFragment extends Fragment {

    String TAG = "ido";
    private RecyclerView rvNego;
    private NegoAdapter negoAdapter;
    private ArrayList<Negosiasi> listNego;
    private Barang barang;
    private Negosiasi nego;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ConstraintLayout noNego;
    private ConstraintLayout failed;
    private Button refresh;
    private long lastClickTime=0;
    private static float kursIdr;

    public NegoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nego, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listNego = new ArrayList<>();

        rvNego = view.findViewById(R.id.rvNego);
        noNego = view.findViewById(R.id.noNego);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        failed = view.findViewById(R.id.failed);
        refresh = view.findViewById(R.id.refresh);

        failed.setVisibility(View.INVISIBLE);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(getActivity().getIntent());
                    intent.putExtra("fragment", "negoFragment");
                    getActivity().finish();
                    startActivity(intent);
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });

        noNego.setVisibility(View.INVISIBLE);

        //kursRequest();
        negoRequest();
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
////                negoRequest();
////                Log.d("kursnyaaa", "onResponse: "+kursIdr);
////            }
////
////            @Override
////            public void onFailure(Call<JsonObject> call, Throwable t) {
////                Log.d("", "onFailure: "+t.getMessage());
////                noNego.setVisibility(View.INVISIBLE);
////                shimmerFrameLayout.stopShimmerAnimation();
////                shimmerFrameLayout.setVisibility(View.GONE);
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
//                negoRequest();
//                Log.d("kursnyaaa", "onResponse: "+kursIdr);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d("", "onFailure: "+t.getMessage());
//                noNego.setVisibility(View.INVISIBLE);
//                shimmerFrameLayout.stopShimmerAnimation();
//                shimmerFrameLayout.setVisibility(View.GONE);
//                failed.setVisibility(View.VISIBLE);
//            }
//        });
//    }

    /**Method untuk request list negosiasi*/
    private void negoRequest(){
        try {
            Call<JsonObject> cartCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT a.company_id as id, b.kode_barang, d.kode_seller, e.nama, e.berat, a.barang_id, b.price, f.harga_final, " +
                            "b.price_terendah, (select concat('https://www.glob.co.id/admin/assets/images/product/', d.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, e.category_id, b.company_id, a.nego_count, a.harga_konsumen, a.harga_sales, " +
                            "a.id as id_cart, a.history_nego_id as id_history, a.qty, " +
                            "f.harga_nego, f.harga_nego_2, f.harga_nego_3, f.harga_sales as harga_sales_1, f.harga_sales_2, f.harga_sales_3, case when f.time_respon is null then '2000-01-01' else f.time_respon end, case when f.timestamp_respon is null then '0000000000000' else f.timestamp_respon end, h.nama_perusahaan, g.alias, b.persen_nego_1, b.persen_nego_2, b.persen_nego_3, i.nominal as kurs "+
                            "FROM gcm_master_cart a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang e on b.barang_id=e.id " +
                            "left join gcm_master_user c on c.id=a.update_by left join gcm_master_company d on d.id=b.company_id inner join gcm_master_company h on h.id=b.company_id " +
                            "inner join gcm_history_nego f on a.history_nego_id = f.id inner join gcm_master_satuan g on e.satuan = g.id inner join gcm_listing_kurs i on i.company_id = b.company_id "+
                            "where a.company_id="+ SharedPrefManager.getInstance(getActivity()).getUser().getCompanyId()+
                            " and a.status='A' and nego_count > 0 and now() between i.tgl_start and i.tgl_end order by a.update_date desc;")));

            cartCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        Log.d(TAG, "onResponse: suksesssss");
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "onResponse: Suksessss lagi");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String namaBarang = jsonObject.get("nama").getAsString();
                                int idBarang = jsonObject.get("barang_id").getAsInt();
                                double harga = jsonObject.get("price").getAsDouble();
                                double harga_terendah = jsonObject.get("price_terendah").getAsDouble();
                                String foto = jsonObject.get("foto").getAsString();
                                int categoryId = jsonObject.get("category_id").getAsInt();
                                int companyId = jsonObject.get("company_id").getAsInt();
                                String namaPerusahaan = jsonObject.get("nama_perusahaan").getAsString();
                                String alias = jsonObject.get("alias").getAsString();
                                int berat = jsonObject.get("berat").getAsInt();
                                float persen_nego_1 = jsonObject.get("persen_nego_1").getAsFloat();
                                float persen_nego_2 = jsonObject.get("persen_nego_2").getAsFloat();
                                float persen_nego_3 = jsonObject.get("persen_nego_3").getAsFloat();
                                String time_respon = jsonObject.get("time_respon").getAsString();
                                String timestamp_respon = jsonObject.get("timestamp_respon").getAsString();
                                kursIdr = jsonObject.get("kurs").getAsFloat();
                                String kode_barang = jsonObject.get("kode_barang").getAsString();
                                int harga_final = jsonObject.get("harga_final").getAsInt();
                                String flag_foto = jsonObject.get("flag_foto").getAsString();
                                barang = new Barang(namaBarang, idBarang, harga, harga_terendah, foto, categoryId, companyId, namaPerusahaan, alias, persen_nego_1, persen_nego_2, persen_nego_3, kursIdr, kode_barang, flag_foto);
                                int count = jsonObject.get("nego_count").getAsInt();
                                int hargaKonsumen = jsonObject.get("harga_konsumen").getAsInt();
                                //jika null maka harga_sales dijadikan 0
                                int hargaSales;
                                if(jsonObject.get("harga_sales").toString().equals("null")){
                                    hargaSales = 0;
                                }
                                else{
                                    hargaSales = jsonObject.get("harga_sales").getAsInt();
                                    Log.d(TAG, "onResponse: harga Sales"+hargaSales);
                                }
                                int idHistoryNego = jsonObject.get("id_history").getAsInt();

                                //cek apakah sales sudah respon atau belum menggunakan update by
                                boolean responSales;
                                int idCart = jsonObject.get("id_cart").getAsInt();
                                int qty    = jsonObject.get("qty").getAsInt();
                                int harga_nego, harga_nego_2, harga_nego_3, harga_sales_1, harga_sales_2, harga_sales_3;
                                if (jsonObject.get("harga_nego").toString().equals("null")) { harga_nego = 0; }
                                    else { harga_nego = jsonObject.get("harga_nego").getAsInt(); }
                                if (jsonObject.get("harga_nego_2").toString().equals("null")) { harga_nego_2 = 0; }
                                    else { harga_nego_2 = jsonObject.get("harga_nego_2").getAsInt(); }
                                if (jsonObject.get("harga_nego_3").toString().equals("null")) { harga_nego_3 = 0; }
                                    else { harga_nego_3 = jsonObject.get("harga_nego_3").getAsInt(); }

                                if (jsonObject.get("harga_sales_1").toString().equals("null")) { harga_sales_1 = 0; }
                                    else { harga_sales_1 = jsonObject.get("harga_sales_1").getAsInt(); }
                                if (jsonObject.get("harga_sales_2").toString().equals("null")) { harga_sales_2 = 0; }
                                    else { harga_sales_2 = jsonObject.get("harga_sales_2").getAsInt(); }
                                if (jsonObject.get("harga_sales_3").toString().equals("null")) { harga_sales_3 = 0; }
                                    else { harga_sales_3 = jsonObject.get("harga_sales_3").getAsInt(); }

                                if(jsonObject.get("id").getAsInt() == SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()){
                                    responSales=false;
                                }
                                else {
                                    responSales=true;
                                }

                                Log.d("cekcek ===", String.valueOf(jsonObject.get("id").getAsInt()) + "----" + SharedPrefManager.getInstance(getContext()).getUser().getCompanyId());
                                //buat object nego dan masukkan ke list
                                nego = new Negosiasi(barang, berat, hargaKonsumen, hargaSales, count,
                                        harga_nego, harga_nego_2, harga_nego_3,
                                        harga_sales_1, harga_sales_2, harga_sales_3,
                                        responSales, idCart, idHistoryNego, qty, time_respon, harga_final, timestamp_respon);
                                listNego.add(nego);

                                //buat adapter nego
                                noNego.setVisibility(View.INVISIBLE);
                                shimmerFrameLayout.stopShimmerAnimation();
                                shimmerFrameLayout.setVisibility(View.GONE);
                                RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
                                ((LinearLayoutManager) layoutManager1).setOrientation(LinearLayoutManager.VERTICAL);
                                rvNego.setLayoutManager(layoutManager1);
                                rvNego.setItemAnimator(new DefaultItemAnimator());
                                negoAdapter = new NegoAdapter(getActivity(), listNego, kursIdr);
                                rvNego.setAdapter(negoAdapter);
                            }
                        }
                        else if(status.equals("error")){
                            Log.d(TAG, "onResponse: error");
                            noNego.setVisibility(View.VISIBLE);
                            shimmerFrameLayout.stopShimmerAnimation();
                            shimmerFrameLayout.setVisibility(View.GONE);
                        }
                    }
                    else {
                        Log.d(TAG, "onResponse: "+response);
                        noNego.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.stopShimmerAnimation();
                        shimmerFrameLayout.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    noNego.setVisibility(View.INVISIBLE);
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    failed.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
