package com.enseval.gcmuser.OTP;

import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendOTPActivity extends AppCompatActivity {

    private TextInputLayout phoneNumber;
    private Spinner typeSendOTP;
    private Button btnSendOTP;
    private String viaOTP, temp_username;
    private String username, password, PhoneNumber;
    private int id;
    private long lastClickTime = 0;
    private boolean isValidNumber = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        phoneNumber = findViewById(R.id.nomorHandphone);
        typeSendOTP = findViewById(R.id.spinnViaOTP);
        btnSendOTP = findViewById(R.id.btnSendOTP);

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        id = getIntent().getIntExtra("id", 0);

        getNumberPhone();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.otp_via, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        typeSendOTP.setAdapter(adapter);
        typeSendOTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viaOTP = typeSendOTP.getSelectedItem().toString();
                Log.d("cekitViaOTP", viaOTP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeSendOTP.setPrompt(getResources().getString(R.string.prompt_otp_via));
            }
        });

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String valuePhone = String.valueOf(phoneNumber.getEditText().getText());
                Log.d("ido", String.valueOf(typeSendOTP.getSelectedItemId()));
                Log.d("ido", "onClick: "+PhoneNumber);
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                } else {
                    Log.d("ido", "onClick: "+typeSendOTP.getSelectedItemId());
                    check();
                    if (phoneNumber.getEditText().getText().length() >= 10 && typeSendOTP.getSelectedItemId() != 0) {
                        Log.d("ido", "onClick: "+valuePhone+" "+phoneNumber);
                        if(valuePhone.equals(PhoneNumber)) {
                            sendOTP(valuePhone, viaOTP);
                        }else{
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.attention_incorrect_number), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }

    private void getNumberPhone(){
        try {
            String getNumber = "select nama, no_hp from gcm_master_user where " +
                    "username = '"+username+"' and password = '"+ QueryEncryption.Encrypt(password) +"'";
            final Call<JsonObject> getPhoneNumber = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(getNumber)));
            getPhoneNumber.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            PhoneNumber = jsonArray.get(0).getAsJsonObject().get("no_hp").getAsString();
                            Log.d("ido", "Phone Number: "+PhoneNumber);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void check(){
        Log.d("ido", "check: "+phoneNumber.getEditText().getText().toString()+" "+typeSendOTP.getSelectedItemId());
        if(phoneNumber.getEditText().getText().toString().equals("") && typeSendOTP.getSelectedItemId()==0){
            Toast.makeText(getApplicationContext(), "Anda harus mengisi nomor HP\ndan memilih tipe pengiriman OTP", Toast.LENGTH_LONG).show();
        }else if(phoneNumber.getEditText().getText().toString().equals("") && typeSendOTP.getSelectedItemId()!=0) {
            Toast.makeText(getApplicationContext(), "Nomor handphone belum dimasukan", Toast.LENGTH_LONG).show();
        }else if(!phoneNumber.getEditText().getText().toString().equals("") && typeSendOTP.getSelectedItemId()==0){
            Toast.makeText(getApplicationContext(), "Tipe OTP belum dipilih", Toast.LENGTH_LONG).show();
        }
    }
}
