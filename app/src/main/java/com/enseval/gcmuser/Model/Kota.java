package com.enseval.gcmuser.Model;

public class Kota {
    private String idKota;
    private String idProvinsi;
    private String namaKota;

    public Kota(String idKota, String idProvinsi, String namaKota) {
        this.idKota = idKota;
        this.idProvinsi = idProvinsi;
        this.namaKota = namaKota;
    }

    public String getIdKota() {
        return idKota;
    }

    public String getIdProvinsi() {
        return idProvinsi;
    }

    public String getNamaKota() {
        return namaKota;
    }
}
