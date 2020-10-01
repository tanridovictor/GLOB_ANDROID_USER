package com.enseval.gcmuser.Model.otp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelMessageID {

    @SerializedName("messageID")
    @Expose
    private String messageID;

    @SerializedName("userid")
    @Expose
    private String userid;

    @SerializedName("key")
    @Expose
    private String key;

    @Override
    public String toString() {
        return "Post{" +
                "messageID='" + messageID + '\'' +
                ", userid='" + userid + '\'' +
                ", key =" + key + '\'' +
                '}';
    }
}
