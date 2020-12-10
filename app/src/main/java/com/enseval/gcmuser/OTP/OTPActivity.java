package com.enseval.gcmuser.OTP;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.API;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.LoginActivity;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.Activity.ResetPasswordActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.User;
import com.enseval.gcmuser.Model.otp.ModelMessageID;
import com.enseval.gcmuser.Model.otp.ModelOTP;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.enseval.gcmuser.Utilities.Helper;
import com.enseval.gcmuser.Utilities.OnBackPressedListener;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;
    private EditText code1, code2, code3, code4, code5, code6;
    private final StringBuilder sb=new StringBuilder();
    private Button btnVerify;
    private String phoneNumber, via, username, email, strSellerStatusKevin;
    private int id;
    private TextView descOTP, textEror, kirimulang;
    private CountDownTimer countDownTimer = null;
    private Boolean isRunningTimer = true;
    public int counter;
    public String kode;
    private String tipe = "akun";
    private API mAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_layout);

        loadingDialog = new LoadingDialog(this);

        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        code5 = findViewById(R.id.code5);
        code6 = findViewById(R.id.code6);
        btnVerify = findViewById(R.id.btnVerif);
        textEror = findViewById(R.id.textError);
        descOTP = findViewById(R.id.descOTP);
        kirimulang = findViewById(R.id.kirimulang);

        phoneNumber = getIntent().getStringExtra("phonenumber");
        via = getIntent().getStringExtra("via");
        username = getIntent().getStringExtra("username");
        id = getIntent().getIntExtra("id", 0);
        email = getIntent().getStringExtra("email");

        code1.addTextChangedListener(textWatcher);
        code2.addTextChangedListener(textWatcher);
        code3.addTextChangedListener(textWatcher);
        code4.addTextChangedListener(textWatcher);
        code5.addTextChangedListener(textWatcher);
        code6.addTextChangedListener(textWatcher);
        btnVerify.setEnabled(false);

        kode = String.valueOf(Helper.sendGenerateOTP(6));
        mAPI = RetrofitClient.getAPIService();

        descOTP.setText("Masukkan kode verifikasi yang dikirimkan ke nomor : "+phoneNumber);

        insertKode(kode);

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

    private void insertKode(String kode){
        Call<JsonObject> insertKode = RetrofitClient
                .getInstanceGLOB()
                .getApi()
                .reqsenddataakun(new JSONRequest(email, kode, username, tipe));
        insertKode.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    String status_insert = response.body().getAsJsonObject().get("status_insert").getAsString();
                    Log.d("ido", "status insert: "+status_insert);
                    if (status_insert.equals("success")){
                        sendOTP();
                    }else{
                        Toast.makeText(getApplicationContext(), "Gagal insert kode", Toast.LENGTH_LONG).show();
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

    private void sendOTP(){
        loadingDialog.showDialog();
        String formatMessage = String.valueOf(getResources().getString(R.string.otp_format_1)+" "+phoneNumber+ ". " +getResources().getString(R.string.otp_format_2)+": "+kode+" "+getResources().getString(R.string.otp_format_3));
        String otpType = via;
        String userID = RetrofitClient.BASE_USER_ID;
        String valueKey = RetrofitClient.BASE_KEY_ID;

        Log.d("ido", "sendOTP: "+formatMessage);
//        countDownTimer = null;
//        kirimUlangKode(30000, true, false);
//        loadingDialog.hideDialog();
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("otptype", otpType);
        requestBody.put("nohp", phoneNumber);
        requestBody.put("message", formatMessage);
        requestBody.put("userid", userID);
        requestBody.put("key", valueKey);

        Log.d("ido",String.valueOf(requestBody));
        Call<ModelOTP> call = mAPI.sendOTPValue(requestBody);
        call.enqueue(new Callback<ModelOTP>() {
            @Override
            public void onResponse(Call<ModelOTP> call, Response<ModelOTP> response) {
                if (response.isSuccessful()){
                    loadingDialog.hideDialog();
                    countDownTimer = null;
                    kirimUlangKode(30000, true, false);
                }else{
                    loadingDialog.hideDialog();
                    Log.d("ido", "gagal send message: ");
                }
            }

            @Override
            public void onFailure(Call<ModelOTP> call, Throwable t) {
                loadingDialog.hideDialog();
                Toast.makeText(OTPActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
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
                            kode = String.valueOf(Helper.sendGenerateOTP(6));
                            insertKode(kode);
//                            sendOTP();
                        }
                    });
                }
            }.start();
        }
    }

    private void checkKode(){
        Log.d("ido", "checkKode: "+kode);
        String kodegabung = code1.getText().toString()
                +code2.getText().toString()
                +code3.getText().toString()
                +code4.getText().toString()
                +code5.getText().toString()
                +code6.getText().toString();
        if (kodegabung.equals(kode)){
            checkExpiredCode(email, kode);
        }else{
            textEror.setText("Kode yang anda masukan tidak sesuai");
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
                            makeVerified();
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

    private void makeVerified() {
        loadingDialog.showDialog();
        //String userID = String.valueOf(SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId());
        String verified= "UPDATE gcm_master_user set no_hp_verif = true, update_by = "+id+", update_date = now() where username ='"+username+"';" ;
        try {
            Call<JsonObject> verifyPhone = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(verified)));
            verifyPhone.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        makeActivate();
                    }else {
                        loadingDialog.hideDialog();
                        Toast.makeText(OTPActivity.this, "response is UNSUCCESSFUL", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                }
            });
        }catch (Exception e){
            loadingDialog.hideDialog();
            e.printStackTrace();
        }
    }

    private void makeActivate() {
        try {
            String q= "update gcm_master_user set status = 'A' where username = '" + username +"';";
            Call<JsonObject> activasion = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(q)));
            activasion.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Toast.makeText(OTPActivity.this, "Your Account has been ACTIVATED", Toast.LENGTH_SHORT).show();
                    login();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                }
            });

        }catch (Exception e){
            loadingDialog.hideDialog();
        };
    }

    private void login(){
        try {
            String getData = "select a.id, b.id as company_id, b.type as tipe, tipe_bisnis, listing_id, password from gcm_master_user a " +
                    "inner join gcm_master_company b on a.company_id = b.id where username='"+username+"';";
            Call<JsonObject> userLoginCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(getData)));
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
                                try {
                                    User user = new User(id, company_id, tipe_bisnis);
                                    Toast.makeText(OTPActivity.this, "Selamat datang!", Toast.LENGTH_SHORT).show();
                                    SharedPrefManager
                                            .getInstance(OTPActivity.this)
                                            .saveUser(user); //informasi login user disimpan di local
                                    statusRequestKevin();
                                    getToken(id, company_id);
                                    Intent intent = new Intent(OTPActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                loadingDialog.hideDialog();
                                Toast.makeText(OTPActivity.this, "Akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if(status.equals("error")){
                            loadingDialog.hideDialog();
                            Toast.makeText(OTPActivity.this, "Akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("", "onFailure: "+t.getMessage());
                    loadingDialog.hideDialog();
                    Toast.makeText(OTPActivity.this, "Login gagal", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
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
                                        .getInstance(OTPActivity.this)
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
                                        .getInstance(OTPActivity.this)
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
                       loadingDialog.hideDialog();
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
                        loadingDialog.hideDialog();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                }
            });
        }catch (Exception e){
            loadingDialog.hideDialog();
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        /*super.onBackPressed();*/
        boolean isCanShowAlertDialog = false;
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            //TODO: Perform your logic to pass back press here
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof OnBackPressedListener) {
                    isCanShowAlertDialog = true;
                    ((OnBackPressedListener) fragment).onBackPressed();
                }
            }
        }

        if (!isCanShowAlertDialog) {
            showExitDialogConfirmation();
        }
    }

    void showExitDialogConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.attention_leave_verfiy_otp))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        Button positive = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setTextColor(Color.BLACK);
        Button negative = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
        negative.setTextColor(Color.BLACK);
    }

}
