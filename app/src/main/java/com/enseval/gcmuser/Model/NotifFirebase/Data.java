package com.enseval.gcmuser.Model.NotifFirebase;

import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    public Data(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
