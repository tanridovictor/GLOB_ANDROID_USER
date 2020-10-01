package com.enseval.gcmuser.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.PaymentAdapter;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.Payment;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static ArrayList<Payment> listPayment;
    private ArrayList<Company> listCheckoutCompany;
    private ArrayList<Cart> listCart;

    private RecyclerView rvPayment;
    private PaymentAdapter adapter;
    String TAG = "ido";
    private int seller_id;
    private long total;
    private float kursIdr;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        rvPayment = findViewById(R.id.rvPembayaran);
        back = findViewById(R.id.btnBack);

        seller_id=getIntent().getIntExtra("seller", 0);
        listCart = getIntent().getParcelableArrayListExtra("arridcart");
        listCheckoutCompany = getIntent().getParcelableArrayListExtra("listSeller");
        total = getIntent().getLongExtra("total",0);
        //kursIdr = getIntent().getFloatExtra("kurs",0);

        paymentRequest();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void paymentRequest(){
        try {
            Call<JsonObject> paymentCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT a.id, c.payment_name, c.deskripsi FROM gcm_payment_listing a " +
                            "inner join gcm_seller_payment_listing b on a.payment_id = b.id " +
                            "inner join gcm_master_payment c on b.payment_id = c.id "+
                            "WHERE a.seller_id="+seller_id+
                            " and a.buyer_id="+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+
                            " and a.status='A' and b.status='A';")));
            paymentCall.enqueue(new Callback<JsonObject>(){
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status =  response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            listPayment = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                listPayment.add(new Payment(
                                        jsonObject.get("id").getAsInt(),
                                        jsonObject.get("payment_name").getAsString(),
                                        jsonObject.get("deskripsi").getAsString()
                                ));
                            }
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PaymentActivity.this);
                            rvPayment.setLayoutManager(layoutManager);
                            rvPayment.setItemAnimator(new DefaultItemAnimator());
                            adapter = new PaymentAdapter(PaymentActivity.this, listPayment, listCart,
                                    listCheckoutCompany, total);
                            rvPayment.setAdapter(adapter);
                        }
                    }else{
                        Log.d(TAG, "onResponse: gagal cuyyy");
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

}
