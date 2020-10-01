package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.AkunBuyerAdapter;
import com.enseval.gcmuser.Adapter.DistributorAdapter;
import com.enseval.gcmuser.Adapter.SellerListAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Alamat;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.Distributor;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class DistributorActivity extends AppCompatActivity {
    private String TAG = "ido";
    private LoadingDialog loadingDialog;
    private ConstraintLayout failed;
    private Button refresh;
    private ImageView back;
    private long lastClickTime = 0;
    private RecyclerView rvDistributor;
    private DistributorAdapter distributorAdapter;
    private ArrayList<Distributor> listDistributor;
    private static ArrayList<Company> listCompany;
    private static ArrayList<Alamat> listAlamat;
    TextView tambahDistributor;
    private SellerListAdapter sellerListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor);

        failed = findViewById(R.id.failed);
        refresh = findViewById(R.id.refresh);
        back = findViewById(R.id.btnBack);
        loadingDialog = new LoadingDialog(this);
        rvDistributor = findViewById(R.id.rvDistributor);
        tambahDistributor = findViewById(R.id.tambahDistributor);

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

        failed.setVisibility(GONE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }else {
                    finish();
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        tambahDistributor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSeller();

            }
        });

        getDistributor();

    }

    private void getDistributor(){
        String query = "SELECT b.id, b.nama_perusahaan, a.status FROM gcm_company_listing a inner join gcm_master_company b on a.seller_id=b.id " +
                "where buyer_id = "+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() +";";
        try {
            loadingDialog.showDialog();
            Call<JsonObject> callGetDistributor = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetDistributor.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            listDistributor = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listDistributor.add(new Distributor(
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("nama_perusahaan").getAsString(),
                                        jsonObject.get("status").getAsString()
                                ));
                            }
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DistributorActivity.this);
                            rvDistributor.setLayoutManager(layoutManager);
                            rvDistributor.setItemAnimator(new DefaultItemAnimator());
                            distributorAdapter = new DistributorAdapter(getApplicationContext(), listDistributor);
                            rvDistributor.setAdapter(distributorAdapter);

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
                    failed.setVisibility(View.VISIBLE);
                    rvDistributor.setVisibility(GONE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getSeller(){
        String query = "";
        if (SharedPrefManager.getInstance(getApplicationContext()).getUser().getTipeBisnis()==1){
            query = "select id, nama_perusahaan from gcm_master_company " +
                    "where id not in (" + SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller() + ") and type ='S' and seller_status='A'";
        }else{
            query = "select id, nama_perusahaan from gcm_master_company where tipe_bisnis in " +
                    "((select tipe_bisnis from gcm_master_company where id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+"),1) and " +
                    "id not in ("+SharedPrefManager.getInstance(getApplicationContext()).getActiveSeller()+") and type ='S' and seller_status='A'";
        }
        Log.d(TAG, "getSeller: "+query);
        try{
            Call<JsonObject> callGetSeller = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetSeller.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            listCompany = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listCompany.add(new Company(
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("nama_perusahaan").getAsString()
                                ));
                            }
                            if (listCompany.size()>0) {
                                showSellerDialog();
                            }else{
                                noShowSellerDialog();
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

    private void showSellerDialog(){
        BottomSheetDialog dialog = new BottomSheetDialog(DistributorActivity.this);
        dialog.setContentView(R.layout.pilih_seller_dialog);
        RecyclerView rvSellerList = dialog.findViewById(R.id.recyclerView);
        Button registerBtn = dialog.findViewById(R.id.registerBtn);
        TextView titleStatusListNull = dialog.findViewById(R.id.tvDescription);

        titleStatusListNull.setText("Silahkan pilih distributor di bawah ini : ");
        registerBtn.setText("Berlangganan");

        String status = "profile_distributor";

        rvSellerList.setLayoutManager(new LinearLayoutManager(this));
        rvSellerList.setItemAnimator(new DefaultItemAnimator());
        sellerListAdapter = new SellerListAdapter(this, listCompany, status);
        rvSellerList.setAdapter(sellerListAdapter);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Company> CompanyList = new ArrayList<>();
                for (int i=0; i<listCompany.size(); i++){
                    if (listCompany.get(i).isChecked()) {
                        CompanyList.add(listCompany.get(i));
                        Log.d(TAG, "listcompany set checked: " + listCompany.get(i));
                    }
                }
                getAlamat(CompanyList);

            }
        });

        dialog.show();
    }

    private void noShowSellerDialog(){

    }

    /**Method untuk mengubah checked value seller*/
    public static void setChecked(int position, boolean isChecked){
        listCompany.get(position).setChecked(isChecked);
    }

    private void insertBerlangganan(ArrayList<Company> CompanyList, ArrayList<Alamat> AlamatList){
        int companyId = SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId();
        String query = "insert into gcm_company_listing (buyer_id, seller_id, buyer_number_mapping, " +
                "seller_number_mapping, status, create_date, update_date, blacklist_by, is_blacklist, " +
                "id_blacklist, notes_blacklist) values ";
        String loop1 = "";
        String loop2 = "";
        for (int i=0; i<CompanyList.size(); i++){
            loop1 = loop1 + "("+companyId+", "+CompanyList.get(i).getId()+", null, null, 'I', now(), now(), null, false, 0, '')";
            if (i<CompanyList.size()-1){
                loop1 = loop1.concat(",");
            }
        }
        String queryResult1 = query.concat(loop1);
        Log.d(TAG, "insertBerlangganan: "+queryResult1);
        String query_list_alamat = "insert into gcm_listing_alamat (id_master_alamat, id_buyer, " +
                "id_seller, kode_shipto_customer, kode_billto_customer) values ";

        for (int i=0; i<CompanyList.size(); i++){
            for (int j=0; j<AlamatList.size(); j++){
                loop2 = loop2 + "(" + AlamatList.get(j).getId() + ", " + SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() + ", " +
                        "" + CompanyList.get(i).getId() + ", null, null)";
                if (j < AlamatList.size() - 1) {
                    loop2 = loop2.concat(",");
                }
            }
            if (i < CompanyList.size() - 1) {
                loop2 = loop2.concat(",");
            }
        }

        String query_result2 = query_list_alamat.concat(loop2);
        Log.d(TAG, "insertBerlangganan: "+query_result2);
        String final_query = "with new_insert as (" + queryResult1 + ")" + query_result2 + " returning id";
        Log.d(TAG, "insertBerlangganan: "+final_query);

        try {
            Log.d(TAG, "insertBerlangganan: "+QueryEncryption.Encrypt(final_query));
            Call<JsonObject> callInsertDistributor = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(final_query)));
            callInsertDistributor.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "cek respon insert distributor baru: SUKSES");
                        Intent i = new Intent(getApplicationContext(), DistributorActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        Log.d(TAG, "cek respon insert distributor baru: GAGAL");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "cek respon insert distributor baru: GAGAL PARAH");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getAlamat(final ArrayList<Company> CompanyList){
        String query = "select * from gcm_master_alamat gma where company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" " +
                "and flag_active = 'A'";
        Log.d(TAG, "getAlamat: "+query);
        try {
            Call<JsonObject> callAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            listAlamat = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listAlamat.add(new Alamat(
                                        jsonObject.get("kelurahan").getAsString(),
                                        jsonObject.get("kecamatan").getAsString(),
                                        jsonObject.get("kota").getAsString(),
                                        jsonObject.get("provinsi").getAsString(),
                                        jsonObject.get("kodepos").getAsString(),
                                        jsonObject.get("alamat").getAsString(),
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("shipto_active").getAsString(),
                                        jsonObject.get("billto_active").getAsString()
                                ));
                            }
                            insertBerlangganan(CompanyList, listAlamat);
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
}
