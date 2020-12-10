package com.enseval.gcmuser.Activity;

import android.Manifest;
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
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.SellerListAdapter;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.Kecamatan;
import com.enseval.gcmuser.Model.Kelurahan;
import com.enseval.gcmuser.Model.Kota;
import com.enseval.gcmuser.Model.Provinsi;
import com.enseval.gcmuser.SharedPrefManager;
import com.enseval.gcmuser.Utilities.Regex;
import com.enseval.gcmuser.Model.TipeBisnis;
import com.enseval.gcmuser.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RegisterActivity extends AppCompatActivity {

    private Spinner tipeRegistrasiSpinner, tipeBisnisSpinner, provinsiSpinner, kotaSpinner, kecamatanSpinner, kelurahanSpinner;
    private ImageButton backBtn;
    private Button lanjutBtn, btnUpload, registerBtn;
    private TextView backBtn2, login, tvFilename, berkas;
    private TextInputLayout namaPerusahaan, noNPWP, noSIUP, alamat, kelurahan,
            kecamatan, kodepos, emailPerusahaan, notelp, namaPengguna,
            noktp, emailPengguna, nohp, username, password, konfirmasiPassword,
            npwpExpiredDate , siupExpiredDate;
    private String TAG = "ido";
    private static final int PICK_FILE = 100;
    private ArrayList<TipeBisnis> tipeBisnisArrayList;
    private ArrayList<Provinsi> provinsiArrayList;
    private ArrayList<Kota> kotaArrayList;
    private ArrayList<Kecamatan> kecamatanArrayList;
    private ArrayList<Kelurahan> kelurahanArrayList;
    private ArrayList<String> tipeBisnisContain;
    private ArrayList<String> provinsiContain;
    private ArrayList<String> kotaContain;
    private ArrayList<String> kecamatanContain;
    private ArrayList<String> kelurahanContain;
    private static int idBisnis;
    private static String idProvinsi, idKota, idKecamatan, idKelurahan;//ido
    private static int companyId, userId;
    private StorageReference storageReference;
    private Uri filepath;
    private String fileUrl, extension;
    private String filename = null;
    private ImageView close;
    private LoadingDialog loadingDialog;
    private ConstraintLayout content, failed;
    private Button refresh;
    private long lastClickTime=0;
    private RecyclerView rvSellerList;
    private Button pilihSeller;
    private SellerListAdapter sellerListAdapter;
    private static ArrayList<Company> sellerList;
    private BottomSheetDialog dialog;
    private static ArrayList<Company> sellerListTemp;
    private static ArrayList<Company> listSeller;
    private String valueTipeRegister = "";
    private String value= "0";
    private String value_notes_blacklist = "";

    private TextView titleStatusListNull;
    private Boolean isChoosen = false;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private SimpleDateFormat dateFormatter;

    private DatePickerDialog picker;
    private Button btnPlus;
    private EditText nambahinDate;
    private Spinner nambahinjenis;
    private LinearLayout dinamicLayout;


    //DIALOG UNGGAH BERKAS
    private ImageButton btnClose;
    private RadioGroup rbList;
    private ImageView gambarNPWP, gambarSIUP, gambarGXP, gambarPBF, gambarKTP;
    private Button btnSelectNPWP, btnSelectSIUP, btnSelectGXP, btnSelectPBF, btnSelectKTP, btnUnggagBerkas;
    private TextView txtPathNPWP, txtPathSIUP, txtPathGXP, txtPathPBF, txtPathKTP;
    private TextView KTP, NPWP, SIUP, GXP, PBF;
    private String tipeUpload;
    private ArrayList<Uri> ImageList = new ArrayList<Uri>();
    private ArrayList<String> tipeUploadDoc = new ArrayList<>();
    private ArrayList<String> ext = new ArrayList<>();
    private ArrayList<String> urlFile = new ArrayList<>();
    private TextView txtSudahUpload;
    private String statusRb;

    //cek hasil request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
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
        setContentView(R.layout.activity_register);

        content = findViewById(R.id.content);
        failed = findViewById(R.id.failed);
        content.setVisibility(VISIBLE);
        failed.setVisibility(View.INVISIBLE);

        refresh = findViewById(R.id.refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        //INISIALISASI FIREBASE STORAGE
        storageReference = FirebaseStorage.getInstance().getReference();

        //BUAT ARRAY LIST UNTUK MENAMPUNG RESPONSE
        tipeBisnisArrayList = new ArrayList<>();
        provinsiArrayList = new ArrayList<>();
        kotaArrayList = new ArrayList<>();
        kecamatanArrayList = new ArrayList<>();
        kelurahanArrayList = new ArrayList<>();

        loadingDialog = new LoadingDialog(this);

        //BUAT ARRAY LIST UNTUK DITAMPILKAN DI SPINNER
        tipeBisnisContain = new ArrayList<>();
        provinsiContain = new ArrayList<>();
        kotaContain = new ArrayList<>();
        kecamatanContain = new ArrayList<>();
        kelurahanContain = new ArrayList<>();

        //REQUEST TIPE BISNIS
        tipeBisnisContain.add( "----Pilih Tipe Bisnis----");
        provinsiContain.add( "----Pilih Provinsi----");
        kotaContain.add( "----Pilih Kota----");
        kecamatanContain.add( "----Pilih Kecamatan----");
        kelurahanContain.add( "----Pilih Kelurahan----");

        loadingDialog.showDialog();
        try {
            requestTipeBisnis();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getProvinsi();
//        nambahinDate    = findViewById(R.id.nambahinDate);
//        dinamicLayout   = findViewById(R.id.nambahin);
//        btnPlus         = findViewById(R.id.btnPlus);
//        nambahinjenis   = findViewById(R.id.nambahinjenis);

        //BUAT ADAPTER UNTUK SPINNER
        final ArrayAdapter<String> tipeBisnisAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, tipeBisnisContain);
        final ArrayAdapter<String> provinsiAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, provinsiContain);
        final ArrayAdapter<String> kotaAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, kotaContain);
        final ArrayAdapter<String> kecamatanAdapter = new ArrayAdapter<String>(this,//ido
                R.layout.spinner_item, kecamatanContain);//ido
        final ArrayAdapter<String> kelurahanAdapter = new ArrayAdapter<String>(this,//ido
                R.layout.spinner_item, kelurahanContain);//ido


        //INISIALISASI ID XML
        backBtn = findViewById(R.id.backBtn);
        backBtn2 = findViewById(R.id.backBtn2);
        login = findViewById(R.id.login);
        lanjutBtn = findViewById(R.id.lanjutBtn);
        namaPerusahaan = findViewById(R.id.namaPerusahaan);
        noNPWP = findViewById(R.id.npwpLayout);
        noSIUP = findViewById(R.id.siupLayout);
        alamat = findViewById(R.id.alamatLayout);
        kodepos = findViewById(R.id.kodeposLayout);
        emailPerusahaan = findViewById(R.id.emailPerusahaanLayout);
        notelp = findViewById(R.id.notelpPerusahaanLayout);
        namaPengguna = findViewById(R.id.namaPenggunaLayout);
        noktp = findViewById(R.id.noktpLayout);
        emailPengguna = findViewById(R.id.emailPenggunaLayout);
        nohp = findViewById(R.id.nohpLayout);
        username = findViewById(R.id.usernameLayout);
        password = findViewById(R.id.passwordLayout);
        konfirmasiPassword = findViewById(R.id.passwordLayout2);
        berkas = findViewById(R.id.berkas);
        btnUpload = findViewById(R.id.btnUpload);
        tvFilename = findViewById(R.id.tvFilename);
        tipeRegistrasiSpinner = findViewById(R.id.tipeRegistrasi);
        tipeBisnisSpinner = findViewById(R.id.tipeBisnis);
        tipeBisnisAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        tipeBisnisSpinner.setAdapter(tipeBisnisAdapter);
        provinsiSpinner = findViewById(R.id.provinsiSpinner);
        provinsiAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        provinsiSpinner.setAdapter(provinsiAdapter);
        kotaSpinner = findViewById(R.id.kotaSpinner);
        kotaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kecamatanSpinner = findViewById(R.id.kecamatanSpinner);
        kecamatanAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kelurahanSpinner = findViewById(R.id.kelurahanSpinner);
        kelurahanAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        close = findViewById(R.id.close);
        txtSudahUpload = findViewById(R.id.txtSudahUpload);
        txtSudahUpload.setVisibility(GONE);
        tvFilename.setVisibility(GONE);

        checkValidation();

        //HANDLE PERUBAHAN ISI FORM
        namaPerusahaan.getEditText().addTextChangedListener(mWatcher);
        noNPWP.getEditText().addTextChangedListener(mWatcher);
        noSIUP.getEditText().addTextChangedListener(mWatcher);
        alamat.getEditText().addTextChangedListener(mWatcher);
        kodepos.getEditText().addTextChangedListener(mWatcher);
        emailPerusahaan.getEditText().addTextChangedListener(mWatcher);
        notelp.getEditText().addTextChangedListener(mWatcher);
        namaPengguna.getEditText().addTextChangedListener(mWatcher);
        noktp.getEditText().addTextChangedListener(mWatcher);
        emailPengguna.getEditText().addTextChangedListener(mWatcher);
        nohp.getEditText().addTextChangedListener(mWatcher);
        username.getEditText().addTextChangedListener(mWatcher);
        password.getEditText().addTextChangedListener(mWatcher);
        konfirmasiPassword.getEditText().addTextChangedListener(mWatcher);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.tipe_register, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        tipeRegistrasiSpinner.setAdapter(adapter);
        tipeRegistrasiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (tipeRegistrasiSpinner.getSelectedItemId() == 1) {
                    valueTipeRegister = "B";
                }else if (tipeRegistrasiSpinner.getSelectedItemId() == 2) {
                    valueTipeRegister = "S";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                tipeRegistrasiSpinner.setPrompt(getResources().getString(R.string.prompt_type_regster));
                checkValidation();
            }
        });

        //HANDLE PERUBAHAN PILIHAN SPINNER TIPE BISNIS
        tipeBisnisSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(tipeBisnisArrayList.size()>0){
                    for(TipeBisnis tb : tipeBisnisArrayList){
                        if(tb.getName().equals(tipeBisnisSpinner.getSelectedItem().toString())){
                            idBisnis =  tb.getId();
                            Log.d(TAG, "onItemSelected: "+idBisnis);

                        }
                    }
                }

                if(tipeBisnisSpinner.getSelectedItemPosition()==0){
                    berkas.setText("(Harap pilih tipe bisnis dahulu)");
                    btnUpload.setEnabled(false);
                }
                else if(idBisnis==1){
                    berkas.setVisibility(View.INVISIBLE);
                    berkas.setText("(NPWP, TDP, SIUP, SPPKP, Izin Farmasi)");
                    btnUpload.setEnabled(true);
                }
                else if(idBisnis>1){
                    berkas.setVisibility(View.INVISIBLE);
                    berkas.setText("(NPWP, TDP, SIUP, SPPKP)");
                    btnUpload.setEnabled(true);
                }
                checkValidation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkValidation();
            }
        });

        //HANDLE PERUBAHAN PROVINSI
        provinsiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kotaContain.clear();
                kotaContain.add("----Pilih Kota----");
                for (Provinsi prov : provinsiArrayList) {
                    if (prov.getProvinceName().equals(provinsiSpinner.getSelectedItem().toString())) {
                        idProvinsi = prov.getProvinceId();
                        Log.d(TAG, "onItemSelected: " + idProvinsi);
                    }
                }
                if (idProvinsi != "") {
                    getKota(idProvinsi);
                    kotaSpinner.setAdapter(kotaAdapter);
                    checkValidation();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkValidation();
            }
        });

        //HANDLE PERUBAHAN KOTA
        kotaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kecamatanContain.clear();
                kecamatanContain.add("----Pilih Kecamatan----");
                for (Kota kota : kotaArrayList) {
                    if (kota.getNamaKota().equals(kotaSpinner.getSelectedItem().toString())) {
                        idKota = kota.getIdKota();
                        Log.d("", "onItemSelected: " + idKota);
                    }
                }
                if (idKota != "") {
                    getKecamatan(idKota);
                    kecamatanSpinner.setAdapter(kecamatanAdapter);
                    checkValidation();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkValidation();
            }
        });

        //HANDLE PERUBAHAN KECAMATAN
        kecamatanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                kelurahanContain.clear();
                kelurahanContain.add("----Pilih Kelurahan----");
                for (Kecamatan kec : kecamatanArrayList) {
                    if (kec.getNamaKecamatan().equals(kecamatanSpinner.getSelectedItem().toString())) {
                        idKecamatan = kec.getIdKecamatan();
                        Log.d("", "onItemSelected: " + idKecamatan);
                    }
                }
                if (idKecamatan != "") {
                    getKelurahan(idKecamatan);
                    kelurahanSpinner.setAdapter(kelurahanAdapter);
                    checkValidation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkValidation();
            }
        });

        //HANDLE PERUBAHAN KELURAHAN
        kelurahanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for(Kelurahan k : kelurahanArrayList){
                    if(k.getNamaKelurahan().equals(kelurahanSpinner.getSelectedItem().toString())){
                        idKelurahan = k.getCityIdidKelurahan();
                        Log.d("", "onItemSelected: "+idKelurahan);
                    }
                }
                checkValidation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkValidation();
            }
        });

        //HANDLE BUTTON
        backBtn.setOnClickListener(loginClick);
        backBtn2.setOnClickListener(loginClick);
        login.setOnClickListener(loginClick);

        //jika ditekan lanjut maka akan masuk ke request seller
        lanjutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String flag = "";
                if (tipeRegistrasiSpinner.getSelectedItemId() == 1) {
                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                        return;
                    }
                    else {
                        flag = "buyer";
                        checkUniqData(flag);
//                        sellerRequest();
                        //registerBuyer();
                    }
                    lastClickTime=SystemClock.elapsedRealtime();
                }else if (tipeRegistrasiSpinner.getSelectedItemId() == 2){
                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                        return;
                    }
                    else {
                        try {
                            flag = "seller";
                            //checkUsername();
                            checkUniqData(flag);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    lastClickTime=SystemClock.elapsedRealtime();
                }else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.error_register_type), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {

                    final Dialog dialog = new Dialog(RegisterActivity.this);
                    dialog.setContentView(R.layout.dialog_unggah_berkas_register);
                    Window window = dialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    btnClose = dialog.findViewById(R.id.btnClose);
                    rbList = dialog.findViewById(R.id.rbList);
                    gambarNPWP = dialog.findViewById(R.id.gambarNPWP);
                    gambarSIUP = dialog.findViewById(R.id.gambarSIUP);
                    gambarGXP = dialog.findViewById(R.id.gambarGXP);
                    gambarPBF = dialog.findViewById(R.id.gambarPBF);
                    gambarKTP = dialog.findViewById(R.id.gambarKTP);
                    btnSelectNPWP = dialog.findViewById(R.id.btnSelectNPWP);
                    btnSelectSIUP = dialog.findViewById(R.id.btnSelectSIUP);
                    btnSelectGXP = dialog.findViewById(R.id.btnSelectGXP);
                    btnSelectPBF = dialog.findViewById(R.id.btnSelectPBF);
                    btnSelectKTP = dialog.findViewById(R.id.btnSelectKTP);
                    txtPathNPWP = dialog.findViewById(R.id.txtPathNPWP);
                    txtPathSIUP = dialog.findViewById(R.id.txtPathSIUP);
                    txtPathGXP = dialog.findViewById(R.id.txtPathGXP);
                    txtPathPBF = dialog.findViewById(R.id.txtPathPBF);
                    txtPathKTP = dialog.findViewById(R.id.txtPathKTP);
                    btnUnggagBerkas = dialog.findViewById(R.id.btnUnggahBerkas);
                    KTP = dialog.findViewById(R.id.KTP);
                    NPWP = dialog.findViewById(R.id.NPWP);
                    SIUP = dialog.findViewById(R.id.SIUP);
                    GXP = dialog.findViewById(R.id.GXP);
                    PBF = dialog.findViewById(R.id.PBF);

                    dialog.setCancelable(false);

                    if (idBisnis == 1){
                        KTP.setVisibility(GONE);
                        statusRb = "perusahaan";
                        btnSelectKTP.setVisibility(GONE);
                        txtPathKTP.setVisibility(GONE);
                        gambarNPWP.setVisibility(GONE);
                        gambarSIUP.setVisibility(GONE);
                        gambarGXP.setVisibility(GONE);
                        gambarPBF.setVisibility(GONE);
                        gambarKTP.setVisibility(GONE);
                        SIUP.setVisibility(VISIBLE);
                        btnSelectSIUP.setVisibility(VISIBLE);
                        txtPathSIUP.setVisibility(VISIBLE);
                        GXP.setVisibility(VISIBLE);
                        btnSelectGXP.setVisibility(VISIBLE);
                        txtPathGXP.setVisibility(VISIBLE);
                        PBF.setVisibility(VISIBLE);
                        btnSelectPBF.setVisibility(VISIBLE);
                        txtPathPBF.setVisibility(VISIBLE);
                    }else{
                        statusRb = "perusahaan";
                        KTP.setVisibility(GONE);
                        btnSelectKTP.setVisibility(GONE);
                        txtPathKTP.setVisibility(GONE);
                        gambarNPWP.setVisibility(GONE);
                        gambarSIUP.setVisibility(GONE);
                        gambarGXP.setVisibility(GONE);
                        gambarPBF.setVisibility(GONE);
                        gambarKTP.setVisibility(GONE);
                        GXP.setVisibility(GONE);
                        btnSelectGXP.setVisibility(GONE);
                        txtPathGXP.setVisibility(GONE);
                        PBF.setVisibility(GONE);
                        btnSelectPBF.setVisibility(GONE);
                        txtPathPBF.setVisibility(GONE);
                    }

                    final RadioButton rbPerusahaan = dialog.findViewById(R.id.rbPerusahaan);
                    rbPerusahaan.setChecked(true);
                    ImageList.clear();
                    tipeUploadDoc.clear();

                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    rbList.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            switch (checkedId){
                                case R.id.rbPerusahaan :
                                    tipeUploadDoc.clear();
                                    if (idBisnis == 1){
                                        statusRb = "perusahaan";
                                        KTP.setVisibility(GONE);
                                        btnSelectKTP.setVisibility(GONE);
                                        txtPathKTP.setVisibility(GONE);
                                        gambarNPWP.setVisibility(GONE);
                                        gambarSIUP.setVisibility(GONE);
                                        gambarGXP.setVisibility(GONE);
                                        gambarPBF.setVisibility(GONE);
                                        gambarKTP.setVisibility(GONE);
                                        SIUP.setVisibility(VISIBLE);
                                        btnSelectSIUP.setVisibility(VISIBLE);
                                        txtPathSIUP.setVisibility(VISIBLE);
                                        GXP.setVisibility(VISIBLE);
                                        btnSelectGXP.setVisibility(VISIBLE);
                                        txtPathGXP.setVisibility(VISIBLE);
                                        PBF.setVisibility(VISIBLE);
                                        btnSelectPBF.setVisibility(VISIBLE);
                                        txtPathPBF.setVisibility(VISIBLE);
                                    }else{
                                        statusRb = "perusahaan";
                                        KTP.setVisibility(GONE);
                                        btnSelectKTP.setVisibility(GONE);
                                        txtPathKTP.setVisibility(GONE);
                                        gambarNPWP.setVisibility(GONE);
                                        gambarSIUP.setVisibility(GONE);
                                        gambarGXP.setVisibility(GONE);
                                        gambarPBF.setVisibility(GONE);
                                        gambarKTP.setVisibility(GONE);
                                        GXP.setVisibility(GONE);
                                        btnSelectGXP.setVisibility(GONE);
                                        txtPathGXP.setVisibility(GONE);
                                        PBF.setVisibility(GONE);
                                        btnSelectPBF.setVisibility(GONE);
                                        txtPathPBF.setVisibility(GONE);
                                        SIUP.setVisibility(VISIBLE);
                                        btnSelectSIUP.setVisibility(VISIBLE);
                                        txtPathSIUP.setVisibility(VISIBLE);
                                    }
                                    break;

                                case R.id.rbPerorangan :
                                    statusRb = "perorangan";
                                    tipeUploadDoc.clear();
                                    ImageList.clear();
                                    gambarNPWP.setVisibility(GONE);
                                    gambarSIUP.setVisibility(GONE);
                                    gambarGXP.setVisibility(GONE);
                                    gambarPBF.setVisibility(GONE);
                                    gambarKTP.setVisibility(GONE);
                                    SIUP.setVisibility(GONE);
                                    btnSelectSIUP.setVisibility(GONE);
                                    txtPathSIUP.setVisibility(GONE);
                                    GXP.setVisibility(GONE);
                                    btnSelectGXP.setVisibility(GONE);
                                    txtPathGXP.setVisibility(GONE);
                                    PBF.setVisibility(GONE);
                                    btnSelectPBF.setVisibility(GONE);
                                    txtPathPBF.setVisibility(GONE);
                                    KTP.setVisibility(VISIBLE);
                                    btnSelectKTP.setVisibility(VISIBLE);
                                    txtPathKTP.setVisibility(VISIBLE);
                                    break;
                            }
                        }
                    });

                    btnSelectNPWP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                Intent i = new Intent();
                                i.setType("*/*");
                                i.setAction(Intent.ACTION_GET_CONTENT);
                                tipeUpload = "NPWP";
                                startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                            }
                            lastClickTime=SystemClock.elapsedRealtime();
                        }
                    });

                    btnSelectKTP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                Intent i = new Intent();
                                i.setType("*/*");
                                i.setAction(Intent.ACTION_GET_CONTENT);
                                tipeUpload = "KTP";
                                startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                            }
                            lastClickTime=SystemClock.elapsedRealtime();
                        }
                    });

                    btnSelectSIUP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                Intent i = new Intent();
                                i.setType("*/*");
                                i.setAction(Intent.ACTION_GET_CONTENT);
                                tipeUpload = "SIUP";
                                startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                            }
                            lastClickTime=SystemClock.elapsedRealtime();
                        }
                    });

                    btnSelectGXP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                Intent i = new Intent();
                                i.setType("*/*");
                                i.setAction(Intent.ACTION_GET_CONTENT);
                                tipeUpload = "GXP";
                                startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                            }
                            lastClickTime=SystemClock.elapsedRealtime();
                        }
                    });

                    btnSelectPBF.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
                                Intent i = new Intent();
                                i.setType("*/*");
                                i.setAction(Intent.ACTION_GET_CONTENT);
                                tipeUpload = "PBF";
                                startActivityForResult(Intent.createChooser(i,"SELECT FILE"),PICK_FILE);
                            }
                            lastClickTime=SystemClock.elapsedRealtime();
                        }
                    });

                    btnUnggagBerkas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                                return;
                            }
                            else {
//                                if (txtPathNPWP.getText().toString().equals("No file choosen")||txtPathSIUP.getText().toString().equals("No file choosen")||
//                                        txtPathGXP.getText().toString().equals("No file choosen")){
//                                    Toast.makeText(getApplicationContext(), "Berkas belum lengkap", Toast.LENGTH_LONG).show();
//                                }else{
//                                    Log.d(TAG, "masuk kesini: "+txtPathNPWP.getText().toString());
//                                    unggahBerkas();
//                                    dialog.dismiss();
//                                }
                                checkUnggahBerkas(dialog);
                            }
                            lastClickTime=SystemClock.elapsedRealtime();
                        }
                    });

                    dialog.show();

//                    if(!doesUserHavePermission()){
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//                        }
//                    }
//                    else{
//                        Intent intent = new Intent();
//                        intent.setType("*/*");
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                        startActivityForResult(Intent.createChooser(intent,"SELECT FILE"),PICK_FILE);
//                    }
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                return;
            }
            else {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            lastClickTime=SystemClock.elapsedRealtime();
        }
    };

    //text watcher untuk handle input yang dimasukkan
    TextWatcher mWatcher = new TextWatcher() {
        int len=0;
        final android.os.Handler handler = new android.os.Handler();
        Runnable runnable;
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            //nama perusahaan maks 70 karakter
            if(namaPerusahaan.getEditText().getText().hashCode()==s.hashCode()){
                if(namaPerusahaan.getEditText().getText().length()>70){
                    namaPerusahaan.setErrorEnabled(true);
                }
                else{
                    namaPerusahaan.setErrorEnabled(false);
                }
            }

            //npwp ada pengaturan format
            if(noNPWP.getEditText().getText().hashCode()==s.hashCode()){
                String str = noNPWP.getEditText().getText().toString();
                if(str.length()<=20){
                    if((str.length()==2 && len <str.length()) || (str.length()==6 && len <str.length() || (str.length()==10 && len <str.length()) || (str.length()==16 && len <str.length()))){
                        //checking length  for backspace.
                        noNPWP.getEditText().append(".");
                    }
                    if(str.length()==12 && len <str.length()){
                        noNPWP.getEditText().append("-");
                    }
                }
                else if(str.length()>20){
                    noNPWP.getEditText().setText(noNPWP.getEditText().getText().toString().substring(0,20));
                    //noNPWP.getEditText().clear();
                    noSIUP.requestFocus();
                }
            }

            //error jika password dan konfirmasinya tidak cocok
            if(konfirmasiPassword.getEditText().getText().hashCode()==s.hashCode()){
                if(!konfirmasiPassword.getEditText().getText().toString().equals(password.getEditText().getText().toString())){
                    lanjutBtn.setEnabled(false);
                    konfirmasiPassword.setErrorEnabled(true);
                    konfirmasiPassword.setError("Password tidak cocok");
                }
                else{
                    konfirmasiPassword.setErrorEnabled(false);
                }
            }
            handler.removeCallbacks(runnable);
            checkValidation();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
            if(noNPWP.getEditText().getText().hashCode()==s.hashCode()){
                String str = noNPWP.getEditText().getText().toString();
                len = str.length();
            }
        }

        @Override
        public void afterTextChanged(final Editable s) {
            runnable = new Runnable() {

                @Override
                public void run() {
                    //nomor ktp maks 16 karakter
                    if(noktp.getEditText().getText().hashCode()==s.hashCode()){
                        if(noktp.getEditText().getText().length()!=16){
                            noktp.setErrorEnabled(true);
                            noktp.setError("Tidak valid");
                        }
                        else {
                            noktp.setErrorEnabled(false);
                        }
                    }

                    //kodepos maks 5 karakter
                    if(kodepos.getEditText().getText().hashCode()==s.hashCode()){
                        if(kodepos.getEditText().getText().length()!=5){
                            kodepos.setErrorEnabled(true);
                            kodepos.setError("Tidak valid");
                        }
                        else{
                            kodepos.setErrorEnabled(false);
                        }
                    }

                    //email harus sesuai regex
                    if(emailPerusahaan.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mEmailPerusahaan = Regex.emailPattern.matcher(emailPerusahaan.getEditText().getText());
                        if (!mEmailPerusahaan.find()){
                            lanjutBtn.setEnabled(false);
                            emailPerusahaan.setErrorEnabled(true);
                            emailPerusahaan.setError("Harap masukkan email yang valid");
                        }
                        else{
                            emailPerusahaan.setErrorEnabled(false);
                        }
                    }

                    //no telp maks 9 karakter
                    if(notelp.getEditText().getText().hashCode()==s.hashCode()){
                        if(notelp.getEditText().getText().length()<9){
                            notelp.setErrorEnabled(true);
                            notelp.setError("Harap sertakan kode area");
                        }
                        else {
                            notelp.setErrorEnabled(false);
                        }
                    }

                    //email harus sesuai regex
                    if(emailPengguna.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mEmailPemilik = Regex.emailPattern.matcher(emailPengguna.getEditText().getText());
                        if (!mEmailPemilik.find()){
                            lanjutBtn.setEnabled(false);
                            emailPengguna.setErrorEnabled(true);
                            emailPengguna.setError("Harap masukkan email yang valid");
                        }
                        else{
                            emailPengguna.setErrorEnabled(false);
                        }
                    }

                    //no hp maks 10 karakter
                    if(nohp.getEditText().getText().hashCode()==s.hashCode()){
                        if(nohp.getEditText().getText().length()<10){
                            nohp.setErrorEnabled(true);
                            nohp.setError("Tidak valid");
                        }
                        else {
                            nohp.setErrorEnabled(false);
                        }
                    }

                    //username harus sesuai regex
                    if(username.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mUsername = Regex.usernamePattern.matcher(username.getEditText().getText());
                        if (!mUsername.find()){
                            lanjutBtn.setEnabled(false);
                            username.setErrorEnabled(true);
                            username.setError("Minimal 8 karakter terdiri dari huruf besar, kecil, angka atau spesial karakter _ - .");
                        }
                        else{
                            username.setErrorEnabled(false);
                        }
                    }

                    //password harus sesuai ketentuan
                    if(password.getEditText().getText().hashCode()==s.hashCode()){
                        Matcher mPassword = Regex.passwordPattern.matcher(password.getEditText().getText());
                        if (!mPassword.find()){
                            lanjutBtn.setEnabled(false);
                            password.setErrorEnabled(true);
                            password.setError("Password minimal 8 karakter dan harus terdiri dari huruf besar, kecil, dan angka");
                        }
                        else{
                            password.setErrorEnabled(false);
                        }

                        if(!konfirmasiPassword.getEditText().getText().toString().equals(password.getEditText().getText().toString())){
                            lanjutBtn.setEnabled(false);
                            konfirmasiPassword.setErrorEnabled(true);
                            konfirmasiPassword.setError("Password tidak cocok");
                        }
                        else{
                            konfirmasiPassword.setErrorEnabled(false);
                        }
                    }
                checkValidation();
                }
            };
            handler.postDelayed(runnable, 700);
            checkValidation(); //cek apakah sudah terisi semua
        }
    };

    /**Method untuk cek apakah semua sudah terisi*/
    void checkValidation(){
        if ((TextUtils.isEmpty(namaPerusahaan.getEditText().getText()))
                || (TextUtils.isEmpty(noNPWP.getEditText().getText()))
                || (TextUtils.isEmpty(noSIUP.getEditText().getText()))
                || (TextUtils.isEmpty(alamat.getEditText().getText()))
//                || (TextUtils.isEmpty(kelurahan.getEditText().getText()))
//                || (TextUtils.isEmpty(kecamatan.getEditText().getText()))
                || (TextUtils.isEmpty(kodepos.getEditText().getText()))
                || (TextUtils.isEmpty(emailPerusahaan.getEditText().getText()))
                || (TextUtils.isEmpty(notelp.getEditText().getText()))
                || (TextUtils.isEmpty(namaPengguna.getEditText().getText()))
                || (TextUtils.isEmpty(noktp.getEditText().getText()))
                || (TextUtils.isEmpty(emailPengguna.getEditText().getText()))
                || (TextUtils.isEmpty(nohp.getEditText().getText()))
                || (TextUtils.isEmpty(username.getEditText().getText()))
                || (TextUtils.isEmpty(password.getEditText().getText()))
                || (TextUtils.isEmpty(konfirmasiPassword.getEditText().getText()))
                || tipeRegistrasiSpinner.getSelectedItemPosition()==0
                || tipeBisnisSpinner.getSelectedItemPosition()==0
                || provinsiSpinner.getSelectedItemPosition()==0
                || kotaSpinner.getSelectedItemPosition()==0
                || kecamatanSpinner.getSelectedItemPosition()==0
                || kelurahanSpinner.getSelectedItemPosition()==0
                || emailPerusahaan.getError()!=null
                || emailPengguna.getError() != null
                || username.isErrorEnabled()
                || password.getError() != null
                || konfirmasiPassword.getError() != null
                || namaPerusahaan.isErrorEnabled()
                //|| kecamatan.isErrorEnabled()
                //|| kelurahan.isErrorEnabled()
                || kodepos.isErrorEnabled()
                || noktp.isErrorEnabled()
        ){
            lanjutBtn.setEnabled(false);
        }
        else{
//            if (!filename.substring(filename.lastIndexOf(".")).equals(".rar")){
//                final Dialog dialog = new Dialog(RegisterActivity.this);
//                dialog.setContentView(R.layout.konfirmasi_dialog);
//                Window window = dialog.getWindow();
//                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                TextView btnBatal = dialog.findViewById(R.id.btnBatal);
//                Button btnSetuju = dialog.findViewById(R.id.btnSetuju);
//                TextView title = dialog.findViewById(R.id.title);
//                TextView description = dialog.findViewById(R.id.description);
//                dialog.setCancelable(false);
//                btnBatal.setVisibility(GONE);
//
//                title.setText("Upload File");
//                description.setText("File yang diupload tidak berupa file rar/zip, harap melakukan upload ulang");
//
//                btnBatal.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//
//                btnSetuju.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
//                            return;
//                        } else {
//                            dialog.dismiss();
//                        }
//                    }
//                });
//
//                dialog.show();
//            }else {
                lanjutBtn.setEnabled(true);
//            }
        }
    }

    /**Method kembalian dari intent ke files (untuk ambil dokumen)*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_FILE && data !=null && data.getData() != null) {
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
            Log.d("ido", "onActivityResult: "+imageuri);
            ImageList.add(imageuri);

            if (tipeUpload.equals("NPWP")) {
                tipeUploadDoc.add("NPWP/SPPKP");
                txtPathNPWP.setText(filename); //nama file
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambarNPWP.setImageBitmap(bitmap);
                    gambarNPWP.setVisibility(View.VISIBLE);
                }

                catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
                btnSelectNPWP.setVisibility(GONE);
            }else if (tipeUpload.equals("KTP")){
                txtPathKTP.setText(filename);
                tipeUploadDoc.add("KTP");
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambarKTP.setImageBitmap(bitmap);
                    gambarKTP.setVisibility(View.VISIBLE);
                }

                catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
                btnSelectKTP.setVisibility(GONE);
            }else if (tipeUpload.equals("SIUP")){
                txtPathSIUP.setText(filename);
                tipeUploadDoc.add("SIUP/SIUI");
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambarSIUP.setImageBitmap(bitmap);
                    gambarSIUP.setVisibility(View.VISIBLE);
                }

                catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
                btnSelectSIUP.setVisibility(GONE);
            }else if (tipeUpload.equals("GXP")){
                txtPathGXP.setText(filename);
                tipeUploadDoc.add("GXP");
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambarGXP.setImageBitmap(bitmap);
                    gambarGXP.setVisibility(View.VISIBLE);
                }

                catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
                btnSelectGXP.setVisibility(GONE);
            }else if (tipeUpload.equals("PBF")){
                txtPathPBF.setText(filename);
                tipeUploadDoc.add("PBF");
                try {
                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filepath);
                    gambarPBF.setImageBitmap(bitmap);
                    gambarPBF.setVisibility(View.VISIBLE);
                }

                catch (IOException e) {
                    // Log the exception
                    e.printStackTrace();
                }
                ext.add(filename.substring(filename.lastIndexOf("."))); //ekstension file);
                btnSelectPBF.setVisibility(GONE);
            }
        }

//        if (resultCode == RESULT_OK && requestCode == PICK_FILE && data !=null && data.getData() != null) {
//            filepath = data.getData(); //path file yang dipilih
//            File file = new File(filepath.toString()); //file yang dipilih
//
//            if (filepath.toString().startsWith("content://")) {
//                Cursor cursor = null;
//                try {
//                    cursor = getApplicationContext().getContentResolver().query(filepath, null, null, null, null);
//                    if (cursor != null && cursor.moveToFirst()) {
//                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                    }
//                } finally {
//                    cursor.close();
//                }
//            } else if (filepath.toString().startsWith("file://")) {
//                filename = file.getName();
//            }
//
//            tvFilename.setText(filename); //nama file
//
//            extension = filename.substring(filename.lastIndexOf(".")); //ekstension file
//            Log.d(TAG, "onActivityResult: "+extension);
//
//            checkValidation();
//            if (!filename.substring(filename.lastIndexOf(".")).equals(".rar")){
//                Toast.makeText(getApplicationContext(), "Harus upload file dengan extensi rar/zip", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    /**Method untuk request tipe bisnis*/
    private void requestTipeBisnis() throws Exception {
        Call <JsonObject> tipeBisnisCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_category where nama != 'Umum';;")));

        tipeBisnisCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            int id = jsonObject.get("id").getAsInt();
                            String name = jsonObject.get("nama").getAsString();
                            TipeBisnis tipeBisnis = new TipeBisnis(id, name);
                            tipeBisnisArrayList.add(tipeBisnis);
                            tipeBisnisContain.add(tipeBisnis.getName());
                            Log.d(TAG, "onResponse: tipe : "+tipeBisnis.getName());
                        }
                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("", "onFailure: "+t.getMessage());
                try {
                    loadingDialog.hideDialog();
                    content.setVisibility(View.INVISIBLE);
                    failed.setVisibility(VISIBLE);
//                    requestTipeBisnis();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**Method untuk cek username apakah sudah ada atau belum*/
    private void checkUsername() throws Exception {
        loadingDialog.showDialog();
        final Call <JsonObject> checkUsernameCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT nama FROM gcm_master_user where username='"+username.getEditText().getText().toString()+"';")));

        checkUsernameCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        if(jsonArray.size()>0){
                            loadingDialog.hideDialog();
                            Toast.makeText(getApplicationContext(),"Username telah dipakai",Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(status.equals("error")){
                        uploadFile();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("", "onFailure: "+t.getMessage());
                try {
                    loadingDialog.hideDialog();
                    Toast.makeText(RegisterActivity.this, "Koneksi gagal di checkUsername", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkUniqData(final String flag){
        String query = "select * from " +
                "(select count (username) as check_username from gcm_master_user gmu where username like '"+username.getEditText().getText().toString()+"') a, " +
                "(select count (no_hp) check_nohp from gcm_master_user gmu where no_hp like '"+nohp.getEditText().getText().toString()+"') b, " +
                "(select count (email) check_email from gcm_master_user gmu where email like '"+emailPengguna.getEditText().getText().toString()+"') c, " +
                "(select count (no_ktp) check_no_ktp from gcm_master_user gmu where no_ktp like '"+noktp.getEditText().getText().toString()+"') d ";

        try {
            Log.d(TAG, "checkUniqData: "+QueryEncryption.Encrypt(query));
            Call<JsonObject> callCheckUniqData = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCheckUniqData.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        Log.d(TAG, "onResponse: sukses kok "+status);
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int username_check = jsonArray.get(0).getAsJsonObject().get("check_username").getAsInt();
                            int no_hp_check = jsonArray.get(0).getAsJsonObject().get("check_nohp").getAsInt();
                            int email_check = jsonArray.get(0).getAsJsonObject().get("check_email").getAsInt();
                            int no_ktp_check = jsonArray.get(0).getAsJsonObject().get("check_no_ktp").getAsInt();
                            Log.d(TAG, "isi check: "+username);
                            if (username_check==0) {
                                Log.d(TAG, "onResponse: " + username);
                                if (no_hp_check == 0) {
                                    if (email_check == 0) {
                                        if (no_ktp_check == 0) {
                                            if (flag.equals("buyer")) {
                                                sellerRequest();
                                            }else{
//                                                uploadFile();
                                                registerBuyer();
                                            }
                                        }else{
                                            Toast.makeText(getApplicationContext(), "Nomor KTP anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Alamat email anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(), "Nomor HP anda sudah terdaftar", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "Username anda sudah terdaftar", Toast.LENGTH_LONG).show();
                            }
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

    /**Method untuk upload file ke firebase*/
    private void uploadFile(){
        if(filepath != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            loadingDialog.hideDialog();
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference riversRef = storageReference.child("dokumen/"+filename.substring(0,filename.indexOf(extension))+Calendar.getInstance().getTimeInMillis()+extension); //directory penyimpanan di firebase storage
            final UploadTask uploadTask = riversRef.putFile(filepath); //upload file

            uploadTask
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //jika berhasil, ambil url
                                    fileUrl = String.valueOf(uri);
                                    loadingDialog.showDialog();
//                                    register(); //lanjut ke register data-data registrasi
//                                    registerBuyer();
                                    Log.d("", "onSuccess: "+fileUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Register gagal pada saat upload task", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage((int) progress+"% Uploaded...");
                        }
                    });
        }
    }

    /**Check permission*/
    private boolean doesUserHavePermission()
    {
        int result = checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**Method untuk request list seller*/
    private void sellerRequest(){
        try {
            Call<JsonObject> sellerCall = RetrofitClient.getInstance().getApi().request(new JSONRequest(QueryEncryption.Encrypt(
                    "SELECT id, nama_perusahaan, tipe_bisnis from gcm_master_company where type='S' and seller_status = 'A';"
            )));

            sellerCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        if(response.body().getAsJsonObject().get("status").getAsString().equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            sellerList = new ArrayList<>();
                            for(int i=0; i<jsonArray.size(); i++){
                                sellerList.add(new Company(
                                        jsonArray.get(i).getAsJsonObject().get("id").getAsInt(),
                                        jsonArray.get(i).getAsJsonObject().get("nama_perusahaan").getAsString(),
                                        jsonArray.get(i).getAsJsonObject().get("tipe_bisnis").getAsInt()
                                ));
                                Log.d("", "onResponse: "+sellerList.get(i).getNamaPerusahaan());
                            }
                            showSellerDialog(); //setelah semua seller berhasil didapatkan maka tampilkan dialog
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    content.setVisibility(View.INVISIBLE);
                    failed.setVisibility(VISIBLE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNoSellerDialog(){
        dialog = new BottomSheetDialog(RegisterActivity.this);
        dialog.setContentView(R.layout.dialog_global_attention);

        TextView message = dialog.findViewById(R.id.tvDetail);
        Button action    = dialog.findViewById(R.id.actionBtn);
        message.setText(getResources().getString(R.string.attention_no_seller));
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**Method untuk menampilkan dialog seller yang akan dipilih*/
    private void showSellerDialog(){
        dialog = new BottomSheetDialog(RegisterActivity.this);
        dialog.setContentView(R.layout.pilih_seller_dialog);
        rvSellerList = dialog.findViewById(R.id.recyclerView);
        pilihSeller = dialog.findViewById(R.id.btnLanjut);
        registerBtn = dialog.findViewById(R.id.registerBtn);
        titleStatusListNull = dialog.findViewById(R.id.tvDescription);

        rvSellerList.setLayoutManager(new LinearLayoutManager(this));
        rvSellerList.setItemAnimator(new DefaultItemAnimator());
        sellerListTemp = new ArrayList<>();
        //hanya seller dengan tipe bisnis yang sama dengan user saja yang dapat dipilih
        if(sellerList.size()>0){
            for(Company company : sellerList){
//                Log.d("ido", "sellerList getId: "+company.getId());
                if(company.getTipeBisnis()==idBisnis || company.getTipeBisnis() == 1){
                    sellerListTemp.add(company);
//                    Log.d("ido", "sellerListTemp getId: "+company.getId());
                }
            }
        }else {
            showNoSellerDialog();
//            titleStatusListNull.setText(getResources().getString(R.string.alert_sellert_list));
        }
        //buat adapter dari list yg baru
        String status = "register";
        sellerListAdapter = new SellerListAdapter(this, sellerListTemp, status);
        rvSellerList.setAdapter(sellerListAdapter);

        //jika register ditekan maka akan lanjut ke pengecekan username
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                        return;
                    }
                    else {
                        //checkUsername();
//                        uploadFile();
                        registerBuyer();
                    }
                    lastClickTime= SystemClock.elapsedRealtime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();
    }

    /**Method untuk mengubah checked value seller*/
    public static void setChecked(int position, boolean isChecked){
        sellerList.get(position).setChecked(isChecked);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    //GET PROVINSI
    private void getProvinsi(){
        try {
            String query = "SELECT * FROM gcm_location_province;";
            Call <JsonObject> provinsiCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            provinsiCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "berhasil req propinsi");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String provinceId = jsonObject.get("id").getAsString();
                                String provinceName = jsonObject.get("name").getAsString();

                                Provinsi provinsi = new Provinsi(provinceId, provinceName);
                                provinsiArrayList.add(provinsi);
                                provinsiContain.add(provinsi.getProvinceName());
                            }
                            loadingDialog.hideDialog();
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
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

    //GET DATA KOTA
    private void getKota(String idProvinsi){
        String query = "SELECT * FROM gcm_master_city where id_provinsi = '"+idProvinsi+"';";
        try {
            Call <JsonObject> kotaCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));

            kotaCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "berhasil req kota");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String cityId = jsonObject.get("id").getAsString();
                                String provinceId = jsonObject.get("id_provinsi").getAsString();
                                String cityName = jsonObject.get("nama").getAsString();

                                Kota kota = new Kota(cityId,provinceId,cityName);
                                kotaArrayList.add(kota);
                                kotaContain.add(kota.getNamaKota());
                            }
                            loadingDialog.hideDialog();
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "gagal req kota");
                    Log.d("", "onFailure: "+t.getMessage());
                    try {
                        loadingDialog.hideDialog();
                        content.setVisibility(View.INVISIBLE);
                        failed.setVisibility(VISIBLE);
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //GET DATA KECAMATAN
    private void getKecamatan(String idKota){
        try {
            Call <JsonObject> kecamatanCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kecamatan where id_city = '"+idKota+"'")));
            kecamatanCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "berhasil req camat");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String kecamatanId = jsonObject.get("id").getAsString();
                                String kotaId = jsonObject.get("id_city").getAsString();
                                String kecamatanName = jsonObject.get("nama").getAsString();

                                Kecamatan kecamatan = new Kecamatan(kecamatanId, kotaId, kecamatanName);
                                kecamatanArrayList.add(kecamatan);
                                kecamatanContain.add(kecamatan.getNamaKecamatan());
                            }
                            loadingDialog.hideDialog();
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "gagal req camat");
                    Log.d("", "onFailure: "+t.getMessage());
                    try {
                        loadingDialog.hideDialog();
                        content.setVisibility(View.INVISIBLE);
                        failed.setVisibility(VISIBLE);
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //GET DATA KELURAHAN
    private void getKelurahan(String idKecamatan){
        try {
            Call <JsonObject> kelurahanCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kelurahan where id_kecamatan = '"+idKecamatan+"';")));
            kelurahanCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "sukses req lurah");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String kelurahanId = jsonObject.get("id").getAsString();
                                String kecamatanId = jsonObject.get("id_kecamatan").getAsString();
                                String kelurahanName = jsonObject.get("nama").getAsString();

                                Kelurahan kelurahan = new Kelurahan(kelurahanId, kecamatanId, kelurahanName);
                                kelurahanArrayList.add(kelurahan);
                                kelurahanContain.add(kelurahan.getNamaKelurahan());
                            }
                            Log.d(TAG, "onResponse: "+kelurahanArrayList.size());
                            loadingDialog.hideDialog();
                        }else{
                            Log.d(TAG, "belom dapet data");
                        }
                    }else{
                        Log.d(TAG, "Status Error");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "Failur kelurahan: "+t.getMessage());
                    try {
                        loadingDialog.hideDialog();
                        content.setVisibility(View.INVISIBLE);
                        failed.setVisibility(VISIBLE);
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //INSERT DATA KE DATABASE
    private void registerBuyer() {

        String sellerStatus, sa_role, listingCompany, listingAlamat, insertCompany;
        Integer sa_divisi;
        String pass = password.getEditText().getText().toString();
        Log.d(TAG, "registerBuyer: "+valueTipeRegister);
        if (valueTipeRegister.equals("S")) {
            sellerStatus = "I";
            sa_role = "admin";
            sa_divisi = idBisnis;
            Log.d(TAG, "registerBuyer: kesini ga ya?");
        } else {
            sellerStatus = null;
            sa_role = null;
            sa_divisi = null;
        }
        String registrasiPerusahaan = "with new_insert1 as (insert into gcm_master_company " +
                "(nama_perusahaan, no_npwp, no_siup, email, no_telp, tipe_bisnis, dokumen_pendukung, " +
                "listing_id, type, seller_status, blacklist_by, notes_blacklist, payment_id) " +
                "values( " +
                "'" + namaPerusahaan.getEditText().getText().toString() + "', " +
                "'" + noNPWP.getEditText().getText().toString() + "', " +
                "'" + noSIUP.getEditText().getText().toString() + "', " +
                "'" + emailPerusahaan.getEditText().getText().toString() + "', " +
                "'" + notelp.getEditText().getText().toString() + "', " +
                idBisnis + "," +
//                "'" + fileUrl + "'," +
                "'listing'," +
                "'" + value + "'," +
                "'" + valueTipeRegister + "', " +
                sellerStatus + ", " +
                "null," +
                "'',null)RETURNING id as id_company),";

        String alamatPerusahaan = " new_insert2 as (insert into gcm_master_alamat (kelurahan, kecamatan, kota, provinsi, " +
                "kodepos, no_telp, shipto_active, billto_active, company_id, alamat, flag_active) " +
                "values( " +
                "'" + idKelurahan + "'," +
                "'" + idKecamatan + "'," +
                "'" + idKota + "'," +
                "'" + idProvinsi + "'," +
                "'" + kodepos.getEditText().getText().toString() + "'," +
                "'" + notelp.getEditText().getText().toString() + "'," +
                "'Y', 'Y', (select id_company from new_insert1)," +
                "'" + alamat.getEditText().getText().toString() + "'," +
                "'A') returning id )";

        String registrasiAkunBuyer = null;
        try {
            registrasiAkunBuyer = " new_insert3 as (insert into gcm_master_user " +
                    "(nama, no_ktp, email, no_hp, username, password, status, role, company_id, create_by, update_by, "+
                    "update_date, sa_role, sa_divisi, email_verif, no_hp_verif, blacklist_by, id_blacklist, is_blacklist, notes_blacklist) " +
                    "VALUES ( " +
                    "'" + namaPengguna.getEditText().getText().toString() + "'," +
                    "'" + noktp.getEditText().getText().toString() + "'," +
                    "'" + emailPengguna.getEditText().getText().toString() + "'," +
                    "'" + nohp.getEditText().getText().toString() + "'," +
                    "'" + username.getEditText().getText().toString() + "'," +
                    "'" + QueryEncryption.Encrypt(pass) + "'," +
                    "'I'," +
                    "'admin'," +
                    "(select id_company from new_insert1)" +
                    ", 0, 0, null, null, null, false, false, null, 0, false,'')),";
        } catch (Exception e) {
            e.printStackTrace();
        }

        String registrasiAkunSeller = null;
        try {
            registrasiAkunSeller = " new_insert3 as (INSERT INTO gcm_master_user " +
                    "(nama, no_ktp, email, no_hp, username, password, status, role, company_id, create_by, update_by, update_date, "+
                    "sa_role, sa_divisi, email_verif, no_hp_verif, blacklist_by, id_blacklist, is_blacklist, notes_blacklist) " +
                    "VALUES ( " +
                    "'" + namaPengguna.getEditText().getText().toString() + "', " +
                    "'" + noktp.getEditText().getText().toString() + "', " +
                    "'" + emailPengguna.getEditText().getText().toString() + "', " +
                    "'" + nohp.getEditText().getText().toString() + "', " +
                    "'" + username.getEditText().getText().toString() + "', " +
                    "'" + QueryEncryption.Encrypt(pass) + "', " +
                    "'I', " +
                    "'admin', " +
                    "(select id_company from new_insert1)" +
                    ", 0, 0, now(), " + sa_role + ", " + sa_divisi + ", false, false, null, 0, false, ''))";
        } catch (Exception e) {
            e.printStackTrace();
        }

        String queryDokumen = " insert into gcm_listing_dokumen(company_id, url_file, tipe) values";
        String loopDokumen = "";
        for (int i=0; i<urlFile.size(); i++){
            loopDokumen = loopDokumen + " ((select id_company from new_insert1), '"+urlFile.get(i).toString()+"', '"+tipeUploadDoc.get(i).toString()+"')";
            if (i < urlFile.size() - 1){
                loopDokumen = loopDokumen.concat(",");
            }
        }

        if (tipeRegistrasiSpinner.getSelectedItemId()==1) {
            listingCompany = " new_insert5 as (INSERT INTO gcm_company_listing (buyer_id, seller_id, buyer_number_mapping, seller_number_mapping, "+
                    "blacklist_by, notes_blacklist) VALUES ";
            listingAlamat = " new_insert6 as (INSERT INTO gcm_listing_alamat (id_master_alamat, id_buyer, id_seller, kode_shipto_customer, kode_billto_customer) VALUES ";
            String loopCompany = "";
            String loopAlamat = "";
            Log.d(TAG, "showSellerDialog: "+sellerListTemp.size());
            ArrayList<Company> Companylist = new ArrayList<>();
            for (int i = 0; i < sellerListTemp.size(); i++) {
                if (sellerListTemp.get(i).isChecked()){
                    Companylist.add(sellerList.get(i));
                }
            }
            for (int i=0; i<Companylist.size(); i++){
                loopCompany = loopCompany + "((select id_company from new_insert1) ," + Companylist.get(i).getId() + ", null, null, null, '')";
                if (i < Companylist.size() - 1) {
                    loopCompany = loopCompany.concat(",");
                }
                if (i == Companylist.size() - 1) {
                    loopCompany = loopCompany.concat("),");
                }
                loopAlamat = loopAlamat + "((select id from new_insert2), (select id_company from new_insert1)," + Companylist.get(i).getId() + ", null, null )";
                if (i < Companylist.size() - 1) {
                    loopAlamat = loopAlamat.concat(",");
                }
                if (i == Companylist.size() - 1){
                    loopAlamat = loopAlamat.concat(")");
                }
            }
            insertCompany = registrasiPerusahaan.concat(alamatPerusahaan).concat(",").concat(registrasiAkunBuyer)
                    .concat(listingCompany.concat(loopCompany).concat(listingAlamat).concat(loopAlamat).concat(queryDokumen.concat(loopDokumen)));
            Log.d(TAG, "dokumen query: "+insertCompany.concat(queryDokumen.concat(loopDokumen)));
        } else {
//            insertCompany = registrasiPerusahaan.concat(alamatPerusahaan).concat(",").concat(registrasiAkunSeller).concat(queryDokumen.concat(loopDokumen));
            insertCompany = registrasiPerusahaan+alamatPerusahaan+","+registrasiAkunSeller+queryDokumen+loopDokumen;
        }
        try {
            Log.d(TAG, "registerBuyer: " + QueryEncryption.Encrypt(insertCompany));
            Log.d(TAG, "registerBuyer: " + insertCompany);
            Call<JsonObject> registerUser = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(insertCompany)));
            registerUser.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        if (valueTipeRegister.equals("S")){
                            loadingDialog.hideDialog();
                            Toast.makeText(RegisterActivity.this, "REGISTRASI BERHASIL", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(i);
                            finish();
                            Log.d(TAG, "onResponse: berhasil");
                        }else {
                            loadingDialog.hideDialog();
                            dialog.hide();
                            Toast.makeText(RegisterActivity.this, "REGISTRASI BERHASIL", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(i);
                            finish();
                            Log.d(TAG, "onResponse: berhasil");
                        }
                    }else{
                        loadingDialog.hideDialog();
                        Toast.makeText(RegisterActivity.this, "REGISTRASI GAGAL, PERIKSA KONEKSI ANDA", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse: gagal");
                    }
                    Log.d(TAG, "onResponse: cek aja");
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "onFailure: jangan2 masuknya kesini");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unggahBerkas(){
        if (filepath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Unggah berkas");
            progressDialog.show();
            for (int i = 0; i < ImageList.size(); i++) {
                // Defining the child of storageReference
                final StorageReference ref = storageReference.child("dokumen/" + Calendar.getInstance().getTimeInMillis()
                        + "-" + ImageList.get(i).getLastPathSegment() + ext.get(i));

                final int finalI = i;
                ref.putFile(ImageList.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                fileUrl = String.valueOf(uri);
                                urlFile.add(fileUrl);
                                Log.d(TAG, "onSuccess: " + fileUrl + "-" + tipeUploadDoc.get(finalI));
                                ImageList.clear();
                                if (urlFile.size()!=0){
                                    btnUpload.setVisibility(GONE);
                                    tvFilename.setVisibility(GONE);
                                    txtSudahUpload.setVisibility(VISIBLE);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Upload gagal", Toast.LENGTH_LONG).show();
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

    private void checkUnggahBerkas(Dialog dialog){
        Log.d(TAG, "checkUnggahBerkas: "+statusRb+" "+idBisnis);
        if (idBisnis == 1){
            if (statusRb.equals("perusahaan")){
                if (txtPathNPWP.getText().toString().equals("No file choosen") ||
                txtPathSIUP.getText().toString().equals("No file choosen") ||
                txtPathGXP.getText().toString().equals("No file choosen")){
                    Toast.makeText(getApplicationContext(), "Berkas belum lengkap, silahkan dilengkapi", Toast.LENGTH_LONG).show();
                }else{
                    unggahBerkas();
                    dialog.dismiss();
                }
            }else if (statusRb.equals("perorangan")){
                if (txtPathKTP.getText().toString().equals("No file choosen") ||
                txtPathNPWP.getText().toString().equals("No file choosen")){
                    Toast.makeText(getApplicationContext(), "Berkas belum lengkap, silahkan dilengkapi", Toast.LENGTH_LONG).show();
                }else{
                    unggahBerkas();
                    dialog.dismiss();
                }
            }
        }else{
            if (statusRb.equals("perusahaan")){
                if (txtPathNPWP.getText().toString().equals("No file choosen") ||
                        txtPathSIUP.getText().toString().equals("No file choosen")){
                    Toast.makeText(getApplicationContext(), "Berkas belum lengkap, silahkan dilengkapi", Toast.LENGTH_LONG).show();
                }else{
                    unggahBerkas();
                    dialog.dismiss();
                }
            }else if (statusRb.equals("perorangan")){
                if (txtPathKTP.getText().toString().equals("No file choosen") ||
                        txtPathNPWP.getText().toString().equals("No file choosen")){
                    Toast.makeText(getApplicationContext(), "Berkas belum lengkap, silahkan dilengkapi", Toast.LENGTH_LONG).show();
                }else{
                    unggahBerkas();
                    dialog.dismiss();
                }
            }
        }
    }
}


    /**Method untuk request daftar lokasi (provinsi dan kota)*/
    /**private void requestLokasi() throws Exception {
        int i = 0;
        while(i < 90000 ){
            Log.d(TAG, "SELECT * FROM gcm_master_kelurahan limit "+(25000)+" offset "+(i));
            Call <JsonObject> kelurahanCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kelurahan limit "+25000+" offset "+(i)+";")));
            kelurahanCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            Log.d(TAG, "sukses get data response");
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i=0; i<jsonArray.size(); i++){
                                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                                String kelurahanId = jsonObject.get("id").getAsString();
                                String kecamatanId = jsonObject.get("id_kecamatan").getAsString();
                                String kelurahanName = jsonObject.get("nama").getAsString();

                                Kelurahan kelurahan = new Kelurahan(kelurahanId, kecamatanId, kelurahanName);
                                kelurahanArrayList.add(kelurahan);
                            }
                            Log.d(TAG, "onResponse: "+kelurahanArrayList.size());
                            loadingDialog.hideDialog();
                        }else{
                            Log.d(TAG, "Status Error in kelurahan");
                        }
                    }else{
                        Log.d(TAG, "Status Error in kelurahan");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "Failur kelurahan: "+t.getMessage());
                    try {
                        loadingDialog.hideDialog();
                        content.setVisibility(View.INVISIBLE);
                        failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            i+=25000;
        }
        Call <JsonObject> provinsiCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_location_province;")));
        provinsiCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        Log.d(TAG, "berhasil req propinsi");
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String provinceId = jsonObject.get("id").getAsString();
                            String provinceName = jsonObject.get("name").getAsString();

                            Provinsi provinsi = new Provinsi(provinceId,provinceName);
                            provinsiArrayList.add(provinsi);
                            provinsiContain.add(provinsi.getProvinceName());
                        }
                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "gagal req propinsi");
                Log.d("", "onFailure: "+t.getMessage());
                try {
                    loadingDialog.hideDialog();
                    content.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //REQUEST KOTA
        Call <JsonObject> kotaCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_city;")));

        kotaCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        Log.d(TAG, "berhasil req kota");
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String cityId = jsonObject.get("id").getAsString();
                            String provinceId = jsonObject.get("id_provinsi").getAsString();
                            String cityName = jsonObject.get("nama").getAsString();

                            Kota kota = new Kota(cityId,provinceId,cityName);
                            kotaArrayList.add(kota);
                        }
                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "gagal req kota");
                Log.d("", "onFailure: "+t.getMessage());
                try {
                    loadingDialog.hideDialog();
                    content.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //ido yang edit
        Call <JsonObject> kecamatanCall = RetrofitClient
                .getInstance()
                .getApi()
                .request(new JSONRequest(QueryEncryption.Encrypt("SELECT * FROM gcm_master_kecamatan;")));
        kecamatanCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    String status = response.body().getAsJsonObject().get("status").getAsString();
                    if(status.equals("success")){
                        Log.d(TAG, "berhasil req camat");
                        JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                        for(int i=0; i<jsonArray.size(); i++){
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String kecamatanId = jsonObject.get("id").getAsString();
                            String kotaId = jsonObject.get("id_city").getAsString();
                            String kecamatanName = jsonObject.get("nama").getAsString();

                            Kecamatan kecamatan = new Kecamatan(kecamatanId, kotaId, kecamatanName);
                            kecamatanArrayList.add(kecamatan);
                        }
                        loadingDialog.hideDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "gagal req camat");
                Log.d("", "onFailure: "+t.getMessage());
                try {
                    loadingDialog.hideDialog();
                    content.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
//                    requestLokasi();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //ido yang edit


    }*/

