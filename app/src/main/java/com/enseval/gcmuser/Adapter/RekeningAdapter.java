package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enseval.gcmuser.Model.Rekening;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class RekeningAdapter extends RecyclerView.Adapter<RekeningAdapter.ViewHolder> {

    private Context _context;
    private ArrayList<Rekening> listRekening;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rekening, noRek, namaRek;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rekening = itemView.findViewById(R.id.rekening);
            noRek = itemView.findViewById(R.id.nomorRek);
            namaRek = itemView.findViewById(R.id.namaRek);
        }
    }

    public RekeningAdapter(Context _context, ArrayList<Rekening> listRekening) {
        this._context = _context;
        this.listRekening = listRekening;
    }

    @NonNull
    @Override
    public RekeningAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.list_rekening, parent, false);
        return new RekeningAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RekeningAdapter.ViewHolder holder, int position) {
        holder.rekening.setText(listRekening.get(position).getNama());
        holder.noRek.setText("No Rek : "+listRekening.get(position).getNo_rekening());
        holder.namaRek.setText("Nama Rek : "+listRekening.get(position).getPemilik_rekening());
    }

    @Override
    public int getItemCount() {
        return listRekening.size();
    }
}
