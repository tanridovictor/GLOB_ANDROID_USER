package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enseval.gcmuser.Activity.DistributorActivity;
import com.enseval.gcmuser.Activity.RegisterActivity;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class SellerListAdapter extends RecyclerView.Adapter<SellerListAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Company> sellerList;
    private ArrayList<Company> listSeller;
    private long lastClickTime=0;
    private String status;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNama;
        private CheckBox checkBox;
        private LinearLayout linear;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvNama = itemView.findViewById(R.id.tvNama);
            linear = itemView.findViewById(R.id.linear);
        }
    }

    public SellerListAdapter(Context _context, ArrayList<Company> sellerList, String status) {
        this._context = _context;
        this.sellerList = sellerList;
        this.status = status;
    }

    @NonNull
    @Override
    public SellerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.seller_list_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SellerListAdapter.ViewHolder holder, final int position) {
        //tampilkan list seller/distributor yang tersedia
        holder.checkBox.setChecked(sellerList.get(position).isChecked());
        holder.linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (status.equals("register")) {
                    RegisterActivity.setChecked(position, isChecked);
                }else if (status.equals("profile_distributor")){
                    DistributorActivity.setChecked(position, isChecked);
                }
                if (isChecked){
                    holder.checkBox.setSelected(true);
                }
            }
        });
        holder.tvNama.setText(sellerList.get(position).getNamaPerusahaan());
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }
}
