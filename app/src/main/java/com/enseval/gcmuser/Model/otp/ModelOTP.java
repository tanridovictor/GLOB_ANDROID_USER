package com.enseval.gcmuser.Model.otp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelOTP {
    @SerializedName("otptype")
    @Expose
    String otptype;

    @SerializedName("nohp")
    @Expose
    String nohp;

    @SerializedName("message")
    @Expose
    String message;

    @SerializedName("userid")
    @Expose
    String userid;

    @SerializedName("key")
    @Expose
    String key;

    @Override
    public String toString() {
        return "Post{" +
                "otptype='" + otptype + '\'' +
                ", nohp='" + nohp + '\'' +
                ", message=" + message + '\'' +
                ", userid =" + userid + '\'' +
                ", key =" + key + '\'' +
                '}';
    }
}
