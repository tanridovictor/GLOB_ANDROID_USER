package com.enseval.gcmuser.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.CartActivity;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Barang;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartBarangAdapter extends RecyclerView.Adapter<CartBarangAdapter.ViewHolder>{
    private Context _context;
    private ArrayList<Cart> listCart;
    private LoadingDialog loadingDialog;
    private long lastClickTime=0;
    private float kurs;
    private CartSellerAdapter.ViewHolder sellerHolder;
    private int sellerPosition;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNama, tvHarga, tvTotal, tvStatus;
        private CardView cardBarang;
        private ConstraintLayout constCartBarang;
        private ImageView plus, minus, imgBarang, btnDelete;
        private final EditText etJumlah;
        private CheckBox checkBox;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvNama = (TextView) itemView.findViewById(R.id.tvNama);
            etJumlah = (EditText) itemView.findViewById(R.id.etJumlah);
            plus = (ImageView) itemView.findViewById(R.id.plus);
            minus = (ImageView) itemView.findViewById(R.id.minus);
            btnDelete = (ImageView) itemView.findViewById(R.id.btnDelete);
            constCartBarang = (ConstraintLayout) itemView.findViewById(R.id.constCartBarang);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            imgBarang = itemView.findViewById(R.id.imgBarang);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            checkBox = itemView.findViewById(R.id.checkBox);
            checkBox.setVisibility(View.GONE);
        }
    }

    public CartBarangAdapter(Context _context, ArrayList<Cart> listCart, float kurs, CartSellerAdapter.ViewHolder sellerHolder, int sellerPosition) {
        this._context = _context;
        this.listCart = listCart;
        //this.kurs = kurs;
        this.sellerHolder = sellerHolder;
        this.sellerPosition = sellerPosition;
    }

    @NonNull
    @Override
    public CartBarangAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.cart_barang_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartBarangAdapter.ViewHolder holder, final int position) {
        kurs = listCart.get(position).getBarang().getKursIdr();
        holder.tvStatus.setVisibility(View.GONE);
        final Barang barang = listCart.get(position).getBarang();
//        final int jml = listCart.get(position).getQty();
        final int jml = Integer.parseInt(listCart.get(position).getBerat()) * listCart.get(position).getQty();
        final int[] jumlah = {jml};

        holder.checkBox.setChecked(listCart.get(position).isChecked());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(holder.checkBox.isChecked());
            }
        });

        //kondisi jika checked value berubah
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("", "onClick: "+isChecked);
                listCart.get(position).setChecked(isChecked); //mengubah value checked barang di listcart
                CartSellerAdapter.setChecked(sellerHolder, sellerPosition); //memanggil method untuk sinkronisasi checked seller dan barang
                //pengubahan total harga barang yang dipilih
                if(isChecked){
                    CartActivity.addTotalAll(getHarga(barang) * jumlah[0]);
                    Log.d("aprilwoiaddtotalall", String.valueOf(getHarga(barang)*jumlah[0]));
                }
                else {
                    CartActivity.subTotalAll(getHarga(barang) * jumlah[0]);
                    Log.d("aprilsubtotalall", String.valueOf(getHarga(barang)*jumlah[0]));
                }
            }
        });

        //jika barang sudah nego maka kuantitas tidak bisa diubah lagi
        if(listCart.get(position).getNegoCount()>0){
            holder.plus.setEnabled(false);
            holder.plus.setClickable(false);
            holder.plus.setColorFilter(ContextCompat.getColor(_context,R.color.divider));
            holder.minus.setEnabled(false);
            holder.minus.setClickable(false);
            holder.minus.setColorFilter(ContextCompat.getColor(_context,R.color.divider));
            holder.etJumlah.setEnabled(false);
            holder.etJumlah.setSelected(false);
            holder.etJumlah.setClickable(false);
        }
        else{
            holder.plus.setEnabled(true);
            holder.plus.clearColorFilter();
            holder.minus.setEnabled(true);
            holder.minus.clearColorFilter();
            holder.etJumlah.setEnabled(true);
            holder.etJumlah.setSelected(true);
            holder.etJumlah.setClickable(true);
        }

        if (barang.getFlag_foto().equals("Y")) {
            Glide.with(_context).load(barang.getFoto()).into(holder.imgBarang);
        }else{
            Glide.with(_context).load("https://www.glob.co.id/admin/assets/images/no_image.png").into(holder.imgBarang);
        }
        holder.tvNama.setText(barang.getNama());

        //informasi status negosiasi
        if(listCart.get(position).getNegoCount()>0){
            if(listCart.get(position).getHargaKonsumen()==listCart.get(position).getHargaSales()){
                holder.tvStatus.setText("Negosiasi selesai");
                holder.tvStatus.setVisibility(View.VISIBLE);
            }
            else {
                holder.tvStatus.setText("Lihat proses negosiasi >>");
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                            return;
                        }
                        else {
                            Intent intent = new Intent(_context, MainActivity.class);
                            intent.putExtra("fragment", "negoFragment");
                            _context.startActivity(intent);
                            ((Activity)_context).finish();
                        }
                        lastClickTime = SystemClock.elapsedRealtime();
                    }
                });
            }
        }
        //kondisi belum pernah nego
        else {
            holder.tvStatus.setText("Nego harga >>");
            holder.tvStatus.setVisibility(View.GONE);
            holder.tvStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                        return;
                    }
                    else {
                        final Dialog dialog = new Dialog(_context);
                        dialog.setContentView(R.layout.nego_dialog);
                        TextView namaBarang = dialog.findViewById(R.id.tvNama);
                        TextView hargaBarang = dialog.findViewById(R.id.tvHarga);
                        ImageView jatahTerpakai1 = dialog.findViewById(R.id.jatahTerpakai1);
                        ImageView jatahTerpakai2 = dialog.findViewById(R.id.jatahTerpakai2);
                        ImageView jatahTerpakai3 = dialog.findViewById(R.id.jatahTerpakai3);
                        jatahTerpakai1.setVisibility(View.GONE);
                        jatahTerpakai2.setVisibility(View.GONE);
                        jatahTerpakai3.setVisibility(View.GONE);

                        final TextInputLayout tvHargaNego = dialog.findViewById(R.id.hargaNego);
                        final Button btnNego = dialog.findViewById(R.id.btnNego);

                        btnNego.setEnabled(false);

                        namaBarang.setText(listCart.get(position).getBarang().getNama());
                        hargaBarang.setText(Currency.getCurrencyFormat().format(listCart.get(position).getBarang().getHarga()*kurs));

                        //textwatcher untuk handle input harga nego
                        tvHargaNego.getEditText().addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!TextUtils.isEmpty(tvHargaNego.getEditText().getText())){
                                    int input = Integer.parseInt(tvHargaNego.getEditText().getText().toString());
                                    if(input>barang.getHarga()*kurs){
                                        tvHargaNego.setErrorEnabled(true);
                                        tvHargaNego.setError("Harga nego melebihi harga barang");
                                        btnNego.setEnabled(false);
                                    }
                                    else{
                                        tvHargaNego.setErrorEnabled(false);
                                        btnNego.setEnabled(true);
                                    }
                                }
                                else{
                                    btnNego.setEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });

                        //kondisi jika button nego ditekan
                        btnNego.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                        return;
                                    }
                                    else {
                                        loadingDialog = new LoadingDialog(_context);
                                        loadingDialog.showDialog();

                                        //request untuk update cart
                                        Call<JsonObject> negoCall = RetrofitClient
                                                .getInstance2()
                                                .getApi()
                                                .requestInsert(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_cart SET harga_konsumen="
                                                        +Integer.parseInt(tvHargaNego.getEditText().getText().toString()) +", nego_count=nego_count+1, update_date=now() where company_id=" +
                                                        SharedPrefManager.getInstance(_context).getUser().getCompanyId()+"and barang_id="+
                                                        barang.getId()+" RETURNING id;")));

                                        negoCall.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                if(response.isSuccessful()){
                                                    String status = response.body().getAsJsonObject().get("status").getAsString();
                                                    if(status.equals("success")){
                                                        loadingDialog.hideDialog();
                                                        Toast.makeText(_context,"Negosiasi berhasil dilakukan", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                        ((CartActivity)_context).finish();
                                                        ((CartActivity)_context).overridePendingTransition(0, 0);
                                                        ((CartActivity)_context).startActivity(((CartActivity)_context).getIntent());
                                                        ((CartActivity)_context).overridePendingTransition(0, 0);
                                                    }
                                                    else if(status.equals("error")){
                                                        loadingDialog.hideDialog();
                                                        Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                Log.d("", "onFailure: "+t.getMessage());
                                                Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    lastClickTime=SystemClock.elapsedRealtime();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        dialog.show();
                    }
                    lastClickTime=SystemClock.elapsedRealtime();
                }
            });
        }

        holder.tvHarga.setText(Currency.getCurrencyFormat().format(getHarga(barang)) + "/" + barang.getAlias());
//        holder.etJumlah.setText(String.format("%d", jml));
        holder.etJumlah.setText(String.format("%d", Math.round(listCart.get(position).getBarang().getMinBeli())));//edit

        if (listCart.get(position).getNegoCount()>0){
            holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang) * jumlah[0]));
        }else {
//            holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang)));
            holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang) * listCart.get(position).getQty() * Integer.parseInt(listCart.get(position).getBerat())));
            Log.d("harga", "Total: "+getHarga(barang)+"*"+listCart.get(position).getQty()+"*"+Integer.parseInt(listCart.get(position).getBerat()));
        }

        //jika jumlah = 1 maka tidak bisa minus
//        if(jumlah[0]==1){
//            holder.minus.setVisibility(View.INVISIBLE);
//        }
//        else{
//            holder.minus.setVisibility(View.VISIBLE);
//        }
//        holder.plus.setVisibility(View.VISIBLE);
//        holder.etJumlah.setText(String.format("%d", jumlah[0]));
        if(jumlah[0]==Math.round(listCart.get(position).getBarang().getMinBeli())){
            holder.minus.setVisibility(View.INVISIBLE);
        }
        else{
            holder.minus.setVisibility(View.VISIBLE);
        }
        holder.plus.setVisibility(View.VISIBLE);
        holder.etJumlah.setText(String.format("%d", jumlah[0]));

        //kondisi jika button plus ditekan
        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                jumlah[0] += 1;
                jumlah[0] += Integer.parseInt(listCart.get(position).getBerat());
                holder.etJumlah.setError(null);
                holder.minus.setVisibility(View.VISIBLE);
                holder.etJumlah.setText(String.format("%d", jumlah[0]));
                holder.etJumlah.clearFocus();
                holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang) * jumlah[0])); //update harga barang
                CartActivity.addTotalAll(getHarga(barang) * Integer.parseInt(listCart.get(position).getBerat())); //ubah total harga seluruhnya
                CartActivity.changeQty(listCart.get(position).getId(), jumlah[0] / Integer.parseInt(listCart.get(position).getBerat())); //ubah quantity dari barang pada array list
            }
        });

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.etJumlah.clearFocus();
//                if(jumlah[0] >1){
//                    jumlah[0] -= 1;
//                    holder.etJumlah.setText(String.format("%d", jumlah[0]));
//                    holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang) * jumlah[0])); //update harga barang
//                    CartActivity.subTotalAll(getHarga(barang)); //ubah total harga seluruhnya
//                    CartActivity.changeQty(listCart.get(position).getId(), jumlah[0]); //ubah quantity dari barang pada array list
//                    if(jumlah[0] ==1){
//                        holder.minus.setVisibility(View.INVISIBLE);
//                    }
//                }
                if(jumlah[0] >Integer.parseInt(listCart.get(position).getBerat())){
                    jumlah[0] -= Integer.parseInt(listCart.get(position).getBerat());
                    holder.etJumlah.setError(null);
                    holder.etJumlah.setText(String.format("%d", jumlah[0]));
                    holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang) * jumlah[0])); //update harga barang
//                    CartActivity.subTotalAll(getHarga(barang)); //ubah total harga seluruhnya
                    CartActivity.subTotalAll(getHarga(barang) * Integer.parseInt(listCart.get(position).getBerat())); //ubah total harga seluruhnya
                    CartActivity.changeQty(listCart.get(position).getId(), jumlah[0] / Integer.parseInt(listCart.get(position).getBerat())); //ubah quantity dari barang pada array list
//                    if(jumlah[0] ==1){
//                        holder.minus.setVisibility(View.INVISIBLE);
//                    }
                    if(jumlah[0] ==Integer.parseInt(listCart.get(position).getBerat())){
                        holder.minus.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        //textwatcher untuk handle perubahan qty di edit text
        holder.etJumlah.addTextChangedListener(new TextWatcher() {
            int tempJumlah;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tempJumlah = jumlah[0];
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(holder.etJumlah.getText().toString().equals("")){
//                    holder.etJumlah.setError("Minimum pembelian 1");
                    holder.etJumlah.setError("Minimum pembelian "+listCart.get(position).getBarang().getMinBeli()+ listCart.get(position).getBarang().getAlias());
                    holder.minus.setVisibility(View.INVISIBLE);
                    CartActivity.disableCheckout("error");
                }
                else if(Integer.parseInt(holder.etJumlah.getText().toString())<Math.round(listCart.get(position).getBarang().getMinBeli())){
//                    holder.etJumlah.setError("Minimum pembelian 1");
                    holder.etJumlah.setError("Minimum pembelian "+listCart.get(position).getBarang().getMinBeli()+ listCart.get(position).getBarang().getAlias());
                    jumlah[0] = 0;
                    holder.minus.setVisibility(View.INVISIBLE);
                    CartActivity.disableCheckout("error");
                }
                else if(Integer.parseInt(holder.etJumlah.getText().toString())==Math.round(listCart.get(position).getBarang().getMinBeli())){
                    holder.minus.setVisibility(View.INVISIBLE);
                    jumlah[0] = Integer.parseInt(holder.etJumlah.getText().toString());
                    CartActivity.disableCheckout("normal");
                }
                else{
                    jumlah[0] = Integer.parseInt(holder.etJumlah.getText().toString());
                    holder.minus.setVisibility(View.VISIBLE);
                    CartActivity.disableCheckout("normal");
                }

                holder.tvTotal.setText(Currency.getCurrencyFormat().format(getHarga(barang) * jumlah[0]));
                int selisih;
                if(tempJumlah>jumlah[0]){
                    selisih = tempJumlah - jumlah[0];
                    CartActivity.subTotalAll(getHarga(barang) * selisih); //ubah total harga seluruhnya
                }
                else if(tempJumlah<jumlah[0]){
                    selisih = jumlah[0] - tempJumlah;
                    CartActivity.addTotalAll(getHarga(barang) * selisih); //ubah total harga seluruhnya
                }
                CartActivity.changeQty(listCart.get(position).getId(),jumlah[0] / Integer.parseInt(listCart.get(position).getBerat())); //ubah quantity dari barang pada array list
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //kondisi saat button delete ditekan
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                builder.setCancelable(true);
                if(listCart.get(position).getNegoCount()>0){
                    builder.setMessage("Anda yakin ingin menghapus barang dari daftar pesanan? Hasil negosiasi juga akan dihapus dan harga akan kembali seperti semula.");
                }
                else{
                    builder.setMessage("Hapus barang dari cart?");
                }
                builder.setPositiveButton("Hapus",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                        return;
                                    }
                                    else {
                                        String query = "UPDATE gcm_master_cart SET status='I', update_date=now(), " +
                                                "update_by="+SharedPrefManager.getInstance(_context).getUser().getUserId()+
                                                " where company_id="+ SharedPrefManager.getInstance(_context).getUser().getCompanyId()+
                                                " and barang_id="+barang.getId()+" returning id;";
                                        Log.d("ido", "cart: "+query);
                                        loadingDialog = new LoadingDialog(_context);
                                        loadingDialog.showDialog();
                                        Call<JsonObject> cartCall = RetrofitClient
                                                .getInstance()
                                                .getApi()
                                                .request(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_cart SET status='I', update_date=now(), " +
                                                        "update_by="+SharedPrefManager.getInstance(_context).getUser().getUserId()+
                                                        " where company_id="+ SharedPrefManager.getInstance(_context).getUser().getCompanyId()+
                                                        " and barang_id="+barang.getId()+" returning id;")));

                                        cartCall.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(retrofit2.Call<JsonObject> call, Response<JsonObject> response) {
                                                if(response.isSuccessful()){
                                                    String status = response.body().getAsJsonObject().get("status").getAsString();
                                                    if(status.equals("success")){
                                                        loadingDialog.hideDialog();
                                                        Toast.makeText(_context, "Barang berhasil dihapus", Toast.LENGTH_SHORT).show();
                                                        ((CartActivity)_context).finish();
                                                        ((CartActivity)_context).overridePendingTransition(0, 0);
                                                        ((CartActivity)_context).startActivity(((CartActivity)_context).getIntent());
                                                        ((CartActivity)_context).overridePendingTransition(0, 0);
                                                    }
                                                    else if(status.equals("error")){
                                                        loadingDialog.hideDialog();
                                                        Toast.makeText(_context, "Koneksi gagal", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context, "Koneksi gagal", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    lastClickTime = SystemClock.elapsedRealtime();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                builder.setNegativeButton("Batal",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                Button pButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pButton.setTextColor(ContextCompat.getColor(_context, R.color.colorAccent));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listCart.size();
    }

    /**Method untuk mendapatkan harga suatu barang di cart*/
    private long getHarga(Barang barang){
        long harga=0;
        for(Cart cart : listCart){
            if(barang.getId()==cart.getBarang().getId()){
                if(cart.getNegoCount()>0){
//                    harga = cart.getHargaSales() / cart.getQty();
                    if (cart.getHargaKonsumen() == cart.getHargaSales()){
                        harga = cart.getHargaSales() / (Integer.parseInt(cart.getBerat()) * cart.getQty());
                    } else {
//                        harga = cart.getHargaKonsumen() / (Integer.parseInt(cart.getBerat()) * cart.getQty());
                        harga = (long) Math.ceil(cart.getBarang().getHarga()*kurs);
                    }
                }
                else {
//                    harga = (int) (cart.getBarang().getHarga()*kurs);
                    harga = (long) Math.ceil(cart.getBarang().getHarga()*kurs);
                    Log.d("harga", "getHarga: "+harga+" "+cart.getBarang().getHarga()+"*"+kurs);
                }
            }
        }
        return harga;
    }
}