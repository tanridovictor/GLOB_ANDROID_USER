package com.enseval.gcmuser.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.CheckoutActivity;
import com.enseval.gcmuser.Fragment.CatatanBottomSheetDialog;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Note;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutBarangAdapter extends RecyclerView.Adapter<CheckoutBarangAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Cart> listCheckoutBarang;
    private ArrayList<Note> listNote;
    private float kurs;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNama, tvJumlah, tvHarga;
        TextInputLayout etCatatan;
        Button catatan;
        private ImageView foto;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            foto = itemView.findViewById(R.id.foto);
            catatan = itemView.findViewById(R.id.btnTambahCatatan);
            etCatatan =itemView.findViewById(R.id.etCatatan);
        }
    }

    public CheckoutBarangAdapter(Context _context, ArrayList<Cart> listCheckoutBarang, ArrayList<Note> listNote) {
        this._context = _context;
        this.listCheckoutBarang = listCheckoutBarang;
        this.kurs = kurs;
        this.listNote = listNote;
    }

    @NonNull
    @Override
    public CheckoutBarangAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.checkout_barang_view, parent, false);
        return new CheckoutBarangAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckoutBarangAdapter.ViewHolder holder, final int position) {
        int i = 0;
        while (i < listNote.size()){
            if (listCheckoutBarang.get(position).getId()==listNote.get(i).getId()){
                holder.etCatatan.getEditText().setText(listNote.get(i).getNote());
                Log.d("ido", "idcheckout: "+listCheckoutBarang.get(position).getId()+"=="+listNote.get(i).getId());
                break;
            }
            i++;
        }
        final Barang barang = listCheckoutBarang.get(position).getBarang();
        final int jml = listCheckoutBarang.get(position).getQty();
        final int jmlkg = listCheckoutBarang.get(position).getQty()*Integer.parseInt(listCheckoutBarang.get(position).getBerat());
//        Glide.with(_context)
//                .load(barang.getFoto())
//                .fallback(R.id.shimmer_view_container)
//                .error(R.id.shimmer_view_container)
//                .into(holder.foto);
        Glide.with(_context)
                .load(barang.getFoto())
                .into(holder.foto);
        holder.tvNama.setText(barang.getNama());
//        holder.tvJumlah.setText(String.format("         %d barang", jml));
        holder.tvJumlah.setText(String.format("Kuantitas : %d "+barang.getAlias(), jmlkg));
        Log.d("aprilcheckoutjumlah", String.valueOf(jml));
        if (listCheckoutBarang.get(position).getNegoCount() > 0){
//            holder.tvHarga.setText(Currency.getCurrencyFormat().format(getHarga(barang)));
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(getHarga(barang) *
                    listCheckoutBarang.get(position).getQty() * Integer.parseInt(listCheckoutBarang.get(position).getBerat())));
        }else {
//            holder.tvHarga.setText(Currency.getCurrencyFormat().format(getHarga(barang) * listCheckoutBarang.get(position).getQty()));
            holder.tvHarga.setText(Currency.getCurrencyFormat().format(getHarga(barang) *
                    listCheckoutBarang.get(position).getQty() * Integer.parseInt(listCheckoutBarang.get(position).getBerat())));
        }
        holder.catatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               Bundle bundle = new Bundle();
//               bundle.putInt("idCart", listCheckoutBarang.get(position).getId());
//                BottomSheetDialogFragment bottomSheetDialogFragment = new CatatanBottomSheetDialog();
//                bottomSheetDialogFragment.setArguments(bundle);
//                bottomSheetDialogFragment.show(((FragmentActivity)_context).getSupportFragmentManager(),"tes");
                tambahCatatan(listCheckoutBarang.get(position).getId(), holder.etCatatan.getEditText().getText().toString());
                holder.etCatatan.clearFocus();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listCheckoutBarang.size();
    }

    private void tambahCatatan(int id, String catatan){
        String query = "update gcm_master_cart set note = '"+catatan+"' where id = "+id;
        Log.d("ido", "tambahCatatan: "+query);
        try {
            Call<JsonObject> callTambahCatatan = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callTambahCatatan.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d("ido", "update catatan: Sukses");
                    }else{
                        Log.d("ido", "update catatan: Gagal");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**Method untuk mendapatkan harga barang*/
    private long getHarga(Barang barang){
        long harga=0;
        for(Cart cart : listCheckoutBarang){
            if(barang.getId()==cart.getBarang().getId()){
                if(cart.getNegoCount()>0){
//                    harga = cart.getHargaSales();
                    if (cart.getHargaKonsumen() == cart.getHargaSales()){
                        harga = cart.getHargaSales() / (Integer.parseInt(cart.getBerat()) * cart.getQty());
                    } else {
                        harga = (long) Math.ceil(cart.getBarang().getHarga()*cart.getBarang().getKursIdr());
                    }
                }
                else {
                    harga = (long) Math.ceil(cart.getBarang().getHarga()*cart.getBarang().getKursIdr());
                }
            }
        }
        return harga;
    }
}
