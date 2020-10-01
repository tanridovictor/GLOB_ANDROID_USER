package com.enseval.gcmuser.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enseval.gcmuser.Activity.CartActivity;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Fragment.NegoFragment;
import com.enseval.gcmuser.Model.Notifikasi;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class NotifikasiAdapter extends RecyclerView.Adapter<NotifikasiAdapter.ViewHolder> {

    private Context _context;
    private LoadingDialog loadingDialog;
    String TAG = "ido";
    private long lastClickTime = 0;
    private ArrayList<Notifikasi> listNotif;

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvJudulNotif, tvNamaBarang, tvNamaSeller, tvWaktu;
        private CardView cvNotif;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudulNotif = itemView.findViewById(R.id.judulNotifikasi);
            tvNamaBarang = itemView.findViewById(R.id.namaBarang);
            tvNamaSeller = itemView.findViewById(R.id.namaSeller);
            tvWaktu = itemView.findViewById(R.id.tanggalNotif);
            cvNotif = itemView.findViewById(R.id.cvnotif);
        }
    }

    public NotifikasiAdapter(Context _context, ArrayList<Notifikasi> listNotif){
        this._context = _context;
        this.listNotif = listNotif;
    }

    @NonNull
    @Override
    public NotifikasiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.list_notifikasi_adapter, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifikasiAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: "+listNotif.get(position).getBarang_nama());
        if (listNotif.get(position).getStatus().equals("nego")) {
            holder.tvJudulNotif.setText("Balasan Negosiasi");
            holder.tvNamaBarang.setText(listNotif.get(position).getBarang_nama());
            holder.tvNamaSeller.setText(listNotif.get(position).getSeller_nama());
            holder.tvWaktu.setText(listNotif.get(position).getDate());
        }else if (listNotif.get(position).getStatus().equals("approve")){
            holder.tvJudulNotif.setText("Negosiasi berhasil disepakati");
            holder.tvNamaBarang.setText(listNotif.get(position).getBarang_nama());
            holder.tvNamaSeller.setText(listNotif.get(position).getSeller_nama());
            holder.tvWaktu.setText(listNotif.get(position).getDate());
        }

        holder.cvNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_context, MainActivity.class);
                i.putExtra("fragment", "negoFragment");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                _context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listNotif.size();
    }
}
