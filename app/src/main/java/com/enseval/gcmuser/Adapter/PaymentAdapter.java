package com.enseval.gcmuser.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.enseval.gcmuser.API.JSONRequest;
import com.enseval.gcmuser.API.QueryEncryption;
import com.enseval.gcmuser.API.RetrofitClient;
import com.enseval.gcmuser.Activity.CheckoutActivity;
import com.enseval.gcmuser.Fragment.LoadingDialog;
import com.enseval.gcmuser.Model.Cart;
import com.enseval.gcmuser.Model.Company;
import com.enseval.gcmuser.Model.Payment;
import com.enseval.gcmuser.R;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    public int SelectItem = -1;
    private Context _context;
    private ArrayList<Payment> paymentList;
    private ArrayList<Cart> cartList;
    private ArrayList<Company> companyList;
    private long total;
    private float kurs;
    private LoadingDialog loadingDialog;

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final RadioButton judulPayment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            judulPayment = itemView.findViewById(R.id.rbPembayaran);
            loadingDialog = new LoadingDialog(_context);
            View.OnClickListener l = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    SelectItem = getAdapterPosition();
                    SelectItem = paymentList.get(getAdapterPosition()).getId();
                    notifyItemRangeChanged(0, paymentList.size());
                    loadingDialog.showDialog();
                    for (int i=0; i<cartList.size(); i++){
                        updatePaymentIdCart(SelectItem, cartList.get(i).getId());
                    }
                    Intent intent = new Intent(_context, CheckoutActivity.class);
                    intent.putParcelableArrayListExtra("listSeller", companyList);
                    intent.putExtra("total", total);
                    intent.putExtra("status", "perubahanPembayaran");
                    //intent.putExtra("kurs", kurs);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    _context.startActivity(intent);
                    ((Activity)_context).finish();
                    loadingDialog.hideDialog();
                }
            };
            itemView.setOnClickListener(l);
            judulPayment.setOnClickListener(l);

        }
    }

    public PaymentAdapter(Context _context, ArrayList<Payment> paymentList, ArrayList<Cart> cartList,  ArrayList<Company> companyList, long total){
        this._context = _context;
        this.paymentList = paymentList;
        this.cartList = cartList;
        this.companyList = companyList;
        this.total = total;
        this.kurs = kurs;
    }

    @NonNull
    @Override
    public PaymentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(_context).inflate(R.layout.payment_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentAdapter.ViewHolder holder, int position) {
        holder.judulPayment.setText(paymentList.get(position).getPayment_name());
        holder.judulPayment.setId(paymentList.get(position).getId());
        holder.judulPayment.setChecked(SelectItem == paymentList.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    private void updatePaymentIdCart(int payment_id, int id_cart) {
        String query = "update gcm_master_cart set payment_id='"+payment_id+"' where id="+id_cart;
        try {
            Call<JsonObject> updatePaymentIdCart = RetrofitClient
                    .getInstance()
                    .getApi()
                    .request(new JSONRequest(QueryEncryption.Encrypt(query)));
            updatePaymentIdCart.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

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
