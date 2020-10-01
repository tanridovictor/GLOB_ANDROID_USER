package com.enseval.gcmuser.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.OTP.OTPActivity;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPBottomSheetDialog extends BottomSheetDialog {
    private Spinner spinTypeOTP;
    private TextInputLayout inputNomor;
    private Button sendOtp;
    private String viaOTP, temp_username;
    private long lastClickTime = 0;
    private boolean isValidNumber = true;
    private LoadingDialog loadingDialog;
    private Dialog dialog;

    public OTPBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_bottom_sheet_dialog);
        inputNomor = findViewById(R.id.nomorHandphone);
        spinTypeOTP = findViewById(R.id.spinnViaOTP);
        sendOtp = findViewById(R.id.btnSendOTP);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.otp_via, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinTypeOTP.setAdapter(adapter);
        spinTypeOTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viaOTP = spinTypeOTP.getSelectedItem().toString();
                Log.d("cekitViaOTP", viaOTP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinTypeOTP.setPrompt(getContext().getResources().getString(R.string.prompt_otp_via));
            }
        });

        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.konfirmasi_bottom_sheet_dialog);
                TextView text = bottomSheetDialog.findViewById(R.id.tvMessage);
                Button negative = bottomSheetDialog.findViewById(R.id.negative);
                Button positive = bottomSheetDialog.findViewById(R.id.positive);

                text.setText(getContext().getResources().getString(R.string.otp_msg_number) + "  " + inputNomor.getEditText().getText() + " ? ");
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String valuePhone = String.valueOf(inputNomor.getEditText().getText());
                        Log.d("cekit", String.valueOf(spinTypeOTP.getSelectedItemId()));
                        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                            return;
                        } else {
                            //validPhoneNumber(inputNomor.getEditText().getText().toString(), temp_username);
                            if (inputNomor.getEditText().getText().length() >= 10 && spinTypeOTP.getSelectedItemId() != 0) {
                                if (isValidNumber==true) {
                                    sendOTP(valuePhone, viaOTP);
                                } else {
                                    dialog = new BottomSheetDialog(getContext());
                                    dialog.setContentView(R.layout.dialog_global_attention);

                                    TextView message = dialog.findViewById(R.id.tvDetail);
                                    Button action = dialog.findViewById(R.id.actionBtn);
                                    message.setText(getContext().getResources().getString(R.string.attention_incorrect_number));
                                    action.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                }
                            }
                        }
                    }
                });

                bottomSheetDialog.show();
            }
        });

    }

    private void sendOTP(String valuePhone, String valueVia) {
        Intent intent = new Intent(getContext(), OTPActivity.class);
        intent.putExtra("phonenumber", valuePhone);
        intent.putExtra("via", valueVia);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(intent);
    }
}
