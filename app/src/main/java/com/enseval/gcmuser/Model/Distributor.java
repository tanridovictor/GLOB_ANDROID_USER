package com.enseval.gcmuser.Model;

public class Distributor {
    private int id;
    private String nama_perusahaan;
    private String status;

    public Distributor(int id, String nama_perusahaan, String status) {
        this.id = id;
        this.nama_perusahaan = nama_perusahaan;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getNama_perusahaan() {
        return nama_perusahaan;
    }

    public String getStatus() {
        return status;
    }
}
