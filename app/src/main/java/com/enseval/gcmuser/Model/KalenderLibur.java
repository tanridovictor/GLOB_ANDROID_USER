package com.enseval.gcmuser.Model;

public class KalenderLibur {
    private int id;
    private String tanggal;
    private String keterangan;

    public KalenderLibur(int id, String tanggal, String keterangan) {
        this.id = id;
        this.tanggal = tanggal;
        this.keterangan = keterangan;
    }

    public int getId() {
        return id;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getKeterangan() {
        return keterangan;
    }
}
