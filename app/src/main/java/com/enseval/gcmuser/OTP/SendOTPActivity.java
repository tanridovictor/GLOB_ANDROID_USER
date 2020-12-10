package com.enseval.gcmuser.OTP;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.LoginActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.Utilities.OnBackPressedListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendOTPActivity extends AppCompatActivity {

    private long lastClickTime = 0;
    private TextView deskripsiNomorTelp;
    private CardView sms, whatsapp;
    private int id;
    private String username, password, PhoneNumber, statusKirim, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        id = getIntent().getIntExtra("id", 0);
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        email = getIntent().getStringExtra("email");

        deskripsiNomorTelp = findViewById(R.id.txtDeskripsi);
        sms = findViewById(R.id.cvSMS);
        whatsapp = findViewById(R.id.cvWA);

        getNumberPhone();

        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusKirim = "SMS";
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    sendOTP(PhoneNumber, statusKirim);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusKirim = "WA";
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    sendOTP(PhoneNumber, statusKirim);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
    }

    private void getNumberPhone(){
        try {
            String query = "select nama, no_hp from gcm_master_user where " +
                    "username = '"+username+"' and password = '"+ QueryEncryption.Encrypt(password) +"'";
            final Call<JsonObject> getPhoneNumber = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            getPhoneNumber.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            PhoneNumber = jsonArray.get(0).getAsJsonObject().get("no_hp").getAsString();
                            Log.d("ido", "Phone Number: "+PhoneNumber);
                            deskripsiNomorTelp.setText("Kode verifikasi akan dikirimkan ke nomor\n"+PhoneNumber+" melalui : ");
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

    private void sendOTP(String valuePhone, String valueVia) {
        Intent intent = new Intent(getApplicationContext(), OTPActivity.class);
        intent.putExtra("phonenumber", valuePhone);
        intent.putExtra("via", valueVia);
        intent.putExtra("username", username);
        intent.putExtra("id", id);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
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
