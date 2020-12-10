package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<OrderDetail> orderDetailList;
    private String Status;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNama, tvHarga, tvHargaPerKg, tvJumlah, tvNoBatch, tvExpDate, kuantitasDipenuhi, tvQtyPenuhi, tvComplain, tvCatatan;
        private ImageView imgBarang;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvHargaPerKg = itemView.findViewById(R.id.tvHargaPerKg);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            imgBarang = itemView.findViewById(R.id.imageView);
            tvNoBatch = itemView.findViewById(R.id.noBatch);
            tvExpDate = itemView.findViewById(R.id.exp);
            kuantitasDipenuhi = itemView.findViewById(R.id.kuantitasDipenuhi);
            tvQtyPenuhi = itemView.findViewById(R.id.qtypenuhi);
            tvComplain = itemView.findViewById(R.id.complain);
            tvCatatan = itemView.findViewById(R.id.tvCatatan);

        }
    }

    public OrderDetailAdapter(Context _context, ArrayList<OrderDetail> orderDetailList, String Status) {
        this._context = _context;
        this.orderDetailList = orderDetailList;
        this.Status = Status;
    }


    @NonNull
    @Override
    public OrderDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.order_detail_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderDetailAdapter.ViewHolder holder, final int position) {
        Log.d("ido", "cek gambar: "+orderDetailList.get(position).getFotoUrl()+", "+orderDetailList.get(position).getFlag_foto());
        if (orderDetailList.get(position).getFlag_foto().equals("Y")) {
            Glide.with(_context).load(orderDetailList.get(position).getFotoUrl()).into(holder.imgBarang);
        }else{
            Glide.with(_context).load("https://www.glob.co.id/admin/assets/images/no_image.png").into(holder.imgBarang);
        }

        if(Status.equals("menunggu")){
            holder.tvNoBatch.setVisibility(View.GONE);
            holder.tvExpDate.setVisibility(View.GONE);
            holder.tvQtyPenuhi.setVisibility(View.GONE);
            holder.tvComplain.setVisibility(View.GONE);
            holder.kuantitasDipenuhi.setVisibility(View.GONE);
            holder.tvNama.setText(orderDetailList.get(position).getNamaBarang());
            holder.tvHargaPerKg.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga() / (orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())))+"/"+orderDetailList.get(position).getAlias());
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga()));
            holder.tvJumlah.setText(String.format("Kuantitas : %d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvCatatan.setText(orderDetailList.get(position).getNote());
        }else if(Status.equals("diproses")){
            holder.tvComplain.setVisibility(View.GONE);
            holder.tvNama.setText(orderDetailList.get(position).getNamaBarang());
            holder.tvNoBatch.setText("No Batch : "+orderDetailList.get(position).getBatchNumber());
            holder.tvExpDate.setText("Tgl exp : "+orderDetailList.get(position).getExpDate());
            holder.tvQtyPenuhi.setText(String.format("%d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQtyDiterima() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvHargaPerKg.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga() / (orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())))+"/"+orderDetailList.get(position).getAlias());
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga_final()));
            holder.tvJumlah.setText(String.format("Kuantitas : %d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvCatatan.setText(orderDetailList.get(position).getNote());
        }else if(Status.equals("dibatalkan")) {
            holder.tvNoBatch.setVisibility(View.GONE);
            holder.tvExpDate.setVisibility(View.GONE);
            holder.tvQtyPenuhi.setVisibility(View.GONE);
            holder.tvComplain.setVisibility(View.GONE);
            holder.tvNama.setText(orderDetailList.get(position).getNamaBarang());
            holder.tvHargaPerKg.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga() / (orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())))+"/"+orderDetailList.get(position).getAlias());
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga()));
            holder.tvJumlah.setText(String.format("Kuantitas : %d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvCatatan.setText(orderDetailList.get(position).getNote());
        }else if(Status.equals("complain")){
            //detail informasi transaksi
            holder.tvNama.setText(orderDetailList.get(position).getNamaBarang());
            holder.tvComplain.setText("*"+orderDetailList.get(position).getNotes_complain());
            holder.tvComplain.setTextColor(ContextCompat.getColor(_context, R.color.color_warning));
            holder.tvNoBatch.setText("No Batch : "+orderDetailList.get(position).getBatchNumber());
            holder.tvExpDate.setText("Tgl exp : "+orderDetailList.get(position).getExpDate());
            holder.tvQtyPenuhi.setText(String.format("%d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQtyDiterima() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvHargaPerKg.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga() / (orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())))+"/"+orderDetailList.get(position).getAlias());
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga_final()));
            holder.tvJumlah.setText(String.format("Kuantitas : %d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvCatatan.setText(orderDetailList.get(position).getNote());
        }else {
            //detail informasi transaksi
            holder.tvComplain.setVisibility(View.GONE);
            holder.tvNama.setText(orderDetailList.get(position).getNamaBarang());
            holder.tvNoBatch.setText("No Batch : "+orderDetailList.get(position).getBatchNumber());
            holder.tvExpDate.setText("Tgl exp : "+orderDetailList.get(position).getExpDate());
            holder.tvQtyPenuhi.setText(String.format("%d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQtyDiterima() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvHargaPerKg.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga() / (orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())))+"/"+orderDetailList.get(position).getAlias());
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(orderDetailList.get(position).getHarga_final()));
            holder.tvJumlah.setText(String.format("Kuantitas : %d "+orderDetailList.get(position).getAlias(), orderDetailList.get(position).getQty() * Integer.parseInt(orderDetailList.get(position).getBerat())));
            holder.tvCatatan.setText(orderDetailList.get(position).getNote());
        }
//        holder.tvJumlah.setText(String.format("Qty : %d", orderDetailList.get(position).getQty()));
        Log.d("ido", "onBindViewHolder: "+holder.tvHarga.getText().toString());
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }
}
