package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.API;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Fragment.NewAccountBottomSheetDialog;
import com.enseval.gcmuser.Fragment.OTPBottomSheetDialog;
import com.enseval.gcmuser.Fragment.StatusBottomSheetDialogFragment;
import com.enseval.gcmuser.Fragment.TambahAlamatBottomSheetDialog;
import com.enseval.gcmuser.Model.DataAkun.ModelDataAkun;
import com.enseval.gcmuser.Model.NotifAI.ModelNotif;
import com.enseval.gcmuser.Model.User;
import com.enseval.gcmuser.OTP.OTPActivity;
import com.enseval.gcmuser.OTP.SendOTPActivity;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout, passwordLayout;
    private Button loginBtn;
    private TextView register;
    private ImageView close;
    private LoadingDialog loadingDialog;
    private long lastClickTime=0;
    String strSellerStatusKevin;
    String statusUser;
    private TextView lupaPass;
    private API mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mApi = RetrofitClient.getNotifService();

        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id  .passwordLayout);
        loginBtn = findViewById(R.id.loginBtn);
        register = findViewById(R.id.register);
        lupaPass = findViewById(R.id.lupaPass);
        close = findViewById(R.id.close);
        close.setVisibility(View.GONE);

        loadingDialog = new LoadingDialog(this);
        checkValidation(); //panggil method untuk mengecek apakah username dan password sudah diisi

        //text watcher untuk username dan password
        usernameLayout.getEditText().addTextChangedListener(mWatcher);
        passwordLayout.getEditText().addTextChangedListener(mWatcher);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //LUPA PASSWORD
        lupaPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    lupaPassword();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        //pindah ke halaman register
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        //jika login button ditekan
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    checkAccount();
                    Log.d("ido", "statusUser: "+statusUser);

                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });
    }

    //text watcher untuk username dan password
    TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            checkValidation(); //cek apakah sudah diisi
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    /**Cek apakah sudah terisi semua*/
    void checkValidation(){
        if ((TextUtils.isEmpty(usernameLayout.getEditText().getText()))
                || (TextUtils.isEmpty(passwordLayout.getEditText().getText()))){
            loginBtn.setEnabled(false);
        }
        else{
            loginBtn.setEnabled(true);
        }
    }

    /**Method pengecekan informasi login. Jika sudah login maka langsung ke main activity*/
    @Override
    protected void onStart() {
        super.onStart();
        if(SharedPrefManager.getInstance(this).isLoggedin()){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /**Method untuk request status pengguna terdaftar di seller mana aja*/
    private void statusRequestKevin(){
        if(SharedPrefManager.getInstance(getApplicationContext()).isLoggedin()){
            try {
                Call<JsonObject> statusCall = RetrofitClient
                        .getInstance()
                        .getApi()
//                        .request(new JSONRequest(QueryEncryption.Encrypt(
//                                "select string_agg(distinct cast(gcl.seller_id as varchar), ',') as seller FROM gcm_master_company gmc, gcm_company_listing gcl " +
//                                        "where gcl.seller_id = gmc.id  and gcl.buyer_id ="+ SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() +
//                                        " and gcl.status = 'A' and gmc.seller_status = 'A'")));
                        .request(new JSONRequest(QueryEncryption.Encrypt(
                                "select string_agg(distinct cast(gcl.seller_id as varchar), ',') as seller FROM gcm_master_company gmc, gcm_company_listing gcl " +
                                        "where gcl.seller_id = gmc.id and gcl.buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and gmc.seller_status = 'A'")));

                statusCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            Log.d("april", "onResponse: "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId());
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                strSellerStatusKevin = jsonArray.get(0).getAsJsonObject().get("seller").getAsString();
                                Log.d("april", strSellerStatusKevin);
                                SharedPrefManager
                                        .getInstance(LoginActivity.this)
                                        .saveActiveSeller(strSellerStatusKevin);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Call<JsonObject> statusCall = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(
                                "select string_agg(distinct cast(gcl.seller_id as varchar), ',') as seller FROM gcm_master_company gmc ,gcm_company_listing gcl where gcl.seller_id = gmc.id and gmc.seller_status = 'A'")));

                statusCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if(status.equals("success")){
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                strSellerStatusKevin = jsonArray.get(0).getAsJsonObject().get("seller").getAsString();
                                Log.d("april", strSellerStatusKevin);
                                SharedPrefManager
                                        .getInstance(LoginActivity.this)
                                        .saveActiveSeller(strSellerStatusKevin);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAccount(){
        try {
            String checkStatusAkun = "select a.id, a.nama as nama, b.id as company_id, tipe_bisnis, a.status, listing_id from gcm_master_user a " +
                    "inner join gcm_master_company b on a.company_id = b.id where b.type = 'B' and username='"+usernameLayout.getEditText().getText().toString()+"'";
            Log.d("ido", "checkAccount: "+checkStatusAkun);
            Call<JsonObject> cekStatusAkun = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(checkStatusAkun)));
            cekStatusAkun.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            statusUser = jsonArray.get(0).getAsJsonObject().get("status").getAsString();
                            if(statusUser.equals("I")){
                                CheckLogin();
                            }else if(statusUser.equals("A")){
                                login();
                            }else{
                                Toast.makeText(LoginActivity.this, "Akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }
                            Log.d("ido", "onResponse: "+statusUser);
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

    private void login(){
        try {
            loadingDialog.showDialog();
            Call<JsonObject> userLoginCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select a.id, b.id as company_id, b.type as tipe, tipe_bisnis, listing_id, password from gcm_master_user a " +
                            "inner join gcm_master_company b on a.company_id = b.id where username='"+
                            usernameLayout.getEditText().getText().toString()+"';")));

            userLoginCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            if(jsonArray.size()==1){
                                JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                                int id = jsonObject.get("id").getAsInt();
                                int company_id = jsonObject.get("company_id").getAsInt();
                                int tipe_bisnis = jsonObject.get("tipe_bisnis").getAsInt();
                                String tipe_user = jsonObject.get("tipe").getAsString();
                                String password = jsonObject.get("password").getAsString();

                                Log.d("cekit",tipe_user);

                                try {
                                    //jika username ditemukan dan password sesuai maka berhasil
                                    if(QueryEncryption.Decrypt(password).equals(passwordLayout.getEditText().getText().toString()) && tipe_user.equals("B")){
                                        User user = new User(id, company_id, tipe_bisnis);

                                        loadingDialog.hideDialog();
                                        Toast.makeText(LoginActivity.this, "Selamat datang!", Toast.LENGTH_SHORT).show();
                                        SharedPrefManager
                                                .getInstance(LoginActivity.this)
                                                .saveUser(user); //informasi login user disimpan di local
                                        statusRequestKevin();
                                        getToken(id, company_id);
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                    else{
                                        loadingDialog.hideDialog();
                                        Toast.makeText(LoginActivity.this, "Password salah", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                loadingDialog.hideDialog();
                                Toast.makeText(LoginActivity.this, "Akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if(status.equals("error")){
                            loadingDialog.hideDialog();
                            Toast.makeText(LoginActivity.this, "Akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    loadingDialog.hideDialog();
                    Toast.makeText(LoginActivity.this, "Login gagal", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CheckLogin() {
        loadingDialog.showDialog();
        try {
            String check = "SELECT a.id, password, b.type FROM gcm_master_user a inner join " +
                    "gcm_master_company b on a.company_id = b.id WHERE " +
                    "username = '"+usernameLayout.getEditText().getText().toString()+"'";
            Log.d("ido", "CheckLogin: "+check);
            Call<JsonObject> checklogin = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(check)));
            checklogin.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String password = jsonArray.get(0).getAsJsonObject().get("password").getAsString();
                            String type = jsonArray.get(0).getAsJsonObject().get("type").getAsString();
                            int id = jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
                            try {
                                if(QueryEncryption.Decrypt(password).equals(passwordLayout.getEditText().getText().toString()) && type.equals("B")){
                                        loadingDialog.hideDialog();
                                        Intent i = new Intent(getApplicationContext(), SendOTPActivity.class);
                                        i.putExtra("id", id);
                                        i.putExtra("username", usernameLayout.getEditText().getText().toString());
                                        i.putExtra("password", passwordLayout.getEditText().getText().toString());
                                        startActivity(i);
                                }else if(!QueryEncryption.Decrypt(password).equals(passwordLayout.getEditText().getText().toString()) && type.equals("B")){
                                    loadingDialog.hideDialog();
                                    Toast.makeText(getApplicationContext(), "Password yang anda masukan salah", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else {
                            loadingDialog.hideDialog();
                            Toast.makeText(getApplicationContext(), "Akun anda tidak ditemukan", Toast.LENGTH_LONG).show();
                        }
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

    private void getToken(final int id, final int company_id){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new
           OnCompleteListener<InstanceIdResult>() {
               @Override
               public void onComplete(@NonNull Task<InstanceIdResult> task) {
                   String userToken;
                   if (task.isSuccessful()){
                       userToken= task.getResult().getToken();
                       Log.e("ido", "User Token: " + userToken);
                       SharedPrefManager.getInstance(getApplicationContext()).saveToken(userToken);
                       insertToken(id, company_id, userToken);
                   }else{
                       Log.e("ido", "get user token Failed", task.getException());
                   }
               }
           });
    }

    private void insertToken(int id, int company_id, String token){
        loadingDialog.showDialog();
        String query = "insert into gcm_notification_token (user_id, company_id, token) values " +
                "("+id+", "+company_id+", '"+token+"' )";
        Log.d("ido", "insertToken: "+query);
        try {
            Call<JsonObject> callInsertToken = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callInsertToken.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        loadingDialog.hideDialog();
                    }else{

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e){

        }

    }

    private void lupaPassword(){
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.dialog_lupa_password);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final TextInputEditText email = dialog.findViewById(R.id.EMAIL);
        Button simpan = dialog.findViewById(R.id.btnKirimPassword);
        ImageButton close = dialog.findViewById(R.id.close);
//
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailterdaftar(email.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void checkEmailterdaftar(final String email){
        loadingDialog.showDialog();
        String query = "select count (email) as check_email from gcm_master_user gmu where email like '"+email+"'";
        try {
            Call<JsonObject> callCheckEmail = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCheckEmail.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int emailcheck = jsonArray.get(0).getAsJsonObject().get("check_email").getAsInt();
                            if (emailcheck==0){
                                Toast.makeText(getApplicationContext(), "Email tidak terdaftar", Toast.LENGTH_LONG).show();
                                loadingDialog.hideDialog();
                            }else{
                                Toast.makeText(getApplicationContext(), "Email sukses", Toast.LENGTH_LONG).show();
                                getDataAkun(email);
                            }
                        }else {
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

    private void getDataAkun(final String email){
        String query = "select username, password from gcm_master_user gmu where email = '"+email+"'";
        try {
            Call<JsonObject> callGetDataAkun = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetDataAkun.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            String username = jsonArray.get(0).getAsJsonObject().get("username").getAsString();
                            String password = jsonArray.get(0).getAsJsonObject().get("password").getAsString();
                            sendDataAkun(email, password, username);
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

    private void sendDataAkun(String email, String password, String username){
        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("password", password);
            requestBody.put("email_receiver", email);
            requestBody.put("username", username);

            Log.d("ido", "sendDataAkun: "+String.valueOf(requestBody));

            Call<ModelDataAkun> call = mApi.sendDataAkun(requestBody);
            call.enqueue(new Callback<ModelDataAkun>() {
                @Override
                public void onResponse(Call<ModelDataAkun> call, Response<ModelDataAkun> response) {
                    if (response.isSuccessful()){
                        loadingDialog.hideDialog();
                        Toast.makeText(getApplicationContext(), "Sukses mengirim data akun", Toast.LENGTH_LONG).show();
                    }else{
                        Log.d("ido", "gagal kirim data akun");
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<ModelDataAkun> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "send Email sukses", Toast.LENGTH_LONG).show();
                    loadingDialog.hideDialog();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
