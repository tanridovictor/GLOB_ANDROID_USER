package com.enseval.gcmuser.Model.otp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataOTP {

    @SerializedName("successCode")
    @Expose
    private String successCode;

    @SerializedName("desc")
    @Expose
    private String desc;

    @SerializedName("messageID")
    @Expose
    private String messageID;

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

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
}
