package com.enseval.gcmuser.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.ComplainActivity;
import com.enseval.gcmuser.Adapter.ComplainCheckBarangAdapter;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplainBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private String transactionId;
    private RecyclerView recyclerView;
    private static ArrayMap<OrderDetail, Boolean> orderDetailList;
    private ComplainCheckBarangAdapter complainCheckBarangAdapter;
    private static CheckBox pilihSemua;
    private Button btnLanjut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.complain_bottom_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionId = getArguments().getString("transactionId");

        recyclerView = view.findViewById(R.id.recyclerView);
        pilihSemua = view.findViewById(R.id.pilihSemua);
        btnLanjut = view.findViewById(R.id.btnLanjut);

        //buat adapter untuk pesanan yang akan dipilih, cek ComplainCheckBarangAdapter
        orderDetailList = new ArrayMap<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        complainCheckBarangAdapter = new ComplainCheckBarangAdapter(getContext(), orderDetailList);
        recyclerView.setAdapter(complainCheckBarangAdapter);

        transactionRequest();

        //jika checkbox untuk pilih semua ditekan
        pilihSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("", "onClick: "+pilihSemua.isChecked());
                //jika kondisinya checked, ubah semua pesanan didalamnya menjadi checked
                if(pilihSemua.isChecked()){
                    for(int i=0; i<orderDetailList.size(); i++){
                        setChecked(i, true); //mengubah checked value dari pesanan tersebut
                        complainCheckBarangAdapter.notifyDataSetChanged();
                    }
                }
                //jika kondisinya unchecked, ubah semua pesanan didalamnya menjadi unchecked
                else{
                    for(int i=0; i<orderDetailList.size(); i++){
                        setChecked(i, false); //mengubah checked value dari pesanan tersebut
                        complainCheckBarangAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        btnLanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bikin list baru, masukkin cuma yang dipilih aja
                ArrayList<OrderDetail> complainList = new ArrayList<>();
                for(int i=0; i<orderDetailList.size(); i++){
                    if(orderDetailList.valueAt(i)){
                        complainList.add(orderDetailList.keyAt(i));
                    }
                }
                //intent ke ComplainActivity
                Intent intent = new Intent(getContext(), ComplainActivity.class);
                intent.putExtra("transactionId", transactionId);
                intent.putParcelableArrayListExtra("complainList", complainList);
                intent.putExtra("from", "complain");
                getContext().startActivity(intent);
            }
        });
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        complainCheckBarangAdapter.notifyDataSetChanged();
//    }


    @Override
    public void onResume() {
        super.onResume();
        pilihSemua.setChecked(false);
        for(int i=0; i<orderDetailList.size(); i++){
            orderDetailList.setValueAt(i, false);
        }
        complainCheckBarangAdapter.notifyDataSetChanged();
        for(int i=0; i<orderDetailList.size(); i++){
            Log.d("", "onResume: "+orderDetailList.valueAt(i)+" di "+i);
        }
        complainCheckBarangAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    /**Method untuk request data pesanan*/
    private void transactionRequest(){
        try {
            Call<JsonObject> orderDetailCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select a.id, a.transaction_id, c.nama, b.foto, a.qty, c.berat, a.harga, a.batch_number, a.exp_date, a.qty as qty_diterima, e.alias from gcm_transaction_detail a inner join \n" +
                            "gcm_list_barang b on a.barang_id=b.id inner join gcm_master_barang c on b.barang_id=c.id inner join gcm_master_satuan e on c.satuan=e.id inner join gcm_master_transaction d on d.id_transaction = a.transaction_id \n" +
                            "where a.transaction_id='"+transactionId+"' and d.status ='RECEIVED' order by c.category_id asc, c.nama asc;")));

            orderDetailCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                if(jsonObject.get("exp_date").isJsonNull()){
                                    orderDetailList.put(new OrderDetail(
                                            jsonObject.get("id").getAsInt(),
                                            jsonObject.get("transaction_id").getAsString(),
                                            jsonObject.get("nama").getAsString(),
                                            jsonObject.get("foto").getAsString(),
                                            jsonObject.get("qty").getAsInt(),
                                            jsonObject.get("berat").getAsString(),
                                            jsonObject.get("harga").getAsInt(),
                                            jsonObject.get("batch_number").getAsString(),
                                            "",
                                            jsonObject.get("qty_diterima").getAsInt(),
                                            jsonObject.get("alias").getAsString()
                                    ), false);
                                }
                                else{
                                    orderDetailList.put(new OrderDetail(
                                            jsonObject.get("id").getAsInt(),
                                            jsonObject.get("transaction_id").getAsString(),
                                            jsonObject.get("nama").getAsString(),
                                            jsonObject.get("foto").getAsString(),
                                            jsonObject.get("qty").getAsInt(),
                                            jsonObject.get("berat").getAsString(),
                                            jsonObject.get("harga").getAsInt(),
                                            jsonObject.get("batch_number").getAsString(),
                                            jsonObject.get("exp_date").getAsString(),
                                            jsonObject.get("qty_diterima").getAsInt(),
                                            jsonObject.get("alias").getAsString()
                                    ), false);
                                }
                                Log.d("nama", "onResponse: "+jsonObject.get("nama").getAsString());
                            }
                        }
                        complainCheckBarangAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    transactionRequest();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk mengubah checked value suatu pesanan lalu mengecek apakah semua barang di checked*/
    public static void setChecked(int position, boolean isChecked){
        boolean allChecked = true; //flag untuk menandakan apakah semuanya di checked
        orderDetailList.setValueAt(position, isChecked);
        Log.d("", "setChecked: "+orderDetailList.size());
        for(int i=0; i<orderDetailList.size(); i++){
            if(!orderDetailList.valueAt(i)){
                allChecked=false; //jika ada barang yg ga di checked
            }
            Log.d("cekvalue", "setChecked: "+orderDetailList.valueAt(i)+" pada "+i);
        }
        //jika semua cheked maka checkbox utama juga checked. jika ada yg ga checked maka jadi unchecked
        pilihSemua.setChecked(allChecked);
    }
}
