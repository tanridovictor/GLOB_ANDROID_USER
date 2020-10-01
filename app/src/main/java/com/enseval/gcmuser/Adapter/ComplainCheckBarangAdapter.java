package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.Fragment.ComplainBottomSheetDialogFragment;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;

public class ComplainCheckBarangAdapter extends RecyclerView.Adapter<ComplainCheckBarangAdapter.ViewHolder> {
    private Context _context;
    private ArrayMap<OrderDetail, Boolean> orderDetailList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNama, tvJumlah;
        private ImageView imgBarang;
        private CheckBox checkBox;
        private ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            imgBarang = itemView.findViewById(R.id.imageView);
            checkBox = itemView.findViewById(R.id.checkBox2);
            constraintLayout = itemView.findViewById(R.id.constraintlayout);
        }
    }

    public ComplainCheckBarangAdapter(Context _context, ArrayMap<OrderDetail, Boolean> orderDetailList) {
        this._context = _context;
        this.orderDetailList = orderDetailList;
    }


    @NonNull
    @Override
    public ComplainCheckBarangAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.complain_pilih_barang_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ComplainCheckBarangAdapter.ViewHolder holder, final int position) {
        Glide.with(_context).load(orderDetailList.keyAt(position).getFotoUrl()).into(holder.imgBarang);
        holder.tvNama.setText(orderDetailList.keyAt(position).getNamaBarang());
        holder.tvJumlah.setText(String.format("Diterima %d dari %d", orderDetailList.keyAt(position).getQtyDiterima(), orderDetailList.keyAt(position).getQty()));

        holder.checkBox.setChecked(orderDetailList.valueAt(position));

        //jika pesanan ditekan, ubah checekd value checkbox
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });

        //jika checked value pada checkbox berubah, ubah nilai isChecked pada model
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ComplainBottomSheetDialogFragment.setChecked(position, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }
}