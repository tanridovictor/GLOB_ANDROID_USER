package com.enseval.gcmuser.Model;

public class Rekening {
    private int id;
    private String no_rekening;
    private String pemilik_rekening;
    private String nama;

    public Rekening(int id, String no_rekening, String pemilik_rekening, String nama) {
        this.id = id;
        this.no_rekening = no_rekening;
        this.pemilik_rekening = pemilik_rekening;
        this.nama = nama;
    }

    public int getId() {
        return id;
    }

    public String getNo_rekening() {
        return no_rekening;
    }

    public String getPemilik_rekening() {
        return pemilik_rekening;
    }

    public String getNama() {
        return nama;
    }
}
