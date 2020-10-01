package com.enseval.gcmuser.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.enseval.gcmuser.R;

/**Dialog untuk loading (gif)*/
public class LoadingDialog {

    Activity activity = null;
    Context context = null;
    Dialog dialog;
    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public LoadingDialog(Context context) {
        this.context = context;
    }

    public void showDialog() {
        if(activity!=null){
            dialog = new Dialog(activity);
        }
        else {
            dialog = new Dialog(context);
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loading_dialog);

        ImageView gifImageView = dialog.findViewById(R.id.custom_loading_imageView);

        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(gifImageView);

        if(activity!=null){
            Glide.with(activity)
                    .load(R.drawable.coba)
                    .into(imageViewTarget);
        }
        else{
            Glide.with(context)
                    .load(R.drawable.coba)
                    .into(imageViewTarget);
        }

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
