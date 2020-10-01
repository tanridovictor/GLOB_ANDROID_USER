package com.enseval.gcmuser.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.StatusTipeAdapter;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.StatusCompany;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatusBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView rvTipeStatus;
    private ArrayList<StatusCompany> listStatus;
    private ArrayList<Company> listCompany;
    private StatusTipeAdapter statusTipeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.status_bottom_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTipeStatus = view.findViewById(R.id.rvStatus);

        requestStatus(); //request status perusahaan
    }

    /**Method untuk request status perusahaan pada setiap seller*/
    private void requestStatus(){
        try {
            Call<JsonObject> statusCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT b.id, b.nama_perusahaan, a.status FROM gcm_company_listing a inner join gcm_master_company b on a.seller_id=b.id " +
                            "where buyer_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+"")));

            statusCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            listStatus = new ArrayList<>();
                            listCompany = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                String statusPerusahaan = jsonArray.get(i).getAsJsonObject().get("status").getAsString();
                                String namaPerusahaan = jsonArray.get(i).getAsJsonObject().get("nama_perusahaan").getAsString();
                                int sellerId = jsonArray.get(i).getAsJsonObject().get("id").getAsInt();
                                Company company = new Company(sellerId, namaPerusahaan);
                                //grouping seller berdasarkan statusnya
                                if(!checkListStatus(statusPerusahaan)){ //kondisi jika status belum ada
                                    ArrayList<Company> temp = new ArrayList<Company>();
                                    temp.add(company);
                                    listStatus.add(new StatusCompany(statusPerusahaan, temp)); //tambahkan status dan perusahaan dengan status tersebut
                                }
                                else{ //kondisi jika sttaus sudah ada
                                    for(StatusCompany s : listStatus){
                                        if(s.getStatus().equals(statusPerusahaan)){
                                            s.addCompany(company); //tambahkan seller ke array list seller dalam status tersebut (cek method addCompany di model StatusCompany)
                                        }
                                    }
                                }
                            }
                        }
                        //buat adapter untuk setiap status, nanti didalamnya ada lg adapter utk sellernya
                        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
                        rvTipeStatus.setLayoutManager(layoutManager1);
                        rvTipeStatus.setItemAnimator(new DefaultItemAnimator());
                        statusTipeAdapter = new StatusTipeAdapter(getContext(), listStatus);
                        rvTipeStatus.setAdapter(statusTipeAdapter);
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

    /**Method untuk mengecek apakah status sudah ada*/
    private boolean checkListStatus(String status){
        boolean ada = false;
        for(StatusCompany s : listStatus){
            if(s.getStatus().equals(status)){
                ada = true;
            }
        }
        return ada;
    }
}