package com.enseval.gcmuser.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enseval.gcmuser.Activity.CatalogActivity;
import com.enseval.gcmuser.Model.Kategori;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private Context _context;
    private ArrayList<Kategori> categoryList;
    private long lastClickTime=0;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView kategori;
        private final ImageView image;
        private final CardView cardView;
        private final ConstraintLayout constCategory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            kategori = (TextView) itemView.findViewById(R.id.kategori);
            kategori.setVisibility(View.VISIBLE);
            image = (ImageView) itemView.findViewById(R.id.imageCat);
            image.setVisibility(View.VISIBLE);
            cardView = itemView.findViewById(R.id.cardCategory);
            constCategory = itemView.findViewById(R.id.constCategory);
        }
    }

    public CategoryAdapter(Context _context, ArrayList<Kategori> categoryList) {
        this._context = _context;
        this.categoryList = categoryList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.category_view, parent, false);
        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryAdapter.ViewHolder holder, final int position) {
        final String category = categoryList.get(position).getNama();
        holder.kategori.setText(category);

        //set icon (masih static)
        if(position==0){
            holder.image.setImageResource(R.drawable.medicine);
        }
        else if(position==1){
            holder.image.setImageResource(R.drawable.food);
        }
        else if (position==2){
            holder.image.setImageResource(R.drawable.makeup);
        }
        else{
            holder.image.setImageResource(R.drawable.veterinary);
        }

        //jika kategori dipilih maka pindah ke CatalogActivity
        holder.constCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
                    return;
                }
                else {
                    Intent intent = new Intent(_context, CatalogActivity.class);
                    intent.putExtra("tipe", "kategori"); //tipe intent ke CatalogActivity adalah kategori
                    intent.putExtra("kategori", categoryList.get(position)); //passing parameter kategori
                    _context.startActivity(intent);
                }
                lastClickTime=SystemClock.elapsedRealtime();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
