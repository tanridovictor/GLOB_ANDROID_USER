package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class StatusCompanyAdapter extends RecyclerView.Adapter<StatusCompanyAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Company> listCompany;
    private String status;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNamaPerusahaan;
        private ImageView iconWaiting, iconVerified, iconRejected;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvNamaPerusahaan = itemView.findViewById(R.id.tvNamaPerusahaan);
            iconWaiting = itemView.findViewById(R.id.iconWaiting);
            iconRejected = itemView.findViewById(R.id.iconRejected);
            iconVerified = itemView.findViewById(R.id.iconVerified);
        }
    }

    public StatusCompanyAdapter(Context _context, ArrayList<Company> listCompany, String status) {
        this._context = _context;
        this.listCompany = listCompany;
        this.status = status;
    }

    @NonNull
    @Override
    public StatusCompanyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.status_company_view, parent, false);
        return new StatusCompanyAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final StatusCompanyAdapter.ViewHolder holder, final int position) {
        holder.tvNamaPerusahaan.setText(listCompany.get(position).getNamaPerusahaan());
        //kondisi awal semua icon gone
        holder.iconWaiting.setVisibility(View.GONE);
        holder.iconVerified.setVisibility(View.GONE);
        holder.iconRejected.setVisibility(View.GONE);
        //pemasangan icon sesuai dengan status perusahaan
        if(status.equals("A")){
            holder.iconVerified.setVisibility(View.VISIBLE);
        }
        else if(status.equals("R")){
            holder.iconRejected.setVisibility(View.VISIBLE);
        }
        else if(status.equals("I")){
            holder.iconWaiting.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listCompany.size();
    }
}