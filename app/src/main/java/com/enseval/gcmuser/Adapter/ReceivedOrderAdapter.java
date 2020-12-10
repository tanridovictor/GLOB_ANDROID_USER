package com.enseval.gcmuser.Adapter;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.DetailOrderActivity;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Fragment.ComplainBottomSheetDialogFragment;
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

public class ReceivedOrderAdapter extends RecyclerView.Adapter<ReceivedOrderAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Order> orderList;
    private long lastClickTime=0;
    private LoadingDialog loadingDialog;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvID, tvStatus, tvTotal, tvDate, tvDate2;
        private CardView btnDetail;
        private Button btnKomplain, btnSelesai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDate2 = itemView.findViewById(R.id.tvDate2);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnKomplain = itemView.findViewById(R.id.btnKomplain);
            btnSelesai = itemView.findViewById(R.id.btnSelesai);
        }
    }

    public ReceivedOrderAdapter(Context _context, ArrayList<Order> orderList) {
        this._context = _context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ReceivedOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.received_order_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReceivedOrderAdapter.ViewHolder holder, final int position) {
        holder.tvID.setText(String.format(orderList.get(position).getTransactionId()));
        holder.tvDate.setText(orderList.get(position).getCreateDate());
        holder.tvDate2.setText(orderList.get(position).getUpdateDate());
        holder.tvTotal.setText(Currency.getCurrencyFormat().format(orderList.get(position).getHarga_final()+orderList.get(position).getOngkir()+orderList.get(position).getHarga_final()*(orderList.get(position).getPpn_seller()/100)));
        holder.tvStatus.setText("Telah diterima");

        //jika detail ditekan, buka OrderBottomSheetDialogFragment
        holder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_context, DetailOrderActivity.class);
                i.putExtra("status", "diterima");
                i.putExtra("transactionId", orderList.get(position).getTransactionId());
                i.putExtra("total", orderList.get(position).getHarga_final());
                i.putExtra("ongkir", orderList.get(position).getOngkir());
                i.putExtra("ppn", orderList.get(position).getPpn_seller());
                _context.startActivity(i);

//                Bundle bundle = new Bundle();
//                bundle.putString("status", "diterima");
//                bundle.putString("transactionId", orderList.get(position).getTransactionId());
//                bundle.putLong("total", orderList.get(position).getHarga_final());
//                bundle.putDouble("ongkir", orderList.get(position).getOngkir());
//                bundle.putFloat("ppn", orderList.get(position).getPpn_seller());
//                BottomSheetDialogFragment bottomSheetDialogFragment = new OrderBottomSheetDialogFragment();
//                bottomSheetDialogFragment.setArguments(bundle);
//                bottomSheetDialogFragment.show(((FragmentActivity)_context).getSupportFragmentManager(), "tes");
            }
        });

        //jika button complain ditekan, buka ComplainBottomSheetDialogFragment
        holder.btnKomplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("transactionId", orderList.get(position).getTransactionId());
                BottomSheetDialogFragment bottomSheetDialogFragment = new ComplainBottomSheetDialogFragment();
                bottomSheetDialogFragment.setArguments(bundle);
                bottomSheetDialogFragment.show(((FragmentActivity)_context).getSupportFragmentManager(), "tes");
            }
        });

        //jika button selesai ditekan
        holder.btnSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(_context);
                dialog.setContentView(R.layout.dialog_handle);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ImageView image = dialog.findViewById(R.id.iconImage);
                TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                Button btnSetuju = dialog.findViewById(R.id.btnYa);
                TextView title = dialog.findViewById(R.id.judul);
                TextView description = dialog.findViewById(R.id.isi);
                dialog.setCancelable(false);

                title.setText("Transaksi");
                description.setText("Selesaikan pesanan sekarang? \nKetika pesanan telah diselesaikan, segala bentuk komplain tidak akan diterima.");
                image.setImageResource(R.drawable.done);
                btnBatal.setText("BATAL");
                btnSetuju.setText("SETUJU");

                btnBatal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btnSetuju.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                dialog.dismiss();
                                loadingDialog = new LoadingDialog(_context);
                                loadingDialog.showDialog();
                                Call<JsonObject> terimaBarangCall = RetrofitClient
                                        .getInstance()
                                        .getApi()
                                        .request(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_transaction SET status='FINISHED', date_finished=now() WHERE " +
                                                "id_transaction='"+orderList.get(position).getTransactionId()+"' returning id;")));

                                terimaBarangCall.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        if(response.isSuccessful()){
                                            String status = response.body().getAsJsonObject().get("status").getAsString();
                                            if(status.equals("success")){
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context, "Status pesanan telah diperbarui", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(_context, MainActivity.class);
                                                intent.putExtra("fragment", "orderFragment");
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

                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

}
