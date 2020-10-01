package com.enseval.gcmuser.Fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.ListAlamat;
import com.enseval.gcmuser.Activity.MainActivity;
import com.enseval.gcmuser.OTP.OTPActivity;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.enseval.gcmuser.Utilities.Helper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.Calendar;

//import fr.ganfra.materialspinner.MaterialSpinner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private Button logoutBtn, btnTambahUser, btnTambahAlamat;
    private TextView tvNamaPerusahaan, tvStatus, tvKontak, tvAlamat, tvTipeBisnis,
            tvNamaUser, tvStatusUser, tvKontakUser, tvUsername, tvRole, tvKTP,
            tvDokumen;
    private String statusPerusahaan, statusUser, role;
    private ShimmerFrameLayout shimmerFrameLayout, shimmerFrameLayout2;
    private ConstraintLayout consCompany, consUser;
    private CardView cardView1, cardView2;
    private int primary, accent;
    private static final int PICK_FILE = 100;
    private Uri filepath;
    private String fileUrl, extension;
    private String filename = null;
    private Dialog dialog = null;
    private TextView tvFilename;
    private StorageReference storageReference;
    private Button btnKirim, btnOTP;
    private ConstraintLayout failed;
    private Button refresh;
    private ScrollView scrollView;
    private long lastClickTime = 0;
    private LoadingDialog loadingDialog;

    private String alamat;
    private String TAG = "ido";

    private TextInputLayout inputPhone;

    private CardView layoutOTP, layoutVerify;
    private String statusVerify = null;

    private LinearLayout layoutInputNomor;
    private RelativeLayout layoutVerified;

    private Spinner spinnTypeOTP;
    private String viaOTP;
    private String kontak_no_hp, temp_kontak_no_hp, temp_username;
    private Boolean isValidNumber = false;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        primary = getContext().getResources().getColor(R.color.colorPrimary);
        accent = getContext().getResources().getColor(R.color.colorAccent);

        storageReference = FirebaseStorage.getInstance().getReference();

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logoutBtn = view.findViewById(R.id.logoutBtn);
        tvNamaPerusahaan = view.findViewById(R.id.tvNamaPerusahaan);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvNamaUser = view.findViewById(R.id.tvNamaUser);
        tvStatusUser = view.findViewById(R.id.tvStatusUser);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_company);
        shimmerFrameLayout2 = view.findViewById(R.id.shimmer_view_user);
        consUser = view.findViewById(R.id.consUser);
        consCompany = view.findViewById(R.id.consCompany);
        cardView1 = view.findViewById(R.id.cardView1);
        cardView2 = view.findViewById(R.id.cardView2);
        tvKontak = view.findViewById(R.id.tvKontak);
        tvTipeBisnis = view.findViewById(R.id.tvTipeBisnis);
        tvAlamat = view.findViewById(R.id.tvAlamat);
        tvDokumen = view.findViewById(R.id.tvDokumen);
        failed = view.findViewById(R.id.failed);
        refresh = view.findViewById(R.id.refresh);
        scrollView = view.findViewById(R.id.scroll);
        tvKontakUser = view.findViewById(R.id.tvKontakUser);
        tvRole = view.findViewById(R.id.tvRole);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvKTP = view.findViewById(R.id.tvKTP);
        btnTambahUser = view.findViewById(R.id.btnTambahUser);
        btnTambahAlamat = view.findViewById(R.id.btnTambahAlamat);

        spinnTypeOTP = view.findViewById(R.id.spinnViaOTP);

        layoutInputNomor = view.findViewById(R.id.inputNomor);
        layoutVerified = view.findViewById(R.id.verified);

        inputPhone = view.findViewById(R.id.nomorHandphone);

        loadingDialog = new LoadingDialog(getActivity());

        failed.setVisibility(View.INVISIBLE);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.otp_via, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnTypeOTP.setAdapter(adapter);
        spinnTypeOTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viaOTP = spinnTypeOTP.getSelectedItem().toString();
                Log.d("cekitViaOTP", viaOTP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinnTypeOTP.setPrompt(getResources().getString(R.string.prompt_otp_via));
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                } else {

                    Intent intent = new Intent(getActivity().getIntent());
                    intent.putExtra("fragment", "profileFragment");
                    getActivity().finish();
                    startActivity(intent);
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });


        scrollView.setVisibility(View.VISIBLE);
        consCompany.setVisibility(View.INVISIBLE);
        consUser.setVisibility(View.INVISIBLE);

        tvDokumen.setVisibility(View.GONE);
        btnTambahUser.setVisibility(View.GONE);
        //btnTambahAlamat.setVisibility(view.GONE);

        btnOTP = view.findViewById(R.id.btnSendOTP);
        btnOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.konfirmasi_bottom_sheet_dialog);
                TextView text = bottomSheetDialog.findViewById(R.id.tvMessage);
                Button negative = bottomSheetDialog.findViewById(R.id.negative);
                Button positive = bottomSheetDialog.findViewById(R.id.positive);

                text.setText(getResources().getString(R.string.otp_msg_number) + "  " + inputPhone.getEditText().getText() + " ? ");
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String valuePhone = String.valueOf(inputPhone.getEditText().getText());
                        Log.d("cekit", String.valueOf(spinnTypeOTP.getSelectedItemId()));
                        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                            return;
                        } else {
                            validPhoneNumber(inputPhone.getEditText().getText().toString(), temp_username);
                            if (inputPhone.getEditText().getText().length() >= 10 && spinnTypeOTP.getSelectedItemId() != 0) {
                                if (isValidNumber==true) {
                                    sendOTP(valuePhone, viaOTP);
                                } else {
                                    dialog = new BottomSheetDialog(getContext());
                                    dialog.setContentView(R.layout.dialog_global_attention);

                                    TextView message = dialog.findViewById(R.id.tvDetail);
                                    Button action = dialog.findViewById(R.id.actionBtn);
                                    message.setText(getResources().getString(R.string.attention_incorrect_number));
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

        getVerifiedStatus();

        if (SharedPrefManager.getInstance(getActivity()).isLoggedin()) {
            companyRequest();
        } else {
            shimmerFrameLayout.stopShimmerAnimation();
            shimmerFrameLayout.setVisibility(View.GONE);
            shimmerFrameLayout2.stopShimmerAnimation();
            shimmerFrameLayout2.setVisibility(View.GONE);
            cardView1.setVisibility(View.GONE);
            cardView2.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.GONE);
        }

        //jika button logout ditekan
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tampilkan dialog konfirmasi
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                builder.setMessage("Anda yakin ingin keluar dari akun ini?");
                builder.setPositiveButton("Ya",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //jika ya, hapus info login dari local storage.
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return;
                                } else {
                                    deleteToken();
                                    SharedPrefManager.getInstance(getActivity()).clearUser();
                                    SharedPrefManager.getInstance(getActivity()).clearActiveSeller();
                                    SharedPrefManager.getInstance(getActivity()).clearToken();
                                    SharedPrefManager.getInstance(getActivity()).clearMessages();
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                            }
                        });
                builder.setNegativeButton("Batal",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                Button pButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            }
        });

        //ini untuk mekanisme tambah dokumen, tapi nanti disesuaikan lg aja sama mekanisme terbaru (verifikasi oleh masing2 seller)
        tvDokumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                } else {
                    dialog = new Dialog(getActivity());
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dokumen_dialog);

                    TextView tvTitle = dialog.findViewById(R.id.tvTitle);
                    TextView tvDetail = dialog.findViewById(R.id.tvDetail);
                    ImageView close = dialog.findViewById(R.id.close);
                    tvFilename = dialog.findViewById(R.id.tvFilename);
                    Button btnUpload = dialog.findViewById(R.id.btnUpload);
                    btnKirim = dialog.findViewById(R.id.kirimBtn);
                    tvFilename.setVisibility(View.GONE);

                    if (filename == null) {
                        btnKirim.setEnabled(false);
                    } else {
                        btnKirim.setEnabled(true);
                    }

                    if (statusPerusahaan.equals("A")) {
                        tvTitle.setText("Tambah Dokumen");
                        tvDetail.setText("Diwajibkan untuk mengupload seluruh dokumen baik yang lama maupun yang akan ditambahkan " +
                                "dan dimasukkan ke dalam satu buah file zip/rar.\n\n" +
                                "Selama proses verifikasi berlangsung, akun akan dibekukan sementara dari proses pembelian barang baru.");
                    } else if (statusPerusahaan.equals("R")) {
                        tvTitle.setText("Revisi Dokumen");
                        if (SharedPrefManager.getInstance(getActivity()).getUser().getTipeBisnis() == 1) {
                            tvDetail.setText("Silahkan siapkan seluruh dokumen yang dibutuhkan secara lengkap " +
                                    "dan dimasukkan ke dalam satu buah file zip/rar.\n\n" +
                                    "Dokumen yang dibutuhkan adalah NPWP, TDP, SIUP, SPPKP, dan Izin Farmasi");
                        } else {
                            tvDetail.setText("Silahkan siapkan seluruh dokumen yang dibutuhkan secara lengkap " +
                                    "dan dimasukkan ke dalam satu buah file zip/rar.\n\n" +
                                    "Dokumen yang dibutuhkan adalah NPWP, TDP, SIUP, dan SPPKP");
                        }
                    }

                    btnUpload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return;
                            } else {
                                if (!doesUserHavePermission()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    }
                                } else {
                                    Intent intent = new Intent();
                                    intent.setType("*/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "SELECT FILE"), PICK_FILE);
                                }
                            }
                            lastClickTime = SystemClock.elapsedRealtime();
                        }
                    });

                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btnKirim.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return;
                            } else {
                                uploadFile();
                            }
                            lastClickTime = SystemClock.elapsedRealtime();
                        }
                    });

                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                dialog.dismiss();
                            }
                            return true;
                        }
                    });

                    dialog.show();
                }
                lastClickTime = SystemClock.elapsedRealtime();
            }
        });

        //untuk tambah akun user
        btnTambahUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                bottomSheetDialog.setContentView(R.layout.konfirmasi_bottom_sheet_dialog);

                Button negative = bottomSheetDialog.findViewById(R.id.negative);
                Button positive = bottomSheetDialog.findViewById(R.id.positive);

                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //tampilkan bottom sheet dialog untuk isi data-data (selengkapnya cek di NewAccountBottomSheetDialog)
                        BottomSheetDialog bottomSheetDialog1 = new NewAccountBottomSheetDialog(getActivity());
                        bottomSheetDialog1.setContentView(R.layout.new_account_bottom_sheet_dialog);
                        bottomSheetDialog.dismiss();
                        bottomSheetDialog1.show();
                    }
                });

                bottomSheetDialog.show();
            }
        });

        btnTambahAlamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final BottomSheetDialog bottomSheetDialog = new TambahAlamatBottomSheetDialog(getActivity());
//                bottomSheetDialog.setContentView(R.layout.tambah_alamat_bootom_sheet_dialog);
//                bottomSheetDialog.dismiss();
//                bottomSheetDialog.show();
                Intent intent = new Intent(getContext(), ListAlamat.class);
                intent.putExtra("tipe", "edit");
                startActivity(intent);
            }
        });
    }

    private void btnShowByUserRole() {
        if (role.equalsIgnoreCase("user")) {
            btnTambahUser.setVisibility(View.GONE);
        } else {
            btnTambahUser.setVisibility(View.VISIBLE);
        }
    }

    public boolean isVerified() {
        return true;
    }

    private void getVerifiedStatus() {
        String userID = String.valueOf(SharedPrefManager.getInstance(getContext()).getUser().getUserId());
        String querryVerified = "SELECT id, no_hp_verif FROM public.gcm_master_user where id = " + userID;
        Log.d("cekit", querryVerified);
        if (Helper.isOnline(getActivity())) {
            try {
                Call<JsonObject> verifyPhone = RetrofitClient
                        .getInstance()
                        .getApi()
                        .request(new JSONRequest(QueryEncryption.Encrypt(querryVerified)));
                verifyPhone.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, final Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            String status = response.body().getAsJsonObject().get("status").getAsString();
                            if (status.equals("success")) {
                                JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                                String statusVerifikasi = jsonArray.get(0).getAsJsonObject().get("no_hp_verif").getAsString();
                                statusVerify = statusVerifikasi;
                                if (statusVerify.equals("true")) {
                                    layoutInputNomor.setVisibility(View.GONE);
                                    layoutVerified.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, final Throwable t) {
                        new FancyAlertDialog.Builder(getActivity()).setTitle(getResources().getString(R.string.attention_label))
                                .isCancellable(false)
                                .setMessage(getResources().getString(R.string.on_failure))
                                .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                                .setAnimation(Animation.POP)
                                .setPositiveBtnText("Ok")
                                .OnPositiveClicked(new FancyAlertDialogListener() {
                                    @Override
                                    public void OnClick() {
                                        Toast.makeText(getContext(), "Error = " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).build();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.attention_offline_mode), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOTP(String valuePhone, String valueVia) {
        Intent intent = new Intent(getContext(), OTPActivity.class);
        intent.putExtra("phonenumber", valuePhone);
        intent.putExtra("via", valueVia);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    //ini mekanisme untuk ambil file yang dipilih pada saat upload dokumen (sama seperti pada registrasi, penjelasan detail cek di registrasi)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_FILE && data != null && data.getData() != null) {
            filepath = data.getData();
            File file = new File(filepath.toString());

//            ContentResolver cr = getApplicationContext().getContentResolver();
//            String filetype = cr.getType(filepath);
//            extension = filetype.substring(filetype.lastIndexOf("/")+1);

            if (filepath.toString().startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContext().getContentResolver().query(filepath, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (filepath.toString().startsWith("file://")) {
                filename = file.getName();
            }

            tvFilename.setVisibility(View.VISIBLE);
            tvFilename.setText(filename);

            extension = filename.substring(filename.lastIndexOf("."));

            if (filename == null) {
                btnKirim.setEnabled(false);
            } else {
                btnKirim.setEnabled(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout2.stopShimmerAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmerAnimation();
        shimmerFrameLayout2.startShimmerAnimation();
    }

    /**
     * Method untuk request data perusahaan dan user
     */
    private void companyRequest() {
        try {
            Call<JsonObject> companyCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT nama_perusahaan, e.nama as nama_tipe_bisnis, " +
                            " h.alamat, g.nama as kelurahan, f.nama as kecamatan, c.nama as kota, d.name as provinsi, " +
                            "h.kodepos, a.email as p_email, a.no_telp," +
                            "tipe_bisnis, b.nama as u_nama, no_ktp, username, b.email as u_email, no_hp, role, b.status as u_status, b.no_hp " +
                            "FROM gcm_master_company a inner join gcm_master_user b on a.id=b.company_id inner join gcm_master_alamat h on a.id=h.company_id " +
                            "inner join gcm_master_city c on h.kota=c.id inner join gcm_location_province d on h.provinsi=d.id " +
                            "inner join gcm_master_category e on a.tipe_bisnis=e.id inner join gcm_master_kecamatan f on h.kecamatan = f.id inner join gcm_master_kelurahan g on h.kelurahan = g.id " +
                            "where a.id=" + SharedPrefManager.getInstance(getActivity()).getUser().getCompanyId() + " and " +
                            "b.id=" + SharedPrefManager.getInstance(getActivity()).getUser().getUserId() + " and h.billto_active = 'Y';")));

            companyCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            JsonObject jsonObject = response.body().get("data").getAsJsonArray().get(0).getAsJsonObject();
//                            statusPerusahaan = jsonObject.get("p_status").getAsString();
                            statusUser = jsonObject.get("u_status").getAsString();
                            role = jsonObject.get("role").getAsString();

                            shimmerFrameLayout.stopShimmerAnimation();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout2.stopShimmerAnimation();
                            shimmerFrameLayout2.setVisibility(View.GONE);

                            tvNamaPerusahaan.setText(jsonObject.get("nama_perusahaan").getAsString());
                            tvTipeBisnis.setText(jsonObject.get("nama_tipe_bisnis").getAsString());
                            tvAlamat.setText(String.format("%s, %s, %s\n%s\n%s %s",
                                    jsonObject.get("alamat").getAsString(),
                                    WordUtils.capitalizeFully(jsonObject.get("kelurahan").getAsString().toLowerCase().trim()),
                                    WordUtils.capitalizeFully(jsonObject.get("kecamatan").getAsString().toLowerCase().trim()),
                                    WordUtils.capitalizeFully(jsonObject.get("kota").getAsString().toLowerCase().trim()),
                                    WordUtils.capitalizeFully(jsonObject.get("provinsi").getAsString().toLowerCase().trim()),
                                    jsonObject.get("kodepos").getAsString()));
                            tvKontak.setText(String.format("%s\n%s",
                                    jsonObject.get("p_email").getAsString(),
                                    jsonObject.get("no_telp").getAsString()));
                            tvNamaUser.setText(jsonObject.get("u_nama").getAsString());
                            tvUsername.setText(jsonObject.get("username").getAsString());
                            tvKontakUser.setText(String.format("%s\n%s",
                                    jsonObject.get("u_email").getAsString(),
                                    jsonObject.get("no_hp").getAsString()));
                            tvKTP.setText(jsonObject.get("no_ktp").getAsString());

                            temp_kontak_no_hp = jsonObject.get("no_hp").getAsString();
                            temp_username = jsonObject.get("username").getAsString();

                            inputPhone.setHint(temp_kontak_no_hp);

                            btnShowByUserRole();

                            if (role.equals("superadmin")) {
                                tvRole.setText("Admin GCM");
                            } else if (role.equals("admin")) {
                                tvRole.setText("Superuser (Owner)");
                            }
                            if (role.equals("user")) {
                                tvRole.setText("User");
                            }

//                            if(statusPerusahaan.equals("A")){
//                                tvStatus.setText("Aktif");
//                                tvStatus.setTextColor(primary);
//                                tvDokumen.setVisibility(View.VISIBLE);
//                            }
//                            else if(statusPerusahaan.equals("I")) {
//                                tvStatus.setText("Belum diverifikasi");
//                                tvStatus.setTextColor(accent);
//                                tvDokumen.setVisibility(View.GONE);
//                            }
//                            else if(statusPerusahaan.equals("R")){
//                                tvStatus.setText("Tidak lolos verifikasi");
//                                tvStatus.setTextColor(accent);
//                                tvDokumen.setVisibility(View.VISIBLE);
//                            }

                            tvStatus.setText("Cek status perusahaan");
                            tvStatus.setTextColor(accent);
                            tvStatus.setPaintFlags(tvStatus.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                            //untuk melihat status perusahaan, panggil StatusBottomSheetDialogFragment
                            tvStatus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    BottomSheetDialogFragment bottomSheetDialogFragment = new StatusBottomSheetDialogFragment();
                                    bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "STAT");
                                }
                            });

                            if (statusUser.equals("A")) {
                                tvStatusUser.setText("Aktif");
                                tvStatusUser.setTextColor(primary);
                            } else if (statusUser.equals("I")) {
                                tvStatusUser.setText("Belum diverifikasi");
                                tvStatusUser.setTextColor(accent);
                                btnTambahUser.setVisibility(View.GONE);
                            } else if (statusUser.equals("R")) {
                                tvStatusUser.setText("Tidak lolos verifikasi");
                                tvStatusUser.setTextColor(accent);
                                btnTambahUser.setVisibility(View.GONE);
                            }
                            consCompany.setVisibility(View.VISIBLE);
                            consUser.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    scrollView.setVisibility(View.INVISIBLE);
                    failed.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validPhoneNumber(final String pass_kontak_no_hp, String pass_username) {
        loadingDialog.showDialog();

        try {
            String q = "select no_hp from gcm_master_user where username = '" + pass_username + "'";
            Call<JsonObject> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(q)));
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            loadingDialog.hideDialog();
                            JsonObject jsonObject = response.body().get("data").getAsJsonArray().get(0).getAsJsonObject();
                            String temp_no_hp = jsonObject.get("no_hp").getAsString();
                            inputPhone.setHint(temp_no_hp);
                            if (pass_kontak_no_hp.equals(temp_no_hp)) {
                                isValidNumber = true;
                                Toast.makeText(getContext(), "Nomor kontak yang dimasukkan sama", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loadingDialog.hideDialog();
                        }
                    } else {
                        loadingDialog.hideDialog();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    t.printStackTrace();
                    Log.wtf("", t.toString());
                }
            });

        } catch (Exception e) {
            loadingDialog.hideDialog();
            e.printStackTrace();
            Log.wtf("", e.toString());
        }
        ;
    }

    /**
     * Method untuk upload file dokumen baru (disesuaikan saja dengan mekanisme yg baru)
     */
    private void uploadFile() {
        if (filepath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference riversRef = storageReference.child("dokumen/" + filename.substring(0, filename.indexOf(extension)) + Calendar.getInstance().getTimeInMillis() + extension);
            final UploadTask uploadTask = riversRef.putFile(filepath);

            uploadTask
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            loadingDialog.showDialog();
                            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    fileUrl = String.valueOf(uri);
                                    updateUrl();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Koneksi gagal", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage((int) progress + "% Uploaded...");
                        }
                    });
        }
    }

    /**
     * Method untuk mengupdate url dokumen di database (Disesuaikan saja dengan mekanisme yg baru)
     */
    private void updateUrl() {
        try {
            Call<JsonObject> updateUrlCall = RetrofitClient
                    .getInstance2()
                    .getApi()
                    .requestInsert(new JSONRequest(QueryEncryption.Encrypt("UPDATE gcm_master_company set dokumen_pendukung='" + fileUrl +
                            "', status='I', update_by=" + SharedPrefManager.getInstance(getContext()).getUser().getUserId()
                            + " where id=" + SharedPrefManager.getInstance(getActivity()).getUser().getCompanyId() + ";")));

            updateUrlCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")) {
                            loadingDialog.hideDialog();
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            getContext().startActivity(intent);
                            dialog.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    loadingDialog.hideDialog();
                    Toast.makeText(getContext(), "Koneksi gagal", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method pengecekan permission
     */
    private boolean doesUserHavePermission() {
        int result = getContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void deleteToken(){
        loadingDialog.showDialog();
        String query = "delete from gcm_notification_token where token = '"+SharedPrefManager.getInstance(getContext()).getToken()+"' and user_id = "+SharedPrefManager.getInstance(getContext()).getUser().getUserId()+" " +
                "and company_id = "+SharedPrefManager.getInstance(getContext()).getUser().getCompanyId()+"";
        Log.d(TAG, "deleteToken: "+query);
        try {
            Call<JsonObject> callDeleteToken = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callDeleteToken.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        loadingDialog.hideDialog();
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
