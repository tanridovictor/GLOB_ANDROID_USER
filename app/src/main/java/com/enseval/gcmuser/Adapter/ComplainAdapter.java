package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.enseval.gcmuser.Activity.ComplainActivity;
import com.enseval.gcmuser.Model.OrderDetail;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class ComplainAdapter extends RecyclerView.Adapter<ComplainAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<OrderDetail> complainList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNama, tvBatchNumber, tvJumlah;
        private ImageView imgBarang;
        private CheckBox checkBox;
        private ConstraintLayout constraintLayout;
        private RadioGroup radioGroup;
        private EditText editText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvBatchNumber = itemView.findViewById(R.id.tvBatchNumber);
            tvJumlah = itemView.findViewById(R.id.tvJumlah);
            imgBarang = itemView.findViewById(R.id.imageView);
            checkBox = itemView.findViewById(R.id.checkBox2);
            constraintLayout = itemView.findViewById(R.id.constraintlayout);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            editText = itemView.findViewById(R.id.editText);
        }
    }

    public ComplainAdapter(Context _context, ArrayList<OrderDetail> complainList) {
        this._context = _context;
        this.complainList = complainList;
    }


    @NonNull
    @Override
    public ComplainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.complain_barang_input_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ComplainAdapter.ViewHolder holder, final int position) {
        Glide.with(_context).load(complainList.get(position).getFotoUrl()).into(holder.imgBarang);
        holder.tvNama.setText(complainList.get(position).getNamaBarang());
        holder.tvBatchNumber.setText("Batch no. "+complainList.get(position).getBatchNumber());
        holder.tvJumlah.setText(String.format("Diterima %d dari %d", complainList.get(position).getQtyDiterima(), complainList.get(position).getQty()));
        //pemilihan alasan komplain
        holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioBarang:
                        ComplainActivity.setJenisKomplain(position, "BARANG");
                        break;
                    case R.id.radioJumlah:
                        ComplainActivity.setJenisKomplain(position, "QTY");
                        break;
                }
            }
        });

        //jika catatan sudah diisi, ubah value pada model
        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ComplainActivity.setCatatan(position, s.toString().trim());
            }
        });

    }

    @Override
    public int getItemCount() {
        return complainList.size();
    }
}
