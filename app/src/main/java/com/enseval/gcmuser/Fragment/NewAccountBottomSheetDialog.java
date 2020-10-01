package com.enseval.gcmuser.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.PengaturanAkunActivity;
import com.enseval.gcmuser.Utilities.Regex;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewAccountBottomSheetDialog extends BottomSheetDialog {

    public NewAccountBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    private TextInputLayout namaPengguna, noktp, emailPengguna, nohp, username, password, konfirmasiPassword;
    private Button registerBtn;
    private LoadingDialog loadingDialog;
    private long lastClickTime=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_account_bottom_sheet_dialog);

        loadingDialog = new LoadingDialog(getContext());

        namaPengguna = findViewById(R.id.namaPenggunaLayout);
        noktp = findViewById(R.id.noktpLayout);
        emailPengguna = findViewById(R.id.emailPenggunaLayout);
        nohp = findViewById(R.id.nohpLayout);
        username = findViewById(R.id.usernameLayout);
        password = findViewById(R.id.passwordLayout);
        konfirmasiPassword = findViewById(R.id.passwordLayout2);
        registerBtn = findViewById(R.id.registerBtn);

        checkValidation(); //pengecekan apakah form sudah terisi semua

        namaPengguna.getEditText().addTextChangedListener(mWatcher);
        noktp.getEditText().addTextChangedListener(mWatcher);
        emailPengguna.getEditText().addTextChangedListener(mWatcher);
        nohp.getEditText().addTextChangedListener(mWatcher);
        username.getEditText().addTextChangedListener(mWatcher);
        password.getEditText().addTextChangedListener(mWatcher);
        konfirmasiPassword.getEditText().addTextChangedListener(mWatcher);

        //saat button register ditekan
        registerBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                        return;
                    }
                    else {
//                        checkUsername(); //cek username apakah tersedia atau tidak
                        checkUniqData();
                    }
                    lastClickTime= SystemClock.elapsedRealtime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //text watcher untuk handle input yang dimasukkan
    TextWatcher mWatcher = new TextWatcher() {
        int len=0;
        final android.os.Handler handler = new android.os.Handler();
        Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //cek apakah password dan konfirmasinya sesuai
            if(konfirmasiPassword.getEditText().getText().hashCode()==s.hashCode()){
                if(!konfirmasiPassword.getEditText().getText().toString().equals(password.getEditText().getText().toString())){
                    registerBtn.setEnabled(false);
                    konfirmasiPassword.setErrorEnabled(true);
                    konfirmasiPassword.setError("Password tidak cocok");
                }
                else{
                    konfirmasiPassword.setErrorEnabled(false);
                }
            }
            handler.removeCallbacks(runnable);
            checkValidation(); //cek apakah sudah terisi semua
        }

        @Override
        public void afterTextChanged(final Editable s) {
            //pengecekan apakah input sudah valid/sesuai format atau belum, seperti pada registrasi
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(noktp.getEditText().getText().hashCode()==s.hashCode()){
                        if(noktp.getEditText().getText().length()!=16){
                            noktp.setErrorEnabled(true);
                            noktp.setError("Tidak valid");
                        }
                        else {
                            noktp.setErrorEnabled(false);
                        }
                    }

                    if(emailPengguna.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mEmailPemilik = Regex.emailPattern.matcher(emailPengguna.getEditText().getText());
                        if (!mEmailPemilik.find()){
                            registerBtn.setEnabled(false);
                            emailPengguna.setErrorEnabled(true);
                            emailPengguna.setError("Harap masukkan email yang valid");
                        }
                        else{
                            emailPengguna.setErrorEnabled(false);
                        }
                    }

                    if(nohp.getEditText().getText().hashCode()==s.hashCode()){
                        if(nohp.getEditText().getText().length()<10){
                            nohp.setErrorEnabled(true);
                            nohp.setError("Tidak valid");
                        }
                        else {
                            nohp.setErrorEnabled(false);
                        }
                    }

                    if(username.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mUsername = Regex.usernamePattern.matcher(username.getEditText().getText());
                        if (!mUsername.find()){
                            registerBtn.setEnabled(false);
                            username.setErrorEnabled(true);
                            username.setError("Minimal 8 karakter huruf atau angka");
                        }
                        else{
                            username.setErrorEnabled(false);
                        }
                    }

                    if(password.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mPassword = Regex.passwordPattern.matcher(password.getEditText().getText());
                        if (!mPassword.find()){
                            registerBtn.setEnabled(false);
                            password.setErrorEnabled(true);
                            password.setError("Password minimal 8 karakter dan harus terdiri dari A-Z, a-z, 0-9");
                        }
                        else{
                            password.setErrorEnabled(false);
                        }

                        if(!konfirmasiPassword.getEditText().getText().toString().equals(password.getEditText().getText().toString())){
                            registerBtn.setEnabled(false);
                            konfirmasiPassword.setErrorEnabled(true);
                            konfirmasiPassword.setError("Password tidak cocok");
                        }
                        else{
                            konfirmasiPassword.setErrorEnabled(false);
                        }
                    }
                }
            };
            handler.postDelayed(runnable, 700);
            checkValidation();
        }
    };

    /**Method untuk mengecek apakah form sudah terisi semua atau belum*/
    private void checkValidation(){
        if ((TextUtils.isEmpty(namaPengguna.getEditText().getText()))
                || (TextUtils.isEmpty(noktp.getEditText().getText()))
                || (TextUtils.isEmpty(emailPengguna.getEditText().getText()))
                || (TextUtils.isEmpty(nohp.getEditText().getText()))
                || (TextUtils.isEmpty(username.getEditText().getText()))
                || (TextUtils.isEmpty(password.getEditText().getText()))
                || (TextUtils.isEmpty(konfirmasiPassword.getEditText().getText()))
                || emailPengguna.getError() != null
                || username.isErrorEnabled()
                || password.getError() != null
                || konfirmasiPassword.getError() != null
                || noktp.isErrorEnabled()
        ){
            registerBtn.setEnabled(false);
        }
        else{
            registerBtn.setEnabled(true);
        }
    }

    /**Method pengecekan apakah username sudah dipakai atau belum*/
    private void checkUsername() throws Exception {
        loadingDialog.showDialog();
        final Call<JsonObject> checkUsernameCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT nama FROM gcm_master_user where username='"+username.getEditText().getText().toString()+"';")));

        checkUsernameCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        if(jsonArray.size()>0){
                            loadingDialog.hideDialog();
                            Toast.makeText(getContext(),"Username telah dipakai",Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(status.equals("error")){
                        userRegister(); //jika username tersedia, lanjutkan registrasi
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("", "onFailure: "+t.getMessage());
                try {
                    loadingDialog.hideDialog();
                    Toast.makeText(getContext(), "Koneksi gagal", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void userRegister(){
        try {
            String query = "insert into gcm_master_user " +
                    "(nama, no_ktp, email, no_hp, username, password, status, role, company_id, create_by, update_by, notes_blacklist) values (" +
                    "'"+namaPengguna.getEditText().getText().toString()+"', " +
                    "'"+noktp.getEditText().getText().toString()+"', " +
                    "'"+emailPengguna.getEditText().getText().toString()+"', " +
                    "'"+nohp.getEditText().getText().toString()+"', " +
                    "'"+username.getEditText().getText().toString()+"', " +
                    "'"+QueryEncryption.Encrypt(password.getEditText().getText().toString())+"', " +
                    "'I', 'user', " +
                    SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+", " +
                    SharedPrefManager.getInstance(getContext()).getUser().getUserId()+", " +
                    SharedPrefManager.getInstance(getContext()).getUser().getUserId()+", '') returning id";
            Log.d("ido", "userRegister: "+QueryEncryption.Encrypt(query));
            Call<JsonObject> callRegisterUser = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callRegisterUser.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        loadingDialog.hideDialog();
                        Toast.makeText(getContext(), "Register berhasil!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getContext(), PengaturanAkunActivity.class);
                        getContext().startActivity(i);
                        dismiss();
                    }else{
                        loadingDialog.hideDialog();
                        Toast.makeText(getContext(), "Register gagal!", Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
            loadingDialog.hideDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Method untuk mengirimkan data-data registrasi user baru*/
    private void registerUser() {
        try {
            Call<JsonObject> registerUserCall = RetrofitClient
                    .getInstance2()
                    .getApi()
                    .requestInsert(new JSONRequest(QueryEncryption.Encrypt("INSERT INTO gcm_master_user " +
                            "(nama, no_ktp, email, no_hp, username, password, status, role, company_id, create_by, update_by) " +
                            "VALUES ('"+ namaPengguna.getEditText().getText().toString()+"'," +
                            "'"+noktp.getEditText().getText().toString()+"'," +
                            "'"+ emailPengguna.getEditText().getText().toString()+"'," +
                            "'"+nohp.getEditText().getText().toString()+"'," +
                            "'"+username.getEditText().getText().toString()+"'," +
                            "'"+QueryEncryption.Encrypt(password.getEditText().getText().toString())+"', 'A', " +
                            "'user'," +
                            SharedPrefManager.getInstance(getContext()).getUser().getCompanyId() +"," +
                            +SharedPrefManager.getInstance(getContext()).getUser().getUserId()+"," +
                            +SharedPrefManager.getInstance(getContext()).getUser().getUserId()+");")));
                registerUserCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            loadingDialog.hideDialog();
                            Toast.makeText(getContext(), "Register berhasil!", Toast.LENGTH_LONG).show();
                            dismiss();
                        }
                        else if(status.equals("error")){
                            loadingDialog.hideDialog();
                            Toast.makeText(getContext(), "Register gagal", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        loadingDialog.hideDialog();
                        Toast.makeText(getContext(), "Register gagal ga sukses", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    Toast.makeText(getContext(), "Koneksi gagal", Toast.LENGTH_LONG).show();
                    Log.d("", "onFailure: "+t.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkUniqData(){
        String query = "select * from " +
                "(select count (username) as check_username from gcm_master_user gmu where username like '"+username.getEditText().getText().toString()+"') a, " +
                "(select count (no_hp) check_nohp from gcm_master_user gmu where no_hp like '"+nohp.getEditText().getText().toString()+"') b, " +
                "(select count (email) check_email from gcm_master_user gmu where email like '"+emailPengguna.getEditText().getText().toString()+"') c, " +
                "(select count (no_ktp) check_no_ktp from gcm_master_user gmu where no_ktp like '"+noktp.getEditText().getText().toString()+"') d ";

        try {
            loadingDialog.showDialog();
            Log.d("ido", "checkUniqData: "+QueryEncryption.Encrypt(query));
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
                            int no_ktp_check = jsonArray.get(0).getAsJsonObject().get("check_no_ktp").getAsInt();
                            Log.d("ido", "isi check: "+username);
                            if (username_check==0) {
                                Log.d("ido", "onResponse: " + username);
                                if (no_hp_check == 0) {
                                    if (email_check == 0) {
                                        if (no_ktp_check == 0) {
                                            userRegister();
                                        }else{
                                            loadingDialog.hideDialog();
                                            Toast.makeText(getContext(), "Nomor KTP anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                        }
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
}