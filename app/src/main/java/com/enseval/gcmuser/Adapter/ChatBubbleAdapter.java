package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Model.Chats;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.R;
import com.enseval.gcmuser.SharedPrefManager;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBubbleAdapter extends RecyclerView.Adapter<ChatBubbleAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Chats> chatsList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout sent, received, preview;
        private TextView textSent, textReceived, timeReceived, timeSent, date, read, textOnPreview, namaBarang, readPreview, timeSentPreview, harga;
        private ImageView imgBarang;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sent = itemView.findViewById(R.id.linearSent);
            received = itemView.findViewById(R.id.linearReceived);
            textSent = itemView.findViewById(R.id.textSent);
            textReceived = itemView.findViewById(R.id.textReceived);
            timeReceived = itemView.findViewById(R.id.timeReceived);
            timeSent = itemView.findViewById(R.id.timeSent);
            date = itemView.findViewById(R.id.date);
            read = itemView.findViewById(R.id.read);
            textOnPreview = itemView.findViewById(R.id.textAboutPreview);
            namaBarang = itemView.findViewById(R.id.namaPreview);
            readPreview = itemView.findViewById(R.id.readPreview);
            timeSentPreview = itemView.findViewById(R.id.timeSentPreview);
            imgBarang = itemView.findViewById(R.id.imgPreview);
            preview = itemView.findViewById(R.id.linearPreview);
            harga = itemView.findViewById(R.id.harga);
        }
    }

    public ChatBubbleAdapter(Context _context, ArrayList<Chats> chatsList) {
        this._context = _context;
        this.chatsList = chatsList;
    }


    @NonNull
    @Override
    public ChatBubbleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.chat_bubble_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatBubbleAdapter.ViewHolder holder, final int position) {
        final Chats bubble = chatsList.get(position);

        holder.read.setVisibility(View.GONE);
        holder.harga.setVisibility(View.GONE);

        //jika posisi bukan posisi awal
        if(position>0){
            Chats prevBubble = chatsList.get(position-1);
            //pengecekan apabila range harinya masih sama dengan bubble sebelumnya maka tidak usah menampilkan tanggal dan bulannya lagi
            if(new SimpleDateFormat("dd/MM/yyyy").format(bubble.getTimestamp()).equals(new SimpleDateFormat("dd/MM/yyyy").format(prevBubble.getTimestamp()))){
                holder.date.setVisibility(View.GONE);
            }
            //jika berbeda, maka ditulis keterangan waktunya
            else{
                holder.date.setVisibility(View.VISIBLE);
                //jika tahun bulan dan tanggal sama dengan hari ini, tulis hari ini
                if(Calendar.getInstance().get(Calendar.YEAR)==Integer.parseInt(new SimpleDateFormat("yyyy").format(bubble.getTimestamp())) &&
                        Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(bubble.getTimestamp())) &&
                        Calendar.getInstance().get(Calendar.DATE)==Integer.parseInt(new SimpleDateFormat("dd").format(bubble.getTimestamp()))){
                    holder.date.setText("Hari ini");
                }
                //jika kemarin, tulis kemarin
                else if(Calendar.getInstance().get(Calendar.YEAR)==Integer.parseInt(new SimpleDateFormat("yyyy").format(bubble.getTimestamp())) &&
                        Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(bubble.getTimestamp())) &&
                        Calendar.getInstance().get(Calendar.DATE)-1==Integer.parseInt(new SimpleDateFormat("dd").format(bubble.getTimestamp()))){
                    holder.date.setText("Kemarin");
                }
                //selain itu, tulis lengkap tanggal bulan tahunnya
                else{
                    holder.date.setText(new SimpleDateFormat("MMMM dd, yyyy").format(bubble.getTimestamp()));
                }
            }
        }
        //jika posisi awal langsung tulis keterangan waktunya
        else{
            holder.date.setVisibility(View.VISIBLE);
            if(Calendar.getInstance().get(Calendar.YEAR)==Integer.parseInt(new SimpleDateFormat("yyyy").format(bubble.getTimestamp())) &&
                    Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(bubble.getTimestamp())) &&
                    Calendar.getInstance().get(Calendar.DATE)==Integer.parseInt(new SimpleDateFormat("dd").format(bubble.getTimestamp()))){
                holder.date.setText("Hari ini");
            }
            else if(Calendar.getInstance().get(Calendar.YEAR)==Integer.parseInt(new SimpleDateFormat("yyyy").format(bubble.getTimestamp())) &&
                    Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(bubble.getTimestamp())) &&
                    Calendar.getInstance().get(Calendar.DATE)-1==Integer.parseInt(new SimpleDateFormat("dd").format(bubble.getTimestamp()))){
                holder.date.setText("Kemarin");
            }
            else{
                holder.date.setText(new SimpleDateFormat("MMMM dd, yyyy").format(bubble.getTimestamp()));
            }
        }

        holder.received.setVisibility(View.GONE);
        holder.sent.setVisibility(View.GONE);
        holder.preview.setVisibility(View.GONE);

        //tampilan bubble jika tipenya text
        if(bubble.getTipe().equals("text")){
            //jika
            if(bubble.getUserId()== SharedPrefManager.getInstance(_context).getUser().getCompanyId()){
                holder.sent.setVisibility(View.VISIBLE);
                holder.textSent.setText(bubble.getMessage());
                holder.timeSent.setText(new SimpleDateFormat("HH:mm").format(bubble.getTimestamp()));
                if(bubble.isRead()){
                    holder.read.setVisibility(View.VISIBLE);
                }
                else {
                    holder.read.setVisibility(View.GONE);
                }
            }
            else{
                holder.received.setVisibility(View.VISIBLE);
                holder.textReceived.setText(bubble.getMessage());
                holder.timeReceived.setText(new SimpleDateFormat("HH:mm").format(bubble.getTimestamp()));
            }
        }
        else if(bubble.getTipe().equals("barang")){
            requestBarang(holder, position, bubble.getBarangId());
            holder.preview.setVisibility(View.VISIBLE);
            holder.textOnPreview.setText(bubble.getMessage());
            holder.timeSentPreview.setText(new SimpleDateFormat("HH:mm").format(bubble.getTimestamp()));
            if(bubble.isRead()){
                holder.readPreview.setVisibility(View.VISIBLE);
            }
            else {
                holder.readPreview.setVisibility(View.GONE);
            }
        }
        else if(bubble.getTipe().equals("nego")){
            requestBarang(holder, position, bubble.getBarangId());
            holder.preview.setVisibility(View.VISIBLE);
            holder.harga.setVisibility(View.VISIBLE);
            holder.harga.setText(Currency.getCurrencyFormat().format(bubble.getHargaTerakhir()));
            holder.textOnPreview.setText(bubble.getMessage());
            holder.timeSentPreview.setText(new SimpleDateFormat("HH:mm").format(bubble.getTimestamp()));
            if(bubble.isRead()){
                holder.readPreview.setVisibility(View.VISIBLE);
            }
            else {
                holder.readPreview.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    private void requestBarang(final ChatBubbleAdapter.ViewHolder holder, final int position, int barangId){
        try {
            Call<JsonObject> barangCall = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt("SELECT nama, foto " +
                            "FROM gcm_master_barang a inner join gcm_list_barang b on a.id=b.barang_id where b.id="+barangId+";")));

            barangCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        if(response.body().getAsJsonObject().get("status").getAsString().equals("success")){
                            Glide.with(_context)
                                    .load(response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("foto").getAsString())
                                    .fallback(R.color.bg)
                                    .error(R.color.bg)
                                    .into(holder.imgBarang);

                            holder.namaBarang.setText(response.body().getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("nama").getAsString());
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
}
