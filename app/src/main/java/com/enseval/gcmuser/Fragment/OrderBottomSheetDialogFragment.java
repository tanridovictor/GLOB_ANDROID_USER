package com.enseval.gcmuser.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.OrderDetailAdapter;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.enseval.gcmuser.Utilities.Currency;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**Untuk detail pesanan pada transaksi*/
public class OrderBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private String transactionId, Status;
    private RecyclerView recyclerView;
    private OrderDetailAdapter orderDetailAdapter;
    private ArrayList<OrderDetail> orderDetailList;
    private TextView idTransaksi, status, penjual, tglPermintaanKirim, shipTo, billTo, payment, tvTotalharga, tvOngkosKirim, tvHargaTotal, tvKetDibatalkan, tvPPN, tvTotalPPN;
    private long totalHarga, hargaTotal, hargaPPN;
    private double ongkosKirim;
    private float ppn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_details_bottom_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionId = getArguments().getString("transactionId");
        hargaTotal = getArguments().getLong("total");
        ongkosKirim = getArguments().getDouble("ongkir");
        Status = getArguments().getString("status");
        ppn = getArguments().getFloat("ppn");


        recyclerView = view.findViewById(R.id.rvOrderDetails);
        idTransaksi = view.findViewById(R.id.idTransaksi);
        status = view.findViewById(R.id.status);
        penjual = view.findViewById(R.id.penjual);
        tglPermintaanKirim = view.findViewById(R.id.permintaanKirim);
        shipTo = view.findViewById(R.id.shipTo);
        billTo = view.findViewById(R.id.billTo);
        payment = view.findViewById(R.id.payment);
        tvHargaTotal = view.findViewById(R.id.totalHarga);
        tvOngkosKirim = view.findViewById(R.id.ongkosKirim);
        tvTotalharga = view.findViewById(R.id.total);
        tvKetDibatalkan = view.findViewById(R.id.ketDibatalkan);
        tvPPN = view.findViewById(R.id.tvPpntotalHarga);
        tvTotalPPN = view.findViewById(R.id.ppntotalHarga);


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
        }else if(Status.equals("diproses")){
            status.setText("Diproses");
            tvKetDibatalkan.setVisibility(View.GONE);
        }else if(Status.equals("dikirim")){
            status.setText("Dikirim");
            tvKetDibatalkan.setVisibility(View.GONE);
        }else if(Status.equals("diterima")){
            status.setText("Diterima");
            tvKetDibatalkan.setVisibility(View.GONE);
        }else if(Status.equals("dibatalkan")){
            status.setText("Dibatalkan");
            getKetDibatalkan();
        }else if(Status.equals("complain")){
            status.setText("Dikomplain");
            tvKetDibatalkan.setVisibility(View.GONE);
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
    }

    /**Method untuk request detail pesanan*/
    private void orderDetailRequest(){
//        String query = "select a.id, a.transaction_id, c.nama, f.nama_perusahaan, (select concat('https://www.glob.co.id/admin/assets/images/product/', f.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, a.qty, a.harga, c.berat, d.alias, to_char(e.tgl_permintaan_kirim, 'dd-MON-YYYY') tgl_permintaan_kirim, a.batch_number, a.exp_date, a.qty_dipenuhi, a.harga_final , e.shipto_id, e.billto_id, e.payment_id, case when note is null then '' else note end "+
//                "from gcm_transaction_detail a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang c on c.id = b.barang_id "+
//                "inner join gcm_master_satuan d on d.id = c.satuan inner join gcm_master_transaction e on e.id_transaction = a.transaction_id inner join gcm_master_company f on b.company_id = f.id "+
//                "where a.transaction_id= '"+transactionId+"' order by c.category_id asc, c.nama asc";
//        Log.d("ido", "orderDetailRequest: "+query);
//        try {
//            Call<JsonObject> orderDetailCall = RetrofitClient
//                    .getInstance()
//                    .getApi()
//                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
//
//            orderDetailCall.enqueue(new Callback<JsonObject>() {
//                @Override
//                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                    if(response.isSuccessful()){
//                        Log.d("ido", "onResponse: sukses nembak");
//                        String status = response.body().getAsJsonObject().get("status").getAsString();
//                        if(status.equals("success")){
//                            Log.d("ido", "onResponse: sukses get data");
//                            orderDetailList = new ArrayList<>();
//                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
//                            for(int i=0; i<jsonArray.size(); i++) {
//                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
//                                if(jsonObject.get("batch_number").isJsonNull() && jsonObject.get("exp_date").isJsonNull() && jsonObject.get("qty_dipenuhi").isJsonNull() && jsonObject.get("harga_final").isJsonNull()){
//                                    Log.d("ido", "onResponse: pertama");
//                                    orderDetailList.add(new OrderDetail(
//                                            jsonObject.get("id").getAsInt(),
//                                            jsonObject.get("transaction_id").getAsString(),
//                                            jsonObject.get("nama").getAsString(),
//                                            jsonObject.get("foto").getAsString(),
//                                            jsonObject.get("qty").getAsInt(),
//                                            jsonObject.get("harga").getAsLong(),
//                                            jsonObject.get("berat").getAsString(),
//                                            jsonObject.get("alias").getAsString(),
//                                            jsonObject.get("shipto_id").getAsInt(),
//                                            jsonObject.get("billto_id").getAsInt(),
//                                            jsonObject.get("payment_id").getAsInt(),
//                                            "",
//                                            "",
//                                            0,
//                                            0,
//                                            jsonObject.get("note").getAsString(),
//                                            jsonObject.get("flag_foto").getAsString()
//                                    ));
//                                }else{
//                                    Log.d("ido", "onResponse: kedua");
//                                    orderDetailList.add(new OrderDetail(
//                                            jsonObject.get("id").getAsInt(),
//                                            jsonObject.get("transaction_id").getAsString(),
//                                            jsonObject.get("nama").getAsString(),
//                                            jsonObject.get("foto").getAsString(),
//                                            jsonObject.get("qty").getAsInt(),
//                                            jsonObject.get("harga").getAsLong(),
//                                            jsonObject.get("berat").getAsString(),
//                                            jsonObject.get("alias").getAsString(),
//                                            jsonObject.get("shipto_id").getAsInt(),
//                                            jsonObject.get("billto_id").getAsInt(),
//                                            jsonObject.get("payment_id").getAsInt(),
//                                            jsonObject.get("batch_number").getAsString(),
//                                            jsonObject.get("exp_date").getAsString(),
//                                            jsonObject.get("qty_dipenuhi").getAsInt(),
//                                            jsonObject.get("harga_final").getAsInt(),
//                                            jsonObject.get("note").getAsString(),
//                                            jsonObject.get("flag_foto").getAsString()
//                                    ));
//                                }
//                                penjual.setText(jsonArray.get(0).getAsJsonObject().get("nama_perusahaan").getAsString());
//                                if(jsonArray.get(0).getAsJsonObject().get("tgl_permintaan_kirim").isJsonNull()){
//                                    tglPermintaanKirim.setText("-");
//                                }else{
//                                    tglPermintaanKirim.setText(jsonArray.get(0).getAsJsonObject().get("tgl_permintaan_kirim").getAsString());
//                                }
//                            }
//                        }
//                        //buat adapter untuk masing-masing barang pesanan
//                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//                        recyclerView.setItemAnimator(new DefaultItemAnimator());
//                        orderDetailAdapter = new OrderDetailAdapter(getActivity(), orderDetailList, Status);
//                        recyclerView.setAdapter(orderDetailAdapter);
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<JsonObject> call, Throwable t) {
//                    Log.d("", "onFailure: "+t.getMessage());
//                    Log.d("ido", "onFailure: gagal");
//                    orderDetailRequest();
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
                            shipTo.setText(Alamat +"\n"+ Kelurahan.toLowerCase() +", "+ Kecamatan.toLowerCase() +"\n"+ Kota.toLowerCase() +"\n"+ Provinsi +", "+Kodepos);
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
                            billTo.setText(Alamat +"\n"+ Kelurahan.toLowerCase() +", "+ Kecamatan.toLowerCase() +"\n"+ Kota.toLowerCase() +"\n"+ Provinsi +", "+Kodepos);
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
                            tvKetDibatalkan.setText("Keterangan : "+jsonArray.get(0).getAsJsonObject().get("cancel_reason").getAsString());
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
        String query = "select a.id, a.transaction_id, c.nama, f.nama_perusahaan, (select concat('https://www.glob.co.id/admin/assets/images/product/', f.id,'/',b.kode_barang,'.png')) as foto, case when b.flag_foto is null then '' else b.flag_foto end as flag_foto, a.qty, a.harga, c.berat, d.alias, to_char(e.tgl_permintaan_kirim, 'dd-Mon-YYYY') tgl_permintaan_kirim, a.batch_number, a.exp_date, a.qty_dipenuhi, a.harga_final, g.notes_complain, case when note is null then '' else note end " +
                "from gcm_transaction_detail a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang c on c.id = b.barang_id " +
                "inner join gcm_master_satuan d on d.id = c.satuan inner join gcm_master_transaction e on e.id_transaction = a.transaction_id " +
                "inner join gcm_master_company f on b.company_id = f.id inner join gcm_transaction_complain g on g.detail_transaction_id = a.id " +
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
                            }
                        }
                        //buat adapter untuk masing-masing barang pesanan
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        orderDetailAdapter = new OrderDetailAdapter(getActivity(), orderDetailList, Status);
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
}
