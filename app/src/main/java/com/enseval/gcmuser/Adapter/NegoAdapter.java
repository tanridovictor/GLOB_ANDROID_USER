package com.enseval.gcmuser.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.API;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.ChatActivity;
import com.enseval.gcmuser.Activity.DetailHistoryNego;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.NotifAI.ModelNotif;
import com.enseval.gcmuser.Model.NotifFirebase.Data;
import com.enseval.gcmuser.Model.NotifFirebase.NotificationBody;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.Model.Negosiasi;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NegoAdapter extends RecyclerView.Adapter<NegoAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Negosiasi> listNego;
    private String result;
    private long lastClickTime=0;
    private LoadingDialog loadingDialog;
    private float kurs;
    Date dateTime, TimeRespon;
    long timestamp_respon_now, timestamp_respon;

    private API mApi;

    private String TAG = "ido";

    private int value_count_nego;
    private int value_idart;


    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout2, linearLayout3, linearChat;
        private TextView tvNama, tvHarga, tvHargaNego, tvResponSales, tvStaticHarga, tvStaticRespon, tvStaticJatah, tvSepakat, tvhargadeal, tvhargafinal, tvNamaPerusahaan;
        private Button btnSetuju, btnSetujuNegoTerakhir, btnNegoLagi, btnChat;
        private ImageView jatah1, jatah2, jatahTerpakai1, jatahTerpakai2, jatahTerpakai3;

        private TextView tvHistoryNego;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNama = itemView.findViewById(R.id.tvNama);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvHargaNego = itemView.findViewById(R.id.tvHargaNego);
            tvResponSales = itemView.findViewById(R.id.tvResponSales);
            btnSetuju = itemView.findViewById(R.id.btnSetuju);
            btnSetujuNegoTerakhir = itemView.findViewById(R.id.btnSetujuNegoTerakhir);
            btnNegoLagi = itemView.findViewById(R.id.btnNegoLagi);
            jatah1 = itemView.findViewById(R.id.jatah1);
            jatah2 = itemView.findViewById(R.id.jatah2);
            jatahTerpakai1 = itemView.findViewById(R.id.jatahTerpakai1);
            jatahTerpakai2 = itemView.findViewById(R.id.jatahTerpakai2);
            jatahTerpakai3 = itemView.findViewById(R.id.jatahTerpakai3);
            tvStaticHarga = itemView.findViewById(R.id.tvStaticHarga);
            tvStaticRespon = itemView.findViewById(R.id.tvStaticRespon);
            tvStaticJatah = itemView.findViewById(R.id.tvStaticJatah);
            linearLayout2 = itemView.findViewById(R.id.linearLayout2);
            linearLayout3 = itemView.findViewById(R.id.linearLayout3);
            tvSepakat = itemView.findViewById(R.id.tvSepakat);
            linearChat = itemView.findViewById(R.id.linearLayoutChat);
            btnChat = itemView.findViewById(R.id.btnChat);
            tvhargafinal = itemView.findViewById(R.id.tvHargaFinal);
            tvhargadeal = itemView.findViewById(R.id.tvHargaDeal);
            tvNamaPerusahaan = itemView.findViewById(R.id.tvNamaPerusahaan);

            tvHistoryNego   = itemView.findViewById(R.id.tvHistoryNego);
//            SpannableString content = new SpannableString("History Nego");
//            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//            tvHistoryNego.setText(content);
        }
    }

    public NegoAdapter(Context _context, ArrayList<Negosiasi> listNego, float kurs) {
        this._context = _context;
        this.listNego = listNego;
        this.kurs = kurs;
    }


    @NonNull
    @Override
    public NegoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.nego_detail_view, parent, false);
        return new NegoAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final NegoAdapter.ViewHolder holder, final int position) {
        mApi = RetrofitClient.getNotifService();

        final double harga = (double) Math.ceil(listNego.get(position).getBarang().getHarga()*kurs);
        Calendar calendar = Calendar.getInstance();
        Date DateTime = calendar.getTime();
        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeS = datetime.format(DateTime);

        timestamp_respon_now = Calendar.getInstance().getTimeInMillis();
        timestamp_respon = Long.parseLong(listNego.get(position).getTimestamp_respon());

        Log.d(TAG, "onBindViewHolder: "+Calendar.getInstance().getTimeZone().getID());

        try {
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateTime = time.parse(listNego.get(position).getTime_respon());
            TimeRespon = datetime.parse(DateTimeS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvNamaPerusahaan.setText(listNego.get(position).getBarang().getNamaPerusahaan());
        holder.tvNama.setText(listNego.get(position).getBarang().getNama());
        holder.tvHarga.setText(Currency.getCurrencyFormat().format(harga) + "/"+listNego.get(position).getBarang().getAlias());
        if (listNego.get(position).getHarga_nego() != 0) {
            holder.tvHargaNego.setText(Currency.getCurrencyFormat().format(listNego.get(position).getHarga_nego()) + "/"+listNego.get(position).getBarang().getAlias());
        }
        if (listNego.get(position).getHarga_nego_2() != 0) {
            holder.tvHargaNego.setText(Currency.getCurrencyFormat().format(listNego.get(position).getHarga_nego_2()) + "/"+listNego.get(position).getBarang().getAlias());
        }
        if (listNego.get(position).getHarga_nego_3() != 0) {
            holder.tvHargaNego.setText(Currency.getCurrencyFormat().format(listNego.get(position).getHarga_nego_3()) + "/"+listNego.get(position).getBarang().getAlias());
        }

        if(listNego.get(position).getHarga_final() == 0){
            holder.tvhargafinal.setVisibility(View.GONE);
            holder.tvhargadeal.setVisibility(View.GONE);
        }
//        else if(listNego.get(position).getHarga_final() != 0 && TimeRespon.before(dateTime)){
        else if(listNego.get(position).getHarga_final() != 0 && timestamp_respon_now < timestamp_respon){
            holder.tvhargafinal.setVisibility(View.GONE);
            holder.tvhargadeal.setVisibility(View.GONE);
        }else{
            holder.tvhargafinal.setVisibility(View.VISIBLE);
            holder.tvhargadeal.setVisibility(View.VISIBLE);
            holder.tvhargadeal.setText(Currency.getCurrencyFormat().format(listNego.get(position).getHarga_final())+"/"+listNego.get(position).getBarang().getAlias());
        }

        Log.d("ido", "onBindViewHolder: Nego "+listNego.get(position).getHargaSales());
//        holder.tvHargaNego.setText(Currency.getCurrencyFormat().format(listNego.get(position).getHargaKonsumen()));
        holder.tvSepakat.setVisibility(View.GONE);
        holder.linearChat.setVisibility(View.GONE);
//        callData(listNego.get(position).getHistory_id());

        final int hargaSales = listNego.get(position).getHargaSales();
        Log.d("cekit",String.valueOf(hargaSales));
        Log.d("cekit", "onBindViewHolder: "+hargaSales);
        Log.d("cekit",String.valueOf(listNego.get(position).getBarang().getBerat()));
//        final long harga_satuan = hargaSales / Integer.parseInt(listNego.get(position).getBarang().getBerat());
        //kondisi harga sudah disepakati
        if(hargaSales==listNego.get(position).getHargaKonsumen()) {
//            if (TimeRespon.before(dateTime)) {
//                holder.tvResponSales.setText("Menunggu respon");
//                holder.btnNegoLagi.setVisibility(View.GONE);
//                holder.btnSetuju.setVisibility(View.GONE);
//                jatahNego(listNego.get(position).getNegoCount(), holder);
//            } else {
                holder.tvStaticRespon.setVisibility(View.GONE);
                holder.tvResponSales.setVisibility(View.GONE);
                holder.btnSetuju.setVisibility(View.GONE);
                holder.btnNegoLagi.setVisibility(View.GONE);
                holder.jatah1.setVisibility(View.GONE);
                holder.jatah2.setVisibility(View.GONE);
                holder.jatahTerpakai1.setVisibility(View.GONE);
                holder.jatahTerpakai2.setVisibility(View.GONE);
                holder.jatahTerpakai3.setVisibility(View.GONE);
                holder.tvStaticJatah.setVisibility(View.GONE);
                holder.tvStaticHarga.setText("Harga Negosiasi");
                holder.linearLayout2.setVisibility(View.GONE);
                holder.linearLayout3.setVisibility(View.GONE);
                holder.tvSepakat.setVisibility(View.VISIBLE);
                Log.d("ido", "onBindViewHolder: if pertama " + TimeRespon + " " + dateTime);
        }
        //kondisi belum direspon sales
//        else if(
//                !listNego.get(position).isResponSales() || listNego.get(position).getHargaSales() == 0
//        ){
//            holder.tvResponSales.setText("Menunggu respon");
//            holder.btnNegoLagi.setVisibility(View.GONE);
//            holder.btnSetuju.setVisibility(View.GONE);
//            jatahNego(listNego.get(position).getNegoCount(), holder);
//        }
//        else if(listNego.get(position).getHargaSales() == 0){
//            holder.tvResponSales.setText("Menunggu respon");
//            holder.btnNegoLagi.setVisibility(View.GONE);
//            holder.btnSetuju.setVisibility(View.GONE);
//            jatahNego(listNego.get(position).getNegoCount(), holder);
//        }
        //rules untuk waktu nego
        //rubah
//        else if(TimeRespon.before(dateTime) || (listNego.get(position).getHarga_nego() != 0 && listNego.get(position).getHargaSales() == 0) ||
        else if(timestamp_respon_now < timestamp_respon || (listNego.get(position).getHarga_nego() != 0 && listNego.get(position).getHargaSales() == 0) ||
                (listNego.get(position).getHarga_nego_2() != 0 && listNego.get(position).getHarga_sales_2() == 0) ||
                (listNego.get(position).getHarga_nego_3() != 0 && listNego.get(position).getHarga_sales_3() == 0)){
            holder.tvResponSales.setText("Menunggu respon");
            holder.btnNegoLagi.setVisibility(View.GONE);
            holder.btnSetuju.setVisibility(View.GONE);
            jatahNego(listNego.get(position).getNegoCount(), holder);
            Log.d("ido", "onBindViewHolder: if kedua "+TimeRespon+" "+dateTime);
        }
        //kondisi sudah melewati respon time
        else{
            Log.d("ido", "onBindViewHolder: if ketiga "+TimeRespon+" "+dateTime);
            jatahNego(listNego.get(position).getNegoCount(), holder);
            //kondisi jika nego sudah 3x dan belum deal
            if(listNego.get(position).getNegoCount()==3 && listNego.get(position).getHargaKonsumen() != listNego.get(position).getHargaSales()){
                holder.btnNegoLagi.setVisibility(View.GONE);
                holder.btnSetuju.setVisibility(View.GONE);
                holder.linearChat.setVisibility(View.VISIBLE);
                holder.btnChat.setVisibility(View.VISIBLE);
                holder.tvResponSales.setText(String.format("%s/"+listNego.get(position).getBarang().getAlias(), Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_3())));
                String statusNotif = "terima nego";
                if (listNego.get(position).getBarang().getPersen_nego_1()==0 && listNego.get(position).getBarang().getPersen_nego_2()==0 && listNego.get(position).getBarang().getPersen_nego_3()==0){
                    Log.d(TAG, "notif dikirim admin");
                }
//                else {
//                    //getTokenSeles(position, statusNotif);
//                    sendNotifNegoPersen(position);
//                }
            }
            //kondisi jika nego belum 3x
            //rubah
            else{
                if (listNego.get(position).getHarga_sales_1() != 0) {
                    holder.tvResponSales.setText(String.format("%s", Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_1())+"/"+listNego.get(position).getBarang().getAlias()));
                }
                if (listNego.get(position).getHarga_sales_2() != 0) {
                    holder.tvResponSales.setText(String.format("%s", Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_2())+"/"+listNego.get(position).getBarang().getAlias()));
                }
                if (listNego.get(position).getHarga_sales_3() != 0) {
                    holder.tvResponSales.setText(String.format("%s", Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_3())+"/"+listNego.get(position).getBarang().getAlias()));
                }
                String statusNotif = "terima nego";
                if (listNego.get(position).getBarang().getPersen_nego_1()==0 && listNego.get(position).getBarang().getPersen_nego_2()==0 && listNego.get(position).getBarang().getPersen_nego_3()==0){
                    Log.d(TAG, "notif dikirim admin");
                }
//                else {
//                    sendNotifNegoPersen(position);
//                }
                holder.btnChat.setVisibility(View.GONE);
//                holder.tvResponSales.setText(String.format("Maaf, harga belum cocok.\nBagaimana jika %s?", Currency.getCurrencyFormat().format(listNego.get(position).getHargaSales())));
            }
        }

        //jika button nego lagi ditekan (mekanismenya sama seperti pada detail barang)
        holder.btnNegoLagi.setOnClickListener(new View.OnClickListener() {
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
                    TextView kuantitasBarang = dialog.findViewById(R.id.tvKuantitas);
                    LinearLayout linearLayout = dialog.findViewById(R.id.linearJatah);
                    linearLayout.setVisibility(View.GONE);

                    final TextInputLayout tvHargaNego = dialog.findViewById(R.id.hargaNego);
                    final Button btnNego = dialog.findViewById(R.id.btnNego);

                    btnNego.setEnabled(false);

                    namaBarang.setText(holder.tvNama.getText().toString());
//                    hargaBarang.setText("Harga Sales dalam satuan: "+Currency.getCurrencyFormat().format(hargaSales / listNego.get(position).getQty() ));
                    if (listNego.get(position).getHarga_sales_1() != 0) {
                        hargaBarang.setText("Harga Sales : " + Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_1())+"/"+listNego.get(position).getBarang().getAlias());
                    }
                    if (listNego.get(position).getHarga_sales_2() != 0) {
                        hargaBarang.setText("Harga Sales : " + Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_2())+"/"+listNego.get(position).getBarang().getAlias());
                    }
                    if (listNego.get(position).getHarga_sales_3() != 0) {
                        hargaBarang.setText("Harga Sales : "+ Currency.getCurrencyFormat().format(listNego.get(position).getHarga_sales_3())+"/"+listNego.get(position).getBarang().getAlias());
                    }

                    kuantitasBarang.setText("Kuantitas : "+ listNego.get(position).getBerat()*listNego.get(position).getQty() +" "+listNego.get(position).getBarang().getAlias());
                    Log.d("seksek", String.valueOf(listNego.get(position).getNegoCount()) + " ==== " + String.valueOf(listNego.get(position).getHistory_id()));

                    callNegoCOunt(String.valueOf(listNego.get(position).getIdCart()));

                    tvHargaNego.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) throws IllegalArgumentException{
                            if(!TextUtils.isEmpty(tvHargaNego.getEditText().getText())){
                                int input = Integer.parseInt(tvHargaNego.getEditText().getText().toString());
//                                nang kene
                                if(listNego.get(position).getNegoCount()==1 && input>listNego.get(position).getHarga_sales_1() ||
                                        listNego.get(position).getNegoCount()==2 && input>listNego.get(position).getHarga_sales_2()){
                                    tvHargaNego.setErrorEnabled(true);
                                    tvHargaNego.setError("Harga nego melebihi harga sales");
                                    btnNego.setEnabled(false);
                                }else if(listNego.get(position).getNegoCount()==1 && input<=listNego.get(position).getHarga_nego() ||
                                        listNego.get(position).getNegoCount()==2 && input<=listNego.get(position).getHarga_nego_2()){
                                    tvHargaNego.setErrorEnabled(true);
                                    tvHargaNego.setError("Harga nego harus melibihi nego sebelumnya");
                                    btnNego.setEnabled(false);
                                }else{
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

                    btnNego.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                    return;
                                }
                                else {
//                                    int price = Integer.parseInt(tvHargaNego.getEditText().getText().toString()) * listNego.get(position).getQty();
                                    int price = Integer.parseInt(tvHargaNego.getEditText().getText().toString()) * listNego.get(position).getQty() * listNego.get(position).getBerat();
                                    dialog.dismiss();
                                    loadingDialog = new LoadingDialog(_context);
                                    loadingDialog.showDialog();
                                    Call<JsonObject> negoCall = RetrofitClient
                                            .getInstance()
                                            .getApi()
                                            .request(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_cart set nego_count=nego_count+1," +
                                                    "harga_konsumen="+price+
                                                    ", update_date=now(), update_by="+
                                                    SharedPrefManager.getInstance(_context).getUser().getUserId()+" where company_id="+
                                                    SharedPrefManager.getInstance(_context).getUser().getCompanyId()+" and" +
//                                                    " barang_id="+listNego.get(position).getBarang().getId()+" and status='A' returning id;")));
                                                    " id="+listNego.get(position).getIdCart()+" and status='A' returning id;")));
                                            Log.d("april", String.valueOf(listNego.get(position).getBarang().getId()));

                                    negoCall.enqueue(new Callback<JsonObject>() {
                                        @Override
                                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                            if(response.isSuccessful()){
                                                String status = response.body().getAsJsonObject().get("status").getAsString();
                                                if(status.equals("success")){
                                                    loadingDialog.hideDialog();
                                                    Log.d("ido", "Sukses update nego");
                                                    Toast.makeText(_context,"Harga nego telah diperbarui", Toast.LENGTH_SHORT).show();
                                                    updateDataNego(listNego.get(position).getHistory_id(),listNego.get(position).getIdCart(), Integer.parseInt(tvHargaNego.getEditText().getText().toString()), listNego.get(position).getBarang().getHarga_terendah());
//                                                    Intent intent = new Intent(_context, MainActivity.class);
//                                                    intent.putExtra("fragment", "negoFragment");
//                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                    _context.startActivity(intent);
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
                                            loadingDialog.hideDialog();
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

            private void callNegoCOunt(String valueOf) {
                loadingDialog = new LoadingDialog(_context);
                loadingDialog.showDialog();
                try {
                    String query_nego_count = "select nego_count, history_nego_id from gcm_master_cart where id =" + valueOf;
                    Log.d("callNegoCOunt",query_nego_count);
                    Call<JsonObject> kiw = RetrofitClient
                            .getInstance()
                            .getApi()
                            .request(new JSONRequest(QueryEncryption.Encrypt(query_nego_count)));
                    kiw.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if(response.isSuccessful()) {
                                String status = response.body().getAsJsonObject().get("status").getAsString();
                                if (status.equals("success") && response.body() != null) {
                                    loadingDialog.hideDialog();
                                    value_count_nego = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("nego_count").getAsInt();
                                    Log.d("callNegoCOunt resbody", response.body().toString());
                                    Log.d("callNegoCOunt count", String.valueOf(value_count_nego));
                                } else {
                                    loadingDialog.hideDialog();
                                    Toast.makeText(_context, "Proses mengisi database History gagal  ", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                loadingDialog.hideDialog();
                                Toast.makeText(_context, "Procces not success  ", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            loadingDialog.hideDialog();
                            t.printStackTrace();
                            Log.wtf("ONFAILURE callNegoCOunt=== ",t);
                        }
                    });
                }catch (Exception e){
                    loadingDialog.hideDialog();
                    e.printStackTrace();
                    Log.wtf("callNegoCOunt function ===",e);
                }
            }

            private void updateDataNego(int id_hist, int id_cart, int price, double harga_terendah) {
//                Toast.makeText(_context, "Cekit cart ===" +id_cart + "Cekit idHistory + " + id_hist , Toast.LENGTH_SHORT).show();

                loadingDialog = new LoadingDialog(_context);
                loadingDialog.showDialog();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, 1);
                Date DateTime = calendar.getTime();
                long TimeStamp = calendar.getTimeInMillis();
                SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String Date = datetime.format(DateTime);

                if (listNego.get(position).getNegoCount() == 1){
//                    Toast.makeText(_context, "count_nego bernilai 1 dan siap update history nego", Toast.LENGTH_SHORT).show();
                    String q = "";
                    double hargaNormal = (double) Math.ceil(listNego.get(position).getBarang().getHarga()*kurs);
                    double hargaRendah = (double) Math.ceil(listNego.get(position).getBarang().getHarga_terendah()*kurs);
                    double persenNego = (double) listNego.get(position).getBarang().getPersen_nego_2()/100;
                    long hargaNego2 = (long) Math.ceil(hargaRendah+(hargaNormal-hargaRendah)*persenNego);
                    long hargaSales = (long) Math.ceil(listNego.get(position).getQty()*listNego.get(position).getBerat()*hargaNego2);
                    int jumlah = (int) listNego.get(position).getQty()*listNego.get(position).getBerat();
                    Log.d("Dialog", "updateDataNego: "+hargaNego2+" "+hargaSales);

                    try {
                        String query = "";
                        if (listNego.get(position).getBarang().getPersen_nego_1() == 0 && listNego.get(position).getBarang().getPersen_nego_2() == 0 && listNego.get(position).getBarang().getPersen_nego_3() == 0){
                            query = "UPDATE gcm_master_cart set harga_sales = " + null + " where id = "+listNego.get(position).getIdCart()+" returning id;";
                        }else {
                            if (listNego.get(position).getBarang().getPersen_nego_2() == 0 && price < Math.round(hargaRendah)) {
                                query = "UPDATE gcm_master_cart set harga_sales = " + hargaRendah * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                            } else if (listNego.get(position).getBarang().getPersen_nego_2() == 0 && price > Math.round(hargaRendah)) {
                                query = "UPDATE gcm_master_cart set harga_sales = " + price * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                            } else {
                                if (price < hargaNego2) {
                                    query = "UPDATE gcm_master_cart set harga_sales = " + hargaNego2 * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                                } else {
                                    query = "UPDATE gcm_master_cart set harga_sales = " + price * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                                }
                            }
                        }
                        Log.d("dialog", "updateDataNego: "+ query);
                        Call<JsonObject> UpC = RetrofitClient
                                .getInstance()
                                .getApi()
                                .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                        UpC.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if(response.isSuccessful()) {
                                    String status = response.body().getAsJsonObject().get("status").getAsString();
                                    if (status.equals("success")) {
                                        Toast.makeText(_context, "Update success", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(_context, "Update cart not success", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    loadingDialog.hideDialog();
                                    Toast.makeText(_context, "WRONG PROGRAM", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {

                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    };

                    try {
                        if (listNego.get(position).getBarang().getPersen_nego_1()==0 && listNego.get(position).getBarang().getPersen_nego_2()==0 && listNego.get(position).getBarang().getPersen_nego_3()==0){
//                            if (price >= Math.round(hargaRendah)){
//                                q = "UPDATE gcm_history_nego set harga_nego_2 = " + price + ", harga_sales_2 = " + price + ", harga_final= " + price + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now() where id =" + id_hist + "returning id;";
//                            }else{
                                q = "UPDATE gcm_history_nego set harga_nego_2 = " + price + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now(), timestamp_updated_date_2 = "+timestamp_respon_now+" where id =" + id_hist + " returning id;";
//                            }
                        }else {
                            if (listNego.get(position).getBarang().getPersen_nego_2() == 0 && price < Math.round(hargaRendah)) {
                                q = "UPDATE gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_2 = " + price + ", harga_sales_2 = " + Math.round(hargaRendah) + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now(), timestamp_updated_date_2 = "+timestamp_respon_now+" where id =" + id_hist + " returning id;";
                            } else if (listNego.get(position).getBarang().getPersen_nego_2() == 0 && price > Math.round(hargaRendah)) {
                                q = "UPDATE gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_2 = " + price + ", harga_sales_2 = " + price + ", harga_final= " + price + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now(), timestamp_updated_date_2 = "+timestamp_respon_now+" where id =" + id_hist + " returning id;";
                            } else {
                                if (price < (hargaNego2)) {
                                    q = "UPDATE gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_2 = " + price + ", harga_sales_2 = " + hargaNego2 + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now(), timestamp_updated_date_2 = "+timestamp_respon_now+" where id =" + id_hist + " returning id;";
                                } else {
                                    q = "UPDATE gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_2 = " + price + ", harga_sales_2 = " + price + ", harga_final= " + price + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now(), timestamp_updated_date_2 = "+timestamp_respon_now+" where id =" + id_hist + " returning id;";
                                    //                            updateOnRange(price, id_hist);
                                }
                            }
                        }
                        Log.d("ido",q);
                        Call<JsonObject> wew = RetrofitClient
                                .getInstance()
                                .getApi()
                                .request(new JSONRequest(QueryEncryption.Encrypt(q)));
                        Log.d("ido",QueryEncryption.Encrypt(q));
                        wew.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if(response.isSuccessful()) {
                                    String status = response.body().getAsJsonObject().get("status").getAsString();
                                    Log.d("response2", response.body().toString());
                                    if (status.equals("success")) {
                                        String statusNotif = "kirim nego";
                                        getTokenSeles(position, statusNotif);
                                        loadingDialog.hideDialog();
                                        Intent intent = new Intent(_context, MainActivity.class);
                                        intent.putExtra("fragment", "negoFragment");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        _context.startActivity(intent);

                                    }else {
                                        loadingDialog.hideDialog();
                                        Toast.makeText(_context, "Proses mengisi database History gagal NEGO KE 2 ", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    loadingDialog.hideDialog();
                                    Toast.makeText(_context, "not isSuccessful", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                loadingDialog.hideDialog();
                                Toast.makeText(_context, "onFailure === updateDataNego", Toast.LENGTH_SHORT).show();
                                t.printStackTrace();
                                Log.wtf("",t);
                            }
                        });

                    }catch (Exception e){
                        loadingDialog.hideDialog();
                        Toast.makeText(_context, "onCatchError === updateDataNego", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        Log.wtf("",e);
                    };

                }else if (listNego.get(position).getNegoCount() == 2){
//                    Toast.makeText(_context, "count_nego bernilai 2 dan siap update history nego", Toast.LENGTH_SHORT).show();
                    String q = "";
                    double hargaNormal = (double) Math.ceil(listNego.get(position).getBarang().getHarga()*kurs);
                    double hargaRendah = (double) Math.ceil(listNego.get(position).getBarang().getHarga_terendah()*kurs);
                    double persenNego = (double) listNego.get(position).getBarang().getPersen_nego_3()/100;
                    long hargaNego3 = (long) Math.ceil(hargaRendah+(hargaNormal-hargaRendah)*persenNego);
                    long hargaSales = (long) Math.ceil(listNego.get(position).getQty()*listNego.get(position).getBerat()*hargaNego3);
                    int jumlah = (int) listNego.get(position).getQty()*listNego.get(position).getBerat();

                    try {
                        String query = "";
                        if (listNego.get(position).getBarang().getPersen_nego_1() == 0 && listNego.get(position).getBarang().getPersen_nego_2() == 0 && listNego.get(position).getBarang().getPersen_nego_3() == 0){
                            query = "UPDATE gcm_master_cart set harga_sales = " + null + " where id = "+listNego.get(position).getIdCart()+" returning id;";
                        }else {
                            if (listNego.get(position).getBarang().getPersen_nego_3() == 0 && price < Math.round(hargaRendah)) {
                                query = "UPDATE gcm_master_cart set harga_sales = " + hargaRendah * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                            } else if (listNego.get(position).getBarang().getPersen_nego_3() == 0 && price > Math.round(hargaRendah)) {
                                query = "UPDATE gcm_master_cart set harga_sales = " + price * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                            } else {
                                if (price < hargaNego3) {
                                    query = "UPDATE gcm_master_cart set harga_sales = " + hargaNego3 * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                                } else {
                                    query = "UPDATE gcm_master_cart set harga_sales = " + price * jumlah + " where id = " + listNego.get(position).getIdCart() + " returning id;";
                                }
                            }
                        }
                        Log.d("dialog", "updateDataNego: "+ query);
                        Call<JsonObject> UpC = RetrofitClient
                                .getInstance()
                                .getApi()
                                .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                        UpC.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if(response.isSuccessful()) {
                                    String status = response.body().getAsJsonObject().get("status").getAsString();
                                    if (status.equals("success")) {
                                        Toast.makeText(_context, "Update success", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(_context, "Update cart not success", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    loadingDialog.hideDialog();
                                    Toast.makeText(_context, "WRONG PROGRAM", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {

                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    };

                    try {
                        if (listNego.get(position).getBarang().getPersen_nego_1()==0 && listNego.get(position).getBarang().getPersen_nego_2()==0 && listNego.get(position).getBarang().getPersen_nego_3()==0){
//                            if (price >= Math.round(hargaRendah)){
//                                q = "UPDATE gcm_history_nego set harga_nego_2 = " + price + ", harga_sales_2 = " + price + ", harga_final= " + price + ", updated_by_2 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_2 = now() where id =" + id_hist + "returning id;";
//                            }else{
                                q = "UPDATE gcm_history_nego set harga_nego_3 = " + price + ", updated_by_3 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_3 = now(), timestamp_updated_date_3 = "+timestamp_respon_now+" where id =" + id_hist + "returning id;";
//                            }
                        }else {
                            if (listNego.get(position).getBarang().getPersen_nego_3() == 0 && price < Math.round(hargaRendah)) {
                                q = "UPDATE gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_3 = " + price + ", harga_sales_3 = " + Math.round(hargaRendah) + ", updated_by_3 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_3 = now(), timestamp_updated_date_3 = "+timestamp_respon_now+" where id =" + id_hist + "returning id;";
                            } else if (listNego.get(position).getBarang().getPersen_nego_2() == 0 && price > Math.round(hargaRendah)) {
                                q = "UPDATE gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_3 = " + price + ", harga_sales_3 = " + price + ", harga_final= " + price + ", updated_by_3 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + ", updated_date_3 = now(), timestamp_updated_date_3 = "+timestamp_respon_now+" where id =" + id_hist + "returning id;";
                            } else {
                                if (price < (hargaNego3)) {
                                    q = "update gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_3 = " + price + ", harga_sales_3 = " + hargaNego3 + "," +
                                            "updated_by_3 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + "," +
                                            "updated_date_3 = now(), timestamp_updated_date_3 = "+timestamp_respon_now+" where id = " + id_hist + "returning id;";
                                } else {
                                    q = "update gcm_history_nego set timestamp_respon = '"+TimeStamp+"', time_respon = '" + Date + "', harga_nego_3 = " + price + ", harga_sales_3 = " + price + ", harga_final = " + price + "" +
                                            ", updated_by_3 = " + SharedPrefManager.getInstance(_context).getUser().getUserId() + "," +
                                            "updated_date_3 = now(), timestamp_updated_date_3 = "+timestamp_respon_now+" where id = " + id_hist + "returning id;";
                                    //                            updateOnRange(price, id_hist);
                                }
                            }
                        }
                        Log.d("updated_nego3", q);
                        Call<JsonObject> wew = RetrofitClient
                                .getInstance()
                                .getApi()
                                .request(new JSONRequest(QueryEncryption.Encrypt(q)));
                        Log.d("updated_nego3",QueryEncryption.Encrypt(q));
                        wew.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if(response.isSuccessful()) {
                                    Log.d("response", response.body().toString());
                                    String status = response.body().getAsJsonObject().get("status").getAsString();
                                    if (status.equals("success")) {
                                        String statusNotif = "kirim nego";
                                        getTokenSeles(position, statusNotif);
                                        loadingDialog.hideDialog();
                                        Intent intent = new Intent(_context, MainActivity.class);
                                        intent.putExtra("fragment", "negoFragment");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        _context.startActivity(intent);

                                    }else {
                                        loadingDialog.hideDialog();
                                        Toast.makeText(_context, "Proses mengisi database History gagal NEGO KE 3 ", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    loadingDialog.hideDialog();
                                    Toast.makeText(_context, "not isSuccessful", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                loadingDialog.hideDialog();
                                Toast.makeText(_context, "onFailure === updateDataNego", Toast.LENGTH_SHORT).show();
                                t.printStackTrace();
                                Log.wtf("",t);
                            }
                        });

                    }catch (Exception e){
                        loadingDialog.hideDialog();
                        Toast.makeText(_context, "onCatchError === updateDataNego", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        Log.wtf("",e);
                    };

                }else {
                    Toast.makeText(_context, "Terimakasih Cinta", Toast.LENGTH_SHORT).show();
                }
            }


        });

        holder.btnSetujuNegoTerakhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tampilkan konfirmasi
                final Dialog dialog = new Dialog(_context);
                dialog.setContentView(R.layout.konfirmasi_dialog);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                TextView title = dialog.findViewById(R.id.title);
                TextView description = dialog.findViewById(R.id.description);

                title.setText("Konfirmasi Harga");
                description.setText("Kesepakatan harga akan dibuat jika anda memilih setuju");

                btnBatal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //jika setuju lanjut ke request untuk update cart
                btnSetuju.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                            return;
                        }
                        else {
                            try {
                                dialog.dismiss();
                                loadingDialog = new LoadingDialog(_context);
                                loadingDialog.showDialog();
                                Call<JsonObject> setujuNegoCall = RetrofitClient
                                        .getInstance()
                                        .getApi()
                                        .request(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_cart set harga_konsumen=harga_sales, update_date=now() where company_id="+
                                                SharedPrefManager.getInstance(_context).getUser().getCompanyId()+" and barang_id="+
                                                listNego.get(position).getBarang().getId()+" and status='A' returning harga_sales,qty,history_nego_id;")));


                                setujuNegoCall.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        if(response.isSuccessful()){
                                            String status = response.body().getAsJsonObject().get("status").getAsString();
                                            if(status.equals("success")){
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context,"Harga nego berlaku dalam hari yang sama. Silakan membuat pesanan sebelum pukul 24:00 untuk dapat menggunakan harga nego", Toast.LENGTH_SHORT).show();
                                                int sales_price = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("harga_sales").getAsInt();
                                                int qty= response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("qty").getAsInt();
                                                int id_nego_history = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("history_nego_id").getAsInt();
                                                updateDealToHistory(sales_price,qty,id_nego_history);
//                                                Intent intent = new Intent(_context, MainActivity.class);
//                                                intent.putExtra("fragment", "negoFragment");
//                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                _context.startActivity(intent);
                                            }
                                            else if(status.equals("error")){
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    private void updateDealToHistory(int passing_price, int passing_qty, int passing_id) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.add(Calendar.HOUR, 1);
                                        Date DateTime = calendar.getTime();
                                        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        String Date = datetime.format(DateTime);
//                                        int deal = passing_price / passing_qty;
                                        int deal = passing_price / listNego.get(position).getBerat() / passing_qty;
                                        String q= "UPDATE gcm_history_nego set harga_final = "+ deal +"  where id = "+ passing_id +" returning id;";
                                        Log.d("price_deal",q+" "+passing_price);

                                        try {
                                            loadingDialog = new LoadingDialog(_context);
                                            loadingDialog.showDialog();
                                            Call<JsonObject> setujuNegoCall = RetrofitClient
                                                    .getInstance()
                                                    .getApi()
                                                    .request(new JSONRequest(QueryEncryption.Encrypt(q)));
                                            Log.d("price_deal",QueryEncryption.Encrypt(q));

                                            setujuNegoCall.enqueue(new Callback<JsonObject>() {
                                                @Override
                                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                    if(response.isSuccessful()){
                                                        String status = response.body().getAsJsonObject().get("status").getAsString();
                                                        if(status.equals("success")){
                                                            final Dialog dialog = new Dialog(_context);
                                                            dialog.setContentView(R.layout.dialog_handle);
                                                            Window window = dialog.getWindow();
                                                            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                            ImageView image = dialog.findViewById(R.id.iconImage);
                                                            TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                                                            Button btnSetuju = dialog.findViewById(R.id.btnYa);
                                                            TextView title = dialog.findViewById(R.id.judul);
                                                            TextView description = dialog.findViewById(R.id.isi);
                                                            dialog.setCancelable(false);
                                                            btnBatal.setVisibility(View.GONE);

                                                            title.setText("Nego Berhasil Disepakati");
                                                            description.setText("Harga nego berlaku dalam hari yang sama. Silakan membuat pesanan sebelum pukul 24:00 untuk dapat menggunakan harga nego");
                                                            image.setImageResource(R.drawable.ic_chat_black_24dp);

                                                            //jika setuju lanjut ke request
                                                            btnSetuju.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent intent = new Intent(_context, MainActivity.class);
                                                                    intent.putExtra("fragment", "negoFragment");
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    _context.startActivity(intent);
                                                                    dialog.dismiss();
                                                                }
                                                            });

                                                            dialog.show();
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
                                                    loadingDialog.hideDialog();
                                                    Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.wtf("",e);
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Log.d("", "onFailure: "+t.getMessage());
                                        loadingDialog.hideDialog();
                                        Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        lastClickTime=SystemClock.elapsedRealtime();
                    }
                });

                dialog.show();
            }
        });

        //jika button setuju ditekan
        holder.btnSetuju.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tampilkan konfirmasi
                final Dialog dialog = new Dialog(_context);
                dialog.setContentView(R.layout.konfirmasi_dialog);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                TextView title = dialog.findViewById(R.id.title);
                TextView description = dialog.findViewById(R.id.description);

                title.setText("Konfirmasi Harga");
                description.setText("Kesepakatan harga akan dibuat jika anda memilih setuju");

                btnBatal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //jika setuju lanjut ke request untuk update cart
                btnSetuju.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                            return;
                        }
                        else {
                            try {
                                dialog.dismiss();
                                loadingDialog = new LoadingDialog(_context);
                                loadingDialog.showDialog();
                                Call<JsonObject> setujuNegoCall = RetrofitClient
                                        .getInstance()
                                        .getApi()
                                        .request(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_cart set harga_konsumen=harga_sales, update_date=now() where company_id="+
                                                SharedPrefManager.getInstance(_context).getUser().getCompanyId()+" and barang_id="+
                                                listNego.get(position).getBarang().getId()+" and status='A' returning harga_sales,qty,history_nego_id;")));


                                setujuNegoCall.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        if(response.isSuccessful()){
                                            String status = response.body().getAsJsonObject().get("status").getAsString();
                                            if(status.equals("success")){
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context,"Harga telah disepakati", Toast.LENGTH_SHORT).show();
                                                int sales_price = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("harga_sales").getAsInt();
                                                int qty= response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("qty").getAsInt();
                                                int id_nego_history = response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("history_nego_id").getAsInt();
                                                updateDealToHistory(sales_price,qty,id_nego_history);
                                                Log.d("price_deal", "onResponse: price "+sales_price);
//                                                Intent intent = new Intent(_context, MainActivity.class);
//                                                intent.putExtra("fragment", "negoFragment");
//                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                _context.startActivity(intent);
                                            }
                                            else if(status.equals("error")){
                                                loadingDialog.hideDialog();
                                                Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    private void updateDealToHistory(int passing_price, int passing_qty, int passing_id) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.add(Calendar.HOUR, 1);
                                        Date DateTime = calendar.getTime();
                                        SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        String Date = datetime.format(DateTime);
                                        int deal = passing_price / passing_qty / listNego.get(position).getBerat();
                                        String q= "UPDATE gcm_history_nego set harga_final = "+ deal +"  where id = "+ passing_id +" returning id;";
                                        Log.d("price_deal",q);

                                        try {
                                            loadingDialog = new LoadingDialog(_context);
                                            loadingDialog.showDialog();
                                            Call<JsonObject> setujuNegoCall = RetrofitClient
                                                    .getInstance()
                                                    .getApi()
                                                    .request(new JSONRequest(QueryEncryption.Encrypt(q)));
                                            Log.d("price_deal",QueryEncryption.Encrypt(q));

                                            setujuNegoCall.enqueue(new Callback<JsonObject>() {
                                                @Override
                                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                    if(response.isSuccessful()){
                                                        String status = response.body().getAsJsonObject().get("status").getAsString();
                                                        if(status.equals("success")){
                                                            final Dialog dialog = new Dialog(_context);
                                                            dialog.setContentView(R.layout.dialog_handle);
                                                            Window window = dialog.getWindow();
                                                            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                            ImageView image = dialog.findViewById(R.id.iconImage);
                                                            TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                                                            Button btnSetuju = dialog.findViewById(R.id.btnYa);
                                                            TextView title = dialog.findViewById(R.id.judul);
                                                            TextView description = dialog.findViewById(R.id.isi);
                                                            dialog.setCancelable(false);
                                                            btnBatal.setVisibility(View.GONE);

                                                            title.setText("Nego Berhasil Disepakati");
                                                            description.setText("Harga nego berlaku dalam hari yang sama. Silakan membuat pesanan sebelum pukul 24:00 untuk dapat menggunakan harga nego");
                                                            image.setImageResource(R.drawable.ic_chat_black_24dp);

                                                            //jika setuju lanjut ke request
                                                            btnSetuju.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    getTokenSeles(position, "kirim nego");
                                                                    Intent intent = new Intent(_context, MainActivity.class);
                                                                    intent.putExtra("fragment", "negoFragment");
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    _context.startActivity(intent);
                                                                    dialog.dismiss();
                                                                }
                                                            });

                                                            dialog.show();

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
                                                    loadingDialog.hideDialog();
                                                    Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.wtf("",e);
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Log.d("", "onFailure: "+t.getMessage());
                                        loadingDialog.hideDialog();
                                        Toast.makeText(_context,"Koneksi gagal", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        lastClickTime=SystemClock.elapsedRealtime();
                    }
                });

                dialog.show();
            }
        });

        //jika button chat ditekan maka pindah ke ChatActivity
        holder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_context, ChatActivity.class);
                intent.putExtra("from", "nego");
                intent.putExtra("companyId", listNego.get(position).getBarang().getCompanyId());
                intent.putExtra("id", listNego.get(position).getBarang().getId());
                if (listNego.get(position).getBarang().getFlag_foto().equals("Y")) {
                    intent.putExtra("img", listNego.get(position).getBarang().getFoto());
                }else{
                    intent.putExtra("img", "https://www.glob.co.id/admin/assets/images/no_image.png");
                }
                intent.putExtra("nama", listNego.get(position).getBarang().getNama());
                intent.putExtra("harga_terakhir", listNego.get(position).getHargaSales());
                _context.startActivity(intent);
            }
        });

        holder.tvHistoryNego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(_context, DetailHistoryNego.class);
                    intent.putExtra("from", "nego");
                    intent.putExtra("companyId", listNego.get(position).getBarang().getCompanyId());
                    intent.putExtra("id", listNego.get(position).getBarang().getId());
                    if (listNego.get(position).getBarang().getFlag_foto().equals("Y")) {
                        intent.putExtra("img", listNego.get(position).getBarang().getFoto());
                    }else{
                        intent.putExtra("img", "https://www.glob.co.id/admin/assets/images/no_image.png");
                    }
                    intent.putExtra("nama", listNego.get(position).getBarang().getNama());
                    intent.putExtra("harga_terakhir", listNego.get(position).getHargaSales());
                    intent.putExtra("id_nego",String.valueOf(listNego.get(position).getIdCart()));
                    intent.putExtra("id_nego_history",String.valueOf(listNego.get(position).getHistory_id()));
                    intent.putExtra("count",String.valueOf(listNego.get(position).getNegoCount()));
                    intent.putExtra("timeRespon", String.valueOf(listNego.get(position).getTime_respon()));

                    _context.startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
    }

    private void sendNotif(ArrayList<String> listToken){
        String title = "My Title";
        String content = "My message";

        Data data = new Data(title, content);
        NotificationBody body = new NotificationBody(listToken, data);

        API api = RetrofitClient.getClient().create(API.class) ;

        Call<ResponseBody> call = api.sendNotification(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: sukses kirim pesan");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "Error: ", t);
            }
        });
    }

    private void getTokenSeles(final int position, final String statusNotif){
        String query = "";
        final ArrayList<String> listToken = new ArrayList<>();
        if (statusNotif.equals("kirim nego")) {
            query = "select a.*, b.status from(select * from gcm_notification_token " +
                    "where user_id in (select id_sales from gcm_company_listing_sales " +
                    "where buyer_id = "+SharedPrefManager.getInstance(_context).getUser().getCompanyId()+" and seller_id = "+listNego.get(position).getBarang().getCompanyId()+" " +
                    "and status = 'A') or company_id = "+SharedPrefManager.getInstance(_context).getUser().getCompanyId()+" ) as a, " +
                    "(select status from gcm_master_cart where id = "+listNego.get(position).getIdCart()+") as b order by id desc";
        }else{
            query = "select distinct token from gcm_notification_token where user_id in ("+SharedPrefManager.getInstance(_context).getUser().getUserId()+")";
        }
        Log.d(TAG, "getTokenSeles: "+query);
        try {
            Call<JsonObject> callGetToken = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetToken.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                listToken.add(i, jsonArray.get(i).getAsJsonObject().get("token").getAsString());
                            }
                            Log.d(TAG, "tes notif: "+listNego.get(position).getBarang().getNama()+", "+listNego.get(position).getBarang().getPersen_nego_1()+", "+listNego.get(position).getBarang().getPersen_nego_2()+", "+listNego.get(position).getBarang().getPersen_nego_3());
                            if (listNego.get(position).getBarang().getPersen_nego_1()==0 && listNego.get(position).getBarang().getPersen_nego_2()==0 && listNego.get(position).getBarang().getPersen_nego_3()==0){
                                String flag = "sales";
                                insertNotif(position, flag);
//                                sendNotif(listToken);
                                sendNotifNegoPersen(position, "nego_sales", "0");
                                Log.d(TAG, "notif: nego sales");
                            }else{
                                String flag = "AI";
                                insertNotif(position, flag);
                                sendNotifNegoPersen(position, "nego_persen", "3600000");
                            }

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

    private void insertNotif(int position, String flag){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        long timestamp = calendar.getTimeInMillis();

        long timestamp_now = Calendar.getInstance().getTimeInMillis();
        String query = "";
        if (flag.equals("sales")){
            query = "insert into gcm_notification_nego (barang_id, buyer_id, seller_id, source, status, timestamp_kirim) values (" +
                    listNego.get(position).getBarang().getId() + ", " +
                    SharedPrefManager.getInstance(_context).getUser().getCompanyId() + ", " +
                    listNego.get(position).getBarang().getCompanyId() + ", " +
                    "'buyer', 'nego', '"+timestamp_now+"') returning id;";
        }else if (flag.equals("salesApprove")){
            query = "insert into gcm_notification_nego (barang_id, buyer_id, seller_id, source, status, timestamp_kirim) values (" +
                    listNego.get(position).getBarang().getId() + ", " +
                    SharedPrefManager.getInstance(_context).getUser().getCompanyId() + ", " +
                    listNego.get(position).getBarang().getCompanyId() + ", " +
                    "'buyer', 'approve', '"+timestamp_now+"') returning id;";
        }else if (flag.equals("AI")) {
            query = "insert into gcm_notification_nego (barang_id, buyer_id, seller_id, date, source, status, timestamp_kirim) values (" +
                    listNego.get(position).getBarang().getId() + ", " +
                    SharedPrefManager.getInstance(_context).getUser().getCompanyId() + ", " +
                    listNego.get(position).getBarang().getCompanyId() + ", " +
                    "now() + interval '1 hour', " +
                    "'seller', 'nego', '"+timestamp+"') returning id;";
        }
        Log.d(TAG, "insertNotif: "+query);
        try {
            Call<JsonObject> callInsertNotif = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callInsertNotif.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d(TAG, "sukses insert notif: YES");
                    }else{
                        Log.d(TAG, "sukses insert notif: NO");
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

    private void sendNotifNegoPersen(int position, String nego_type, String timeout){
        String id_cart = String.valueOf(listNego.get(position).getIdCart());
        String company_id_buyer = String.valueOf(SharedPrefManager.getInstance(_context).getUser().getCompanyId());
        String company_id_seller = String.valueOf(listNego.get(position).getBarang().getCompanyId());

        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("nego_type", nego_type);
            requestBody.put("timeout", timeout);
            requestBody.put("id_cart", id_cart);
            requestBody.put("company_id_buyer", company_id_buyer);
            requestBody.put("company_id_seller", company_id_seller);

            Log.d(TAG, "sendNotifNegoPersen: "+String.valueOf(requestBody));

            Call<ModelNotif> call = mApi.sendNotifNegoPersen(requestBody);
            call.enqueue(new Callback<ModelNotif>() {
                @Override
                public void onResponse(Call<ModelNotif> call, Response<ModelNotif> response) {
                    Log.d(TAG, "tesNotif: berhasilll");
                }

                @Override
                public void onFailure(Call<ModelNotif> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listNego.size();
    }

    /**Method untuk mengatur visibility icon jatah nego*/
    private void jatahNego(int negoCount, NegoAdapter.ViewHolder holder){
        if(negoCount==1){
            holder.jatah1.setVisibility(View.VISIBLE);
            holder.jatah2.setVisibility(View.VISIBLE);
            holder.jatahTerpakai1.setVisibility(View.VISIBLE);
            holder.jatahTerpakai2.setVisibility(View.GONE);
            holder.jatahTerpakai3.setVisibility(View.GONE);
        }
        else if(negoCount==2){
            holder.jatah1.setVisibility(View.VISIBLE);
            holder.jatah2.setVisibility(View.GONE);
            holder.jatahTerpakai1.setVisibility(View.VISIBLE);
            holder.jatahTerpakai2.setVisibility(View.VISIBLE);
            holder.jatahTerpakai3.setVisibility(View.GONE);
        }
        else if(negoCount==3){
            holder.jatah1.setVisibility(View.GONE);
            holder.jatah2.setVisibility(View.GONE);
            holder.jatahTerpakai1.setVisibility(View.VISIBLE);
            holder.jatahTerpakai2.setVisibility(View.VISIBLE);
            holder.jatahTerpakai3.setVisibility(View.VISIBLE);
        }
    }
}
