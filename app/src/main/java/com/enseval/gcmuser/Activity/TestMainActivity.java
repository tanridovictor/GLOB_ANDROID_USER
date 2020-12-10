package com.enseval.gcmuser.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.enseval.gcmuser.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class TestMainActivity extends AppCompatActivity {

    //dialog unggah buki komplain
    private ImageButton close;
    private ImageView gambar1, gambar2, gambar3;
    private Button btnGambar1, btnGambar2, btnGambar3, unggahBuktiKomplain;
    private TextView txtGambar1, txtGambar2, txtGambar3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);

        close = findViewById(R.id.btnClose);
        gambar1 = findViewById(R.id.gambar1);
        gambar2 = findViewById(R.id.gambar2);
        gambar3 = findViewById(R.id.gambar3);
        btnGambar1 = findViewById(R.id.btnGambar1);
        btnGambar2 = findViewById(R.id.btnGambar2);
        btnGambar3 = findViewById(R.id.btnGambar3);
        txtGambar1 = findViewById(R.id.txtPathGambar1);
        txtGambar2 = findViewById(R.id.txtPathGambar2);
        txtGambar3 = findViewById(R.id.txtPathGambar3);

        unggahBuktiKomplain = findViewById(R.id.btnUploadBukti);

        unggahBuktiKomplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ComplainActivity.class);
                startActivity(intent);
            }
        });


    }

}
