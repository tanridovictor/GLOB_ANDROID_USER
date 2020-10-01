package com.enseval.gcmuser.Fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.OrderViewPagerAdapter;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private OrderViewPagerAdapter adapter;
    private LoadingDialog loadingDialog;

    public OrderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog(getContext());

        loadingDialog.showDialog();
        jumlahWaiting();

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new OrderViewPagerAdapter(getChildFragmentManager());

        //penambahan sub-fragment untuk tiap-tiap status (cek method addFragment di OrderViewPagerAdapter)

//        adapter.addFragment(new FragmentPagerOngoing(), "Diproses"); //sub-fragment untuk transaksi ongoing
//        adapter.addFragment(new FragmentPagerSend(), "Dikirim");//belom dibuat
//        adapter.addFragment(new FragmentPagerReceived(), "Diterima"); //sub-fragment untuk transaksi received
//        adapter.addFragment(new FragmentPagerComplained(), "Dikomplain"); //sub-fragment untuk transaksi complained
//        adapter.addFragment(new FragmentPagerFinished(), "Selesai"); //sub-fragment untuk transaksi finished
//        adapter.addFragment(new FragmentPagerCanceled(), "Dibatalkan");


        Log.d("", "onViewCreated: tes pertama");
        //checkReceived();
    }

    private void jumlahWaiting(){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'WAITING'";
        try {
            Call<JsonObject> callJumlahWaiting = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahWaiting.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahWaiting;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahWaiting = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            jumlahOngoing(jumlahWaiting);
                        }
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

    private void jumlahShipped(final int jumlahWaiting, final int jumlahOngoing){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'SHIPPED'";
        try {
            Call<JsonObject> callJumlahShipped = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahShipped.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahShipped;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahShipped= jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            jumlahReceived(jumlahWaiting, jumlahOngoing, jumlahShipped);
                        }
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

    private void jumlahReceived(final int jumlahWaiting, final int jumlahOngoing, final int jumlahShipped){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'RECEIVED'";
        try {
            Call<JsonObject> callJumlahReceived= RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahReceived.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahReceived;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahReceived = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            jumlahComplained(jumlahWaiting, jumlahOngoing, jumlahShipped, jumlahReceived);
                        }
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

    private void jumlahComplained(final int jumlahWaiting, final int jumlahOngoing, final int jumlahShipped, final int jumlahReceived){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'COMPLAINED'";
        try {
            Call<JsonObject> callJumlahComplained = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahComplained.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahComplained;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahComplained = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            jumlahFinished(jumlahWaiting, jumlahOngoing, jumlahShipped, jumlahReceived, jumlahComplained);
                        }
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

    private void jumlahFinished(final int jumlahWaiting, final int jumlahOngoing, final int jumlahShipped, final int jumlahReceived, final int jumlahComplained){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'FINISHED'";
        try {
            Call<JsonObject> callJumlahFinished = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahFinished.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahFinished;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahFinished = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            jumlahCanceled(jumlahWaiting, jumlahOngoing, jumlahShipped, jumlahReceived, jumlahComplained, jumlahFinished);
                        }
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

    private void jumlahCanceled(final int jumlahWaiting, final int jumlahOngoing, final int jumlahShipped, final int jumlahReceived, final int jumlahComplained, final int jumlahFinished){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'CANCELED'";
        try {
            Call<JsonObject> callJumlahCanceled= RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahCanceled.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahCanceled;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahCanceled = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            adapter.addFragment(new FragmentPagerWaiting(), "Menunggu ("+jumlahWaiting+")"); //sub-fragment untuk transaksi waiting
                            adapter.addFragment(new FragmentPagerOngoing(), "Diproses ("+jumlahOngoing+")"); //sub-fragment untuk transaksi ongoing
                            adapter.addFragment(new FragmentPagerSend(), "Dikirim ("+jumlahShipped+")");//belom dibuat
                            adapter.addFragment(new FragmentPagerReceived(), "Diterima ("+jumlahReceived+")"); //sub-fragment untuk transaksi received
                            adapter.addFragment(new FragmentPagerComplained(), "Dikomplain ("+jumlahComplained+")"); //sub-fragment untuk transaksi complained
                            adapter.addFragment(new FragmentPagerFinished(), "Selesai ("+jumlahFinished+")"); //sub-fragment untuk transaksi finished
                            adapter.addFragment(new FragmentPagerCanceled(), "Dibatalkan ("+jumlahCanceled+")");

                            viewPager.setOffscreenPageLimit(5);
                            viewPager.setAdapter(adapter);
                            tabLayout.setupWithViewPager(viewPager);
                            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

                            loadingDialog.hideDialog();
                        }
                        loadingDialog.hideDialog();
                    }
                    loadingDialog.hideDialog();
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

    private void jumlahOngoing(final int jumlahWaiting){
        String query = "select count(id) as jumlah from gcm_master_transaction gmt where company_id = "+ SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +" and status = 'ONGOING'";
        try {
            Call<JsonObject> callJumlahOngoing = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callJumlahOngoing.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    int jumlahOngoing;
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            jumlahOngoing = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();

                            jumlahShipped(jumlahWaiting, jumlahOngoing);
                        }
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

}
