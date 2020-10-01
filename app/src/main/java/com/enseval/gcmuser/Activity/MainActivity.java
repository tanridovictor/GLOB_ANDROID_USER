package com.enseval.gcmuser.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Fragment.HomeFragment;
import com.enseval.gcmuser.Fragment.NegoFragment;
import com.enseval.gcmuser.Fragment.NotLoggedInFragment;
import com.enseval.gcmuser.Fragment.OrderFragment;
import com.enseval.gcmuser.Fragment.ProfileFragment;
import com.enseval.gcmuser.Fragment.ProfileUserFragment;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.enseval.gcmuser.Utilities.OnBackPressedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ImageView cart, chat, notif;
    private static TextView badgeNotifCart, badgeNotifNotif;
    private static BottomNavigationView navigationView;
    private String fragmentFlag;
    private long lastClickTime=0;

    /**Method untuk chheck permission untuk melakukan panggilan*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==2){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(Intent.ACTION_DIAL);
                        String p = "tel:" + "081310695040";
                        i.setData(Uri.parse(p));
                        startActivity(i);
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new
           OnCompleteListener<InstanceIdResult>() {
               @Override
               public void onComplete(@NonNull Task<InstanceIdResult> task) {
                   String userToken;
                   if (task.isSuccessful()){
                       userToken= task.getResult().getToken();
                       Log.e("FCM", "User Token: " + userToken);
                   }else{
                       Log.e("FCM", "get user token Failed", task.getException());
                   }
               }
           });

        cartNotif();
        notifBadge();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        cart = (ImageView) findViewById(R.id.imgBtnCart);
        chat = findViewById(R.id.chat);
        notif = findViewById(R.id.notif);
        badgeNotifCart = findViewById(R.id.badgeNotif);
        badgeNotifNotif = findViewById(R.id.badgenotif);

        navigationView = findViewById(R.id.btmNav);
        navigationView.setOnNavigationItemSelectedListener(this);

        fragmentFlag=null;

        fragmentFlag = getIntent().getStringExtra("fragment");

        //flag untuk menandakan akan menuju fragment mana
        if(fragmentFlag==null || fragmentFlag.equals("homeFragment")){
            navigationView.getMenu().getItem(0).setChecked(true);
            displayFragment(new HomeFragment());
        }
        else if(fragmentFlag.equals("negoFragment")){
            displayFragment(new NegoFragment());
            navigationView.getMenu().getItem(1).setChecked(true);
        }
        else if(fragmentFlag.equals("orderFragment")){
            displayFragment(new OrderFragment());
            navigationView.getMenu().getItem(2).setChecked(true);
        }
        else if(fragmentFlag.equals("profileFragment")){
            displayFragment(new ProfileUserFragment());
            navigationView.getMenu().getItem(3).setChecked(true);
        }

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fungsi untuk handle agar tidak bisa double click
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(MainActivity.this, CartActivity.class);
                    if(navigationView.getSelectedItemId()==R.id.navbar_home){
                        startActivityForResult(intent, 100);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_nego){
                        startActivityForResult(intent, 200);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_order){
                        startActivityForResult(intent, 300);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_profile){
                        startActivityForResult(intent, 400);
                    }
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fungsi untuk handle agar tidak bisa double click
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(MainActivity.this, AllChatsActivity.class);
                    if(navigationView.getSelectedItemId()==R.id.navbar_home){
                        startActivityForResult(intent, 100);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_nego){
                        startActivityForResult(intent, 200);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_order){
                        startActivityForResult(intent, 300);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_profile){
                        startActivityForResult(intent, 400);
                    }
                    finish();
                }
                lastClickTime= SystemClock.elapsedRealtime();
            }
        });

        notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                    if(navigationView.getSelectedItemId()==R.id.navbar_home){
                        startActivityForResult(intent, 100);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_nego){
                        startActivityForResult(intent, 200);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_order){
                        startActivityForResult(intent, 300);
                    }
                    else if(navigationView.getSelectedItemId()==R.id.navbar_profile){
                        startActivityForResult(intent, 400);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**method untuk menampilkan fragment yang dituju*/
    private void displayFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.navbar_home:
                fragment = new HomeFragment();
                menuItem.setChecked(true);
                break;
            case R.id.navbar_order:
                if(SharedPrefManager.getInstance(this).isLoggedin()){
                    fragment = new OrderFragment();
                }
                else {
                    fragment = new NotLoggedInFragment();
                }
                menuItem.setChecked(true);
                break;
            case R.id.navbar_nego:
                if(SharedPrefManager.getInstance(this).isLoggedin()){
                    fragment = new NegoFragment();
                }
                else {
                    fragment = new NotLoggedInFragment();
                }
                menuItem.setChecked(true);
                break;
            case R.id.navbar_profile:
                if(SharedPrefManager.getInstance(this).isLoggedin()){
                    fragment = new ProfileUserFragment();
                }
                else {
                    fragment = new NotLoggedInFragment();
                }
                menuItem.setChecked(true);
                break;
        }

        if (fragment != null) {
            displayFragment(fragment);
        }
        return false;
    }

    /**method untuk mengganti menu yang dipilih di navbar*/
    public static void changeNavbar(int position){
        navigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = getIntent();
        finish();
        if(requestCode==100){
            intent.putExtra("fragment", "homeFragment");
        }
        else if(requestCode==200){
            intent.putExtra("fragment", "negoFragment");
        }
        else if(requestCode==300){
            intent.putExtra("fragment", "orderFragment");
        }
        else if(requestCode==400){
            intent.putExtra("fragment", "profileFragment");
        }
        startActivity(intent);
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
        builder.setMessage(getResources().getString(R.string.attention_leave_app))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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

    private void cartNotif(){
        String query = "select count(id) as jumlah from gcm_master_cart gmc where company_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and status = 'A'";
        try {
            Call<JsonObject> callCartNotif = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCartNotif.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int jumlah = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();
                            if (jumlah==0){
                                badgeNotifCart.setVisibility(View.GONE);
                            }else {
                                badgeNotifCart.setText(String.valueOf(jumlah));
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

    private void notifBadge(){
        String query = "select count(a.id) as jumlah from (select a.id, a.seller_id " +
                "from gcm_notification_nego a " +
                "inner join gcm_list_barang b on a.barang_id = b.id " +
                "inner join gcm_master_barang c on b.barang_id = c.id " +
                "inner join gcm_master_company d on a.buyer_id = d.id " +
                "where a.read_flag = 'N' and a.source = 'seller' and now() >= a.date and a.buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+") a " +
                "inner join gcm_master_company b on a.seller_id = b.id";
        try {
            Call<JsonObject> callCartNotif = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callCartNotif.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int jumlah = jsonArray.get(0).getAsJsonObject().get("jumlah").getAsInt();
                            if (jumlah==0){
                                badgeNotifNotif.setVisibility(View.GONE);
                            }else {
                                badgeNotifNotif.setText(String.valueOf(jumlah));
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

    public static void notifNotif(int jumlah){
        badgeNotifNotif.setVisibility(View.VISIBLE);
        badgeNotifNotif.setText(String.valueOf(jumlah));
    }
}
