package com.enseval.gcmuser.Adapter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.R;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class PagerAdapter extends android.support.v4.view.PagerAdapter {
    Context context;
    ArrayList<String> listPager;
    LayoutInflater inflater;
    public PagerAdapter(Context context, ArrayList<String> listPager) {
        this.context = context;
        this.listPager = listPager;
        inflater = ((Activity)context).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return listPager.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == ((View)o);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager)container).removeView((View) object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.pembayaran_dialog, container, false);
        TextView tvidtrans = (TextView) view.findViewById(R.id.tvTransNo);
        Button btnClose = (Button) view.findViewById(R.id.btnClose);
        TextView tvSalin = (TextView) view.findViewById(R.id.tvSalin);
        TextView tvCall = (TextView) view.findViewById(R.id.tvCall);
        view.setTag(position);
        tvSalin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("noRek", "8990407721"); //salin nomor
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(context, "Nomor rekening telah disalin", Toast.LENGTH_SHORT).show();
            }
        });

        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(!doesUserHavePermission()){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, 2);
                        }
                    }
                    else{
                        Intent i = new Intent(Intent.ACTION_DIAL);
                        String p = "tel:" + "081310695040";
                        i.setData(Uri.parse(p));
                        context.startActivity(i); //pindah ke telepon, sudah berisi nomor telpnya
                    }
            }
        });
        ((ViewPager) container).addView(view);
        String idTrans = listPager.get(position);
        tvidtrans.setText("Transaksi #"+idTrans+" berhasil dibuat!");
        btnClose.setVisibility(View.GONE);
        return view;
    }
    private boolean doesUserHavePermission()
    {
        int result = context.checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
