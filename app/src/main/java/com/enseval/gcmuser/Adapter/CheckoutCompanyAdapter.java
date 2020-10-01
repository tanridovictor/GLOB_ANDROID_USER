package com.enseval.gcmuser.Adapter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.CheckoutActivity;
import com.enseval.gcmuser.Activity.ListAlamat;
import com.enseval.gcmuser.Activity.PaymentActivity;
import com.enseval.gcmuser.Model.Alamat;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.KalenderLibur;
import com.enseval.gcmuser.Model.Note;
import com.enseval.gcmuser.Model.Payment;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutCompanyAdapter extends RecyclerView.Adapter<CheckoutCompanyAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Cart> listongkir;
    private ArrayList<Alamat> listAlamat;
    private ArrayList<Alamat> listAlamatBillto;
    private ArrayList<Payment> listPayment;
    private ArrayList<Company> listCheckoutCompany;
    private ArrayList<KalenderLibur> listLibur;
    private ArrayList<Note> listNote;
    private float kurs;
    private long total, ongkir;
    private String tglLibur, ketLibur;
    String TAG = "ido";
    public String alamat, provinsi,kota, kecamatan, kelurahan ;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNamaPerusahaan, tvAlamatShipto, tvAlamatBillto, tvPayment, tvTanggalKirim;
        private TextView tvTotalHarga, tvppn, tvPpn, tvOngkir, tvSubtotal;
        private RecyclerView rvCheckoutBarang;
        private CardView alamatShipto, alamatBillto, pembayaran, tanggalKirim;
        private int subtotal;
        private ArrayList<Cart> listBarangPerCompany;
        private CheckoutBarangAdapter checkoutBarangAdapter;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvNamaPerusahaan = itemView.findViewById(R.id.tvNamaPerusahaan);
            rvCheckoutBarang = itemView.findViewById(R.id.rvCheckoutBarang);
            tvSubtotal = itemView.findViewById(R.id.tvSubTotal);
            alamatShipto = itemView.findViewById(R.id.cvShipto);
            alamatBillto = itemView.findViewById(R.id.cvBillto);
            pembayaran = itemView.findViewById(R.id.cvPembayaran);
            tvAlamatShipto = itemView.findViewById(R.id.alamatShipto);
            tvAlamatBillto = itemView.findViewById(R.id.alamatBillto);
            tvPayment = itemView.findViewById(R.id.pembayaran);
            tanggalKirim = itemView.findViewById(R.id.cvPermintaanKirim);
            tvTanggalKirim = itemView.findViewById(R.id.permintaanKirim);
            tvTotalHarga = itemView.findViewById(R.id.tvTotalHarga);
            tvppn = itemView.findViewById(R.id.tvppn);
            tvPpn = itemView.findViewById(R.id.tvPpn);
            tvOngkir = itemView.findViewById(R.id.tvOngkir);
        }
    }

    public CheckoutCompanyAdapter(Context _context, ArrayList<Company> listCheckoutCompany, long total, ArrayList<Cart> listongkir, ArrayList<Alamat> listAlamat, ArrayList<Payment> listPayment, ArrayList<Alamat> listAlamatBillto, ArrayList<KalenderLibur> listLibur, ArrayList<Note> listNote) {
        this._context = _context;
        this.listCheckoutCompany = listCheckoutCompany;
        //this.kurs = kurs;
        this.total = total;
        this.listongkir = listongkir;
        this.listAlamat = listAlamat;
        this.listPayment = listPayment;
        this.listAlamatBillto = listAlamatBillto;
        this.listLibur = listLibur;
        this.listNote = listNote;
    }

    @NonNull
    @Override
    public CheckoutCompanyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.checkout_company_view, parent, false);
        return new CheckoutCompanyAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckoutCompanyAdapter.ViewHolder holder, final int position) {
        final Company company = listCheckoutCompany.get(position);
        //Log.d(TAG, "listAlamatAdapter: "+listAlamat.size());
        SpannableStringBuilder str = new SpannableStringBuilder("Distributor : " + company.getNamaPerusahaan());
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 13, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvNamaPerusahaan.setText(str);

        //untuk menampilkan dan mengupdate payment
        int i = 0;
        while (i<listPayment.size()){
            Log.d(TAG, "payment condition: "+listPayment.get(i).getNama_perusahaan()+"="+company.getNamaPerusahaan());
            if(listPayment.get(i).getNama_perusahaan().equals(company.getNamaPerusahaan())){
                holder.tvPayment.setText(listPayment.get(i).getPayment_name());
                break;
            }
            i++;
        }


        //untuk menampilkan dan mengupdate alamat ship-to
        int j = 0;
        while (j<listAlamat.size()){
            if(listAlamat.get(i).getNama_perusahaan().equals(company.getNamaPerusahaan())){
                holder.tvAlamatShipto.setText(listAlamat.get(i).getAlamat()+",\n"+listAlamat.get(i).getKelurahan().toLowerCase()+", "+listAlamat.get(i).getKecamatan().toLowerCase()+",\n"+listAlamat.get(i).getKota().toLowerCase()+", "+listAlamat.get(i).getProvinsi()+", "+listAlamat.get(i).getKodepos());
                holder.tvTanggalKirim.setText(listAlamat.get(i).getTgl_permintaan_kirim());
            }
            j++;
        }

        //untuk menampilkan dan mengupdate alamat bill-to
        int k = 0;
        while (k<listAlamatBillto.size()){
            if(listAlamatBillto.get(i).getNama_perusahaan().equals(company.getNamaPerusahaan())){
                holder.tvAlamatBillto.setText(listAlamatBillto.get(i).getAlamat()+",\n"+listAlamatBillto.get(i).getKelurahan().toLowerCase()+", "+listAlamatBillto.get(i).getKecamatan().toLowerCase()+",\n"+listAlamatBillto.get(i).getKota().toLowerCase()+", "+listAlamatBillto.get(i).getProvinsi()+", "+listAlamatBillto.get(i).getKodepos());
            }
            k++;
        }

        holder.tvTotalHarga.setText(Currency.getCurrencyFormat().format(getSubtotal(company.getListCart())));
        holder.tvppn.setText("PPN "+Math.round(getppn(company.getListCart()))+"%");
        holder.tvPpn.setText(Currency.getCurrencyFormat().format(getPpn(company.getListCart())));

        //untuk menampilkan harga ongkos kirim
        int l = 0;
        while (l<listongkir.size()){
            Log.d(TAG, "ongkir: "+listongkir.get(i).getNama_perusahaan()+", "+company.getNamaPerusahaan());
            if(listongkir.get(i).getNama_perusahaan().equals(company.getNamaPerusahaan())){
                ongkir = (long) (listongkir.get(i).getOngkir()*getBerat(company.getListCart()));
                Log.d(TAG, "ongkir: "+ongkir);
                holder.tvOngkir.setText(Currency.getCurrencyFormat().format(ongkir));
            }
            l++;
        }

        holder.tvSubtotal.setText(Currency.getCurrencyFormat().format(getSubtotal(company.getListCart())+getPpn(company.getListCart())+ongkir)); //subtotal harga per seller

        //buat adapter untuk barang-barang per seller
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(_context);
        holder.rvCheckoutBarang.setLayoutManager(layoutManager1);
        holder.rvCheckoutBarang.setItemAnimator(new DefaultItemAnimator());
        holder.listBarangPerCompany = listCheckoutCompany.get(position).getListCart();
        holder.checkoutBarangAdapter = new CheckoutBarangAdapter(_context, holder.listBarangPerCompany, listNote);
        holder.rvCheckoutBarang.setAdapter(holder.checkoutBarangAdapter);

        Log.d(TAG, "onBindViewHolder: " + company.getListCart());

        holder.alamatShipto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_context, ListAlamat.class);
                i.putParcelableArrayListExtra("arridcart", listCheckoutCompany.get(position).getListCart());
                i.putExtra("flag", "shipto");
                i.putParcelableArrayListExtra("listSeller", listCheckoutCompany);
                i.putExtra("total", total);
                //i.putExtra("kurs", kurs);
                i.putExtra("tipe", "pilih");
                _context.startActivity(i);
            }
        });

//        holder.alamatBillto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(_context, ListAlamat.class);
//                i.putParcelableArrayListExtra("arridcart", listCheckoutCompany.get(position).getListCart());
//                i.putExtra("flag", "billto");
//                i.putParcelableArrayListExtra("listSeller", listCheckoutCompany);
//                i.putExtra("total", total);
//                i.putExtra("kurs", kurs);
//                _context.startActivity(i);
//            }
//        });

        holder.pembayaran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_context, PaymentActivity.class);
                i.putParcelableArrayListExtra("arridcart", listCheckoutCompany.get(position).getListCart());
                i.putExtra("seller", company.getId());
                i.putParcelableArrayListExtra("listSeller", listCheckoutCompany);
                i.putExtra("total", total);
                //i.putExtra("kurs", kurs);
                _context.startActivity(i);
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            final Calendar calendar = Calendar.getInstance();
            final int Day = calendar.get(Calendar.DAY_OF_MONTH);
            final int Month = calendar.get(Calendar.MONTH);
            final int Year = calendar.get(Calendar.YEAR);

            holder.tanggalKirim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatePickerDialog dpd = new DatePickerDialog(_context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            int k = 0;
                            while (k < listLibur.size()){
                                if (String.valueOf(month).length()==1){
                                    Log.d(TAG, "onDateSet: "+String.valueOf(year+"-0"+(month+1)+"-"+dayOfMonth));
                                    if (String.valueOf(year+"-0"+(month+1)+"-"+dayOfMonth).equals(listLibur.get(k).getTanggal().substring(0, 10))){
                                        tglLibur = listLibur.get(k).getTanggal().substring(0,10);
                                        ketLibur = listLibur.get(k).getKeterangan();
                                        break;
                                    }else{
                                        tglLibur = "tdkLibur";
                                        ketLibur = "tdkLibur";
                                    }
                                }else {
                                    if (String.valueOf(year + "-" + (month + 1) + "-" + dayOfMonth).equals(listLibur.get(k).getTanggal().substring(0, 10))) {
                                        tglLibur = listLibur.get(k).getTanggal().substring(0,10);
                                        ketLibur = listLibur.get(k).getKeterangan();
                                        break;
                                    }else{
                                        tglLibur = "tdkLibur";
                                        ketLibur = "tdkLibur";
                                    }
                                }
                                k++;
                            }
//                            for (int i=0; i<listLibur.size(); i++){
//                                if (String.valueOf(month).length()==1){
//                                    Log.d(TAG, "onDateSet: "+String.valueOf(year+"-0"+(month+1)+"-"+dayOfMonth));
//                                    if (String.valueOf(year+"-0"+(month+1)+"-"+dayOfMonth).equals(listLibur.get(i).getTanggal().substring(0, 10))){
//                                        tglLibur = listLibur.get(i).getTanggal().substring(0,10);
//                                        ketLibur = listLibur.get(i).getKeterangan();
//                                    }else{
//                                        tglLibur = "tdkLibur";
//                                        ketLibur = "tdkLibur";
//                                    }
//                                }else {
//                                    if (String.valueOf(year + "-" + (month + 1) + "-" + dayOfMonth).equals(listLibur.get(i).getTanggal().substring(0, 10))) {
//                                        tglLibur = listLibur.get(i).getTanggal().substring(0,10);
//                                        ketLibur = listLibur.get(i).getKeterangan();
//                                    }else{
//                                        tglLibur = "tdkLibur";
//                                        ketLibur = "tdkLibur";
//                                    }
//                                }
//                            }
                            SimpleDateFormat simpledateformat = new SimpleDateFormat("EEEE");
                            Date date = new Date(year, month, dayOfMonth-1);
                            String dayOfWeek = simpledateformat.format(date);
                            if (dayOfWeek.equals("Sunday")||dayOfWeek.equals("Saturday")){
                                Toast.makeText(_context, "Silahkan pilih tanggal pada hari kerja (Senin - Jumat).", Toast.LENGTH_LONG).show();
                            }else{
                                if (String.valueOf(month).length()==1) {
                                    if (!tglLibur.equals("tdkLibur") && tglLibur.equals(String.valueOf(year + "-0" + (month + 1) + "-" + dayOfMonth))) {
                                        Toast.makeText(_context, "Tanggal yang dipilih jatuh pada hari libur nasional.", Toast.LENGTH_LONG).show();
                                    } else {
                                        holder.tvTanggalKirim.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                                        for (int i = 0; i < holder.listBarangPerCompany.size(); i++) {
                                            setKirim(dayOfMonth, (month + 1), year, holder.listBarangPerCompany.get(i).getId());
                                        }
                                        Intent i = new Intent(_context, CheckoutActivity.class);
                                        i.putParcelableArrayListExtra("listSeller", listCheckoutCompany);
                                        i.putExtra("total", total);
                                        //i.putExtra("kurs", kurs);
                                        _context.startActivity(i);
                                        ((Activity)_context).finish();
                                    }
                                }else{
                                    if (!tglLibur.equals("tdkLibur") && tglLibur.equals(String.valueOf(year + "-" + (month + 1) + "-" + dayOfMonth))) {
                                        Toast.makeText(_context, "Tanggal yang dipilih jatuh pada hari libur nasional.", Toast.LENGTH_LONG).show();
                                    } else {
                                        holder.tvTanggalKirim.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                                        for (int i = 0; i < holder.listBarangPerCompany.size(); i++) {
                                            setKirim(dayOfMonth, (month + 1), year, holder.listBarangPerCompany.get(i).getId());
                                        }
                                        Intent i = new Intent(_context, CheckoutActivity.class);
                                        i.putParcelableArrayListExtra("listSeller", listCheckoutCompany);
                                        i.putExtra("total", total);
                                        //i.putExtra("kurs", kurs);
                                        _context.startActivity(i);
                                        ((Activity)_context).finish();
                                    }
                                }
                            }
                        }
                    },Year, Month, Day);
                    DatePicker datePicker = dpd.getDatePicker();
                    datePicker.setFirstDayOfWeek(Calendar.MONDAY);
                    dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    dpd.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000*60*60*24*20)+(1000*60*60*24*10));
                    dpd.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listCheckoutCompany.size();
    }

    /**Method untuk menghitung subtotal harga per seller*/
    private long getSubtotal(ArrayList<Cart> listCart){
        long harga=0;
        for(Cart cart : listCart){
            if(cart.getNegoCount()>0){
                harga += cart.getHargaSales();
            }
            else {
//                harga += (int) (cart.getBarang().getHarga()*kurs*cart.getQty());
                long hargaBarang = (long) Math.ceil(cart.getBarang().getHarga()*cart.getBarang().getKursIdr());
                harga += (long) (hargaBarang*cart.getQty()*Integer.parseInt(cart.getBerat()));
                Log.d(TAG, "getSubtotal: "+harga);
            }
        }
        return harga;
    }

    private String getTglKirim(ArrayList<Cart> listCart){
        String tgl_kirim="";
        for(Cart cart : listCart){
            tgl_kirim = cart.getTgl_permintaan_kirim().substring(0,10);
        }
        return tgl_kirim;
    }

    private long getPpn(ArrayList<Cart> listCart){
        long harga=0;
        for(Cart cart : listCart){
            if(cart.getNegoCount()>0){
                harga += cart.getHargaSales()*(cart.getPpn_seller()/100);
            }
            else {
//                harga += (int) (cart.getBarang().getHarga()*kurs*cart.getQty());
                long hargaBarang = (long) Math.ceil(cart.getBarang().getHarga()*cart.getBarang().getKursIdr());
                harga += (long) ((hargaBarang*cart.getQty()*Integer.parseInt(cart.getBerat()))*(cart.getPpn_seller()/100));
                Log.d(TAG, "getSubtotal: "+harga);
            }
        }
        return harga;
    }

    private int getBerat(ArrayList<Cart> listCart){
        int berat = 0;
        long ongkir = 0;
        for(Cart cart : listCart){
            berat += cart.getQty()*Integer.parseInt(cart.getBerat());
            Log.d(TAG, "getBerat: "+berat);
        }
        return berat;
    }

    private float getppn(ArrayList<Cart> listcart){
        float ppn = 0;
        for(Cart cart : listcart){
            ppn = cart.getPpn_seller();
        }
        return ppn;
    }

    private void setKirim(final int day, final int month, final int year, final int id_cart){
        String tgl_kirim = "update gcm_master_cart set tgl_permintaan_kirim = '"+year+"-"+month+"-"+day+"' where id="+id_cart+" returning id";
        Log.d(TAG, "setKirim: "+tgl_kirim);
        try {
            Call<JsonObject> setkirim = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(tgl_kirim)));
            setkirim.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Toast.makeText(_context, "Tanggal permintaan kirim berhasil disetel", Toast.LENGTH_SHORT).show();
                        }
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

}
