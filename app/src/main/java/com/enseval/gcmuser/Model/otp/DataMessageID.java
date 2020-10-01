package com.enseval.gcmuser.Model.otp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataMessageID {
    @SerializedName("successCode")
    @Expose
    private String successCode;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("message")
    @Expose
    private Message message;

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
