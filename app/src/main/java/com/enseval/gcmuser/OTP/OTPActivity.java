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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.API;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.LoginActivity;
import com.enseval.gcmuser.Activity.MainActivity;
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
    private TextView title,timer;
    private Pinview inputOTP;
    private Button btnSubmit, btnResend;
    private String nilaiOTP = "";
    private String username;
    private int id;
    private Boolean isValidate = false;
    public int counter;
    private String no_handphone , otpVia;
    private API mAPI;
    RetrofitClient client;
    public static OTPActivity ma;
    String strSellerStatusKevin;

    private Boolean isVerified = false  ;

    private String messageID = "";
    private TextView txtStatusDelivered, txtDateExpired, txtTimeExpired;
    private Boolean isRunningTimer = true;
    private CountDownTimer countDownTimer = null;

    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_layout);

        Intent intent = getIntent();
        no_handphone = intent.getStringExtra("phonenumber");
        otpVia       = intent.getStringExtra("via");
        id = intent.getIntExtra("id", 0);

        mAPI = client.getAPIService();
        ma = this;

        initialTools();
        randomOTP();
//        countdown();
        makeExpired(60000 , true,false);

        username = getIntent().getStringExtra("username");
    }

    public void makeExpired(long value, Boolean makeRunning, Boolean makeStopped) {
        if (countDownTimer == null && isRunningTimer == makeRunning && isRunningTimer != makeStopped ){
             countDownTimer = new CountDownTimer(value, 1000) {
                public void onTick(long millisUntilFinished) {
                    Calendar now = Calendar.getInstance();
                    Calendar timeout = Calendar.getInstance();
                    long currentDateTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentDateTime);
                    DateFormat df = new SimpleDateFormat("dd - MM - yyyy");
                    String tanggalExpired = String.valueOf(df.format(currentDate));
                    timeout.setTimeInMillis(now.getTimeInMillis() + millisUntilFinished);
                    Date expireDate = new Date(now.getTimeInMillis() + millisUntilFinished);
                    String jamExpired = expireDate.getHours() + " : " + expireDate.getMinutes();
                    txtDateExpired.setText(tanggalExpired);
                    txtTimeExpired.setText(jamExpired);

                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                    timer.setText(String.valueOf(minutes + " minutes and  " + seconds + " " + getString(R.string.otp_timecountdown_remaining)));
                    counter--;
                    btnSubmit.setText(getResources().getString(R.string.otp_submit));
                    timer.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                public void onFinish() {
                    nilaiOTP = "";
                    isRunningTimer = false;
                    timer.setText(R.string.otp_timesup);
                    timer.setTextColor(getResources().getColor(R.color.color_warning));
                    btnSubmit.setEnabled(false);
                    btnResend.setEnabled(true);
                    title.setText(getResources().getString(R.string.otp_title_resend));
                    Toast.makeText(OTPActivity.this, getResources().getString(R.string.attention_expired_otp), Toast.LENGTH_LONG).show();
                }
            }.start();
        }if (countDownTimer == null && isRunningTimer == makeRunning && isRunningTimer == makeStopped){
            countDownTimer = new CountDownTimer(value, 1000) {
                public void onTick(long millisUntilFinished) {
                    Calendar now = Calendar.getInstance();
                    Calendar timeout = Calendar.getInstance();
                    long currentDateTime = System.currentTimeMillis();
                    Date currentDate = new Date(currentDateTime);
                    DateFormat df = new SimpleDateFormat("dd - MM - yyyy");
                    String tanggalExpired = String.valueOf(df.format(currentDate));
                    timeout.setTimeInMillis(now.getTimeInMillis() + millisUntilFinished);
                    Date expireDate = new Date(now.getTimeInMillis() + millisUntilFinished);
                    String jamExpired = expireDate.getHours() + " : " + expireDate.getMinutes();
                    txtDateExpired.setText(tanggalExpired);
                    txtTimeExpired.setText(jamExpired);

                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                    timer.setText(String.valueOf(minutes + " minutes and  " + seconds + " " + getString(R.string.otp_timecountdown_remaining)));
                    counter--;
                    btnSubmit.setText(getResources().getString(R.string.otp_submit));
                    timer.setTextColor(getResources().getColor(R.color.colorPrimary));
                }

                public void onFinish() {
                    nilaiOTP = "";
                    isRunningTimer = false;
                    timer.setText(R.string.otp_timesup);
                    timer.setTextColor(getResources().getColor(R.color.color_warning));
                    btnSubmit.setEnabled(false);
                    btnResend.setEnabled(true);
                    title.setText(getResources().getString(R.string.otp_title_resend));
                    Toast.makeText(OTPActivity.this, getResources().getString(R.string.attention_expired_otp), Toast.LENGTH_LONG).show();
                }
            }.start();
        }

    }

    private void initialTools() {
        inputOTP  = findViewById(R.id.pinview);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnResend = findViewById(R.id.btnResend);
        title     = findViewById(R.id.textTitle);
        timer     = findViewById(R.id.textTimer);

        txtStatusDelivered = findViewById(R.id.txtStatusDelivered);
        txtDateExpired     = findViewById(R.id.txtDateExpired);
        txtTimeExpired     = findViewById(R.id.txtTimeExpired);

        loadingDialog = new LoadingDialog(this);

        btnResend.setEnabled(false);

        btnSubmit.setEnabled(isValidate);

        inputOTP.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                btnResend.setEnabled(false);
                if (inputOTP.getValue() != null){
                    checkLengthOTP();
                }else {
                    btnSubmit.setEnabled(false);
                }
            }

            private void checkLengthOTP() {
                int lengthOTP = inputOTP.getPinLength();
                if (lengthOTP == 6 ){
                    btnSubmit.setEnabled(true);
                    btnResend.setEnabled(false);
                }else {
                    Toast.makeText(OTPActivity.this, "Kode OTP ânda kurang", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
                btnSubmit.setEnabled(false);
                inputOTP.clearValue();
                //makeActivate();
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                randomOTP();
                if (countDownTimer != null) makeExpired(60000 , false, true);
                if (countDownTimer == null) makeExpired(60000 , true, true);
                makeExpired(60000,false, true);
                btnResend.setEnabled(false);
                if (inputOTP != null) inputOTP.clearValue();
            }
        });

    }

    private void makeActivate() {
        loadingDialog.showDialog();
        try {
            String q= "update gcm_master_user set status = 'A' where username = '" + username +"';";
            Call<JsonObject> activasion = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(q)));
            activasion.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    loadingDialog.hideDialog();
                    Toast.makeText(OTPActivity.this, "Your Account has been ACTIVATED", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    t.printStackTrace();
                    Log.wtf("",t);
                }
            });

        }catch (Exception e){
            loadingDialog.hideDialog();
            e.printStackTrace();
            Log.wtf("",e);
        };
    }

    private void randomOTP() {
        nilaiOTP = String.valueOf(Helper.sendGenerateOTP(6));
        sendOTPUser(no_handphone,nilaiOTP);
    }

    private void sendOTPUser(String no_handphone, String nilaiOTP) {
        loadingDialog.showDialog();
        String formatMessage = String.valueOf(getResources().getString(R.string.otp_format_1)+" "+no_handphone+ ". " +getResources().getString(R.string.otp_format_2)+": "+nilaiOTP+" "+getResources().getString(R.string.otp_format_3));
        String otpType = otpVia;
        String userID = RetrofitClient.BASE_USER_ID;
        String valueKey = RetrofitClient.BASE_KEY_ID;

        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("otptype", otpType);
            requestBody.put("nohp", no_handphone);
            requestBody.put("message", formatMessage);
            requestBody.put("userid", userID);
            requestBody.put("key", valueKey);

            Log.d("cekit",String.valueOf(requestBody));
            Call<ModelOTP> call = mAPI.sendOTPValue(requestBody);
            call.enqueue(new Callback<ModelOTP>() {
                @Override
                public void onResponse(Call<ModelOTP> call, Response<ModelOTP> response) {
                    try {
                        loadingDialog.hideDialog();
                        JSONObject object=new JSONObject(new Gson().toJson(response.body()));
                        String valueMessageID = object.getString("messageID");
                        Toast.makeText(OTPActivity.this, "messageID === " + messageID, Toast.LENGTH_SHORT).show();
                        getStatusDelivered(valueMessageID);
                    } catch (JSONException e) {
                        loadingDialog.hideDialog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ModelOTP> call, Throwable t) {
                    loadingDialog.hideDialog();
                    Toast.makeText(OTPActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            loadingDialog.hideDialog();
            e.printStackTrace();
            Toast.makeText(this, "onException", Toast.LENGTH_SHORT).show();
        }
    }

    private void getStatusDelivered(String value) {
        loadingDialog.showDialog();
        String messageID    = value;
        String userID       = RetrofitClient.BASE_USER_ID;
        String key          = RetrofitClient.BASE_KEY_ID;
        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("messageID", messageID);
            requestBody.put("userid", userID);
            requestBody.put("key", key);

            Log.d("cekit",String.valueOf(requestBody));
            Call<ModelMessageID> call = mAPI.getMessageInformation(requestBody);
            call.enqueue(new Callback<ModelMessageID>() {
                @Override
                public void onResponse(Call<ModelMessageID> call, Response<ModelMessageID> response) {
                    try {
                        loadingDialog.hideDialog();
                        JSONObject object=new JSONObject(new Gson().toJson(response.body()));
                        String status = object.getString("message");
                        txtStatusDelivered.setText(status);
                        Log.d("liatstatus", status);

                    } catch (JSONException e) {
                        loadingDialog.hideDialog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ModelMessageID> call, Throwable t) {
                    loadingDialog.hideDialog();
                    Toast.makeText(OTPActivity.this, "onFailure getMessageStatusDelivered   ==== "+ t, Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            loadingDialog.hideDialog();
            Toast.makeText(ma, "onerror Catch nilai error value ====" + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void countdown() {
        new CountDownTimer(60000,1000){
            public void onTick(long millisUntilFinished){
                int seconds = (int) (millisUntilFinished / 1000) % 60 ;
                int minutes = (int) ((millisUntilFinished / (1000*60)) % 60);
                int hours   = (int) ((millisUntilFinished / (1000*60*60)) % 24);
                timer.setText(String.valueOf(minutes+" minutes and  "+seconds+" "+getString(R.string.otp_timecountdown_remaining)));
                counter--;
                btnSubmit.setText(getResources().getString(R.string.otp_submit));
                timer.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            public  void onFinish(){
                timer.setText(R.string.otp_timesup);
                timer.setTextColor(getResources().getColor(R.color.color_warning));
                btnSubmit.setEnabled(false);
                btnResend.setEnabled(true);
                title.setText(getResources().getString(R.string.otp_title_resend));
            }
        }.start();
    }

    private void validate() {
        if (inputOTP.getValue().equalsIgnoreCase(nilaiOTP)){
            isValidate = true;
            btnResend.setEnabled(true);
            Toast.makeText(this, getResources().getString(R.string.otp_match), Toast.LENGTH_SHORT).show();
            makeVerified();
            makeActivate();//untuk mengaktifkan status
            login();
        }else {
            //Toast.makeText(this, getResources().getString(R.string.otp_unmatch) + " Lah kok bisa unmatched,   " +inputOTP.getValue() + " itu nilai dari inputan dan nilai random otp nya  " + nilaiOTP, Toast.LENGTH_LONG).show();
            Toast.makeText(this, getResources().getString(R.string.otp_unmatch), Toast.LENGTH_SHORT).show();
            btnResend.setEnabled(true);
            title.setText(getResources().getString(R.string.otp_unmatch));
        }

    }

    private void makeVerified() {
        Log.d("ido", "makeVerified: "+no_handphone);
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
                        loadingDialog.hideDialog();
                        new FancyAlertDialog.Builder(OTPActivity.this)
                                .setTitle(getResources().getString(R.string.attention_label))
                                .isCancellable(false)
                                .setMessage(getResources().getString(R.string.on_sucess_verified_phone))
                                .setIcon(R.drawable.verified_user, Icon.Visible)
                                .setBackgroundColor(getResources().getColor(R.color.white))
                                .setAnimation(Animation.POP)
                                .setPositiveBtnText("Ok")
                                .setNegativeBtnBackground(getResources().getColor(R.color.white))
                                .setNegativeBtnText("")
                                .OnPositiveClicked(new FancyAlertDialogListener() {
                                    @Override
                                    public void OnClick() {
//                                        Toast.makeText(OTPActivity.this, "Lalala", Toast.LENGTH_SHORT).show();
                                    }
                                }).build();
                    }else {
                        Toast.makeText(OTPActivity.this, "response is UNSUCCESSFUL", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    new FancyAlertDialog.Builder(OTPActivity.this)
                            .setTitle(getResources().getString(R.string.attention_label))
                            .isCancellable(false)
                            .setMessage(getResources().getString(R.string.on_failure))
                            .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                            .setAnimation(Animation.POP)
                            .setPositiveBtnText("Ok")
                            .OnPositiveClicked(new FancyAlertDialogListener() {
                                @Override
                                public void OnClick() {
                                    Toast.makeText(getApplicationContext(), "Okay", Toast.LENGTH_SHORT).show();

                                }
                            }).build();
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

    private void login(){
        try {
            String getData = "select a.id, b.id as company_id, b.type as tipe, tipe_bisnis, listing_id, password from gcm_master_user a " +
                    "inner join gcm_master_company b on a.company_id = b.id where username='"+username+"';";
            loadingDialog.showDialog();
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
                                    loadingDialog.hideDialog();
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
}
