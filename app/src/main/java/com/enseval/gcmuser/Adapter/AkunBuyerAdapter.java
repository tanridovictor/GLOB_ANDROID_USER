package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.AkunBuyer;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class AkunBuyerAdapter extends RecyclerView.Adapter<AkunBuyerAdapter.ViewHolder> {

    private Context _context;
    private LoadingDialog loadingDialog;
    String TAG = "ido";
    private long lastClickTime = 0;
    private ArrayList<AkunBuyer> listAkun;

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView namaLengkap, username, nomorHP, email, status;
        private CardView cvAkun;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            namaLengkap = itemView.findViewById(R.id.namaLengkap);
            username = itemView.findViewById(R.id.username);
            nomorHP = itemView.findViewById(R.id.nomorHandphone);
            email = itemView.findViewById(R.id.email);
            status = itemView.findViewById(R.id.status);
        }
    }

    public AkunBuyerAdapter(Context _context, ArrayList<AkunBuyer> listAkun){
        this._context = _context;
        this.listAkun = listAkun;
    }

    @NonNull
    @Override
    public AkunBuyerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.list_akun_adapter, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AkunBuyerAdapter.ViewHolder holder, int position) {
        holder.namaLengkap.setText(": "+listAkun.get(position).getNama());
        holder.username.setText(": "+listAkun.get(position).getUsername());
        holder.nomorHP.setText(": "+listAkun.get(position).getNo_hp());
        holder.email.setText(": "+listAkun.get(position).getEmail());
        if (listAkun.get(position).getStatus().equals("A")) {
            holder.status.setText(": Aktif");
        }else{
            holder.status.setText(": Tidak Aktif");
        }
    }

    @Override
    public int getItemCount() {
        return listAkun.size();
    }
}
