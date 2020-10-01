package com.enseval.gcmuser.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.ChatListAdapter;
import com.enseval.gcmuser.Model.Chatroom;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllChatsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private ArrayList<Chatroom> chatroomList;
    private ArrayList<Chatroom> listromm;
    private int userId, companyId, isUser, lastSenderId;
    private String roomId, lastMessage;
    private boolean isRead;
    private Calendar calendar;
    private int counter;
    private ChatListAdapter chatListAdapter;
    private long lastClickTime = 0;
    private LinearLayout back;
    private ImageView srcBtn;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot(); //database referencenya adalah root atau yg terluar

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chats);

        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.rvAllChats);
        back = findViewById(R.id.back);
        srcBtn = findViewById(R.id.searchBtn);

        etSearch.setVisibility(View.GONE);



        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //getKey();

        chatroomList = new ArrayList<>();

        //buat adapter untuk masing-masing chatroom
//        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(AllChatsActivity.this);
//        recyclerView.setLayoutManager(layoutManager1);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        chatListAdapter = new ChatListAdapter(AllChatsActivity.this, chatroomList);
//        recyclerView.setAdapter(chatListAdapter);

        //mengecek apakah chatroom sudah tersimpan di local storage
        //jika sudah, ambil dulu chatroom yg sudah disimpan tersebut
//        if(SharedPrefManager.getInstance(this).isChatroomSaved()){
//            chatroomList.clear();
//            ArrayList<Chatroom> chatroomListTemp = SharedPrefManager.getInstance(this).getChatroom();
//            for(Chatroom chatroom : chatroomListTemp){
//                if(chatroom.getCompanyId()==SharedPrefManager.getInstance(this).getUser().getCompanyId()){
//                    chatroomList.add(chatroom);
//                }
//            }
//            chatListAdapter.notifyDataSetChanged();
//        }

        root.addValueEventListener(chatroomListener); //listener untuk data chat di firebase

        srcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setVisibility(View.VISIBLE);
                srcBtn.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });


    }

    //listener ke firebase realtime database untuk chat
    //akan masuk kesini tiap kali ada value change atau perubahan data pada firebase
    ValueEventListener chatroomListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            listromm = new ArrayList<>();
            Chatroom roomchat = null;
            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                String key = ds.getKey();
                int companyBuyer = Integer.parseInt(ds.child("company_id_seller").getValue().toString());
                long timestamp = Long.parseLong(ds.child("last_timestamp").getValue().toString());
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp); //timestamp chat
                String lastMessage="";
                int count_message = 0;
                if (Integer.parseInt(ds.child("company_id_buyer").getValue().toString())==SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()){
                    for (DataSnapshot chat : ds.child("message").getChildren()){
                        lastMessage = chat.child("contain").getValue().toString();
                        if (Integer.parseInt(chat.child("sender").getValue().toString()) != SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() && Boolean.valueOf(chat.child("read").getValue().toString())==false){
                            count_message += 1;
                        }
                    }
                    Log.d("ido", key);
                    Log.d("ido", "companyIdBuyer: "+companyBuyer);
                    Log.d("ido", "lasttime: "+timestamp);
                    Log.d("ido", "lastMessage: "+lastMessage);
                    Log.d("ido", "lastMessage: "+count_message);
                    roomchat = new Chatroom(key, companyBuyer, "", calendar.getTime(), lastMessage, count_message);
                    listromm.add(roomchat);
                }
            }
            for (int i=0; i<listromm.size(); i++){
                Log.d("ido", "cek data: "+listromm.get(i).getRoomId());
            }
            getCompanyName(listromm);
            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(AllChatsActivity.this);
            recyclerView.setLayoutManager(layoutManager1);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            chatListAdapter = new ChatListAdapter(AllChatsActivity.this, listromm);
            recyclerView.setAdapter(chatListAdapter);

            Log.d("ido", "onCreate: "+listromm.size());
            if (listromm.size()==0){
                final Dialog dialog = new Dialog(AllChatsActivity.this);
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

                title.setText("Chat");
                description.setText("Belum ada chat dengan distributor manapun");
                image.setImageResource(R.drawable.ic_chat_black_24dp);

                //jika setuju lanjut ke request
                btnSetuju.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }else{
                Log.d("ido", "cek list: ada");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d("ido", databaseError.getMessage()); //Don't ignore errors!
        }
    };

    /**Method untuk mengambil nama perusahaan
     * Nama perusahaan hrs selalu ambil dr Postgre dan tidak diwrite ke firebase karena bisa saja terjadi perubahan nama perusahaan*/
    private void requestCompanyName(final ArrayList<Chatroom> chatroomList){
        try {
            //build query untuk mengambil nama perusahaan
            StringBuilder query = new StringBuilder();
            query.append("SELECT id, nama_perusahaan from gcm_master_company where ");
            Log.d("size sblmbgt", "requestCompanyName: "+chatroomList.size());
            for(int i = 0; i< chatroomList.size(); i++){
                query.append("id="+ chatroomList.get(i).getCompanyId()+" ");
                if(i< chatroomList.size()-1){
                    query.append("or ");
                }
                else {
                    query.append(";");
                }
            }
            Log.d("", "requestCompanyName: "+query);
            //mulai lakukan request
            Call<JsonObject> companyCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query.toString())));

            companyCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if(status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            //proses memasukkan nama perusahaan ke list yg sebelumnya telah dibuat (sebelumnya companyName masih string kosong)
                            for(int i = 0; i< chatroomList.size(); i++){
                                for(int j=0; j<jsonArray.size(); j++){
                                    if(jsonArray.get(j).getAsJsonObject().get("id").getAsInt()== chatroomList.get(i).getCompanyId()){
                                        chatroomList.get(i).setCompanyName(jsonArray.get(j).getAsJsonObject().get("nama_perusahaan").getAsString());
                                    }
                                }
                            }
                            SharedPrefManager.getInstance(AllChatsActivity.this).saveChatroom(chatroomList); //save chatroom ke local storage
                            chatListAdapter.notifyDataSetChanged();
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

    private void getKey(){
        Log.d("ido", "masuk kesini dong: ");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listromm = new ArrayList<>();
                Chatroom roomchat = null;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String key = ds.getKey();
                    int companyBuyer = Integer.parseInt(ds.child("company_id_seller").getValue().toString());
                    long timestamp = Long.parseLong(ds.child("last_timestamp").getValue().toString());
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp); //timestamp chat
                    String lastMessage="";
                    int count_message = 0;
                    if (Integer.parseInt(ds.child("company_id_buyer").getValue().toString())==SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()){
                        for (DataSnapshot chat : ds.child("message").getChildren()){
                            lastMessage = chat.child("contain").getValue().toString();
                            if (Integer.parseInt(chat.child("sender").getValue().toString()) != SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId() && Boolean.valueOf(chat.child("read").getValue().toString())==false){
                                count_message += 1;
                            }
                        }
                        Log.d("ido", key);
                        Log.d("ido", "companyIdBuyer: "+companyBuyer);
                        Log.d("ido", "lasttime: "+timestamp);
                        Log.d("ido", "lastMessage: "+lastMessage);
                        Log.d("ido", "lastMessage: "+count_message);
                        roomchat = new Chatroom(key, companyBuyer, "", calendar.getTime(), lastMessage, count_message);
                        listromm.add(roomchat);
                    }
                }
                for (int i=0; i<listromm.size(); i++){
                    Log.d("ido", "cek data: "+listromm.get(i).getRoomId());
                }
                getCompanyName(listromm);
                RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(AllChatsActivity.this);
                recyclerView.setLayoutManager(layoutManager1);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                chatListAdapter = new ChatListAdapter(AllChatsActivity.this, listromm);
                recyclerView.setAdapter(chatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ido", databaseError.getMessage()); //Don't ignore errors!
            }
        };
        root.addListenerForSingleValueEvent(valueEventListener);
    }

    private void getCompanyName(final ArrayList<Chatroom> listromm){
        String query = "select id, nama_perusahaan from gcm_master_company where ";
        String loop = "";
        int count=0;
        for (int i=0; i<listromm.size(); i++){
            loop = loop + "id = "+listromm.get(i).getCompanyId()+"";
            if (count < listromm.size()-1){
                loop = loop.concat(" or ");
            }else{
                loop = loop.concat(";");
            }
            count++;
        }
        String queryGetPerusahaan = query+loop;
        Log.d("ido", "query get company: "+queryGetPerusahaan);
        try {
            Call<JsonObject> callGetCompanyName = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(queryGetPerusahaan)));
            callGetCompanyName.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        if (status.equals("success")){
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            for(int i = 0; i< listromm.size(); i++){
                                for(int j=0; j<jsonArray.size(); j++){
                                    if(jsonArray.get(j).getAsJsonObject().get("id").getAsInt()== listromm.get(i).getCompanyId()){
                                        listromm.get(i).setCompanyName(jsonArray.get(j).getAsJsonObject().get("nama_perusahaan").getAsString());
                                    }
                                }
                            }
                            chatListAdapter.notifyDataSetChanged();
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
}
