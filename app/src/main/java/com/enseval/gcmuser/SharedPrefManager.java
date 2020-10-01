package com.enseval.gcmuser;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.enseval.gcmuser.Model.Chatroom;
import com.enseval.gcmuser.Model.Chats;
import com.enseval.gcmuser.Model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharedPrefManager {

    private static final String SHARED_PREF_USER = "user_login"; //tag untuk penyimpanan info user
    private static final String SHARED_PREF_ACTIVE_SELLER = "active_seller"; //tag untuk penyimpanan info user
    private static final String SHARED_PREF_CHATROOM = "chatroom"; //tag untuk penyimpanan data chatroom
    private static final String SHARED_PREF_CHAT_MESSAGE = "chat_message"; //tag untuk penyimpanan data isi chat
    private static final String SHARED_PREF_TOKEN = "token"; //tag untuk penyimpanan data isi chat

    private static SharedPrefManager mInstance;
    private Context mCtx;

    private SharedPrefManager(Context mCtx) {
        this.mCtx = mCtx;
    }

    public static synchronized SharedPrefManager getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(mCtx);
        }
        return mInstance;

    }

    /**Method untuk save informasi user*/
    public void saveUser(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", user.getUserId());
        editor.putInt("companyId", user.getCompanyId());
        editor.putInt("tipeBisnis", user.getTipeBisnis());
        editor.apply();
    }

    public void saveToken(String userToken){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userToken", userToken);
        editor.apply();
    }

    public String getToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_TOKEN, Context.MODE_PRIVATE);
        return sharedPreferences.getString("userToken", "");
    }

    public void clearToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**Method untuk mengecek apakah sudah login atau belum*/
    public boolean isLoggedin() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1) != -1;
    }

    /**Method untuk mendapatkan info user*/
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE);
        User user = new User(
                sharedPreferences.getInt("userId", -1),
                sharedPreferences.getInt("companyId", -1),
                sharedPreferences.getInt("tipeBisnis", -1)
        );
        return user;
    }

    /**Menghapus info user dari local*/
    public void clearUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /** Method untuk save informasi user*/
    public void saveActiveSeller(String strseller) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_ACTIVE_SELLER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("active_seller", strseller);
        editor.apply();
    }

    /**Method untuk mendapatkan info user*/
    public String getActiveSeller() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_ACTIVE_SELLER, Context.MODE_PRIVATE);
        return sharedPreferences.getString("active_seller", "");
    }

    /**Menghapus info user dari local*/
    public void clearActiveSeller() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_ACTIVE_SELLER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

//    public void saveChatroom(Chatroom chatroom){
//        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHATROOM, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("chatroom", new Gson().toJson(chatroom));
//        editor.apply();
//    }

    /**Method untuk menyimpan chatroom*/
    public void saveChatroom(ArrayList<Chatroom> chatroomList){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHATROOM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("chatroom", new Gson().toJson(chatroomList));
        editor.apply();
    }

    /**Method untuk menambahkan chatroom ke penyimpanan*/
    public void saveNewChatroom(Chatroom chatroom){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHATROOM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<Chatroom> chatroomList = getChatroom();
        chatroomList.add(chatroom);
        editor.putString("chatroom", new Gson().toJson(chatroomList));
        editor.apply();
    }

    /**Method untuk mendapatkan data seluruh chatroom*/
    public ArrayList<Chatroom> getChatroom(){
        ArrayList<Chatroom> chatroomList;
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHATROOM, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("chatroom", "");
        if(json.isEmpty()){
            chatroomList = new ArrayList<>();
            chatroomList.clear();
        }
        else {
            Type type = new TypeToken<ArrayList<Chatroom>>(){}.getType();
            chatroomList = new Gson().fromJson(json, type);
        }
        return chatroomList;
    }

    /**Method untuk mendapatkan data chatroom dengan parameter companyid nya*/
    public Chatroom getChatroom(int companyId){
        ArrayList<Chatroom> chatroomList;
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHATROOM, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("chatroom", "");
        if(json.isEmpty()){
            return null;
        }
        else {
            Type type = new TypeToken<ArrayList<Chatroom>>(){}.getType();
            chatroomList = new Gson().fromJson(json, type);
        }
        for(int i=0; i<chatroomList.size(); i++){
            if(companyId==chatroomList.get(i).getCompanyId()){
                return chatroomList.get(i);
            }
        }
        return null;
    }

    /**Method untuk mengecek apakah ada chatroom yang disimpan*/
    public boolean isChatroomSaved(){
        ArrayList<Chatroom> chatroomList = getChatroom();
        if(chatroomList.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    /**Method untuk menyimpan bubble chat baru*/
    public void saveNewMessage(Chats chats){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHAT_MESSAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        ArrayList<Chats> chatList = getChatMessages(chats.getChatroom());
        chatList.add(chats);

        editor.putString("chat", new Gson().toJson(chatList));
        editor.apply();
    }

    /**Method untuk menyimpan seluruh isi bubble chat suatu chatroom*/
    public void saveChatMessages(ArrayList<Chats> chatMessages, String chatroom){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHAT_MESSAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(chatroom, new Gson().toJson(chatMessages));
        editor.apply();
    }

//    public ArrayList<Chats> getChatMessages(){
//        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHAT_MESSAGE, Context.MODE_PRIVATE);
//        ArrayList<Chats> chatMessages = null;
//        String json = sharedPreferences.getString("chat", "");
//        if (!json.isEmpty()) {
//            Type type = new TypeToken<ArrayList<Chats>>(){}.getType();
//            chatMessages = new Gson().fromJson(json, type);
//        }
//        Log.d("chat messages size", "getChatMessages: "+chatMessages.size());
//        return chatMessages;
//    }

    /**Method untuk mendapatkan data seluruh bubble chat suatu chatroom*/
    public ArrayList<Chats> getChatMessages(String chatroom){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHAT_MESSAGE, Context.MODE_PRIVATE);
        ArrayList<Chats> chatMessages;
        String json = sharedPreferences.getString(chatroom, "");
        chatMessages = new ArrayList<>();
        if (!json.isEmpty()) {
            Log.d("", "getChatMessages: masuk ga");
            Type type = new TypeToken<ArrayList<Chats>>(){}.getType();
            ArrayList<Chats> chatMessagesTemp = new Gson().fromJson(json, type);
            Log.d("temp size", "getChatMessages: "+chatMessagesTemp.size());
            for(int i=0; i<chatMessagesTemp.size(); i++){
//                Log.d("nama chatroom", "getChatMessages: "+chatroom);
//                Log.d("chatroom di array", "getChatMessages: "+chatMessagesTemp.get(i).getChatroom());
//                if(chatMessagesTemp.get(i).getChatroom().equals(chatroom)){
//                    Log.d("", "getChatMessages: apakah masuk");
//                    chatMessages.add(chatMessagesTemp.get(i));
//                    Log.d("Size setelah tambah", "getChatMessages: "+chatMessages.size());
//                }
                Log.d("", "getChatMessages: apakah masuk");
                chatMessages.add(chatMessagesTemp.get(i));
                Log.d("Size setelah tambah", "getChatMessages: "+chatMessages.size());
            }
        }
        Log.d("size sblm dibalik", "getChatMessages: "+chatMessages.size());
        return chatMessages;
    }

    /**Method untuk mengecek suatu chatroom spesifik apakah sudah disimpan atau belum*/
    public boolean isChatMessagesSaved(String chatroom){
        ArrayList<Chats> chatList = getChatMessages(chatroom);
//        Log.d("", "isChatMessagesSaved: "+getChatMessages(chatroom).get(0));
        if(chatList.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    /**Method untuk menghapus isi chat suatu chatroom*/
    public void clearMessages(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHAT_MESSAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void clearRoomMessages(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHATROOM, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

//    public int getChatMessagesCount(String chatroom){
//        int count =0;
//        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CHAT_MESSAGE, Context.MODE_PRIVATE);
//        ArrayList<Chats> chatMessages;
//        String json = sharedPreferences.getString("chat", "");
//        chatMessages = new ArrayList<>();
//        if (!json.isEmpty()) {
//            Type type = new TypeToken<ArrayList<Chats>>(){}.getType();
//            ArrayList<Chats> chatMessagesTemp = new Gson().fromJson(json, type);
//            for(int i=0; i<chatMessagesTemp.size(); i++){
//                if(chatMessagesTemp.get(i).getChatroom().equals(chatroom)){
//                    count++;
//                }
//            }
//        }
//        return count;
//    }
}

