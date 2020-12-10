package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.AkunBuyer;
import com.enseval.gcmuser.Model.Distributor;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class DistributorAdapter extends RecyclerView.Adapter<DistributorAdapter.ViewHolder> {

    private Context _context;
    private LoadingDialog loadingDialog;
    String TAG = "ido";
    private long lastClickTime = 0;
    private ArrayList<Distributor> listDistributor;

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView namaPerusahaan, status;
        private CardView cvDistributor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            namaPerusahaan = itemView.findViewById(R.id.namaPerusahaan);
            status = itemView.findViewById(R.id.status);
        }
    }

    public DistributorAdapter(Context _context, ArrayList<Distributor> listDistributor){
        this._context = _context;
        this.listDistributor = listDistributor;
    }

    @NonNull
    @Override
    public DistributorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.adapter_distributor, parent, false);
        return new DistributorAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DistributorAdapter.ViewHolder holder, int position) {
        holder.namaPerusahaan.setText(listDistributor.get(position).getNama_perusahaan());
        if (listDistributor.get(position).getStatus().equals("A")){
            holder.status.setText("Diverivikasi");
            holder.status.setTextColor(Color.rgb(40, 180, 99));
        }else if (listDistributor.get(position).getStatus().equals("I")){
            holder.status.setText("Menunggu Verivikasi");
            holder.status.setTextColor(Color.rgb(241, 196, 15));
        }else if (listDistributor.get(position).getStatus().equals("R")){
            holder.status.setText("Dinonaktifkan");
            holder.status.setTextColor(Color.rgb(255, 0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return listDistributor.size();
    }
}
