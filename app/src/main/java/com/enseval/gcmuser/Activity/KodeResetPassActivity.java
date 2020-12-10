package com.enseval.gcmuser.Activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.Utilities.Helper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KodeResetPassActivity extends AppCompatActivity {

    private EditText code1, code2, code3, code4, code5, code6;
    private final StringBuilder sb=new StringBuilder();
    private Button btnVerify;
    private String emailKiriman, usernameKiriman;
    private TextView descEmail, textEror, kirimulang;
    private CountDownTimer countDownTimer = null;
    private Boolean isRunningTimer = true;
    public int counter;
    public String kode;
    private String tipe = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kode_reset_pass);

        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        code5 = findViewById(R.id.code5);
        code6 = findViewById(R.id.code6);
        btnVerify = findViewById(R.id.btnVerif);
        textEror = findViewById(R.id.textError);
        descEmail = findViewById(R.id.descEmail);
        kirimulang = findViewById(R.id.kirimulang);

        emailKiriman = getIntent().getStringExtra("email");
        usernameKiriman = getIntent().getStringExtra("username");
        descEmail.setText("Masukkan kode verifikasi yang dikirimkan ke email : "+emailKiriman);

        code1.addTextChangedListener(textWatcher);
        code2.addTextChangedListener(textWatcher);
        code3.addTextChangedListener(textWatcher);
        code4.addTextChangedListener(textWatcher);
        code5.addTextChangedListener(textWatcher);
        code6.addTextChangedListener(textWatcher);
        btnVerify.setEnabled(false);

        sendKode();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkKode();
            }
        });

    }

    TextWatcher textWatcher = new TextWatcher() {
        int count = 0;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if(sb.length()==1)
            {
                sb.deleteCharAt(0);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(code1.length()==1) {
                code1.clearFocus();
                code2.requestFocus();
                code2.setCursorVisible(true);
                count++;
            }

            if(code2.length()==1) {
                code2.clearFocus();
                code3.requestFocus();
                code3.setCursorVisible(true);
                count++;
            }

            if(code3.length()==1) {
                code3.clearFocus();
                code4.requestFocus();
                code4.setCursorVisible(true);
                count++;
            }

            if(code4.length()==1) {
                code4.clearFocus();
                code5.requestFocus();
                code5.setCursorVisible(true);
                count++;
            }

            if(code5.length()==1) {
                code5.clearFocus();
                code6.requestFocus();
                code6.setCursorVisible(true);
                count++;
            }

            if (code1.length()==1 &&
                    code2.length()==1 &&
                    code3.length()==1 &&
                    code4.length()==1 &&
                    code5.length()==1 &&
                    code6.length()==1){
                Log.d("ido", "onTextChanged: masuk kesini");
                btnVerify.setEnabled(true);
                checkKode();
            }else{
                textEror.setVisibility(View.GONE);
                btnVerify.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            code6.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL){
                        code6.clearFocus();
                        code5.requestFocus();
                        code5.setCursorVisible(true);
                    }
                    return false;
                }
            });
            code5.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL){
                        code5.clearFocus();
                        code4.requestFocus();
                        code4.setCursorVisible(true);
                    }
                    return false;
                }
            });
            code4.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL){
                        code4.clearFocus();
                        code3.requestFocus();
                        code3.setCursorVisible(true);
                    }
                    return false;
                }
            });
            code3.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL){
                        code3.clearFocus();
                        code2.requestFocus();
                        code2.setCursorVisible(true);
                    }
                    return false;
                }
            });
            code2.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL){
                        code2.clearFocus();
                        code1.requestFocus();
                        code1.setCursorVisible(true);
                    }
                    return false;
                }
            });

        }
    };

    private void checkKode(){
        Log.d("ido", "checkKode: "+kode);
        String kodegabung = code1.getText().toString()
                +code2.getText().toString()
                +code3.getText().toString()
                +code4.getText().toString()
                +code5.getText().toString()
                +code6.getText().toString();
        if (kodegabung.equals(kode)){
            checkExpiredCode(emailKiriman, kode);
        }else{
            textEror.setVisibility(View.VISIBLE);
        }
    }

    private void checkExpiredCode(final String email, String kode){
        Call<JsonObject> callpass = RetrofitClient
                .getInstanceGLOB()
                .getApi()
                .reqpassavailable(new JSONRequest(email, kode, tipe));
        callpass.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if (status.equals("success")){
                        JsonArray jsonArray = response.body().getAsJsonObject().get("values").getAsJsonArray();
                        String status_kode = jsonArray.get(0).getAsJsonObject().get("status_kode").getAsString();
                        if (status_kode.equals("available")) {
                            Intent i = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                            i.putExtra("email", email);
                            startActivity(i);
                            finish();
                        }else{
                            textEror.setText("Kode yang anda masukan sudah tidak berlaku");
                            textEror.setVisibility(View.VISIBLE);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Status pengecekan kode gagal", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Pengecekan kode gagal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Koneksi Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendKode(){
        final String generateKode = String.valueOf(Helper.sendGenerateForgetPass(6));

        Call<JsonObject> callSendDataAkun = RetrofitClient
                .getInstanceGLOB()
                .getApi()
                .reqsenddataakun(new JSONRequest(emailKiriman, generateKode, usernameKiriman, tipe));
        callSendDataAkun.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    String status_insert = response.body().getAsJsonObject().get("status_insert").getAsString();
                    String status_kirim = response.body().getAsJsonObject().get("status_kirim").getAsString();
                    if (status_insert.equals("success") && status_kirim.equals("success")){
                        countDownTimer = null;
                        kirimUlangKode(30000, true, false);
                        kode = generateKode;
                    }else{
                        Toast.makeText(getApplicationContext(), "Gagal mengirim email", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "not successfull", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Koneksi GAGAL", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void kirimUlangKode(long value, Boolean running, Boolean stop){
        if (countDownTimer == null && isRunningTimer == running && isRunningTimer != stop){
            Log.d("ido", "kirimUlangKode: masuk kesini");
            countDownTimer = new CountDownTimer(value, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    kirimulang.setText("Kirim ulang kode verifikasi dalam "+seconds+" detik");
                    kirimulang.setTextColor(getResources().getColor(R.color.textColor));
                    counter--;
                }

                @Override
                public void onFinish() {
                    kirimulang.setText("Kirim ulang kode verifikasi");
                    kirimulang.setTextColor(getResources().getColor(R.color.colorPrimary));
                    kirimulang.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendKode();
                        }
                    });
                }
            }.start();
        }
    }
}
