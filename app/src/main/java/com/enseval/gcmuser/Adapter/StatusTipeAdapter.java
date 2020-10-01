package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.StatusCompany;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class StatusTipeAdapter extends RecyclerView.Adapter<StatusTipeAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<StatusCompany> listStatus;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStatus;
        private ArrayList<Company> listCompanyPerStatus;
        private RecyclerView rvStatusCompany;
        private StatusCompanyAdapter statusCompanyAdapter;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvTipeStatus);
            rvStatusCompany = itemView.findViewById(R.id.rvStatusCompany);
        }
    }

    public StatusTipeAdapter(Context _context, ArrayList<StatusCompany> listStatus) {
        this._context = _context;
        this.listStatus = listStatus;
    }

    @NonNull
    @Override
    public StatusTipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.status_jenis_view, parent, false);
        return new StatusTipeAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final StatusTipeAdapter.ViewHolder holder, final int position) {
        //keterangan status
        if(listStatus.get(position).getStatus().equals("A")){
            holder.tvStatus.setText("Telah diverifikasi:");
        }
        else if(listStatus.get(position).getStatus().equals("I")){
            holder.tvStatus.setText("Menunggu verifikasi:");
        }
        else if(listStatus.get(position).getStatus().equals("R")){
            holder.tvStatus.setText("Ditolak:");
        }

        //buat adapter untuk list seller dengan status tersebut
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(_context);
        holder.rvStatusCompany.setLayoutManager(layoutManager1);
        holder.rvStatusCompany.setItemAnimator(new DefaultItemAnimator());
        holder.listCompanyPerStatus = listStatus.get(position).getListCompany();
        holder.statusCompanyAdapter = new StatusCompanyAdapter(_context, holder.listCompanyPerStatus, listStatus.get(position).getStatus());
        holder.rvStatusCompany.setAdapter(holder.statusCompanyAdapter);
    }

    @Override
    public int getItemCount() {
        if (listStatus != null) {
            return listStatus.size();
        }
        return 0;
    }
}

