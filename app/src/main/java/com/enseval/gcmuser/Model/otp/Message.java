package com.enseval.gcmuser.Model.otp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("nohp")
    @Expose
    private String nohp;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("otptype")
    @Expose
    private String otptype;
    @SerializedName("senddate")
    @Expose
    private String senddate;
    @SerializedName("status")
    @Expose
    private String status;

    public String getNohp() {
        return nohp;
    }

    public void setNohp(String nohp) {
        this.nohp = nohp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOtptype() {
        return otptype;
    }

    public void setOtptype(String otptype) {
        this.otptype = otptype;
    }

    public String getSenddate() {
        return senddate;
    }

    public void setSenddate(String senddate) {
        this.senddate = senddate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
