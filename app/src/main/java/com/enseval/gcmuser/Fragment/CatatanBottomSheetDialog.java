package com.enseval.gcmuser.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.enseval.gcmuser.Activity.CheckoutActivity;
import com.enseval.gcmuser.R;

public class CatatanBottomSheetDialog extends BottomSheetDialogFragment {

    private EditText etCatatan;
    private Button btnCatatan;
    private int idCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.catatan_bottom_sheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCatatan = view.findViewById(R.id.etCatatan);
        btnCatatan = view.findViewById(R.id.btnCatatan);

        idCart = getArguments().getInt("idCart");

        btnCatatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tambahCatatan(idCart);
                dismiss();
                
            }
        });
    }

    private void tambahCatatan(int id){
        String sql = "update gcm_master_cart set note = '"+etCatatan.getText().toString()+"' where id = "+id;
        Log.d("ido", "tambahCatatan: "+sql);
    }
}
