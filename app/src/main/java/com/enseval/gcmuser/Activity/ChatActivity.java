package com.enseval.gcmuser.Activity;

import android.arch.persistence.room.Database;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Adapter.ChatBubbleAdapter;
import com.enseval.gcmuser.Model.Chatroom;
import com.enseval.gcmuser.Model.Chats;
import com.enseval.gcmuser.Model.SalesId;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private EditText etChat;
    private ImageView send, back, more;
    private ArrayList<Chats> chatsList;
    private DatabaseReference message;
    private ChatBubbleAdapter chatBubbleAdapter;
    private TextView companyName;
    private long lastClickTime = 0;
    private String chatroom;
    private Chats chat;
    private int companyId;
    private int companyBarang;
    private final int FROM_BARANG = 100;
    private final int FROM_CHATROOM = 200;
    private  DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    private DatabaseReference room;
    private LinearLayout linearPreview;
    private View dividerPreview;
    private ImageView imgPreview;
    private TextView textPreview, harga;
    private String from;
    private ArrayList<SalesId> idSales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvChat = findViewById(R.id.rvChat);
        etChat = findViewById(R.id.etChat);
        send = findViewById(R.id.send);
        companyName = findViewById(R.id.companyName);
        back = findViewById(R.id.backBtn);
        more = findViewById(R.id.moreBtn);
        linearPreview = findViewById(R.id.linearPreview);
        dividerPreview = findViewById(R.id.divideratas);
        imgPreview = findViewById(R.id.imgPreview);
        textPreview = findViewById(R.id.namaPreview);
        harga = findViewById(R.id.harga);

        linearPreview.setVisibility(View.GONE);
        dividerPreview.setVisibility(View.GONE);
        harga.setVisibility(View.GONE);

        chatsList = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(ChatActivity.this);
        rvChat.setLayoutManager(layoutManager1);
        rvChat.setItemAnimator(new DefaultItemAnimator());
        chatBubbleAdapter = new ChatBubbleAdapter(ChatActivity.this, chatsList);
        rvChat.setAdapter(chatBubbleAdapter);

        from = getIntent().getStringExtra("from"); //ambil sumber intent

        //jika dari barang, tampilkan preview barang
        if(from.equals("barang")){
            linearPreview.setVisibility(View.VISIBLE);
            dividerPreview.setVisibility(View.VISIBLE);

            Glide.with(getApplicationContext())
                    .load(getIntent().getStringExtra("img"))
                    .fallback(R.color.bg)
                    .error(R.color.bg)
                    .into(imgPreview);
            textPreview.setText(getIntent().getStringExtra("nama"));
            companyId = getIntent().getIntExtra("companyId",-1);
            requestCompanyName(companyId);
            getIdSeller(companyId);
        }
        //jika dari chatlist, tidak ada preview
        else if(from.equals("chatlist")){
            chatroom = getIntent().getStringExtra("chatroom");
            companyName.setText(getIntent().getStringExtra("companyName"));
            message = FirebaseDatabase.getInstance().getReference().getRoot().child(chatroom).child("message");
            checkSaved();
        }
        //jika dari nego, tampilkan preview negosiasi
        else if(from.equals("nego")){
            linearPreview.setVisibility(View.VISIBLE);
            dividerPreview.setVisibility(View.VISIBLE);
            harga.setVisibility(View.VISIBLE);
            harga.setText(Currency.getCurrencyFormat().format(getIntent().getIntExtra("harga_terakhir",0)));
            Glide.with(getApplicationContext())
                    .load(getIntent().getStringExtra("img"))
                    .fallback(R.color.bg)
                    .error(R.color.bg)
                    .into(imgPreview);
            textPreview.setText(getIntent().getStringExtra("nama"));
            companyId = getIntent().getIntExtra("companyId",-1);
            getIdSeller(companyId);
        }

        //jika button send ditekan dan edittext tidak kosong, panggil method sendMessage
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etChat.getText().toString().trim().isEmpty()){
                    sendMessage(etChat.getText().toString());
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    finish();
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });

//        getKey();
    }

    /**Method untuk cek apakah chat sudah disimpan di local dengan parameter chatroom*/
    private void checkSaved(){
        if(SharedPrefManager.getInstance(this).isChatMessagesSaved(chatroom)){
            chatsList.clear();
            ArrayList<Chats> chatsListTemp = SharedPrefManager.getInstance(this).getChatMessages(chatroom); //cek apakah isi chat utk chatroom ini sudah pernah disimpan
            //masukin ke list, lalu notify adapter
            for(Chats chats : chatsListTemp){
                chatsList.add(chats);
            }
            chatBubbleAdapter.notifyDataSetChanged();
            rvChat.scrollToPosition(chatBubbleAdapter.getItemCount()-1);
        }
        checkChat(); //ambil isi chat dari firebase
    }

    /**Method untuk cek apakah chatroom sudah disimpan di local dengan parameter companyId*/
    private void checkSavedByCompanyId(final int companyId, final int idsales){
        //kondisi jika chatroomnya ada
        if(SharedPrefManager.getInstance(this).getChatroom(companyId)!=null){
            Log.d("ido", "checkSavedByCompanyId: ADA DI LOKAL");
            chatroom = SharedPrefManager.getInstance(this).getChatroom(companyId).getRoomId(); //ambil room idnya
            ArrayList<Chats> chatsListTemp = SharedPrefManager.getInstance(this).getChatMessages(chatroom);
            chatsList.clear();
            //masukkin ke list laly notify adapter
            for(Chats chats : chatsListTemp){
                chatsList.add(chats);
            }
            chatBubbleAdapter.notifyDataSetChanged();
            rvChat.scrollToPosition(chatBubbleAdapter.getItemCount()-1);
            message = FirebaseDatabase.getInstance().getReference().getRoot().child(chatroom).child("message");
        }
        //kalo chatroomnya belum ada, langsung ke database
        else{
            Log.d("ido", "checkSavedByCompanyId: NGGA ADA DI LOKAL");
            root.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean ada=false;
                    //ambil chatroom dengan yang company id nya sesuai dengan company id lawan bicara user
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                        if(Integer.parseInt(snapshot.child("user1").child("company_id").getValue().toString())==SharedPrefManager.getInstance(ChatActivity.this).getUser().getCompanyId()){
//                            ada = true;
//                        }
//                        else if(Integer.parseInt(snapshot.child("user2").child("company_id").getValue().toString())==SharedPrefManager.getInstance(ChatActivity.this).getUser().getCompanyId()){
//                            ada = true;
//                        }
                        Log.d("ido", "cek : "+snapshot.child("company_id_buyer").getValue().toString()+"="+SharedPrefManager.getInstance(ChatActivity.this).getUser().getCompanyId()+", "+snapshot.child("company_id_seller").getValue().toString()+"="+companyId);
                        if (Integer.parseInt(snapshot.child("company_id_buyer").getValue().toString())==SharedPrefManager.getInstance(ChatActivity.this).getUser().getCompanyId() &&
                                Integer.parseInt(snapshot.child("company_id_seller").getValue().toString())==companyId){
                            ada = true;
                            chatroom = snapshot.getKey();
                            message = FirebaseDatabase.getInstance().getReference().getRoot().child(chatroom).child("message");
                            companyBarang = companyId;
                        }
                    }
                    //jika belum ada, buat chatroom baru
                    if(!ada){
                        root.removeEventListener(this);
                        createNewChatroom(idsales);
                        Log.d("ido", "NGGA ADA JADI BUAT BARU");
                    }
                    //jika sudah ada, masuk ke checkchat untuk mabil data-datanya
                    else {
                        Log.d("ido", "ADA JADI CEK CHATNYA");
                        root.removeEventListener(this);
                        checkChat();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    /**Method untuk membuat chatroom baru di firebase*/
    private void createNewChatroom(int idseller){
        String key = root.push().getKey();
        Map<String, Object> map = new HashMap<String, Object>();
        root.updateChildren(map);

        room = root.child(key);
        chatroom = key;
        Map<String,Object> map1 = new HashMap<String, Object>();
        map1.put("message","");
        map1.put("type", "user_to_sales");
        map1.put("company_id_buyer", SharedPrefManager.getInstance(this).getUser().getCompanyId());
        map1.put("user_id_buyer", SharedPrefManager.getInstance(this).getUser().getUserId());
        map1.put("company_id_seller", companyId);
        map1.put("user_id_seller", idseller);
        map1.put("last_timestamp", Calendar.getInstance().getTimeInMillis());

//        Map<String,Object> map2 = new HashMap<String, Object>();
//        map2.put("company_id", SharedPrefManager.getInstance(this).getUser().getCompanyId());
//        map2.put("user_id", SharedPrefManager.getInstance(this).getUser().getUserId());
//
//        Map<String,Object> map3 = new HashMap<String, Object>();
//        map3.put("company_id", companyId);
//        map3.put("user_id", idseller);
//
//        map1.put("user1",map2);
//        map1.put("user2",map3);

        room.updateChildren(map1); //masukkin ke firebae

        message = room.child("message");

        SharedPrefManager.getInstance(ChatActivity.this).saveNewChatroom(new Chatroom(chatroom, companyId, companyName.getText().toString(),true)); //simpan di local storage

        checkChat(); //ambil data-data terbaru dr firebase
        requestCompanyName(companyId); //request nama perusahaan dari chatroom yg baru dibuat ini
    }

    /**Method utk ambil data dari firebase berdasarkan perubahan value*/
    private void checkChat(){
        message.addValueEventListener(checkChatListener);
    }

    //listener untuk perubahan data chat di firebase
    ValueEventListener checkChatListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            chatsList.clear();
            //looping utk semua bubble chat
            for(DataSnapshot bubble : dataSnapshot.getChildren()){
                //kondisi jika benar user merupakan pengirim atau penerima
                if(Integer.parseInt(bubble.child("receiver").getValue().toString())==SharedPrefManager.getInstance(ChatActivity.this).getUser().getCompanyId() ||
                        Integer.parseInt(bubble.child("sender").getValue().toString())==SharedPrefManager.getInstance(ChatActivity.this).getUser().getCompanyId()){
                    //pengecekan status apakah sudah dibaca atau belum
                    if (((Boolean) bubble.child("read").getValue()).booleanValue()==false && Integer.parseInt(bubble.child("sender").getValue().toString())!=SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()) {
                        DatabaseReference read = message.child(bubble.getKey()).child("read");
                        read.setValue(true);
                        Log.d("ido", "chatRead: " + read);
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis((Long) bubble.child("timestamp").child("time").getValue()); //ambil timestamp

                    //masukkin data ke list sesuai tipenya masing-masing krn cosntructor modelnya beda
                    if(bubble.child("type").getValue().toString().equals("text")){
                        chatsList.add(new Chats(
                                chatroom,
                                bubble.getKey().toString(),
                                bubble.child("contain").getValue().toString(),
                                Integer.parseInt(bubble.child("sender").getValue().toString()),
                                ((Boolean) bubble.child("read").getValue()).booleanValue(),
//                                calendar.getTime()
                                calendar.getTime(),
                                bubble.child("type").getValue().toString()
                        ));
                    }
                    else if(bubble.child("type").getValue().toString().equals("barang")){
                        Log.d("ido", "cek Chatroom: "+chatroom);
                        chatsList.add(new Chats(
                                chatroom,
                                bubble.getKey().toString(),
                                bubble.child("contain").getValue().toString(),
                                Integer.parseInt(bubble.child("sender").getValue().toString()),
                                ((Boolean) bubble.child("read").getValue()).booleanValue(),
//                                calendar.getTime()
                                calendar.getTime(),
                                bubble.child("type").getValue().toString(),
                                Integer.parseInt(bubble.child("barang_id").getValue().toString())
                        ));
                    }
                    else if(bubble.child("type").getValue().toString().equals("nego")){
                        chatsList.add(new Chats(
                                chatroom,
                                bubble.getKey().toString(),
                                bubble.child("contain").getValue().toString(),
                                Integer.parseInt(bubble.child("sender").getValue().toString()),
                                ((Boolean) bubble.child("read").getValue()).booleanValue(),
//                                calendar.getTime()
                                calendar.getTime(),
                                bubble.child("type").getValue().toString(),
                                Integer.parseInt(bubble.child("barang_id").getValue().toString()),
                                Integer.parseInt(bubble.child("harga_terakhir").getValue().toString())
                        ));
                    }
                }
            }
            SharedPrefManager.getInstance(ChatActivity.this).saveChatMessages(chatsList, chatroom); //save isi chat terbaru ke local
            Log.d("ido", "SAVE DATANYA DI LOKAL: ");
            //ambil lg database dr local utk di show ke adapter
            ArrayList<Chats> chatsListTemp = SharedPrefManager.getInstance(ChatActivity.this).getChatMessages(chatroom);
            chatsList.clear();
            for(int i=0; i<chatsListTemp.size(); i++){
                chatsList.add(chatsListTemp.get(i));
            }
            Log.d("ido", "chats list size: "+chatsList.size());
            chatBubbleAdapter.notifyDataSetChanged();
            rvChat.scrollToPosition(chatBubbleAdapter.getItemCount()-1);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    /**Method untuk request nama perusahaan lawan bicara*/
    private void requestCompanyName(final int companyId){
        Log.d("ido", "requestCompanyName: kesini lahh");
        try {
            Call<JsonObject> companyCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("select nama_perusahaan from gcm_master_company where id="+companyId+";")));

            companyCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if(response.isSuccessful()){
                        Log.d("ido", "onResponse: "+response.body().getAsJsonObject().get("status"));
                        if(response.body().getAsJsonObject().get("status").getAsString().equals("success")){
                            companyName.setText(response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("nama_perusahaan").getAsString());
                            if(getIntent().getStringExtra("chatroom")==null && getIntent().getIntExtra("companyId", -1)!=-1){
                                getIdSeller(companyId);
                            }
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

    /**Method untuk mengirimkan pesan yg diketik*/
    private void sendMessage(String contain){
        String key = message.push().getKey();
        Map<String, Object> map = new HashMap<String, Object>();
        message.updateChildren(map);


        String keyroom = "";

        DatabaseReference latestBubble = message.child(key);
        Map<String,Object> map1 = new HashMap<String, Object>();

        map1.put("timestamp", Calendar.getInstance().getTime());
        map1.put("contain", contain);
        map1.put("read", false);
        map1.put("sender", SharedPrefManager.getInstance(this).getUser().getCompanyId());
        map1.put("uid", key);

        if(from.equals("barang")){
            Log.d("ido", "sendMessage: "+chatroom);
            map1.put("type", "barang");
            map1.put("barang_id", getIntent().getIntExtra("id",-1));
            map1.put("receiver", companyId);
            keyroom = chatroom;
            DatabaseReference rommChat = root.child(keyroom);
            Log.d("ido", "sendMessage: "+keyroom);
            Map<String, Object> map2 = new HashMap<String, Object>();
            map2.put("last_timestamp", Calendar.getInstance().getTimeInMillis());
            rommChat.updateChildren(map2);
        }
        else if(from.equals("nego")){
            map1.put("type", "nego");
            map1.put("barang_id", getIntent().getIntExtra("id",-1));
            map1.put("harga_terakhir", getIntent().getIntExtra("harga_terakhir", 0));
        }
        else if(from.equals("chatlist")){
            int comapny_id = getIntent().getIntExtra("company",-1);
            keyroom = chatroom;
            DatabaseReference rommChat = root.child(keyroom);
            Log.d("ido", "sendMessage: "+keyroom);
            Map<String, Object> map2 = new HashMap<String, Object>();
            map2.put("last_timestamp", Calendar.getInstance().getTimeInMillis());
            rommChat.updateChildren(map2);
            map1.put("type", "text");
            map1.put("receiver", comapny_id);
            Log.d("ido", "sendMessage: "+comapny_id);
        }

        latestBubble.updateChildren(map1); //kirim ke firebase
        etChat.getText().clear(); //kosongin lagi edittextnya

        checkChat(); //cek ke listener lagi jika ada perubahan data
    }

    @Override
    protected void onStop() {
        super.onStop();
        message.removeEventListener(checkChatListener); //kalo activity berhenti, listener distop agar tidak jalan terus
    }

    private void getIdSeller(final int companyid){
        String query = "select distinct id, id_sales from gcm_company_listing_sales where buyer_id = "+SharedPrefManager.getInstance(getApplicationContext()).getUser().getCompanyId()+" and seller_id = "+companyid+";";
        String query1 = "select distinct id, id_sales from gcm_company_listing_sales " +
                "where buyer_id = "+SharedPrefManager.getInstance(this).getUser().getCompanyId()+" and seller_id = "+companyid+"";
        Log.d("ido", "getIdSeller: "+query);
        try {
            Call<JsonObject> callGetIdSeller = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            callGetIdSeller.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()){
                        Log.d("ido", "kalo ga kesini gagal");
                        String status = response.body().getAsJsonObject().get("status").getAsString();
                        Log.d("ido", "cek status: "+status);
                        if (status.equals("success")){
                            Log.d("ido", "masuk kesini lohhh");
                            idSales = new ArrayList<>();
                            JsonArray jsonArray = response.body().getAsJsonObject().get("data").getAsJsonArray();
                            int idseller = jsonArray.get(0).getAsJsonObject().get("id_sales").getAsInt();
//                            for (int i=0; i<jsonArray.size(); i++){
//                                int id = jsonArray.get(i).getAsJsonObject().get("id").getAsInt();
//                                idseller = jsonArray.get(i).getAsJsonObject().get("id_sales").getAsInt();
//                                idSales.add(new SalesId(id, idseller));
//                            }
                            checkSavedByCompanyId(companyid, idseller);
                            Log.d("ido", "jumlah sales: "+idseller);
                        }else{
                            Log.d("ido", "Gagal sukses");
                        }
                    }else{
                        Log.d("ido", "bubar aja dah");
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });
        }catch (Exception e){

        }
    }

    private void getKey(){
//        Log.d("ido", "masuk kesini dong: ");
//        final Query query = root.orderByKey().limitToFirst(1);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot ds : dataSnapshot.getChildren()) {
//                    Query query1 = FirebaseDatabase.getInstance().getReference().child("message").limitToFirst(5);
//                    query1.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            for (DataSnapshot chat : ds.child("message").getChildren()){
//                                Log.d("ido", chat.child("contain").getValue().toString());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//

//        Log.d("ido", "masuk kesini dong: ");
//        room = root.getRoot();
//        ValueEventListener valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot ds : dataSnapshot.getChildren()) {
//                    String key = ds.getKey();
//                    Log.d("ido", key);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("ido", databaseError.getMessage()); //Don't ignore errors!
//            }
//        };
//        room.addListenerForSingleValueEvent(valueEventListener);
    }
}
