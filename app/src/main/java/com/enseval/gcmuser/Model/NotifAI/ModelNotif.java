package com.enseval.gcmuser.Model.NotifAI;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelNotif {
    @SerializedName("nego_type")
    @Expose
    private String nego_type;

    @SerializedName("timeout")
    @Expose
    private String timeout;

    @SerializedName("id_cart")
    @Expose
    private String id_cart;

    @SerializedName("company_id_buyer")
    @Expose
    private String company_id_buyer;

    @SerializedName("company_id_seller")
    @Expose
    private String company_id_seller;

    @NonNull
    @Override
    public String toString() {
        return "post{" +
                "nego_type = '" +nego_type+ '\'' +
                ", timeout = " +timeout+ '\'' +
                ", id_cart = " +id_cart+ '\'' +
                ", company_id_buyer = " +company_id_buyer+ '\'' +
                ", company_id_seller = " +company_id_seller+ '\'' +
                '}';
    }
}
