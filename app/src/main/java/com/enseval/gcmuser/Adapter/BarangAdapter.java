package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.Activity.DetailBarangActivity;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class BarangAdapter extends RecyclerView.Adapter<BarangAdapter.ViewHolder>{
    private Context _context;
    private ArrayList<Barang> barangList;
    private long lastClickTime=0;
    private float kursIdr;
    private String checkNego;
    private ArrayMap<Integer, String> listSellerStatus;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNamaBarang,tvHargaBarang, tvPenjual;
        private final ImageView imgBarang;
        private CardView cardBarang;
        private String status="I";

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaBarang = (TextView) itemView.findViewById(R.id.namaBarang);
            tvHargaBarang =(TextView) itemView.findViewById(R.id.harga);
            tvPenjual = (TextView) itemView.findViewById(R.id.penjual);
            imgBarang = (ImageView) itemView.findViewById(R.id.imgBarang);
            cardBarang = (CardView) itemView.findViewById(R.id.cardBarang);
        }
    }

    public BarangAdapter(Context _context, ArrayList<Barang> barangList, ArrayMap<Integer, String> listSellerStatus) {
        this._context = _context;
        this.barangList = barangList;
        this.listSellerStatus = listSellerStatus;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.barang_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final BarangAdapter.ViewHolder holder, final int position) {
        final Barang barang = barangList.get(position);
        holder.tvNamaBarang.setText(barang.getNama());
        kursIdr = barang.getKursIdr();
        double harga = (double) Math.ceil(barang.getHarga()*kursIdr);
        holder.tvPenjual.setText(barang.getNamaPerusahaan());

        NumberFormat hargaa = new DecimalFormat("#.##");

        //pada kondisi default harga barang tidak tersedia
        holder.tvHargaBarang.setText("Tidak tersedia");

        //pengecekan status user pada masing-masing seller
        if(listSellerStatus.size()>0){
            for(int i=0; i<listSellerStatus.size(); i++){
                //kondisional jika status=A maka harga barang ditampilkan.
                if(barang.getCompanyId()==listSellerStatus.keyAt(i) && listSellerStatus.valueAt(i).equals("A")){
                    holder.status="A"; //mengubah nilai status utk holder ini menjadi A
                    holder.tvHargaBarang.setText(Currency.getCurrencyFormat().format(harga)+"/"+barang.getAlias());
                }
            }
        }
        
        //load gambar barang
//        Glide.with(_context).load("https://www.glob.co.id/admin/assets/images/product/"+barangList.get(position).getKode_seller()+"/"+barangList.get(position).getKode_barang()+".png").into(holder.imgBarang);
        if (barang.getFlag_foto().equals("Y")){
            Glide.with(_context).load(barang.getFoto()).into(holder.imgBarang);
        }else{
            Glide.with(_context).load("https://www.glob.co.id/admin/assets/images/no_image.png").into(holder.imgBarang);
        }

        //jika card ditekan maka akan pindah ke activity DetailBarangActivity
        //dengan passing parameter id barang dan status user pada seller ini
        holder.cardBarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    if (barangList.get(position).getHarga_terendah() != barangList.get(position).getHarga()){
                        checkNego = "nego";
                    }else{
                        checkNego = "tidakNego";
                    }
                    Intent intent = new Intent(_context, DetailBarangActivity.class);
                    intent.putExtra("id", barang.getId());
                    intent.putExtra("status",holder.status);
                    intent.putExtra("kursIdr", kursIdr);
                    intent.putExtra("checkNego", checkNego);
                    Log.d("idbarang", "onClick: "+barang.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    _context.startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
        //jika ditekan lama maka hanya ditampilkan nama barang dalam bentuk toast saja
        holder.cardBarang.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(_context, barang.getNama(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return barangList.size();
    }
}
