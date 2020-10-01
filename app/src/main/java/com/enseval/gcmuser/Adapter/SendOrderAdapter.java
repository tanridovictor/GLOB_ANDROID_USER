package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Fragment.OrderBottomSheetDialogFragment;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.Model.Order;
import com.enseval.gcmuser.R;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendOrderAdapter extends RecyclerView.Adapter<SendOrderAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Order> orderList;
    private long lastClickTime=0;
    private LoadingDialog loadingDialog;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvID, tvStatus, tvTotal, tvDate, btnDetail;
        private CardView btnTerima;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnTerima = itemView.findViewById(R.id.btnTerimaBarang);
        }
    }

    public SendOrderAdapter(Context _context, ArrayList<Order> orderList) {
        this._context = _context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public SendOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.send_order_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SendOrderAdapter.ViewHolder holder, final int position) {
        holder.tvID.setText(orderList.get(position).getTransactionId());
        holder.tvDate.setText(orderList.get(position).getUpdateDate());
        holder.tvTotal.setText(Currency.getCurrencyFormat().format(orderList.get(position).getHarga_final()+orderList.get(position).getOngkir()+orderList.get(position).getHarga_final()*(orderList.get(position).getPpn_seller()/100)));
        holder.tvStatus.setText("Sedang dikirim");

        //jika detail ditekan, buka OrderBottomSheetDialogFragment
        holder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("status", "dikirim");
                bundle.putString("transactionId", orderList.get(position).getTransactionId());
                bundle.putLong("total", orderList.get(position).getHarga_final());
                bundle.putDouble("ongkir", orderList.get(position).getOngkir());
                bundle.putFloat("ppn",orderList.get(position).getPpn_seller());
                BottomSheetDialogFragment bottomSheetDialogFragment = new OrderBottomSheetDialogFragment();
                bottomSheetDialogFragment.setArguments(bundle);
                bottomSheetDialogFragment.show(((FragmentActivity)_context).getSupportFragmentManager(), "tes");
            }
        });

        holder.btnTerima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(_context);
                bottomSheetDialog.setContentView(R.layout.konfirmasi_bottom_sheet_dialog);

                Button negative = bottomSheetDialog.findViewById(R.id.negative);
                Button positive = bottomSheetDialog.findViewById(R.id.positive);
                TextView message = bottomSheetDialog.findViewById(R.id.tvMessage);

                message.setText("Apakah barang telah anda terima?");

                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                negative.setText("Belum");
                positive.setText("Ya");

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                bottomSheetDialog.dismiss();
                                loadingDialog = new LoadingDialog(_context);
                                loadingDialog.showDialog();
                                Call<JsonObject> terimaBarangCall = RetrofitClient
                                        .getInstance2()
                                        .getApi()
                                        .requestInsert(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_transaction SET status='RECEIVED', date_received=now() WHERE " +
                                                "id_transaction='"+orderList.get(position).getTransactionId()+"';")));

                                terimaBarangCall.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        if(response.isSuccessful()){
                                            String status = response.body().getAsJsonObject().get("status").getAsString();
                                            if(status.equals("success")){
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context, "Status pesanan telah diperbarui", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(_context, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                _context.startActivity(intent);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        loadingDialog.hideDialog();
                                        Toast.makeText(_context, "Koneksi gagal", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            lastClickTime= SystemClock.elapsedRealtime();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                bottomSheetDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
