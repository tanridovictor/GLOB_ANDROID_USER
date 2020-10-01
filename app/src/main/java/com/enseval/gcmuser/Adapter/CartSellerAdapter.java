package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class CartSellerAdapter extends RecyclerView.Adapter<CartSellerAdapter.ViewHolder> {
    private Context _context;
    private static ArrayList<Company> listSeller;
    private float kursIdr;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView namaSeller;
        private ArrayList<Cart> cartPerSeller;
        private RecyclerView rvCartBarang;
        private CartBarangAdapter cartBarangAdapter;
        private CheckBox checkBox;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            namaSeller = itemView.findViewById(R.id.namaSeller);
            rvCartBarang = itemView.findViewById(R.id.rvCartBarang);
            checkBox = itemView.findViewById(R.id.checkSemua);
            checkBox.setVisibility(View.GONE);
        }
    }

    public CartSellerAdapter(Context _context, ArrayList<Company> listSeller, float kursIdr) {
        this._context = _context;
        this.listSeller = listSeller;
        this.kursIdr = kursIdr;
    }

    @NonNull
    @Override
    public CartSellerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.cart_seller_view, parent, false);
        return new CartSellerAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartSellerAdapter.ViewHolder holder, final int position) {
        SpannableStringBuilder str = new SpannableStringBuilder("Distributor : "+listSeller.get(position).getNamaPerusahaan());
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 13, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.namaSeller.setText(str);

        holder.checkBox.setChecked(listSeller.get(position).isChecked()); //set kondisi checkbox

        //kondisi jika checkbox ditekan
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(holder.checkBox.isChecked());

                //menggunakan looping untuk mengubah kondisi checked pada semua barang utk seller ini
                //jika seller di checked maka semua barang didalamnya juga menjadi checked
                //jika seller di unckecked maka semua barang didalamnya juga menjadi unchecked
                for (int i = 0; i < listSeller.get(position).getListCart().size(); i++) {
                    listSeller.get(position).getListCart().get(i).setChecked(holder.checkBox.isChecked());
                    holder.cartBarangAdapter.notifyDataSetChanged();
                }
                setChecked(holder, position); //memanggil method untuk sinkronisasi checked value seller dan barang didalamnnya
            }
        });

        //buat adapter baru untuk barng-barang milik seller ini
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(_context);
        holder.rvCartBarang.setLayoutManager(layoutManager1);
        holder.rvCartBarang.setItemAnimator(new DefaultItemAnimator());
        holder.cartPerSeller = new ArrayList<>();
        holder.cartPerSeller = listSeller.get(position).getListCart();
        holder.cartBarangAdapter = new CartBarangAdapter(_context, holder.cartPerSeller, kursIdr, holder, position);
        holder.rvCartBarang.setAdapter(holder.cartBarangAdapter);
    }

    @Override
    public int getItemCount() {
        return listSeller.size();
    }

    /**Method untuk sinkronisasi checked value dari seller dan barang didalamnya
     * Jika semua barang didalmnya checked maka seller diubah menjadi checked
     * Jika ada barang yang tidak checked, maka seller tidak checked*/
    public static void setChecked(CartSellerAdapter.ViewHolder sellerHolder, int sellerPosition){
        Log.d("", "posisi seller"+sellerPosition);
        boolean allChecked = true;
        ArrayList<Cart> listCartPerCompany = listSeller.get(sellerPosition).getListCart();
        for(int i=0; i<listCartPerCompany.size(); i++){
            if(!listCartPerCompany.get(i).isChecked()){
                allChecked=false; //jika ada yg tidak checked maka nilai flag menjadi false
            }
        }
        listSeller.get(sellerPosition).setChecked(allChecked); //ubah nilai checked seller sesuai flag allChecked
        sellerHolder.checkBox.setChecked(listSeller.get(sellerPosition).isChecked());
    }
}
