package com.enseval.gcmuser.Model;

public class Payment {
    private int id;
    private String payment_name;
    private String deskripsi;
    private String nama_perusahaan;

    public Payment(int id, String payment_name, String deskripsi) {
        this.id = id;
        this.payment_name = payment_name;
        this.deskripsi = deskripsi;
    }

    public Payment(String nama_perusahaan, int id, String payment_name){
        this.nama_perusahaan = nama_perusahaan;
        this.id = id;
        this.payment_name = payment_name;
    }

    public String getPayment_name() {
        return payment_name;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public int getId() {
        return id;
    }

    public String getNama_perusahaan() {
        return nama_perusahaan;
    }
}
