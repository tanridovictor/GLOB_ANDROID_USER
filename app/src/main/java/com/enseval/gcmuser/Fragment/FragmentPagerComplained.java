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
import com.enseval.gcmuser.Adapter.ComplainedOrderAdapter;
import com.enseval.gcmuser.Model.Order;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPagerComplained extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<Order> orderList;
    private ComplainedOrderAdapter complainedOrderAdapter;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ConstraintLayout noOrder;
    private ConstraintLayout failed;
    private Button refresh;
    private long lastClickTime=0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        recyclerView = view.findViewById(R.id.recyclerView);
        noOrder = view.findViewById(R.id.noOrder);

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
                    intent.putExtra("fragment", "orderFragment");
                    getActivity().finish();
                    startActivity(intent);
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });

        noOrder.setVisibility(View.INVISIBLE);

        orderRequest(); //request informasi transaksi
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

    /**Method untuk request informasi transaksi*/
    private void orderRequest(){
        try {
            Call<JsonObject> orderCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select a.id_transaction, status, to_char(a.create_date, 'dd-MON-YYYY') create_date, to_char(a.date_complained, 'dd-MON-YYYY') date_complained, case when a.ongkos_kirim is null then 0 else a.ongkos_kirim end, sum(harga_final) as harga_final, sum(harga) as total, a.ppn_seller from gcm_master_transaction a inner join gcm_transaction_detail b " +
                            "on a.id_transaction=b.transaction_id where a.company_id="+
                            SharedPrefManager.getInstance(getActivity()).getUser().getCompanyId()+" and status='COMPLAINED' group by a.id_transaction, status, a.create_date, a.date_complained, a.ongkos_kirim, a.ppn_seller " +
                            "order by a.date_complained desc;")));

            orderCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            orderList = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++) {
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                if (jsonObject.get("date_complained").isJsonNull()) {
                                    orderList.add(new Order(
                                            jsonObject.get("id_transaction").getAsString(),
                                            jsonObject.get("status").getAsString(),
                                            jsonObject.get("create_date").getAsString(),
                                            "",
                                            jsonObject.get("total").getAsLong(),
                                            jsonObject.get("ongkos_kirim").getAsDouble(),
                                            jsonObject.get("harga_final").getAsLong(),
                                            jsonObject.get("ppn_seller").getAsFloat()
                                    ));
                                } else {
                                    orderList.add(new Order(
                                            jsonObject.get("id_transaction").getAsString(),
                                            jsonObject.get("status").getAsString(),
                                            jsonObject.get("create_date").getAsString(),
                                            jsonObject.get("date_complained").getAsString(),
                                            jsonObject.get("total").getAsLong(),
                                            jsonObject.get("ongkos_kirim").getAsDouble(),
                                            jsonObject.get("harga_final").getAsLong(),
                                            jsonObject.get("ppn_seller").getAsFloat()
                                    ));
                                }
                            }
                            noOrder.setVisibility(View.INVISIBLE);
                            shimmerFrameLayout.stopShimmerAnimation();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            //buat adapter untuk masing-masing transaksi
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            complainedOrderAdapter = new ComplainedOrderAdapter(getActivity(), orderList);
                            recyclerView.setAdapter(complainedOrderAdapter);
                        }
                        else if(status.equals("error")){
                            shimmerFrameLayout.stopShimmerAnimation();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            noOrder.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    noOrder.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
