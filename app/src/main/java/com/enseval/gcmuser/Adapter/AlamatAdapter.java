package com.enseval.gcmuser.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.CheckoutActivity;
import com.enseval.gcmuser.Activity.ListAlamat;
import com.enseval.gcmuser.Activity.TambahAlamatActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Alamat;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlamatAdapter extends RecyclerView.Adapter<AlamatAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Alamat> alamatList;
    private ArrayList<Cart> cartList;
    private ArrayList<Company> companyList;
    private long total;
    private float kurs;
    private String flag;
    private String status;
    private LoadingDialog loadingDialog;
    String TAG = "ido";
    private long lastClickTime = 0;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView alamat;
        private final TextView kelkec;
        private final TextView kota;
        private CardView pilihAlamat;
        private Button btnHapus, btnUbah, btnUtamakan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            alamat = itemView.findViewById(R.id.tvAlamattv);
            kelkec = itemView.findViewById(R.id.tvKelKec);
            kota = itemView.findViewById(R.id.tvKot);
            pilihAlamat = itemView.findViewById(R.id.alamatklik);
            loadingDialog = new LoadingDialog(context);
            btnHapus = itemView.findViewById(R.id.btnHapus);
            btnUbah = itemView.findViewById(R.id.btnUbah);
            btnUtamakan = itemView.findViewById(R.id.btnUtamakan);
        }
    }

    public AlamatAdapter(Context context, ArrayList<Alamat> alamatList, ArrayList<Cart> cartList,
                         String flag, ArrayList<Company> companyList, long total, String status) {
        this.context = context;
        this.alamatList = alamatList;
        this.cartList = cartList;
        this.flag = flag;
        this.companyList = companyList;
        this.total = total;
        this.kurs = kurs;
        this.status = status;
    }

    @NonNull
    @Override
    public AlamatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.alamat, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlamatAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: "+status);
        if (alamatList.get(position).getShipto_active().equals("Y")){
            holder.btnUtamakan.setVisibility(View.INVISIBLE);
        }else{
            holder.btnUtamakan.setVisibility(View.VISIBLE);
            holder.btnUtamakan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    utamakanAlamat(alamatList.get(position).getId());
                }
            });
        }
        holder.alamat.setText(alamatList.get(position).getAlamat().toUpperCase() + ",");
        holder.kelkec.setText(alamatList.get(position).getKelurahan() + ", " + alamatList.get(position).getKecamatan() + ",");
        holder.kota.setText(alamatList.get(position).getKota() + " - " + alamatList.get(position).getProvinsi() + ", " + alamatList.get(position).getKodepos());
        if (status.equals("pilih")) {
            holder.btnUtamakan.setVisibility(View.GONE);
            holder.btnUbah.setVisibility(View.GONE);
            holder.btnHapus.setVisibility(View.GONE);
            holder.pilihAlamat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag.equals("shipto")) {
                        loadingDialog.showDialog();
                        for (int i = 0; i < cartList.size(); i++) {
                            updateShipToCart(alamatList.get(position).getId(), cartList.get(i).getId());
                        }
                        Intent intent = new Intent(context, CheckoutActivity.class);
                        intent.putParcelableArrayListExtra("listSeller", companyList);
                        intent.putExtra("total", total);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        ((Activity)context).finish();

                    } else if (flag.equals("billto")) {
                        loadingDialog.showDialog();
                        for (int i = 0; i < cartList.size(); i++) {
                            updateBillToCart(alamatList.get(position).getId(), cartList.get(i).getId());
                        }
                    }
                }
            });
        }else{
            holder.btnHapus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.konfirmasi_dialog);
                    Window window = dialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                    Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
                    TextView title = dialog.findViewById(R.id.title);
                    TextView description = dialog.findViewById(R.id.description);
                    dialog.setCancelable(false);

                    if (alamatList.get(position).getBillto_active().equals("Y")){
                        btnSetuju.setText("Ok");
                        btnBatal.setVisibility(View.GONE);
                        title.setText("Hapus Alamat");
                        description.setText("Anda Tidak bisa menghapus alamat ini karena merupakan alamat penagihan. jika ingin mengganti silahkan hubungi Administrator GLOB.");

                        btnBatal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        btnSetuju.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                    return;
                                }else{
                                    dialog.dismiss();
                                }
                            }
                        });
                    }else {

                        btnSetuju.setText("Ya");
                        btnBatal.setText("Tidak");

                        title.setText("Hapus Alamat");
                        description.setText("Anda Yakin ingin menghapus alamat ini : \n\n" + alamatList.get(position).getAlamat().toUpperCase() + ",\n" +
                                alamatList.get(position).getKelurahan() + ", " + alamatList.get(position).getKecamatan() + ",\n" +
                                alamatList.get(position).getKota() + " - " + alamatList.get(position).getProvinsi() + ", " + alamatList.get(position).getKodepos());

                        btnBatal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        btnSetuju.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return;
                                } else {
                                    hapusAlamat(alamatList.get(position).getId());
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                    dialog.show();
                }
            });

            holder.btnUbah.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (alamatList.get(position).getBillto_active().equals("Y")){
//                        final Dialog dialog = new Dialog(context);
//                        dialog.setContentView(R.layout.konfirmasi_dialog);
//                        Window window = dialog.getWindow();
//                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                        TextView btnBatal = dialog.findViewById(R.id.btnBatal);
//                        Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
//                        TextView title = dialog.findViewById(R.id.title);
//                        TextView description = dialog.findViewById(R.id.description);
//                        dialog.setCancelable(false);
//                        btnSetuju.setText("Ok");
//                        btnBatal.setVisibility(View.GONE);
//                        title.setText("Ubah Alamat");
//                        description.setText("Anda Tidak bisa mengubah alamat ini karena merupakan alamat penagihan. jika ingin mengubah silahkan hubungi Administrator GLOB.");
//
//                        btnBatal.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                        btnSetuju.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
//                                    return;
//                                }else{
//                                    dialog.dismiss();
//                                }
//                            }
//                        });
//                        dialog.show();
//                    }else {
                        ubahAlamat(alamatList.get(position).getId());
//                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() { return alamatList.size(); }

    private void utamakanAlamat(int id){
        String query = "with new_update_1 as (update gcm_master_alamat set shipto_active = case id when "+id+" then 'Y' else 'N' end where company_id = "+ SharedPrefManager.getInstance(context).getUser().getCompanyId() +") " +
                "update gcm_master_cart set shipto_id = "+id+" where company_id = "+SharedPrefManager.getInstance(context).getUser().getCompanyId()+" and status = 'A';";
        Log.d(TAG, "utamakanAlamat: "+query);
        try {
            Call<JsonObject> callUtamakanAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callUtamakanAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Toast.makeText(context, "Berhasil mengutamakan Alamat", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(context, ListAlamat.class);
                        i.putExtra("tipe", "edit");
                        context.startActivity(i);
                        ((Activity)context).finish();
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

    private void ubahAlamat(int id){
        String query  = "select * from gcm_master_alamat where id = "+id+";";
        Log.d(TAG, "ubahAlamat: "+query);
        try {
            Call<JsonObject> callUbahAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callUbahAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int id = jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
                            String alamat = jsonArray.get(0).getAsJsonObject().get("alamat").getAsString();
                            String provinsi = jsonArray.get(0).getAsJsonObject().get("provinsi").getAsString();
                            String kota = jsonArray.get(0).getAsJsonObject().get("kota").getAsString();
                            String kecamatan = jsonArray.get(0).getAsJsonObject().get("kecamatan").getAsString();
                            String kelurahan = jsonArray.get(0).getAsJsonObject().get("kelurahan").getAsString();
                            String kodepos = jsonArray.get(0).getAsJsonObject().get("kodepos").getAsString();
                            String no_telp = jsonArray.get(0).getAsJsonObject().get("no_telp").getAsString();
                            String billtoActive = jsonArray.get(0).getAsJsonObject().get("billto_active").getAsString();
                            String shiptoActive = jsonArray.get(0).getAsJsonObject().get("shipto_active").getAsString();

                            Intent i = new Intent(context, TambahAlamatActivity.class);
                            i.putExtra("status", "ubah");
                            i.putExtra("id", id);
                            i.putExtra("alamat", alamat);
                            i.putExtra("prov", provinsi);
                            i.putExtra("kota", kota);
                            i.putExtra("kec", kecamatan);
                            i.putExtra("kel", kelurahan);
                            i.putExtra("kodepos", kodepos);
                            i.putExtra("no_telp", no_telp);
                            i.putExtra("billtoActive", billtoActive);
                            i.putExtra("shiptoActive", shiptoActive);
                            context.startActivity(i);
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

    private void hapusAlamat(int id){
        String query = "update gcm_master_alamat set flag_active = 'I' where id = "+id+"";
        Log.d(TAG, "hapusAlamat: "+query);
        try {
            Call<JsonObject> callHapusAlamat = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callHapusAlamat.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Toast.makeText(context, "Alamat Berhasil dihapus", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(context, ListAlamat.class);
                        i.putExtra("tipe", "edit");
                        context.startActivity(i);
                        ((Activity)context).finish();
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

    private void updateShipToCart(int id_ship_to, final int id_cart) {
        String query = "update gcm_master_cart set shipto_id='"+id_ship_to+"' where id="+id_cart;
        try {
            Call<JsonObject> updateShipTo = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            updateShipTo.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    loadingDialog.hideDialog();

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBillToCart(int id_ship_to, int id_cart) {
        String query = "update gcm_master_cart set billto_id='"+id_ship_to+"' where id="+id_cart;
        try {
            Call<JsonObject> updateBillTo = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            updateBillTo.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    loadingDialog.hideDialog();
                    Intent intent = new Intent(context, CheckoutActivity.class);
                    intent.putParcelableArrayListExtra("listSeller", companyList);
                    intent.putExtra("total", total);
                    //intent.putExtra("kurs", kurs);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    ((Activity)context).finish();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    Intent intent = new Intent(context, CheckoutActivity.class);
                    intent.putParcelableArrayListExtra("listSeller", companyList);
                    intent.putExtra("total", total);
                    //intent.putExtra("kurs", kurs);
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
