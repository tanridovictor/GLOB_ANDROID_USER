package com.enseval.gcmuser.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enseval.gcmuser.Activity.DetailOrderActivity;
import com.enseval.gcmuser.Fragment.OrderBottomSheetDialogFragment;
import com.enseval.gcmuser.Utilities.Currency;
import com.enseval.gcmuser.Model.Order;
import com.enseval.gcmuser.R;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class FinishedOrderAdapter extends RecyclerView.Adapter<FinishedOrderAdapter.ViewHolder> {
    private Context _context;
    private ArrayList<Order> orderList;
    private long lastClickTime=0;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvID, tvStatus, tvTotal, tvDate, tvDate2;
        private CardView btnDetail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDate2 = itemView.findViewById(R.id.tvDate2);
            btnDetail = itemView.findViewById(R.id.btnDetail);
//            tvStatusPembayaran = itemView.findViewById(R.id.tvStatusPembayaran);
//            btnPembayaran = itemView.findViewById(R.id.btnPembayaran);
        }
    }

    public FinishedOrderAdapter(Context _context, ArrayList<Order> orderList) {
        this._context = _context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public FinishedOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context)
                .inflate(R.layout.finished_order_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FinishedOrderAdapter.ViewHolder holder, final int position) {
//        holder.tvStatusPembayaran.setVisibility(View.GONE);
//        holder.btnPembayaran.setVisibility(View.GONE);
        holder.tvID.setText(String.format(orderList.get(position).getTransactionId()));
        holder.tvDate.setText(orderList.get(position).getCreateDate());
        holder.tvDate2.setText(orderList.get(position).getUpdateDate());
        holder.tvTotal.setText(Currency.getCurrencyFormat().format(orderList.get(position).getHarga_final()+orderList.get(position).getOngkir()+orderList.get(position).getHarga_final()*(orderList.get(position).getPpn_seller()/100)));
        holder.tvStatus.setText("Selesai");

//        if(orderList.get(position).getStatusPayment().equals("PAID")){
//            holder.tvStatusPembayaran.setText("Lunas");
//            holder.btnPembayaran.setVisibility(View.GONE);
//        }
//        else if(orderList.get(position).getStatusPayment().equals("UNPAID")){
//            holder.tvStatusPembayaran.setText("Belum Lunas");
//            holder.btnPembayaran.setVisibility(View.VISIBLE);
//        }

        //jika detail ditekan, buka OrderBottomSheetDialogFragment
        holder.btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_context, DetailOrderActivity.class);
                i.putExtra("status", "selesai");
                i.putExtra("transactionId", orderList.get(position).getTransactionId());
                i.putExtra("total", orderList.get(position).getHarga_final());
                i.putExtra("ongkir", orderList.get(position).getOngkir());
                i.putExtra("ppn", orderList.get(position).getPpn_seller());
                _context.startActivity(i);

//                Bundle bundle = new Bundle();
//                bundle.putString("status", "selesai");
//                bundle.putString("transactionId", orderList.get(position).getTransactionId());
//                bundle.putLong("total", orderList.get(position).getHarga_final());
//                bundle.putDouble("ongkir", orderList.get(position).getOngkir());
//                bundle.putFloat("ppn", orderList.get(position).getPpn_seller());
//                BottomSheetDialogFragment bottomSheetDialogFragment = new OrderBottomSheetDialogFragment();
//                bottomSheetDialogFragment.setArguments(bundle);
//                bottomSheetDialogFragment.show(((FragmentActivity)_context).getSupportFragmentManager(), "tes");
            }
        });

//        holder.btnPembayaran.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(SystemClock.elapsedRealtime()-lastClickTime<1000){
//                    return;
//                }
//                else {
//                    final Dialog dialog = new Dialog(_context);
//                    dialog.setCancelable(false);
//                    dialog.setContentView(R.layout.pembayaran_dialog);
//                    View divider9 = dialog.findViewById(R.id.divider9);
//                    TextView textView27 = dialog.findViewById(R.id.textView27);
//                    TextView tvTransNo = dialog.findViewById(R.id.tvTransNo);
//                    TextView tvNoRek = dialog.findViewById(R.id.tvNoRek);
//                    TextView tvDueDate = dialog.findViewById(R.id.tvDueDate);
//                    TextView tvSalin = dialog.findViewById(R.id.tvSalin);
//                    Button btnClose = dialog.findViewById(R.id.btnClose);
//                    TextView tvCall = dialog.findViewById(R.id.tvCall);
//
//                    divider9.setVisibility(View.GONE);
//                    textView27.setVisibility(View.GONE);
//
//                    tvSalin.setPaintFlags(tvSalin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
//                    tvCall.setPaintFlags(tvCall.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
//
//                    tvSalin.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ClipboardManager clipboard = (ClipboardManager) _context.getSystemService(CLIPBOARD_SERVICE);
//                            ClipData clipData = ClipData.newPlainText("noRek", "8990407721");
//                            clipboard.setPrimaryClip(clipData);
//                            Toast.makeText(_context, "Nomor rekening telah disalin", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    tvCall.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if(!doesUserHavePermission()){
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    ActivityCompat.requestPermissions((Activity) _context, new String[]{Manifest.permission.CALL_PHONE}, 2);
//                                }
//                            }
//                            else{
//                                Intent i = new Intent(Intent.ACTION_DIAL);
//                                String p = "tel:" + "081310695040";
//                                i.setData(Uri.parse(p));
//                                _context.startActivity(i);
//                            }
//                        }
//                    });
//
//                    tvTransNo.setText("Transaksi No. #"+orderList.get(position).getTransactionId());
//                    tvDueDate.setText("Apabila sudah melakukan pembayaran, harap hubungi sales kami di 081310695040");
//
//                    btnClose.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    dialog.show();
//                }
//                lastClickTime=SystemClock.elapsedRealtime();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

//    private boolean doesUserHavePermission()
//    {
//        int result = _context.checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE);
//        return result == PackageManager.PERMISSION_GRANTED;
//    }
}
