package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.Utilities.Regex;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    //DIBUAT LEBIH DETAIL LAGI BAGIAN FORGOT PASSWORDNYA DARI AWAL

    private TextInputEditText passBaru, passKonfirmasi;
    private TextInputLayout newPass, confirmPass;
    private Button btnSimpan;
    private LoadingDialog loadingDialog;
    private long lastClickTime=0;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        passBaru = findViewById(R.id.PassBaru);
        passKonfirmasi = findViewById(R.id.KonfirmasiPassword);
        btnSimpan = findViewById(R.id.btnSimpanPass);
        newPass = findViewById(R.id.newPass);
        confirmPass = findViewById(R.id.confirmPass);

        loadingDialog = new LoadingDialog(this);

        email = getIntent().getStringExtra("email");

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    simpanPassword();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        newPass.getEditText().addTextChangedListener(textWatcher);
        confirmPass.getEditText().addTextChangedListener(textWatcher);
    }

    private void simpanPassword(){
        String password;
        try {
            loadingDialog.showDialog();
            if (passBaru.getText().toString().equals(passKonfirmasi.getText().toString())){
                password = QueryEncryption.Encrypt(passBaru.getText().toString());
                String query = "update gcm_master_user set password = '"+password+"' where email = '"+email+"'";
                Call<JsonObject> callSavePass = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(query)));
                callSavePass.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        loadingDialog.hideDialog();
                        if (response.isSuccessful()){
                            final Dialog dialog = new Dialog(ResetPasswordActivity.this);
                            dialog.setContentView(R.layout.dialog_handle);
                            Window window = dialog.getWindow();
                            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            ImageView image = dialog.findViewById(R.id.iconImage);
                            TextView btnBatal = dialog.findViewById(R.id.btnBatal);
                            Button btnSetuju = dialog.findViewById(R.id.btnYa);
                            TextView title = dialog.findViewById(R.id.judul);
                            TextView description = dialog.findViewById(R.id.isi);
                            dialog.setCancelable(false);

                            title.setText("Konfirmasi reset password");
                            description.setText("Password akun Anda akan diubah dengan password yang baru. Lanjutkan ?");
                            //image.setImageResource(R.drawable.ic_chat_black_24dp);

                            btnBatal.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            //jika setuju lanjut ke request
                            btnSetuju.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(i);
                                    finish();
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();

                        }else{
                            loadingDialog.hideDialog();
                            Toast.makeText(getApplicationContext(), "Gagal reset password baru", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            }else{
                loadingDialog.hideDialog();
                Toast.makeText(getApplicationContext(), "Password tidak cocok", Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        final android.os.Handler handler = new android.os.Handler();
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (confirmPass.getEditText().getText().hashCode() == s.hashCode()) {
                if (!confirmPass.getEditText().getText().toString().equals(newPass.getEditText().getText().toString())) {
                    btnSimpan.setEnabled(false);
                    confirmPass.setErrorEnabled(true);
                    confirmPass.setError("Password tidak cocok");
                } else {
                    confirmPass.setErrorEnabled(false);
                }
            }
        }

        @Override
        public void afterTextChanged(final Editable s) {
            if(newPass.getEditText().getText().hashCode()==s.hashCode()){
                Matcher mPassword = Regex.passwordPattern.matcher(newPass.getEditText().getText());
                if (!mPassword.find()){
                    btnSimpan.setEnabled(false);
                    newPass.setErrorEnabled(true);
                    newPass.setError("Password minimal 8 karakter dan harus terdiri dari huruf besar, kecil, dan angka");
                }
                else{
                    newPass.setErrorEnabled(false);
                }
                if(!confirmPass.getEditText().getText().toString().equals(newPass.getEditText().getText().toString())){
                    btnSimpan.setEnabled(false);
                    confirmPass.setErrorEnabled(true);
                    confirmPass.setError("Password tidak cocok");
                }
                else{
                    confirmPass.setErrorEnabled(false);
                }

            }checkvalidation();
        }

    };

    private void checkvalidation(){
        if (TextUtils.isEmpty(newPass.getEditText().getText()) || TextUtils.isEmpty(confirmPass.getEditText().getText()) || newPass.getError() != null || confirmPass.getError() != null){
            btnSimpan.setEnabled(false);
        }else{
            btnSimpan.setEnabled(true);
        }
    }
}
