package com.enseval.gcmuser.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class UploadBuktiActivity extends AppCompatActivity {

    //dialog unggah buki komplain
    private ImageButton close;
    private ImageView gambar1, gambar2, gambar3;
    private Button btnGambar1, btnGambar2, btnGambar3, unggahBuktiKomplain;
    private TextView txtGambar1, txtGambar2, txtGambar3;
    private long lastClickTime = 0;

    private ArrayList<OrderDetail> complainList;
    private String transactionId;

    private String TAG = "ido";

    private static final int PICK_FILE = 100;
    private String tipeUpload="";
    private Uri filepath;
    private String filename = null;
    private ArrayList<String> ext = new ArrayList<>();
    private ArrayList<Uri> ImageList = new ArrayList<Uri>();
    private StorageReference storageReference;
    private String fileUrl;
    private ArrayList<String> urlFile = new ArrayList<>();

    private ArrayList<Integer> position = new ArrayList<>();

    private int posisi;

    //cek hasil request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setType("*/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"SELECT FILE"),PICK_FILE);
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_bukti);

        complainList = getIntent().getParcelableArrayListExtra("complainList");
        transactionId = getIntent().getStringExtra("transactionId");
        position = getIntent().getIntegerArrayListExtra("posisi");

        storageReference = FirebaseStorage.getInstance().getReference();

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
        unggahBuktiKomplain = findViewById(R.id.btnUnggahBuktiComplain);

        gambar1.setVisibility(GONE);
        gambar2.setVisibility(GONE);
        gambar3.setVisibility(GONE);
        btnGambar2.setVisibility(GONE);
        btnGambar3.setVisibility(GONE);
        txtGambar2.setVisibility(GONE);
        txtGambar3.setVisibility(GONE);

        if (txtGambar1.getText().toString().equals("No file choosen")){
            unggahBuktiKomplain.setEnabled(false);
        }

        unggahBuktiKomplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unggahBerkas();
            }
        });

        btnGambar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent i = new Intent();
                    i.setType("*/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    tipeUpload = "Gambar1";
                    startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        btnGambar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent i = new Intent();
                    i.setType("*/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    tipeUpload = "Gambar2";
                    startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        btnGambar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent i = new Intent();
                    i.setType("*/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    tipeUpload = "Gambar3";
                    startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
    }

    /**Method kembalian dari intent ke files (untuk ambil dokumen)*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_FILE && data != null && data.getData() != null) {
            filepath = data.getData(); //path file yang dipilih
            File file = new File(filepath.toString()); //file yang dipilih

            if (filepath.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getApplicationContext().getContentResolver().query(filepath, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (filepath.toString().startsWith("file://")) {
                filename = file.getName();
            }

            Uri imageuri = filepath;
            Log.d("ido", "onActivityResult: " + imageuri);
            ImageList.add(imageuri);

            if (tipeUpload.equals("Gambar1")) {
                txtGambar1.setText(filename); //nama file
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambar1.setImageBitmap(bitmap);
                    gambar1.setVisibility(VISIBLE);
                    if (!txtGambar1.getText().toString().equals("No file choosen")){
                        btnGambar2.setVisibility(VISIBLE);
                        txtGambar2.setVisibility(VISIBLE);
                        btnGambar1.setVisibility(GONE);
                        unggahBuktiKomplain.setEnabled(true);
                    }
                } catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
            } else if (tipeUpload.equals("Gambar2")) {
                txtGambar2.setText(filename); //nama file
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambar2.setImageBitmap(bitmap);
                    gambar2.setVisibility(VISIBLE);
                    if (!txtGambar2.getText().toString().equals("No file choosen")){
                        btnGambar3.setVisibility(VISIBLE);
                        txtGambar3.setVisibility(VISIBLE);
                        btnGambar2.setVisibility(GONE);
                    }
                } catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
            } else if (tipeUpload.equals("Gambar3")) {
                txtGambar3.setText(filename); //nama file
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambar3.setImageBitmap(bitmap);
                    gambar3.setVisibility(VISIBLE);
                    btnGambar3.setVisibility(GONE);
                } catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
            }
        }
    }

    private void unggahBerkas(){
        if (filepath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Unggah berkas");
            progressDialog.show();
            Log.d(TAG, "unggahBerkas: "+ImageList.size());
            for (int i = 0; i < ImageList.size(); i++) {
                // Defining the child of storageReference
                final StorageReference ref = storageReference.child("bukti_complain/" + Calendar.getInstance().getTimeInMillis()
                        + "-" + ImageList.get(i).getLastPathSegment() + ext.get(i));

                ref.putFile(ImageList.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                fileUrl = String.valueOf(uri);
                                urlFile.add(fileUrl);
                                Log.d(TAG, "onSuccess: " + fileUrl);
//                                ImageList.clear();
                                if (urlFile.size()==ImageList.size()){
                                    insertBuktiComplain();
                                    ImageList.clear();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UploadBuktiActivity.this, "Upload gagal", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(
                        new OnProgressListener<UploadTask.TaskSnapshot>() {

                            // Progress Listener for loading
                            // percentage on the dialog box
                            @Override
                            public void onProgress(
                                    UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploading...");
                                Log.d(TAG, "onProgress: "+progress);
                            }
                        });
            }
        }
    }

    private void insertBuktiComplain(){
        Log.d(TAG, "insertBuktiComplain: "+posisi);
        String query = "INSERT INTO gcm_listing_bukti_complain (detail_transaction_id, bukti) VALUES ";
        String loopFile = "";
        for (int i=0; i<urlFile.size(); i++){
            loopFile = loopFile + "( "+complainList.get(posisi).getId()+", '"+urlFile.get(i).toString()+"')";
            if (i < urlFile.size() - 1){
                loopFile = loopFile.concat(", ");
            }
            if (i == urlFile.size() - 1){
                loopFile = loopFile.concat(" returning id");
            }
        }
        try {
            Call<JsonObject> insertBuktiComplain = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query+loopFile)));
            insertBuktiComplain.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Intent intent = new Intent(getApplicationContext(), ComplainActivity.class);
                        intent.putParcelableArrayListExtra("complainList", complainList);
                        intent.putExtra("transactionId", transactionId);
                        intent.putIntegerArrayListExtra("posisi", position);
                        intent.putExtra("from", "upload");
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "insertBuktiComplain: "+query+loopFile);
    }
}
