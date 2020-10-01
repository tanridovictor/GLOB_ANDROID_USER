package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enseval.gcmuser.Activity.ChatActivity;
import com.enseval.gcmuser.Model.Chatroom;
import com.enseval.gcmuser.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Chatroom> chatroomList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView namaCompany, date, lastMessage, notif;
        private ConstraintLayout constraintLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            namaCompany = itemView.findViewById(R.id.namaCompany);
            date = itemView.findViewById(R.id.tvDate);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            constraintLayout = itemView.findViewById(R.id.constraint);
            notif = itemView.findViewById(R.id.badgenotif);
        }
    }

    public ChatListAdapter(Context _context, ArrayList<Chatroom> chatroomList) {
        this._context = _context;
        this.chatroomList = chatroomList;
    }


    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.list_chat_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListAdapter.ViewHolder holder, final int position) {
        final Chatroom chatroom = chatroomList.get(position);
        holder.namaCompany.setText(String.valueOf(chatroom.getCompanyName()));
        holder.lastMessage.setText(chatroom.getLastMessage());
        if (chatroom.getCount_message_read_false()==0){
            holder.notif.setVisibility(View.GONE);
        }else {
            holder.notif.setText(String.valueOf(chatroom.getCount_message_read_false()));
        }

        if(Calendar.getInstance().get(Calendar.YEAR) == Integer.parseInt(new SimpleDateFormat("yyyy").format(chatroom.getDate()))){
            //kondisi jika harinya adalah hari ini, tulis jam dan menitnya saja
            if(Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(chatroom.getDate())) &&
                    Calendar.getInstance().get(Calendar.DATE) == Integer.parseInt(new SimpleDateFormat("dd").format(chatroom.getDate()))){
                holder.date.setText(new SimpleDateFormat("HH:mm").format(chatroom.getDate()));
            }
            //jika kemarin, tulis kemarin
            else if(Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(chatroom.getDate())) &&
                    Calendar.getInstance().get(Calendar.DATE)-1 == Integer.parseInt(new SimpleDateFormat("dd").format(chatroom.getDate()))){
                holder.date.setText("Kemarin");
            }
            //jika selain itu, tulis tanggal dan bulan
            else{
                holder.date.setText(new SimpleDateFormat("dd/MM/yy").format(chatroom.getDate()));
            }
        }
            //kondisi jika tahun yang sudah lewat, tulis nama tahunnya saja
        else{
            holder.date.setText(new SimpleDateFormat("yyyy").format(chatroom.getDate()));
        }

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_context, ChatActivity.class);
                intent.putExtra("from", "chatlist");
                intent.putExtra("company", chatroom.getCompanyId());
                intent.putExtra("chatroom", chatroom.getRoomId());
//                if(chatroom.isEmpty()){
//                    intent.putExtra("userId", -1);
//                }
//                else{
//                    intent.putExtra("userId", chatroom.getUserId());
//                }
                intent.putExtra("companyName", chatroom.getCompanyName());
                _context.startActivity(intent);
            }
        });

//        holder.namaCompany.setText(chatroom.getCompanyName());
//        //jika chatroom kosong, last message dan date dibiarkan kosong
//        if(chatroom.isEmpty()){
//            holder.lastMessage.setText("");
//            holder.date.setText("");
//        }
//        //kondisi jika chatroom sudah ada isi chatnya
//        else{
//            holder.lastMessage.setText(chatroom.getLastMessage()); //pakai last message yang sudah diambil
//            //set keterangan waktu, kondisi jika tahunnya sama dengan tahun sekarang
//            if(Calendar.getInstance().get(Calendar.YEAR) == Integer.parseInt(new SimpleDateFormat("yyyy").format(chatroom.getDate()))){
//                //kondisi jika harinya adalah hari ini, tulis jam dan menitnya saja
//                if(Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(chatroom.getDate())) &&
//                        Calendar.getInstance().get(Calendar.DATE) == Integer.parseInt(new SimpleDateFormat("dd").format(chatroom.getDate()))){
//                    holder.date.setText(new SimpleDateFormat("HH:mm").format(chatroom.getDate()));
//                }
//                //jika kemarin, tulis kemarin
//                else if(Calendar.getInstance().get(Calendar.MONTH)+1 == Integer.parseInt(new SimpleDateFormat("MM").format(chatroom.getDate())) &&
//                        Calendar.getInstance().get(Calendar.DATE)-1 == Integer.parseInt(new SimpleDateFormat("dd").format(chatroom.getDate()))){
//                    holder.date.setText("Kemarin");
//                }
//                //jika selain itu, tulis tanggal dan bulan
//                else{
//                    holder.date.setText(new SimpleDateFormat("dd/MM").format(chatroom.getDate()));
//                }
//            }
//            //kondisi jika tahun yang sudah lewat, tulis nama tahunnya saja
//            else{
//                holder.date.setText(new SimpleDateFormat("yyyy").format(chatroom.getDate()));
//            }
//        }
//
//        //jika salah satu chatroom dipilih, buka isi chatroomnya
//        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(_context, ChatActivity.class);
//                intent.putExtra("from", "chatlist");
//                intent.putExtra("company", chatroom.getCompanyId());
//                intent.putExtra("chatroom", chatroom.getRoomId());
//                if(chatroom.isEmpty()){
//                    intent.putExtra("userId", -1);
//                }
//                else{
//                    intent.putExtra("userId", chatroom.getUserId());
//                }
//                intent.putExtra("companyName", chatroom.getCompanyName());
//                _context.startActivity(intent);
//            }
//        });


    }

    @Override
    public int getItemCount() {
        return chatroomList.size();
    }
}
