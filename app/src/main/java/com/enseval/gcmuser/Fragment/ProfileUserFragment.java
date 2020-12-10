package com.enseval.gcmuser.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.AllChatsActivity;
import com.enseval.gcmuser.Activity.DistributorActivity;
import com.enseval.gcmuser.Activity.ListAlamat;
import com.enseval.gcmuser.Activity.LoginActivity;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Activity.PengaturanAkunActivity;
import com.enseval.gcmuser.Activity.TentangKamiActivity;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class ProfileUserFragment extends Fragment {
    private TextView namaPengguna, username, nomorIdentitas, nomorHandphone, emailPengguna, role, statusPengguna;
    private TextView namaPerusahaan, tipeBisnis, npwp, siup, teleponPerusahaan, emailPerusahaan;
    private CardView distributor, pengaturanAkun, pengaturanAlamat, tentangKami;
    private ImageButton editAkun;
    private Button keluar;
    private long lastClickTime = 0;
    private LoadingDialog loadingDialog;
    private String TAG = "ido";

    public ProfileUserFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //INFORMASI AKUN
        namaPengguna = view.findViewById(R.id.namaPengguna);
        username = view.findViewById(R.id.username);
        nomorIdentitas = view.findViewById(R.id.nomorIdentitas);
        nomorHandphone = view.findViewById(R.id.nomorHandphone);
        emailPengguna = view.findViewById(R.id.emailPengguna);
        role = view.findViewById(R.id.rolePengguna);
        statusPengguna = view.findViewById(R.id.statuspengguna);
        editAkun = view.findViewById(R.id.editAkun);

        //INFORMASI PERUSAHAAN
        namaPerusahaan = view.findViewById(R.id.namaPerusahaan);
        tipeBisnis = view.findViewById(R.id.tipeBisnis);
        npwp = view.findViewById(R.id.npwp);
        siup = view.findViewById(R.id.siup);
        teleponPerusahaan = view.findViewById(R.id.telepon);
        emailPerusahaan = view.findViewById(R.id.emailPerusahaan);

        distributor = view.findViewById(R.id.distributor);
        pengaturanAkun = view.findViewById(R.id.pengaturanAkun);
        pengaturanAlamat = view.findViewById(R.id.pengaturanAlamat);
        tentangKami = view.findViewById(R.id.tentangKami);

        keluar = view.findViewById(R.id.keluar);

        loadingDialog = new LoadingDialog(getActivity());

        getDataPengguna();
        getDataPerusahaan();

        //EDIT AKUN
        editAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_edit_user);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                ImageButton close = dialog.findViewById(R.id.btnClose);
                final TextInputEditText username = dialog.findViewById(R.id.USERNAME);
                final TextInputEditText email = dialog.findViewById(R.id.EMAIL);
                final TextInputEditText nomor_hp = dialog.findViewById(R.id.NOHP);
                final TextInputEditText password = dialog.findViewById(R.id.PASSWORD);
                final TextInputEditText passlama = dialog.findViewById(R.id.PASSLAMA);
                Button simpan = dialog.findViewById(R.id.btnSimpan);
                final String[] passLama = {""};
                final String query = "select nama, username, email, no_hp, password from gcm_master_user gmu " +
                        "where id = "+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+";";
                try {
                    Call<JsonObject> callUser = RetrofitClient
                            .getInstance()
                            .getApi()
                            .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                    callUser.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful()){
                                String status = response.body().getAsJsonObject().get("status").getAsString();
                                if (status.equals("success")){
                                    JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                    username.setText(jsonArray.get(0).getAsJsonObject().get("username").getAsString());
                                    email.setText(jsonArray.get(0).getAsJsonObject().get("email").getAsString());
                                    nomor_hp.setText(jsonArray.get(0).getAsJsonObject().get("no_hp").getAsString());
                                    passLama[0] = jsonArray.get(0).getAsJsonObject().get("password").getAsString();
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
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                simpan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        String query = "select * from " +
                                "(select count (username) as check_username from gcm_master_user gmu where username like '"+username.getText().toString()+"'  and id not in("+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+")) a, " +
                                "(select count (no_hp) check_nohp from gcm_master_user gmu where no_hp like '"+nomor_hp.getText().toString()+"'  and id not in("+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+")) b, " +
                                "(select count (email) check_email from gcm_master_user gmu where email like '"+email.getText().toString()+"'  and id not in("+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+")) c ";

                        try {
                            loadingDialog.showDialog();
                            Log.d("ido", "checkUniqData: "+query);
                            Call<JsonObject> callCheckUniqData = RetrofitClient
                                    .getInstance()
                                    .getApi()
                                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                            callCheckUniqData.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    if (response.isSuccessful()){
                                        String status = response.body().getAsJsonObject().get("status").getAsString();
                                        Log.d("ido", "onResponse: sukses kok "+status);
                                        if (status.equals("success")){
                                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                            int username_check = jsonArray.get(0).getAsJsonObject().get("check_username").getAsInt();
                                            int no_hp_check = jsonArray.get(0).getAsJsonObject().get("check_nohp").getAsInt();
                                            int email_check = jsonArray.get(0).getAsJsonObject().get("check_email").getAsInt();
                                            if (username_check==0) {
                                                if (no_hp_check == 0) {
                                                    if (email_check == 0) {
                                                        UpdateDataAkun(username.getText().toString(), nomor_hp.getText().toString(), passlama.getText().toString(), password.getText().toString(), email.getText().toString(), passLama);
                                                    }else{
                                                        loadingDialog.hideDialog();
                                                        Toast.makeText(getContext(), "Alamat email anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                                    }
                                                }else{
                                                    loadingDialog.hideDialog();
                                                    Toast.makeText(getContext(), "Nomor HP anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                                }
                                            }else{
                                                loadingDialog.hideDialog();
                                                Toast.makeText(getContext(), "Username anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                            }
                                        }else{
                                            loadingDialog.hideDialog();
                                        }
                                    }else{
                                        loadingDialog.hideDialog();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    loadingDialog.hideDialog();
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            }
        });

        //DISTRIBUTOR
        distributor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                } else {
                    Intent i = new Intent(getContext(), DistributorActivity.class);
                    startActivity(i);
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        //PENGATURAN AKUN
        pengaturanAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime<1000){
                    return;
                }else{
                    Intent i = new Intent(getContext(), PengaturanAkunActivity.class);
                    startActivity(i);
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        //PENGATURAN ALAMAT
        pengaturanAlamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                } else {
                    Intent intent = new Intent(getContext(), ListAlamat.class);
                    intent.putExtra("tipe", "edit");
                    startActivity(intent);
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        //TENTANG KAMI
        tentangKami.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                } else {
                    Intent intent = new Intent(getContext(), TentangKamiActivity.class);
                    startActivity(intent);
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        //BUTTON KELUAR
        keluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tampilkan dialog konfirmasi
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                builder.setMessage("Anda yakin ingin keluar dari akun ini?");
                builder.setPositiveButton("Ya",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //jika ya, hapus info login dari local storage.
                            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return;
                            } else {
                                deleteToken();
                                SharedPrefManager.getInstance(getActivity()).clearUser();
                                SharedPrefManager.getInstance(getActivity()).clearActiveSeller();
                                SharedPrefManager.getInstance(getActivity()).clearToken();
                                SharedPrefManager.getInstance(getActivity()).clearMessages();
                                SharedPrefManager.getInstance(getActivity()).clearRoomMessages();
//                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            lastClickTime = SystemClock.elapsedRealtime();
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
                pButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            }
        });
    }

    private void getDataPengguna(){
        String query = "select nama, username, no_ktp, no_hp, email, role, status from gcm_master_user gmu " +
                "where id = "+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+";";
        Log.d(TAG, "query getDataPengguna: "+query);
        try {
            Call<JsonObject> callDataPengguna = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callDataPengguna.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            namaPengguna.setText(jsonArray.get(0).getAsJsonObject().get("nama").getAsString());
                            username.setText(jsonArray.get(0).getAsJsonObject().get("username").getAsString());
                            nomorIdentitas.setText(jsonArray.get(0).getAsJsonObject().get("no_ktp").getAsString());
                            nomorHandphone.setText(jsonArray.get(0).getAsJsonObject().get("no_hp").getAsString());
                            emailPengguna.setText(jsonArray.get(0).getAsJsonObject().get("email").getAsString());
                            role.setText(jsonArray.get(0).getAsJsonObject().get("role").getAsString());
                            String statusP = jsonArray.get(0).getAsJsonObject().get("status").getAsString();
                            if (statusP.equals("A")){
                                statusPengguna.setText("Aktif");
                            }else if (statusP.equals("I")){
                                statusPengguna.setText("Belum Aktif");
                            }
                            if (role.getText().toString().equals("user")){
                                pengaturanAkun.setVisibility(GONE);
                            }else{
                                pengaturanAkun.setVisibility(View.VISIBLE);
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

    private void getDataPerusahaan(){
        String query = "select nama_perusahaan, b.nama, no_npwp, no_siup, no_telp, email from " +
                "gcm_master_company a inner join gcm_master_category b on a.tipe_bisnis = b.id " +
                "where a.id = "+SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+";";
        Log.d(TAG, "query getDataPerusahaan: "+query);
        try {
            Call<JsonObject> callDataPerusahaan = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callDataPerusahaan.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            namaPerusahaan.setText(jsonArray.get(0).getAsJsonObject().get("nama_perusahaan").getAsString());
                            tipeBisnis.setText(jsonArray.get(0).getAsJsonObject().get("nama").getAsString());
                            npwp.setText(jsonArray.get(0).getAsJsonObject().get("no_npwp").getAsString());
                            siup.setText(jsonArray.get(0).getAsJsonObject().get("no_siup").getAsString());
                            teleponPerusahaan.setText(jsonArray.get(0).getAsJsonObject().get("no_telp").getAsString());
                            emailPerusahaan.setText(jsonArray.get(0).getAsJsonObject().get("email").getAsString());
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

    private void deleteToken(){
        loadingDialog.showDialog();
        String query = "delete from gcm_notification_token where token = '"+SharedPrefManager.getInstance(getContext()).getToken()+"' and user_id = "+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+" " +
                "and company_id = "+SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+"";
        Log.d(TAG, "deleteToken: "+query);
        try {
            Call<JsonObject> callDeleteToken = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callDeleteToken.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e){

        }
    }

    private void UpdateDataAkun(String username, String nomor_hp, String passlama, String password, String email, String[] passLama){
        Log.d(TAG, "UpdateDataAkun: "+passlama+" "+passLama[0]);
        try {
            if (passlama.equals("")){
                Toast.makeText(getContext(), "Kata Sandi Lama harus diisi terlebih dahulu", Toast.LENGTH_LONG).show();
                loadingDialog.hideDialog();
            }else {
                if (QueryEncryption.Encrypt(passlama).equals(passLama[0])) {
                    String queryUpdate = "";
                    if (password.equals("")) {
                        queryUpdate = "update gcm_master_user set username = '" + username + "', email = '" + email + "', no_hp = '" + nomor_hp + "' " +
                                "where id = " + SharedPrefManager.getInstance(getContext()).getUser().getUserId() + ";";
                    } else {
                        queryUpdate = "update gcm_master_user set username = '" + username + "', email = '" + email + "', no_hp = '" + nomor_hp + "', password = '" + QueryEncryption.Encrypt(password) + "' " +
                                "where id = " + SharedPrefManager.getInstance(getContext()).getUser().getUserId() + ";";
                    }
                    Log.d(TAG, "QueryUpdate: " + queryUpdate);

                    Call<JsonObject> callUpdate = RetrofitClient
                            .getInstance()
                            .getApi()
                            .request(new JSONRequest(QueryEncryption.Encrypt(queryUpdate)));
                    callUpdate.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            loadingDialog.hideDialog();
                            Intent i = new Intent(getContext(), MainActivity.class);
                            i.putExtra("fragment", "profileFragment");
                            startActivity(i);
                            getActivity().finish();
                            Toast.makeText(getContext(), "Update data berhasil", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            loadingDialog.hideDialog();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Kata Sandi Lama salah", Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
