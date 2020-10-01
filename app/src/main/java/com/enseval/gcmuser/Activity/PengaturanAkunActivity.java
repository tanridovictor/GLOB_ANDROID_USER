package com.enseval.gcmuser.Activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.AkunBuyerAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Fragment.NewAccountBottomSheetDialog;
import com.enseval.gcmuser.Fragment.ProfileUserFragment;
import com.enseval.gcmuser.Model.AkunBuyer;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PengaturanAkunActivity extends AppCompatActivity {

    private RecyclerView rvListAkunBuyer;
    private ImageView back;
    private TextView tambahAkun;
    private long lastClickTime = 0;
    private AkunBuyerAdapter akunBuyerAdapter;
    private String TAG = "ido";
    private LoadingDialog loadingDialog;
    private ArrayList<AkunBuyer> listAkun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan_akun);

        rvListAkunBuyer = findViewById(R.id.rvAkun);
        back = findViewById(R.id.btnBack);
        tambahAkun = findViewById(R.id.tambahAkun);
        loadingDialog = new LoadingDialog(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getAkunBuyer();

        tambahAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    BottomSheetDialog bottomSheetDialog = new NewAccountBottomSheetDialog(PengaturanAkunActivity.this);
                    bottomSheetDialog.setContentView(R.layout.new_account_bottom_sheet_dialog);
                    bottomSheetDialog.show();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
    }

    private void getAkunBuyer(){
        String query = "select nama, username, no_hp, email, status from gcm_master_user gmu " +
                "where company_id = "+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() +" " +
                "and id != "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId()+"";
        Log.d(TAG, "query getAkunBuyer: "+query);
        try {
            loadingDialog.showDialog();
            Call<JsonObject> callGetAkunBuyer = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetAkunBuyer.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            listAkun = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++) {
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listAkun.add(new AkunBuyer(
                                        jsonObject.get("nama").getAsString(),
                                        jsonObject.get("username").getAsString(),
                                        jsonObject.get("no_hp").getAsString(),
                                        jsonObject.get("email").getAsString(),
                                        jsonObject.get("status").getAsString()
                                ));
                            }
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PengaturanAkunActivity.this);
                            rvListAkunBuyer.setLayoutManager(layoutManager);
                            rvListAkunBuyer.setItemAnimator(new DefaultItemAnimator());
                            akunBuyerAdapter = new AkunBuyerAdapter(getApplicationContext(), listAkun);
                            rvListAkunBuyer.setAdapter(akunBuyerAdapter);
                            loadingDialog.hideDialog();
                        }else{
                            loadingDialog.hideDialog();
                        }
                    }else{
                        loadingDialog.hideDialog();
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
