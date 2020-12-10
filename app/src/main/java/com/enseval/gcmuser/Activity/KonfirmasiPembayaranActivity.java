package com.enseval.gcmuser.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Model.Rekening;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KonfirmasiPembayaranActivity extends AppCompatActivity {

    private ImageButton btnKembali;
    private TextInputEditText idTransaksi, txtNamaPemilikRek;
    private Spinner spinBankTujuan;
    private CardView cardTglPembayaran;
    private TextView txtTglPembayaran, txtFilePath;
    private Button btnKonfirmasi;
    private int idRekening, days, months, years;
    // views for button
    private Button btnPilihFile;

    // view for image view
    private ImageView imgBuktiTransfer;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    private long lastClickTime=0;

    private String filename = null;
    private String fileUrl = "", extension;

    private ArrayList<Rekening> listRekening;
    private ArrayList<String> listRekeningContain;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(
                                        intent,
                                        "Select Image from here..."),
                                PICK_IMAGE_REQUEST);
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi_pembayaran);

        idTransaksi = findViewById(R.id.idTransaksi);
        txtNamaPemilikRek = findViewById(R.id.namaPemilikrekening);
        spinBankTujuan = findViewById(R.id.spinBankTujuan);
        cardTglPembayaran = findViewById(R.id.tglBayar);
        txtTglPembayaran = findViewById(R.id.txtTglBayar);
        btnPilihFile = findViewById(R.id.btnUploadBuktiBayar);
        imgBuktiTransfer = findViewById(R.id.gambarBuktiUpload);
        txtFilePath = findViewById(R.id.txtFilePath);
        btnKonfirmasi = findViewById(R.id.btnKonfirmasi);
        btnKembali = findViewById(R.id.btnBack);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        idTransaksi.setText(getIntent().getStringExtra("idTransaksi"));

        imgBuktiTransfer.setVisibility(View.GONE);
        txtFilePath.setVisibility(View.GONE);

        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent i = new Intent(getApplicationContext(), DetailOrderActivity.class);
                    i.putExtra("status", "menunggu");
                    i.putExtra("transactionId", getIntent().getStringExtra("idTransaksi"));
                    i.putExtra("total", getIntent().getLongExtra("total",0));
                    i.putExtra("ongkir", getIntent().getDoubleExtra("ongkir",0));
                    i.putExtra("ppn", getIntent().getFloatExtra("ppn",0));
                    startActivity(i);
                    finish();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        listRekening = new ArrayList<>();
        listRekeningContain = new ArrayList<>();

        listRekeningContain.add("---Pilih Bank Tujuan---");

        getRekening();

        final ArrayAdapter<String> rekeningAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, listRekeningContain);
        spinBankTujuan.setAdapter(rekeningAdapter);
        rekeningAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        btnPilihFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    if(!doesUserHavePermission()){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }
                    else{
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(
                                        intent,
                                        "Select Image from here..."),
                                PICK_IMAGE_REQUEST);
                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        btnKonfirmasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkData();
            }
        });

        spinBankTujuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (Rekening rek : listRekening){
                    if ((rek.getNama()+" | "+rek.getNo_rekening()+" - "+rek.getPemilik_rekening()).equals(spinBankTujuan.getSelectedItem().toString())){
                        idRekening = rek.getId();
                        Log.d("ido", "onItemSelected: "+idRekening);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            final Calendar calendar = Calendar.getInstance();
            final int Day = calendar.get(Calendar.DAY_OF_MONTH);
            final int Month = calendar.get(Calendar.MONTH);
            final int Year = calendar.get(Calendar.YEAR);

            cardTglPembayaran.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DatePickerDialog dpd = new DatePickerDialog(KonfirmasiPembayaranActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            txtTglPembayaran.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                            days = dayOfMonth;
                            months = (month+1);
                            years = year;
                        }
                    }, Year, Month, Day);
                    DatePicker datePicker = dpd.getDatePicker();
                    datePicker.setFirstDayOfWeek(android.icu.util.Calendar.MONDAY);
                    dpd.getDatePicker().setMinDate(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 20) - (1000 * 60 * 60 * 24 * 10));
                    dpd.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 20) + (1000 * 60 * 60 * 24 * 10));
                    dpd.show();
                }
            });
        }
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        txtFilePath.setVisibility(View.VISIBLE);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            File file = new File(filePath.toString()); //file yang dipilih

            if (filePath.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getApplicationContext().getContentResolver().query(filePath, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (filePath.toString().startsWith("file://")) {
                filename = file.getName();
            }

            txtFilePath.setText(filename); //nama file
            extension = filename.substring(filename.lastIndexOf(".")); //ekstension file

            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imgBuktiTransfer.setImageBitmap(bitmap);
                imgBuktiTransfer.setVisibility(View.VISIBLE);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    /**Check permission*/
    private boolean doesUserHavePermission()
    {
        int result = checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // UploadImage method
    private void uploadFile()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            final StorageReference ref = storageReference.child("bukti_bayar/"+Calendar.getInstance().getTimeInMillis()+"-buktibayar"+extension);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Image uploaded successfully
                            // Dismiss dialog
                            progressDialog.dismiss();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    fileUrl = String.valueOf(uri);
                                    saveDataPayment();
                                    Toast.makeText(KonfirmasiPembayaranActivity.this, "Upload sukses", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(KonfirmasiPembayaranActivity.this, "Upload gagal", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                }
                            });
        }
    }

    private void getRekening(){
        String query = "SELECT a.id, a.no_rekening, a.pemilik_rekening, b.nama " +
                "FROM gcm_listing_bank a " +
                "left join gcm_master_bank b on a.id_bank = b.id " +
                "WHERE a.status = 'A' and b.status = 'A' and a.company_id = (select distinct company_id from gcm_transaction_detail a inner join gcm_list_barang b on a.barang_id=b.id inner join gcm_master_company f on b.company_id = f.id where a.transaction_id= '"+idTransaksi.getText().toString()+"') " +
                "ORDER BY b.nama";
        try {
            Log.d("ido", "getRekening: "+QueryEncryption.Encrypt(query));
            Log.d("ido", "getRekening: "+query);
            Call<JsonObject> getRekening = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            getRekening.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for (int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                int id = jsonObject.get("id").getAsInt();
                                String noRek = jsonObject.get("no_rekening").getAsString();
                                String namaPemilikRek = jsonObject.get("pemilik_rekening").getAsString();
                                String namaRek = jsonObject.get("nama").getAsString();

                                Rekening rekening = new Rekening(id, noRek, namaPemilikRek, namaRek);
                                listRekening.add(rekening);
                                listRekeningContain.add(rekening.getNama()+" | "+rekening.getNo_rekening()+" - "+rekening.getPemilik_rekening());
                            }
                            Log.d("ido", "sukses get rekening");
                        }else{
                            Log.d("ido", "gagal get rekening");
                        }
                    }else{
                        Log.d("ido", "gagal rekening");
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

    private void saveDataPayment(){
        String query;
        query = "update gcm_master_transaction set bukti_bayar = '" + fileUrl + "', tanggal_bayar = '" + years + "-" + months + "-" + days + "', id_list_bank = " + idRekening + ", " +
                "pemilik_rekening = '" + txtNamaPemilikRek.getText().toString() + "', update_date = now(), update_by = " + SharedPrefManager.getInstance(getApplicationContext()).getUser().getUserId() + " where id_transaction = '" + idTransaksi.getText().toString() + "'";
        Log.d("ido", "saveDataPayment: "+query);
        try{
            Call<JsonObject> saveDataPayment = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            saveDataPayment.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
//                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                        i.putExtra("fragment", "orderFragment");
//                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);

                        Intent i = new Intent(getApplicationContext(), DetailOrderActivity.class);
                        i.putExtra("status", "menunggu");
                        i.putExtra("transactionId", getIntent().getStringExtra("idTransaksi"));
                        i.putExtra("total", getIntent().getLongExtra("total",0));
                        i.putExtra("ongkir", getIntent().getDoubleExtra("ongkir",0));
                        i.putExtra("ppn", getIntent().getFloatExtra("ppn",0));
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Gagal menyimpan data pembayaran", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Gagal menyimpan data pembayaran", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkData(){
        if (txtNamaPemilikRek.getText().toString().equals("") ||  spinBankTujuan.getSelectedItemId()==0|| txtTglPembayaran.getText().toString().equals("dd-mm-yyyy")){
//            Toast.makeText(getApplicationContext(), "Data masih ada yang kososng", Toast.LENGTH_LONG).show();
            dialogNotif("Kolom data masih ada yang kosong, anda harus isi semua data terlebih dahulu");
        }else{
            if (txtFilePath.getText().toString().equals("")){
//                saveDataPayment();
//                Toast.makeText(getApplicationContext(), "Bukti pembayaran belum anda lampirkan, anda WAJIB melakukan upload bukti pembayaran", Toast.LENGTH_LONG).show();
                dialogNotif("Bukti pembayaran belum anda lampirkan, anda WAJIB melakukan upload bukti pembayaran");
            }else{
                uploadFile();
            }
        }
    }

    private void dialogNotif(String text){
        final Dialog dialog = new Dialog(KonfirmasiPembayaranActivity.this);
        dialog.setContentView(R.layout.dialog_handle);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView image = dialog.findViewById(R.id.iconImage);
        TextView btnBatal = dialog.findViewById(R.id.btnBatal);
        Button btnSetuju = dialog.findViewById(R.id.btnYa);
        TextView title = dialog.findViewById(R.id.judul);
        TextView description = dialog.findViewById(R.id.isi);
        dialog.setCancelable(false);
        btnBatal.setVisibility(View.GONE);
        image.setVisibility(View.GONE);
        btnSetuju.setText("Mengerti");

        title.setText("Konfirmasi Pembayaran");
        description.setText(text);

        //jika setuju lanjut ke request
        btnSetuju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
