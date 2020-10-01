package com.enseval.gcmuser.Model.NotifFirebase;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NotificationBody {
    @SerializedName("data")
    private Data data;

    @SerializedName("registration_ids")
    private ArrayList<String> listto;

    public NotificationBody(ArrayList<String> listto, Data data) {
        this.listto = listto;
        this.data = data;
    }
}
